<template>
  <div class="weight-record-page">
    <!-- 页面标题 -->
    <div class="page-header">
      <div class="header-content">
        <div class="header-icon">
          <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke-width="1.5" stroke="currentColor" class="icon">
            <path stroke-linecap="round" stroke-linejoin="round" d="M12 3v17.25m0 0c-1.472 0-2.882.265-4.185.75M12 20.25c1.472 0 2.882.265 4.185.75M18.75 4.97A48.416 48.416 0 0 0 12 4.5c-2.291 0-4.545.16-6.75.47m13.5 0c1.01.143 2.01.317 3 .52m-3-.52 2.62 10.726c.122.499-.106 1.028-.589 1.202a5.988 5.988 0 0 1-2.031.352 5.988 5.988 0 0 1-2.031-.352c-.483-.174-.711-.703-.59-1.202L18.75 4.971Zm-16.5.52c.99-.203 1.99-.377 3-.52m0 0 2.62 10.726c.122.499-.106 1.028-.589 1.202a5.989 5.989 0 0 1-2.031.352 5.989 5.989 0 0 1-2.031-.352c-.483-.174-.711-.703-.59-1.202L5.25 4.971Z" />
          </svg>
        </div>
        <div class="header-text">
          <h1 class="page-title">体重记录管理</h1>
          <p class="page-subtitle">追踪体重变化，科学管理身材</p>
        </div>
      </div>
      <div class="header-actions">
        <el-button type="primary" size="large" class="add-btn" @click="showAddDialog">
          <el-icon><Plus /></el-icon>
          添加体重记录
        </el-button>
      </div>
    </div>

    <!-- 统计卡片 -->
    <el-row :gutter="16" class="stats-section">
      <el-col :xs="24" :sm="12" :md="6">
        <div class="stat-card weight-card">
          <div class="stat-icon">
            <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke-width="1.5" stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" d="M12 3v17.25m0 0c-1.472 0-2.882.265-4.185.75M12 20.25c1.472 0 2.882.265 4.185.75M18.75 4.97A48.416 48.416 0 0 0 12 4.5c-2.291 0-4.545.16-6.75.47m13.5 0c1.01.143 2.01.317 3 .52m-3-.52 2.62 10.726c.122.499-.106 1.028-.589 1.202a5.988 5.988 0 0 1-2.031.352 5.988 5.988 0 0 1-2.031-.352c-.483-.174-.711-.703-.59-1.202L18.75 4.971Zm-16.5.52c.99-.203 1.99-.377 3-.52m0 0 2.62 10.726c.122.499-.106 1.028-.589 1.202a5.989 5.989 0 0 1-2.031.352 5.989 5.989 0 0 1-2.031-.352c-.483-.174-.711-.703-.59-1.202L5.25 4.971Z" />
            </svg>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ statistics.avgWeight || '--' }}</div>
            <div class="stat-label">平均体重 (kg)</div>
          </div>
        </div>
      </el-col>
      <el-col :xs="24" :sm="12" :md="6">
        <div class="stat-card bmi-card">
          <div class="stat-icon">
            <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke-width="1.5" stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" d="M3 13.125C3 12.504 3.504 12 4.125 12h2.25c.621 0 1.125.504 1.125 1.125v6.75C7.5 20.496 6.996 21 6.375 21h-2.25A1.125 1.125 0 0 1 3 19.875v-6.75ZM9.75 8.625c0-.621.504-1.125 1.125-1.125h2.25c.621 0 1.125.504 1.125 1.125v11.25c0 .621-.504 1.125-1.125 1.125h-2.25a1.125 1.125 0 0 1-1.125-1.125V8.625ZM16.5 4.125c0-.621.504-1.125 1.125-1.125h2.25C20.496 3 21 3.504 21 4.125v15.75c0 .621-.504 1.125-1.125 1.125h-2.25a1.125 1.125 0 0 1-1.125-1.125V4.125Z" />
            </svg>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ statistics.currentBmi || '--' }}</div>
            <div class="stat-label">当前BMI</div>
          </div>
        </div>
      </el-col>
      <el-col :xs="24" :sm="12" :md="6">
        <div class="stat-card trend-card">
          <div class="stat-icon">
            <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke-width="1.5" stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" d="M2.25 18 9 11.25l4.306 4.306a11.95 11.95 0 0 1 5.814-5.518l2.74-1.22m0 0-5.94-2.281m5.94 2.28-2.28 5.941" />
            </svg>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ formatWeightChange(statistics.weightChange30Days) }}</div>
            <div class="stat-label">30天变化 (kg)</div>
          </div>
        </div>
      </el-col>
      <el-col :xs="24" :sm="12" :md="6">
        <div class="stat-card target-card">
          <div class="stat-icon">
            <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke-width="1.5" stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" d="M9 12.75 11.25 15 15 9.75M21 12c0 1.268-.63 2.39-1.593 3.068a3.745 3.745 0 0 1-1.043 3.296 3.745 3.745 0 0 1-3.296 1.043A3.745 3.745 0 0 1 12 21c-1.268 0-2.39-.63-3.068-1.593a3.746 3.746 0 0 1-3.296-1.043 3.745 3.745 0 0 1-1.043-3.296A3.745 3.745 0 0 1 3 12c0-1.268.63-2.39 1.593-3.068a3.745 3.745 0 0 1 1.043-3.296 3.746 3.746 0 0 1 3.296-1.043A3.746 3.746 0 0 1 12 3c1.268 0 2.39.63 3.068 1.593a3.746 3.746 0 0 1 3.296 1.043 3.746 3.746 0 0 1 1.043 3.296A3.745 3.745 0 0 1 21 12Z" />
            </svg>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ statistics.targetWeight || '--' }}</div>
            <div class="stat-label">目标体重 (kg)</div>
          </div>
        </div>
      </el-col>
    </el-row>

    <!-- 主要内容 -->
    <el-card class="main-card">
      <template #header>
        <div class="card-header">
          <div class="header-left">
            <span class="title">体重记录</span>
            <el-tag v-if="selectedRecords.length > 0" type="info" class="selection-tag">已选择 {{ selectedRecords.length }} 条记录</el-tag>
          </div>
          <div class="header-actions">
            <el-button v-if="selectedRecords.length > 0" type="danger" size="small" :disabled="loading" @click="batchDeleteRecords">
              <el-icon><Delete /></el-icon>批量删除
            </el-button>
            <el-button type="primary" size="small" class="add-record-btn" @click="showAddDialog">
              <el-icon><Plus /></el-icon>添加记录
            </el-button>
          </div>
        </div>
      </template>

      <!-- 筛选区域 -->
      <div class="filter-section">
        <el-row :gutter="16">
          <el-col :xs="24" :sm="8" :md="6">
            <el-select v-model="filters.measureState" placeholder="测量状态" clearable @change="searchRecords">
              <el-option label="晨起" value="morning" />
              <el-option label="晚间" value="evening" />
              <el-option label="餐后" value="after_meal" />
              <el-option label="餐前" value="before_meal" />
            </el-select>
          </el-col>
          <el-col :xs="24" :sm="8" :md="8">
            <el-date-picker v-model="filters.dateRange" type="datetimerange" range-separator="至" start-placeholder="开始时间" end-placeholder="结束时间" format="YYYY-MM-DD HH:mm" value-format="YYYY-MM-DD HH:mm:ss" @change="searchRecords" />
          </el-col>
          <el-col :xs="24" :sm="8" :md="4">
            <div class="filter-actions">
              <el-button @click="searchRecords" :loading="loading" class="search-btn">搜索</el-button>
              <el-button @click="resetFilters">重置</el-button>
            </div>
          </el-col>
        </el-row>
      </div>

      <!-- 体重记录表格 -->
      <el-table :data="records" v-loading="loading" empty-text="暂无体重记录" style="width: 100%" @selection-change="handleSelectionChange" class="data-table">
        <el-table-column type="selection" width="55" />
        <el-table-column prop="weight" label="体重" width="120">
          <template #default="{ row }">
            <div class="weight-value"><span class="value">{{ row.weight }}</span><span class="unit">kg</span></div>
          </template>
        </el-table-column>
        <el-table-column prop="bmi" label="BMI" width="100">
          <template #default="{ row }">
            <div v-if="row.bmi" class="bmi-info">
              <div class="bmi-value">{{ row.bmi }}</div>
              <el-tag :type="getBmiTagType(row.bmi)" size="small">{{ getBmiLabel(row.bmi) }}</el-tag>
            </div>
            <span v-else class="text-gray">--</span>
          </template>
        </el-table-column>
        <el-table-column prop="bodyFatRate" label="体脂率" width="100">
          <template #default="{ row }">
            <span v-if="row.bodyFatRate">{{ row.bodyFatRate }}%</span>
            <span v-else class="text-gray">--</span>
          </template>
        </el-table-column>
        <el-table-column prop="height" label="身高" width="100">
          <template #default="{ row }">
            <span v-if="row.height">{{ row.height }}cm</span>
            <span v-else class="text-gray">--</span>
          </template>
        </el-table-column>
        <el-table-column prop="measureState" label="测量状态" width="120">
          <template #default="{ row }">
            <span v-if="row.measureState">{{ getMeasureStateLabel(row.measureState) }}</span>
            <span v-else class="text-gray">--</span>
          </template>
        </el-table-column>
        <el-table-column prop="measureTime" label="测量时间" width="180">
          <template #default="{ row }">
            <div class="time-info">
              <div>{{ dayjs(row.measureTime).format('MM-DD HH:mm') }}</div>
              <div class="time-detail">{{ dayjs(row.measureTime).format('YYYY年') }}</div>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="note" label="备注" min-width="150" show-overflow-tooltip>
          <template #default="{ row }">
            <span v-if="row.note">{{ row.note }}</span>
            <span v-else class="text-gray">--</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button text type="primary" size="small" @click="editRecord(row)">编辑</el-button>
            <el-button text type="danger" size="small" @click="deleteRecord(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-container">
        <el-pagination v-model:current-page="pagination.current" v-model:page-size="pagination.size" :total="pagination.total" :page-sizes="[10, 20, 50, 100]" layout="total, sizes, prev, pager, next, jumper" @size-change="handleSizeChange" @current-change="handleCurrentChange" />
      </div>
    </el-card>

    <!-- 添加/编辑体重记录对话框 -->
    <el-dialog v-model="dialogVisible" :title="isEditing ? '编辑体重记录' : '添加体重记录'" width="500px" @close="resetForm" class="record-dialog">
      <el-form ref="recordFormRef" :model="recordForm" :rules="recordRules" label-width="100px">
        <el-form-item label="体重" prop="weight">
          <el-input-number v-model="recordForm.weight" :min="1" :max="500" :step="0.1" :precision="1" placeholder="请输入体重" style="width: 100%" />
          <span class="input-suffix">kg</span>
        </el-form-item>
        <el-form-item label="身高">
          <el-input-number v-model="recordForm.height" :min="50" :max="250" :step="0.1" :precision="1" placeholder="身高（可选，用于计算BMI）" style="width: 100%" />
          <span class="input-suffix">cm</span>
        </el-form-item>
        <el-form-item label="体脂率">
          <el-input-number v-model="recordForm.bodyFatRate" :min="0" :max="100" :step="0.1" :precision="1" placeholder="体脂率（可选）" style="width: 100%" />
          <span class="input-suffix">%</span>
        </el-form-item>
        <el-form-item label="测量状态">
          <el-select v-model="recordForm.measureState" placeholder="请选择测量状态（可选）" clearable style="width: 100%">
            <el-option label="晨起" value="morning" />
            <el-option label="晚间" value="evening" />
            <el-option label="餐后" value="after_meal" />
            <el-option label="餐前" value="before_meal" />
          </el-select>
        </el-form-item>
        <el-form-item label="测量时间" prop="measureTime">
          <el-date-picker v-model="recordForm.measureTime" type="datetime" placeholder="请选择测量时间" format="YYYY-MM-DD HH:mm" value-format="YYYY-MM-DD HH:mm:ss" style="width: 100%" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="recordForm.note" type="textarea" :rows="3" placeholder="请输入备注（可选）" maxlength="200" show-word-limit />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveRecord" :loading="saving" class="save-btn">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Delete } from '@element-plus/icons-vue'
