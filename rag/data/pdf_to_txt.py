"""
PDF转TXT工具 - 支持左右分栏布局
使用 pdfplumber 库处理双栏PDF文档
"""

import pdfplumber
import os
import re
import argparse
from pathlib import Path


def clean_text_line(line: str) -> str:
    """
    清理单行文本
    - 去除多余空格
    - 保留必要的空格（如英文单词间）
    """
    # 去除行首尾空白
    line = line.strip()
    if not line:
        return ""
    
    # 将多个连续空格替换为单个空格
    line = re.sub(r' {2,}', ' ', line)
    
    # 去除中文字符之间的空格
    # 匹配：中文+空格+中文 或 中文+空格+中文标点 或 中文标点+空格+中文
    line = re.sub(r'([\u4e00-\u9fff])\s+([\u4e00-\u9fff])', r'\1\2', line)
    line = re.sub(r'([\u4e00-\u9fff])\s+([，。、；：？！""''（）【】])', r'\1\2', line)
    line = re.sub(r'([，。、；：？！""''（）【】])\s+([\u4e00-\u9fff])', r'\1\2', line)
    
    # 多次处理确保清理干净
    prev = ""
    while prev != line:
        prev = line
        line = re.sub(r'([\u4e00-\u9fff])\s+([\u4e00-\u9fff])', r'\1\2', line)
    
    return line


def clean_extracted_text(text: str) -> str:
    """
    清理提取的文本
    - 合并被错误分割的行
    - 去除多余空白
    - 保持段落结构
    """
    lines = text.split('\n')
    cleaned_lines = []
    
    for line in lines:
        cleaned = clean_text_line(line)
        if cleaned:
            cleaned_lines.append(cleaned)
    
    # 合并段落：如果一行不是以句号等结束，且下一行不是新段落开头，则合并
    merged_lines = []
    buffer = ""
    
    for line in cleaned_lines:
        # 判断是否是新段落开头（数字序号、章节标题等）
        is_new_paragraph = bool(re.match(r'^(第[一二三四五六七八九十百]+章|[一二三四五六七八九十]+、|\d+\.|[（\(]\d+[）\)]|·)', line))
        
        # 判断是否是目录行（包含页码）
        is_toc_line = bool(re.search(r'…+\s*\d+\s*$', line))
        
        if is_new_paragraph or is_toc_line:
            if buffer:
                merged_lines.append(buffer)
                buffer = ""
            merged_lines.append(line)
        else:
            # 检查上一行是否以句末标点结束
            if buffer and buffer[-1] in '。！？；：.!?;:':
                merged_lines.append(buffer)
                buffer = line
            elif buffer:
                # 合并到上一行
                buffer += line
            else:
                buffer = line
    
    if buffer:
        merged_lines.append(buffer)
    
    return '\n'.join(merged_lines)


def extract_two_column_pdf(pdf_path: str, output_path: str = None) -> str:
    """
    提取双栏PDF文档的文本内容
    
    Args:
        pdf_path: PDF文件路径
        output_path: 输出TXT文件路径，默认为同名.txt文件
    
    Returns:
        提取的文本内容
    """
    if not os.path.exists(pdf_path):
        raise FileNotFoundError(f"PDF文件不存在: {pdf_path}")
    
    # 默认输出路径
    if output_path is None:
        output_path = Path(pdf_path).with_suffix('.txt')
    
    full_text = []
    
    with pdfplumber.open(pdf_path) as pdf:
        total_pages = len(pdf.pages)
        print(f"开始处理PDF: {pdf_path}")
        print(f"总页数: {total_pages}")
        
        for i, page in enumerate(pdf.pages):
            print(f"处理第 {i + 1}/{total_pages} 页...")
            
            # 获取页面尺寸
            width = page.width
            height = page.height
            mid_x = width / 2
            
            # 裁剪左栏区域
            left_bbox = (0, 0, mid_x, height)
            left_page = page.within_bbox(left_bbox)
            left_text = left_page.extract_text() or ""
            
            # 裁剪右栏区域
            right_bbox = (mid_x, 0, width, height)
            right_page = page.within_bbox(right_bbox)
            right_text = right_page.extract_text() or ""
            
            # 清理并合并左右栏文本
            left_cleaned = clean_extracted_text(left_text)
            right_cleaned = clean_extracted_text(right_text)
            
            page_text = f"\n--- 第 {i + 1} 页 ---\n"
            page_text += left_cleaned
            page_text += "\n"
            page_text += right_cleaned
            
            full_text.append(page_text)
    
    # 合并所有页面
    result = "\n".join(full_text)
    
    # 最终清理：去除多余空行
    result = re.sub(r'\n{3,}', '\n\n', result)
    
    # 保存到文件
    with open(output_path, 'w', encoding='utf-8') as f:
        f.write(result)
    
    print(f"转换完成！输出文件: {output_path}")
    print(f"总字符数: {len(result)}")
    
    return result


def extract_single_column_pdf(pdf_path: str, output_path: str = None) -> str:
    """
    提取单栏PDF文档的文本内容（备用方法）
    
    Args:
        pdf_path: PDF文件路径
        output_path: 输出TXT文件路径
    
    Returns:
        提取的文本内容
    """
    if output_path is None:
        output_path = Path(pdf_path).with_suffix('.txt')
    
    full_text = []
    
    with pdfplumber.open(pdf_path) as pdf:
        for i, page in enumerate(pdf.pages):
            text = page.extract_text() or ""
            cleaned = clean_extracted_text(text)
            full_text.append(f"--- 第 {i + 1} 页 ---\n{cleaned}")
    
    result = "\n\n".join(full_text)
    result = re.sub(r'\n{3,}', '\n\n', result)
    
    with open(output_path, 'w', encoding='utf-8') as f:
        f.write(result)
    
    return result


def main():
    parser = argparse.ArgumentParser(description='PDF转TXT工具 - 支持双栏布局')
    parser.add_argument('pdf_path', help='PDF文件路径')
    parser.add_argument('-o', '--output', help='输出TXT文件路径', default=None)
    parser.add_argument('--single', action='store_true', help='使用单栏模式提取')
    
    args = parser.parse_args()
    
    if args.single:
        extract_single_column_pdf(args.pdf_path, args.output)
    else:
        extract_two_column_pdf(args.pdf_path, args.output)


if __name__ == '__main__':
    main()
#https://aike.smu.edu.cn/pluginfile.php/699357/mod_resource/content/1/2021%E4%B8%AD%E5%9B%BD2%E5%9E%8B%E7%B3%96%E5%B0%BF%E7%97%85%E9%98%B2%E6%B2%BB%E6%8C%87%E5%8D%97%EF%BC%882020%E5%B9%B4%E7%89%88%EF%BC%89.pdf