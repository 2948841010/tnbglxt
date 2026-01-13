<template>
  <div class="stats-view">
    <!-- 统计卡片 -->
    <el-row :gutter="20" style="margin-bottom: 20px;">
      <el-col :span="6">
        <div class="stat-card">
          <div class="stat-icon primary">
            <el-icon :size="24"><Document /></el-icon>
          </div>
          <div class="stat-value">{{ stats.total_documents || 0 }}</div>
          <div class="stat-label">文档总数</div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card">
          <div class="stat-icon success">
            <el-icon :size="24"><CircleCheck /></el-icon>
          </div>
          <div class="stat-value">{{ healthStatus.status === 'healthy' ? '正常' : '异常' }}</div>
          <div class="stat-label">服务状态</div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card">
          <div class="stat-icon warning">
            <el-icon :size="24"><Timer /></el-icon>
          </div>
          <div class="stat-value">{{ formatUptime(stats.uptime) }}</div>
          <div class="stat-label">运行时间</div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card">
          <div class="stat-icon info">
            <el-icon :size="24"><Cpu /></el-icon>
          </div>
          <div class="stat-value">{{ healthStatus.gpu_available ? 'GPU' : 'CPU' }}</div>
          <div class="stat-label">计算设备</div>
        </div>
      </el-col>
    </el-row>

    <!-- 服务详情 -->
    <div class="card">
      <div class="card-header">
        <div class="card-title">
          <el-icon><Monitor /></el-icon>
          服务详情
        </div>
        <el-button size="small" @click="refreshStats">
          <el-icon><Refresh /></el-icon>
          刷新
        </el-button>
      </div>

      <el-descriptions :column="2" border>
        <el-descriptions-item label="服务状态">
          <el-tag :type="healthStatus.status === 'healthy' ? 'success' : 'danger'">
            {{ healthStatus.status }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="模型加载">
          <el-tag :type="healthStatus.model_loaded ? 'success' : 'danger'">
            {{ healthStatus.model_loaded ? '已加载' : '未加载' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="数据库连接">
          <el-tag :type="healthStatus.database_connected ? 'success' : 'danger'">
            {{ healthStatus.database_connected ? '已连接' : '未连接' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="缓存连接">
          <el-tag :type="healthStatus.cache_connected ? 'success' : 'warning'">
            {{ healthStatus.cache_connected ? '已连接' : '未连接' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="GPU可用">
          <el-tag :type="healthStatus.gpu_available ? 'success' : 'info'">
            {{ healthStatus.gpu_available ? '是' : '否' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="文档总数">
          {{ healthStatus.total_documents || 0 }}
        </el-descriptions-item>
      </el-descriptions>
    </div>

    <!-- GPU内存 -->
    <div class="card" v-if="healthStatus.memory_usage">
      <div class="card-header">
        <div class="card-title">
          <el-icon><Cpu /></el-icon>
          GPU内存使用
        </div>
      </div>
      <el-descriptions :column="2" border>
        <el-descriptions-item label="已分配">
          {{ healthStatus.memory_usage?.gpu_memory_allocated || '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="已缓存">
          {{ healthStatus.memory_usage?.gpu_memory_cached || '-' }}
        </el-descriptions-item>
      </el-descriptions>
    </div>

    <!-- 配置信息 -->
    <div class="card">
      <div class="card-header">
        <div class="card-title">
          <el-icon><Setting /></el-icon>
          系统配置
        </div>
      </div>
      <el-descriptions :column="2" border>
        <el-descriptions-item label="最大返回数">
          {{ stats.config?.max_top_k || 20 }}
        </el-descriptions-item>
        <el-descriptions-item label="缓存TTL">
          {{ stats.config?.cache_ttl || 1800 }}秒
        </el-descriptions-item>
        <el-descriptions-item label="缓存启用">
          <el-tag :type="stats.config?.enable_cache ? 'success' : 'info'">
            {{ stats.config?.enable_cache ? '是' : '否' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="API端口">8001</el-descriptions-item>
      </el-descriptions>
    </div>

    <!-- 操作区 -->
    <div class="card">
      <div class="card-header">
        <div class="card-title">
          <el-icon><Operation /></el-icon>
          系统操作
        </div>
      </div>
      <el-space>
        <el-button type="warning" @click="handleClearCache" :loading="clearing">
          <el-icon><Delete /></el-icon>
          清理缓存
        </el-button>
      </el-space>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRagStore } from '@/stores/rag'
import { ElMessage } from 'element-plus'

const ragStore = useRagStore()
const clearing = ref(false)

const healthStatus = computed(() => ragStore.healthStatus)
const stats = computed(() => ragStore.stats)

onMounted(() => {
  refreshStats()
})

const refreshStats = async () => {
  await Promise.all([
    ragStore.fetchHealth(),
    ragStore.fetchStats()
  ])
}

const handleClearCache = async () => {
  try {
    clearing.value = true
    const result = await ragStore.clearCache()
    ElMessage.success(result.message || '缓存已清理')
  } catch (error) {
    ElMessage.error('清理失败: ' + error.message)
  } finally {
    clearing.value = false
  }
}

const formatUptime = (seconds) => {
  if (!seconds) return '-'
  const h = Math.floor(seconds / 3600)
  const m = Math.floor((seconds % 3600) / 60)
  const s = Math.floor(seconds % 60)
  if (h > 0) return `${h}h ${m}m`
  if (m > 0) return `${m}m ${s}s`
  return `${s}s`
}
</script>
