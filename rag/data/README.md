# 糖尿病QA数据集预处理工具

这个工具用于处理韩文糖尿病QA数据集，使用DeepSeek API将其翻译为专业的中文医学文本。

## 功能特性

- 📊 读取Parquet格式的韩文医学数据
- 🌐 使用DeepSeek API进行专业医学翻译
- 🏥 专门针对糖尿病相关术语优化
- 📝 输出韩文原文和中文翻译两个CSV文件
- 📋 详细的翻译日志和统计信息
- ⏱️ 智能请求间隔，避免API限制

## 安装依赖

```bash
pip install -r requirements.txt
```

## 使用方法

1. 确保 `train-00000-of-00001.parquet` 文件在当前目录
2. 运行预处理脚本：

```bash
python diabetes_data_preprocess.py
```

## 输出文件

- `diabetes_qa_korean.csv` - 韩文原文CSV文件
- `diabetes_qa_chinese.csv` - 中文翻译CSV文件
- `translation.log` - 翻译过程日志
- `translation_stats.json` - 处理统计信息

## 数据格式

输入的Parquet文件应包含以下列：
- `question`: 韩文问题
- `context`: 韩文回答/上下文

输出的CSV文件保持相同的列结构，但内容为对应的中文翻译。

## 翻译质量保证

- 使用专门的医学翻译提示词
- 针对糖尿病相关术语进行优化
- 低温度设置确保翻译一致性
- 保持原文的结构和专业性

## 注意事项

- 翻译过程可能需要较长时间（284条数据约需5-10分钟）
- 确保网络连接稳定
- API密钥已内置在脚本中
- 建议在翻译完成后检查部分结果的质量 