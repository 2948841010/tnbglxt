#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
糖尿病QA数据集预处理脚本
- 读取韩文parquet文件
- 使用DeepSeek API翻译为专业中文医学文本
- 输出韩文原文和中文翻译的CSV文件
"""

import pandas as pd
import requests
import json
import time
import os
from typing import Dict, List, Tuple
import logging
from tqdm import tqdm

# 设置日志
logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(levelname)s - %(message)s',
    handlers=[
        logging.FileHandler('translation.log', encoding='utf-8'),
        logging.StreamHandler()
    ]
)
logger = logging.getLogger(__name__)

class DeepSeekTranslator:
    """DeepSeek API翻译器，专门用于医学文本翻译"""
    
    def __init__(self, api_key: str):
        self.api_key = api_key
        self.base_url = "https://api.deepseek.com/v1/chat/completions"
        self.headers = {
            "Authorization": f"Bearer {api_key}",
            "Content-Type": "application/json"
        }
        # 请求间隔，避免API限制
        self.request_interval = 1.0
        
    def translate_medical_text(self, korean_text: str, text_type: str = "question") -> str:
        """
        翻译医学文本，保持专业术语准确性
        
        Args:
            korean_text: 待翻译的韩文文本
            text_type: 文本类型 ("question" 或 "context")
        
        Returns:
            翻译后的中文文本
        """
        
        # 根据文本类型调整提示词
        if text_type == "question":
            system_prompt = """你是一位专业的医学翻译专家，精通韩文和中文医学术语。请将以下韩文医学问题翻译成中文，要求：
1. 保持医学术语的专业性和准确性
2. 使用标准的中文医学表达方式
3. 保持原文的语气和结构
4. 确保翻译自然流畅
5. 特别注意糖尿病相关的专业术语"""
        else:
            system_prompt = """你是一位专业的医学翻译专家，精通韩文和中文医学术语。请将以下韩文医学回答/解释翻译成中文，要求：
