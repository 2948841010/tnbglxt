# MCP RAG工具集成说明

## 概述

本文档描述了在MCP (Multi-modal Conversational Platform) 服务中集成的RAG (检索增强生成) 工具集。这些工具通过调用localhost:8001上的RAG API服务，为Agent提供强大的糖尿病知识库检索能力。

## 集成的RAG工具

### 1. `rag_health_check()`
**功能**: 检查RAG检索服务的健康状态

**参数**: 无

**返回**: JSON格式的健康状态信息
- `service_status`: 服务状态 (healthy/unhealthy/unavailable)
- `model_loaded`: BGE模型是否已加载
- `database_connected`: ChromaDB是否连接正常
- `cache_connected`: Redis缓存是否连接正常
- `total_documents`: 知识库总文档数
- `uptime_seconds`: 服务运行时间(秒)
- `gpu_available`: GPU是否可用
- `memory_usage`: GPU内存使用情况

**使用场景**: 
- 在使用其他RAG工具前检查服务状态
- 监控RAG服务是否正常运行
- 调试RAG服务连接问题

### 2. `search_diabetes_knowledge(query, top_k=5, similarity_threshold=0.0, use_cache=True, include_entities=True, category_filter="")`
**功能**: 搜索糖尿病知识库，获取相关医学信息

**参数**:
- `query` (必需): 查询问题，如"糖尿病的主要症状有哪些？"
- `top_k` (可选): 返回结果数量，默认5条 (1-20)
- `similarity_threshold` (可选): 相似度阈值，默认0.0 (0.0-1.0)
- `use_cache` (可选): 是否使用缓存，默认True
- `include_entities` (可选): 是否包含医学实体信息，默认True
- `category_filter` (可选): 分类过滤器，多个分类用逗号分隔，如"眼部疾病,神经疾病"

**返回**: JSON格式的搜索结果
- `success`: 搜索是否成功
- `query`: 原始查询问题
- `search_results`: 搜索结果数组
  - `rank`: 排名
  - `question`: 匹配的问题
  - `answer`: 答案内容
  - `category`: 分类
  - `similarity_score`: 相似度分数
  - `medical_entities`: 医学实体列表
  - `source_info`: 来源信息
- `search_summary`: 搜索摘要
  - `total_found`: 找到的总数
  - `returned_count`: 返回的数量
  - `cache_hit`: 是否命中缓存
  - `search_time_seconds`: 搜索耗时
- `search_parameters`: 搜索参数
- `timestamp`: 搜索时间戳

**使用场景**:
- 回答用户关于糖尿病的问题
- 为医生提供专业的医学知识支持
- 智能问答系统的知识检索

### 3. `get_diabetes_knowledge_categories()`
**功能**: 获取糖尿病知识库的所有可用分类

**参数**: 无

**返回**: JSON格式的分类信息
- `success`: 是否成功
- `available_categories`: 可用分类字典
  - 键: 分类名称
  - 值: 分类描述
- `total_documents`: 总文档数
- `service_status`: 服务状态
- `usage_tips`: 使用提示数组

**可用分类**:
- `眼部疾病`: 糖尿病视网膜病变等眼部并发症相关信息
- `神经疾病`: 糖尿病神经病变相关信息
- `基础知识`: 糖尿病基础概念、定义等
- `治疗方法`: 糖尿病治疗方案、药物治疗等
- `诊断检查`: 糖尿病诊断标准、检查方法等
- `其他`: 其他糖尿病相关信息

**使用场景**:
- 了解知识库包含的内容分类
- 为用户提供分类选择
- 指导如何使用分类过滤器

### 4. `clear_rag_cache()`
**功能**: 清理RAG检索服务的缓存

**参数**: 无

**返回**: JSON格式的缓存清理结果
- `success`: 是否成功
- `message`: 操作消息
- `timestamp`: 操作时间戳

**使用场景**:
- 当知识库更新后清理旧缓存
- 解决缓存相关的问题
- 强制刷新检索结果

## 使用示例

### 基础知识查询
```python
# 查询糖尿病症状
result = search_diabetes_knowledge(
    query="糖尿病有什么症状？",
    top_k=3,
    include_entities=True
)
```

### 分类过滤查询
```python
# 只搜索眼部疾病相关内容
result = search_diabetes_knowledge(
    query="视网膜病变如何治疗？",
    top_k=5,
    category_filter="眼部疾病"
)
```

### 高精度查询
```python
# 设置较高的相似度阈值
result = search_diabetes_knowledge(
    query="血糖控制方法",
    top_k=5,
    similarity_threshold=0.3,
    use_cache=False
)
```

## 服务依赖

### 必需服务
1. **RAG API服务**: 运行在localhost:8001
   - BGE-large-zh-v1.5模型已加载
   - ChromaDB向量数据库已初始化
   - 糖尿病知识库数据已导入

2. **Redis服务** (可选): 用于缓存优化
   - 运行在localhost:6379
   - 如果不可用，系统会自动降级为无缓存模式

### 启动顺序
1. 启动Redis服务 (可选)
2. 启动RAG API服务: `python rag/start_api.py`
3. 启动MCP服务: `python mcp/server.py`

## 错误处理

### 常见错误及解决方案

1. **RAG服务不可用**
   ```json
   {
     "success": false,
     "error": "无法连接到RAG服务 (http://localhost:8001)",
     "suggestion": "请确保RAG API服务正在运行"
   }
   ```
   **解决方案**: 检查RAG API服务是否启动

2. **查询超时**
   ```json
   {
     "success": false,
     "error": "RAG服务响应超时 (>30秒)"
   }
   ```
   **解决方案**: 检查服务负载，考虑优化查询参数

3. **查询内容为空**
   ```json
   {
     "success": false,
     "error": "查询内容不能为空"
   }
   ```
   **解决方案**: 确保query参数不为空

## 性能优化建议

### 1. 合理使用缓存
- 对于重复查询，启用缓存(`use_cache=True`)
- 定期清理缓存以获取最新结果

### 2. 优化查询参数
- 根据需求调整`top_k`参数，避免返回过多无用结果
- 使用`similarity_threshold`过滤低相关性结果
- 合理使用`category_filter`缩小搜索范围

### 3. 并发控制
- MCP工具调用具有30秒超时保护
- RAG API服务支持并发请求

## 监控和维护

### 健康检查
定期调用`rag_health_check()`监控服务状态：
```python
health = rag_health_check()
# 检查返回的service_status字段
```

### 性能监控
通过搜索结果的`search_summary`监控性能：
- `cache_hit`: 缓存命中率
- `search_time_seconds`: 搜索耗时
- `api_response_time_seconds`: API响应时间

### 容量管理
- 监控`total_documents`了解知识库规模
- 观察GPU内存使用情况
- 根据使用情况调整Redis缓存TTL

## 集成测试

使用提供的测试脚本验证集成是否正常：
```bash
python mcp/test_rag_tools.py
```

测试包括：
- RAG服务健康检查
- 知识搜索功能
- 分类获取功能
- 缓存操作功能

## 总结

MCP RAG工具集为Agent提供了强大的糖尿病知识库检索能力，具备以下特点：

- ✅ **易于使用**: 简单的函数调用接口
- ✅ **功能完整**: 涵盖搜索、分类、缓存管理等功能
- ✅ **性能优化**: 支持缓存、GPU加速
- ✅ **错误处理**: 完善的异常处理和容错机制
- ✅ **可监控**: 详细的状态信息和性能指标

通过这些工具，Agent可以为用户提供准确、及时的糖尿病相关医学知识服务。 