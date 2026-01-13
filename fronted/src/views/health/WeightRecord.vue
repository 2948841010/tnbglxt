<template>
  <div class="weight-record-page">
    <!-- 统计卡片 -->
    <el-row :gutter="16" class="stats-section">
      <el-col :xs="24" :sm="12" :md="6">
        <el-card class="stats-card">
          <div class="stats-content">
            <div class="stats-icon weight-icon">
              <el-icon><Grid /></el-icon>
            </div>
            <div class="stats-info">
              <div class="stats-value">{{ statistics.avgWeight || '--' }}</div>
              <div class="stats-label">平均体重 (kg)</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="12" :md="6">
        <el-card class="stats-card">
          <div class="stats-content">
            <div class="stats-icon bmi-icon">
              <el-icon><Odometer /></el-icon>
            </div>
            <div class="stats-info">
              <div class="stats-value">{{ statistics.currentBmi || '--' }}</div>
              <div class="stats-label">当前BMI</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="12" :md="6">
        <el-card class="stats-card">
          <div class="stats-content">
            <div class="stats-icon trend-icon">
              <el-icon><TrendCharts /></el-icon>
            </div>
            <div class="stats-info">
              <div class="stats-value">{{ formatWeightChange(statistics.weightChange30Days) }}</div>
              <div class="stats-label">30天变化 (kg)</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="12" :md="6">
        <el-card class="stats-card">
          <div class="stats-content">
            <div class="stats-icon target-icon">
              <el-icon><Aim /></el-icon>
            </div>
            <div class="stats-info">
              <div class="stats-value">{{ statistics.targetWeight || '--' }}</div>
              <div class="stats-label">目标体重 (kg)</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 主要内容 -->
    <el-card class="main-card">
      <template #header>
        <div class="card-header">
          <div class="header-left">
            <span class="title">体重记录</span>
            <el-tag v-if="selectedRecords.length > 0" type="info" class="selection-tag">
              已选择 {{ selectedRecords.length }} 条记录
            </el-tag>
          </div>
          <div class="header-actions">
            <el-button 
              v-if="selectedRecords.length > 0" 
              type="danger" 
              size="small"
              :disabled="loading"
              @click="batchDeleteRecords"
            >
              <el-icon><Delete /></el-icon>
              批量删除
            </el-button>
            <el-button type="primary" size="small" @click="showAddDialog">
              <el-icon><Plus /></el-icon>
              添加记录
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
            <el-date-picker
              v-model="filters.dateRange"
              type="datetimerange"
              range-separator="至"
              start-placeholder="开始时间"
              end-placeholder="结束时间"
              format="YYYY-MM-DD HH:mm"
              value-format="YYYY-MM-DD HH:mm:ss"
              @change="searchRecords"
            />
          </el-col>
          <el-col :xs="24" :sm="8" :md="4">
            <div class="filter-actions">
              <el-button @click="searchRecords" :loading="loading">搜索</el-button>
              <el-button @click="resetFilters">重置</el-button>
            </div>
          </el-col>
        </el-row>
      </div>

      <!-- 体重记录表格 -->
      <el-table
        :data="records"
        v-loading="loading"
        empty-text="暂无体重记录"
        style="width: 100%"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55" />
        
        <el-table-column prop="weight" label="体重" width="120">
          <template #default="{ row }">
            <div class="weight-value">
              <span class="value">{{ row.weight }}</span>
              <span class="unit">kg</span>
            </div>
          </template>
        </el-table-column>

        <el-table-column prop="bmi" label="BMI" width="100">
          <template #default="{ row }">
            <div v-if="row.bmi" class="bmi-info">
              <div class="bmi-value">{{ row.bmi }}</div>
              <el-tag :type="getBmiTagType(row.bmi)" size="small">
                {{ getBmiLabel(row.bmi) }}
              </el-tag>
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
            <el-button
              text
              type="primary"
              size="small"
              @click="editRecord(row)"
            >
              编辑
            </el-button>
            <el-button
              text
              type="danger"
              size="small"
              @click="deleteRecord(row)"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="pagination.current"
          v-model:page-size="pagination.size"
          :total="pagination.total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <!-- 添加/编辑体重记录对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEditing ? '编辑体重记录' : '添加体重记录'"
      width="500px"
      @close="resetForm"
    >
      <el-form
        ref="recordFormRef"
        :model="recordForm"
        :rules="recordRules"
        label-width="100px"
      >
        <el-form-item label="体重" prop="weight">
          <el-input-number
            v-model="recordForm.weight"
            :min="1"
            :max="500"
            :step="0.1"
            :precision="1"
            placeholder="请输入体重"
            style="width: 100%"
          />
          <span class="input-suffix">kg</span>
        </el-form-item>
        
        <el-form-item label="身高">
          <el-input-number
            v-model="recordForm.height"
            :min="50"
            :max="250"
            :step="0.1"
            :precision="1"
            placeholder="身高（可选，用于计算BMI）"
            style="width: 100%"
          />
          <span class="input-suffix">cm</span>
        </el-form-item>
        
        <el-form-item label="体脂率">
          <el-input-number
            v-model="recordForm.bodyFatRate"
            :min="0"
            :max="100"
            :step="0.1"
            :precision="1"
            placeholder="体脂率（可选）"
            style="width: 100%"
          />
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
          <el-date-picker
            v-model="recordForm.measureTime"
            type="datetime"
            placeholder="请选择测量时间"
            format="YYYY-MM-DD HH:mm"
            value-format="YYYY-MM-DD HH:mm:ss"
            style="width: 100%"
          />
        </el-form-item>

        <el-form-item label="备注">
          <el-input
            v-model="recordForm.note"
            type="textarea"
            :rows="3"
            placeholder="请输入备注（可选）"
            maxlength="200"
            show-word-limit
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveRecord" :loading="saving">
          确定
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Delete, Grid, Odometer, TrendCharts, Aim } from '@element-plus/icons-vue'
import dayjs from 'dayjs'
import * as healthApi from '@/api/health'

