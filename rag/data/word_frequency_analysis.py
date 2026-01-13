#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
糖尿病QA数据集词频统计和词云分析脚本
- 中文分词和词频统计
- 生成专业的词云图
- 支持中文字体显示
"""

import pandas as pd
import jieba
import jieba.posseg as pseg
from collections import Counter
import matplotlib.pyplot as plt
from wordcloud import WordCloud
import numpy as np
import seaborn as sns
import os
import platform
from PIL import Image, ImageFont
import warnings
warnings.filterwarnings('ignore')

# 设置matplotlib支持中文
plt.rcParams['font.sans-serif'] = ['SimHei', 'Microsoft YaHei', 'DejaVu Sans']
plt.rcParams['axes.unicode_minus'] = False

class ChineseTextAnalyzer:
    """中文文本分析器"""
    
    def __init__(self, csv_file: str):
        """
        初始化分析器
        
        Args:
            csv_file: CSV文件路径
        """
        self.csv_file = csv_file
        self.df = None
        self.medical_stopwords = self._load_medical_stopwords()
        self.font_path = self._get_chinese_font_path()
        
        # 加载医学词典
        self._load_medical_dict()
        
    def _load_medical_stopwords(self):
        """加载医学专业停用词"""
        basic_stopwords = {
            '的', '是', '在', '有', '和', '与', '或', '等', '如', '由于', '因为', '所以', 
            '但是', '然而', '而且', '另外', '此外', '同时', '通过', '根据', '对于', '关于',
            '什么', '哪些', '怎样', '如何', '为什么', '会', '可以', '能够', '需要', '应该',
            '一般', '常见', '主要', '重要', '基本', '正常', '异常', '严重', '轻微', '明显',
            '进行', '出现', '发生', '引起', '导致', '造成', '产生', '形成', '具有', '包括',
            '以及', '还有', '其中', '之间', '之后', '之前', '时候', '情况', '方面', '问题'
        }
        return basic_stopwords
    
    def _load_medical_dict(self):
        """加载医学专业词汇词典"""
        medical_terms = [
            '糖尿病', '视网膜病变', '黄斑水肿', '胰岛素', '血糖', '血压', '并发症',
            '诊断', '治疗', '症状', '眼底', '视力', '视野', '眼科', '内分泌科',
            '激光治疗', '抗VEGF', '玻璃体切割', '眼内注射', '荧光造影',
            '糖化血红蛋白', '空腹血糖', '餐后血糖', '胰岛功能', '胰岛素抵抗'
        ]
        
        for term in medical_terms:
            jieba.add_word(term, freq=1000)
    
    def _get_chinese_font_path(self):
        """获取中文字体路径"""
        system = platform.system()
        
        if system == "Windows":
            font_paths = [
                "C:/Windows/Fonts/msyh.ttc",  # 微软雅黑
                "C:/Windows/Fonts/simhei.ttf",  # 黑体
                "C:/Windows/Fonts/simsun.ttc",  # 宋体
            ]
        elif system == "Darwin":  # macOS
            font_paths = [
                "/System/Library/Fonts/Hiragino Sans GB.ttc",
                "/System/Library/Fonts/PingFang.ttc",
            ]
        else:  # Linux
            font_paths = [
                "/usr/share/fonts/truetype/wqy/wqy-microhei.ttc",
                "/usr/share/fonts/truetype/droid/DroidSansFallbackFull.ttf",
            ]
        
        for font_path in font_paths:
            if os.path.exists(font_path):
                return font_path
        
        print("警告: 未找到合适的中文字体，词云可能显示异常")
        return None
    
    def load_data(self):
        """加载CSV数据"""
        print(f"加载数据文件: {self.csv_file}")
        self.df = pd.read_csv(self.csv_file, encoding='utf-8')
        print(f"数据形状: {self.df.shape}")
        return self.df
    
    def preprocess_text(self, text):
        """
        预处理文本
        
        Args:
            text: 输入文本
            
        Returns:
            处理后的词汇列表
        """
        if pd.isna(text) or text.strip() == "":
            return []
        
        # 分词并保留名词、动词、形容词
        words = []
        for word, flag in pseg.cut(text):
            # 过滤条件：长度大于1，不是停用词，是有意义的词性
            if (len(word) > 1 and 
                word not in self.medical_stopwords and 
                flag in ['n', 'nr', 'ns', 'nt', 'nz', 'v', 'vd', 'vn', 'a', 'ad', 'an']):
                words.append(word)
        
        return words
    
    def analyze_word_frequency(self):
        """分析词频"""
        print("\n开始词频分析...")
        
        # 分别处理问题和回答
        question_words = []
        context_words = []
        
        for _, row in self.df.iterrows():
            question_words.extend(self.preprocess_text(row['question']))
            context_words.extend(self.preprocess_text(row['context']))
        
        # 统计词频
        question_freq = Counter(question_words)
        context_freq = Counter(context_words)
        all_freq = Counter(question_words + context_words)
        
        return {
            'question_freq': question_freq,
            'context_freq': context_freq,
            'all_freq': all_freq
        }
    
    def plot_top_words(self, freq_counter, title, top_n=20, save_path=None):
        """绘制高频词条形图"""
        top_words = freq_counter.most_common(top_n)
        words, counts = zip(*top_words)
        
        plt.figure(figsize=(12, 8))
        sns.barplot(x=list(counts), y=list(words), palette='viridis')
        plt.title(f'{title} - 前{top_n}高频词', fontsize=16, fontweight='bold')
        plt.xlabel('词频', fontsize=12)
        plt.ylabel('词汇', fontsize=12)
        
        # 在条形图上显示数值
        for i, count in enumerate(counts):
            plt.text(count + max(counts)*0.01, i, str(count), 
                    va='center', fontsize=10)
        
        plt.tight_layout()
        
        if save_path:
            plt.savefig(save_path, dpi=300, bbox_inches='tight')
            print(f"图表已保存到: {save_path}")
        
        plt.show()
    
    def create_wordcloud(self, freq_counter, title, save_path=None, mask_shape=None):
        """创建词云图"""
        if not freq_counter:
            print("词频数据为空，无法生成词云")
            return
        
        # 配置词云参数
        wordcloud_config = {
            'width': 800,
            'height': 600,
            'background_color': 'white',
            'max_words': 100,
            'colormap': 'Set3',
            'relative_scaling': 0.5,
            'random_state': 42
        }
        
        # 如果有中文字体路径，添加字体配置
        if self.font_path:
            wordcloud_config['font_path'] = self.font_path
        
        # 如果有遮罩形状
        if mask_shape is not None:
            wordcloud_config['mask'] = mask_shape
        
        # 创建词云
        wordcloud = WordCloud(**wordcloud_config).generate_from_frequencies(freq_counter)
        
        plt.figure(figsize=(12, 8))
        plt.imshow(wordcloud, interpolation='bilinear')
        plt.axis('off')
        plt.title(title, fontsize=16, fontweight='bold', pad=20)
        
        if save_path:
            plt.savefig(save_path, dpi=300, bbox_inches='tight')
            print(f"词云图已保存到: {save_path}")
        
        plt.show()
    
    def create_heart_mask(self):
        """创建心形遮罩（适合医学主题）"""
        try:
            # 创建心形遮罩
            x = np.linspace(-2, 2, 400)
            y = np.linspace(-1, 1.5, 400)
            X, Y = np.meshgrid(x, y)
            
            # 心形方程: (x²+y²-1)³ - x²y³ ≤ 0
            heart = (X**2 + Y**2 - 1)**3 - X**2 * Y**3
            mask = np.zeros_like(heart, dtype=np.uint8)
            mask[heart <= 0] = 255
            
            return mask
        except:
            return None
    
    def generate_analysis_report(self, freq_data, output_dir="analysis_output"):
        """生成完整的分析报告"""
        
        # 创建输出目录
        os.makedirs(output_dir, exist_ok=True)
        
        print(f"\n=== 糖尿病QA数据集词频分析报告 ===")
        print(f"数据总量: {len(self.df)} 条记录")
        print(f"分析时间: {pd.Timestamp.now().strftime('%Y-%m-%d %H:%M:%S')}")
        
        # 1. 整体词频分析
        print("\n1. 整体高频词分析:")
        top_all = freq_data['all_freq'].most_common(10)
        for i, (word, count) in enumerate(top_all, 1):
            print(f"   {i:2d}. {word:<10} : {count:>3} 次")
        
        # 2. 问题词频分析
        print("\n2. 问题高频词分析:")
        top_questions = freq_data['question_freq'].most_common(10)
        for i, (word, count) in enumerate(top_questions, 1):
            print(f"   {i:2d}. {word:<10} : {count:>3} 次")
        
        # 3. 回答词频分析
        print("\n3. 回答高频词分析:")
        top_contexts = freq_data['context_freq'].most_common(10)
        for i, (word, count) in enumerate(top_contexts, 1):
            print(f"   {i:2d}. {word:<10} : {count:>3} 次")
        
        # 生成可视化图表
        print("\n4. 生成可视化图表...")
        
        # 条形图
        self.plot_top_words(
            freq_data['all_freq'], 
            "糖尿病QA数据集整体词频", 
            save_path=f"{output_dir}/all_words_frequency.png"
        )
        
        self.plot_top_words(
            freq_data['question_freq'], 
            "问题词频分析", 
            save_path=f"{output_dir}/question_words_frequency.png"
        )
        
        self.plot_top_words(
            freq_data['context_freq'], 
            "回答词频分析", 
            save_path=f"{output_dir}/context_words_frequency.png"
        )
        
        # 词云图
        print("\n5. 生成词云图...")
        
        # 创建心形遮罩
        heart_mask = self.create_heart_mask()
        
        self.create_wordcloud(
            freq_data['all_freq'],
            "糖尿病QA数据集词云 - 整体",
            save_path=f"{output_dir}/all_words_wordcloud.png"
        )
        
        self.create_wordcloud(
            freq_data['question_freq'],
            "糖尿病QA数据集词云 - 问题",
            save_path=f"{output_dir}/question_wordcloud.png",
            mask_shape=heart_mask
        )
        
        self.create_wordcloud(
            freq_data['context_freq'],
            "糖尿病QA数据集词云 - 回答",
            save_path=f"{output_dir}/context_wordcloud.png"
        )
        
        # 保存词频数据到文件
        freq_df = pd.DataFrame([
            {'类型': '整体', '词汇': word, '频次': count} 
            for word, count in freq_data['all_freq'].most_common(50)
        ] + [
            {'类型': '问题', '词汇': word, '频次': count} 
            for word, count in freq_data['question_freq'].most_common(30)
        ] + [
            {'类型': '回答', '词汇': word, '频次': count} 
            for word, count in freq_data['context_freq'].most_common(30)
        ])
        
        freq_df.to_csv(f"{output_dir}/word_frequency_data.csv", 
                      index=False, encoding='utf-8-sig')
        
        print(f"\n✅ 分析完成！结果已保存到 {output_dir}/ 目录下")
        print(f"   - 词频数据: word_frequency_data.csv")
        print(f"   - 图表文件: *.png")

def main():
    """主函数"""
    csv_file = "diabetes_qa_chinese.csv"
    
    if not os.path.exists(csv_file):
        print(f"错误: 找不到文件 {csv_file}")
        print("请确保已运行数据预处理脚本生成中文翻译文件")
        return
    
    try:
        # 初始化分析器
        analyzer = ChineseTextAnalyzer(csv_file)
        
        # 加载数据
        analyzer.load_data()
        
        # 词频分析
        freq_data = analyzer.analyze_word_frequency()
        
        # 生成完整分析报告
        analyzer.generate_analysis_report(freq_data)
        
    except Exception as e:
        print(f"分析过程中发生错误: {str(e)}")
        raise

if __name__ == "__main__":
    main() 