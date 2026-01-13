#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
糖尿病知识库向量检索测试脚本
测试ChromaDB数据库的检索功能和性能
"""

import time
import json
from typing import List, Dict, Any, Optional
import logging
from pathlib import Path

import torch
import numpy as np
from sentence_transformers import SentenceTransformer
import chromadb
from chromadb.config import Settings

# 配置日志
logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(levelname)s - %(message)s'
)
logger = logging.getLogger(__name__)

class DiabetesRAGTester:
    """糖尿病知识库RAG检索测试器"""
    
    def __init__(self, 
                 model_path: str = "data/models/AI-ModelScope/bge-large-zh-v1___5",
                 db_path: str = "chroma_db",
                 collection_name: str = "diabetes_knowledge",
                 use_gpu: bool = True):
        
        self.model_path = model_path
        self.db_path = Path(db_path)
        self.collection_name = collection_name
        self.use_gpu = use_gpu and torch.cuda.is_available()
        
        # 初始化
        self.model = None
        self.client = None
        self.collection = None
        
        logger.info(f"初始化RAG检索测试器")
        logger.info(f"GPU支持: {'✅ 启用' if self.use_gpu else '❌ 禁用'}")
    
    def load_model_and_db(self):
        """加载模型和数据库"""
        print("🔄 初始化模型和数据库...")
        
        # 加载BGE模型
        try:
            device = 'cuda' if self.use_gpu else 'cpu'
            print(f"📱 加载BGE模型到 {device}...")
            start_time = time.time()
            
            self.model = SentenceTransformer(self.model_path, device=device)
            
            load_time = time.time() - start_time
            print(f"✅ 模型加载完成! 用时: {load_time:.2f}秒")
            
            if self.use_gpu:
                memory_allocated = torch.cuda.memory_allocated() / (1024**2)
                print(f"💾 GPU内存使用: {memory_allocated:.1f}MB")
            
        except Exception as e:
            print(f"❌ 模型加载失败: {e}")
            return False
        
        # 连接ChromaDB
        try:
            print("🔗 连接ChromaDB数据库...")
            self.client = chromadb.PersistentClient(
                path=str(self.db_path),
                settings=Settings(anonymized_telemetry=False)
            )
            
            self.collection = self.client.get_collection(self.collection_name)
            
            count = self.collection.count()
            print(f"✅ 数据库连接成功! 总记录数: {count}")
            
        except Exception as e:
            print(f"❌ 数据库连接失败: {e}")
            return False
        
        return True
    
    def search_knowledge(self, query: str, top_k: int = 5, similarity_threshold: float = 0.0) -> Dict[str, Any]:
        """执行知识检索"""
        try:
            start_time = time.time()
            
            # 向量化查询
            query_start = time.time()
            query_embedding = self.model.encode([query], normalize_embeddings=True)
            query_time = time.time() - query_start
            
            # 执行检索
            search_start = time.time()
            results = self.collection.query(
                query_embeddings=query_embedding.tolist(),
                n_results=top_k,
                include=['documents', 'metadatas', 'distances']
            )
            search_time = time.time() - search_start
            
            total_time = time.time() - start_time
            
            # 处理结果
            processed_results = []
            if results['documents'] and results['documents'][0]:
                for i, (doc, metadata, distance) in enumerate(zip(
                    results['documents'][0],
                    results['metadatas'][0], 
                    results['distances'][0]
                )):
                    similarity = 1 - distance
                    if similarity >= similarity_threshold:
                        processed_results.append({
                            'rank': i + 1,
                            'question': metadata['question'],
                            'answer': doc,
                            'category': metadata['category'],
                            'similarity': similarity,
                            'distance': distance,
                            'entities': json.loads(metadata.get('entities', '[]'))
                        })
            
            return {
                'success': True,
                'query': query,
                'results': processed_results,
                'performance': {
                    'query_time': query_time,
                    'search_time': search_time,
                    'total_time': total_time,
                    'results_count': len(processed_results)
                }
            }
            
        except Exception as e:
            return {
                'success': False,
                'query': query,
                'error': str(e),
                'results': [],
                'performance': {}
            }
    
    def run_predefined_tests(self):
        """运行预定义测试用例"""
        print("\n" + "="*80)
        print("🧪 运行预定义测试用例")
        print("="*80)
        
        test_cases = [
            {
                'name': '症状查询',
                'query': '糖尿病有什么症状？',
                'expected_categories': ['基础知识', '其他']
            },
            {
                'name': '并发症查询',
                'query': '糖尿病视网膜病变怎么治疗？',
                'expected_categories': ['眼部疾病', '治疗方法']
            },
            {
                'name': '治疗方法查询',
                'query': '如何控制血糖？',
                'expected_categories': ['治疗方法', '基础知识']
            },
            {
                'name': '诊断检查查询',
                'query': '糖尿病需要做什么检查？',
                'expected_categories': ['诊断检查', '基础知识']
            },
            {
                'name': '神经并发症查询',
                'query': '糖尿病神经病变的症状',
                'expected_categories': ['神经疾病']
            },
            {
                'name': '模糊查询',
                'query': '眼睛看不清楚',
                'expected_categories': ['眼部疾病']
            }
        ]
        
        all_results = []
        total_time = 0
        
        for i, test_case in enumerate(test_cases, 1):
            print(f"\n--- 测试 {i}: {test_case['name']} ---")
            print(f"查询: {test_case['query']}")
            
            result = self.search_knowledge(test_case['query'], top_k=3)
            all_results.append(result)
            
            if result['success']:
                perf = result['performance']
                total_time += perf['total_time']
                
                print(f"⏱️  检索用时: {perf['total_time']:.3f}秒 (查询: {perf['query_time']:.3f}s, 搜索: {perf['search_time']:.3f}s)")
                print(f"📊 找到结果: {perf['results_count']} 条")
                
                if result['results']:
                    print("🎯 检索结果:")
                    for j, res in enumerate(result['results'], 1):
                        print(f"  {j}. [{res['category']}] {res['question']} (相似度: {res['similarity']:.4f})")
                        print(f"     答案: {res['answer'][:100]}...")
                    
                    # 检查分类匹配
                    found_categories = [res['category'] for res in result['results']]
                    expected = test_case['expected_categories']
                    category_match = any(cat in found_categories for cat in expected)
                    
                    if category_match:
                        print("✅ 分类匹配预期")
                    else:
                        print(f"⚠️  分类不匹配。期望: {expected}, 实际: {found_categories}")
                else:
                    print("❌ 未找到相关结果")
            else:
                print(f"❌ 检索失败: {result['error']}")
        
        # 性能统计
        print(f"\n📈 总体性能统计:")
        print(f"   总测试用例: {len(test_cases)}")
        print(f"   成功测试: {len([r for r in all_results if r['success']])}")
        print(f"   总检索时间: {total_time:.3f}秒")
        print(f"   平均检索时间: {total_time/len(test_cases):.3f}秒")
        
        return all_results
    
    def interactive_test(self):
        """交互式测试"""
        print("\n" + "="*80)
        print("🔍 交互式检索测试")
        print("="*80)
        print("输入查询问题，输入 'quit' 或 'exit' 退出")
        
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
                result = self.search_knowledge(query, top_k=5)
                
                if result['success']:
                    perf = result['performance']
                    print(f"⏱️  检索用时: {perf['total_time']:.3f}秒")
                    print(f"📊 找到结果: {perf['results_count']} 条")
                    
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
                        print("💡 尝试使用不同的关键词或更具体的问题")
                else:
                    print(f"❌ 检索失败: {result['error']}")
                    
            except KeyboardInterrupt:
                print("\n👋 测试结束！")
                break
            except Exception as e:
                print(f"❌ 测试出错: {e}")
    
    def benchmark_test(self, num_queries: int = 50):
        """性能基准测试"""
        print(f"\n" + "="*80)
        print(f"🚀 性能基准测试 (执行 {num_queries} 次查询)")
        print("="*80)
        
        # 准备测试查询
        test_queries = [
            "糖尿病症状", "血糖控制", "视网膜病变", "胰岛素治疗", "并发症",
            "神经病变", "肾病", "眼病", "饮食控制", "运动疗法",
            "血糖监测", "药物治疗", "低血糖", "高血糖", "糖尿病检查",
            "糖尿病分类", "1型糖尿病", "2型糖尿病", "妊娠糖尿病", "糖尿病诊断"
        ]
        
        # 扩展查询到指定数量
        extended_queries = (test_queries * ((num_queries // len(test_queries)) + 1))[:num_queries]
        
        print(f"📋 开始执行 {len(extended_queries)} 次检索...")
        
        times = []
        successful_queries = 0
        
        start_time = time.time()
        
        for i, query in enumerate(extended_queries, 1):
            if i % 10 == 0:
                print(f"进度: {i}/{num_queries}")
            
            result = self.search_knowledge(query, top_k=3)
            
            if result['success']:
                times.append(result['performance']['total_time'])
                successful_queries += 1
        
        total_time = time.time() - start_time
        
        if times:
            avg_time = np.mean(times)
            min_time = np.min(times)
            max_time = np.max(times)
            std_time = np.std(times)
            
            print(f"\n📊 性能基准测试结果:")
            print(f"   总查询数: {num_queries}")
            print(f"   成功查询: {successful_queries}")
            print(f"   成功率: {successful_queries/num_queries*100:.1f}%")
            print(f"   总时间: {total_time:.3f}秒")
            print(f"   平均响应时间: {avg_time:.3f}秒")
            print(f"   最快响应: {min_time:.3f}秒")
            print(f"   最慢响应: {max_time:.3f}秒")
            print(f"   响应时间标准差: {std_time:.3f}秒")
            print(f"   QPS (每秒查询数): {successful_queries/total_time:.1f}")
            
            # 响应时间分布
            fast_queries = len([t for t in times if t < 0.05])
            medium_queries = len([t for t in times if 0.05 <= t < 0.1])
            slow_queries = len([t for t in times if t >= 0.1])
            
            print(f"\n📈 响应时间分布:")
            print(f"   < 50ms: {fast_queries} 次 ({fast_queries/len(times)*100:.1f}%)")
            print(f"   50-100ms: {medium_queries} 次 ({medium_queries/len(times)*100:.1f}%)")
            print(f"   > 100ms: {slow_queries} 次 ({slow_queries/len(times)*100:.1f}%)")
        
        return {
            'total_queries': num_queries,
            'successful_queries': successful_queries,
            'success_rate': successful_queries/num_queries if num_queries > 0 else 0,
            'total_time': total_time,
            'average_time': avg_time if times else 0,
            'min_time': min_time if times else 0,
            'max_time': max_time if times else 0,
            'qps': successful_queries/total_time if total_time > 0 else 0
        }

def main():
    """主函数"""
    print("="*80)
    print("🔍 糖尿病知识库RAG检索测试")
    print("="*80)
    
    # 创建测试器
    tester = DiabetesRAGTester(
        model_path="data/models/AI-ModelScope/bge-large-zh-v1___5",
        db_path="chroma_db",
        collection_name="diabetes_knowledge",
        use_gpu=True
    )
    
    # 初始化
    if not tester.load_model_and_db():
        print("❌ 初始化失败，退出测试")
        return False
    
    try:
        while True:
            print(f"\n{'='*60}")
            print("🛠️  测试选项:")
            print("1. 运行预定义测试用例")
            print("2. 交互式检索测试")
            print("3. 性能基准测试")
            print("4. 退出")
            print("="*60)
            
            choice = input("请选择测试类型 (1-4): ").strip()
            
            if choice == '1':
                tester.run_predefined_tests()
            elif choice == '2':
                tester.interactive_test()
            elif choice == '3':
                num_queries = input("请输入测试查询数量 (默认50): ").strip()
                num_queries = int(num_queries) if num_queries.isdigit() else 50
                tester.benchmark_test(num_queries)
            elif choice == '4':
                print("👋 测试结束！")
                break
            else:
                print("❌ 无效选择，请输入 1-4")
                
    except KeyboardInterrupt:
        print("\n👋 测试结束！")
    finally:
        # 清理GPU内存
        if torch.cuda.is_available():
            torch.cuda.empty_cache()
    
    return True

if __name__ == "__main__":
    success = main()
    exit(0 if success else 1) 