// 响应式数据
const loading = ref(false)
const saving = ref(false)
const dialogVisible = ref(false)
const isEditing = ref(false)
const recordFormRef = ref()
const selectedRecords = ref([])

const records = ref([])
const statistics = ref({})

const filters = reactive({
  measureState: '',
  dateRange: []
})

const pagination = reactive({
  current: 1,
  size: 20,
  total: 0
})

const recordForm = reactive({
  id: null,
  weight: null,
  height: null,
  bodyFatRate: null,
  measureState: '',
  measureTime: new Date(),
  note: ''
})

// 表单验证规则
const recordRules = {
  weight: [
    { required: true, message: '请输入体重', trigger: 'blur' },
    { type: 'number', min: 1, max: 500, message: '体重应在1-500kg之间', trigger: 'blur' }
  ],
  measureTime: [
    { required: true, message: '请选择测量时间', trigger: 'change' }
  ]
}

// 获取BMI标签样式
const getBmiTagType = (bmi) => {
  if (bmi < 18.5) return 'warning'
  if (bmi < 24) return 'success'
  if (bmi < 28) return 'warning'
  return 'danger'
}

// 获取BMI标签文本
const getBmiLabel = (bmi) => {
  if (bmi < 18.5) return '偏瘦'
  if (bmi < 24) return '正常'
  if (bmi < 28) return '超重'
  return '肥胖'
}

// 获取测量状态标签文本
const getMeasureStateLabel = (state) => {
  const stateMap = {
    morning: '晨起',
    evening: '晚间',
    after_meal: '餐后',
    before_meal: '餐前'
  }
  return stateMap[state] || state
}

// 格式化体重变化
const formatWeightChange = (change) => {
  if (!change) return '--'
  const sign = change > 0 ? '+' : ''
  return `${sign}${change}`
}

