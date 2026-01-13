<template>
  <div class="document-detail">
    <!-- 返回按钮和标题 -->
    <div class="page-header">
      <el-button @click="$router.push('/documents')" text>
        <el-icon><ArrowLeft /></el-icon> 返回文档列表
      </el-button>
      <h2>{{ docInfo.name || '文档详情' }}</h2>
    </div>

    <!-- 文档信息卡片 -->
    <div class="card">
      <div class="card-header">
        <div class="card-title">
          <el-icon><Document /></el-icon>
          文档信息
        </div>
        <el-button type="danger" size="small" @click="handleDeleteDoc">
          <el-icon><Delete /></el-icon> 删除文档
        </el-button>
      </div>
      <el-descriptions :column="4" border>
        <el-descriptions-item label="文档名称">{{ docInfo.name }}</el-descriptions-item>
        <el-descriptions-item label="类型">{{ docInfo.type?.toUpperCase() }}</el-descriptions-item>
        <el-descriptions-item label="知识条目数">{{ total }}</el-descriptions-item>
        <el-descriptions-item label="导入时间">{{ docInfo.created_at }}</el-descriptions-item>
      </el-descriptions>
    </div>

    <!-- 知识条目列表 -->
    <div class="card">
      <div class="card-header">
        <div class="card-title">
          <el-icon><List /></el-icon>
          知识条目
        </div>
        <el-button type="primary" size="small" @click="showAddDialog">
          <el-icon><Plus /></el-icon> 添加条目
        </el-button>
      </div>

      <!-- 条目列表 -->
      <el-table :data="items" v-loading="loading" style="width: 100%">
        <el-table-column prop="chunk_index" label="#" width="60" />
        <el-table-column prop="content" label="内容" min-width="300">
          <template #default="{ row }">
            <div class="content-cell">
              {{ truncate(row.content, 150) }}
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="question" label="相关问题" width="200">
          <template #default="{ row }">
            <span class="question-text">{{ row.question || '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="category" label="分类" width="100">
          <template #default="{ row }">
            <el-tag size="small">{{ row.category || '其他' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" text @click="handleEdit(row)">
              <el-icon><Edit /></el-icon>
            </el-button>
            <el-button type="danger" size="small" text @click="handleDelete(row)">
              <el-icon><Delete /></el-icon>
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="page"
          v-model:page-size="pageSize"
          :total="total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="fetchItems"
          @current-change="fetchItems"
        />
      </div>
    </div>

    <!-- 编辑/添加弹窗 -->
    <el-dialog 
      v-model="dialogVisible" 
      :title="isEdit ? '编辑知识条目' : '添加知识条目'"
      width="600px"
    >
      <el-form :model="form" label-width="80px">
        <el-form-item label="内容" required>
          <el-input 
            v-model="form.content" 
            type="textarea" 
            :rows="6"
            placeholder="输入知识内容"
          />
        </el-form-item>
        <el-form-item label="相关问题">
          <el-input v-model="form.question" placeholder="可选，输入相关问题" />
        </el-form-item>
        <el-form-item label="分类">
          <el-input v-model="form.category" placeholder="默认：其他" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSave" :loading="saving">
          保存
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import * as ragApi from '@/api/rag'

const route = useRoute()
const router = useRouter()

const docId = route.params.id
const docInfo = ref({})
const items = ref([])
const loading = ref(false)
const saving = ref(false)

const page = ref(1)
const pageSize = ref(20)
const total = ref(0)

const dialogVisible = ref(false)
const isEdit = ref(false)
const currentItemId = ref(null)
const form = reactive({
  content: '',
  question: '',
  category: ''
})

onMounted(() => {
  fetchDocInfo()
  fetchItems()
})

const fetchDocInfo = async () => {
  try {
    const { data } = await ragApi.getDocuments()
    if (data.success) {
      docInfo.value = data.documents.find(d => d.id === docId) || { name: '未知文档', id: docId }
    }
  } catch (error) {
    console.error('获取文档信息失败:', error)
  }
}

const fetchItems = async () => {
  try {
    loading.value = true
    const { data } = await ragApi.getDocumentItems(docId, page.value, pageSize.value)
    if (data.success) {
      items.value = data.items
      total.value = data.total
    }
  } catch (error) {
    ElMessage.error('获取知识条目失败')
  } finally {
    loading.value = false
  }
}

const truncate = (text, len) => {
  if (!text) return ''
  return text.length > len ? text.slice(0, len) + '...' : text
}

const showAddDialog = () => {
  isEdit.value = false
  currentItemId.value = null
  form.content = ''
  form.question = ''
  form.category = ''
  dialogVisible.value = true
}

const handleEdit = (row) => {
  isEdit.value = true
  currentItemId.value = row.id
  form.content = row.content
  form.question = row.question || ''
  form.category = row.category || ''
  dialogVisible.value = true
}

const handleSave = async () => {
  if (!form.content.trim()) {
    ElMessage.warning('请输入内容')
    return
  }

  try {
    saving.value = true
    if (isEdit.value) {
      await ragApi.updateKnowledgeItem(currentItemId.value, form)
      ElMessage.success('更新成功')
    } else {
      await ragApi.createKnowledgeItem({ ...form, doc_id: docId })
      ElMessage.success('添加成功')
    }
    dialogVisible.value = false
    fetchItems()
  } catch (error) {
    ElMessage.error('保存失败')
  } finally {
    saving.value = false
  }
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm('确定删除该知识条目吗？', '确认删除', { type: 'warning' })
    await ragApi.deleteKnowledgeItem(row.id)
    ElMessage.success('删除成功')
    fetchItems()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

const handleDeleteDoc = async () => {
  try {
    await ElMessageBox.confirm(
      `确定删除文档 "${docInfo.value.name}" 及其所有知识条目吗？此操作不可恢复！`,
      '确认删除',
      { type: 'warning' }
    )
    await ragApi.deleteDocumentWithItems(docId)
    ElMessage.success('文档已删除')
    router.push('/documents')
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}
</script>

<style scoped>
.page-header {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 20px;
}

.page-header h2 {
  margin: 0;
  font-size: 18px;
  color: var(--text-primary);
}

.content-cell {
  line-height: 1.5;
  color: var(--text-secondary);
  font-size: 13px;
}

.question-text {
  color: var(--primary);
  font-size: 13px;
}

.pagination-wrapper {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style>