1. 保持医学术语的专业性和准确性
2. 使用标准的中文医学表达方式
3. 保持原文的结构和逻辑
4. 确保医学信息的完整性和准确性
5. 特别注意糖尿病相关的专业术语和症状描述"""
        
        payload = {
            "model": "deepseek-chat",
            "messages": [
                {
                    "role": "system",
                    "content": system_prompt
                },
                {
                    "role": "user", 
                    "content": f"请翻译以下韩文医学文本：\n{korean_text}"
                }
            ],
            "temperature": 0.1,  # 低温度确保翻译一致性
            "max_tokens": 2000,
            "stream": False
        }
        
        try:
            response = requests.post(
                self.base_url,
                headers=self.headers,
                json=payload,
                timeout=30
            )
            
            if response.status_code == 200:
                result = response.json()
                translated_text = result['choices'][0]['message']['content'].strip()
                
                # 清理可能的多余文本
                if translated_text.startswith("翻译："):
                    translated_text = translated_text[3:].strip()
                elif translated_text.startswith("中文翻译："):
                    translated_text = translated_text[5:].strip()
                
                return translated_text
            else:
                logger.error(f"API请求失败: {response.status_code} - {response.text}")
                return f"翻译失败: {korean_text}"
                
        except Exception as e:
            logger.error(f"翻译过程出错: {str(e)}")
            return f"翻译错误: {korean_text}"
    
    def batch_translate(self, texts: List[Tuple[str, str]]) -> List[str]:
        """
        批量翻译文本
        
        Args:
            texts: [(text, type), ...] 文本和类型的元组列表
            
        Returns:
            翻译结果列表
        """
        translations = []
        
        for i, (text, text_type) in enumerate(tqdm(texts, desc="翻译中")):
            if pd.isna(text) or text.strip() == "":
                translations.append("")
                continue
                
            logger.info(f"翻译第 {i+1}/{len(texts)} 条: {text_type}")
            
            translation = self.translate_medical_text(text, text_type)
            translations.append(translation)
            
            # 请求间隔
            if i < len(texts) - 1:
                time.sleep(self.request_interval)
                
        return translations

class DiabetesDataPreprocessor:
    """糖尿病数据预处理器"""
    
    def __init__(self, api_key: str):
        self.translator = DeepSeekTranslator(api_key)
        
    def load_data(self, file_path: str) -> pd.DataFrame:
        """加载parquet数据"""
        logger.info(f"加载数据文件: {file_path}")
        df = pd.read_parquet(file_path)
        logger.info(f"数据形状: {df.shape}")
        logger.info(f"列名: {df.columns.tolist()}")
        return df
    
    def preprocess_and_translate(self, df: pd.DataFrame) -> Tuple[pd.DataFrame, pd.DataFrame]:
        """
        预处理并翻译数据
        
        Returns:
            (korean_df, chinese_df): 韩文原文和中文翻译的DataFrame
        """
        logger.info("开始预处理和翻译...")
        
        # 创建韩文原文DataFrame
        korean_df = df.copy()
        
        # 准备翻译数据
        texts_to_translate = []
        for _, row in df.iterrows():
            texts_to_translate.extend([
                (str(row['question']), 'question'),
                (str(row['context']), 'context')
            ])
        
        # 批量翻译
        translations = self.translator.batch_translate(texts_to_translate)
        
        # 重新组织翻译结果
        chinese_questions = []
        chinese_contexts = []
        
        for i in range(0, len(translations), 2):
            chinese_questions.append(translations[i])
            chinese_contexts.append(translations[i + 1])
        
        # 创建中文翻译DataFrame
        chinese_df = pd.DataFrame({
            'question': chinese_questions,
            'context': chinese_contexts
        })
        
        logger.info("翻译完成!")
        
        return korean_df, chinese_df
    
    def save_to_csv(self, korean_df: pd.DataFrame, chinese_df: pd.DataFrame, 
                    korean_file: str = "diabetes_qa_korean.csv",
                    chinese_file: str = "diabetes_qa_chinese.csv"):
        """保存为CSV文件"""
        
        logger.info(f"保存韩文原文到: {korean_file}")
        korean_df.to_csv(korean_file, index=False, encoding='utf-8-sig')
        
        logger.info(f"保存中文翻译到: {chinese_file}")
        chinese_df.to_csv(chinese_file, index=False, encoding='utf-8-sig')
        
        # 保存统计信息
        stats = {
            "原始数据行数": len(korean_df),
            "翻译数据行数": len(chinese_df),
            "处理时间": time.strftime("%Y-%m-%d %H:%M:%S"),
            "韩文文件": korean_file,
            "中文文件": chinese_file
        }
        
        with open("translation_stats.json", "w", encoding='utf-8') as f:
            json.dump(stats, f, ensure_ascii=False, indent=2)
        
        logger.info("数据保存完成!")
        logger.info(f"处理统计: {stats}")

def main():
    """主函数"""
    # DeepSeek API密钥
    API_KEY = "sk-86bc0ca023294b4d94596861c70c6f45"
    
    # 输入文件路径
    INPUT_FILE = "train-00000-of-00001.parquet"
    
    try:
        # 初始化预处理器
        preprocessor = DiabetesDataPreprocessor(API_KEY)
        
        # 加载数据
        df = preprocessor.load_data(INPUT_FILE)
        
        # 显示数据样本
        logger.info("数据样本:")
        print("\n" + "="*50)
        print("前3行数据:")
        print(df.head(3).to_string())
        print("="*50 + "\n")
        
        # 询问用户是否继续
        confirm = input("是否开始翻译处理？这可能需要较长时间 (y/n): ").strip().lower()
        if confirm != 'y':
            logger.info("用户取消操作")
            return
        
        # 预处理和翻译
        korean_df, chinese_df = preprocessor.preprocess_and_translate(df)
        
        # 保存结果
        preprocessor.save_to_csv(korean_df, chinese_df)
        
        # 显示结果样本
        logger.info("翻译结果样本:")
        print("\n" + "="*50)
        print("韩文原文 (前2条):")
        print(korean_df.head(2).to_string())
        print("\n中文翻译 (前2条):")
        print(chinese_df.head(2).to_string())
        print("="*50)
        
    except Exception as e:
        logger.error(f"处理过程中发生错误: {str(e)}")
        raise

if __name__ == "__main__":
    main() 