// 处理选择变化
const handleSelectionChange = (selection) => {
  selectedRecords.value = selection
}

// 批量删除记录
const batchDeleteRecords = async () => {
  try {
    await ElMessageBox.confirm(
      `确定要删除选中的 ${selectedRecords.value.length} 条体重记录吗？`,
      '批量删除',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    const recordIds = selectedRecords.value.map(r => r.id)
    await healthApi.batchDeleteWeightRecords(recordIds)
    
    ElMessage.success('批量删除成功')
    selectedRecords.value = []
    await loadRecords()
    await loadStatistics()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('批量删除失败:', error)
      ElMessage.error('批量删除失败')
    }
  }
}

// 显示添加对话框
const showAddDialog = () => {
  isEditing.value = false
  dialogVisible.value = true
  resetForm()
}

// 编辑记录
const editRecord = (row) => {
  isEditing.value = true
  dialogVisible.value = true
  
  Object.assign(recordForm, {
    id: row.id,
    weight: row.weight,
    height: row.height,
    bodyFatRate: row.bodyFatRate,
    measureState: row.measureState,
    measureTime: row.measureTime,
    note: row.note || ''
  })
}

// 删除记录
const deleteRecord = async (row) => {
  try {
    await ElMessageBox.confirm(
      '确定要删除这条体重记录吗？',
      '提示',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    await healthApi.deleteWeightRecord(row.id)
    ElMessage.success('删除成功')
    await loadRecords()
    await loadStatistics()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除失败:', error)
      ElMessage.error('删除失败')
    }
  }
}

// 保存记录
const saveRecord = async () => {
  if (saving.value) return

  try {
    const valid = await recordFormRef.value.validate()
    if (!valid) return

    saving.value = true

    const data = {
      weight: recordForm.weight,
      height: recordForm.height,
      bodyFatRate: recordForm.bodyFatRate,
      measureState: recordForm.measureState,
      measureTime: recordForm.measureTime,
      note: recordForm.note
    }

    if (isEditing.value) {
      await healthApi.updateWeightRecord(recordForm.id, data)
    } else {
      await healthApi.addWeightRecord(data)
    }

    ElMessage.success(isEditing.value ? '体重记录已更新' : '体重记录已添加')
    dialogVisible.value = false
    await loadRecords()
    await loadStatistics()
  } catch (error) {
    console.error('保存失败:', error)
    ElMessage.error('保存失败')
  } finally {
    saving.value = false
  }
}

// 重置表单
const resetForm = () => {
  if (recordFormRef.value) {
    recordFormRef.value.clearValidate()
  }
  Object.assign(recordForm, {
    id: null,
    weight: null,
    height: null,
    bodyFatRate: null,
    measureState: '',
    measureTime: new Date(),
    note: ''
  })
}

// 搜索记录
const searchRecords = () => {
  pagination.current = 1
  loadRecords()
}

// 重置筛选
const resetFilters = () => {
  Object.assign(filters, {
    measureState: '',
    dateRange: []
  })
  searchRecords()
}

// 分页大小变化
const handleSizeChange = (size) => {
  pagination.size = size
  loadRecords()
}

// 当前页变化
const handleCurrentChange = (current) => {
  pagination.current = current
  loadRecords()
}

// 加载体重记录
const loadRecords = async () => {
  try {
    loading.value = true

    // 构建查询参数
    const params = {
      current: pagination.current,
      size: pagination.size
    }

    // 添加筛选条件
    if (filters.measureState) {
      params.measureState = filters.measureState
    }
    if (filters.dateRange && filters.dateRange.length === 2) {
      params.startTime = filters.dateRange[0]
      params.endTime = filters.dateRange[1]
    }

    const response = await healthApi.getWeightRecords(params)
    
    if (response.data && response.data.records) {
      records.value = response.data.records
      pagination.total = response.data.records.length // 注意：实际应该从后端返回总数
      
      // 更新统计信息
      if (response.data.statistics) {
        statistics.value = response.data.statistics
      }
    } else {
      records.value = []
      pagination.total = 0
    }

  } catch (error) {
    console.error('加载体重记录失败:', error)
    ElMessage.error('加载体重记录失败')
    records.value = []
    pagination.total = 0
  } finally {
    loading.value = false
  }
}

