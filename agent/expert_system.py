"""
多专家系统 - Plan and Execute + ReAct 混合模式
包含规划器、执行器和多个专业领域的专家
支持 ReAct 循环推理模式
"""

import json
import logging
from datetime import datetime
from typing import Dict, List, Any, Optional, Callable
from abc import ABC, abstractmethod
from deepseek_client import DeepSeekClient
from mcp_client import MCPClient
from react_expert import ReActExpert, ReActLoop

logger = logging.getLogger(__name__)


def json_serializable(obj):
    """
    将对象转换为JSON可序列化的格式
    处理datetime等特殊类型
    """
    if isinstance(obj, datetime):
        return obj.isoformat()
    elif isinstance(obj, dict):
        return {k: json_serializable(v) for k, v in obj.items()}
    elif isinstance(obj, (list, tuple)):
        return [json_serializable(item) for item in obj]
    else:
        return obj


def normalize_time_format(time_str: str) -> str:
    """
    标准化时间格式为ISO 8601格式
    支持多种常见时间格式的自动转换
    
    Args:
        time_str: 待转换的时间字符串
        
    Returns:
        ISO 8601格式的时间字符串 (YYYY-MM-DDTHH:MM:SS)
    """
    if not time_str or not isinstance(time_str, str):
        return datetime.now().isoformat()
    
    time_str = time_str.strip()
    
    # 如果已经是ISO格式，直接返回
    if 'T' in time_str and len(time_str) >= 19:
        try:
            datetime.fromisoformat(time_str)
            return time_str
        except:
            pass
    
    # 尝试多种常见格式
    formats = [
        "%Y-%m-%d %H:%M:%S",      # 2025-10-03 12:30:00
        "%Y-%m-%d %H:%M",          # 2025-10-03 12:30
        "%Y-%m-%d",                # 2025-10-03 (补充时间为00:00:00)
        "%Y/%m/%d %H:%M:%S",      # 2025/10/03 12:30:00
        "%Y/%m/%d %H:%M",          # 2025/10/03 12:30
        "%Y/%m/%d",                # 2025/10/03
        "%Y年%m月%d日 %H:%M:%S",  # 2025年10月03日 12:30:00
        "%Y年%m月%d日 %H:%M",      # 2025年10月03日 12:30
        "%Y年%m月%d日",            # 2025年10月03日
    ]
    
    for fmt in formats:
        try:
            dt = datetime.strptime(time_str, fmt)
            return dt.isoformat()
        except ValueError:
            continue
    
    # 如果都无法解析，记录警告并返回当前时间
    logger.warning(f"无法解析时间格式: {time_str}，使用当前时间")
    return datetime.now().isoformat()


class Expert(ABC):
    """专家基类"""
    
    def __init__(self, deepseek_client: DeepSeekClient, mcp_client: MCPClient):
        self.deepseek_client = deepseek_client
        self.mcp_client = mcp_client
        self.name = "BaseExpert"
        self.description = "基础专家"
        self.tools = []
    
    @abstractmethod
    async def process(self, context: Dict[str, Any]) -> Dict[str, Any]:
        """
        处理任务
        
        Args:
            context: 上下文信息，包含用户问题、历史信息等
            
        Returns:
            处理结果
        """
        pass
    
    def get_system_prompt(self) -> str:
        """获取专家的系统提示词"""
        return f"你是{self.description}，专注于{self.name}领域的任务。"
    
    def _extract_mcp_data(self, mcp_response: Any) -> Any:
        """
        提取MCP返回的实际数据，处理多层嵌套的JSON结构
        
        MCP可能返回的格式：
        1. 简单格式: {"success": true, "data": {...}}
        2. 嵌套格式: {"success": true, "data": {"content": [{"text": "{...}"}], "structuredContent": {"result": "{...}"}}}
        """
        if not isinstance(mcp_response, dict):
            logger.debug(f"_extract_mcp_data: 非字典类型，直接返回")
            return mcp_response
        
        # 第一层：提取data字段
        data = mcp_response.get("data", {})
        logger.debug(f"_extract_mcp_data: 提取data字段，类型={type(data)}")
        
        # 如果data是字典且包含content或structuredContent，说明是嵌套格式
        if isinstance(data, dict):
            # 尝试从structuredContent.result获取
            if "structuredContent" in data:
                logger.debug(f"_extract_mcp_data: 发现structuredContent字段")
                structured = data.get("structuredContent", {})
                if isinstance(structured, dict) and "result" in structured:
                    result_str = structured.get("result", "")
                    if isinstance(result_str, str):
                        try:
                            parsed = json.loads(result_str)
                            logger.info(f"_extract_mcp_data: 成功从structuredContent.result解析JSON")
                            return parsed
                        except Exception as e:
                            logger.warning(f"_extract_mcp_data: structuredContent.result解析失败: {e}")
            
            # 尝试从content[0].text获取
            if "content" in data:
                logger.debug(f"_extract_mcp_data: 发现content字段")
                content = data.get("content", [])
                if isinstance(content, list) and len(content) > 0:
                    first_item = content[0]
                    if isinstance(first_item, dict) and "text" in first_item:
                        text_str = first_item.get("text", "")
                        if isinstance(text_str, str):
                            try:
                                parsed = json.loads(text_str)
                                logger.info(f"_extract_mcp_data: 成功从content[0].text解析JSON")
                                return parsed
                            except Exception as e:
                                logger.warning(f"_extract_mcp_data: content[0].text解析失败: {e}")
        
        # 如果不是嵌套格式，直接返回data
        logger.debug(f"_extract_mcp_data: 使用默认data字段，keys={list(data.keys()) if isinstance(data, dict) else 'N/A'}")
        return data


class DiagnosisExpert(Expert):
    """诊断专家 - 分析症状和健康状况"""
    
    def __init__(self, deepseek_client: DeepSeekClient, mcp_client: MCPClient):
        super().__init__(deepseek_client, mcp_client)
        self.name = "诊断专家"
        self.description = "专业的医疗诊断专家，擅长分析症状、评估健康风险"
        self.tools = []
    
    def get_system_prompt(self) -> str:
        return """你是一位专业的医疗诊断专家，擅长：
1. 分析用户描述的症状和体征
2. 评估糖尿病相关的健康风险
3. 识别可能的并发症
4. 提供初步的健康评估

**重要：数据分析原则**
1. **基于真实数据**：诊断和评估必须基于用户实际提供的健康数据
   - 引用具体的血糖、血压、体重等数值
   - 对照医学标准进行分析
   - 指出具体哪些指标异常
2. **综合评估**：结合症状、数据和病史
   - 症状描述（如口渴、多尿等）
   - 客观数据（血糖、血压等）
   - 既往病史和家族史
3. **明确风险等级**：基于实际数据评估风险
   - 轻度风险：指标略高但可控
   - 中度风险：多项指标异常
   - 高度风险：严重超标或有并发症

重要原则：
- 只分析症状和风险，不要给出生活方式建议（那是综合专家的职责）
- 不要重复数据专家已经说明的数据分析
- 聚焦于诊断评估和风险识别
- 简洁专业，避免冗长"""
    
    async def process(self, context: Dict[str, Any]) -> Dict[str, Any]:
        """分析症状和诊断"""
        try:
            user_question = context.get("user_question", "")
            health_data = context.get("health_data", {})
            conversation_history = context.get("conversation_history", [])
            
            # 简化历史记录
            simplified_history = []
            for msg in conversation_history[-10:]:
                simplified_history.append({
                    "role": msg.get("role"),
                    "content": msg.get("content", "")[:200]
                })
            
            logger.info(f"💬 诊断专家使用历史对话记录，共 {len(simplified_history)} 条消息")
            
            # 构建诊断提示
            messages = [
                {"role": "system", "content": self.get_system_prompt()},
                {"role": "user", "content": f"""
请分析以下情况：

当前用户问题：{user_question}

历史对话记录：
{json.dumps(simplified_history, ensure_ascii=False, indent=2) if simplified_history else "无历史对话"}

健康数据：{json.dumps(health_data, ensure_ascii=False, indent=2)}

重要提示：请结合历史对话记录中用户提到的症状和描述进行综合分析。

请提供：
1. 症状分析（结合历史对话中的信息）
2. 可能的健康风险
3. 需要注意的事项
"""}
            ]
            
            # 调用AI进行分析
            response = await self.deepseek_client.chat_completion(
                messages=messages,
                temperature=0.3
            )
            
            return {
                "expert": self.name,
                "success": True,
                "analysis": response["message"]["content"],
                "confidence": "high"
            }
            
        except Exception as e:
            logger.error(f"诊断专家处理失败: {e}")
            return {
                "expert": self.name,
                "success": False,
                "error": str(e)
            }


