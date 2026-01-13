#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
BGE-large-zh-v1.5 模型测试脚本
检查模型是否完整并测试基本功能
"""

import os
import time
import numpy as np
from pathlib import Path

def check_model_files():
    """检查模型文件完整性"""
    model_path = Path("models/AI-ModelScope/bge-large-zh-v1___5")
    
    print("🔍 检查模型文件...")
    print(f"📁 模型路径: {model_path.absolute()}")
    
    # 检查关键文件
    required_files = [
        "config.json",
        "pytorch_model.bin",
        "tokenizer.json",
        "vocab.txt",
        "config_sentence_transformers.json"
    ]
    
    missing_files = []
    existing_files = []
    
    for file in required_files:
        file_path = model_path / file
        if file_path.exists():
            size = file_path.stat().st_size / (1024 * 1024)  # MB
            existing_files.append(f"  ✅ {file} ({size:.1f} MB)")
        else:
            missing_files.append(f"  ❌ {file}")
    
    print("\n📋 文件检查结果:")
    for file in existing_files:
        print(file)
    
    if missing_files:
        print("\n⚠️  缺失文件:")
        for file in missing_files:
            print(file)
        return False
    
    print("\n✅ 所有必需文件都存在!")
    return True

def test_model_loading():
    """测试模型加载"""
    print("\n🧪 测试模型加载...")
    
    try:
        from sentence_transformers import SentenceTransformer
        
        model_path = "models/AI-ModelScope/bge-large-zh-v1___5"
        
        print(f"🔄 正在加载模型: {model_path}")
        start_time = time.time()
        
        # 加载模型
        model = SentenceTransformer(model_path)
        
        load_time = time.time() - start_time
        print(f"✅ 模型加载成功! 用时: {load_time:.2f}秒")
        
        # 获取模型信息
        print(f"📊 模型最大序列长度: {model.max_seq_length}")
        print(f"🔢 输出维度: {model.get_sentence_embedding_dimension()}")
        
        return model
        
    except Exception as e:
        print(f"❌ 模型加载失败: {str(e)}")
        return None

def test_text_embedding(model):
    """测试文本向量化"""
    if model is None:
        return False
        
    print("\n🧠 测试文本向量化...")
    
    # 测试文本
    test_texts = [
        "糖尿病的主要症状有哪些？",
        "如何预防高血压？",
        "健康饮食的建议",
        "运动对身体健康的好处"
    ]
    
    try:
        start_time = time.time()
        
        # 编码文本
        embeddings = model.encode(test_texts)
        
        encode_time = time.time() - start_time
        
        print(f"✅ 文本编码成功!")
        print(f"⏱️  编码用时: {encode_time:.3f}秒")
        print(f"📐 向量形状: {embeddings.shape}")
        print(f"🔢 向量维度: {embeddings.shape[1]}")
        print(f"📊 向量类型: {type(embeddings)}")
        
        # 显示第一个文本的向量预览
        print(f"\n📝 测试文本: '{test_texts[0]}'")
        print(f"🔢 向量预览: {embeddings[0][:10]}...")
        print(f"📈 向量范围: [{embeddings[0].min():.3f}, {embeddings[0].max():.3f}]")
        
        return True
        
    except Exception as e:
        print(f"❌ 文本编码失败: {str(e)}")
        return False

def test_similarity_calculation(model):
    """测试相似度计算"""
    if model is None:
        return False
        
    print("\n🔗 测试相似度计算...")
    
    # 测试文本对
    text_pairs = [
        ("糖尿病的症状", "糖尿病有什么表现"),
        ("高血压治疗", "如何降低血压"),
        ("健康饮食", "运动锻炼")
    ]
    
    try:
        for text1, text2 in text_pairs:
            # 计算向量
            embedding1 = model.encode([text1])
            embedding2 = model.encode([text2])
            
            # 计算余弦相似度
            similarity = np.dot(embedding1[0], embedding2[0]) / (
                np.linalg.norm(embedding1[0]) * np.linalg.norm(embedding2[0])
            )
            
            print(f"📝 '{text1}' vs '{text2}'")
            print(f"   相似度: {similarity:.4f}")
        
        print("✅ 相似度计算测试通过!")
        return True
        
    except Exception as e:
        print(f"❌ 相似度计算失败: {str(e)}")
        return False

def main():
    """主函数"""
    print("=" * 60)
    print("🤖 BGE-large-zh-v1.5 模型测试")
    print("=" * 60)
    
    # 1. 检查文件完整性
    if not check_model_files():
        print("\n❌ 模型文件不完整，请重新下载!")
        return False
    
    # 2. 测试模型加载
    model = test_model_loading()
    if model is None:
        print("\n❌ 模型加载失败!")
        return False
    
    # 3. 测试文本向量化
    if not test_text_embedding(model):
        print("\n❌ 文本向量化测试失败!")
        return False
    
    # 4. 测试相似度计算
    if not test_similarity_calculation(model):
        print("\n❌ 相似度计算测试失败!")
        return False
    
    print("\n" + "=" * 60)
    print("🎉 所有测试通过! BGE模型工作正常!")
    print("=" * 60)
    
    return True

if __name__ == "__main__":
    success = main()
    exit(0 if success else 1) 