import dayjs from 'dayjs'
import * as healthApi from '@/api/health'

const loading = ref(false)
const saving = ref(false)
const dialogVisible = ref(false)
const isEditing = ref(false)
const recordFormRef = ref()
const selectedRecords = ref([])

const records = ref([])
const statistics = ref({})
const filters = reactive({ measureState: '', dateRange: [] })
const pagination = reactive({ current: 1, size: 20, total: 0 })
const recordForm = reactive({ id: null, weight: null, height: null, bodyFatRate: null, measureState: '', measureTime: new Date(), note: '' })

const recordRules = {
  weight: [{ required: true, message: '请输入体重', trigger: 'blur' }, { type: 'number', min: 1, max: 500, message: '体重应在1-500kg之间', trigger: 'blur' }],
  measureTime: [{ required: true, message: '请选择测量时间', trigger: 'change' }]
}

const getBmiTagType = (bmi) => { if (bmi < 18.5) return 'warning'; if (bmi < 24) return 'success'; if (bmi < 28) return 'warning'; return 'danger' }
const getBmiLabel = (bmi) => { if (bmi < 18.5) return '偏瘦'; if (bmi < 24) return '正常'; if (bmi < 28) return '超重'; return '肥胖' }
const getMeasureStateLabel = (state) => ({ morning: '晨起', evening: '晚间', after_meal: '餐后', before_meal: '餐前' }[state] || state)
const formatWeightChange = (change) => { if (!change) return '--'; const sign = change > 0 ? '+' : ''; return `${sign}${change}` }
const handleSelectionChange = (selection) => { selectedRecords.value = selection }

