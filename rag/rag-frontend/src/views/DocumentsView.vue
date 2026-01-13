<template>
  <div class="documents-view">
    <!-- 上传区域 -->
    <div class="card">
      <div class="card-header">
        <div class="card-title">
          <el-icon><Upload /></el-icon>
          文档上传
        </div>
      </div>
      
      <el-upload
        class="upload-area"
        drag
        action="/api/documents/upload"
        :on-success="handleUploadSuccess"
        :on-error="handleUploadError"
        :before-upload="beforeUpload"
        accept=".pdf,.txt,.json,.csv"
      >
        <el-icon class="upload-icon"><UploadFilled /></el-icon>
        <div class="upload-text">
          <p>将文件拖到此处，或<em>点击上传</em></p>
          <p style="font-size: 12px; color: var(--text-muted); margin-top: 8px;">
            支持 PDF、TXT、JSON、CSV 格式
          </p>
        </div>
      </el-upload>
    </div>

    <!-- 文档列表 -->
    <div class="card">
      <div class="card-header">
        <div class="card-title">
          <el-icon><Folder /></el-icon>
          已导入文档
        </div>
        <el-button type="primary" size="small" @click="handleRebuildIndex" :loading="rebuilding">
          <el-icon><Refresh /></el-icon>
          重建索引
        </el-button>
      </div>

      <el-table :data="documents" v-loading="ragStore.loading" style="width: 100%">
        <el-table-column prop="name" label="文档名称" min-width="200">
          <template #default="{ row }">
            <div 
              class="doc-name-cell"
              @click="goToDetail(row)"
            >
              <el-icon :size="18" :color="getFileIconColor(row.type)">
                <Document />
              </el-icon>
              <span>{{ row.name }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="type" label="类型" width="100">
          <template #default="{ row }">
            <el-tag size="small">{{ row.type?.toUpperCase() }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="chunks" label="知识条目" width="100" />
        <el-table-column prop="size" label="大小" width="100">
          <template #default="{ row }">
            {{ formatSize(row.size) }}
          </template>
        </el-table-column>
        <el-table-column prop="created_at" label="导入时间" width="180" />
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" text @click="goToDetail(row)">
              <el-icon><View /></el-icon>
            </el-button>
            <el-button type="danger" size="small" text @click="handleDelete(row)">
              <el-icon><Delete /></el-icon>
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div v-if="!documents.length && !ragStore.loading" class="empty-state">
        <el-icon class="empty-icon"><FolderOpened /></el-icon>
        <p>暂无文档，请上传文档以构建知识库</p>
      </div>
    </div>

    <!-- 处理说明 -->
    <div class="card">
      <div class="card-header">
        <div class="card-title">
          <el-icon><InfoFilled /></el-icon>
          文档处理说明
        </div>
      </div>
      <el-descriptions :column="1" border>
        <el-descriptions-item label="分块大小">250 字符</el-descriptions-item>
        <el-descriptions-item label="重叠大小">50 字符</el-descriptions-item>
        <el-descriptions-item label="向量模型">BGE-Large-ZH</el-descriptions-item>
        <el-descriptions-item label="向量维度">1024</el-descriptions-item>
        <el-descriptions-item label="向量数据库">ChromaDB</el-descriptions-item>
      </el-descriptions>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useRagStore } from '@/stores/rag'
import { ElMessage, ElMessageBox } from 'element-plus'
import * as ragApi from '@/api/rag'

const router = useRouter()
const ragStore = useRagStore()
const rebuilding = ref(false)

// 模拟文档数据（实际从后端获取）
const documents = computed(() => ragStore.documents)

onMounted(() => {
  ragStore.fetchDocuments()
})

const goToDetail = (row) => {
  router.push(`/documents/${row.id}`)
}

const beforeUpload = (file) => {
  const allowedTypes = ['application/pdf', 'text/plain', 'application/json', 'text/csv']
  const isAllowed = allowedTypes.includes(file.type) || 
    file.name.endsWith('.pdf') || 
    file.name.endsWith('.txt') ||
    file.name.endsWith('.json') ||
    file.name.endsWith('.csv')
  
  if (!isAllowed) {
    ElMessage.error('只支持 PDF、TXT、JSON、CSV 格式')
    return false
  }
  
  const isLt50M = file.size / 1024 / 1024 < 50
  if (!isLt50M) {
    ElMessage.error('文件大小不能超过 50MB')
    return false
  }
  
  return true
}

const handleUploadSuccess = (response) => {
  ElMessage.success('文档上传成功')
  ragStore.fetchDocuments()
}

const handleUploadError = (error) => {
  ElMessage.error('上传失败: ' + error.message)
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(`确定删除文档 "${row.name}" 吗？`, '确认删除', {
      type: 'warning'
    })
    await ragApi.deleteDocument(row.id)
    ElMessage.success('删除成功')
    ragStore.fetchDocuments()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

const handleRebuildIndex = async () => {
  try {
    await ElMessageBox.confirm('重建索引将重新处理所有文档，可能需要较长时间，确定继续吗？', '确认重建', {
      type: 'warning'
    })
    rebuilding.value = true
    await ragApi.rebuildIndex()
    ElMessage.success('索引重建完成')
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('重建失败: ' + error.message)
    }
  } finally {
    rebuilding.value = false
  }
}

const getFileIconColor = (type) => {
  const colors = {
    pdf: '#E53935',
    txt: '#43A047',
    json: '#FB8C00',
    csv: '#1E88E5'
  }
  return colors[type] || '#757575'
}

const formatSize = (bytes) => {
  if (!bytes) return '-'
  if (bytes < 1024) return bytes + ' B'
  if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(1) + ' KB'
  return (bytes / 1024 / 1024).toFixed(1) + ' MB'
}
</script>

<style scoped>
.upload-area {
  width: 100%;
}

.upload-area :deep(.el-upload-dragger) {
  border: 2px dashed var(--border);
  border-radius: 12px;
  padding: 40px;
  transition: all 0.2s ease;
}

.upload-area :deep(.el-upload-dragger:hover) {
  border-color: var(--primary);
  background: rgba(8, 145, 178, 0.02);
}

.upload-icon {
  font-size: 48px;
  color: var(--primary-light);
  margin-bottom: 16px;
}

.upload-text em {
  color: var(--primary);
  font-style: normal;
}

.doc-name-cell {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  color: var(--primary);
}

.doc-name-cell:hover {
  text-decoration: underline;
}
</style>