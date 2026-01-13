#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
MCP RAG工具测试脚本
测试在MCP服务中集成的RAG检索功能
"""

import json
import requests
import time
from typing import Dict, Any

class MCPRAGTester:
    """MCP RAG工具测试器"""
    
    def __init__(self, mcp_base_url: str = "http://localhost:50001", rag_base_url: str = "http://localhost:8001"):
        self.mcp_base_url = mcp_base_url.rstrip("/")
        self.rag_base_url = rag_base_url.rstrip("/")
    
    def check_rag_service_direct(self) -> Dict[str, Any]:
        """直接检查RAG服务状态"""
        try:
            response = requests.get(f"{self.rag_base_url}/health", timeout=5)
            response.raise_for_status()
            return {"success": True, "data": response.json()}
        except Exception as e:
            return {"success": False, "error": str(e)}
    
    def call_mcp_tool(self, tool_name: str, params: Dict = None) -> Dict[str, Any]:
        """调用MCP工具"""
        try:
            payload = {
                "method": "tools/call",
                "params": {
                    "name": tool_name,
                    "arguments": params or {}
                }
            }
            
            response = requests.post(
                f"{self.mcp_base_url}/call",
                json=payload,
                timeout=30
            )
            response.raise_for_status()
            return {"success": True, "data": response.json()}
        except Exception as e:
            return {"success": False, "error": str(e)}
    
    def test_rag_health_check(self):
        """测试RAG健康检查工具"""
        print("\n1. 🏥 测试RAG健康检查工具")
        print("-" * 50)
        
        # 直接检查RAG服务
        direct_check = self.check_rag_service_direct()
        if direct_check["success"]:
            print("✅ RAG服务直接访问正常")
            rag_status = direct_check["data"]
            print(f"   - 服务状态: {rag_status.get('status')}")
            print(f"   - 模型已加载: {rag_status.get('model_loaded')}")
            print(f"   - 总文档数: {rag_status.get('total_documents')}")
        else:
            print(f"❌ RAG服务直接访问失败: {direct_check['error']}")
            return False
        
        # 通过MCP调用RAG健康检查
        mcp_result = self.call_mcp_tool("rag_health_check")
        if mcp_result["success"]:
            print("✅ MCP RAG健康检查工具调用成功")
            try:
                health_data = json.loads(mcp_result["data"].get("content", [{}])[0].get("text", "{}"))
                print(f"   - MCP返回状态: {health_data.get('service_status')}")
                print(f"   - 模型状态: {health_data.get('model_loaded')}")
                return True
            except Exception as e:
                print(f"⚠️  MCP返回数据解析失败: {e}")
                return False
        else:
            print(f"❌ MCP RAG健康检查工具调用失败: {mcp_result['error']}")
            return False
    
    def test_diabetes_knowledge_search(self):
        """测试糖尿病知识搜索工具"""
        print("\n2. 🔍 测试糖尿病知识搜索工具")
        print("-" * 50)
        
        test_queries = [
            {
                "query": "糖尿病的主要症状有哪些？",
                "top_k": 3,
                "description": "基础症状查询"
            },
            {
                "query": "糖尿病视网膜病变如何治疗？",
                "top_k": 2,
                "category_filter": "眼部疾病",
                "description": "分类过滤查询"
            },
            {
                "query": "血糖控制方法",
                "top_k": 3,
                "similarity_threshold": 0.2,
                "description": "阈值过滤查询"
            }
        ]
        
        success_count = 0
        
        for i, test_case in enumerate(test_queries, 1):
            print(f"\n--- 测试查询 {i}: {test_case['description']} ---")
            print(f"查询: {test_case['query']}")
            
            # 准备参数
            params = {
                "query": test_case["query"],
                "top_k": test_case.get("top_k", 5),
                "similarity_threshold": test_case.get("similarity_threshold", 0.0),
                "use_cache": True,
                "include_entities": True
            }
            
            if test_case.get("category_filter"):
                params["category_filter"] = test_case["category_filter"]
            
            # 调用MCP工具
            start_time = time.time()
            result = self.call_mcp_tool("search_diabetes_knowledge", params)
            call_time = time.time() - start_time
            
            if result["success"]:
                try:
                    # 解析返回的JSON数据
                    response_text = result["data"].get("content", [{}])[0].get("text", "{}")
                    search_data = json.loads(response_text)
                    
                    if search_data.get("success", False):
                        print(f"✅ 搜索成功 (用时: {call_time:.3f}秒)")
                        
                        summary = search_data.get("search_summary", {})
                        print(f"   - 找到结果: {summary.get('total_found', 0)} 条")
                        print(f"   - 返回结果: {summary.get('returned_count', 0)} 条")
                        print(f"   - 缓存命中: {'是' if summary.get('cache_hit') else '否'}")
                        print(f"   - 搜索用时: {summary.get('search_time_seconds', 0):.3f}秒")
                        
                        # 显示前2个结果
                        results = search_data.get("search_results", [])
                        for j, res in enumerate(results[:2], 1):
                            print(f"   结果{j}: [{res.get('category')}] {res.get('question')} (相似度: {res.get('similarity_score', 0):.4f})")
                        
                        success_count += 1
                        
                    else:
                        print(f"❌ 搜索失败: {search_data.get('error', '未知错误')}")
                        
                except Exception as e:
                    print(f"⚠️  结果解析失败: {e}")
                    print(f"原始返回: {result['data']}")
            else:
                print(f"❌ MCP工具调用失败: {result['error']}")
        
        print(f"\n搜索测试总结: {success_count}/{len(test_queries)} 成功")
        return success_count == len(test_queries)
    
    def test_get_categories(self):
        """测试获取分类工具"""
        print("\n3. 📋 测试获取知识库分类工具")
        print("-" * 50)
        
        result = self.call_mcp_tool("get_diabetes_knowledge_categories")
        
        if result["success"]:
            try:
                response_text = result["data"].get("content", [{}])[0].get("text", "{}")
                categories_data = json.loads(response_text)
                
                if categories_data.get("success", False):
                    print("✅ 获取分类信息成功")
                    
                    categories = categories_data.get("available_categories", {})
                    print(f"   - 可用分类数量: {len(categories)}")
                    
                    for category, description in categories.items():
                        print(f"   - {category}: {description}")
                    
                    usage_tips = categories_data.get("usage_tips", [])
                    if usage_tips:
                        print("   使用提示:")
                        for tip in usage_tips:
                            print(f"     • {tip}")
                    
                    return True
                else:
                    print(f"❌ 获取分类失败: {categories_data.get('error', '未知错误')}")
                    return False
                    
            except Exception as e:
                print(f"⚠️  分类数据解析失败: {e}")
                return False
        else:
            print(f"❌ MCP工具调用失败: {result['error']}")
            return False
    
    def test_cache_operations(self):
        """测试缓存操作工具"""
        print("\n4. 💾 测试缓存操作工具")
        print("-" * 50)
        
        # 测试清理缓存
        result = self.call_mcp_tool("clear_rag_cache")
        
        if result["success"]:
            try:
                response_text = result["data"].get("content", [{}])[0].get("text", "{}")
                cache_data = json.loads(response_text)
                
                if cache_data.get("success", False):
                    print("✅ 缓存清理成功")
                    print(f"   - 消息: {cache_data.get('message', '已清理')}")
                    return True
                else:
                    print(f"❌ 缓存清理失败: {cache_data.get('error', '未知错误')}")
                    return False
                    
            except Exception as e:
                print(f"⚠️  缓存操作结果解析失败: {e}")
                return False
        else:
            print(f"❌ MCP工具调用失败: {result['error']}")
            return False

def main():
    """主测试函数"""
    print("=" * 80)
    print("🧪 MCP RAG工具集成测试")
    print("=" * 80)
    
    tester = MCPRAGTester()
    
    test_results = []
    
    # 1. 健康检查测试
    test_results.append(tester.test_rag_health_check())
    
    # 2. 知识搜索测试
    test_results.append(tester.test_diabetes_knowledge_search())
    
    # 3. 分类获取测试
    test_results.append(tester.test_get_categories())
    
    # 4. 缓存操作测试
    test_results.append(tester.test_cache_operations())
    
    # 测试总结
    print("\n" + "=" * 80)
    print("📊 测试总结")
    print("=" * 80)
    
    passed_tests = sum(test_results)
    total_tests = len(test_results)
    
    test_names = [
        "RAG健康检查工具",
        "糖尿病知识搜索工具", 
        "知识库分类获取工具",
        "缓存操作工具"
    ]
    
    for i, (name, result) in enumerate(zip(test_names, test_results)):
        status = "✅ 通过" if result else "❌ 失败"
        print(f"{i+1}. {name}: {status}")
    
    print(f"\n总体结果: {passed_tests}/{total_tests} 通过")
    
    if passed_tests == total_tests:
        print("🎉 所有测试通过！MCP RAG工具集成成功！")
        return True
    else:
        print("⚠️  部分测试失败，请检查服务状态")
        return False

if __name__ == "__main__":
    success = main()
    exit(0 if success else 1) 