class DataExpert(ReActExpert):
    """数据专家 - 使用ReAct模式查询和分析健康数据"""
    
    def __init__(self, deepseek_client: DeepSeekClient, mcp_client: MCPClient):
        super().__init__(deepseek_client, mcp_client)
        self.name = "数据专家"
        self.description = "使用ReAct模式进行健康数据深度分析"
        self.max_iterations = 10  # 安全上限，但主要由模型自己决定何时停止
    
    def get_system_prompt(self) -> str:
        return """你是健康数据分析专家，使用ReAct（推理-行动）模式工作。

你的职责：
1. 通过多步推理和查询，深度分析用户健康数据
2. 识别数据趋势、异常值和关键模式
3. 基于实际数据得出客观结论

工作模式（ReAct循环）：
- Thought: 分析当前情况，决定查询什么数据
- Action: 执行数据查询操作
- Observation: 观察查询结果
- Reflection: 评估数据的意义和完整性

分析策略：
1. 从基础查询开始（如近7天数据）
2. 根据初步结果，决定是否扩大范围
3. 分段分析，寻找趋势
4. 计算统计指标（平均值、波动等）
5. 识别异常值和风险点

重要原则：
- 每次查询都要深入分析结果
- 根据发现动态调整查询策略
- 只陈述数据事实，不做诊断
- 简洁明了，聚焦关键发现

何时结束：
- 当已获得足够数据支持任务目标时，调用 finish 结束
- 不要过度查询，3-5次迭代通常足够
- 如果数据已经能够回答任务要求，立即结束

记住：你可以多次查询不同范围的数据，但要自主判断何时结束。"""
    
    def get_available_actions(self, context: Dict[str, Any]) -> Dict[str, Callable]:
        """获取可用的动作"""
        user_id = context.get("user_id")
        
        async def query_recent_data(days: int = 7, **kwargs):
            """查询最近N天的健康数据（days: 查询天数）"""
            params = {"user_id": user_id, "days": days}
            result = await self._call_mcp_and_track("query_user_health_records", params)
            return self._extract_mcp_data(result)
        
        async def query_glucose_data(days: int = 30, **kwargs):
            """专门查询血糖数据（days: 查询天数）"""
            params = {"user_id": user_id, "days": days, "record_type": "glucose"}
            result = await self._call_mcp_and_track("query_user_health_records", params)
            return self._extract_mcp_data(result)
        
        async def query_pressure_data(days: int = 30, **kwargs):
            """专门查询血压数据（days: 查询天数）"""
            params = {"user_id": user_id, "days": days, "record_type": "pressure"}
            result = await self._call_mcp_and_track("query_user_health_records", params)
            return self._extract_mcp_data(result)
        
        async def analyze_trend(data_list: list, data_type: str, **kwargs):
            """分析数据趋势（data_list: 数据列表, data_type: 数据类型如glucose/pressure/weight）"""
            if not data_list:
                return {"trend": "无数据"}
            
            # 简单趋势分析
            values = []
            for item in data_list:
                if data_type == "glucose":
                    values.append(float(item.get("value", 0)))
                elif data_type == "pressure":
                    values.append(float(item.get("systolic", 0)))
                elif data_type == "weight":
                    values.append(float(item.get("weight", 0)))
            
            if not values:
                return {"trend": "无有效数据"}
            
            avg = sum(values) / len(values)
            max_val = max(values)
            min_val = min(values)
            
            # 简单判断趋势
            first_half = values[:len(values)//2]
            second_half = values[len(values)//2:]
            trend = "stable"
            if first_half and second_half:
                avg_first = sum(first_half) / len(first_half)
                avg_second = sum(second_half) / len(second_half)
                if avg_second > avg_first * 1.1:
                    trend = "上升"
                elif avg_second < avg_first * 0.9:
                    trend = "下降"
                else:
                    trend = "稳定"
            
            return {
                "count": len(values),
                "average": round(avg, 2),
                "max": max_val,
                "min": min_val,
                "trend": trend
            }
        
        return {
            "query_recent_data": query_recent_data,
            "query_glucose_data": query_glucose_data,
            "query_pressure_data": query_pressure_data,
            "analyze_trend": analyze_trend,
            "finish": lambda **kwargs: "数据分析完成"
        }
    
    # 继承 ReActExpert 的 process 方法，使用 ReAct 循环


class KnowledgeExpert(Expert):
    """知识专家 - 检索医学知识库"""
    
    def __init__(self, deepseek_client: DeepSeekClient, mcp_client: MCPClient):
        super().__init__(deepseek_client, mcp_client)
        self.name = "知识专家"
        self.description = "医学知识检索专家"
        self.tools = ["search_diabetes_knowledge", "get_diabetes_knowledge_categories"]
    
    def get_system_prompt(self) -> str:
        return """你是医学知识专家，擅长：
1. 检索糖尿病相关的医学知识
2. 解释专业医学概念
3. 提供医学标准和指南
4. 引用权威医学资料

**可用工具**：
- search_diabetes_knowledge: 检索糖尿病知识库

**重要：工具使用原则**
1. **仔细阅读检索结果**：调用search_diabetes_knowledge后，必须认真阅读所有返回的知识条目
   - 每条结果都包含问题、答案、相关度分数
   - 选择最相关的知识进行引用
   - 理解知识内容后再进行解释
2. **引用具体内容**：基于实际检索到的知识进行回答
   - 引用原文中的关键信息
   - 提及知识的来源和相关度
   - 如果检索结果不足，明确说明
3. **评估检索质量**：观察search_results的数量和相关度
   - 如果返回0条结果，说明"知识库中暂无相关信息"
   - 如果相关度低，说明"检索到的信息相关度较低"

重要原则：
- 只提供医学知识和标准，不要给出个性化建议
- 不要重复诊断专家的风险评估
- 聚焦于知识库中的权威信息
- 简洁准确，引用来源"""
    
    async def process(self, context: Dict[str, Any]) -> Dict[str, Any]:
        """检索知识库"""
        try:
            user_question = context.get("user_question", "")
            
            # 检索知识库
            knowledge_query_params = {"query": user_question, "top_k": 5}
            knowledge_response = await self.mcp_client.call_tool(
                "search_diabetes_knowledge",
                knowledge_query_params
            )
            
            # 提取实际数据 - 使用辅助方法处理嵌套JSON
            knowledge_results = self._extract_mcp_data(knowledge_response)
            
            # 如果返回的是search_results格式，提取结果数组
            if isinstance(knowledge_results, dict) and "search_results" in knowledge_results:
                knowledge_results = knowledge_results.get("search_results", [])
            
            # 记录MCP调用详情
            mcp_calls = [
                {
                    "tool": "search_diabetes_knowledge",
                    "input": knowledge_query_params,
                    "output": knowledge_response
                }
            ]
            
            # 使用AI整理知识
            messages = [
                {"role": "system", "content": self.get_system_prompt()},
                {"role": "user", "content": f"""
用户问题：{user_question}

检索到的知识库内容：
{json.dumps(knowledge_results, ensure_ascii=False, indent=2)}

请基于这些知识：
1. 提供准确的医学信息
2. 解释相关概念
3. 给出专业建议
4. 注明信息来源
"""}
            ]
            
            response = await self.deepseek_client.chat_completion(
                messages=messages,
                temperature=0.2
            )
            
            return {
                "expert": self.name,
                "success": True,
                "knowledge": knowledge_results,  # 传递提取后的实际数据
                "explanation": response["message"]["content"],
                "confidence": "high",
                "mcp_calls": mcp_calls  # 添加MCP调用详情
            }
            
        except Exception as e:
            logger.error(f"知识专家处理失败: {e}")
            return {
                "expert": self.name,
                "success": False,
                "error": str(e)
            }


class DoctorExpert(Expert):
    """医生推荐专家 - 推荐合适的医生"""
    
    def __init__(self, deepseek_client: DeepSeekClient, mcp_client: MCPClient):
        super().__init__(deepseek_client, mcp_client)
        self.name = "医生推荐专家"
        self.description = "医生匹配和推荐专家"
        self.tools = ["query_doctor_list", "search_doctors_by_condition", "query_department_info"]
    
    def get_system_prompt(self) -> str:
        return """你是医生推荐专家，擅长：
1. 根据用户病情推荐合适的医生
2. 匹配医生的专业领域和擅长方向
3. 考虑医生的在线状态和评分
4. 提供就诊建议

请基于用户需求，推荐最合适的医生。"""
    
    async def process(self, context: Dict[str, Any]) -> Dict[str, Any]:
        """推荐医生"""
        try:
            user_question = context.get("user_question", "")
            diagnosis_result = context.get("diagnosis_result", {})
            
            # 查询医生列表
            doctor_query_params = {"status": "online"}
            doctors_response = await self.mcp_client.call_tool(
                "query_doctor_list",
                doctor_query_params
            )
            
            # 提取实际数据 - 使用辅助方法处理嵌套JSON
            doctors_data = self._extract_mcp_data(doctors_response)
            
            # query_doctor_list 返回的是 {total_count: N, doctors: [...]}
            if isinstance(doctors_data, dict):
                doctors = doctors_data.get("doctors", [])
                logger.info(f"👨‍⚕️ 医生推荐专家：从doctors字段提取，数量={len(doctors)}")
            elif isinstance(doctors_data, list):
                doctors = doctors_data
                logger.info(f"👨‍⚕️ 医生推荐专家：数据本身是列表，数量={len(doctors)}")
            else:
                doctors = []
                logger.warning(f"👨‍⚕️ 医生推荐专家：无法识别医生数据格式，类型={type(doctors_data)}")
            
            # 记录MCP调用详情
            mcp_calls = [
                {
                    "tool": "query_doctor_list",
                    "input": doctor_query_params,
                    "output": doctors_response
                }
            ]
            
            # 使用AI匹配医生
            messages = [
                {"role": "system", "content": self.get_system_prompt()},
                {"role": "user", "content": f"""
用户问题：{user_question}

诊断结果：{json.dumps(diagnosis_result, ensure_ascii=False, indent=2)}

可选医生：
{json.dumps(doctors, ensure_ascii=False, indent=2)}

请推荐：
1. 最合适的医生（考虑专业匹配度）
2. 推荐理由
3. 就诊建议
"""}
            ]
            
            response = await self.deepseek_client.chat_completion(
                messages=messages,
                temperature=0.3
            )
            
            return {
                "expert": self.name,
                "success": True,
                "doctors": doctors,  # 传递提取后的实际数据
                "recommendation": response["message"]["content"],
                "confidence": "high",
                "mcp_calls": mcp_calls  # 添加MCP调用详情
            }
            
        except Exception as e:
            logger.error(f"医生推荐专家处理失败: {e}")
            return {
                "expert": self.name,
                "success": False,
                "error": str(e)
            }


class DataRecordExpert(Expert):
    """数据记录专家 - 解析用户输入并记录健康数据"""
    
    def __init__(self, deepseek_client: DeepSeekClient, mcp_client: MCPClient):
        super().__init__(deepseek_client, mcp_client)
        self.name = "数据记录专家"
        self.description = "解析用户输入的健康数据并记录到系统"
        self.tools = ["add_health_record", "query_user_health_records"]
    
    def get_system_prompt(self) -> str:
        current_date = datetime.now().strftime("%Y-%m-%d")
        current_time = datetime.now().isoformat()  # ISO 8601格式：2025-10-03T12:30:00
        
        return f"""你是数据记录专家，负责从用户输入中提取健康数据并智能判断如何处理。

**当前日期时间**：{current_time}

你的职责：
1. 识别用户输入中的健康数据（血糖、血压、体重等）
2. 智能判断是绝对值还是相对值变化
3. 对于相对值，标记 is_relative=true，系统会自动查询历史数据并计算实际值
4. 验证数据的合理性

**可用工具**：
- query_user_health_records: 查询用户历史健康数据（相对值时会自动调用）
- add_health_record: 添加新的健康记录

**重要：工具使用原则**
1. **调用前思考**：明确工具调用的目的
2. **观察返回值**：仔细阅读工具返回的所有信息
3. **验证结果**：检查操作是否成功（success字段）
4. **分析数据**：基于实际返回的数据进行分析和决策
5. **报告问题**：如果工具调用失败，必须在分析中说明

示例：
- 调用add_health_record后，必须检查success字段
- 如果失败，分析失败原因并告知用户
- 如果成功，确认记录的具体内容

支持的数据类型：
- 血糖（glucose）：正常范围 3.9-11.1 mmol/L（空腹4-7，餐后<11.1）
  * **重要**：必须识别测量类型（measureType）：
    - "空腹"、"早上起床"、"饭前" → "空腹"
    - "餐后"、"饭后"、"吃完饭" → "餐后" 
    - "随机"、未指明时间 → "随机"
  * 血糖记录必须包含 measureType 字段
- 血压（pressure）：正常范围 收缩压90-140，舒张压60-90 mmHg
- 体重（weight）：正常范围 30-200 kg（成人）

**智能识别规则**：

1. **相对值表达**（需要查询历史数据）：
   - "下降了X"、"降低了X"、"减少了X"  → change为负数
   - "增加了X"、"升高了X"、"上升了X"  → change为正数
   - "比上次少X"、"比上次多X"
   - "较上一次下降/增加X"
   
   **重要**: 识别到相对值时，必须设置 is_relative=true 和 change值，系统会：
   1. 自动调用 query_user_health_records 查询历史数据
   2. 找到最近的该类型记录
   3. 计算实际值：新值 = 历史值 + change
   4. 调用 add_health_record 记录计算后的实际值
   
2. **绝对值表达**（直接记录）：
   - "体重是70kg"、"血糖9.0"
   - "测了血压120/80"
   - 明确给出具体数值
   
   设置 is_relative=false 或不设置，直接记录提供的值

3. **合理性验证**：
   - 体重：<30kg 或 >200kg（明显异常，需确认）
   - 血糖：<2 或 >20 mmol/L（危险值，需确认）
   - 血压：<80/50 或 >180/110 mmHg（异常值，需确认）
   
   不合理的数据设置 needs_confirmation=true，系统不会记录

**时间解析**：
- "今天"、"现在"、"刚才" → 当前时间
- "早上"、"上午" → 今天 08:00:00
- "中午"、"餐后" → 今天 12:30:00
- "下午" → 今天 15:00:00
- "晚上" → 今天 19:00:00
- "昨天" → 昨天相应时间

**时间格式要求**：
- 必须使用 ISO 8601 格式：YYYY-MM-DDTHH:MM:SS
- 或使用简单格式：YYYY-MM-DD HH:MM:SS（系统会自动转换）
- 示例：{current_time} 或 {current_date} 12:30:00

**输出格式和示例**：

示例1 - 用户说"我空腹血糖是6.8"（绝对值血糖，包含测量类型）：
{{
    "has_data": true,
    "records": [
        {{
            "type": "glucose",
            "value": 6.8,
            "unit": "mmol/L",
            "measureType": "空腹",
            "measure_time": "{current_date}T08:00:00",
            "is_relative": false,
            "needs_confirmation": false
        }}
    ]
}}

示例2 - 用户说"我餐后血糖7.0"（绝对值血糖，餐后类型）：
{{
    "has_data": true,
    "records": [
        {{
            "type": "glucose",
            "value": 7.0,
            "unit": "mmol/L",
            "measureType": "餐后",
            "measure_time": "{current_date}T12:30:00",
            "is_relative": false,
            "needs_confirmation": false
        }}
    ]
}}

示例3 - 用户说"我体重是70kg"（绝对值，直接记录）：
{{
    "has_data": true,
    "records": [
        {{
            "type": "weight",
            "value": 70.0,
            "unit": "kg",
            "measure_time": "{current_date}T12:00:00",
            "is_relative": false,
            "needs_confirmation": false
        }}
    ]
}}

示例4 - 用户说"我体重下降了10kg"（相对值，需要查询历史）：
{{
    "has_data": true,
    "records": [
        {{
            "type": "weight",
            "is_relative": true,
            "change": -10.0,
            "unit": "kg",
            "measure_time": "{current_time}",
            "needs_confirmation": false
        }}
    ]
}}
注意：change为-10（负数表示下降），系统会自动：
1. 查询历史体重（假设是80kg）
2. 计算：80 + (-10) = 70kg
3. 记录新值70kg

示例5 - 用户说"我体重增加了5斤"（相对值，需要单位转换）：
{{
    "has_data": true,
    "records": [
        {{
            "type": "weight",
            "is_relative": true,
            "change": 2.5,
            "unit": "kg",
            "measure_time": "{current_time}",
            "needs_confirmation": false
        }}
    ]
}}
注意：5斤 = 2.5kg，change为正数

示例6 - 数据不合理，需要确认：
{{
    "has_data": true,
    "need_history": false,
    "records": [
        {{
            "type": "weight",
            "value": 10.0,
            "unit": "kg",
            "measure_time": "{current_date}T12:00:00",
            "needs_confirmation": true,
            "confirmation_reason": "体重10kg明显偏低（正常成人体重在30-200kg范围），请确认是否输入正确"
        }}
    ]
}}

示例7 - 用户说"我今天早上空腹血糖6.8，餐后血糖7.0"（多条血糖记录，需要正确识别类型）：
{{
    "has_data": true,
    "records": [
        {{
            "type": "glucose",
            "value": 6.8,
            "unit": "mmol/L",
            "measureType": "空腹",
            "measure_time": "{current_date}T08:00:00",
            "is_relative": false,
            "needs_confirmation": false
        }},
        {{
            "type": "glucose",
            "value": 7.0,
            "unit": "mmol/L",
            "measureType": "餐后",
            "measure_time": "{current_date}T12:30:00",
            "is_relative": false,
            "needs_confirmation": false
        }}
    ]
}}
注意：
- 识别到"空腹"关键词，第一条记录 measureType 设为 "空腹"
- 识别到"餐后"关键词，第二条记录 measureType 设为 "餐后"
- 两条记录的测量时间不同（早上8点和中午12:30）

示例8 - 没有数据：
{{"has_data": false}}

**关键要点**：
1. **🩸 血糖测量类型识别（重要！）**：
   - **空腹血糖**："空腹"、"早上起床"、"早上测的"、"饭前" → measureType: "空腹"
   - **餐后血糖**："餐后"、"饭后"、"吃完饭"、"早餐后"、"午餐后"、"晚餐后" → measureType: "餐后"
   - **随机血糖**：其他情况或未明确说明 → measureType: "随机"
   - **必须在每条血糖记录中包含 measureType 字段**

2. **识别相对值的关键词**：
   - "下降/降低/减少/少了" → change为负数，is_relative=true
   - "增加/升高/上升/多了" → change为正数，is_relative=true
   - "比上次"、"较上一次" → is_relative=true
   
3. **相对值不需要value字段**：
   - 只需要：type, is_relative=true, change, unit, measure_time
   - 系统会自动查询历史数据并计算实际value
   
4. **绝对值需要value字段**：
   - 必须包含：type, value, unit, measure_time
   - 血糖还必须包含：measureType
   - is_relative=false 或不设置

5. **单位转换规则**：
   - 体重：1斤 = 0.5kg（统一转换为kg）
   - 血糖：统一使用 mmol/L
   - 血压：统一使用 mmHg

6. **验证数据合理性**：
   - 不合理的数据设置 needs_confirmation=true
   - 添加 confirmation_reason 说明原因

当前时间：{current_time}
当前日期：{current_date}

只返回JSON格式，不要其他内容。"""
    
    async def process(self, context: Dict[str, Any]) -> Dict[str, Any]:
        """解析用户输入并记录健康数据"""
        try:
            user_id = context.get("user_id")
            user_question = context.get("user_question", "")
            
            # 使用AI解析用户输入中的健康数据
            messages = [
                {"role": "system", "content": self.get_system_prompt()},
                {"role": "user", "content": f"""
用户输入：{user_question}

请识别其中的健康数据并提取为JSON格式。
"""}
            ]
            
            response = await self.deepseek_client.chat_completion(
                messages=messages,
                temperature=0.2
            )
            
            content = response["message"]["content"].strip()
            logger.info(f"📝 数据记录专家AI响应: {content[:200]}...")
            
            # 提取JSON
            if "```json" in content:
                content = content.split("```json")[1].split("```")[0].strip()
            elif "```" in content:
                content = content.split("```")[1].split("```")[0].strip()
            
            try:
                result = json.loads(content)
            except Exception as e:
                logger.error(f"解析AI响应失败: {e}, 内容: {content}")
                result = {"has_data": False}
            
            records_added = []
            records_need_confirmation = []
            mcp_calls = []
            
            # 如果找到数据，处理记录
            if result.get("has_data") and result.get("records"):
                logger.info(f"📝 开始处理 {len(result['records'])} 条记录")
                for record in result["records"]:
                    record_type = record.get("type")
                    logger.info(f"📝 数据记录专家：处理{record_type}数据")
                    logger.info(f"📝 记录详情: {json.dumps(record, ensure_ascii=False)}")
                    
                    # 检查是否需要用户确认
                    if record.get("needs_confirmation", False):
                        logger.warning(f"⚠️ 数据需要确认: {record.get('confirmation_reason')}")
                        records_need_confirmation.append({
                            "type": record_type,
                            "value": record.get("value"),
                            "reason": record.get("confirmation_reason", "数据异常，请确认")
                        })
                        continue  # 跳过此记录，不添加到数据库
                    
                    # 处理相对值（需要查询历史数据）
                    if record.get("is_relative", False):
                        logger.info(f"📝 检测到相对值变化: {record.get('change')}")
                        
                        # 查询最近的该类型数据
                        query_params = {
                            "user_id": user_id,
                            "days": 30
                        }
                        health_records = await self.mcp_client.call_tool(
                            "query_user_health_records",
                            query_params
                        )
                        
                        # 记录MCP调用
                        mcp_calls.append({
                            "tool": "query_user_health_records",
                            "input": query_params,
                            "output": health_records,
                            "success": health_records.get("success", False)
                        })
                        
                        # 提取该类型的最新记录
                        latest_value = None
                        
                        # 从health_records中提取数据
                        hr_data = self._extract_mcp_data(health_records)
                        if isinstance(hr_data, dict) and "health_records" in hr_data:
                            type_records = hr_data["health_records"].get(record_type, [])
                            if type_records and len(type_records) > 0:
                                # 获取最新的一条记录
                                latest_record = type_records[-1] if isinstance(type_records, list) else None
                                if latest_record:
                                    # 不同类型的健康记录使用不同的字段名
                                    if record_type == "weight":
                                        # 体重记录使用 weight 字段
                                        latest_value = latest_record.get("weight")
                                    elif record_type == "glucose":
                                        # 血糖记录使用 value 字段
                                        latest_value = latest_record.get("value")
                                    elif record_type == "pressure":
                                        # 血压记录比较特殊，暂不支持相对值
                                        latest_value = None
                                    
                                    if latest_value is not None:
                                        # 确保转换为float
                                        try:
                                            latest_value = float(latest_value)
                                            logger.info(f"📝 找到最近的{record_type}记录: {latest_value}")
                                        except (ValueError, TypeError):
                                            logger.warning(f"⚠️ 最近的{record_type}记录值无法转换为数字: {latest_value}")
                                            latest_value = None
                        
                        if latest_value is not None:
                            # 计算新值
                            change = record.get("change", 0)
                            new_value = latest_value + change
                            logger.info(f"📝 计算新值: {latest_value} + ({change}) = {new_value}")
                            
                            # 设置计算后的值
                            record["value"] = new_value
                        else:
                            logger.warning(f"⚠️ 未找到最近的{record_type}记录，无法计算相对值")
                            # 无法计算，添加到需要确认的列表
                            records_need_confirmation.append({
                                "type": record_type,
                                "change": record.get("change"),
                                "reason": f"未找到历史{record_type}数据，无法计算相对变化后的值，请直接告知当前{record_type}的具体数值"
                            })
                            continue
                    
                    # 准备record_data
                    record_data = {}
                    if "value" in record:
                        record_data["value"] = record["value"]
                    if "unit" in record:
                        record_data["unit"] = record["unit"]
                    # 血糖特有字段
                    if record_type == "glucose" and "measureType" in record:
                        record_data["measureType"] = record.get("measureType", "随机")
                    # 血压特有字段
                    if record_type == "pressure":
                        if "systolic" in record:
                            record_data["systolic"] = record["systolic"]
                        if "diastolic" in record:
                            record_data["diastolic"] = record["diastolic"]
                    
                    # 调用MCP工具添加健康记录
                    record_data_json = json.dumps(record_data, ensure_ascii=False)
                    
                    # 标准化时间格式为ISO 8601
                    measure_time = record.get("measure_time", "")
                    normalized_time = normalize_time_format(measure_time)
                    logger.info(f"📅 时间标准化: {measure_time} -> {normalized_time}")
                    
                    add_params = {
                        "user_id": user_id,
                        "record_type": record["type"],
                        "record_data": record_data_json,
                        "measure_time": normalized_time  # 使用标准化后的时间
                    }
                    add_result = await self.mcp_client.call_tool(
                        "add_health_record",
                        add_params
                    )
                    
                    # 检查工具调用是否成功
                    tool_success = add_result.get("success", False)
                    if not tool_success:
                        logger.error(f"❌ 添加健康记录失败: {add_result.get('error', '未知错误')}")
                    
                    records_added.append({
                        "type": record["type"],
                        "data": record_data,  # 使用构造好的 record_data
                        "result": add_result,
                        "success": tool_success  # 添加成功标志
                    })
                    
                    # 记录MCP调用
                    mcp_calls.append({
                        "tool": "add_health_record",
                        "input": add_params,
                        "output": add_result,
                        "success": tool_success
                    })
            
            # 生成可读的分析文本
            analysis_text = ""
            
            # 处理需要确认的记录
            if records_need_confirmation:
                analysis_text += "⚠️ **以下数据需要您确认**：\n\n"
                for conf_record in records_need_confirmation:
                    type_name = {"glucose": "血糖", "pressure": "血压", "weight": "体重", "height": "身高"}.get(conf_record["type"], conf_record["type"])
                    if "value" in conf_record:
                        analysis_text += f"- **{type_name}**: {conf_record['value']} {conf_record.get('unit', '')}\n"
                        analysis_text += f"  原因：{conf_record['reason']}\n\n"
                    elif "change" in conf_record:
                        change_text = f"变化了{abs(conf_record['change'])}{conf_record.get('unit', 'kg')}" if conf_record['change'] < 0 else f"增加了{conf_record['change']}{conf_record.get('unit', 'kg')}"
                        analysis_text += f"- **{type_name}**: {change_text}\n"
                        analysis_text += f"  原因：{conf_record['reason']}\n\n"
            
            # 处理成功和失败的记录
            if result.get("has_data") and records_added:
                # 区分成功和失败的记录
                successful_records = [r for r in records_added if r.get("success", False)]
                failed_records = [r for r in records_added if not r.get("success", False)]
                
                if successful_records:
                    if records_need_confirmation:
                        analysis_text += "\n"
                    analysis_text += "✅ **已成功识别并记录以下健康数据**：\n\n"
                    for record in successful_records:
                        type_name = {"glucose": "血糖", "pressure": "血压", "weight": "体重", "height": "身高"}.get(record["type"], record["type"])
                        data_str = json.dumps(record["data"], ensure_ascii=False)
                        analysis_text += f"- **{type_name}**: {data_str}\n"
                    analysis_text += f"\n成功记录 {len(successful_records)} 条数据。"
                
                if failed_records:
                    if successful_records or records_need_confirmation:
                        analysis_text += "\n\n"
                    analysis_text += "❌ **以下数据记录失败**：\n\n"
                    for record in failed_records:
                        type_name = {"glucose": "血糖", "pressure": "血压", "weight": "体重", "height": "身高"}.get(record["type"], record["type"])
                        error_msg = record["result"].get("error", "未知错误")
                        analysis_text += f"- **{type_name}**: {error_msg}\n"
            elif not records_need_confirmation:
                analysis_text = "未检测到用户输入中包含健康数据。"
            
            # 计算整体成功状态：如果有记录且全部成功，则为True
            overall_success = True
            if records_added:
                successful_count = sum(1 for r in records_added if r.get("success", False))
                failed_count = len(records_added) - successful_count
                # 如果全部失败，则整体失败
                if successful_count == 0 and failed_count > 0:
                    overall_success = False
            
            return {
                "expert": self.name,
                "success": overall_success,
                "analysis": analysis_text,  # 添加分析文本
                "has_new_data": result.get("has_data", False),
                "records_added": records_added,
                "records_need_confirmation": records_need_confirmation,  # 需要用户确认的记录
                "parsed_data": result.get("records", []),
                "mcp_calls": mcp_calls,  # 保存MCP调用详情
                "has_errors": any(not r.get("success", False) for r in records_added),  # 标记是否有错误
                "has_confirmations": len(records_need_confirmation) > 0  # 标记是否有需要确认的数据
            }
            
        except Exception as e:
            logger.error(f"数据记录专家处理失败: {e}")
            return {
                "expert": self.name,
                "success": False,
                "error": str(e)
            }


class ConsultationExpert(ReActExpert):
    """问诊专家 - 使用ReAct模式收集用户症状和信息"""
    
    def __init__(self, deepseek_client: DeepSeekClient, mcp_client: MCPClient):
        super().__init__(deepseek_client, mcp_client)
        self.name = "问诊专家"
        self.description = "使用ReAct模式收集用户症状、体征和病史信息"
        self.max_iterations = 8  # 安全上限，但主要由模型自己决定何时停止
    
    def get_system_prompt(self) -> str:
        return """你是专业的问诊专家，使用ReAct（推理-行动）模式工作。

你的职责：
1. 通过多步推理和行动，逐步收集和评估信息
2. 判断当前信息是否足够进行诊断评估
3. 如果信息不足，确定需要询问什么

工作模式（ReAct循环）：
- Thought: 分析当前情况，决定下一步
- Action: 执行具体操作（查询数据、检索知识等）
- Observation: 观察操作结果
- Reflection: 评估是否朝目标前进

关注点：
- 症状信息：口渴、多尿、多食、体重变化、疲劳、视力模糊等
- 体征数据：血糖值、血压、身高、体重、BMI
- 病史信息：家族史、既往病史、用药情况
- 生活习惯：饮食、运动、作息

策略：
1. 先查询现有健康数据
2. 评估数据的完整性和时效性
3. 检索诊断标准
4. 对比标准，判断信息是否充足
5. 如果不足，明确还需要什么

何时结束：
- 当已经能够判断信息是否充足时，调用 finish 结束
- 如果数据完整，生成建议；如果不足，列出需要补充的信息
- 通常 2-4 次迭代即可得出结论，不要过度查询

记住：你可以多次查询和分析，但要自主判断何时已经足够。"""
    
    def get_available_actions(self, context: Dict[str, Any]) -> Dict[str, Callable]:
        """获取可用的动作"""
        user_id = context.get("user_id")
        
        async def query_health_data(days: int = 30, record_type: str = None, **kwargs):
            """查询用户健康数据（days: 查询天数, record_type: 记录类型如glucose/pressure/weight）"""
            params = {"user_id": user_id, "days": days}
            if record_type:
                params["record_type"] = record_type
            result = await self._call_mcp_and_track("query_user_health_records", params)
            return self._extract_mcp_data(result)
        
        async def search_knowledge(query: str, top_k: int = 3, **kwargs):
            """检索医学知识（query: 搜索关键词, top_k: 返回结果数量）"""
            params = {"query": query, "top_k": top_k}
            result = await self._call_mcp_and_track("search_diabetes_knowledge", params)
            data = self._extract_mcp_data(result)
            if isinstance(data, dict) and "search_results" in data:
                return data["search_results"]
            return data
        
        async def analyze_completeness(health_data: dict = None, **kwargs):
            """分析数据完整性（health_data: 健康数据字典）"""
            if health_data is None:
                health_data = {}
            analysis = {
                "has_glucose": bool(health_data.get("health_records", {}).get("glucose")),
                "has_pressure": bool(health_data.get("health_records", {}).get("pressure")),
                "has_weight": bool(health_data.get("health_records", {}).get("weight")),
                "glucose_count": len(health_data.get("health_records", {}).get("glucose", [])),
                "pressure_count": len(health_data.get("health_records", {}).get("pressure", [])),
                "weight_count": len(health_data.get("health_records", {}).get("weight", []))
            }
            return analysis
        
        return {
            "query_health_data": query_health_data,
            "search_knowledge": search_knowledge,
            "analyze_completeness": analyze_completeness,
            "finish": lambda **kwargs: "任务完成"
        }
    
    # 继承 ReActExpert 的 process 方法，使用 ReAct 循环


class SynthesisExpert(Expert):
    """综合专家 - 整合各专家意见"""
    
    def __init__(self, deepseek_client: DeepSeekClient, mcp_client: MCPClient):
        super().__init__(deepseek_client, mcp_client)
        self.name = "综合专家"
        self.description = "整合多专家意见的综合专家"
    
    def get_system_prompt(self) -> str:
        return """你是综合专家，负责整合各个专家的分析结果并生成最终回复。

核心职责：
1. 检查问诊专家的评估结果
2. 如果信息不足，友好地向用户提出问诊专家建议的问题
3. 如果信息充足，整合所有专家意见给出完整建议
4. 避免重复内容，提供简洁有价值的回复

回复策略：
- **信息不足时**：以友好的方式提出问题，解释为什么需要这些信息
- **信息充足时**：整合诊断专家、数据专家等的分析，给出综合建议
- **避免说教**：不要重复"我无法诊断"等免责声明（其他专家已说明）
- **重点突出**：聚焦用户最关心的问题

输出要求：
1. 简洁明了，避免冗长重复
2. 如果需要提问，一次不超过3个关键问题
3. 使用Markdown格式，清晰分段"""
    
    async def process(self, context: Dict[str, Any]) -> Dict[str, Any]:
        """整合专家意见"""
        try:
            user_question = context.get("user_question", "")
            expert_results = context.get("expert_results", [])
            conversation_history = context.get("conversation_history", [])
            
            # 检查问诊专家的评估
            consultation_result = None
            for result in expert_results:
                if result.get("expert") == "问诊专家":
                    consultation_result = result
                    break
            
            # 整理专家结果
            results_summary = []
            for result in expert_results:
                if result.get("success"):
                    expert_name = result.get("expert")
                    if expert_name == "问诊专家":
                        # 问诊专家的特殊处理
                        results_summary.append({
                            "expert": expert_name,
                            "info_sufficient": result.get("info_sufficient", True),
                            "questions": result.get("questions", []),
                            "reason": result.get("reason", "")
                        })
                    else:
                        results_summary.append({
                            "expert": expert_name,
                            "content": result.get("analysis") or result.get("explanation") or result.get("recommendation", "")
                        })
            
            # 简化历史记录（只保留最近5轮对话的角色和内容）
            simplified_history = []
            for msg in conversation_history[-10:]:  # 最近10条消息（约5轮对话）
                simplified_history.append({
                    "role": msg.get("role"),
                    "content": msg.get("content", "")[:200]  # 限制每条消息长度
                })
            
            logger.info(f"💬 综合专家使用历史对话记录，共 {len(simplified_history)} 条消息")
            
            # 构建综合提示
            if consultation_result and not consultation_result.get("info_sufficient", True):
                # 信息不足，需要问诊
                prompt = f"""
当前用户问题：{user_question}

历史对话记录：
{json.dumps(simplified_history, ensure_ascii=False, indent=2) if simplified_history else "无历史对话"}

问诊专家评估：信息不足，需要继续收集信息
需要询问的问题：{json.dumps(consultation_result.get('questions', []), ensure_ascii=False)}
原因：{consultation_result.get('reason', '')}

重要提示：请结合历史对话理解用户的完整意图和背景信息。

请以友好、专业的方式向用户提出这些问题，解释为什么需要这些信息。
格式要求：
1. 简短说明当前情况（结合历史对话）
2. 友好地提出问题（不超过3个）
3. 简要说明这些信息的重要性
"""
            else:
                # 信息充足，整合所有专家意见
                prompt = f"""
当前用户问题：{user_question}

历史对话记录：
{json.dumps(simplified_history, ensure_ascii=False, indent=2) if simplified_history else "无历史对话"}

各专家的分析结果：
{json.dumps(results_summary, ensure_ascii=False, indent=2)}

重要提示：请充分结合历史对话记录理解用户的问题和背景。如果用户提到"刚刚"、"之前"等时间词，请参考历史记录。

请整合以上所有专家的意见，生成简洁有价值的回复：
1. 结合历史对话，理解用户的完整意图
2. 避免重复各专家已说的内容
3. 聚焦用户最关心的问题
4. 给出清晰的建议和下一步行动
5. 使用Markdown格式，简洁明了
"""
            
            # 使用AI整合
            messages = [
                {"role": "system", "content": self.get_system_prompt()},
                {"role": "user", "content": prompt}
            ]
            
            response = await self.deepseek_client.chat_completion(
                messages=messages,
                temperature=0.4,
                max_tokens=3000
            )
            
            return {
                "expert": self.name,
                "success": True,
                "final_response": response["message"]["content"],
                "expert_count": len(results_summary)
            }
            
        except Exception as e:
            logger.error(f"综合专家处理失败: {e}")
            return {
                "expert": self.name,
                "success": False,
                "error": str(e)
            }


class Planner:
    """规划器 - 分析问题并制定执行计划，支持动态调整"""
    
    def __init__(self, deepseek_client: DeepSeekClient):
        self.deepseek_client = deepseek_client
        self.adjustment_history = []  # 记录调整历史
    
    async def adjust_plan(
        self,
        original_plan: Dict[str, Any],
        completed_results: List[Dict[str, Any]],
        context: Dict[str, Any]
    ) -> Dict[str, Any]:
        """
        根据已完成专家的结果动态调整计划
        
        Args:
            original_plan: 原始计划
            completed_results: 已完成的专家结果
            context: 上下文信息
            
        Returns:
            调整后的计划（remaining_tasks）
        """
        logger.info(f"🔄 规划器正在评估是否需要调整计划...")
        
        # 分析已完成专家的汇报
        reports = []
        for result in completed_results:
            expert_name = result.get("expert", "未知专家")
            success = result.get("success", False)
            report = result.get("report", "")
            goal_achieved = result.get("goal_achieved", False)
            
            # 对于ReAct专家，提取关键发现
            if result.get("react_mode"):
                steps = result.get("steps", [])
                reports.append({
                    "expert": expert_name,
                    "success": success,
                    "goal_achieved": goal_achieved,
                    "report": report,
                    "iterations": result.get("iterations", 0),
                    "key_findings": [step.get("reflection") for step in steps if step.get("reflection")]
                })
            else:
                reports.append({
                    "expert": expert_name,
                    "success": success,
                    "report": report
                })
        
        # 获取剩余任务
        original_tasks = original_plan.get("tasks", [])
        completed_count = len(completed_results)
        remaining_tasks = original_tasks[completed_count:]
        
        if not remaining_tasks:
            logger.info(f"✅ 所有任务已完成，无需调整")
            return {"adjusted": False, "remaining_tasks": [], "reason": "所有任务已完成"}
        
        # 使用AI评估是否需要调整
        prompt = f"""
你是规划器，需要根据已完成专家的汇报，决定是否调整后续计划。

【原始计划】
{json.dumps(original_tasks, ensure_ascii=False, indent=2)}

【已完成专家汇报】
{json.dumps(reports, ensure_ascii=False, indent=2)}

【剩余任务】
{json.dumps(remaining_tasks, ensure_ascii=False, indent=2)}

请分析：
1. 已完成的专家是否达成了目标？
2. 是否发现了新的问题或缺失的信息？
3. 剩余任务是否仍然必要？
4. 是否需要调整任务顺序或跳过某些任务？

**输出格式（JSON）：**
{{
    "需要调整": true/false,
    "调整理由": "简短说明",
    "调整后的任务": [
        {{"expert": "专家名", "task_description": "任务描述"}},
        ...
    ]
}}

如果不需要调整，"调整后的任务"应与"剩余任务"相同。
只返回JSON，不要其他内容。
"""
        
        try:
            response = await self.deepseek_client.chat_completion(
                messages=[{"role": "user", "content": prompt}],
                temperature=0.3
            )
            
            content = response["message"]["content"].strip()
            
            # 提取JSON
            if "```json" in content:
                content = content.split("```json")[1].split("```")[0].strip()
            elif "```" in content:
                content = content.split("```")[1].split("```")[0].strip()
            
            adjustment = json.loads(content)
            
            needs_adjustment = adjustment.get("需要调整", False)
            reason = adjustment.get("调整理由", "")
            adjusted_tasks = adjustment.get("调整后的任务", remaining_tasks)
            
            if needs_adjustment:
                logger.info(f"🔄 计划已调整: {reason}")
                self.adjustment_history.append({
                    "after_expert": completed_results[-1].get("expert"),
                    "reason": reason,
                    "original_remaining": len(remaining_tasks),
                    "adjusted_remaining": len(adjusted_tasks)
                })
            else:
                logger.info(f"✅ 计划无需调整，继续执行")
            
            return {
                "adjusted": needs_adjustment,
                "reason": reason,
                "remaining_tasks": adjusted_tasks
            }
            
        except Exception as e:
            logger.error(f"计划调整失败: {e}，继续执行原计划")
            return {
                "adjusted": False,
                "reason": f"调整失败: {e}",
                "remaining_tasks": remaining_tasks
            }
    
    async def create_plan(self, user_question: str, user_info: Dict[str, Any], conversation_history: List[Dict[str, Any]] = None) -> Dict[str, Any]:
        """
        创建执行计划
        
        Args:
            user_question: 用户问题
            user_info: 用户信息
            conversation_history: 历史对话记录（可选）
            
        Returns:
            执行计划
        """
        if conversation_history is None:
            conversation_history = []
        
        # 简化历史记录
        simplified_history = []
        for msg in conversation_history[-6:]:  # 最近6条消息（约3轮对话）
            simplified_history.append({
                "role": msg.get("role"),
                "content": msg.get("content", "")[:150]
            })
        
        logger.info(f"📋 规划器使用历史对话记录，共 {len(simplified_history)} 条消息")
        try:
            # 构建规划提示
            messages = [
                {"role": "system", "content": """你是一个智能规划器，负责分析用户问题并决定需要调用哪些专家，同时为每个专家分配具体的任务。

可用的专家：
1. 数据记录专家（DataRecordExpert）- **仅能记录以下3种健康数据：血糖、血压、体重**
2. 问诊专家（ConsultationExpert）- 评估信息充足性，提出需要询问的问题
3. 数据专家（DataExpert）- 查询和分析健康数据
4. 知识专家（KnowledgeExpert）- 检索医学知识库
5. 诊断专家（DiagnosisExpert）- 分析症状和诊断（仅在信息充足时调用）
6. 医生推荐专家（DoctorExpert）- 推荐合适的医生
7. 综合专家（SynthesisExpert）- 整合各专家意见（总是最后调用）

重要规则：
- **只有当用户明确提到血糖、血压、体重的具体数值时，才调用DataRecordExpert**
- 例如："血糖7.3"、"血压120/80"、"体重70kg" → 调用DataRecordExpert
- 例如："我感觉头晕"、"我想诊断"、"帮我看看" → **不要**调用DataRecordExpert
- 如果用户寻求诊断或健康评估，调用ConsultationExpert评估信息是否充足
- 只有信息充足时才调用DiagnosisExpert
- SynthesisExpert总是最后一个

执行顺序：
[DataRecordExpert（仅当有数值数据时）] → ConsultationExpert → [其他专家] → SynthesisExpert

请根据用户问题，返回一个JSON格式的执行计划，格式如下：
{
    "tasks": [
        {
            "expert": "DataRecordExpert",
            "task_description": "识别并记录用户提到的血糖数据7.3mmol/L"
        },
        {
            "expert": "ConsultationExpert",
            "task_description": "评估当前信息是否足够进行健康评估，询问必要的补充信息"
        }
    ],
    "reasoning": "用户提到了血糖数据，先记录下来，然后评估现有信息是否足够诊断，如果不足就询问更多必要信息"
}

专家名称使用：DataRecordExpert, ConsultationExpert, DataExpert, KnowledgeExpert, DiagnosisExpert, DoctorExpert, SynthesisExpert

**任务描述要求**：
- 要具体明确，说明该专家需要完成什么
- 要与用户问题相关，不要泛泛而谈
- 要便于专家理解和执行

**reasoning字段要求（重要）**：
- 使用用户能理解的自然语言，像对朋友说话一样
- **禁止使用专家名称**（如DataRecordExpert、ConsultationExpert等）
- **禁止使用技术术语**（如"调用"、"执行"、"模块"等）
- 用通俗的语言描述分析思路和处理步骤
- 示例："用户提到了血糖数据，先记录下来，然后评估是否足够诊断"
- 而不是："调用DataRecordExpert记录数据，然后执行ConsultationExpert评估"

注意：只返回JSON，不要其他内容
"""},
                {"role": "user", "content": f"""
当前用户问题：{user_question}

历史对话记录：
{json.dumps(simplified_history, ensure_ascii=False, indent=2) if simplified_history else "无历史对话"}

用户信息：{json.dumps(user_info, ensure_ascii=False)}

重要提示：
- 如果用户问题涉及"刚刚"、"之前"、"刚才"等时间词，请参考历史对话理解完整语境
- 如果用户在追问或继续之前的话题，请考虑之前的对话内容
- **DataRecordExpert调用判断**：
  * 用户明确提到血糖/血压/体重数值 → 需要调用
  * 用户只是询问、诊断、症状描述 → 不需要调用
  * 例："血糖7.3" ✅调用；"我头晕" ❌不调用；"帮我诊断" ❌不调用

**reasoning撰写要求（必须遵守）**：
- ✅ 好的示例："用户提到血糖7.3和头晕症状，先记录血糖数据，再评估症状与血糖的关系"
- ❌ 差的示例："调用DataRecordExpert记录数据，然后由ConsultationExpert评估信息充足性"
- 使用"记录"、"评估"、"分析"、"查询"等通俗动词
- 避免"调用"、"执行"、"模块"、"专家"等技术词汇

请制定执行计划。
"""}
            ]
            
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
            
            plan = json.loads(content)
            
            # 兼容旧格式（experts数组）和新格式（tasks数组）
            if "tasks" in plan:
                # 新格式：包含任务描述
                # 确保SynthesisExpert在最后
                expert_names = [task["expert"] for task in plan["tasks"]]
                if "SynthesisExpert" not in expert_names:
                    plan["tasks"].append({
                        "expert": "SynthesisExpert",
                        "task_description": "整合所有专家的意见，生成综合性的回复"
                    })
            elif "experts" in plan:
                # 旧格式：仅有专家名称列表，转换为新格式
                tasks = []
                for expert in plan["experts"]:
                    tasks.append({
                        "expert": expert,
                        "task_description": f"执行{expert}的标准职责"
                    })
                if "SynthesisExpert" not in plan["experts"]:
                    tasks.append({
                        "expert": "SynthesisExpert",
                        "task_description": "整合所有专家的意见，生成综合性的回复"
                    })
                plan["tasks"] = tasks
                # 保留experts字段以便向后兼容
                plan["experts"] = [task["expert"] for task in tasks]
            else:
                # 格式错误，使用默认计划
                raise ValueError("计划格式不正确")
            
            # 添加experts字段（向后兼容）
            if "experts" not in plan:
                plan["experts"] = [task["expert"] for task in plan["tasks"]]
            
            logger.info(f"📋 执行计划: {plan['reasoning']}")
            logger.info(f"📋 任务列表: {json.dumps(plan['tasks'], ensure_ascii=False, indent=2)}")
            return plan
            
        except Exception as e:
            logger.error(f"规划失败: {e}")
            # 返回默认计划（新格式）
            return {
                "tasks": [
                    {"expert": "DataExpert", "task_description": "查询用户的健康数据"},
                    {"expert": "KnowledgeExpert", "task_description": "检索相关医学知识"},
                    {"expert": "DiagnosisExpert", "task_description": "分析健康状况"},
                    {"expert": "SynthesisExpert", "task_description": "整合各专家意见"}
                ],
                "experts": ["DataExpert", "KnowledgeExpert", "DiagnosisExpert", "SynthesisExpert"],
                "reasoning": "使用默认计划（规划失败）"
            }


class Executor:
    """执行器 - 按计划执行专家任务，支持动态调整"""
    
    def __init__(self, deepseek_client: DeepSeekClient, mcp_client: MCPClient, planner: 'Planner' = None):
        self.deepseek_client = deepseek_client
        self.mcp_client = mcp_client
        self.planner = planner  # 规划器引用，用于动态调整
        
        # 初始化所有专家
        self.experts = {
            "DataRecordExpert": DataRecordExpert(deepseek_client, mcp_client),
            "ConsultationExpert": ConsultationExpert(deepseek_client, mcp_client),
            "DataExpert": DataExpert(deepseek_client, mcp_client),
            "KnowledgeExpert": KnowledgeExpert(deepseek_client, mcp_client),
            "DiagnosisExpert": DiagnosisExpert(deepseek_client, mcp_client),
            "DoctorExpert": DoctorExpert(deepseek_client, mcp_client),
            "SynthesisExpert": SynthesisExpert(deepseek_client, mcp_client)
        }
        
        # 定义决策点（在哪些专家完成后评估是否调整计划）
        self.decision_points = [
            "ConsultationExpert",  # 问诊后
            "DataExpert"           # 数据分析后
        ]
    
    async def execute_plan(self, plan: Dict[str, Any], context: Dict[str, Any]) -> Dict[str, Any]:
        """
        执行计划 - 支持动态调整
        
        Args:
            plan: 执行计划（包含tasks或experts）
            context: 上下文信息
            
        Returns:
            执行结果
        """
        # 获取任务列表（优先使用tasks，否则使用experts）
        tasks = plan.get("tasks", [])
        if not tasks:
            # 向后兼容：如果没有tasks，从experts创建简单任务
            expert_names = plan.get("experts", [])
            tasks = [{"expert": name, "task_description": f"执行{name}的标准职责"} for name in expert_names]
        
        results = []
        adjustments = []  # 记录所有调整
        
        logger.info(f"🚀 开始执行计划，共{len(tasks)}个任务")
        logger.info(f"📋 计划理由: {plan.get('reasoning', '无')}")
        
        # 使用索引而不是enumerate，因为tasks可能会被调整
        task_idx = 0
        while task_idx < len(tasks):
            task = tasks[task_idx]
            expert_name = task.get("expert")
            task_description = task.get("task_description", "")
            
            expert = self.experts.get(expert_name)
            if not expert:
                logger.warning(f"专家 {expert_name} 不存在")
                task_idx += 1
                continue
            
            logger.info(f"🤖 [{task_idx+1}/{len(tasks)}] 正在调用 {expert.name}")
            logger.info(f"📝 任务: {task_description}")
            
            # 将任务描述添加到上下文
            context["assigned_task"] = task_description
            context["task_index"] = task_idx + 1
            context["total_tasks"] = len(tasks)
            
            # 执行专家任务
            result = await expert.process(context)
            
            # 添加任务信息到结果中
            result["assigned_task"] = task_description
            result["task_index"] = task_idx + 1
            
            # 生成任务完成汇报
            if result.get("success"):
                completion_report = result.get("completion_report") or self._generate_completion_report(expert_name, task_description, result)
                result["completion_report"] = completion_report
                logger.info(f"✅ [{task_idx+1}/{len(tasks)}] {expert.name} 完成: {completion_report}")
                
                # 对于ReAct专家，展示思考过程
                if result.get("react_mode"):
                    logger.info(f"   🔄 ReAct迭代: {result.get('iterations')}次")
                    logger.info(f"   🎯 目标达成: {'是' if result.get('goal_achieved') else '否'}")
            else:
                error_msg = result.get("error", "未知错误")
                result["completion_report"] = f"任务失败: {error_msg}"
                logger.error(f"❌ [{task_idx+1}/{len(tasks)}] {expert.name} 失败: {error_msg}")
            
            results.append(result)
            
            # ========== 决策点：评估是否需要调整计划 ==========
            if self.planner and expert_name in self.decision_points:
                logger.info(f"🔍 到达决策点: {expert_name} 完成后")
                
                # 调用规划器评估是否调整
                adjustment = await self.planner.adjust_plan(
                    original_plan={"tasks": tasks},
                    completed_results=results,
                    context=context
                )
                
                if adjustment.get("adjusted"):
                    logger.info(f"🔄 计划已调整: {adjustment.get('reason')}")
                    # 更新剩余任务
                    remaining_tasks = adjustment.get("remaining_tasks", [])
                    tasks = results + [{"expert": r.get("expert"), "task_description": r.get("assigned_task")} for r in results] + remaining_tasks
                    # 重新计算tasks（已完成 + 调整后的剩余）
                    tasks = [{"expert": r.get("expert"), "task_description": r.get("assigned_task")} for r in results] + remaining_tasks
                    adjustments.append({
                        "after_expert": expert_name,
                        "reason": adjustment.get("reason"),
                        "timestamp": datetime.now().isoformat()
                    })
                    # 注意：不改变task_idx，因为tasks已经重新构建
                    logger.info(f"📋 调整后剩余任务数: {len(remaining_tasks)}")
                else:
                    logger.info(f"✅ 无需调整，继续执行原计划")
            
            task_idx += 1
        
        # 执行完所有任务后，更新上下文给综合专家
        if results:
            context["expert_results"] = results[:-1]  # 最后一个通常是综合专家自己
        
        return {
            "success": True,
            "results": results,
            "final_response": results[-1].get("final_response", "") if results else "",
            "plan_reasoning": plan.get("reasoning", ""),
            "adjustments": adjustments,  # 添加调整记录
            "total_adjustments": len(adjustments)
        }
    
    def _generate_completion_report(self, expert_name: str, task_description: str, result: Dict[str, Any]) -> str:
        """
        生成任务完成汇报
        
        Args:
            expert_name: 专家名称
            task_description: 任务描述
            result: 执行结果
            
        Returns:
            完成汇报文本
        """
        # 根据不同专家类型生成不同的汇报
        if expert_name == "DataRecordExpert":
            has_data = result.get("has_new_data", False)
            records_count = len(result.get("records_added", []))
            if has_data and records_count > 0:
                return f"已识别并记录{records_count}条健康数据"
            elif result.get("has_confirmations"):
                return "发现需要用户确认的数据"
            else:
                return "未检测到需要记录的健康数据"
        
        elif expert_name == "ConsultationExpert":
            needs_more = result.get("needs_more_info", False)
            if needs_more:
                questions_count = len(result.get("questions", []))
                return f"信息不足，提出{questions_count}个问题"
            else:
                return "当前信息充足，可以进行评估"
        
        elif expert_name == "DataExpert":
            has_data = result.get("has_data", False)
            if has_data:
                return "已查询并分析用户健康数据"
            else:
                return "暂无相关健康数据"
        
        elif expert_name == "KnowledgeExpert":
            knowledge_count = len(result.get("knowledge", []))
            if knowledge_count > 0:
                return f"检索到{knowledge_count}条相关医学知识"
            else:
                return "未找到相关医学知识"
        
        elif expert_name == "DiagnosisExpert":
            return "已完成健康评估和诊断分析"
        
        elif expert_name == "DoctorExpert":
            doctors_count = len(result.get("doctors", []))
            if doctors_count > 0:
                return f"推荐{doctors_count}位合适的医生"
            else:
                return "暂无合适的医生推荐"
        
        elif expert_name == "SynthesisExpert":
            return "已整合所有专家意见，生成综合回复"
        
        return "任务完成"

