"""
MCP客户端 - 严格遵循标准MCP协议
"""

import asyncio
import json
import httpx
import logging
from typing import Dict, List, Any
import uuid
import time

logger = logging.getLogger(__name__)

class MCPClient:
    """MCP客户端，严格遵循标准MCP协议"""
    
    def __init__(self):
        self.sse_url = "http://127.0.0.1:50001/sse"
        self.available_tools = [
            "query_user_health_records",
            "query_doctor_list", 
            "query_user_consultations",
            "query_department_info",
            "query_system_overview",
            "search_doctors_by_condition",
            "add_health_record",
            # RAG检索工具
            "rag_health_check",
            "search_diabetes_knowledge",
            "get_diabetes_knowledge_categories",
            "clear_rag_cache"
        ]
    
    async def test_connection(self):
        """测试MCP服务器连接"""
        try:
            async with httpx.AsyncClient(timeout=3.0) as client:
                async with client.stream("GET", self.sse_url) as response:
                    if response.status_code == 200:
                        try:
                            chunk = await response.aiter_text().__anext__()
                            logger.info("MCP SSE服务器连接正常")
                            return True
                        except Exception:
                            logger.info("MCP SSE服务器连接正常")
                            return True
                    else:
                        logger.warning(f"MCP服务器响应异常: {response.status_code}")
                        return False
        except Exception as e:
            logger.error(f"MCP服务器连接失败: {e}")
            return False
    
    async def _send_mcp_initialize(self, session_endpoint: str) -> str:
        """发送MCP初始化请求"""
        try:
            async with httpx.AsyncClient(timeout=10.0) as init_client:
                full_url = f"http://127.0.0.1:50001{session_endpoint}"
                init_id = "init-" + str(uuid.uuid4())
                
                # 发送initialize请求
                initialize_request = {
                    "jsonrpc": "2.0",
                    "id": init_id,
                    "method": "initialize",
                    "params": {
                        "protocolVersion": "2025-03-26",
                        "clientInfo": {
                            "name": "health-agent",
                            "version": "1.0.0"
                        },
                        "capabilities": {
                            "tools": {},
                            "resources": {},
                            "prompts": {}
                        }
                    }
                }
                
                await init_client.post(full_url, json=initialize_request)
                
                # 发送initialized通知 - 使用正确的方法名
                initialized_notification = {
                    "jsonrpc": "2.0",
                    "method": "notifications/initialized",
                    "params": {}
                }
                
                await init_client.post(full_url, json=initialized_notification)
                
                logger.info("MCP初始化完成")
                return init_id
                
        except Exception as e:
            logger.error(f"MCP初始化失败: {e}")
            return ""
    
    async def _send_tool_request(self, session_endpoint: str, tool_name: str, parameters: Dict[str, Any], request_id: str):
        """发送工具调用请求"""
        try:
            async with httpx.AsyncClient(timeout=10.0) as tool_client:
                full_url = f"http://127.0.0.1:50001{session_endpoint}"
                
                # 使用标准MCP协议格式
                request_data = {
                    "jsonrpc": "2.0",
                    "id": request_id,
                    "method": "tools/call",
                    "params": {
                        "name": tool_name,
                        "arguments": parameters
                    }
                }
                
                response = await tool_client.post(
                    full_url, 
                    json=request_data,
                    headers={
                        "Content-Type": "application/json",
                        "Accept": "application/json"
                    }
                )
                logger.info(f"工具请求发送: {response.status_code}")
                
        except Exception as e:
            logger.error(f"发送工具请求失败: {e}")

    async def call_tool(self, tool_name: str, parameters: Dict[str, Any]) -> Dict[str, Any]:
        """调用MCP工具 - 完整的标准MCP协议流程"""
        try:
            if tool_name not in self.available_tools:
                return {"success": False, "error": f"未知工具: {tool_name}"}
            
            # 📝 记录MCP调用输入
            logger.info(f"🔧 MCP工具调用开始: {tool_name}")
            logger.info(f"   输入参数: {json.dumps(parameters, ensure_ascii=False, indent=2)}")
            
            session_endpoint = None
            init_id = None
            init_completed = False
            tool_request_sent = False
            request_id = str(uuid.uuid4())
            
            # 保持SSE连接开放的完整MCP流程
            async with httpx.AsyncClient(timeout=35.0) as sse_client:
                async with sse_client.stream("GET", self.sse_url) as sse_stream:
                    if sse_stream.status_code != 200:
                        return {"success": False, "error": f"SSE连接失败: {sse_stream.status_code}"}
                    
                    buffer = ""
                    start_time = time.time()
                    
                    async for chunk in sse_stream.aiter_text():
                        buffer += chunk
                        
                        # 第一步：获取session endpoint
                        if not session_endpoint and "data: /messages/" in buffer:
                            lines = buffer.split('\n')
                            for line in lines:
                                if line.startswith("data: /messages/"):
                                    session_endpoint = line[6:].strip()
                                    logger.info(f"获取到session endpoint: {session_endpoint}")
                                    
                                    # 第二步：发送MCP初始化
                                    init_id = await self._send_mcp_initialize(session_endpoint)
                                    break
                        
                        # 监听SSE响应
                        elif "data: " in chunk:
                            lines = chunk.split('\n')
                            for line in lines:
                                if line.startswith("data: "):
                                    data_content = line[6:].strip()
                                    if data_content and "{" in data_content:
                                        try:
                                            result = json.loads(data_content)
                                            
                                            # 处理初始化响应
                                            if result.get("id") == init_id and not init_completed:
                                                logger.info("MCP初始化响应收到")
                                                init_completed = True
                                                
                                                # 初始化完成后发送工具调用
                                                await asyncio.sleep(0.5)
                                                await self._send_tool_request(session_endpoint, tool_name, parameters, request_id)
                                                tool_request_sent = True
                                                continue
                                            
                                            # 处理工具调用响应
                                            elif result.get("id") == request_id and tool_request_sent:
                                                if "error" in result:
                                                    error_msg = result["error"].get("message", "未知错误")
                                                    logger.error(f"❌ MCP工具调用失败: {tool_name}")
                                                    logger.error(f"   错误信息: {error_msg}")
                                                    return {"success": False, "error": error_msg}
                                                elif "result" in result:
                                                    tool_result = result["result"]
                                                    
                                                    # 📝 记录MCP调用输出
                                                    logger.info(f"✅ MCP工具调用成功: {tool_name}")
                                                    if isinstance(tool_result, str):
                                                        try:
                                                            parsed_result = json.loads(tool_result)
                                                            logger.info(f"   输出结果: {json.dumps(parsed_result, ensure_ascii=False, indent=2)}")
                                                            return {"success": True, "data": parsed_result}
                                                        except json.JSONDecodeError:
                                                            logger.info(f"   输出结果(原始): {tool_result}")
                                                            return {"success": True, "data": {"raw_result": tool_result}}
                                                    else:
                                                        logger.info(f"   输出结果: {json.dumps(tool_result, ensure_ascii=False, indent=2)}")
                                                        return {"success": True, "data": tool_result}
                                                        
                                        except json.JSONDecodeError as e:
                                            logger.warning(f"JSON解析失败: {e}, 内容: {data_content}")
                        
                        # 超时控制
                        if time.time() - start_time > 30:
                            if not init_completed:
                                return {"success": False, "error": "MCP初始化超时"}
                            elif not tool_request_sent:
                                return {"success": False, "error": "工具请求发送超时"}
                            else:
                                return {"success": False, "error": "等待工具响应超时"}
                    
                    return {"success": False, "error": "未收到完整响应"}
                    
        except Exception as e:
            logger.error(f"调用工具 {tool_name} 失败: {e}")
            return {"success": False, "error": str(e)}
    
    async def get_available_tools(self) -> List[str]:
        """获取可用工具列表"""
        return self.available_tools
    
    def get_tool_schemas(self) -> List[Dict[str, Any]]:
        """获取工具schemas"""
        return [
            {
                "name": "add_health_record",
                "description": "向MongoDB中添加用户健康记录(血糖、血压、体重)",
                "parameters": {
                    "user_id": {"type": "integer", "description": "用户ID", "required": True},
                    "record_type": {"type": "string", "description": "记录类型: glucose(血糖), pressure(血压), weight(体重)", "required": True},
                    "record_data": {"type": "string", "description": "记录数据JSON字符串", "required": True},
                    "measure_time": {"type": "string", "description": "测量时间(ISO格式)，为空则使用当前时间", "default": ""}
                }
            },
            {
                "name": "query_user_health_records",
                "description": "查询用户健康记录",
                "parameters": {
                    "user_id": {"type": "integer", "description": "用户ID", "required": True},
                    "record_type": {"type": "string", "description": "记录类型", "default": "all"},
                    "days": {"type": "integer", "description": "查询天数", "default": 30},
                    "limit": {"type": "integer", "description": "限制条数", "default": 50}
                }
            },
            {
                "name": "query_doctor_list",
                "description": "查询可用医生列表，支持按科室、专长等条件筛选",
                "parameters": {
                    "department": {"type": "string", "description": "科室名称", "default": ""},
                    "specialty": {"type": "string", "description": "专长领域", "default": ""},
                    "limit": {"type": "integer", "description": "限制返回数量", "default": 20}
                }
            },
            {
                "name": "query_user_consultations",
                "description": "查询用户的咨询历史记录",
                "parameters": {
                    "user_id": {"type": "integer", "description": "用户ID", "required": True},
                    "status": {"type": "string", "description": "咨询状态: pending(待处理), active(进行中), completed(已完成), cancelled(已取消)", "default": "all"},
                    "limit": {"type": "integer", "description": "限制返回数量", "default": 10}
                }
            },
            {
                "name": "query_department_info",
                "description": "查询医院科室信息和介绍",
                "parameters": {
                    "department_name": {"type": "string", "description": "科室名称，为空则返回所有科室", "default": ""}
                }
            },
            {
                "name": "query_system_overview",
                "description": "查询系统整体概览信息，包括用户统计、医生统计等",
                "parameters": {}
            },
            {
                "name": "search_doctors_by_condition",
                "description": "根据疾病或症状条件搜索合适的医生",
                "parameters": {
                    "condition": {"type": "string", "description": "疾病名称或症状描述", "required": True},
                    "limit": {"type": "integer", "description": "限制返回数量", "default": 10}
                }
            },
            {
                "name": "rag_health_check",
                "description": "检查RAG健康知识检索服务的状态",
                "parameters": {}
            },
            {
                "name": "search_diabetes_knowledge",
                "description": "搜索糖尿病相关的专业医学知识，包括症状、治疗、饮食、并发症等",
                "parameters": {
                    "query": {"type": "string", "description": "搜索关键词或问题描述", "required": True},
                    "category": {"type": "string", "description": "知识分类: symptoms(症状), treatment(治疗), diet(饮食), complications(并发症), prevention(预防)", "default": ""},
                    "limit": {"type": "integer", "description": "返回结果数量限制", "default": 5}
                }
            },
            {
                "name": "get_diabetes_knowledge_categories",
                "description": "获取糖尿病知识库的分类信息",
                "parameters": {}
            },
            {
                "name": "clear_rag_cache",
                "description": "清除RAG检索服务的缓存数据",
                "parameters": {}
            }
        ] 