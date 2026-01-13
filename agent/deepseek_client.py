"""
DeepSeek API客户端
"""

import json
import logging
from typing import List, Dict, Any, Optional
from openai import OpenAI
from config import DEEPSEEK_API_KEY, DEEPSEEK_API_BASE, DEEPSEEK_MODEL, SYSTEM_PROMPT

logger = logging.getLogger(__name__)

class DeepSeekClient:
    """DeepSeek API客户端"""
    
    def __init__(self):
        self.client = OpenAI(
            api_key=DEEPSEEK_API_KEY,
            base_url=DEEPSEEK_API_BASE
        )
        self.model = DEEPSEEK_MODEL
    
    async def chat_completion(
        self, 
        messages: List[Dict[str, str]], 
        tools: Optional[List[Dict[str, Any]]] = None,
        temperature: float = 0.3,
        max_tokens: int = 2000
    ) -> Dict[str, Any]:
        """
        调用DeepSeek聊天完成API
        
        Args:
            messages: 对话消息历史
            tools: 可用工具列表
            temperature: 生成温度
            max_tokens: 最大token数
            
        Returns:
            API响应结果
        """
        try:
            # 构建请求参数
            request_params = {
                "model": self.model,
                "messages": messages,
                "temperature": temperature,
                "max_tokens": max_tokens
            }
            
            # 如果提供了工具，添加到请求中
            if tools:
                request_params["tools"] = tools
                request_params["tool_choice"] = "auto"
            
            # 调用API
            response = self.client.chat.completions.create(**request_params)
            
            # 解析响应
            choice = response.choices[0]
            message = choice.message
            
            result = {
                "success": True,
                "message": {
                    "role": message.role,
                    "content": message.content,
                    "tool_calls": []
                },
                "finish_reason": choice.finish_reason,
                "usage": {
                    "prompt_tokens": response.usage.prompt_tokens,
                    "completion_tokens": response.usage.completion_tokens,
                    "total_tokens": response.usage.total_tokens
                }
            }
            
            # 处理工具调用
            if message.tool_calls:
                for tool_call in message.tool_calls:
                    result["message"]["tool_calls"].append({
                        "id": tool_call.id,
                        "type": tool_call.type,
                        "function": {
                            "name": tool_call.function.name,
                            "arguments": tool_call.function.arguments
                        }
                    })
            
            return result
            
        except Exception as e:
            logger.error(f"DeepSeek API调用失败: {e}")
            return {
                "success": False,
                "error": str(e)
            }
    
    async def chat_completion_stream(
        self, 
        messages: List[Dict[str, str]], 
        tools: Optional[List[Dict[str, Any]]] = None,
        temperature: float = 0.3,
        max_tokens: int = 2000
    ):
        """
        调用DeepSeek流式聊天完成API
        
        Args:
            messages: 对话消息历史
            tools: 可用工具列表
            temperature: 生成温度
            max_tokens: 最大token数
            
        Yields:
            API流式响应结果
        """
        try:
            # 构建请求参数
            request_params = {
                "model": self.model,
                "messages": messages,
                "temperature": temperature,
                "max_tokens": max_tokens,
                "stream": True
            }
            
            # 如果提供了工具，添加到请求中
            if tools:
                request_params["tools"] = tools
                request_params["tool_choice"] = "auto"
            
            # 调用流式API
            stream = self.client.chat.completions.create(**request_params)
            
            content_buffer = ""
            tool_calls = []
            
            for chunk in stream:
                if chunk.choices:
                    choice = chunk.choices[0]
                    delta = choice.delta
                    
                    # 处理内容增量
                    if delta.content:
                        content_buffer += delta.content
                        yield {
                            "type": "content",
                            "content": delta.content,
                            "accumulated_content": content_buffer
                        }
                    
                    # 处理工具调用
                    if delta.tool_calls:
                        for tool_call in delta.tool_calls:
                            if tool_call.id:  # 新的工具调用
                                tool_calls.append({
                                    "id": tool_call.id,
                                    "type": tool_call.type,
                                    "function": {
                                        "name": tool_call.function.name if tool_call.function.name else "",
                                        "arguments": tool_call.function.arguments if tool_call.function.arguments else ""
                                    }
                                })
                                yield {
                                    "type": "tool_call_start",
                                    "tool_name": tool_call.function.name
                                }
                            else:  # 工具调用参数增量
                                if tool_calls and tool_call.function.arguments:
                                    tool_calls[-1]["function"]["arguments"] += tool_call.function.arguments
                    
                    # 处理完成
                    if choice.finish_reason:
                        if choice.finish_reason == "tool_calls":
                            yield {
                                "type": "tool_calls_complete",
                                "tool_calls": tool_calls
                            }
                        else:
                            yield {
                                "type": "complete",
                                "content": content_buffer,
                                "tool_calls": tool_calls,
                                "finish_reason": choice.finish_reason
                            }
                        break
            
        except Exception as e:
            logger.error(f"DeepSeek流式API调用失败: {e}")
            yield {
                "type": "error",
                "error": str(e)
            }
    
    def format_tools_for_api(self, tool_schemas: List[Dict[str, Any]]) -> List[Dict[str, Any]]:
        """
        将工具schema转换为DeepSeek API格式
        
        Args:
            tool_schemas: MCP工具schema列表
            
        Returns:
            DeepSeek API格式的工具列表
        """
        formatted_tools = []
        
        for tool in tool_schemas:
            # 构建参数schema
            properties = {}
            required = []
            
            for param_name, param_info in tool.get("parameters", {}).items():
                properties[param_name] = {
                    "type": param_info["type"],
                    "description": param_info["description"]
                }
                
                if param_info.get("required", False):
                    required.append(param_name)
                
                # 添加默认值
                if "default" in param_info:
                    properties[param_name]["default"] = param_info["default"]
            
            formatted_tool = {
                "type": "function",
                "function": {
                    "name": tool["name"],
                    "description": tool["description"],
                    "parameters": {
                        "type": "object",
                        "properties": properties,
                        "required": required
                    }
                }
            }
            
            formatted_tools.append(formatted_tool)
        
        return formatted_tools
    
    def build_system_message(self, user_info: Optional[Dict[str, Any]] = None) -> Dict[str, str]:
        """
        构建系统消息
        
        Args:
            user_info: 用户信息（可选）
            
        Returns:
            系统消息
        """
        system_content = SYSTEM_PROMPT
        
        if user_info:
            system_content += f"\n\n当前用户信息：\n"
            system_content += f"- 姓名：{user_info.get('real_name', '未知')}\n"
            system_content += f"- 用户ID：{user_info.get('id')}\n"
            system_content += f"- 性别：{'男' if user_info.get('gender') == 1 else '女' if user_info.get('gender') == 0 else '未知'}\n"
            if user_info.get('phone'):
                system_content += f"- 手机：{user_info['phone']}\n"
        
        return {
            "role": "system",
            "content": system_content
        } 