// 加载统计信息
const loadStatistics = async () => {
  try {
    // 获取体重记录统计信息
    const response = await healthApi.getWeightRecords({ current: 1, size: 1000 }) // 获取所有记录以计算统计
    
    if (response.data && response.data.statistics) {
      statistics.value = response.data.statistics
    } else {
      // 如果没有统计数据，设置默认值
      statistics.value = {
        avgWeight: 0,
        currentBmi: 0,
        weightChange30Days: 0,
        targetWeight: 0
      }
    }
  } catch (error) {
    console.error('加载统计信息失败:', error)
    // 设置默认统计数据
    statistics.value = {
      avgWeight: 0,
      currentBmi: 0,
      weightChange30Days: 0,
      targetWeight: 0
    }
  }
}

onMounted(() => {
  loadRecords()
  loadStatistics()
})
</script>

<style scoped>
.weight-record-page {
  padding: 0;
}

.stats-section {
  margin-bottom: 16px;
}

.stats-card {
  cursor: pointer;
  transition: all 0.3s ease;
}

.stats-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.stats-content {
  display: flex;
  align-items: center;
  padding: 8px 0;
}

.stats-icon {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 16px;
  font-size: 20px;
  color: white;
}

.weight-icon {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.bmi-icon {
  background: linear-gradient(135deg, #67B26F 0%, #4ca2cd 100%);
}

.trend-icon {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
}

.target-icon {
  background: linear-gradient(135deg, #ffecd2 0%, #fcb69f 100%);
}

.stats-info {
  flex: 1;
}

.stats-value {
  font-size: 24px;
  font-weight: bold;
  color: #303133;
  line-height: 1;
}

.stats-label {
  font-size: 14px;
  color: #909399;
  margin-top: 4px;
}

.main-card {
  margin-top: 0;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.selection-tag {
  font-size: 12px;
}

.header-actions {
  display: flex;
  gap: 8px;
}

.filter-section {
  background: #f8f9fa;
  padding: 16px;
  border-radius: 8px;
  margin-bottom: 16px;
  border: 1px solid #e4e7ed;
}

.filter-actions {
  display: flex;
  gap: 8px;
}

.weight-value {
  display: flex;
  align-items: baseline;
  gap: 4px;
}

.value {
  font-size: 16px;
  font-weight: 600;
  color: #409EFF;
}

.unit {
  font-size: 12px;
  color: #909399;
}

.bmi-info {
  line-height: 1.2;
}

.bmi-value {
  font-weight: 600;
  color: #303133;
  margin-bottom: 4px;
}

.time-info {
  line-height: 1.2;
}

.time-detail {
  font-size: 12px;
  color: #909399;
  margin-top: 2px;
}

.text-gray {
  color: #909399;
}

.input-suffix {
  margin-left: 8px;
  color: #909399;
  font-size: 14px;
}

.pagination-container {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}

@media (max-width: 768px) {
  .stats-section :deep(.el-col) {
    margin-bottom: 8px;
  }
  
  .filter-section {
    padding: 12px;
  }
  
  .filter-actions {
    justify-content: flex-start;
    margin-top: 8px;
  }
  
  .card-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }
  
  .header-actions {
    width: 100%;
    justify-content: flex-end;
  }
  
  :deep(.el-table) {
    font-size: 12px;
  }
  
  .stats-content {
    padding: 4px 0;
  }
  
  .stats-icon {
    width: 40px;
    height: 40px;
    margin-right: 12px;
    font-size: 16px;
  }
  
  .stats-value {
    font-size: 20px;
  }
}</style> 