#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
糖尿病知识库数据清洗和文本分块处理脚本
根据RAG_Retrieval_Service_Plan.md实现数据预处理流程
"""

import pandas as pd
import json
import re
import os
from typing import List, Dict, Tuple, Optional
from pathlib import Path
import hashlib
from datetime import datetime
import logging

# 配置日志
logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(levelname)s - %(message)s',
    handlers=[
        logging.StreamHandler(),
        logging.FileHandler('data_processing.log', encoding='utf-8')
    ]
)
logger = logging.getLogger(__name__)

class DiabetesDataProcessor:
    """糖尿病知识库数据处理器"""
    
    def __init__(self, csv_file: str, output_dir: str = "processed_data"):
        self.csv_file = csv_file
        self.output_dir = Path(output_dir)
        self.output_dir.mkdir(exist_ok=True)
        
        # 医学术语标准化词典
        self.medical_terms = {
            # 糖尿病相关术语
            "糖尿病": ["糖尿病", "DM", "diabetes"],
            "1型糖尿病": ["1型糖尿病", "I型糖尿病", "胰岛素依赖型糖尿病", "IDDM"],
            "2型糖尿病": ["2型糖尿病", "II型糖尿病", "非胰岛素依赖型糖尿病", "NIDDM"],
            "血糖": ["血糖", "血糖值", "血糖水平", "glucose"],
            "胰岛素": ["胰岛素", "insulin"],
            "并发症": ["并发症", "合并症"],
            # 眼部疾病术语
            "视网膜病变": ["视网膜病变", "retinopathy"],
            "黄斑水肿": ["黄斑水肿", "macular edema"],
            "视力下降": ["视力下降", "视力减退", "视力模糊"],
            "飞蚊症": ["飞蚊症", "飞蚊", "眼前飞蚊"],
        }
        
        # 疾病分类
        self.disease_categories = {
            "眼部疾病": ["视网膜病变", "黄斑水肿", "玻璃体出血", "视网膜脱离"],
            "肾脏疾病": ["糖尿病肾病", "肾功能不全"],
            "神经疾病": ["糖尿病神经病变", "周围神经病变"],
            "心血管疾病": ["心血管并发症", "冠心病"],
            "基础知识": ["糖尿病定义", "病因", "分类"],
            "治疗方法": ["药物治疗", "手术治疗", "激光治疗"],
            "诊断检查": ["眼底检查", "血管造影", "超声检查"]
        }
    
    def clean_text(self, text: str) -> str:
        """清洗文本内容"""
        if not text or pd.isna(text):
            return ""
        
        # 移除多余空白字符
        text = re.sub(r'\s+', ' ', str(text).strip())
        
        # 移除HTML标签（如果有）
        text = re.sub(r'<[^>]+>', '', text)
        
        # 规范化标点符号
        text = text.replace('？', '？').replace('！', '！').replace('，', '，')
        
        # 移除重复的标点符号
        text = re.sub(r'([。！？，])\1+', r'\1', text)
        
        return text.strip()
    
    def standardize_medical_terms(self, text: str) -> str:
        """标准化医学术语"""
        for standard_term, variants in self.medical_terms.items():
            for variant in variants:
                if variant != standard_term:
                    text = text.replace(variant, standard_term)
        return text
    
    def extract_category(self, question: str, context: str) -> str:
        """从问题和上下文中提取疾病分类"""
        combined_text = f"{question} {context}".lower()
        
        for category, keywords in self.disease_categories.items():
            for keyword in keywords:
                if keyword.lower() in combined_text:
                    return category
        
        return "其他"
    
    def extract_medical_entities(self, text: str) -> List[str]:
        """提取医学实体"""
        entities = []
        text_lower = text.lower()
        
        for standard_term, variants in self.medical_terms.items():
            for variant in variants:
                if variant.lower() in text_lower:
                    if standard_term not in entities:
                        entities.append(standard_term)
                    break
        
        return entities
    
    def chunk_long_text(self, text: str, max_length: int = 500) -> List[str]:
        """对长文本进行分块处理"""
        if len(text) <= max_length:
            return [text]
        
        chunks = []
        sentences = re.split(r'[。！？]', text)
        current_chunk = ""
        
        for sentence in sentences:
            sentence = sentence.strip()
            if not sentence:
                continue
                
            sentence += "。"  # 恢复标点
            
            if len(current_chunk + sentence) <= max_length:
                current_chunk += sentence
            else:
                if current_chunk:
                    chunks.append(current_chunk.strip())
                current_chunk = sentence
        
        if current_chunk:
            chunks.append(current_chunk.strip())
        
        return chunks if chunks else [text]
    
    def generate_chunk_id(self, text: str, index: int = 0) -> str:
        """生成块的唯一ID"""
        text_hash = hashlib.md5(text.encode('utf-8')).hexdigest()[:8]
        return f"chunk_{text_hash}_{index}"
    
    def process_qa_pair(self, question: str, context: str, row_index: int) -> List[Dict]:
        """处理单个QA对"""
        # 清洗文本
        question = self.clean_text(question)
        context = self.clean_text(context)
        
        if not question or not context:
            return []
        
        # 标准化医学术语
        question = self.standardize_medical_terms(question)
        context = self.standardize_medical_terms(context)
        
        # 提取分类和实体
        category = self.extract_category(question, context)
        entities = self.extract_medical_entities(f"{question} {context}")
        
        # 分块处理context（如果太长）
        context_chunks = self.chunk_long_text(context)
        
        processed_chunks = []
        
        for chunk_idx, chunk in enumerate(context_chunks):
            chunk_data = {
                "id": self.generate_chunk_id(f"{question}_{chunk}", chunk_idx),
                "question": question,
                "context": chunk,
                "category": category,
                "entities": entities,
                "source_row": row_index,
                "chunk_index": chunk_idx,
                "total_chunks": len(context_chunks),
                "text_length": len(chunk),
                "processed_at": datetime.now().isoformat()
            }
            processed_chunks.append(chunk_data)
        
        return processed_chunks
    
    def detect_duplicates(self, data: List[Dict]) -> List[int]:
        """检测重复内容"""
        seen_content = set()
        duplicates = []
        
        for i, item in enumerate(data):
            content_key = f"{item['question']}_{item['context']}"
            content_hash = hashlib.md5(content_key.encode('utf-8')).hexdigest()
            
            if content_hash in seen_content:
                duplicates.append(i)
            else:
                seen_content.add(content_hash)
        
        return duplicates
    
    def validate_data_quality(self, data: List[Dict]) -> Dict:
        """验证数据质量"""
        stats = {
            "total_chunks": len(data),
            "empty_questions": 0,
            "empty_contexts": 0,
            "short_contexts": 0,  # 少于10个字符
            "long_contexts": 0,   # 超过1000个字符
            "categories": {},
            "avg_text_length": 0,
            "duplicates": 0
        }
        
        total_length = 0
        
        for item in data:
            # 检查空内容
            if not item["question"].strip():
                stats["empty_questions"] += 1
            if not item["context"].strip():
                stats["empty_contexts"] += 1
            
            # 检查文本长度
            context_len = len(item["context"])
            total_length += context_len
            
            if context_len < 10:
                stats["short_contexts"] += 1
            elif context_len > 1000:
                stats["long_contexts"] += 1
            
            # 统计分类
            category = item["category"]
            stats["categories"][category] = stats["categories"].get(category, 0) + 1
        
        # 计算平均长度
        if data:
            stats["avg_text_length"] = total_length / len(data)
        
        # 检测重复
        duplicates = self.detect_duplicates(data)
        stats["duplicates"] = len(duplicates)
        
        return stats, duplicates
    
    def process_csv(self) -> Tuple[List[Dict], Dict]:
        """处理CSV文件"""
        logger.info(f"开始处理文件: {self.csv_file}")
        
        # 读取CSV文件
        try:
            df = pd.read_csv(self.csv_file, encoding='utf-8')
            logger.info(f"成功读取CSV文件，共 {len(df)} 行数据")
        except Exception as e:
            logger.error(f"读取CSV文件失败: {e}")
            raise
        
        # 检查必要的列
        if 'question' not in df.columns or 'context' not in df.columns:
            raise ValueError("CSV文件必须包含 'question' 和 'context' 列")
        
        # 处理每一行数据
        all_chunks = []
        processed_count = 0
        error_count = 0
        
        for index, row in df.iterrows():
            try:
                chunks = self.process_qa_pair(
                    row['question'], 
                    row['context'], 
                    index
                )
                all_chunks.extend(chunks)
                processed_count += len(chunks)
                
                if index % 100 == 0:
                    logger.info(f"已处理 {index} 行，生成 {processed_count} 个数据块")
                    
            except Exception as e:
                logger.error(f"处理第 {index} 行时出错: {e}")
                error_count += 1
                continue
        
        logger.info(f"数据处理完成: 处理 {len(df)} 行，生成 {len(all_chunks)} 个数据块，错误 {error_count} 个")
        
        # 数据质量检查
        quality_stats, duplicates = self.validate_data_quality(all_chunks)
        
        # 移除重复项
        if duplicates:
            logger.info(f"发现 {len(duplicates)} 个重复项，正在移除...")
            all_chunks = [chunk for i, chunk in enumerate(all_chunks) if i not in duplicates]
            quality_stats["total_chunks"] = len(all_chunks)
        
        return all_chunks, quality_stats
    
    def save_processed_data(self, data: List[Dict], stats: Dict):
        """保存处理后的数据"""
        # 保存处理后的数据
        data_file = self.output_dir / "diabetes_qa_processed.json"
        with open(data_file, 'w', encoding='utf-8') as f:
            json.dump(data, f, ensure_ascii=False, indent=2)
        logger.info(f"处理后的数据已保存到: {data_file}")
        
        # 保存质量统计
        stats_file = self.output_dir / "data_quality_stats.json"
        with open(stats_file, 'w', encoding='utf-8') as f:
            json.dump(stats, f, ensure_ascii=False, indent=2)
        logger.info(f"数据质量统计已保存到: {stats_file}")
        
        # 生成可读的统计报告
        report_file = self.output_dir / "processing_report.txt"
        with open(report_file, 'w', encoding='utf-8') as f:
            f.write("糖尿病知识库数据处理报告\n")
            f.write("=" * 50 + "\n\n")
            f.write(f"处理时间: {datetime.now().strftime('%Y-%m-%d %H:%M:%S')}\n")
            f.write(f"数据总量: {stats['total_chunks']} 个数据块\n")
            f.write(f"平均文本长度: {stats['avg_text_length']:.1f} 字符\n")
            f.write(f"重复项: {stats['duplicates']} 个\n")
            f.write(f"空问题: {stats['empty_questions']} 个\n")
            f.write(f"空答案: {stats['empty_contexts']} 个\n")
            f.write(f"短文本: {stats['short_contexts']} 个 (< 10字符)\n")
            f.write(f"长文本: {stats['long_contexts']} 个 (> 1000字符)\n\n")
            
            f.write("分类分布:\n")
            for category, count in stats['categories'].items():
                percentage = (count / stats['total_chunks']) * 100
                f.write(f"  {category}: {count} 个 ({percentage:.1f}%)\n")
        
        logger.info(f"处理报告已保存到: {report_file}")
        
        return data_file, stats_file, report_file


def main():
    """主函数"""
    print("=" * 60)
    print("🧹 糖尿病知识库数据清洗和文本分块")
    print("=" * 60)
    
    # 创建处理器
    processor = DiabetesDataProcessor("diabetes_qa_chinese.csv")
    
    try:
        # 处理数据
        processed_data, quality_stats = processor.process_csv()
        
        # 保存数据
        data_file, stats_file, report_file = processor.save_processed_data(
            processed_data, quality_stats
        )
        
        print("\n✅ 数据处理完成！")
        print(f"📊 总数据块: {quality_stats['total_chunks']}")
        print(f"📈 平均长度: {quality_stats['avg_text_length']:.1f} 字符")
        print(f"🗂️  分类数量: {len(quality_stats['categories'])}")
        print(f"📁 输出文件:")
        print(f"   - 处理数据: {data_file}")
        print(f"   - 质量统计: {stats_file}")
        print(f"   - 处理报告: {report_file}")
        
        print("\n📋 分类分布:")
        for category, count in quality_stats['categories'].items():
            percentage = (count / quality_stats['total_chunks']) * 100
            print(f"   {category}: {count} 个 ({percentage:.1f}%)")
        
    except Exception as e:
        logger.error(f"数据处理失败: {e}")
        print(f"❌ 处理失败: {e}")
        return False
    
    print("\n" + "=" * 60)
    print("🎉 数据清洗和分块完成!")
    print("📝 下一步: 进行向量化处理")
    print("=" * 60)
    
    return True


if __name__ == "__main__":
    success = main()
    exit(0 if success else 1) 