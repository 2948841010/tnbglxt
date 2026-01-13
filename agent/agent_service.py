"""
Agent服务 - 整合DeepSeek API和MCP工具调用
支持多专家 Plan and Execute 模式
"""

import json
import logging
from typing import Dict, List, Any, Optional, Tuple
from datetime import datetime
import mysql.connector
from deepseek_client import DeepSeekClient
from mcp_client import MCPClient
from session_manager import SessionManager
from config import MYSQL_CONFIG
from expert_system import Planner, Executor

logger = logging.getLogger(__name__)

class AgentService:
    """Agent服务类"""
    
    def __init__(self, use_expert_mode: bool = True):
        self.deepseek_client = DeepSeekClient()
        self.mcp_client = MCPClient()
        self.session_manager = SessionManager()
        # 用于跟踪thinking标签状态
        self._in_thinking = False
        self._thinking_complete = False
        
        # 多专家模式 - 默认启用 (Plan-and-Execute + ReAct 混合架构)
        self.use_expert_mode = use_expert_mode
        if use_expert_mode:
            self.planner = Planner(self.deepseek_client)
            self.executor = Executor(self.deepseek_client, self.mcp_client, planner=self.planner)
            logger.info("🤖 多专家模式已启用 (Plan-and-Execute + ReAct 混合架构)")
        else:
            logger.info("📝 使用标准单Agent模式")
        
    async def get_user_info(self, user_id: int) -> Optional[Dict[str, Any]]:
        """
        获取用户信息
        
        Args:
            user_id: 用户ID
            
        Returns:
            用户信息字典
        """
        try:
            mysql_conn = mysql.connector.connect(**MYSQL_CONFIG)
            cursor = mysql_conn.cursor(dictionary=True)
            
            cursor.execute("""
                SELECT id, username, real_name, email, phone, gender, user_type, status
                FROM sys_user 
                WHERE id = %s AND is_deleted = 0
            """, (user_id,))
            
            user_info = cursor.fetchone()
            
            cursor.close()
            mysql_conn.close()
            
            return user_info
            
        except Exception as e:
            logger.error(f"获取用户信息失败: {e}")
            return None
    
    async def process_message(self, user_id: int, message: str) -> Dict[str, Any]:
        """
        处理用户消息
        
        Args:
            user_id: 用户ID
            message: 用户消息
            
        Returns:
            处理结果
        """
        try:
            # 获取用户信息
            user_info = await self.get_user_info(user_id)
            if not user_info:
                return {
                    "success": False,
                    "error": "用户不存在或已被删除"
                }
            
            # 获取或创建会话（MongoDB + Redis）
            session_id = await self.session_manager.get_or_create_session(user_id, user_info)
            logger.info(f"📋 当前会话ID: {session_id}")
            
            # 保存用户消息
            await self.session_manager.add_message(user_id, "user", message)
            
            # 获取对话历史
            conversation_history = await self.session_manager.get_conversation_history(user_id, limit=10)
            
            # 构建对话消息
            messages = [self.deepseek_client.build_system_message(user_info)]
            
            # 添加历史对话（排除系统消息）
            for msg in conversation_history:
                if msg["role"] != "system":
                    messages.append({
                        "role": msg["role"],
                        "content": msg["content"]
                    })
            
            # 添加当前用户消息
            messages.append({
                "role": "user", 
                "content": message
            })
            
            # 获取MCP工具schema
            tool_schemas = self.mcp_client.get_tool_schemas()
            formatted_tools = self.deepseek_client.format_tools_for_api(tool_schemas)
            
            # 调用DeepSeek API
            response = await self.deepseek_client.chat_completion(
                messages=messages,
                tools=formatted_tools
            )
            
            if not response["success"]:
                return {
                    "success": False,
                    "error": f"AI调用失败: {response.get('error')}"
                }
            
            ai_message = response["message"]
            
            # 处理工具调用
            if ai_message.get("tool_calls"):
                tool_results = []
                
                for tool_call in ai_message["tool_calls"]:
                    tool_name = tool_call["function"]["name"]
                    try:
                        tool_args = json.loads(tool_call["function"]["arguments"])
                    except json.JSONDecodeError:
                        tool_args = {}
                    
                    # 调用MCP工具
                    tool_result = await self.mcp_client.call_tool(tool_name, tool_args)
                    tool_results.append({
                        "tool_call_id": tool_call["id"],
                        "tool_name": tool_name,
                        "result": tool_result
                    })
                
                # 如果有工具调用，需要再次调用AI来生成最终回复
                
                # ⭐ 重要：提取第一次assistant消息中的thinking内容，稍后与最终回复合并保存
                first_content = ai_message["content"] or ""
                first_thinking_content = ""
                if first_content:
                    logger.info(f"📝 提取第一次消息中的thinking内容，消息长度: {len(first_content)}")
                    first_thinking_content, _ = self._extract_thinking_from_complete_content(first_content)
                    if first_thinking_content:
                        logger.info(f"✅ 提取到thinking内容，长度: {len(first_thinking_content)}")
                
                # 添加工具调用消息到对话历史
                messages.append({
                    "role": "assistant",
                    "content": first_content,
                    "tool_calls": ai_message["tool_calls"]
                })
                
                # 添加工具结果
                for tool_result in tool_results:
                    messages.append({
                        "role": "tool",
                        "tool_call_id": tool_result["tool_call_id"],
                        "content": json.dumps(tool_result["result"], ensure_ascii=False)
                    })
                
                # 再次调用AI获取最终回复
                logger.info(f"准备进行最终AI调用，消息数量: {len(messages)}")
                final_response = await self.deepseek_client.chat_completion(
                    messages=messages,
                    tools=formatted_tools
                )
                
                logger.info(f"最终AI调用结果: success={final_response.get('success')}")
                
                if final_response["success"]:
                    ai_message = final_response["message"]
                    response_content = ai_message.get("content", "")
                    
                    logger.info(f"最终回复内容长度: {len(response_content) if response_content else 0}")
                    
                    # 检查回复内容
                    if not response_content or response_content.strip() == "":
                        logger.warning("AI返回了空的回复内容，生成备用回复")
                        # 生成备用分析回复
                        response_content = self._generate_fallback_analysis(tool_results)
                    
                    # 🔄 合并保存：将第一次消息的thinking与最终回复合并
                    logger.info(f"💾 合并保存完整回复，内容长度: {len(response_content)}")
                    
                    # 检查最终回复中是否也有thinking（罕见情况）
                    final_thinking_content, clean_final_content = self._extract_thinking_from_complete_content(response_content)
                    
                    # 合并thinking内容：优先使用第一次消息的thinking，补充最终回复的thinking
                    combined_thinking = first_thinking_content
                    if final_thinking_content:
                        logger.info(f"⚠️ 最终回复中也包含thinking，长度: {len(final_thinking_content)}")
                        if combined_thinking:
                            combined_thinking += "\n\n" + final_thinking_content
                        else:
                            combined_thinking = final_thinking_content
                    
                    # 使用最终回复的clean content
                    final_content = clean_final_content or response_content
                    
                    logger.info(f"📊 合并结果 - thinking长度: {len(combined_thinking) if combined_thinking else 0}, content长度: {len(final_content)}")
                    
                    await self.session_manager.add_message(
                        user_id, 
                        "assistant", 
                        final_content,
                        tool_results,
                        combined_thinking
                    )
                    
                    return {
                        "success": True,
                        "response": response_content,
                        "tool_calls": tool_results,
                        "usage": final_response.get("usage", {})
                    }
                else:
                    logger.error(f"AI最终回复失败: {final_response.get('error')}")
                    # 生成备用回复
                    fallback_response = self._generate_fallback_analysis(tool_results)
                    
                    # 🔄 fallback情况下也需要合并thinking内容
                    logger.info(f"💾 合并保存fallback回复，内容长度: {len(fallback_response)}")
                    
                    fallback_thinking_content, clean_fallback_content = self._extract_thinking_from_complete_content(fallback_response)
                    
                    # 合并thinking内容：优先使用第一次消息的thinking
                    combined_thinking = first_thinking_content
                    if fallback_thinking_content:
                        logger.info(f"⚠️ fallback回复中也包含thinking，长度: {len(fallback_thinking_content)}")
                        if combined_thinking:
                            combined_thinking += "\n\n" + fallback_thinking_content
                        else:
                            combined_thinking = fallback_thinking_content
                    
                    logger.info(f"📊 fallback合并结果 - thinking长度: {len(combined_thinking) if combined_thinking else 0}, content长度: {len(clean_fallback_content)}")
                    
                    await self.session_manager.add_message(
                        user_id, 
                        "assistant", 
                        clean_fallback_content,
                        tool_results,
                        combined_thinking
                    )
                    
                    return {
                        "success": True,
                        "response": fallback_response,
                        "tool_calls": tool_results,
                        "usage": {}
                    }
            
            else:
                # 没有工具调用，直接返回AI回复
                thinking_content, clean_content = self._extract_thinking_from_complete_content(ai_message["content"])
                logger.info(f"💾 保存AI消息 - thinking长度: {len(thinking_content) if thinking_content else 0}, content长度: {len(clean_content)}")
                if thinking_content:
                    logger.info(f"💾 thinking内容预览: {thinking_content[:50]}...")
                await self.session_manager.add_message(user_id, "assistant", clean_content, thinking=thinking_content)
                
                return {
                    "success": True,
                    "response": ai_message["content"],
                    "tool_calls": [],
                    "usage": response.get("usage", {})
                }
                
        except Exception as e:
            logger.error(f"处理消息失败: {e}")
            return {
                "success": False,
                "error": str(e)
            }
    
    async def process_message_stream(self, user_id: int, message: str, session_id: str = None):
        """
        流式处理用户消息
        
        Args:
            user_id: 用户ID
            message: 用户消息
            session_id: 可选的会话ID，如果提供则使用指定会话，否则自动获取或创建
            
        Yields:
            流式响应数据
        """
        try:
            # 重置thinking状态
            self._in_thinking = False
            self._thinking_complete = False
            # 获取用户信息
            user_info = await self.get_user_info(user_id)
            if not user_info:
                yield {
                    "type": "error",
                    "error": "用户不存在或已被删除"
                }
                return
            
            # 获取或创建会话（与多专家模式一致）
            if session_id:
                # 如果提供了session_id，设置它为当前会话
                logger.info(f"📋 使用指定会话ID: {session_id}")
                await self.session_manager.set_current_session(user_id, session_id, user_info)
            else:
                # 否则自动获取或创建新会话
                session_id = await self.session_manager.get_or_create_session(user_id, user_info)
                logger.info(f"📋 自动获取/创建会话ID: {session_id}")
            
            # 保存用户消息
            await self.session_manager.add_message(user_id, "user", message)
            
            # 获取对话历史
            conversation_history = await self.session_manager.get_conversation_history(user_id, limit=10)
            
            # 构建对话消息
            messages = [self.deepseek_client.build_system_message(user_info)]
            
            # 添加历史对话（排除系统消息）
            for msg in conversation_history:
                if msg["role"] != "system":
                    messages.append({
                        "role": msg["role"],
                        "content": msg["content"]
                    })
            
            # 添加当前用户消息
            messages.append({
                "role": "user", 
                "content": message
            })
            
            # 获取MCP工具schema
            tool_schemas = self.mcp_client.get_tool_schemas()
            formatted_tools = self.deepseek_client.format_tools_for_api(tool_schemas)
            
            # 流式调用DeepSeek API
            accumulated_content = ""
            tool_calls = []
            
            async for chunk in self.deepseek_client.chat_completion_stream(
                messages=messages,
                tools=formatted_tools
            ):
                if chunk.get("type") == "error":
                    yield chunk
                    return
                
                elif chunk.get("type") == "content":
                    # 实时发送内容增量
                    accumulated_content = chunk["accumulated_content"]
                    
                    # 检测思考过程并单独发送
                    thinking_content, clean_content = self._extract_thinking_from_stream(
                        chunk["content"], accumulated_content
                    )
                    
                    if thinking_content:
                        yield {
                            "type": "thinking",
                            "content": thinking_content
                        }
                    
                    if clean_content:
                        yield {
                            "type": "content", 
                            "content": clean_content
                        }
                
                elif chunk.get("type") == "tool_call_start":
                    yield {
                        "type": "tool_call_start", 
                        "tool_name": chunk["tool_name"]
                    }
                
                elif chunk.get("type") == "tool_calls_complete":
                    # 处理工具调用
                    tool_calls = chunk["tool_calls"]
                    yield {
                        "type": "tools_start",
                        "message": f"正在调用 {len(tool_calls)} 个工具..."
                    }
                    
                    tool_results = []
                    for tool_call in tool_calls:
                        tool_name = tool_call["function"]["name"]
                        try:
                            tool_args = json.loads(tool_call["function"]["arguments"])
                        except json.JSONDecodeError:
                            tool_args = {}
                        
                        yield {
                            "type": "tool_executing",
                            "tool_name": tool_name
                        }
                        
                        # 调用MCP工具
                        tool_result = await self.mcp_client.call_tool(tool_name, tool_args)
                        tool_results.append({
                            "tool_call_id": tool_call["id"],
                            "tool_name": tool_name,
                            "result": tool_result
                        })
                        
                        yield {
                            "type": "tool_complete",
                            "tool_name": tool_name,
                            "result": tool_result
                        }
                    
                    # 添加工具调用消息到对话历史
                    messages.append({
                        "role": "assistant",
                        "content": accumulated_content or "",
                        "tool_calls": tool_calls
                    })
                    
                    # 添加工具结果
                    for tool_result in tool_results:
                        messages.append({
                            "role": "tool",
                            "tool_call_id": tool_result["tool_call_id"],
                            "content": json.dumps(tool_result["result"], ensure_ascii=False)
                        })
                    
                    yield {
                        "type": "final_response_start",
                        "message": "正在生成最终回复..."
                    }
                    
                    # 递归处理可能的多轮工具调用
                    final_tool_results = tool_results
                    final_messages = messages.copy()
                    max_tool_rounds = 100  # 最多允许100轮工具调用（实际上几乎不受限制）
                    tool_round = 0
                    
                    while tool_round < max_tool_rounds:
                        tool_round += 1
                        logger.info(f"🔄 工具调用轮次: {tool_round}")
                        
                        # 再次流式调用AI获取最终回复
                        final_content = ""
                        final_thinking_buffer = ""  # 累积thinking内容
                        final_thinking_complete = False
                        final_in_thinking = False
                        has_more_tools = False
                        
                        async for final_chunk in self.deepseek_client.chat_completion_stream(
                            messages=final_messages,
                            tools=formatted_tools
                        ):
                            if final_chunk.get("type") == "content":
                                final_content = final_chunk["accumulated_content"]
                                chunk_content = final_chunk["content"]
                                
                                # 从最终回复中分离thinking内容并实时发送
                                thinking_part, clean_part = self._extract_thinking_from_final_stream(
                                    chunk_content, final_thinking_buffer, final_thinking_complete, final_in_thinking
                                )
                                
                                # 更新状态
                                if thinking_part["thinking_content"]:
                                    final_thinking_buffer += thinking_part["thinking_content"]
                                    yield {
                                        "type": "thinking",
                                        "content": thinking_part["thinking_content"]
                                    }
                                
                                final_thinking_complete = thinking_part["thinking_complete"]
                                final_in_thinking = thinking_part["in_thinking"]
                                
                                # 只发送非thinking的内容部分
                                if clean_part:
                                    yield {
                                        "type": "final_content",
                                        "content": clean_part
                                    }
                            
                            elif final_chunk.get("type") == "tool_calls_complete":
                                # 检测到更多工具调用
                                has_more_tools = True
                                additional_tool_calls = final_chunk["tool_calls"]
                                
                                logger.info(f"🔄 第{tool_round}轮检测到更多工具调用: {len(additional_tool_calls)}个")
                                
                                yield {
                                    "type": "tools_start",
                                    "message": f"继续调用 {len(additional_tool_calls)} 个工具..."
                                }
                                
                                # 执行额外的工具调用
                                additional_results = []
                                for tool_call in additional_tool_calls:
                                    tool_name = tool_call["function"]["name"]
                                    try:
                                        tool_args = json.loads(tool_call["function"]["arguments"])
                                    except json.JSONDecodeError:
                                        tool_args = {}
                                    
                                    yield {
                                        "type": "tool_executing",
                                        "tool_name": tool_name
                                    }
                                    
                                    # 调用MCP工具
                                    tool_result = await self.mcp_client.call_tool(tool_name, tool_args)
                                    additional_results.append({
                                        "tool_call_id": tool_call["id"],
                                        "tool_name": tool_name,
                                        "result": tool_result
                                    })
                                    
                                    yield {
                                        "type": "tool_complete",
                                        "tool_name": tool_name,
                                        "result": tool_result
                                    }
                                
                                # 更新工具结果和消息历史
                                final_tool_results.extend(additional_results)
                                
                                # 添加到消息历史
                                final_messages.append({
                                    "role": "assistant",
                                    "content": final_content or "",
                                    "tool_calls": additional_tool_calls
                                })
                                
                                for tool_result in additional_results:
                                    final_messages.append({
                                        "role": "tool",
                                        "tool_call_id": tool_result["tool_call_id"],
                                        "content": json.dumps(tool_result["result"], ensure_ascii=False)
                                    })
                                
                                break  # 跳出内层循环，继续下一轮
                            
                            elif final_chunk.get("type") == "complete":
                                final_content = final_chunk["content"]
                                break
                            
                            elif final_chunk.get("type") == "error":
                                # 如果最终回复失败，生成备用回复
                                final_content = self._generate_fallback_analysis(final_tool_results)
                                yield {
                                    "type": "final_content",
                                    "content": final_content
                                }
                                break
                        
                        # 如果没有更多工具调用，结束循环
                        if not has_more_tools:
                            break
                    
                    # 保存AI回复 - 需要合并所有thinking内容
                    # 1. 从第一次消息中提取thinking（工具调用前的思考）
                    first_thinking_content, _ = self._extract_thinking_from_complete_content(accumulated_content or "")
                    
                    # 2. 从最终回复中提取thinking（工具调用后的思考）
                    final_thinking_content, clean_final_content = self._extract_thinking_from_complete_content(final_content)
                    
                    # 3. 合并所有thinking内容
                    combined_thinking = ""
                    if first_thinking_content:
                        combined_thinking = first_thinking_content
                        logger.info(f"🧠 第一次thinking长度: {len(first_thinking_content)}")
                    
                    if final_thinking_content:
                        if combined_thinking:
                            combined_thinking += "\n\n" + final_thinking_content
                        else:
                            combined_thinking = final_thinking_content
                        logger.info(f"🧠 最终thinking长度: {len(final_thinking_content)}")
                    
                    # 4. 使用最终的clean content
                    final_clean_content = clean_final_content or final_content
                    
                    logger.info(f"🔄 WebSocket工具流程完成 - 工具轮次: {tool_round}, 总工具数: {len(final_tool_results)}, thinking长度: {len(combined_thinking) if combined_thinking else 0}")
                    
                    await self.session_manager.add_message(
                        user_id, 
                        "assistant", 
                        final_clean_content,
                        final_tool_results,
                        combined_thinking
                    )
                    
                    yield {
                        "type": "complete",
                        "final_response": final_content,
                        "tool_calls": final_tool_results
                    }
                    return
                
                elif chunk.get("type") == "complete":
                    # 没有工具调用，直接完成
                    final_content = chunk["content"]
                    thinking_content, clean_content = self._extract_thinking_from_complete_content(final_content)
                    logger.info(f"🏁 流式完成 - thinking长度: {len(thinking_content) if thinking_content else 0}, content长度: {len(clean_content)}")
                    if thinking_content:
                        logger.info(f"🏁 thinking内容预览: {thinking_content[:50]}...")
                    await self.session_manager.add_message(user_id, "assistant", clean_content, thinking=thinking_content)
                    
                    yield {
                        "type": "complete",
                        "final_response": final_content,
                        "tool_calls": []
                    }
                    return
                    
        except Exception as e:
            logger.error(f"流式处理消息失败: {e}")
            yield {
                "type": "error",
                "error": str(e)
            }
    
    def _generate_fallback_analysis(self, tool_results: List[Dict[str, Any]]) -> str:
        """
        生成备用分析回复（当AI调用失败时使用）
        
        Args:
            tool_results: 工具调用结果列表
            
        Returns:
            备用分析回复文本
        """
        try:
            analysis_parts = []
            action_type = "query"  # 默认为查询类型
            
            for tool_result in tool_results:
                tool_name = tool_result.get("tool_name", "")
                result_data = tool_result.get("result", {})
                
                if not result_data.get("success", False):
                    continue
                
                data = result_data.get("data", {})
                
                if tool_name == "query_user_health_records":
                    analysis_parts.append(self._analyze_health_records(data))
                elif tool_name == "add_health_record":
                    action_type = "add"  # 标记为添加操作
                    analysis_parts.append(self._analyze_added_record(data))
                elif tool_name == "query_doctor_list":
                    analysis_parts.append(self._analyze_doctor_list(data))
                else:
                    # 通用数据分析
                    if isinstance(data, dict) and data:
                        analysis_parts.append(f"📊 {tool_name}查询结果：\n{self._format_data_summary(data)}")
            
            if analysis_parts:
                if action_type == "add":
                    # 对于添加操作，使用简洁的回复
                    return "\n\n".join(analysis_parts)
                else:
                    # 对于查询操作，使用分析性回复
                    return "根据数据查询结果，我为您进行以下分析：\n\n" + "\n\n".join(analysis_parts)
            else:
                return "操作已完成，请查看上方的结果。"
                
        except Exception as e:
            logger.error(f"生成备用分析失败: {e}")
            return "数据查询完成，请查看上方的查询结果。"
    
    def _analyze_health_records(self, data: Dict[str, Any]) -> str:
        """分析健康记录数据"""
        try:
            if not data or "records" not in data:
                return "📊 健康记录查询完成，但未找到相关数据。"
            
            records = data["records"]
            total_count = data.get("total_count", 0)
            
            if not records:
                return f"📊 健康记录分析：目前共有{total_count}条记录，但查询时间范围内无数据。建议扩大查询时间范围。"
            
            analysis = f"📊 **健康记录分析**（共{total_count}条记录）：\n\n"
            
            # 按类型分组分析
            glucose_records = [r for r in records if r.get("type") == "glucose"]
            pressure_records = [r for r in records if r.get("type") == "pressure"]
            weight_records = [r for r in records if r.get("type") == "weight"]
            
            if glucose_records:
                latest_glucose = glucose_records[0]
                value = latest_glucose.get("value", 0)
                measure_type = latest_glucose.get("measureType", "")
                
                analysis += f"🩸 **血糖情况**：\n"
                analysis += f"- 最新记录：{value} mmol/L ({measure_type})\n"
                
                if value < 3.9:
                    analysis += "- ⚠️ 血糖偏低，建议及时补充葡萄糖\n"
                elif value > 7.8:
                    analysis += "- ⚠️ 血糖偏高，建议注意饮食控制\n"
                else:
                    analysis += "- ✅ 血糖水平正常\n"
                
                analysis += f"- 记录数量：{len(glucose_records)}条\n\n"
            
            if pressure_records:
                analysis += f"💓 **血压记录**：{len(pressure_records)}条记录\n\n"
            
            if weight_records:
                analysis += f"⚖️ **体重记录**：{len(weight_records)}条记录\n\n"
            
            analysis += "💡 **建议**：保持规律监测，如有异常请及时咨询医生。"
            
            return analysis
            
        except Exception as e:
            logger.error(f"分析健康记录失败: {e}")
            return "📊 健康记录查询完成，请查看上方数据详情。"
    
    def _analyze_added_record(self, data: Dict[str, Any]) -> str:
        """分析新添加的健康记录"""
        try:
            if not data or not data.get("success"):
                return "❌ 健康记录添加失败。"
            
            record_details = data.get("record_details", {})
            record_type = record_details.get("type", "")
            record_data = record_details.get("data", {})
            
            if record_type == "glucose":
                value = record_data.get("value", 0)
                measure_type = record_data.get("measureType", "")
                return f"✅ 血糖记录添加成功：{value} mmol/L ({measure_type})"
            elif record_type == "pressure":
                systolic = record_data.get("systolic", 0)
                diastolic = record_data.get("diastolic", 0)
                return f"✅ 血压记录添加成功：{systolic}/{diastolic} mmHg"
            elif record_type == "weight":
                weight = record_data.get("weight", record_data.get("value", 0))
                return f"✅ 体重记录添加成功：{weight} kg"
            else:
                return f"✅ 健康记录添加成功"
                
        except Exception as e:
            logger.error(f"分析新增记录失败: {e}")
            return "✅ 健康记录已添加"
    
    def _analyze_doctor_list(self, data: Dict[str, Any]) -> str:
        """分析医生列表数据"""
        try:
            doctors = data.get("doctors", [])
            if not doctors:
                return "📋 医生查询完成，但当前无可用医生。"
            
            available_count = len([d for d in doctors if d.get("is_online")])
            total_count = len(doctors)
            
            return f"👨‍⚕️ **医生列表分析**：\n- 共找到{total_count}位医生\n- 当前在线：{available_count}位\n- 建议选择在线医生进行咨询"
            
        except Exception as e:
            logger.error(f"分析医生列表失败: {e}")
            return "👨‍⚕️ 医生列表查询完成。"
    
    def _format_data_summary(self, data: Dict[str, Any]) -> str:
        """格式化数据摘要"""
        try:
            if isinstance(data, dict):
                summary_parts = []
                for key, value in data.items():
                    if isinstance(value, (list, dict)):
                        summary_parts.append(f"- {key}: {len(value) if isinstance(value, list) else '复杂对象'}项")
                    else:
                        summary_parts.append(f"- {key}: {value}")
                return "\n".join(summary_parts[:5])  # 限制显示前5项
            else:
                return str(data)[:200] + "..." if len(str(data)) > 200 else str(data)
                
        except Exception as e:
            logger.error(f"格式化数据摘要失败: {e}")
            return "数据格式复杂，请查看详细结果。"
    
    def _extract_thinking_from_stream(self, chunk_content: str, accumulated_content: str) -> Tuple[str, str]:
        """
        从流式内容中提取思考过程
        
        Args:
            chunk_content: 当前chunk的内容
            accumulated_content: 累积的内容
            
        Returns:
            tuple: (thinking_content, clean_content)
        """
        thinking_content = ""
        clean_content = ""
        
        try:
            # 如果之前没有完成thinking，继续检测
            if not self._thinking_complete:
                # 检测thinking开始标签
                if not self._in_thinking and "<thinking>" in chunk_content:
                    self._in_thinking = True
                    # 提取thinking开始后的内容
                    thinking_start = chunk_content.find("<thinking>") + len("<thinking>")
                    remaining_content = chunk_content[thinking_start:]
                    
                    # 检查是否在同一chunk中结束
                    if "</thinking>" in remaining_content:
                        thinking_end = remaining_content.find("</thinking>")
                        thinking_content = remaining_content[:thinking_end]
                        self._thinking_complete = True
                        self._in_thinking = False
                        
                        # 清理后的内容是</thinking>之后的部分
                        clean_content = remaining_content[thinking_end + len("</thinking>"):]
                        return thinking_content, clean_content
                    else:
                        # thinking跨多个chunk，立即返回当前部分
                        thinking_content = remaining_content
                        # 不返回普通内容，因为都在thinking中
                        return thinking_content, ""
                
                # 如果正在thinking中
                elif self._in_thinking:
                    if "</thinking>" in chunk_content:
                        # thinking结束
                        thinking_end = chunk_content.find("</thinking>")
                        thinking_part = chunk_content[:thinking_end]
                        self._thinking_complete = True
                        self._in_thinking = False
                        
                        # 清理后的内容是</thinking>之后的部分
                        clean_content = chunk_content[thinking_end + len("</thinking>"):]
                        return thinking_part, clean_content
                    else:
                        # 继续返回thinking内容的当前chunk
                        thinking_content = chunk_content
                        return thinking_content, ""
                
                # 没有thinking标签，正常返回内容
                else:
                    return "", chunk_content
            
            # thinking已完成，正常返回内容
            else:
                return "", chunk_content
                
        except Exception as e:
            logger.error(f"提取thinking失败: {e}")
            # 出错时返回原始内容
            return "", chunk_content
    
    def _extract_thinking_from_final_stream(self, chunk_content: str, thinking_buffer: str, 
                                          thinking_complete: bool, in_thinking: bool) -> tuple[Dict[str, Any], str]:
        """
        从最终回复流中提取thinking内容（工具调用后阶段专用）
        
        Args:
            chunk_content: 当前chunk的内容
            thinking_buffer: 累积的thinking内容
            thinking_complete: thinking是否已完成
            in_thinking: 是否在thinking中
            
        Returns:
            tuple: (thinking_info_dict, clean_content)
        """
        result_thinking = {
            "thinking_content": "",
            "thinking_complete": thinking_complete,
            "in_thinking": in_thinking
        }
        
        try:
            # 如果thinking还没完成，继续检测
            if not thinking_complete:
                # 检测thinking开始标签
                if not in_thinking and "<thinking>" in chunk_content:
                    result_thinking["in_thinking"] = True
                    # 提取thinking开始后的内容
                    thinking_start = chunk_content.find("<thinking>") + len("<thinking>")
                    remaining_content = chunk_content[thinking_start:]
                    
                    # 检查是否在同一chunk中结束
                    if "</thinking>" in remaining_content:
                        thinking_end = remaining_content.find("</thinking>")
                        result_thinking["thinking_content"] = remaining_content[:thinking_end]
                        result_thinking["thinking_complete"] = True
                        result_thinking["in_thinking"] = False
                        
                        # 清理后的内容是</thinking>之后的部分
                        clean_content = remaining_content[thinking_end + len("</thinking>"):]
                        return result_thinking, clean_content
                    else:
                        # thinking跨多个chunk
                        result_thinking["thinking_content"] = remaining_content
                        return result_thinking, ""
                
                # 如果正在thinking中
                elif in_thinking:
                    if "</thinking>" in chunk_content:
                        # thinking结束
                        thinking_end = chunk_content.find("</thinking>")
                        result_thinking["thinking_content"] = chunk_content[:thinking_end]
                        result_thinking["thinking_complete"] = True
                        result_thinking["in_thinking"] = False
                        
                        # 清理后的内容是</thinking>之后的部分
                        clean_content = chunk_content[thinking_end + len("</thinking>"):]
                        return result_thinking, clean_content
                    else:
                        # 继续返回thinking内容的当前chunk
                        result_thinking["thinking_content"] = chunk_content
                        return result_thinking, ""
                
                # 没有thinking标签，正常返回内容
                else:
                    return result_thinking, chunk_content
            
            # thinking已完成，正常返回内容
            else:
                return result_thinking, chunk_content
                
        except Exception as e:
            logger.error(f"提取最终回复thinking失败: {e}")
            # 出错时返回原始内容
            return result_thinking, chunk_content
    
    def _extract_thinking_from_complete_content(self, content: str) -> tuple[str, str]:
        """
        从完整内容中分离thinking和clean content
        
        Args:
            content: 包含thinking标签的完整内容
            
        Returns:
            tuple: (thinking_content, clean_content)
        """
        import re
        
        logger.info(f"🔍 开始提取thinking内容，原始内容长度: {len(content)}")
        logger.info(f"🔍 原始内容预览: {content[:200]}...")
        
        # 查找thinking标签
        thinking_match = re.search(r'<thinking>(.*?)</thinking>', content, re.DOTALL | re.IGNORECASE)
        
        if thinking_match:
            thinking_content = thinking_match.group(1).strip()
            # 移除thinking部分，得到干净的内容
            clean_content = re.sub(r'<thinking>.*?</thinking>', '', content, flags=re.DOTALL | re.IGNORECASE).strip()
            logger.info(f"✅ 找到thinking内容，长度: {len(thinking_content)}")
            logger.info(f"✅ thinking内容预览: {thinking_content[:100]}...")
            return thinking_content, clean_content
        else:
            logger.warning(f"❌ 未找到thinking标签，内容将作为普通content保存")
            return "", content
    
    async def get_conversation_history(self, user_id: int, limit: int = 20) -> Dict[str, Any]:
        """
        获取对话历史
        
        Args:
            user_id: 用户ID
            limit: 消息数量限制
            
        Returns:
            对话历史
        """
        try:
            user_info = await self.get_user_info(user_id)
            if not user_info:
                return {
                    "success": False,
                    "error": "用户不存在"
                }
            
            messages = await self.session_manager.get_conversation_history(user_id, limit)
            
            # 转换字段名以匹配前端期望的格式（驼峰 -> 蛇形）
            formatted_messages = []
            for msg in messages:
                formatted_msg = {
                    "role": msg.get("role"),
                    "content": msg.get("content"),
                    "timestamp": msg.get("timestamp")
                }
                
                # 转换 toolCalls -> tool_calls
                if "toolCalls" in msg:
                    formatted_msg["tool_calls"] = msg["toolCalls"]
                elif "tool_calls" in msg:
                    formatted_msg["tool_calls"] = msg["tool_calls"]
                
                # 转换 thinking 字段
                if "thinking" in msg:
                    formatted_msg["thinking"] = msg["thinking"]
                
                # 转换 expertPlan -> expert_plan
                if "expertPlan" in msg:
                    formatted_msg["expert_plan"] = msg["expertPlan"]
                elif "expert_plan" in msg:
                    formatted_msg["expert_plan"] = msg["expert_plan"]
                
                formatted_messages.append(formatted_msg)
            
            return {
                "success": True,
                "user_info": user_info,
                "messages": formatted_messages,
                "total_messages": len(formatted_messages)
            }
            
        except Exception as e:
            logger.error(f"获取对话历史失败: {e}")
            return {
                "success": False,
                "error": str(e)
            }
    
    async def clear_conversation(self, user_id: int) -> Dict[str, Any]:
        """
        清除对话历史
        
        Args:
            user_id: 用户ID
            
        Returns:
            操作结果
        """
        try:
            await self.session_manager.clear_session(user_id)
            return {
                "success": True,
                "message": "对话历史已清除"
            }
            
        except Exception as e:
            logger.error(f"清除对话历史失败: {e}")
            return {
                "success": False,
                "error": str(e)
            }
    
    async def process_message_expert_stream(self, user_id: int, message: str, session_id: str = None):
        """
        使用多专家模式流式处理用户消息
        
        Args:
            user_id: 用户ID
            message: 用户消息
            session_id: 可选的会话ID，如果提供则使用指定会话，否则自动获取或创建
            
        Yields:
            流式响应数据
        """
        try:
            # 获取用户信息
            user_info = await self.get_user_info(user_id)
            if not user_info:
                yield {
                    "type": "error",
                    "error": "用户不存在或已被删除"
                }
                return
            
            # 获取或创建会话（MongoDB + Redis）
            if session_id:
                # 如果提供了session_id，设置它为当前会话
                logger.info(f"📋 使用指定会话ID: {session_id}")
                await self.session_manager.set_current_session(user_id, session_id, user_info)
            else:
                # 否则自动获取或创建新会话
                session_id = await self.session_manager.get_or_create_session(user_id, user_info)
                logger.info(f"📋 自动获取/创建会话ID: {session_id}")
            
            # 保存用户消息
            await self.session_manager.add_message(user_id, "user", message)
            
            # 获取历史对话记录（包括刚保存的用户消息）
            conversation_history = await self.session_manager.get_conversation_history(user_id, limit=10)
            logger.info(f"📚 获取历史对话记录，共 {len(conversation_history)} 条消息")
            
            # 🎯 步骤1: 规划阶段
            yield {
                "type": "planning_start",
                "message": "🧠 正在分析问题并制定执行计划..."
            }
            
            # 传递历史对话给规划器
            plan = await self.planner.create_plan(message, user_info, conversation_history)
            
            # 获取任务列表（优先使用tasks，否则使用experts）
            tasks = plan.get("tasks", [])
            if not tasks:
                # 向后兼容：如果没有tasks，从experts创建简单任务
                expert_names = plan.get("experts", [])
                tasks = [{"expert": name, "task_description": f"执行{name}的标准职责"} for name in expert_names]
            
            yield {
                "type": "plan_created",
                "plan": plan,
                "tasks": tasks,
                "reasoning": plan.get("reasoning", ""),
                "message": f"📋 执行计划：将依次执行 {len(tasks)} 个任务"
            }
            
            # 🎯 步骤2: 准备执行上下文
            
            context = {
                "user_id": user_id,
                "user_question": message,
                "user_info": user_info,
                "conversation_history": conversation_history  # 添加历史对话记录
            }
            
            # 🎯 步骤3: 执行阶段
            expert_results = []
            
            for i, task in enumerate(tasks):
                expert_name = task.get("expert")
                task_description = task.get("task_description", "")
                
                # 发送专家开始信息（包含任务描述）
                yield {
                    "type": "expert_start",
                    "expert": expert_name,
                    "task_description": task_description,
                    "step": i + 1,
                    "total": len(tasks),
                    "message": f"🤖 [{i+1}/{len(tasks)}] 正在咨询 {expert_name}..."
                }
                
                # 获取专家并执行
                expert = self.executor.experts.get(expert_name)
                if not expert:
                    logger.warning(f"专家 {expert_name} 不存在")
                    continue
                
                # 将任务描述添加到上下文
                context["assigned_task"] = task_description
                context["task_index"] = i + 1
                context["total_tasks"] = len(tasks)
                
                # 执行专家任务
                result = await expert.process(context)
                expert_results.append(result)
                
                # 生成任务完成汇报
                completion_report = self.executor._generate_completion_report(expert_name, task_description, result)
                
                # 发送专家完成信息和结果
                if result.get("success"):
                    # 提取专家的分析内容
                    expert_content = (
                        result.get("analysis") or 
                        result.get("explanation") or 
                        result.get("recommendation") or
                        result.get("final_response") or
                        ""
                    )
                    
                    # 构建完整的专家结果，包含所有相关信息
                    expert_result_data = {
                        "success": True,
                        "content": expert_content,  # 完整内容，不截断
                        "expert_name": expert.name,
                        "expert_type": expert_name,
                        "assigned_task": task_description,  # 分配的任务
                        "completion_report": completion_report  # 完成汇报
                    }
                    
                    # 添加数据专家的数据
                    if expert_name == "DataExpert" and result.get("data"):
                        expert_result_data["data"] = result.get("data")
                        expert_result_data["mcp_tool"] = "query_user_health_records"
                    
                    # 添加知识专家的知识库内容
                    if expert_name == "KnowledgeExpert" and result.get("knowledge"):
                        expert_result_data["knowledge"] = result.get("knowledge")
                        expert_result_data["mcp_tool"] = "search_diabetes_knowledge"
                    
                    # 添加医生推荐专家的医生列表
                    if expert_name == "DoctorExpert" and result.get("doctors"):
                        expert_result_data["doctors"] = result.get("doctors")
                        expert_result_data["mcp_tool"] = "query_doctor_list"
                    
                    # 添加数据记录专家的结果
                    if expert_name == "DataRecordExpert":
                        expert_result_data["has_new_data"] = result.get("has_new_data", False)
                        expert_result_data["records_added"] = result.get("records_added", [])
                        expert_result_data["parsed_data"] = result.get("parsed_data", [])
                    
                    # 添加问诊专家的评估结果
                    if expert_name == "ConsultationExpert":
                        expert_result_data["info_sufficient"] = result.get("info_sufficient", True)
                        expert_result_data["questions"] = result.get("questions", [])
                        expert_result_data["reason"] = result.get("reason", "")
                        expert_result_data["assessment"] = result.get("assessment", {})
                        # 添加问诊专家的MCP调用结果
                        if result.get("health_records"):
                            expert_result_data["data"] = result.get("health_records")
                            expert_result_data["mcp_tool"] = "query_user_health_records"
                        if result.get("knowledge"):
                            expert_result_data["knowledge"] = result.get("knowledge")
                            expert_result_data["mcp_tool"] = "search_diabetes_knowledge"
                    
                    # 添加MCP调用详情
                    if result.get("mcp_calls"):
                        expert_result_data["mcp_calls"] = result.get("mcp_calls")
                    
                    # 添加其他可能的字段
                    for key in ["confidence", "data", "knowledge", "doctors", "info_sufficient", "questions", "reason", "has_new_data", "records_added", "mcp_calls"]:
                        if key in result and key not in expert_result_data:
                            expert_result_data[key] = result[key]
                    
                    # 准备发送的消息
                    complete_message = {
                        "type": "expert_complete",
                        "expert": expert_name,
                        "result": expert_result_data,
                        "message": f"✅ {expert.name} 分析完成"
                    }
                    
                    # 如果有 ReAct 信息，添加到消息中
                    if result.get("react_mode") and result.get("steps"):
                        complete_message["react_info"] = {
                            "iterations": result.get("iterations", 0),
                            "goal_achieved": result.get("goal_achieved", False),
                            "steps": result.get("steps", [])
                        }
                    
                    yield complete_message
                    
                    # 如果是综合专家，流式输出最终回复
                    if expert_name == "SynthesisExpert":
                        final_response = result.get("final_response", "")
                        
                        # 流式发送最终回复
                        yield {
                            "type": "final_response_start",
                            "message": "正在生成最终回复..."
                        }
                        
                        # 分块发送
                        chunk_size = 20
                        for j in range(0, len(final_response), chunk_size):
                            chunk = final_response[j:j + chunk_size]
                            yield {
                                "type": "final_content",
                                "content": chunk
                            }
                    
                else:
                    yield {
                        "type": "expert_error",
                        "expert": expert_name,
                        "error": result.get("error", "未知错误")
                    }
                
                # 更新上下文
                if expert_name == "DiagnosisExpert":
                    context["diagnosis_result"] = result
                elif expert_name == "DataExpert":
                    context["health_data"] = result.get("data", {})
                elif expert_name == "KnowledgeExpert":
                    context["knowledge"] = result.get("knowledge", {})
                elif expert_name == "DoctorExpert":
                    context["doctor_recommendation"] = result
                elif expert_name == "DataRecordExpert":
                    # 数据记录专家更新上下文
                    if result.get("has_new_data"):
                        logger.info(f"📝 用户提供了新数据，已记录到系统")
                elif expert_name == "ConsultationExpert":
                    # 🚨 关键：如果问诊专家判断信息不足，动态调整计划
                    if not result.get("info_sufficient", True):
                        logger.info(f"⚠️ 问诊专家判断信息不足，跳过其他专家，直接综合回复")
                        # 只保留综合专家
                        context["expert_results"] = expert_results
                        # 直接跳到综合专家
                        synthesis_expert = self.executor.experts.get("SynthesisExpert")
                        if synthesis_expert:
                            # 发送综合专家开始
                            yield {
                                "type": "expert_start",
                                "expert": "SynthesisExpert",
                                "step": len(expert_results) + 1,
                                "total": len(expert_results) + 1,
                                "message": "🔄 正在整合意见并生成回复..."
                            }
                            
                            # 执行综合专家
                            synthesis_result = await synthesis_expert.process(context)
                            expert_results.append(synthesis_result)
                            
                            # 发送综合专家完成
                            if synthesis_result.get("success"):
                                final_response = synthesis_result.get("final_response", "")
                                yield {
                                    "type": "expert_complete",
                                    "expert": "SynthesisExpert",
                                    "result": {
                                        "success": True,
                                        "content": final_response,
                                        "expert_name": "综合专家",
                                        "expert_type": "SynthesisExpert"
                                    },
                                    "message": "✅ 综合专家分析完成"
                                }
                                
                                # 流式输出最终回复
                                yield {"type": "final_response_start", "message": "正在生成最终回复..."}
                                chunk_size = 20
                                for j in range(0, len(final_response), chunk_size):
                                    yield {"type": "final_content", "content": final_response[j:j + chunk_size]}
                        
                        # 跳出循环，不再执行后续专家
                        break
                elif expert_name == "SynthesisExpert":
                    context["expert_results"] = expert_results[:-1]
            
            # 🎯 步骤4: 保存最终回复和专家计划
            final_result = expert_results[-1] if expert_results else {}
            final_response = final_result.get("final_response", "抱歉，暂时无法生成回复。")
            
            # 构建完整的expert_plan数据，包含每个专家的执行结果和新的任务信息
            expert_plan_data = {
                "plan": plan,
                "tasks": tasks,  # 新增：任务列表
                "reasoning": plan.get("reasoning", ""),  # 新增：计划推理
                "experts": []
            }
            
            # 收集每个专家的执行结果
            for i, result in enumerate(expert_results):
                expert_name = result.get("expert", "Unknown")
                
                # 为保持与WebSocket事件一致，提取内容并添加content字段
                expert_content = (
                    result.get("analysis") or 
                    result.get("explanation") or 
                    result.get("recommendation") or
                    result.get("final_response") or
                    ""
                )
                
                # 创建result副本并添加新字段（与WebSocket事件保持一致）
                result_with_content = dict(result)  # 复制原始result
                result_with_content["content"] = expert_content  # 添加content字段
                
                # 添加任务分配和完成汇报信息
                if result.get("assigned_task"):
                    result_with_content["assigned_task"] = result["assigned_task"]
                if result.get("completion_report"):
                    result_with_content["completion_report"] = result["completion_report"]
                
                expert_data = {
                    "name": expert_name,
                    "success": result.get("success", False),
                    "result": result_with_content  # 使用包含新字段的result
                }
                expert_plan_data["experts"].append(expert_data)
            
            await self.session_manager.add_message(
                user_id,
                "assistant",
                final_response,
                tool_calls=None,
                thinking=f"执行计划：{plan.get('reasoning', '')}",
                expert_plan=expert_plan_data
            )
            
            # 发送完成信号
            yield {
                "type": "complete",
                "message": "✅ 处理完成",
                "expert_count": len(expert_results)
            }
            
        except Exception as e:
            logger.error(f"多专家处理失败: {e}", exc_info=True)
            yield {
                "type": "error",
                "error": str(e)
            } 