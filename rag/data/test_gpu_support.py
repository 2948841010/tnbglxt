#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
BGE模型GPU支持测试脚本
"""

import torch
import time
import numpy as np

def check_gpu_environment():
    """检查GPU环境"""
    print("🔍 检查GPU环境...")
    print("=" * 50)
    
    # 检查CUDA是否可用
    cuda_available = torch.cuda.is_available()
    print(f"CUDA可用: {'✅ 是' if cuda_available else '❌ 否'}")
    
    if cuda_available:
        # 显示GPU信息
        gpu_count = torch.cuda.device_count()
        print(f"GPU数量: {gpu_count}")
        
        for i in range(gpu_count):
            gpu_name = torch.cuda.get_device_name(i)
            gpu_memory = torch.cuda.get_device_properties(i).total_memory / (1024**3)
            gpu_compute = torch.cuda.get_device_properties(i).major
            print(f"GPU {i}: {gpu_name}")
            print(f"       显存: {gpu_memory:.1f}GB")
            print(f"       计算能力: {gpu_compute}.x")
        
        # 检查当前GPU使用情况
        current_device = torch.cuda.current_device()
        memory_allocated = torch.cuda.memory_allocated(current_device) / (1024**2)
        memory_cached = torch.cuda.memory_reserved(current_device) / (1024**2)
        
        print(f"当前设备: GPU {current_device}")
        print(f"已分配内存: {memory_allocated:.1f}MB")
        print(f"缓存内存: {memory_cached:.1f}MB")
        
        return True, gpu_count
    else:
        print("💡 CUDA不可用，将使用CPU进行计算")
        print("📝 如需GPU支持，请安装CUDA版本的PyTorch")
        return False, 0

def test_bge_with_gpu():
    """测试BGE模型的GPU支持"""
    print("\n🧪 测试BGE模型GPU支持...")
    print("=" * 50)
    
    try:
        from sentence_transformers import SentenceTransformer
        
        model_path = "models/AI-ModelScope/bge-large-zh-v1___5"
        
        # 测试文本
        test_texts = [
            "糖尿病的主要症状有哪些？",
            "如何预防高血压？",
            "健康饮食的建议",
            "运动对身体健康的好处",
            "糖尿病视网膜病变的治疗方法",
            "血糖控制的重要性"
        ]
        
        results = {}
        
        # CPU测试
        print("\n--- CPU性能测试 ---")
        print("🔄 加载模型到CPU...")
        start_time = time.time()
        cpu_model = SentenceTransformer(model_path, device='cpu')
        cpu_load_time = time.time() - start_time
        print(f"✅ CPU模型加载完成: {cpu_load_time:.2f}秒")
        
        # CPU编码测试
        print("🧠 CPU文本编码测试...")
        start_time = time.time()
        cpu_embeddings = cpu_model.encode(test_texts)
        cpu_encode_time = time.time() - start_time
        print(f"✅ CPU编码完成: {cpu_encode_time:.3f}秒")
        print(f"📐 向量形状: {cpu_embeddings.shape}")
        
        results['CPU'] = {
            'load_time': cpu_load_time,
            'encode_time': cpu_encode_time,
            'total_time': cpu_load_time + cpu_encode_time
        }
        
        # GPU测试（如果可用）
        if torch.cuda.is_available():
            print("\n--- GPU性能测试 ---")
            print("🔄 加载模型到GPU...")
            start_time = time.time()
            gpu_model = SentenceTransformer(model_path, device='cuda')
            gpu_load_time = time.time() - start_time
            print(f"✅ GPU模型加载完成: {gpu_load_time:.2f}秒")
            
            # 显示GPU内存使用
            memory_allocated = torch.cuda.memory_allocated() / (1024**2)
            print(f"💾 GPU内存使用: {memory_allocated:.1f}MB")
            
            # GPU预热
            print("🔥 GPU预热...")
            gpu_model.encode(["预热文本"])
            torch.cuda.synchronize()
            
            # GPU编码测试
            print("🧠 GPU文本编码测试...")
            start_time = time.time()
            gpu_embeddings = gpu_model.encode(test_texts)
            torch.cuda.synchronize()  # 确保GPU操作完成
            gpu_encode_time = time.time() - start_time
            print(f"✅ GPU编码完成: {gpu_encode_time:.3f}秒")
            print(f"📐 向量形状: {gpu_embeddings.shape}")
            
            # 显示最终GPU内存使用
            memory_allocated = torch.cuda.memory_allocated() / (1024**2)
            memory_cached = torch.cuda.memory_reserved() / (1024**2)
            print(f"💾 GPU内存使用: {memory_allocated:.1f}MB (分配) + {memory_cached:.1f}MB (缓存)")
            
            results['GPU'] = {
                'load_time': gpu_load_time,
                'encode_time': gpu_encode_time,
                'total_time': gpu_load_time + gpu_encode_time
            }
            
            # 验证结果一致性
            print("\n🔍 验证CPU与GPU结果一致性...")
            max_diff = np.max(np.abs(cpu_embeddings - gpu_embeddings))
            print(f"最大差异: {max_diff:.6f}")
            
            if max_diff < 1e-4:
                print("✅ CPU与GPU结果高度一致")
            elif max_diff < 1e-2:
                print("⚠️  CPU与GPU结果有轻微差异 (正常)")
            else:
                print("❌ CPU与GPU结果差异较大")
        
        # 性能对比
        if len(results) > 1:
            print("\n📊 性能对比结果:")
            print("=" * 60)
            print(f"{'设备':>6} | {'加载时间':>8} | {'编码时间':>8} | {'总时间':>8}")
            print("-" * 60)
            
            for device, times in results.items():
                print(f"{device:>6} | {times['load_time']:>7.2f}s | {times['encode_time']:>7.3f}s | {times['total_time']:>7.2f}s")
            
            if 'CPU' in results and 'GPU' in results:
                speedup_encode = results['CPU']['encode_time'] / results['GPU']['encode_time']
                speedup_total = results['CPU']['total_time'] / results['GPU']['total_time']
                
                print(f"\n🚀 GPU加速效果:")
                print(f"   编码加速比: {speedup_encode:.2f}x")
                print(f"   总体加速比: {speedup_total:.2f}x")
                
                if speedup_encode > 2.0:
                    print("🎉 GPU显著提升编码性能!")
                elif speedup_encode > 1.3:
                    print("💡 GPU有明显性能提升")
                elif speedup_encode > 1.1:
                    print("📈 GPU有轻微性能提升")
                else:
                    print("⚠️  GPU性能提升不明显")
                    print("   可能原因: 数据量较小、GPU预热不足或驱动问题")
        
        return True
        
    except Exception as e:
        print(f"❌ 测试失败: {str(e)}")
        return False

def main():
    """主函数"""
    print("=" * 60)
    print("🚀 BGE-large-zh-v1.5 GPU支持测试")
    print("=" * 60)
    
    # GPU环境检测
    cuda_available, gpu_count = check_gpu_environment()
    
    # BGE模型GPU测试
    if test_bge_with_gpu():
        print("\n" + "=" * 60)
        print("🎉 BGE模型GPU测试完成!")
        
        if cuda_available:
            print("✅ GPU支持正常，可用于生产环境")
            print("💡 建议在向量化大量数据时使用GPU加速")
        else:
            print("💻 当前为CPU模式，性能可能较慢")
            print("📝 如需GPU加速，请安装CUDA和对应的PyTorch版本")
            
        print("=" * 60)
        return True
    else:
        print("\n❌ GPU测试失败!")
        return False

if __name__ == "__main__":
    success = main()
    exit(0 if success else 1) 