#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
RAG检索API客户端测试脚本
测试API服务的各种功能
"""

import requests
import json
import time
from typing import Dict, Any, List

class RAGAPIClient:
    """RAG API客户端"""
    
    def __init__(self, base_url: str = "http://localhost:8001"):
        self.base_url = base_url.rstrip("/")
        self.session = requests.Session()
    
    def health_check(self) -> Dict[str, Any]:
        """健康检查"""
        try:
            response = self.session.get(f"{self.base_url}/health")
            response.raise_for_status()
            return response.json()
        except Exception as e:
            return {"error": str(e)}
    
    def search(self, 
               query: str,
               top_k: int = 5,
               similarity_threshold: float = 0.0,
               use_cache: bool = True,
               include_entities: bool = True,
               category_filter: List[str] = None) -> Dict[str, Any]:
        """执行检索"""
        try:
            payload = {
                "query": query,
                "top_k": top_k,
                "similarity_threshold": similarity_threshold,
                "use_cache": use_cache,
                "include_entities": include_entities,
                "category_filter": category_filter
            }
            
            response = self.session.post(f"{self.base_url}/search", json=payload)
            response.raise_for_status()
            return response.json()
        except Exception as e:
            return {"error": str(e)}
    
    def clear_cache(self) -> Dict[str, Any]:
        """清理缓存"""
        try:
            response = self.session.post(f"{self.base_url}/cache/clear")
            response.raise_for_status()
            return response.json()
        except Exception as e:
            return {"error": str(e)}
    
    def get_stats(self) -> Dict[str, Any]:
        """获取统计信息"""
        try:
            response = self.session.get(f"{self.base_url}/stats")
            response.raise_for_status()
            return response.json()
        except Exception as e:
            return {"error": str(e)}

def test_api_functionality():
    """测试API功能"""
    print("=" * 80)
    print("🧪 RAG检索API功能测试")
    print("=" * 80)
    
    client = RAGAPIClient()
    
    # 1. 健康检查
    print("\n1. 🏥 健康检查测试")
    health = client.health_check()
    if "error" in health:
        print(f"❌ 健康检查失败: {health['error']}")
        return False
    
    print(f"✅ 服务状态: {health['status']}")
    print(f"📊 模型已加载: {health['model_loaded']}")
    print(f"🗄️  数据库已连接: {health['database_connected']}")
    print(f"💾 缓存已连接: {health['cache_connected']}")
    print(f"📈 总文档数: {health['total_documents']}")
    print(f"🚀 运行时间: {health['uptime']:.2f}秒")
    
    # 2. 基础检索测试
    print("\n2. 🔍 基础检索测试")
    test_queries = [
        "糖尿病的主要症状有哪些？",
        "糖尿病视网膜病变如何治疗？",
        "血糖控制的方法",
        "糖尿病并发症"
    ]
    
    for i, query in enumerate(test_queries, 1):
        print(f"\n--- 测试查询 {i}: {query} ---")
        
        start_time = time.time()
        result = client.search(query, top_k=3)
        search_time = time.time() - start_time
        
        if "error" in result:
            print(f"❌ 检索失败: {result['error']}")
            continue
        
        print(f"⏱️  API响应时间: {search_time:.3f}秒")
        print(f"🎯 服务端检索时间: {result['search_time']:.3f}秒")
        print(f"💾 缓存命中: {'是' if result['cache_hit'] else '否'}")
        print(f"📊 找到结果: {result['total_found']} 条")
        
        for j, res in enumerate(result['results'], 1):
            print(f"  {j}. [{res['category']}] {res['question']} (相似度: {res['similarity']:.4f})")
    
    # 3. 缓存测试
    print("\n3. 💾 缓存功能测试")
    test_query = "糖尿病症状"
    
    # 第一次查询（无缓存）
    print("第一次查询（无缓存）...")
    result1 = client.search(test_query, top_k=3, use_cache=True)
    if "error" not in result1:
        print(f"⏱️  检索时间: {result1['search_time']:.3f}秒")
        print(f"💾 缓存命中: {result1['cache_hit']}")
    
    # 第二次查询（应该命中缓存）
    print("第二次查询（应该命中缓存）...")
    result2 = client.search(test_query, top_k=3, use_cache=True)
    if "error" not in result2:
        print(f"⏱️  检索时间: {result2['search_time']:.3f}秒")
        print(f"💾 缓存命中: {result2['cache_hit']}")
        
        if result2['cache_hit']:
            print("✅ 缓存功能正常")
        else:
            print("⚠️  缓存可能未生效")
    
    # 4. 分类过滤测试
    print("\n4. 🏷️  分类过滤测试")
    filter_result = client.search(
        "糖尿病视网膜病变", 
        top_k=5, 
        category_filter=["眼部疾病"]
    )
    
    if "error" not in filter_result:
        print(f"📊 过滤后结果数: {filter_result['total_found']}")
        categories = [res['category'] for res in filter_result['results']]
        print(f"🏷️  结果分类: {set(categories)}")
        
        if all(cat == "眼部疾病" for cat in categories):
            print("✅ 分类过滤功能正常")
        else:
            print("⚠️  分类过滤可能未完全生效")
    
    # 5. 相似度阈值测试
    print("\n5. 📏 相似度阈值测试")
    threshold_result = client.search(
        "糖尿病", 
        top_k=10, 
        similarity_threshold=0.3
    )
    
    if "error" not in threshold_result:
        similarities = [res['similarity'] for res in threshold_result['results']]
        print(f"📊 结果数: {len(similarities)}")
        
        if similarities:
            print(f"📈 相似度范围: {min(similarities):.4f} - {max(similarities):.4f}")
            
            if all(sim >= 0.3 for sim in similarities):
                print("✅ 相似度阈值功能正常")
            else:
                print("⚠️  相似度阈值可能未生效")
        else:
            print("ℹ️  没有满足阈值条件的结果（正常情况）")
    
    # 6. 性能测试
    print("\n6. 🚀 性能基准测试")
    performance_queries = ["糖尿病症状", "血糖控制", "视网膜病变"] * 10
    
    print(f"执行 {len(performance_queries)} 次查询...")
    start_time = time.time()
    successful_queries = 0
    response_times = []
    
    for query in performance_queries:
        result = client.search(query, top_k=3)
        if "error" not in result:
            successful_queries += 1
            response_times.append(result['search_time'])
    
    total_time = time.time() - start_time
    
    if response_times:
        avg_time = sum(response_times) / len(response_times)
        print(f"📊 成功查询: {successful_queries}/{len(performance_queries)}")
        print(f"⏱️  总时间: {total_time:.3f}秒")
        print(f"📈 平均响应时间: {avg_time:.3f}秒")
        print(f"🔥 QPS: {successful_queries/total_time:.1f}")
    
    # 7. 获取服务统计
    print("\n7. 📊 服务统计信息")
    stats = client.get_stats()
    if "error" not in stats:
        print(f"🚀 服务状态: {stats['service_status']}")
        print(f"⏱️  运行时间: {stats['uptime']}")
        print(f"📚 总文档数: {stats['total_documents']}")
        print(f"🖥️  GPU可用: {stats['gpu_available']}")
        print(f"💾 缓存连接: {stats['cache_connected']}")
    
    print("\n" + "=" * 80)
    print("✅ API功能测试完成！")
    print("=" * 80)
    
    return True

def interactive_test():
    """交互式测试"""
    print("\n" + "=" * 80)
    print("🔍 交互式API测试")
    print("=" * 80)
    print("输入查询问题，输入 'quit' 或 'exit' 退出")
    
    client = RAGAPIClient()
    
    while True:
        try:
            query = input("\n请输入查询: ").strip()
            
            if query.lower() in ['quit', 'exit', '退出', 'q']:
                print("👋 测试结束！")
                break
            
            if not query:
                print("请输入有效查询")
                continue
            
            print(f"\n🔍 搜索: {query}")
            result = client.search(query, top_k=5)
            
            if "error" in result:
                print(f"❌ 检索失败: {result['error']}")
                continue
            
            print(f"⏱️  检索用时: {result['search_time']:.3f}秒")
            print(f"💾 缓存命中: {'是' if result['cache_hit'] else '否'}")
            print(f"📊 找到结果: {result['total_found']} 条")
            
            if result['results']:
                print("\n🎯 检索结果:")
                for i, res in enumerate(result['results'], 1):
                    print(f"\n{i}. 相似度: {res['similarity']:.4f}")
                    print(f"   分类: {res['category']}")
                    print(f"   问题: {res['question']}")
                    print(f"   答案: {res['answer']}")
                    if res['entities']:
                        print(f"   实体: {', '.join(res['entities'])}")
            else:
                print("❌ 未找到相关结果")
                
        except KeyboardInterrupt:
            print("\n👋 测试结束！")
            break
        except Exception as e:
            print(f"❌ 测试出错: {e}")

def main():
    """主函数"""
    print("🔧 RAG检索API客户端测试工具")
    
    while True:
        print(f"\n{'='*60}")
        print("🛠️  测试选项:")
        print("1. 完整功能测试")
        print("2. 交互式测试")
        print("3. 健康检查")
        print("4. 清理缓存")
        print("5. 退出")
        print("="*60)
        
        choice = input("请选择测试类型 (1-5): ").strip()
        
        if choice == '1':
            test_api_functionality()
        elif choice == '2':
            interactive_test()
        elif choice == '3':
            client = RAGAPIClient()
            health = client.health_check()
            print(json.dumps(health, indent=2, ensure_ascii=False))
        elif choice == '4':
            client = RAGAPIClient()
            result = client.clear_cache()
            print(json.dumps(result, indent=2, ensure_ascii=False))
        elif choice == '5':
            print("👋 测试结束！")
            break
        else:
            print("❌ 无效选择，请输入 1-5")

if __name__ == "__main__":
    main() 