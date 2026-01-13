<template>
  <div class="search-view">
    <!-- 搜索区域 -->
    <div class="card">
      <div class="card-header">
        <div class="card-title">
          <el-icon><Search /></el-icon>
          知识库语义检索
        </div>
      </div>
      
      <div class="search-box">
        <el-input
          v-model="query"
          placeholder="输入您的问题，例如：糖尿病的诊断标准是什么？"
          size="large"
          class="search-input"
          @keyup.enter="handleSearch"
          clearable
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
        <el-button 
          type="primary" 
          size="large"
          :loading="ragStore.searchLoading"
          @click="handleSearch"
        >
          搜索
        </el-button>
      </div>

      <!-- 搜索选项 -->
      <el-collapse>
        <el-collapse-item title="高级选项" name="options">
          <el-row :gutter="20">
            <el-col :span="8">
              <div class="option-item">
                <label>返回数量</label>
                <el-slider v-model="options.topK" :min="1" :max="10" show-input />
              </div>
            </el-col>
            <el-col :span="8">
              <div class="option-item">
                <label>相似度阈值</label>
                <el-slider v-model="options.threshold" :min="0" :max="1" :step="0.1" show-input />
              </div>
            </el-col>
            <el-col :span="8">
              <div class="option-item">
                <el-checkbox v-model="options.useCache">使用缓存</el-checkbox>
                <el-checkbox v-model="options.includeEntities">显示实体</el-checkbox>
              </div>
            </el-col>
          </el-row>
        </el-collapse-item>
      </el-collapse>
    </div>

    <!-- 搜索结果 -->
    <div class="card" v-if="searchMeta.searched">
      <div class="card-header">
        <div class="card-title">
          <el-icon><Document /></el-icon>
          搜索结果
          <el-tag size="small" style="margin-left: 8px;">
            {{ ragStore.searchResults.length }} 条
          </el-tag>
        </div>
        <div class="search-meta">
          <el-tag :type="searchMeta.cacheHit ? 'success' : 'info'" size="small">
            {{ searchMeta.cacheHit ? '缓存命中' : '实时检索' }}
          </el-tag>
          <span class="search-time">耗时: {{ searchMeta.searchTime }}s</span>
        </div>
      </div>

      <!-- 结果列表 -->
      <div v-if="ragStore.searchResults.length > 0">
        <div 
          v-for="item in ragStore.searchResults" 
          :key="item.rank"
          class="result-item"
          @click="showDetail(item)"
        >
          <div class="result-header">
            <div class="result-rank">{{ item.rank }}</div>
            <div class="result-similarity">
              相似度: {{ (item.similarity * 100).toFixed(1) }}%
            </div>
          </div>
          <div class="result-question">{{ item.question }}</div>
          <div class="result-answer">{{ truncateText(item.answer, 200) }}</div>
          <div class="result-footer">
            <div class="result-left">
              <span class="result-category">{{ item.category }}</span>
              <span class="result-source" v-if="item.source_info?.source">
                <el-icon><Document /></el-icon>
                {{ item.source_info.source }}
              </span>
            </div>
            <div class="result-entities" v-if="item.entities?.length">
              <el-tag 
                v-for="entity in item.entities.slice(0, 5)" 
                :key="entity"
                size="small"
                type="info"
              >
                {{ entity }}
              </el-tag>
            </div>
          </div>
        </div>
      </div>

      <!-- 空结果 -->
      <div v-else class="empty-state">
        <el-icon class="empty-icon"><DocumentDelete /></el-icon>
        <p>未找到相关结果，请尝试其他关键词</p>
      </div>
    </div>

    <!-- 详情弹窗 -->
    <el-dialog v-model="detailVisible" title="详细内容" width="600px">
      <div v-if="currentDetail">
        <h4 style="margin-bottom: 12px; color: var(--text-primary);">
          {{ currentDetail.question }}
        </h4>
        <p style="line-height: 1.8; color: var(--text-secondary);">
          {{ currentDetail.answer }}
        </p>
        <el-divider />
        <div style="display: flex; gap: 12px; flex-wrap: wrap;">
          <el-tag>分类: {{ currentDetail.category }}</el-tag>
          <el-tag type="success">相似度: {{ (currentDetail.similarity * 100).toFixed(1) }}%</el-tag>
        </div>
        <div v-if="currentDetail.entities?.length" style="margin-top: 12px;">
          <span style="color: var(--text-muted); font-size: 13px;">医学实体:</span>
          <div style="margin-top: 8px; display: flex; gap: 8px; flex-wrap: wrap;">
            <el-tag v-for="e in currentDetail.entities" :key="e" size="small" type="info">
              {{ e }}
            </el-tag>
          </div>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRagStore } from '@/stores/rag'
import { ElMessage } from 'element-plus'

const ragStore = useRagStore()

const query = ref('')
const options = reactive({
  topK: 5,
  threshold: 0,
  useCache: true,
  includeEntities: true
})

const searchMeta = reactive({
  searched: false,
  cacheHit: false,
  searchTime: 0
})

const detailVisible = ref(false)
const currentDetail = ref(null)

const handleSearch = async () => {
  if (!query.value.trim()) {
    ElMessage.warning('请输入搜索内容')
    return
  }

  try {
    const result = await ragStore.search(query.value, options)
    searchMeta.searched = true
    searchMeta.cacheHit = result.cache_hit
    searchMeta.searchTime = result.search_time?.toFixed(3) || 0
  } catch (error) {
    ElMessage.error('搜索失败: ' + error.message)
  }
}

const truncateText = (text, maxLen) => {
  if (!text) return ''
  return text.length > maxLen ? text.slice(0, maxLen) + '...' : text
}

const showDetail = (item) => {
  currentDetail.value = item
  detailVisible.value = true
}
</script>

<style scoped>
.option-item {
  margin-bottom: 16px;
}

.option-item label {
  display: block;
  margin-bottom: 8px;
  font-size: 13px;
  color: var(--text-secondary);
}

.search-meta {
  display: flex;
  align-items: center;
  gap: 12px;
}

.search-time {
  font-size: 13px;
  color: var(--text-muted);
}

.result-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 12px;
}

.result-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.result-source {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: var(--text-muted);
  background: var(--bg-main);
  padding: 4px 8px;
  border-radius: 4px;
}

.result-entities {
  display: flex;
  gap: 6px;
  flex-wrap: wrap;
}
</style>
