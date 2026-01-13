#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
RAG检索API服务启动脚本
"""

import os
import sys
import subprocess
import time
import requests
from pathlib import Path

def check_redis():
    """检查Redis是否运行"""
    try:
        import redis
        client = redis.Redis(host='localhost', port=6379, db=2, socket_timeout=3)
        client.ping()
        print("✅ Redis连接正常")
        return True
    except Exception as e:
        print(f"⚠️  Redis连接失败: {e}")
        print("💡 请确保Redis服务正在运行")
        return False

def check_dependencies():
    """检查依赖"""
    required_packages = [
        'fastapi', 'uvicorn', 'redis', 'chromadb', 
        'sentence-transformers', 'torch', 'pydantic'
    ]
    
    missing_packages = []
    
    for package in required_packages:
        try:
            __import__(package.replace('-', '_'))
        except ImportError:
            missing_packages.append(package)
    
    if missing_packages:
        print(f"❌ 缺少依赖包: {', '.join(missing_packages)}")
        print("请运行: pip install " + " ".join(missing_packages))
        return False
    
    print("✅ 所有依赖包已安装")
    return True

def check_model_and_data():
    """检查模型和数据"""
    model_path = Path("data/models/AI-ModelScope/bge-large-zh-v1___5")
    data_path = Path("data/processed_data/diabetes_qa_processed.json")
    chroma_path = Path("chroma_db")
    
    if not model_path.exists():
        print("❌ BGE模型不存在")
        print(f"请确保模型在: {model_path.absolute()}")
        return False
    
    if not data_path.exists():
        print("❌ 处理后的数据不存在")
        print(f"请先运行数据处理: python build_vector_database.py")
        return False
    
    if not chroma_path.exists():
        print("❌ ChromaDB数据库不存在")
        print(f"请先构建向量数据库: python build_vector_database.py")
        return False
    
    print("✅ 模型和数据检查通过")
    return True

def start_api_server():
    """启动API服务"""
    print("🚀 启动RAG检索API服务...")
    
    try:
        # 启动服务
        process = subprocess.Popen([
            sys.executable, "-m", "uvicorn", 
            "retrieval_api:app",
            "--host", "0.0.0.0",
            "--port", "8001",
            "--workers", "1",
            "--log-level", "info"
        ])
        
        # 等待服务启动
        print("⏳ 等待服务启动...")
        time.sleep(5)
        
        # 检查服务是否正常
        try:
            response = requests.get("http://localhost:8001/health", timeout=10)
            if response.status_code == 200:
                print("✅ API服务启动成功！")
                print("📍 服务地址: http://localhost:8001")
                print("📚 API文档: http://localhost:8001/docs")
                print("🏥 健康检查: http://localhost:8001/health")
                print("\n按 Ctrl+C 停止服务...")
                
                # 保持服务运行
                try:
                    process.wait()
                except KeyboardInterrupt:
                    print("\n🔄 正在停止服务...")
                    process.terminate()
                    process.wait()
                    print("✅ 服务已停止")
            else:
                print("❌ API服务启动失败")
                process.terminate()
                return False
                
        except requests.exceptions.RequestException:
            print("❌ API服务启动失败或无响应")
            process.terminate()
            return False
            
    except Exception as e:
        print(f"❌ 启动服务时出错: {e}")
        return False
    
    return True

def main():
    """主函数"""
    print("=" * 80)
    print("🚀 RAG检索API服务启动器")
    print("=" * 80)
    
    # 检查依赖
    if not check_dependencies():
        return False
    
    # 检查Redis
    if not check_redis():
        print("💡 提示: Redis是可选的，没有Redis服务也可以运行（但无缓存功能）")
    
    # 检查模型和数据
    if not check_model_and_data():
        return False
    
    # 启动API服务
    return start_api_server()

if __name__ == "__main__":
    success = main()
    sys.exit(0 if success else 1) 