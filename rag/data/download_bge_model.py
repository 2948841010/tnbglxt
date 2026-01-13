#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
BGE-large-zh-v1.5 模型下载脚本
使用ModelScope下载BGE中文向量模型
"""

import os
import time
from modelscope.hub.snapshot_download import snapshot_download

def download_bge_model():
    """下载BGE-large-zh-v1.5模型"""
    
    # 模型配置
    model_id = "AI-ModelScope/bge-large-zh-v1.5"
    cache_dir = "models"  # 模型保存目录
    
    print("🚀 开始下载BGE-large-zh-v1.5模型...")
    print(f"📁 模型ID: {model_id}")
    print(f"💾 保存路径: {os.path.abspath(cache_dir)}")
    print("-" * 50)
    
    try:
        start_time = time.time()
        
        # 下载模型
        model_dir = snapshot_download(
            model_id=model_id,
            cache_dir=cache_dir,
            revision="master"  # 使用最新版本
        )
        
        end_time = time.time()
        download_time = end_time - start_time
        
        print("✅ 模型下载完成！")
        print(f"📂 模型路径: {model_dir}")
        print(f"⏱️  下载用时: {download_time:.2f} 秒")
        
        # 检查下载的文件
        print("\n📋 下载的文件:")
        for root, dirs, files in os.walk(model_dir):
            for file in files:
                file_path = os.path.join(root, file)
                file_size = os.path.getsize(file_path) / (1024 * 1024)  # MB
                print(f"  - {file} ({file_size:.1f} MB)")
        
        return model_dir
        
    except Exception as e:
        print(f"❌ 下载失败: {str(e)}")
        print("💡 请检查网络连接或稍后重试")
        return None

def test_model_loading(model_dir):
    """测试模型加载"""
    if not model_dir:
        return
        
    print("\n🧪 测试模型加载...")
    try:
        from sentence_transformers import SentenceTransformer
        
        # 加载模型
        model = SentenceTransformer(model_dir)
        print("✅ 模型加载成功！")
        
        # 测试向量化
        test_text = "糖尿病的症状有哪些？"
        embedding = model.encode(test_text)
        print(f"📊 测试文本: {test_text}")
        print(f"🔢 向量维度: {embedding.shape}")
        print(f"📈 向量预览: {embedding[:5]}")
        
    except ImportError:
        print("⚠️  sentence-transformers未安装，跳过加载测试")
        print("💡 运行: pip install sentence-transformers")
    except Exception as e:
        print(f"❌ 模型加载测试失败: {str(e)}")

if __name__ == "__main__":
    print("=" * 60)
    print("🤖 BGE-large-zh-v1.5 模型下载器")
    print("=" * 60)
    
    # 创建模型目录
    os.makedirs("models", exist_ok=True)
    
    # 下载模型
    model_path = download_bge_model()
    
    # 测试模型加载
    test_model_loading(model_path)
    
    print("\n" + "=" * 60)
    print("🎉 下载流程完成！")
    print("=" * 60) 