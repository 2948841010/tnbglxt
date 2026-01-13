#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
文档处理模块 - 支持PDF/TXT/CSV/JSON文档的处理和向量化
处理流程：
1. 提取文本（PDF支持OCR识别图片中的文字）
2. 按chunk_size=250, overlap=50切分
3. 向量化后存入ChromaDB
"""

import os
import json
import csv
import hashlib
from pathlib import Path
from typing import List, Dict, Optional, Tuple
from dataclasses import dataclass
from datetime import datetime

import fitz  # PyMuPDF
import numpy as np
from tqdm import tqdm


@dataclass
class DocumentChunk:
    """文档分块"""
    content: str
    metadata: Dict
    chunk_index: int


@dataclass
class ProcessedDocument:
    """处理后的文档"""
    doc_id: str
    filename: str
    file_type: str
    chunks: List[DocumentChunk]
    total_chars: int
    created_at: str


class DocumentProcessor:
    """文档处理器"""
    
    def __init__(
        self,
        chunk_size: int = 250,
        chunk_overlap: int = 50,
        use_ocr: bool = True,
        ocr_threshold: Tuple[float, float] = (0.3, 0.3)
    ):
        """
        初始化文档处理器
        
        Args:
            chunk_size: 分块大小（字符数）
            chunk_overlap: 分块重叠大小
            use_ocr: 是否对PDF中的图片进行OCR
            ocr_threshold: OCR图片尺寸阈值 (宽度比例, 高度比例)
        """
        self.chunk_size = chunk_size
        self.chunk_overlap = chunk_overlap
        self.use_ocr = use_ocr
        self.ocr_threshold = ocr_threshold
        self.ocr_engine = None
        
        if use_ocr:
            self._init_ocr()
    
    def _init_ocr(self):
        """初始化OCR引擎"""
        try:
            from rapidocr_onnxruntime import RapidOCR
            self.ocr_engine = RapidOCR()
            print("✅ RapidOCR 初始化成功")
        except ImportError:
            print("⚠️  RapidOCR 未安装，将跳过图片OCR")
            print("   安装命令: pip install rapidocr-onnxruntime")
            self.ocr_engine = None
    
    def process_file(self, file_path: str) -> ProcessedDocument:
        """
        处理单个文件
        
        Args:
            file_path: 文件路径
            
        Returns:
            ProcessedDocument: 处理后的文档对象
        """
        file_path = Path(file_path)
        if not file_path.exists():
            raise FileNotFoundError(f"文件不存在: {file_path}")
        
        file_type = file_path.suffix.lower().lstrip('.')
        filename = file_path.name
        
        # 根据文件类型选择处理方法
        if file_type == 'pdf':
            text = self._extract_pdf(str(file_path))
        elif file_type == 'txt':
            text = self._extract_txt(str(file_path))
        elif file_type == 'json':
            text = self._extract_json(str(file_path))
        elif file_type == 'csv':
            text = self._extract_csv(str(file_path))
        else:
            raise ValueError(f"不支持的文件类型: {file_type}")
        
        # 切分文本
        chunks = self._split_text(text, filename, file_type)
        
        # 生成文档ID
        doc_id = hashlib.md5(f"{filename}_{datetime.now().isoformat()}".encode()).hexdigest()[:12]
        
        return ProcessedDocument(
            doc_id=doc_id,
            filename=filename,
            file_type=file_type,
            chunks=chunks,
            total_chars=len(text),
            created_at=datetime.now().isoformat()
        )
    
    def _extract_pdf(self, file_path: str) -> str:
        """
        提取PDF文本（支持OCR）
        
        使用fitz提取文本，对图片使用OCR识别
        """
        doc = fitz.open(file_path)
        all_text = []
        
        print(f"📄 处理PDF: {file_path}")
        print(f"   总页数: {doc.page_count}")
        
        for page_num, page in enumerate(doc):
            print(f"   处理页面 {page_num + 1}/{doc.page_count}...", end="\r")
            
            # 提取页面文本
            page_text = page.get_text("text")
            all_text.append(page_text)
            
            # 如果启用OCR，处理页面中的图片
            if self.use_ocr and self.ocr_engine:
                img_text = self._ocr_page_images(doc, page, page_num)
                if img_text:
                    all_text.append(img_text)
        
        print(f"\n   ✅ PDF文本提取完成")
        doc.close()
        
        # 合并所有文本
        full_text = "\n".join(all_text)
        
        # 清理文本
        full_text = self._clean_text(full_text)
        
        return full_text
    
    def _ocr_page_images(self, doc, page, page_num: int) -> str:
        """对页面中的图片进行OCR"""
        import cv2
        from PIL import Image
        
        ocr_texts = []
        img_list = page.get_image_info(xrefs=True)
        
        for img_info in img_list:
            xref = img_info.get("xref")
            if not xref:
                continue
            
            bbox = img_info["bbox"]
            # 检查图片尺寸是否超过阈值
            width_ratio = (bbox[2] - bbox[0]) / page.rect.width
            height_ratio = (bbox[3] - bbox[1]) / page.rect.height
            
            if width_ratio < self.ocr_threshold[0] or height_ratio < self.ocr_threshold[1]:
                continue
            
            try:
                # 提取图片
                pix = fitz.Pixmap(doc, xref)
                
                # 转换为numpy数组
                img_array = np.frombuffer(pix.samples, dtype=np.uint8).reshape(
                    pix.height, pix.width, -1
                )
                
                # 如果页面有旋转，旋转图片
                if int(page.rotation) != 0:
                    img_array = self._rotate_image(img_array, 360 - page.rotation)
                
                # OCR识别
                result, _ = self.ocr_engine(img_array)
                if result:
                    ocr_result = [line[1] for line in result]
                    ocr_texts.extend(ocr_result)
                    
            except Exception as e:
                print(f"⚠️  OCR处理图片失败 (页{page_num+1}): {e}")
                continue
        
        return "\n".join(ocr_texts)
    
    def _rotate_image(self, img: np.ndarray, angle: float) -> np.ndarray:
        """旋转图片"""
        import cv2
        
        h, w = img.shape[:2]
        center = (w / 2, h / 2)
        
        # 获取旋转矩阵
        M = cv2.getRotationMatrix2D(center, angle, 1.0)
        
        # 计算新边界
        new_w = int(h * np.abs(M[0, 1]) + w * np.abs(M[0, 0]))
        new_h = int(h * np.abs(M[0, 0]) + w * np.abs(M[0, 1]))
        
        # 调整旋转矩阵
        M[0, 2] += (new_w - w) / 2
        M[1, 2] += (new_h - h) / 2
        
        return cv2.warpAffine(img, M, (new_w, new_h))
    
    def _extract_txt(self, file_path: str) -> str:
        """提取TXT文本"""
        encodings = ['utf-8', 'gbk', 'gb2312', 'utf-16']
        
        for encoding in encodings:
            try:
                with open(file_path, 'r', encoding=encoding) as f:
                    return self._clean_text(f.read())
            except UnicodeDecodeError:
                continue
        
        raise ValueError(f"无法解码文件: {file_path}")
    
    def _extract_json(self, file_path: str) -> str:
        """提取JSON文本（支持QA格式）"""
        with open(file_path, 'r', encoding='utf-8') as f:
            data = json.load(f)
        
        texts = []
        
        # 支持多种JSON格式
        if isinstance(data, list):
            for item in data:
                if isinstance(item, dict):
                    # QA格式
                    if 'question' in item and 'answer' in item:
                        texts.append(f"问题：{item['question']}\n答案：{item['answer']}")
                    # 通用格式
                    else:
                        texts.append(json.dumps(item, ensure_ascii=False))
                else:
                    texts.append(str(item))
        elif isinstance(data, dict):
            texts.append(json.dumps(data, ensure_ascii=False, indent=2))
        
        return self._clean_text("\n\n".join(texts))
    
    def _extract_csv(self, file_path: str) -> str:
        """提取CSV文本（支持QA格式）"""
        texts = []
        
        with open(file_path, 'r', encoding='utf-8') as f:
            reader = csv.DictReader(f)
            
            for row in reader:
                # 检查是否是QA格式
                if 'question' in row and 'answer' in row:
                    texts.append(f"问题：{row['question']}\n答案：{row['answer']}")
                else:
                    # 通用格式：将所有列拼接
                    row_text = " | ".join(f"{k}: {v}" for k, v in row.items() if v)
                    texts.append(row_text)
        
        return self._clean_text("\n\n".join(texts))
    
    def _clean_text(self, text: str) -> str:
        """清理文本"""
        import re
        
        # 去除多余空白
        text = re.sub(r'\s+', ' ', text)
        
        # 去除多余换行
        text = re.sub(r'\n{3,}', '\n\n', text)
        
        # 去除首尾空白
        text = text.strip()
        
        return text
    
    def _split_text(
        self, 
        text: str, 
        filename: str, 
        file_type: str
    ) -> List[DocumentChunk]:
        """
        切分文本
        
        使用滑动窗口方式切分，保证chunk之间有overlap
        """
        chunks = []
        
        if len(text) <= self.chunk_size:
            # 文本太短，不需要切分
            chunks.append(DocumentChunk(
                content=text,
                metadata={
                    "source": filename,
                    "file_type": file_type,
                    "chunk_index": 0,
                    "total_chunks": 1,
                    "text_length": len(text)
                },
                chunk_index=0
            ))
            return chunks
        
        # 滑动窗口切分
        start = 0
        chunk_index = 0
        
        while start < len(text):
            end = start + self.chunk_size
            
            # 尝试在句子边界切分
            if end < len(text):
                # 向后查找句子结束符
                for sep in ['。', '！', '？', '；', '\n', '.', '!', '?', ';']:
                    pos = text.rfind(sep, start, end)
                    if pos > start + self.chunk_size // 2:
                        end = pos + 1
                        break
            
            chunk_text = text[start:end].strip()
            
            if chunk_text:
                chunks.append(DocumentChunk(
                    content=chunk_text,
                    metadata={
                        "source": filename,
                        "file_type": file_type,
                        "chunk_index": chunk_index,
                        "text_length": len(chunk_text)
                    },
                    chunk_index=chunk_index
                ))
                chunk_index += 1
            
            # 下一个窗口起始位置（考虑overlap）
            start = end - self.chunk_overlap
            
            # 防止无限循环
            if start >= len(text) - self.chunk_overlap:
                break
        
        # 更新total_chunks
        for chunk in chunks:
            chunk.metadata["total_chunks"] = len(chunks)
        
        return chunks


class VectorIndexer:
    """向量索引器 - 将文档块向量化并存入ChromaDB"""
    
    def __init__(
        self,
        model_path: str = "data/models/AI-ModelScope/bge-large-zh-v1___5",
        chroma_path: str = "chroma_db",
        collection_name: str = "diabetes_knowledge",
        use_gpu: bool = True
    ):
        self.model_path = model_path
        self.chroma_path = chroma_path
        self.collection_name = collection_name
        self.use_gpu = use_gpu
        
        self.model = None
        self.chroma_client = None
        self.collection = None
    
    def initialize(self):
        """初始化模型和数据库"""
        import torch
        from sentence_transformers import SentenceTransformer
        import chromadb
        from chromadb.config import Settings
        
        # 加载向量模型
        print("🔄 加载向量模型...")
        device = 'cuda' if self.use_gpu and torch.cuda.is_available() else 'cpu'
        self.model = SentenceTransformer(self.model_path, device=device)
        print(f"✅ 模型加载完成，设备: {device}")
        
        # 初始化ChromaDB
        print("🔄 初始化ChromaDB...")
        self.chroma_client = chromadb.PersistentClient(
            path=self.chroma_path,
            settings=Settings(anonymized_telemetry=False)
        )
        
        # 获取或创建collection
        try:
            self.collection = self.chroma_client.get_collection(self.collection_name)
            print(f"✅ 已连接到collection: {self.collection_name}")
        except:
            self.collection = self.chroma_client.create_collection(
                name=self.collection_name,
                metadata={"hnsw:space": "cosine"}
            )
            print(f"✅ 创建新collection: {self.collection_name}")
    
    def index_document(self, doc: ProcessedDocument) -> int:
        """
        将文档索引到向量数据库
        
        Returns:
            int: 索引的chunk数量
        """
        if not self.model or not self.collection:
            raise RuntimeError("请先调用 initialize() 初始化")
        
        print(f"📥 索引文档: {doc.filename}")
        print(f"   分块数: {len(doc.chunks)}")
        
        # 准备数据
        ids = []
        documents = []
        metadatas = []
        
        for chunk in doc.chunks:
            chunk_id = f"{doc.doc_id}_{chunk.chunk_index}"
            ids.append(chunk_id)
            documents.append(chunk.content)
            
            metadata = chunk.metadata.copy()
            metadata["doc_id"] = doc.doc_id
            metadata["filename"] = doc.filename
            metadata["created_at"] = doc.created_at
            metadatas.append(metadata)
        
        # 批量向量化
        print("🔄 向量化中...")
        embeddings = self.model.encode(
            documents,
            normalize_embeddings=True,
            show_progress_bar=False,
            batch_size=32
        )
        print(f"   ✅ 向量化完成，共 {len(documents)} 个分块")
        
        # 存入ChromaDB
        print("🔄 存入数据库...")
        self.collection.add(
            ids=ids,
            documents=documents,
            embeddings=embeddings.tolist(),
            metadatas=metadatas
        )
        
        print(f"✅ 索引完成，共 {len(ids)} 个分块")
        return len(ids)
    
    def get_stats(self) -> Dict:
        """获取索引统计"""
        if not self.collection:
            return {"total_documents": 0}
        
        return {
            "total_documents": self.collection.count(),
            "collection_name": self.collection_name
        }


def process_and_index(
    file_path: str,
    chunk_size: int = 250,
    chunk_overlap: int = 50,
    use_ocr: bool = True
) -> Dict:
    """
    处理并索引单个文件的便捷函数
    
    Args:
        file_path: 文件路径
        chunk_size: 分块大小
        chunk_overlap: 分块重叠
        use_ocr: 是否使用OCR
        
    Returns:
        处理结果统计
    """
    # 初始化处理器
    processor = DocumentProcessor(
        chunk_size=chunk_size,
        chunk_overlap=chunk_overlap,
        use_ocr=use_ocr
    )
    
    # 处理文档
    doc = processor.process_file(file_path)
    
    # 初始化索引器
    indexer = VectorIndexer()
    indexer.initialize()
    
    # 索引文档
    indexed_count = indexer.index_document(doc)
    
    return {
        "filename": doc.filename,
        "file_type": doc.file_type,
        "total_chars": doc.total_chars,
        "chunks": len(doc.chunks),
        "indexed": indexed_count,
        "doc_id": doc.doc_id
    }


if __name__ == "__main__":
    import argparse
    
    parser = argparse.ArgumentParser(description="文档处理和向量化工具")
    parser.add_argument("file", help="要处理的文件路径")
    parser.add_argument("--chunk-size", type=int, default=250, help="分块大小")
    parser.add_argument("--chunk-overlap", type=int, default=50, help="分块重叠")
    parser.add_argument("--no-ocr", action="store_true", help="禁用OCR")
    
    args = parser.parse_args()
    
    result = process_and_index(
        args.file,
        chunk_size=args.chunk_size,
        chunk_overlap=args.chunk_overlap,
        use_ocr=not args.no_ocr
    )
    
    print("\n" + "=" * 50)
    print("处理结果:")
    print(f"  文件名: {result['filename']}")
    print(f"  类型: {result['file_type']}")
    print(f"  总字符: {result['total_chars']}")
    print(f"  分块数: {result['chunks']}")
    print(f"  已索引: {result['indexed']}")
    print(f"  文档ID: {result['doc_id']}")
