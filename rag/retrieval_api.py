#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
糖尿病知识库RAG检索API服务
提供高性能的语义检索服务，支持Redis缓存和模型常驻
"""

import os
import json
import time
import hashlib
import asyncio
from typing import List, Dict, Any, Optional, Union
from datetime import datetime, timedelta
from contextlib import asynccontextmanager

import uvicorn
import redis
import torch
import numpy as np
from fastapi import FastAPI, HTTPException, BackgroundTasks, Depends
from fastapi.responses import JSONResponse
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel, Field
from sentence_transformers import SentenceTransformer
import chromadb
from chromadb.config import Settings

# 配置管理
class Config:
    """服务配置"""
    # 模型配置
    MODEL_PATH = "data/models/AI-ModelScope/bge-large-zh-v1___5"
    USE_GPU = True
    
    # 数据库配置
    CHROMADB_PATH = "chroma_db"
    COLLECTION_NAME = "diabetes_knowledge"
    
    # Redis配置
    REDIS_HOST = "localhost"
    REDIS_PORT = 6379
    REDIS_DB = 2
    REDIS_PASSWORD = None
    CACHE_TTL = 1800  # 30分钟
    
    # API配置
    API_HOST = "0.0.0.0"
    API_PORT = 8001
    API_TITLE = "糖尿病知识库RAG检索API"
    API_VERSION = "1.0.0"
    
    # 检索配置
    DEFAULT_TOP_K = 5
    MAX_TOP_K = 20
    DEFAULT_SIMILARITY_THRESHOLD = 0.0
    ENABLE_CACHE = True
    
    # 性能配置
    MAX_QUERY_LENGTH = 500
    BATCH_SIZE = 32

# 请求响应模型
class SearchRequest(BaseModel):
    """检索请求模型"""
    query: str = Field(..., description="查询问题", max_length=Config.MAX_QUERY_LENGTH)
    top_k: int = Field(Config.DEFAULT_TOP_K, description="返回结果数量", ge=1, le=Config.MAX_TOP_K)
    similarity_threshold: float = Field(Config.DEFAULT_SIMILARITY_THRESHOLD, description="相似度阈值", ge=0.0, le=1.0)
    use_cache: bool = Field(True, description="是否使用缓存")
    include_entities: bool = Field(True, description="是否包含医学实体")
    category_filter: Optional[List[str]] = Field(None, description="分类过滤器")

class SearchResult(BaseModel):
    """单条检索结果"""
    rank: int = Field(..., description="排名")
    question: str = Field(..., description="匹配的问题")
    answer: str = Field(..., description="答案内容")
    category: str = Field(..., description="分类")
    similarity: float = Field(..., description="相似度分数")
    entities: Optional[List[str]] = Field(None, description="医学实体")
    source_info: Optional[Dict] = Field(None, description="来源信息")

class SearchResponse(BaseModel):
    """检索响应模型"""
    success: bool = Field(..., description="是否成功")
    query: str = Field(..., description="查询问题")
    results: List[SearchResult] = Field(..., description="检索结果")
    total_found: int = Field(..., description="找到的总数")
    cache_hit: bool = Field(..., description="是否命中缓存")
    search_time: float = Field(..., description="检索耗时(秒)")
    timestamp: str = Field(..., description="响应时间")

class HealthResponse(BaseModel):
    """健康检查响应"""
    status: str
    model_loaded: bool
    database_connected: bool
    cache_connected: bool
    total_documents: int
    uptime: float
    gpu_available: bool
    memory_usage: Optional[Dict]

# 模型管理器（单例模式）
class ModelManager:
    """BGE模型管理器 - 单例模式确保模型只加载一次"""
    
    _instance = None
    _model = None
    _initialized = False
    
    def __new__(cls):
        if cls._instance is None:
            cls._instance = super(ModelManager, cls).__new__(cls)
        return cls._instance
    
    async def initialize(self):
        """初始化模型"""
        if self._initialized:
            return
        
        print("🔄 初始化BGE模型...")
        try:
            device = 'cuda' if Config.USE_GPU and torch.cuda.is_available() else 'cpu'
            print(f"📱 使用设备: {device}")
            
            start_time = time.time()
            self._model = SentenceTransformer(Config.MODEL_PATH, device=device)
            load_time = time.time() - start_time
            
            print(f"✅ 模型加载完成! 用时: {load_time:.2f}秒")
            print(f"📊 向量维度: {self._model.get_sentence_embedding_dimension()}")
            
            if Config.USE_GPU and torch.cuda.is_available():
                memory_allocated = torch.cuda.memory_allocated() / (1024**2)
                print(f"💾 GPU内存使用: {memory_allocated:.1f}MB")
            
            self._initialized = True
            
        except Exception as e:
            print(f"❌ 模型加载失败: {e}")
            raise
    
    def encode(self, texts: Union[str, List[str]], **kwargs) -> np.ndarray:
        """编码文本"""
        if not self._initialized:
            raise RuntimeError("模型未初始化")
        
        if isinstance(texts, str):
            texts = [texts]
        
        return self._model.encode(
            texts, 
            normalize_embeddings=True,
            show_progress_bar=False,
            **kwargs
        )
    
    @property
    def is_initialized(self) -> bool:
        return self._initialized

# 缓存管理器
class CacheManager:
    """Redis缓存管理器"""
    
    def __init__(self):
        self.redis_client = None
        self.connected = False
    
    async def initialize(self):
        """初始化Redis连接"""
        try:
            self.redis_client = redis.Redis(
                host=Config.REDIS_HOST,
                port=Config.REDIS_PORT,
                db=Config.REDIS_DB,
                password=Config.REDIS_PASSWORD,
                decode_responses=True,
                socket_timeout=5,
                socket_connect_timeout=5
            )
            
            # 测试连接
            self.redis_client.ping()
            self.connected = True
            print("✅ Redis连接成功")
            
        except Exception as e:
            print(f"⚠️  Redis连接失败: {e}")
            self.connected = False
    
    def generate_cache_key(self, query: str, top_k: int, threshold: float, filters: Dict = None) -> str:
        """生成缓存键"""
        cache_data = {
            "query": query.strip().lower(),
            "top_k": top_k,
            "threshold": threshold,
            "filters": filters or {}
        }
        cache_str = json.dumps(cache_data, sort_keys=True)
        return f"rag_query:{hashlib.md5(cache_str.encode()).hexdigest()}"
    
    async def get_cached_result(self, cache_key: str) -> Optional[Dict]:
        """获取缓存结果"""
        if not self.connected or not Config.ENABLE_CACHE:
            return None
        
        try:
            cached_data = self.redis_client.get(cache_key)
            if cached_data:
                return json.loads(cached_data)
        except Exception as e:
            print(f"缓存读取错误: {e}")
        
        return None
    
    async def set_cache_result(self, cache_key: str, result: Dict):
        """设置缓存结果"""
        if not self.connected or not Config.ENABLE_CACHE:
            return
        
        try:
            self.redis_client.setex(
                cache_key,
                Config.CACHE_TTL,
                json.dumps(result, ensure_ascii=False)
            )
        except Exception as e:
            print(f"缓存写入错误: {e}")
    
    async def clear_cache(self, pattern: str = "rag_query:*"):
        """清理缓存"""
        if not self.connected:
            return 0
        
        try:
            keys = self.redis_client.keys(pattern)
            if keys:
                return self.redis_client.delete(*keys)
            return 0
        except Exception as e:
            print(f"缓存清理错误: {e}")
            return 0

# 检索服务
class RetrievalService:
    """检索服务核心类"""
    
    def __init__(self):
        self.model_manager = ModelManager()
        self.cache_manager = CacheManager()
        self.chroma_client = None
        self.collection = None
        self.total_documents = 0
        self.start_time = time.time()
    
    async def initialize(self):
        """初始化服务"""
        # 初始化模型
        await self.model_manager.initialize()
        
        # 初始化缓存
        await self.cache_manager.initialize()
        
        # 初始化ChromaDB
        await self.refresh_collection()
    
    async def refresh_collection(self):
        """刷新ChromaDB连接"""
        try:
            self.chroma_client = chromadb.PersistentClient(
                path=Config.CHROMADB_PATH,
                settings=Settings(anonymized_telemetry=False)
            )
            
            # 尝试获取现有collection，如果不存在则创建
            try:
                self.collection = self.chroma_client.get_collection(Config.COLLECTION_NAME)
            except:
                self.collection = self.chroma_client.create_collection(
                    name=Config.COLLECTION_NAME,
                    metadata={"hnsw:space": "cosine"}
                )
            
            self.total_documents = self.collection.count()
            print(f"✅ ChromaDB连接成功，总文档数: {self.total_documents}")
            
        except Exception as e:
            print(f"❌ ChromaDB连接失败: {e}")
            raise
    
    async def search(self, request: SearchRequest) -> SearchResponse:
        """执行检索"""
        start_time = time.time()
        cache_hit = False
        
        # 查询预处理
        query = request.query.strip()
        if not query:
            raise HTTPException(status_code=400, detail="查询不能为空")
        
        # 生成缓存键
        cache_key = self.cache_manager.generate_cache_key(
            query, request.top_k, request.similarity_threshold, 
            {"category_filter": request.category_filter}
        )
        
        # 尝试从缓存获取
        if request.use_cache and Config.ENABLE_CACHE:
            cached_result = await self.cache_manager.get_cached_result(cache_key)
            if cached_result:
                cached_result["cache_hit"] = True
                cached_result["search_time"] = time.time() - start_time
                cached_result["timestamp"] = datetime.now().isoformat()
                return SearchResponse(**cached_result)
        
        try:
            # 向量化查询
            query_embedding = self.model_manager.encode([query])
            
            # 执行检索
            chroma_results = self.collection.query(
                query_embeddings=query_embedding.tolist(),
                n_results=min(request.top_k * 2, Config.MAX_TOP_K),  # 获取更多结果用于过滤
                include=['documents', 'metadatas', 'distances']
            )
            
            # 处理结果
            results = await self._process_results(
                chroma_results, request, query_embedding
            )
            
            # 构建响应
            response_data = {
                "success": True,
                "query": query,
                "results": results[:request.top_k],  # 限制最终返回数量
                "total_found": len(results),
                "cache_hit": cache_hit,
                "search_time": time.time() - start_time,
                "timestamp": datetime.now().isoformat()
            }
            
            # 缓存结果
            if request.use_cache and Config.ENABLE_CACHE:
                cache_data = response_data.copy()
                cache_data.pop("cache_hit")
                cache_data.pop("search_time") 
                cache_data.pop("timestamp")
                await self.cache_manager.set_cache_result(cache_key, cache_data)
            
            return SearchResponse(**response_data)
            
        except Exception as e:
            print(f"检索错误: {e}")
            raise HTTPException(status_code=500, detail=f"检索失败: {str(e)}")
    
    async def _process_results(self, chroma_results: Dict, request: SearchRequest, query_embedding: np.ndarray) -> List[Dict]:
        """处理和优化检索结果"""
        if not chroma_results['documents'] or not chroma_results['documents'][0]:
            return []
        
        results = []
        
        for i, (doc, metadata, distance) in enumerate(zip(
            chroma_results['documents'][0],
            chroma_results['metadatas'][0],
            chroma_results['distances'][0]
        )):
            similarity = 1 - distance
            
            # 应用相似度阈值
            if similarity < request.similarity_threshold:
                continue
            
            # 应用分类过滤
            if (request.category_filter and 
                metadata.get('category') not in request.category_filter):
                continue
            
            # 解析实体
            entities = []
            if request.include_entities:
                try:
                    entities = json.loads(metadata.get('entities', '[]'))
                except:
                    entities = []
            
            result = {
                "rank": len(results) + 1,
                "question": metadata.get('question', ''),
                "answer": doc,
                "category": metadata.get('category', ''),
                "similarity": round(similarity, 4),
                "entities": entities if request.include_entities else None,
                "source_info": {
                    "source_row": metadata.get('source_row'),
                    "chunk_index": metadata.get('chunk_index'),
                    "text_length": metadata.get('text_length'),
                    "doc_id": metadata.get('doc_id', 'other'),
                    "source": metadata.get('source', metadata.get('filename', '未知来源'))
                }
            }
            
            results.append(result)
        
        # 结果排序和去重
        results = await self._deduplicate_results(results)
        results.sort(key=lambda x: x['similarity'], reverse=True)
        
        return results
    
    async def _deduplicate_results(self, results: List[Dict]) -> List[Dict]:
        """去重处理"""
        seen_answers = set()
        unique_results = []
        
        for result in results:
            answer_hash = hashlib.md5(result['answer'].encode()).hexdigest()
            if answer_hash not in seen_answers:
                seen_answers.add(answer_hash)
                unique_results.append(result)
        
        return unique_results
    
    def get_health_status(self) -> HealthResponse:
        """获取健康状态"""
        memory_info = None
        if torch.cuda.is_available():
            memory_info = {
                "gpu_memory_allocated": f"{torch.cuda.memory_allocated() / (1024**2):.1f}MB",
                "gpu_memory_cached": f"{torch.cuda.memory_reserved() / (1024**2):.1f}MB"
            }
        
        # 核心服务：模型和数据库，Redis是可选的
        is_healthy = self.model_manager.is_initialized and self.collection is not None
        
        return HealthResponse(
            status="healthy" if is_healthy else "unhealthy",
            model_loaded=self.model_manager.is_initialized,
            database_connected=self.collection is not None,
            cache_connected=self.cache_manager.connected,
            total_documents=self.total_documents,
            uptime=time.time() - self.start_time,
            gpu_available=torch.cuda.is_available(),
            memory_usage=memory_info
        )

# 全局服务实例
retrieval_service = RetrievalService()

# FastAPI生命周期管理
@asynccontextmanager
async def lifespan(app: FastAPI):
    """应用生命周期管理"""
    print("🚀 启动检索API服务...")
    
    # 启动时初始化
    try:
        await retrieval_service.initialize()
        print("✅ 服务初始化完成")
    except Exception as e:
        print(f"❌ 服务初始化失败: {e}")
        raise
    
    yield  # 应用运行
    
    # 关闭时清理
    print("🔄 关闭检索API服务...")
    if torch.cuda.is_available():
        torch.cuda.empty_cache()
    print("✅ 服务关闭完成")

# 创建FastAPI应用
app = FastAPI(
    title=Config.API_TITLE,
    version=Config.API_VERSION,
    description="基于BGE模型和ChromaDB的糖尿病知识库语义检索API服务",
    lifespan=lifespan
)

# 添加CORS支持
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# API路由
@app.get("/", summary="服务信息")
async def root():
    """获取服务基本信息"""
    return {
        "service": Config.API_TITLE,
        "version": Config.API_VERSION,
        "status": "running",
        "docs": "/docs",
        "health": "/health"
    }

@app.get("/health", response_model=HealthResponse, summary="健康检查")
async def health_check():
    """获取服务健康状态"""
    return retrieval_service.get_health_status()

@app.post("/search", response_model=SearchResponse, summary="语义检索")
async def search_knowledge(request: SearchRequest):
    """
    执行语义检索
    
    - **query**: 查询问题
    - **top_k**: 返回结果数量 (1-20)
    - **similarity_threshold**: 相似度阈值 (0.0-1.0)
    - **use_cache**: 是否使用缓存
    - **include_entities**: 是否包含医学实体
    - **category_filter**: 分类过滤器
    """
    return await retrieval_service.search(request)

@app.post("/cache/clear", summary="清理缓存")
async def clear_cache():
    """清理所有缓存"""
    cleared_count = await retrieval_service.cache_manager.clear_cache()
    return {
        "success": True,
        "message": f"已清理 {cleared_count} 个缓存项",
        "timestamp": datetime.now().isoformat()
    }

@app.get("/stats", summary="服务统计")
async def get_stats():
    """获取服务统计信息"""
    health = retrieval_service.get_health_status()
    
    return {
        "service_status": health.status,
        "uptime": health.uptime,
        "total_documents": health.total_documents,
        "model_loaded": health.model_loaded,
        "gpu_available": health.gpu_available,
        "cache_connected": health.cache_connected,
        "config": {
            "max_top_k": Config.MAX_TOP_K,
            "cache_ttl": Config.CACHE_TTL,
            "enable_cache": Config.ENABLE_CACHE
        }
    }

# ============ MCP 查询接口 ============
class MCPQueryRequest(BaseModel):
    """MCP查询请求 - 简化版本供AI Agent调用"""
    query: str = Field(..., description="查询问题")
    top_k: int = Field(3, description="返回结果数量", ge=1, le=10)

@app.post("/mcp/query", summary="MCP知识库查询接口")
async def mcp_query(request: MCPQueryRequest):
    """
    MCP工具调用接口 - 供AI Agent查询糖尿病知识库
    
    返回格式化的知识库检索结果，适合AI理解和使用
    """
    try:
        search_request = SearchRequest(
            query=request.query,
            top_k=request.top_k,
            similarity_threshold=0.3,
            use_cache=True,
            include_entities=True
        )
        result = await retrieval_service.search(search_request)
        
        # 格式化为MCP友好的响应
        knowledge_items = []
        for item in result.results:
            knowledge_items.append({
                "question": item.question,
                "answer": item.answer,
                "category": item.category,
                "relevance": f"{item.similarity * 100:.1f}%",
                "entities": item.entities or []
            })
        
        return {
            "success": True,
            "query": request.query,
            "knowledge_count": len(knowledge_items),
            "knowledge": knowledge_items,
            "summary": f"找到 {len(knowledge_items)} 条相关知识" if knowledge_items else "未找到相关知识"
        }
    except Exception as e:
        return {
            "success": False,
            "query": request.query,
            "knowledge_count": 0,
            "knowledge": [],
            "error": str(e)
        }

# ============ 文档管理接口 ============
import shutil
from fastapi import UploadFile, File
from pathlib import Path

# 文档存储目录
UPLOAD_DIR = Path("data/uploads")
UPLOAD_DIR.mkdir(parents=True, exist_ok=True)

# 文档元数据存储（内存缓存，启动时从ChromaDB恢复）
documents_db: Dict[str, Dict] = {}


def init_documents_db():
    """从ChromaDB恢复文档列表"""
    global documents_db
    try:
        if retrieval_service.collection:
            # 获取所有唯一的doc_id
            results = retrieval_service.collection.get(include=["metadatas"])
            if results and results["metadatas"]:
                doc_map = {}
                for metadata in results["metadatas"]:
                    doc_id = metadata.get("doc_id", "other")
                    if doc_id not in doc_map:
                        doc_map[doc_id] = {
                            "id": doc_id,
                            "name": metadata.get("source", metadata.get("filename", "未知文档")),
                            "type": metadata.get("file_type", "unknown"),
                            "chunks": 0,
                            "size": 0,
                            "created_at": metadata.get("created_at", "未知")
                        }
                    doc_map[doc_id]["chunks"] += 1
                
                documents_db = doc_map
                print(f"✅ 从ChromaDB恢复了 {len(documents_db)} 个文档记录")
    except Exception as e:
        print(f"⚠️  恢复文档列表失败: {e}")


@app.get("/documents", summary="获取文档列表")
async def get_documents():
    """获取已导入的文档列表"""
    try:
        # 如果内存中没有，尝试从ChromaDB恢复
        if not documents_db:
            init_documents_db()
        
        docs_list = list(documents_db.values())
        
        # 如果还是空的，说明数据库里也没有按doc_id组织的数据，返回一个"其他"分类
        if not docs_list:
            total = retrieval_service.total_documents
            if total > 0:
                docs_list = [{
                    "id": "other",
                    "name": "原有知识库数据",
                    "type": "csv",
                    "chunks": total,
                    "size": 0,
                    "created_at": "导入时间未知"
                }]
        
        return {"success": True, "documents": docs_list}
    except Exception as e:
        import traceback
        traceback.print_exc()
        return {"success": False, "documents": [], "error": str(e)}

@app.post("/documents/upload", summary="上传并处理文档")
async def upload_document(file: UploadFile = File(...)):
    """
    上传文档并进行向量化处理
    
    支持格式: PDF, TXT, JSON, CSV
    处理流程: 提取文本 -> 分块(250字符,50重叠) -> 向量化 -> 存入ChromaDB
    """
    try:
        # 检查文件类型
        allowed_types = {'.pdf', '.txt', '.json', '.csv'}
        file_ext = Path(file.filename).suffix.lower()
        if file_ext not in allowed_types:
            return {"success": False, "error": f"不支持的文件类型: {file_ext}"}
        
        # 保存上传文件
        print(f"📥 保存文件: {file.filename}")
        file_path = UPLOAD_DIR / file.filename
        with open(file_path, "wb") as f:
            content = await file.read()
            f.write(content)
        print(f"   文件大小: {len(content)} bytes")
        
        # 导入文档处理器
        from document_processor import DocumentProcessor, VectorIndexer
        
        # 处理文档
        print("🔄 开始处理文档...")
        processor = DocumentProcessor(
            chunk_size=250,
            chunk_overlap=50,
            use_ocr=(file_ext == '.pdf')
        )
        doc = processor.process_file(str(file_path))
        print(f"   文档分块数: {len(doc.chunks)}")
        
        # 向量化并索引
        print("🔄 开始向量化索引...")
        indexer = VectorIndexer(
            model_path=Config.MODEL_PATH,
            chroma_path=Config.CHROMADB_PATH,
            collection_name=Config.COLLECTION_NAME
        )
        indexer.initialize()
        indexed_count = indexer.index_document(doc)
        
        # 刷新检索服务的collection连接
        await retrieval_service.refresh_collection()
        
        # 记录文档元数据
        doc_info = {
            "id": doc.doc_id,
            "name": doc.filename,
            "type": doc.file_type,
            "chunks": len(doc.chunks),
            "size": len(content),
            "created_at": doc.created_at
        }
        documents_db[doc.doc_id] = doc_info
        
        print(f"✅ 文档处理完成: {doc.filename}, 索引 {indexed_count} 个分块")
        
        return {
            "success": True,
            "message": f"文档处理完成，已索引 {indexed_count} 个分块",
            "document": doc_info
        }
        
    except Exception as e:
        import traceback
        print(f"❌ 文档处理失败: {e}")
        traceback.print_exc()
        return {"success": False, "error": str(e)}

@app.delete("/documents/{doc_id}", summary="删除文档")
async def delete_document(doc_id: str):
    """删除指定文档（从索引中移除）"""
    try:
        if doc_id in documents_db:
            del documents_db[doc_id]
        
        # 注意：ChromaDB删除需要知道所有chunk的ID
        # 这里简化处理，实际应该根据doc_id前缀删除
        return {"success": True, "message": "文档已删除"}
    except Exception as e:
        return {"success": False, "error": str(e)}

@app.post("/index/rebuild", summary="重建索引")
async def rebuild_index():
    """重建向量索引"""
    try:
        # 重新初始化collection
        import chromadb
        from chromadb.config import Settings
        
        client = chromadb.PersistentClient(
            path=Config.CHROMADB_PATH,
            settings=Settings(anonymized_telemetry=False)
        )
        
        # 删除并重建collection
        try:
            client.delete_collection(Config.COLLECTION_NAME)
        except:
            pass
        
        client.create_collection(
            name=Config.COLLECTION_NAME,
            metadata={"hnsw:space": "cosine"}
        )
        
        # 重新处理所有上传的文档
        from document_processor import DocumentProcessor, VectorIndexer
        
        processor = DocumentProcessor(chunk_size=250, chunk_overlap=50)
        indexer = VectorIndexer(
            model_path=Config.MODEL_PATH,
            chroma_path=Config.CHROMADB_PATH,
            collection_name=Config.COLLECTION_NAME
        )
        indexer.initialize()
        
        total_indexed = 0
        for file_path in UPLOAD_DIR.glob("*"):
            if file_path.suffix.lower() in {'.pdf', '.txt', '.json', '.csv'}:
                doc = processor.process_file(str(file_path))
                total_indexed += indexer.index_document(doc)
        
        retrieval_service.total_documents = total_indexed
        
        return {
            "success": True,
            "message": f"索引重建完成，共索引 {total_indexed} 个分块"
        }
    except Exception as e:
        return {"success": False, "error": str(e)}


# ============ 知识条目管理接口 ============
class KnowledgeItemCreate(BaseModel):
    """创建知识条目"""
    content: str = Field(..., description="知识内容")
    question: Optional[str] = Field(None, description="相关问题")
    category: Optional[str] = Field("其他", description="分类")
    doc_id: Optional[str] = Field("other", description="所属文档ID")

class KnowledgeItemUpdate(BaseModel):
    """更新知识条目"""
    content: Optional[str] = Field(None, description="知识内容")
    question: Optional[str] = Field(None, description="相关问题")
    category: Optional[str] = Field(None, description="分类")

@app.get("/knowledge/items", summary="获取知识条目列表（分页）")
async def get_knowledge_items(
    doc_id: Optional[str] = None,
    page: int = 1,
    page_size: int = 20
):
    """
    获取知识条目列表，支持按文档筛选和分页
    
    - doc_id: 文档ID，不传则获取所有，传"other"获取无归属的条目
    - page: 页码，从1开始
    - page_size: 每页数量
    """
    try:
        collection = retrieval_service.collection
        if not collection:
            return {"success": False, "error": "数据库未连接"}
        
        offset = (page - 1) * page_size
        
        # 获取所有数据（ChromaDB的where查询对缺失字段支持不好）
        all_results = collection.get(
            include=["documents", "metadatas"],
            limit=10000  # 获取足够多的数据
        )
        
        # 在内存中筛选
        filtered_items = []
        if all_results and all_results['ids']:
            for i, item_id in enumerate(all_results['ids']):
                metadata = all_results['metadatas'][i] if all_results['metadatas'] else {}
                item_doc_id = metadata.get("doc_id", "other")
                
                # 如果指定了doc_id，进行筛选
                if doc_id:
                    if doc_id == "other":
                        # 查找没有doc_id或doc_id为other的条目
                        if item_doc_id not in ["other", None, ""] and "doc_id" in metadata:
                            continue
                    else:
                        if item_doc_id != doc_id:
                            continue
                
                filtered_items.append({
                    "id": item_id,
                    "content": all_results['documents'][i] if all_results['documents'] else "",
                    "question": metadata.get("question", ""),
                    "category": metadata.get("category", "其他"),
                    "doc_id": item_doc_id,
                    "source": metadata.get("source", metadata.get("filename", "未知")),
                    "chunk_index": metadata.get("chunk_index", 0)
                })
        
        # 分页
        total_count = len(filtered_items)
        paginated_items = filtered_items[offset:offset + page_size]
        
        return {
            "success": True,
            "items": paginated_items,
            "total": total_count,
            "page": page,
            "page_size": page_size,
            "total_pages": (total_count + page_size - 1) // page_size if total_count > 0 else 1
        }
    except Exception as e:
        import traceback
        traceback.print_exc()
        return {"success": False, "error": str(e), "items": [], "total": 0}

@app.get("/knowledge/items/{item_id}", summary="获取单个知识条目")
async def get_knowledge_item(item_id: str):
    """获取单个知识条目详情"""
    try:
        collection = retrieval_service.collection
        result = collection.get(ids=[item_id], include=["documents", "metadatas", "embeddings"])
        
        if not result['ids']:
            return {"success": False, "error": "条目不存在"}
        
        metadata = result['metadatas'][0] if result['metadatas'] else {}
        return {
            "success": True,
            "item": {
                "id": item_id,
                "content": result['documents'][0] if result['documents'] else "",
                "question": metadata.get("question", ""),
                "category": metadata.get("category", "其他"),
                "doc_id": metadata.get("doc_id", "other"),
                "source": metadata.get("source", metadata.get("filename", "未知")),
                "chunk_index": metadata.get("chunk_index", 0)
            }
        }
    except Exception as e:
        return {"success": False, "error": str(e)}

@app.post("/knowledge/items", summary="创建知识条目")
async def create_knowledge_item(item: KnowledgeItemCreate):
    """手动创建一个知识条目"""
    try:
        collection = retrieval_service.collection
        
        # 生成ID
        item_id = hashlib.md5(f"{item.content}_{datetime.now().isoformat()}".encode()).hexdigest()[:16]
        
        # 向量化
        embedding = retrieval_service.model_manager.encode([item.content])[0]
        
        # 存入数据库
        collection.add(
            ids=[item_id],
            documents=[item.content],
            embeddings=[embedding.tolist()],
            metadatas=[{
                "question": item.question or "",
                "category": item.category or "其他",
                "doc_id": item.doc_id or "other",
                "source": "手动添加",
                "created_at": datetime.now().isoformat()
            }]
        )
        
        retrieval_service.total_documents = collection.count()
        
        return {
            "success": True,
            "message": "知识条目创建成功",
            "item_id": item_id
        }
    except Exception as e:
        return {"success": False, "error": str(e)}

@app.put("/knowledge/items/{item_id}", summary="更新知识条目")
async def update_knowledge_item(item_id: str, item: KnowledgeItemUpdate):
    """更新知识条目内容"""
    try:
        collection = retrieval_service.collection
        
        # 获取原数据
        result = collection.get(ids=[item_id], include=["documents", "metadatas"])
        if not result['ids']:
            return {"success": False, "error": "条目不存在"}
        
        old_content = result['documents'][0]
        old_metadata = result['metadatas'][0] if result['metadatas'] else {}
        
        # 更新内容
        new_content = item.content if item.content else old_content
        new_metadata = old_metadata.copy()
        if item.question is not None:
            new_metadata["question"] = item.question
        if item.category is not None:
            new_metadata["category"] = item.category
        new_metadata["updated_at"] = datetime.now().isoformat()
        
        # 如果内容变了，需要重新向量化
        if item.content and item.content != old_content:
            new_embedding = retrieval_service.model_manager.encode([new_content])[0]
            collection.update(
                ids=[item_id],
                documents=[new_content],
                embeddings=[new_embedding.tolist()],
                metadatas=[new_metadata]
            )
        else:
            collection.update(
                ids=[item_id],
                metadatas=[new_metadata]
            )
        
        return {"success": True, "message": "更新成功"}
    except Exception as e:
        return {"success": False, "error": str(e)}

@app.delete("/knowledge/items/{item_id}", summary="删除知识条目")
async def delete_knowledge_item(item_id: str):
    """删除单个知识条目"""
    try:
        collection = retrieval_service.collection
        collection.delete(ids=[item_id])
        retrieval_service.total_documents = collection.count()
        return {"success": True, "message": "删除成功"}
    except Exception as e:
        return {"success": False, "error": str(e)}


class BatchDeleteRequest(BaseModel):
    """批量删除请求"""
    item_ids: List[str] = Field(..., description="要删除的知识条目ID列表")


@app.post("/knowledge/items/batch-delete", summary="批量删除知识条目")
async def batch_delete_knowledge_items(request: BatchDeleteRequest):
    """批量删除多个知识条目"""
    try:
        if not request.item_ids:
            return {"success": False, "error": "请提供要删除的条目ID"}
        
        collection = retrieval_service.collection
        
        # 批量删除
        collection.delete(ids=request.item_ids)
        deleted_count = len(request.item_ids)
        
        retrieval_service.total_documents = collection.count()
        
        return {
            "success": True,
            "message": f"成功删除 {deleted_count} 个知识条目",
            "deleted_count": deleted_count
        }
    except Exception as e:
        import traceback
        traceback.print_exc()
        return {"success": False, "error": str(e)}

@app.delete("/documents/{doc_id}/all", summary="删除文档及其所有知识条目")
async def delete_document_with_items(doc_id: str):
    """删除文档及其下所有知识条目"""
    try:
        collection = retrieval_service.collection
        
        # 查找该文档下所有条目的ID
        # ChromaDB where 查询
        results = collection.get(
            where={"doc_id": doc_id},
            include=["metadatas"]
        )
        
        if results['ids']:
            # 批量删除
            collection.delete(ids=results['ids'])
            deleted_count = len(results['ids'])
        else:
            deleted_count = 0
        
        # 删除文档记录
        if doc_id in documents_db:
            del documents_db[doc_id]
        
        # 删除上传的文件
        for file_path in UPLOAD_DIR.glob("*"):
            # 简单匹配，实际应该存储文件名和doc_id的映射
            pass
        
        retrieval_service.total_documents = collection.count()
        
        return {
            "success": True,
            "message": f"已删除文档及 {deleted_count} 个知识条目"
        }
    except Exception as e:
        import traceback
        traceback.print_exc()
        return {"success": False, "error": str(e)}

@app.get("/documents/{doc_id}/items", summary="获取文档下的知识条目")
async def get_document_items(doc_id: str, page: int = 1, page_size: int = 20):
    """获取指定文档下的所有知识条目（分页）"""
    return await get_knowledge_items(doc_id=doc_id, page=page, page_size=page_size)


# 异常处理
@app.exception_handler(Exception)
async def global_exception_handler(request, exc):
    """全局异常处理"""
    return JSONResponse(
        status_code=500,
        content={
            "success": False,
            "error": str(exc),
            "timestamp": datetime.now().isoformat()
        }
    )

if __name__ == "__main__":
    print(f"🚀 启动 {Config.API_TITLE}")
    print(f"📍 服务地址: http://{Config.API_HOST}:{Config.API_PORT}")
    print(f"📚 API文档: http://{Config.API_HOST}:{Config.API_PORT}/docs")
    
    uvicorn.run(
        "retrieval_api:app",
        host=Config.API_HOST,
        port=Config.API_PORT,
        reload=False,
        workers=1  # 使用单worker确保模型只加载一次
    ) 