const batchDeleteRecords = async () => {
  try {
    await ElMessageBox.confirm(`确定要删除选中的 ${selectedRecords.value.length} 条体重记录吗？`, '批量删除', { confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning' })
    const recordIds = selectedRecords.value.map(r => r.id)
    await healthApi.batchDeleteWeightRecords(recordIds)
    ElMessage.success('批量删除成功')
    selectedRecords.value = []
    await loadRecords()
    await loadStatistics()
  } catch (error) {
    if (error !== 'cancel') { console.error('批量删除失败:', error); ElMessage.error('批量删除失败') }
  }
}

const showAddDialog = () => { isEditing.value = false; dialogVisible.value = true; resetForm() }

const editRecord = (row) => {
  isEditing.value = true
  dialogVisible.value = true
  Object.assign(recordForm, { id: row.id, weight: row.weight, height: row.height, bodyFatRate: row.bodyFatRate, measureState: row.measureState, measureTime: row.measureTime, note: row.note || '' })
}

const deleteRecord = async (row) => {
  try {
    await ElMessageBox.confirm('确定要删除这条体重记录吗？', '提示', { confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning' })
    await healthApi.deleteWeightRecord(row.id)
    ElMessage.success('删除成功')
    await loadRecords()
    await loadStatistics()
  } catch (error) {
    if (error !== 'cancel') { console.error('删除失败:', error); ElMessage.error('删除失败') }
  }
}

const saveRecord = async () => {
  if (saving.value) return
  try {
    const valid = await recordFormRef.value.validate()
    if (!valid) return
    saving.value = true
    const data = { weight: recordForm.weight, height: recordForm.height, bodyFatRate: recordForm.bodyFatRate, measureState: recordForm.measureState, measureTime: recordForm.measureTime, note: recordForm.note }
    if (isEditing.value) { await healthApi.updateWeightRecord(recordForm.id, data) }
    else { await healthApi.addWeightRecord(data) }
    ElMessage.success(isEditing.value ? '体重记录已更新' : '体重记录已添加')
    dialogVisible.value = false
    await loadRecords()
    await loadStatistics()
  } catch (error) { console.error('保存失败:', error); ElMessage.error('保存失败') }
  finally { saving.value = false }
}

const resetForm = () => {
  if (recordFormRef.value) recordFormRef.value.clearValidate()
  Object.assign(recordForm, { id: null, weight: null, height: null, bodyFatRate: null, measureState: '', measureTime: new Date(), note: '' })
}

const searchRecords = () => { pagination.current = 1; loadRecords() }
const resetFilters = () => { Object.assign(filters, { measureState: '', dateRange: [] }); searchRecords() }
const handleSizeChange = (size) => { pagination.size = size; loadRecords() }
const handleCurrentChange = (current) => { pagination.current = current; loadRecords() }

const loadRecords = async () => {
  try {
    loading.value = true
    const params = { current: pagination.current, size: pagination.size }
    if (filters.measureState) params.measureState = filters.measureState
    if (filters.dateRange && filters.dateRange.length === 2) { params.startTime = filters.dateRange[0]; params.endTime = filters.dateRange[1] }
    const response = await healthApi.getWeightRecords(params)
    if (response.data && response.data.records) {
      records.value = response.data.records
      pagination.total = response.data.records.length
      if (response.data.statistics) statistics.value = response.data.statistics
    } else { records.value = []; pagination.total = 0 }
  } catch (error) { console.error('加载体重记录失败:', error); ElMessage.error('加载体重记录失败'); records.value = []; pagination.total = 0 }
  finally { loading.value = false }
}

const loadStatistics = async () => {
  try {
    const response = await healthApi.getWeightRecords({ current: 1, size: 1000 })
    if (response.data && response.data.statistics) statistics.value = response.data.statistics
    else statistics.value = { avgWeight: 0, currentBmi: 0, weightChange30Days: 0, targetWeight: 0 }
  } catch (error) { console.error('加载统计信息失败:', error); statistics.value = { avgWeight: 0, currentBmi: 0, weightChange30Days: 0, targetWeight: 0 } }
}

onMounted(() => { loadRecords(); loadStatistics() })
</script>

<style scoped>
.weight-record-page { padding: 0; }

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  padding: 24px;
  background: linear-gradient(135deg, #ECFEFF 0%, #CFFAFE 100%);
  border-radius: 16px;
  border: 1px solid #A5F3FC;
}

.header-content { display: flex; align-items: center; gap: 16px; }

.header-icon {
  width: 56px;
  height: 56px;
  background: linear-gradient(135deg, #8B5CF6 0%, #A78BFA 100%);
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.header-icon .icon { width: 32px; height: 32px; color: white; }
.page-title { font-size: 24px; font-weight: 700; color: #164E63; margin: 0 0 4px 0; }
.page-subtitle { font-size: 14px; color: #0891B2; margin: 0; }

.add-btn {
  background: linear-gradient(135deg, #0891B2 0%, #22D3EE 100%);
  border: none;
  border-radius: 8px;
  padding: 12px 24px;
}

.add-btn:hover { background: linear-gradient(135deg, #0E7490 0%, #06B6D4 100%); }

.stats-section { margin-bottom: 16px; }

.stat-card {
  background: white;
  border-radius: 16px;
  padding: 20px;
  display: flex;
  align-items: center;
  cursor: pointer;
  transition: all 0.3s ease;
  border: 1px solid #E0F2FE;
}

.stat-card:hover { transform: translateY(-2px); box-shadow: 0 8px 20px rgba(8, 145, 178, 0.15); }

.stat-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 16px;
}

.stat-icon svg { width: 24px; height: 24px; color: white; }

.weight-card .stat-icon { background: linear-gradient(135deg, #8B5CF6 0%, #A78BFA 100%); }
.bmi-card .stat-icon { background: linear-gradient(135deg, #059669 0%, #34D399 100%); }
.trend-card .stat-icon { background: linear-gradient(135deg, #0891B2 0%, #22D3EE 100%); }
.target-card .stat-icon { background: linear-gradient(135deg, #F59E0B 0%, #FBBF24 100%); }

.stat-info { flex: 1; }
.stat-value { font-size: 24px; font-weight: bold; color: #164E63; line-height: 1; }
.stat-label { font-size: 14px; color: #0891B2; margin-top: 4px; }

.main-card { margin-top: 0; border-radius: 16px; border: 1px solid #E0F2FE; }
.card-header { display: flex; justify-content: space-between; align-items: center; width: 100%; }
.header-left { display: flex; align-items: center; gap: 12px; }
.title { font-size: 16px; font-weight: 600; color: #164E63; }
.selection-tag { background: #ECFEFF; color: #0891B2; border: 1px solid #A5F3FC; font-size: 12px; }
.header-actions { display: flex; gap: 8px; }

.add-record-btn { background: #0891B2; border-color: #0891B2; }
.add-record-btn:hover { background: #0E7490; border-color: #0E7490; }

.filter-section { background: #F0FDFA; padding: 16px; border-radius: 12px; margin-bottom: 16px; border: 1px solid #A5F3FC; }
.filter-actions { display: flex; gap: 8px; }
.search-btn { background: #0891B2; border-color: #0891B2; color: white; }
.search-btn:hover { background: #0E7490; border-color: #0E7490; }

.data-table :deep(.el-table__header th) { background: #F0FDFA; color: #164E63; }

.weight-value { display: flex; align-items: baseline; gap: 4px; }
.value { font-size: 16px; font-weight: 600; color: #0891B2; }
.unit { font-size: 12px; color: #67E8F9; }
.bmi-info { line-height: 1.2; }
.bmi-value { font-weight: 600; color: #164E63; margin-bottom: 4px; }
.time-info { line-height: 1.2; }
.time-detail { font-size: 12px; color: #67E8F9; margin-top: 2px; }
.text-gray { color: #94A3B8; }
.input-suffix { margin-left: 8px; color: #67E8F9; font-size: 14px; }
.pagination-container { display: flex; justify-content: center; margin-top: 20px; }

.record-dialog :deep(.el-dialog__header) { background: linear-gradient(135deg, #ECFEFF 0%, #CFFAFE 100%); border-radius: 8px 8px 0 0; }
.record-dialog :deep(.el-dialog__title) { color: #164E63; font-weight: 600; }
.save-btn { background: #0891B2; border-color: #0891B2; }
.save-btn:hover { background: #0E7490; border-color: #0E7490; }

@media (max-width: 768px) {
  .page-header { flex-direction: column; align-items: flex-start; gap: 16px; }
  .page-title { font-size: 20px; }
  .stats-section :deep(.el-col) { margin-bottom: 8px; }
  .filter-section { padding: 12px; }
  .filter-actions { justify-content: flex-start; margin-top: 8px; }
  .card-header { flex-direction: column; align-items: flex-start; gap: 12px; }
  .header-actions { width: 100%; justify-content: flex-end; }
  :deep(.el-table) { font-size: 12px; }
  .stat-icon { width: 40px; height: 40px; margin-right: 12px; }
  .stat-icon svg { width: 20px; height: 20px; }
  .stat-value { font-size: 20px; }
}
</style>