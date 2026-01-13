#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
简单的翻译测试脚本 - 用于验证几个样本的翻译质量
"""

import pandas as pd
import requests
import json
import time

def test_translation_sample():
    """测试几个样本的翻译质量"""
    
    # DeepSeek API配置
    API_KEY = "sk-86bc0ca023294b4d94596861c70c6f45"
    BASE_URL = "https://api.deepseek.com/v1/chat/completions"
    
    headers = {
        "Authorization": f"Bearer {API_KEY}",
        "Content-Type": "application/json"
    }
    
    # 加载数据
    df = pd.read_parquet("train-00000-of-00001.parquet")
    print(f"数据形状: {df.shape}")
    print("="*50)
    
    # 测试前3个样本
    test_samples = [
        ("당뇨망막병증이란 무엇인가요?", "question"),
        ("당뇨망막병증의 카테고리: 눈질환", "context"),
        ("당뇨망막병증은 어떤 증상을 보이나요?", "question")
    ]
    
    system_prompt = """你是一位专业的医学翻译专家，精通韩文和中文医学术语。请将以下韩文医学文本翻译成中文，要求：
1. 保持医学术语的专业性和准确性
2. 使用标准的中文医学表达方式
3. 保持原文的语气和结构
4. 确保翻译自然流畅
5. 特别注意糖尿病相关的专业术语"""
    
    for i, (text, text_type) in enumerate(test_samples, 1):
        print(f"样本 {i} ({text_type}):")
        print(f"韩文原文: {text}")
        
        payload = {
            "model": "deepseek-chat",
            "messages": [
                {"role": "system", "content": system_prompt},
                {"role": "user", "content": f"请翻译以下韩文医学文本：\n{text}"}
            ],
            "temperature": 0.1,
            "max_tokens": 500,
            "stream": False
        }
        
        try:
            response = requests.post(BASE_URL, headers=headers, json=payload, timeout=30)
            
            if response.status_code == 200:
                result = response.json()
                translation = result['choices'][0]['message']['content'].strip()
                
                # 清理可能的多余文本
                if translation.startswith("翻译："):
                    translation = translation[3:].strip()
                elif translation.startswith("中文翻译："):
                    translation = translation[5:].strip()
                
                print(f"中文翻译: {translation}")
            else:
                print(f"翻译失败: {response.status_code}")
                
        except Exception as e:
            print(f"翻译出错: {str(e)}")
            
        print("-" * 50)
        time.sleep(1)  # 避免请求过快

if __name__ == "__main__":
    test_translation_sample() 