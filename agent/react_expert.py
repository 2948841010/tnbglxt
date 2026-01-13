"""
ReAct 专家系统基础组件
实现 Reason + Act 循环模式
"""

import json
import logging
from typing import Dict, List, Any, Optional, Callable
from abc import ABC, abstractmethod
from deepseek_client import DeepSeekClient
from mcp_client import MCPClient

logger = logging.getLogger(__name__)


class ReActStep:
    """ReAct 单步记录"""
    
    def __init__(self, step_num: int):
        self.step_num = step_num
        self.thought: Optional[str] = None
        self.action: Optional[Dict[str, Any]] = None
        self.observation: Optional[Any] = None
        self.reflection: Optional[str] = None
        
    def to_dict(self) -> Dict[str, Any]:
        """转换为字典格式"""
        return {
            "step": self.step_num,
            "thought": self.thought,
            "action": self.action,
            "observation": self.observation,
            "reflection": self.reflection
        }


class ReActLoop:
    """ReAct 循环引擎"""
    
    def __init__(
        self, 
        expert_name: str,
        deepseek_client: DeepSeekClient,
        mcp_client: Optional[MCPClient] = None,
        max_iterations: int = 5,
        stop_keywords: List[str] = None
    ):
        self.expert_name = expert_name
        self.deepseek_client = deepseek_client
        self.mcp_client = mcp_client
        self.max_iterations = max_iterations
        self.stop_keywords = stop_keywords or ["任务完成", "信息充足", "无需继续"]
        self.steps: List[ReActStep] = []
        self.current_iteration = 0
        self.mcp_calls = []  # 收集MCP工具调用记录
        
    async def run(
        self,
        goal: str,
        context: Dict[str, Any],
        available_actions: Dict[str, Callable],
        system_prompt: str
    ) -> Dict[str, Any]:
        """
        运行 ReAct 循环
        
        Args:
            goal: 任务目标
            context: 上下文信息
            available_actions: 可用的动作函数字典
            system_prompt: 系统提示词
            
        Returns:
            循环执行结果
        """
        logger.info(f"🔄 [{self.expert_name}] ReAct循环开始，目标: {goal}")
        
        # 构建历史步骤记录
        history_text = self._format_history()
        
        while self.current_iteration < self.max_iterations:
            self.current_iteration += 1
            step = ReActStep(self.current_iteration)
            
            logger.info(f"  📍 Iteration {self.current_iteration}/{self.max_iterations}")
            
            # 1. Think - 思考下一步
            thought_result = await self._think(
                goal, context, history_text, system_prompt, available_actions
            )
            
            if not thought_result:
                logger.warning(f"  ⚠️ 思考步骤失败，终止循环")
                break
            
            step.thought = thought_result.get("thought")
            step.action = thought_result.get("action")
            
            logger.info(f"  💭 Thought: {step.thought}")
            
            # 检查是否需要停止
            if self._should_stop(step.thought):
                logger.info(f"  ✅ 检测到停止信号，循环结束")
                step.reflection = "任务目标已达成"
                self.steps.append(step)
                break
            
            # 2. Act - 执行动作
            if step.action:
                action_name = step.action.get('name')
                logger.info(f"  🎬 Action: {action_name}")
                
                # 检查是否为 finish 动作，如果是则立即停止
                if action_name == "finish":
                    logger.info(f"  ✅ 模型决定结束，任务完成")
                    step.observation = "任务完成"
                    step.reflection = "模型判断任务目标已达成，主动结束迭代"
                    self.steps.append(step)
                    break
                
                observation = await self._act(step.action, available_actions)
                step.observation = observation
                logger.info(f"  👁️ Observation: {str(observation)[:100]}...")
            else:
                logger.info(f"  ⏭️ 无需执行动作，继续思考")
                step.observation = None
            
            # 3. Reflect - 反思结果
            if step.observation is not None:
                reflection = await self._reflect(
                    goal, step.thought, step.action, step.observation
                )
                step.reflection = reflection
                logger.info(f"  🤔 Reflection: {reflection}")
            
            self.steps.append(step)
            history_text = self._format_history()
            
            # 检查反思中的停止信号
            if step.reflection and self._should_stop(step.reflection):
                logger.info(f"  ✅ 反思中检测到停止信号，循环结束")
                break
        
        # 生成最终报告
        report = await self._generate_report(goal, context)
        
        return {
            "success": True,
            "iterations": self.current_iteration,
            "steps": [s.to_dict() for s in self.steps],
            "report": report,
            "goal_achieved": self._is_goal_achieved(),
            "mcp_calls": self.mcp_calls  # 返回MCP调用记录
        }
    
    async def _think(
        self,
        goal: str,
        context: Dict[str, Any],
        history: str,
        system_prompt: str,
        available_actions: Dict[str, Callable]
    ) -> Optional[Dict[str, Any]]:
        """
        思考步骤：决定下一步做什么
        """
        # 构建可用动作列表
        actions_desc = "\n".join([
            f"- {name}: {func.__doc__ or '执行操作'}" 
            for name, func in available_actions.items()
        ])
        
        # 安全序列化 context（处理 datetime 等特殊类型）
        safe_context = self._make_json_safe(context)
        
        prompt = f"""
你是{self.expert_name}，当前正在使用ReAct模式完成任务。

【任务目标】
{goal}

【上下文信息】
{json.dumps(safe_context, ensure_ascii=False, indent=2)}

【可用动作】
{actions_desc}
- finish: 当任务目标已达成时，调用此动作结束循环

【已执行步骤】
{history if history else "（尚未执行任何步骤）"}

请分析当前情况，决定下一步行动。

**🔥 重要提示：**
- 如果上面的【已执行步骤】中包含Observation数据，你**必须**基于这些实际观察到的数据进行分析
- 优先使用工具返回的Observation数据，而不是仅依赖对话历史中的信息
- 当Observation中有多条记录时，需要综合分析所有记录，而不是只关注某一条
- 你可以执行多次动作来收集信息和分析
- 当你认为已经达成任务目标时，主动调用 "finish" 结束
- 不要受固定次数限制，根据实际需要决定是否继续或结束

**输出格式（JSON）：**
{{
    "thought": "你的思考过程（为什么这样做，基于什么数据）",
    "action": {{
        "name": "动作名称",
        "parameters": {{参数}}
    }}
}}

如果任务已完成，action.name 设置为 "finish"，parameters 可以包含完成原因。
只返回JSON，不要其他内容。
"""
        
        messages = [
            {"role": "system", "content": system_prompt},
            {"role": "user", "content": prompt}
        ]
        
        try:
            response = await self.deepseek_client.chat_completion(
                messages=messages,
                temperature=0.3
            )
            
            content = response["message"]["content"].strip()
            
            # 提取JSON
            if "```json" in content:
                content = content.split("```json")[1].split("```")[0].strip()
            elif "```" in content:
                content = content.split("```")[1].split("```")[0].strip()
            
            result = json.loads(content)
            return result
            
        except Exception as e:
            logger.error(f"思考步骤失败: {e}")
            return None
    
    async def _act(
        self,
        action: Dict[str, Any],
        available_actions: Dict[str, Callable]
    ) -> Any:
        """
        执行动作
        """
        action_name = action.get("name")
        parameters = action.get("parameters", {})
        
        if action_name == "finish":
            return "任务完成"
        
        action_func = available_actions.get(action_name)
        if not action_func:
            return {"error": f"动作 {action_name} 不存在"}
        
        try:
            # 执行动作
            result = await action_func(**parameters)
            return result
        except Exception as e:
            logger.error(f"执行动作失败: {e}")
            return {"error": str(e)}
    
    async def _reflect(
        self,
        goal: str,
        thought: str,
        action: Dict[str, Any],
        observation: Any
    ) -> str:
        """
        反思步骤：评估动作结果
        """
        # 安全序列化
        safe_action = self._make_json_safe(action)
        safe_observation = self._make_json_safe(observation)
        
        # 🔥 修复：增加observation的显示长度，允许反思时看到更多数据
        observation_str = json.dumps(safe_observation, ensure_ascii=False, indent=2)
        # 增加到2000字符（之前是500字符）
        if len(observation_str) > 2000:
            observation_str = observation_str[:2000] + "\n... (数据太长，已截断)"
        
        prompt = f"""
【任务目标】
{goal}

【你的思考】
{thought}

【执行的动作】
{json.dumps(safe_action, ensure_ascii=False)}

【观察到的结果】
{observation_str}

请简短评估：
1. 动作是否成功？
2. 是否朝目标前进？
3. 下一步需要什么？

只返回1-2句话的评估，不要冗长。
"""
        
        try:
            response = await self.deepseek_client.chat_completion(
                messages=[{"role": "user", "content": prompt}],
                temperature=0.2,
                max_tokens=150
            )
            return response["message"]["content"].strip()
        except Exception as e:
            logger.error(f"反思步骤失败: {e}")
            return "反思失败"
    
    async def _generate_report(
        self,
        goal: str,
        context: Dict[str, Any]
    ) -> str:
        """
        生成最终报告
        """
        steps_summary = "\n".join([
            f"{i+1}. {s.thought}" for i, s in enumerate(self.steps)
        ])
        
        # 安全序列化 context（虽然这里没用到，但保持一致性）
        # safe_context = self._make_json_safe(context)
        
        prompt = f"""
【任务目标】
{goal}

【执行步骤】
{steps_summary}

【最终状态】
- 执行了 {len(self.steps)} 个步骤
- 目标{'已达成' if self._is_goal_achieved() else '部分完成'}

请生成简洁的汇报（2-3句话），说明：
1. 完成了什么
2. 关键发现
3. 建议（如果有）
"""
        
        try:
            response = await self.deepseek_client.chat_completion(
                messages=[{"role": "user", "content": prompt}],
                temperature=0.3,
                max_tokens=200
            )
            return response["message"]["content"].strip()
        except Exception as e:
            logger.error(f"生成报告失败: {e}")
            return f"完成了{len(self.steps)}个步骤的探索"
    
    def _should_stop(self, text: str) -> bool:
        """检查是否包含停止关键词"""
        if not text:
            return False
        return any(keyword in text for keyword in self.stop_keywords)
    
    def _is_goal_achieved(self) -> bool:
        """判断目标是否达成"""
        if not self.steps:
            return False
        
        last_step = self.steps[-1]
        
        # 检查最后一步的动作是否为finish
        if last_step.action and last_step.action.get("name") == "finish":
            return True
        
        # 检查思考或反思中是否包含完成信号
        if last_step.thought and self._should_stop(last_step.thought):
            return True
        
        if last_step.reflection and self._should_stop(last_step.reflection):
            return True
        
        return False
    
    def _format_history(self) -> str:
        """格式化历史步骤"""
        if not self.steps:
            return ""
        
        lines = []
        for step in self.steps:
            lines.append(f"Step {step.step_num}:")
            lines.append(f"  Thought: {step.thought}")
            if step.action:
                lines.append(f"  Action: {step.action.get('name')}")
            if step.observation:
                # 🔥 修复：增加observation的显示长度，让Agent能看到完整的MCP工具返回数据
                # 对于字典类型的observation，进行格式化显示
                if isinstance(step.observation, dict):
                    try:
                        obs_str = json.dumps(step.observation, ensure_ascii=False, indent=2)
                        # 对于大型数据，限制在2000字符内（相比之前的100字符大幅提升）
                        if len(obs_str) > 2000:
                            obs_str = obs_str[:2000] + "\n... (数据太长，已截断)"
                        lines.append(f"  Observation:\n{obs_str}")
                    except:
                        obs_str = str(step.observation)[:2000]
                        lines.append(f"  Observation: {obs_str}")
                else:
                    # 对于非字典类型，也增加到2000字符
                    obs_str = str(step.observation)[:2000]
                    if len(str(step.observation)) > 2000:
                        obs_str += "... (已截断)"
                    lines.append(f"  Observation: {obs_str}")
            if step.reflection:
                lines.append(f"  Reflection: {step.reflection}")
            lines.append("")
        
        return "\n".join(lines)
    
    def _make_json_safe(self, obj: Any) -> Any:
        """
        将对象转换为 JSON 可序列化的格式
        处理 datetime、bytes 等特殊类型
        """
        from datetime import datetime, date
        
        if isinstance(obj, (datetime, date)):
            return obj.isoformat()
        elif isinstance(obj, bytes):
            return obj.decode('utf-8', errors='ignore')
        elif isinstance(obj, dict):
            return {k: self._make_json_safe(v) for k, v in obj.items()}
        elif isinstance(obj, (list, tuple)):
            return [self._make_json_safe(item) for item in obj]
        elif isinstance(obj, set):
            return list(obj)
        elif hasattr(obj, '__dict__'):
            # 对于自定义对象，尝试转换其 __dict__
            return self._make_json_safe(obj.__dict__)
        else:
            return obj


class ReActExpert(ABC):
    """
    支持 ReAct 模式的专家基类
    """
    
    def __init__(self, deepseek_client: DeepSeekClient, mcp_client: MCPClient):
        self.deepseek_client = deepseek_client
        self.mcp_client = mcp_client
        self.name = "ReActExpert"
        self.description = "支持ReAct模式的专家"
        self.max_iterations = 5
        self._current_react_loop = None  # 当前运行的ReActLoop实例
    
    async def _call_mcp_and_track(self, tool_name: str, params: dict):
        """调用MCP工具并记录调用信息（供子类使用）"""
        # 调用MCP工具
        result = await self.mcp_client.call_tool(tool_name, params)
        
        # 提取实际数据，避免存储重复的嵌套结构
        extracted_output = self._extract_mcp_data(result) if hasattr(self, '_extract_mcp_data') else result
        
        # 过滤搜索结果，只保留关键字段
        filtered_output = self._filter_search_results(extracted_output) if hasattr(self, '_filter_search_results') else extracted_output
        
        # 记录调用信息（如果在ReAct循环中）
        if self._current_react_loop:
            self._current_react_loop.mcp_calls.append({
                "tool": tool_name,
                "input": params,
                "output": filtered_output  # 使用过滤后的精简数据
            })
        
        return result
        
    @abstractmethod
    def get_system_prompt(self) -> str:
        """获取专家的系统提示词"""
        pass
    
    @abstractmethod
    def get_available_actions(self, context: Dict[str, Any]) -> Dict[str, Callable]:
        """
        获取可用的动作函数
        
        Returns:
            动作名称 -> 动作函数 的字典
        """
        pass
    
    async def process(self, context: Dict[str, Any]) -> Dict[str, Any]:
        """
        处理任务 - 使用 ReAct 循环
        """
        try:
            # 获取任务目标
            goal = context.get("assigned_task", f"执行{self.name}的职责")
            
            logger.info(f"🤖 [{self.name}] 开始处理，使用ReAct模式")
            logger.info(f"📋 任务目标: {goal}")
            
            # 创建 ReAct 循环
            react_loop = ReActLoop(
                expert_name=self.name,
                deepseek_client=self.deepseek_client,
                mcp_client=self.mcp_client,
                max_iterations=self.max_iterations
            )
            
            # 设置当前loop，让get_available_actions可以访问
            self._current_react_loop = react_loop
            
            # 获取可用动作
            available_actions = self.get_available_actions(context)
            
            # 运行循环
            result = await react_loop.run(
                goal=goal,
                context=context,
                available_actions=available_actions,
                system_prompt=self.get_system_prompt()
            )
            
            # 返回结果
            return {
                "expert": self.name,
                "success": result["success"],
                "react_mode": True,
                "iterations": result["iterations"],
                "steps": result["steps"],
                "report": result["report"],
                "goal_achieved": result["goal_achieved"],
                "analysis": result["report"],  # 兼容性
                "completion_report": result["report"],
                "mcp_calls": result.get("mcp_calls", [])  # MCP工具调用记录
            }
            
        except Exception as e:
            logger.error(f"[{self.name}] ReAct处理失败: {e}")
            return {
                "expert": self.name,
                "success": False,
                "react_mode": True,
                "error": str(e)
            }
    
    def _extract_mcp_data(self, mcp_response: Any) -> Any:
        """
        提取MCP返回的实际数据
        """
        if not isinstance(mcp_response, dict):
            return mcp_response
        
        data = mcp_response.get("data", {})
        
        if isinstance(data, dict):
            if "structuredContent" in data:
                structured = data.get("structuredContent", {})
                if isinstance(structured, dict) and "result" in structured:
                    result_str = structured.get("result", "")
                    if isinstance(result_str, str):
                        try:
                            return json.loads(result_str)
                        except:
                            pass
            
            if "content" in data:
                content = data.get("content", [])
                if isinstance(content, list) and len(content) > 0:
                    first_item = content[0]
                    if isinstance(first_item, dict) and "text" in first_item:
                        text_str = first_item.get("text", "")
                        if isinstance(text_str, str):
                            try:
                                return json.loads(text_str)
                            except:
                                pass
        
        return data
    
    def _filter_search_results(self, data: Any) -> Any:
        """
        过滤搜索结果，只保留关键字段，减少上下文占用
        """
        if not isinstance(data, dict):
            return data
        
        # 处理search_results数组（RAG知识库搜索）
        if "search_results" in data:
            filtered_results = []
            for item in data.get("search_results", []):
                filtered_item = {
                    "rank": item.get("rank"),
                    "answer": item.get("answer"),
                    "score": round(item.get("similarity_score", 0), 2),
                    "source": item.get("source_info", {}).get("source", "") if isinstance(item.get("source_info"), dict) else ""
                }
                filtered_results.append(filtered_item)
            return {
                "total": data.get("search_summary", {}).get("total_found", len(filtered_results)),
                "results": filtered_results
            }
        
        # 处理health_records（健康数据查询）
        if "health_records" in data:
            filtered_records = {}
            for record_type, records in data.get("health_records", {}).items():
                if isinstance(records, list):
                    filtered_list = []
                    for r in records:
                        if record_type == "glucose":
                            filtered_list.append({
                                "value": r.get("value"),
                                "measureType": r.get("measureType"),
                                "time": r.get("measureTime", r.get("measure_time", ""))
                            })
                        elif record_type == "pressure":
                            filtered_list.append({
                                "systolic": r.get("systolic"),
                                "diastolic": r.get("diastolic"),
                                "time": r.get("measureTime", r.get("measure_time", ""))
                            })
                        elif record_type == "weight":
                            filtered_list.append({
                                "weight": r.get("weight"),
                                "time": r.get("measureTime", r.get("measure_time", ""))
                            })
                        else:
                            filtered_list.append(r)
                    filtered_records[record_type] = filtered_list
            return {"health_records": filtered_records}
        
        return data

