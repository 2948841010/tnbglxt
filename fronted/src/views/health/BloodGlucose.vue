<template>
  <div class="blood-glucose-page">
    <!-- 页面标题和操作区 -->
    <div class="page-header">
      <div class="header-content">
        <div class="header-icon">
          <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke-width="1.5" stroke="currentColor" class="icon">
            <path stroke-linecap="round" stroke-linejoin="round" d="M9.75 3.104v5.714a2.25 2.25 0 0 1-.659 1.591L5 14.5M9.75 3.104c-.251.023-.501.05-.75.082m.75-.082a24.301 24.301 0 0 1 4.5 0m0 0v5.714c0 .597.237 1.17.659 1.591L19.8 15.3M14.25 3.104c.251.023.501.05.75.082M19.8 15.3l-1.57.393A9.065 9.065 0 0 1 12 15a9.065 9.065 0 0 0-6.23-.693L5 14.5m14.8.8 1.402 1.402c1.232 1.232.65 3.318-1.067 3.611A48.309 48.309 0 0 1 12 21c-2.773 0-5.491-.235-8.135-.687-1.718-.293-2.3-2.379-1.067-3.61L5 14.5" />
          </svg>
        </div>
        <div class="header-text">
          <h1 class="page-title">血糖记录管理</h1>
          <p class="page-subtitle">监测和管理您的血糖水平，保持健康生活</p>
        </div>
      </div>
      <div class="header-actions">
        <el-button type="primary" size="large" class="add-btn" @click="showAddDialog">
          <el-icon><Plus /></el-icon>
          添加血糖记录
        </el-button>
      </div>
    </div>

    <!-- 统计卡片 -->
    <el-row :gutter="20" class="stats-cards">
      <el-col :xs="24" :sm="12" :md="6">
        <div class="stat-card glucose-card">
          <div class="stat-icon">
            <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke-width="1.5" stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" d="M3 13.125C3 12.504 3.504 12 4.125 12h2.25c.621 0 1.125.504 1.125 1.125v6.75C7.5 20.496 6.996 21 6.375 21h-2.25A1.125 1.125 0 0 1 3 19.875v-6.75ZM9.75 8.625c0-.621.504-1.125 1.125-1.125h2.25c.621 0 1.125.504 1.125 1.125v11.25c0 .621-.504 1.125-1.125 1.125h-2.25a1.125 1.125 0 0 1-1.125-1.125V8.625ZM16.5 4.125c0-.621.504-1.125 1.125-1.125h2.25C20.496 3 21 3.504 21 4.125v15.75c0 .621-.504 1.125-1.125 1.125h-2.25a1.125 1.125 0 0 1-1.125-1.125V4.125Z" />
            </svg>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ statistics.avgValue || '--' }}</div>
            <div class="stat-label">平均血糖 (mmol/L)</div>
          </div>
        </div>
      </el-col>
      <el-col :xs="24" :sm="12" :md="6">
        <div class="stat-card normal-card">
          <div class="stat-icon">
            <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke-width="1.5" stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" d="M9 12.75 11.25 15 15 9.75M21 12a9 9 0 1 1-18 0 9 9 0 0 1 18 0Z" />
            </svg>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ statistics.normalCount || 0 }}</div>
            <div class="stat-label">正常记录</div>
            <div class="stat-sub">占比 {{ getNormalPercentage() }}%</div>
          </div>
        </div>
      </el-col>
      <el-col :xs="24" :sm="12" :md="6">
        <div class="stat-card high-card">
          <div class="stat-icon">
            <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke-width="1.5" stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" d="M12 9v3.75m-9.303 3.376c-.866 1.5.217 3.374 1.948 3.374h14.71c1.73 0 2.813-1.874 1.948-3.374L13.949 3.378c-.866-1.5-3.032-1.5-3.898 0L2.697 16.126ZM12 15.75h.007v.008H12v-.008Z" />
            </svg>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ statistics.highCount || 0 }}</div>
            <div class="stat-label">偏高记录</div>
            <div class="stat-sub">最高 {{ statistics.maxValue || '--' }} mmol/L</div>
          </div>
        </div>
      </el-col>
      <el-col :xs="24" :sm="12" :md="6">
        <div class="stat-card low-card">
          <div class="stat-icon">
            <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke-width="1.5" stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" d="m11.25 11.25.041-.02a.75.75 0 0 1 1.063.852l-.708 2.836a.75.75 0 0 0 1.063.853l.041-.021M21 12a9 9 0 1 1-18 0 9 9 0 0 1 18 0Zm-9-3.75h.008v.008H12V8.25Z" />
            </svg>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ statistics.lowCount || 0 }}</div>
            <div class="stat-label">偏低记录</div>
            <div class="stat-sub">最低 {{ statistics.minValue || '--' }} mmol/L</div>
          </div>
        </div>
      </el-col>
    </el-row>

    <!-- 趋势图表 -->
    <el-row :gutter="20" class="charts-section">
      <el-col :xs="24" :lg="16">
        <el-card class="chart-card">
          <template #header>
            <div class="chart-header">
              <span class="chart-title">血糖趋势图</span>
              <el-select v-model="chartPeriod" @change="loadChartData" size="small" class="period-select">
                <el-option label="7天" value="7" />
                <el-option label="30天" value="30" />
                <el-option label="90天" value="90" />
              </el-select>
            </div>
          </template>
          <div class="chart-container" v-loading="loadingChart">
            <div ref="chartRef" class="trend-chart" style="width: 100%; height: 300px;"></div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :xs="24" :lg="8">
        <el-card class="distribution-card">
          <template #header>
            <span class="chart-title">血糖分布</span>
          </template>
          <div class="distribution-chart" v-loading="loadingDistribution">
            <div class="distribution-item">
              <div class="distribution-label">正常范围</div>
              <div class="distribution-bar">
                <div class="distribution-fill normal-fill" :style="{ width: getNormalPercentage() + '%' }"></div>
              </div>
              <div class="distribution-value">{{ getNormalPercentage() }}%</div>
            </div>
            <div class="distribution-item">
              <div class="distribution-label">偏高</div>
              <div class="distribution-bar">
                <div class="distribution-fill high-fill" :style="{ width: getHighPercentage() + '%' }"></div>
              </div>
              <div class="distribution-value">{{ getHighPercentage() }}%</div>
            </div>
            <div class="distribution-item">
              <div class="distribution-label">偏低</div>
              <div class="distribution-bar">
                <div class="distribution-fill low-fill" :style="{ width: getLowPercentage() + '%' }"></div>
              </div>
              <div class="distribution-value">{{ getLowPercentage() }}%</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 记录管理区域 -->
    <el-card class="records-card">
      <template #header>
        <div class="records-header">
          <div class="header-left">
            <span class="records-title">血糖记录</span>
            <el-tag v-if="selectedRecords.length > 0" type="info" class="selection-tag">
              已选择 {{ selectedRecords.length }} 条记录
            </el-tag>
          </div>
          <div class="header-actions">
            <el-button v-if="selectedRecords.length > 0" type="danger" size="small" :disabled="loading" @click="batchDeleteRecords">
              <el-icon><Delete /></el-icon>
              批量删除
            </el-button>
            <el-button type="primary" size="small" class="add-record-btn" @click="showAddDialog">
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
            <el-select v-model="filters.measureType" placeholder="测量类型" clearable @change="searchRecords">
              <el-option label="空腹血糖" value="fasting" />
              <el-option label="餐后血糖" value="after_meal" />
              <el-option label="随机血糖" value="random" />
            </el-select>
          </el-col>
          <el-col :xs="24" :sm="8" :md="6">
            <el-select v-model="filters.mealType" placeholder="餐次" clearable @change="searchRecords">
              <el-option label="早餐" value="breakfast" />
              <el-option label="午餐" value="lunch" />
              <el-option label="晚餐" value="dinner" />
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
              <el-button @click="searchRecords" :loading="loading" class="search-btn">搜索</el-button>
              <el-button @click="resetFilters">重置</el-button>
            </div>
          </el-col>
        </el-row>
      </div>

      <!-- 血糖记录表格 -->
      <el-table :data="records" v-loading="loading" empty-text="暂无血糖记录" style="width: 100%" @selection-change="handleSelectionChange" class="data-table">
        <el-table-column type="selection" width="55" />
        <el-table-column prop="value" label="血糖值" width="120">
          <template #default="{ row }">
            <div class="glucose-value">
              <span class="value">{{ row.value }}</span>
              <span class="unit">mmol/L</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="level" label="水平" width="100">
          <template #default="{ row }">
            <el-tag :type="getLevelTagType(row.level)" size="small">{{ getLevelLabel(row.level) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="measureType" label="测量类型" width="120">
          <template #default="{ row }">
            <span>{{ getMeasureTypeLabel(row.measureType) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="mealType" label="餐次" width="100">
          <template #default="{ row }">
            <span v-if="row.mealType">{{ getMealTypeLabel(row.mealType) }}</span>
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

    <!-- 添加/编辑血糖记录对话框 -->
    <el-dialog v-model="dialogVisible" :title="isEditing ? '编辑血糖记录' : '添加血糖记录'" width="500px" @close="resetForm" class="record-dialog">
      <el-form ref="recordFormRef" :model="recordForm" :rules="recordRules" label-width="100px">
        <el-form-item label="血糖值" prop="value">
          <el-input-number v-model="recordForm.value" :min="0.1" :max="50" :step="0.1" :precision="1" placeholder="请输入血糖值" style="width: 100%" />
          <span class="input-suffix">mmol/L</span>
        </el-form-item>
        <el-form-item label="测量类型" prop="measureType">
          <el-select v-model="recordForm.measureType" placeholder="请选择测量类型" style="width: 100%">
            <el-option label="空腹血糖" value="fasting" />
            <el-option label="餐后血糖" value="after_meal" />
            <el-option label="随机血糖" value="random" />
          </el-select>
        </el-form-item>
        <el-form-item label="餐次" v-if="recordForm.measureType === 'after_meal'">
          <el-select v-model="recordForm.mealType" placeholder="请选择餐次（可选）" clearable style="width: 100%">
            <el-option label="早餐" value="breakfast" />
            <el-option label="午餐" value="lunch" />
            <el-option label="晚餐" value="dinner" />
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
import { ref, reactive, onMounted, nextTick } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Delete } from '@element-plus/icons-vue'
import dayjs from 'dayjs'
import * as healthApi from '@/api/health'
import * as echarts from 'echarts'

// 响应式数据
const loading = ref(false)
const loadingChart = ref(false)
const loadingDistribution = ref(false)
const saving = ref(false)
const dialogVisible = ref(false)
const isEditing = ref(false)
const recordFormRef = ref()
const selectedRecords = ref([])
const chartPeriod = ref('30')

// 图表相关
const chartRef = ref(null)
let chart = null

const records = ref([])
const statistics = ref({})
const trends = ref({})

const filters = reactive({
  measureType: '',
  mealType: '',
  dateRange: []
})

const pagination = reactive({
  current: 1,
  size: 20,
  total: 0
})

const recordForm = reactive({
  id: null,
  value: null,
  measureType: '',
  mealType: '',
  measureTime: new Date(),
  note: ''
})

// 表单验证规则
const recordRules = {
  value: [
    { required: true, message: '请输入血糖值', trigger: 'blur' },
    { type: 'number', min: 0.1, max: 50, message: '血糖值应在0.1-50之间', trigger: 'blur' }
  ],
  measureType: [{ required: true, message: '请选择测量类型', trigger: 'change' }],
  measureTime: [{ required: true, message: '请选择测量时间', trigger: 'change' }]
}

// 计算百分比
const getNormalPercentage = () => {
  if (!statistics.value.totalCount || statistics.value.totalCount === 0) return 0
  return Math.round((statistics.value.normalCount || 0) / statistics.value.totalCount * 100)
}

const getHighPercentage = () => {
  if (!statistics.value.totalCount || statistics.value.totalCount === 0) return 0
  return Math.round((statistics.value.highCount || 0) / statistics.value.totalCount * 100)
}

const getLowPercentage = () => {
  if (!statistics.value.totalCount || statistics.value.totalCount === 0) return 0
  return Math.round((statistics.value.lowCount || 0) / statistics.value.totalCount * 100)
}

// 获取水平标签样式
const getLevelTagType = (level) => {
  const typeMap = { normal: 'success', high: 'danger', low: 'warning' }
  return typeMap[level] || 'info'
}

// 获取水平标签文本
const getLevelLabel = (level) => {
  const labelMap = { normal: '正常', high: '偏高', low: '偏低' }
  return labelMap[level] || level
}

// 获取测量类型标签
const getMeasureTypeLabel = (type) => {
  const typeMap = { fasting: '空腹', after_meal: '餐后', random: '随机' }
  return typeMap[type] || type
}

// 获取餐次标签
const getMealTypeLabel = (type) => {
  const typeMap = { breakfast: '早餐', lunch: '午餐', dinner: '晚餐' }
  return typeMap[type] || type
}

// 处理选择变化
const handleSelectionChange = (selection) => {
  selectedRecords.value = selection
}

// 批量删除记录
const batchDeleteRecords = async () => {
  try {
    await ElMessageBox.confirm(`确定要删除选中的 ${selectedRecords.value.length} 条血糖记录吗？`, '批量删除', { confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning' })
    const recordIds = selectedRecords.value.map(r => r.id)
    await healthApi.batchDeleteBloodGlucoseRecords(recordIds)
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
  Object.assign(recordForm, { id: row.id, value: row.value, measureType: row.measureType, mealType: row.mealType, measureTime: row.measureTime, note: row.note || '' })
}

// 删除记录
const deleteRecord = async (row) => {
  try {
    await ElMessageBox.confirm('确定要删除这条血糖记录吗？', '提示', { confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning' })
    await healthApi.deleteBloodGlucoseRecord(row.id)
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
    const data = { value: recordForm.value, measureType: recordForm.measureType, mealType: recordForm.mealType, measureTime: recordForm.measureTime, note: recordForm.note }
    if (isEditing.value) {
      await healthApi.updateBloodGlucoseRecord(recordForm.id, data)
    } else {
      await healthApi.addBloodGlucoseRecord(data)
    }
    ElMessage.success(isEditing.value ? '血糖记录已更新' : '血糖记录已添加')
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
  if (recordFormRef.value) recordFormRef.value.clearValidate()
  Object.assign(recordForm, { id: null, value: null, measureType: '', mealType: '', measureTime: new Date(), note: '' })
}

// 搜索记录
const searchRecords = () => {
  pagination.current = 1
  loadRecords()
}

// 重置筛选
const resetFilters = () => {
  Object.assign(filters, { measureType: '', mealType: '', dateRange: [] })
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

// 初始化图表
const initChart = async () => {
  await nextTick()
  if (!chartRef.value) return
  chart = echarts.init(chartRef.value)
  const option = {
    tooltip: { trigger: 'axis', formatter: (params) => `<div><div>时间：${params[0].name}</div><div>血糖值：${params[0].value} mmol/L</div></div>` },
    legend: { data: ['血糖值'], bottom: 10 },
    grid: { left: '3%', right: '4%', bottom: '15%', containLabel: true },
    xAxis: { type: 'category', boundaryGap: false, data: [] },
    yAxis: { type: 'value', name: 'mmol/L', min: 3, max: 15, splitLine: { show: true, lineStyle: { color: '#A5F3FC', type: 'dashed' } } },
    series: [{ name: '血糖值', type: 'line', smooth: true, symbol: 'circle', symbolSize: 6, lineStyle: { width: 2, color: '#0891B2' }, itemStyle: { color: '#0891B2' }, areaStyle: { opacity: 0.1, color: '#22D3EE' }, data: [] }]
  }
  chart.setOption(option)
  window.addEventListener('resize', () => chart?.resize())
}

// 加载图表数据
const loadChartData = async () => {
  try {
    loadingChart.value = true
    const response = await healthApi.getHealthDataTrend('glucose', parseInt(chartPeriod.value))
    if (response.data && response.data.data && Array.isArray(response.data.data)) {
      const chartData = response.data.data.map(item => ({ name: dayjs(item.time).format('MM-DD'), value: item.value || 0 }))
      if (chart) {
        chart.setOption({ xAxis: { data: chartData.map(item => item.name) }, series: [{ data: chartData.map(item => item.value) }] })
      }
    } else {
      if (chart) chart.setOption({ xAxis: { data: [] }, series: [{ data: [] }] })
    }
  } catch (error) {
    console.error('加载图表数据失败:', error)
    ElMessage.error('加载图表数据失败')
  } finally {
    loadingChart.value = false
  }
}

// 加载血糖记录
const loadRecords = async () => {
  try {
    loading.value = true
    const params = { current: pagination.current, size: pagination.size }
    if (filters.measureType) params.measureType = filters.measureType
    if (filters.mealType) params.mealType = filters.mealType
    if (filters.dateRange && filters.dateRange.length === 2) {
      params.startTime = filters.dateRange[0]
      params.endTime = filters.dateRange[1]
    }
    const response = await healthApi.getBloodGlucoseRecords(params)
    if (response.data && response.data.records) {
      records.value = response.data.records
      pagination.total = response.data.records.length
    } else {
      records.value = []
      pagination.total = 0
    }
  } catch (error) {
    console.error('加载血糖记录失败:', error)
    ElMessage.error('加载血糖记录失败')
    records.value = []
    pagination.total = 0
  } finally {
    loading.value = false
  }
}

// 加载统计信息
const loadStatistics = async () => {
  try {
    loadingDistribution.value = true
    const response = await healthApi.getBloodGlucoseRecords({ current: 1, size: 1000 })
    if (response.data && response.data.statistics) {
      statistics.value = response.data.statistics
    } else {
      statistics.value = { avgValue: 0, maxValue: 0, minValue: 0, totalCount: 0, normalCount: 0, highCount: 0, lowCount: 0 }
    }
    trends.value = { avg: 0 }
  } catch (error) {
    console.error('加载统计信息失败:', error)
    statistics.value = { avgValue: 0, maxValue: 0, minValue: 0, totalCount: 0, normalCount: 0, highCount: 0, lowCount: 0 }
    trends.value = { avg: 0 }
  } finally {
    loadingDistribution.value = false
  }
}

onMounted(async () => {
  loadRecords()
  loadStatistics()
  await initChart()
  loadChartData()
})
</script>

<style scoped>
.blood-glucose-page {
  max-width: 1400px;
  margin: 0 auto;
}

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

.header-content {
  display: flex;
  align-items: center;
  gap: 16px;
}

.header-icon {
  width: 56px;
  height: 56px;
  background: linear-gradient(135deg, #0891B2 0%, #22D3EE 100%);
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.header-icon .icon {
  width: 32px;
  height: 32px;
  color: white;
}

.page-title {
  font-size: 24px;
  font-weight: 700;
  color: #164E63;
  margin: 0 0 4px 0;
}

.page-subtitle {
  font-size: 14px;
  color: #0891B2;
  margin: 0;
}

.add-btn {
  background: linear-gradient(135deg, #0891B2 0%, #22D3EE 100%);
  border: none;
  border-radius: 8px;
  padding: 12px 24px;
}

.add-btn:hover {
  background: linear-gradient(135deg, #0E7490 0%, #06B6D4 100%);
}

.stats-cards {
  margin-bottom: 24px;
}

.stat-card {
  background: white;
  border-radius: 16px;
  padding: 20px;
  display: flex;
  align-items: center;
  cursor: pointer;
  transition: all 0.3s ease;
  border: 1px solid #E0F2FE;
  height: 100px;
}

.stat-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 12px 24px rgba(8, 145, 178, 0.15);
}

.stat-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 16px;
}

.stat-icon svg {
  width: 24px;
  height: 24px;
  color: white;
}

.glucose-card .stat-icon { background: linear-gradient(135deg, #0891B2 0%, #22D3EE 100%); }
.normal-card .stat-icon { background: linear-gradient(135deg, #059669 0%, #34D399 100%); }
.high-card .stat-icon { background: linear-gradient(135deg, #DC2626 0%, #F87171 100%); }
.low-card .stat-icon { background: linear-gradient(135deg, #D97706 0%, #FBBF24 100%); }

.stat-content { flex: 1; }
.stat-value { font-size: 24px; font-weight: bold; color: #164E63; margin-bottom: 4px; }
.stat-label { font-size: 13px; color: #0891B2; margin-bottom: 2px; }
.stat-sub { font-size: 12px; color: #67E8F9; }

.charts-section { margin-bottom: 24px; }

.chart-card, .distribution-card {
  border-radius: 16px;
  border: 1px solid #E0F2FE;
  height: 380px;
}

.chart-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.chart-title {
  font-weight: 600;
  color: #164E63;
}

.period-select :deep(.el-input__wrapper) {
  border-radius: 8px;
}

.chart-container { height: 300px; }

.distribution-chart { padding: 20px 0; }

.distribution-item {
  display: flex;
  align-items: center;
  margin-bottom: 20px;
}

.distribution-label { width: 80px; font-size: 14px; color: #164E63; }

.distribution-bar {
  flex: 1;
  height: 20px;
  background: #ECFEFF;
  border-radius: 10px;
  margin: 0 16px;
  overflow: hidden;
}

.distribution-fill {
  height: 100%;
  border-radius: 10px;
  transition: width 0.8s ease;
}

.normal-fill { background: linear-gradient(90deg, #059669, #34D399); }
.high-fill { background: linear-gradient(90deg, #DC2626, #F87171); }
.low-fill { background: linear-gradient(90deg, #D97706, #FBBF24); }

.distribution-value { width: 40px; text-align: right; font-size: 14px; font-weight: 500; color: #164E63; }

.records-card {
  border-radius: 16px;
  border: 1px solid #E0F2FE;
}

.records-header {
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

.records-title {
  font-size: 18px;
  font-weight: 600;
  color: #164E63;
}

.selection-tag {
  background: #ECFEFF;
  color: #0891B2;
  border: 1px solid #A5F3FC;
}

.header-actions {
  display: flex;
  gap: 8px;
}

.add-record-btn {
  background: #0891B2;
  border-color: #0891B2;
}

.add-record-btn:hover {
  background: #0E7490;
  border-color: #0E7490;
}

.filter-section {
  background: #F0FDFA;
  padding: 20px;
  border-radius: 12px;
  margin-bottom: 20px;
  border: 1px solid #A5F3FC;
}

.filter-actions {
  display: flex;
  gap: 8px;
}

.search-btn {
  background: #0891B2;
  border-color: #0891B2;
  color: white;
}

.search-btn:hover {
  background: #0E7490;
  border-color: #0E7490;
}

.data-table :deep(.el-table__header th) {
  background: #F0FDFA;
  color: #164E63;
}

.glucose-value {
  display: flex;
  align-items: baseline;
  gap: 4px;
}

.value {
  font-size: 16px;
  font-weight: 600;
  color: #0891B2;
}

.unit {
  font-size: 12px;
  color: #67E8F9;
}

.time-info { line-height: 1.2; }
.time-detail { font-size: 12px; color: #67E8F9; margin-top: 2px; }
.text-gray { color: #94A3B8; }
.input-suffix { margin-left: 8px; color: #67E8F9; font-size: 14px; }

.pagination-container {
  display: flex;
  justify-content: center;
  margin-top: 24px;
}

.record-dialog :deep(.el-dialog__header) {
  background: linear-gradient(135deg, #ECFEFF 0%, #CFFAFE 100%);
  border-radius: 8px 8px 0 0;
}

.record-dialog :deep(.el-dialog__title) {
  color: #164E63;
  font-weight: 600;
}

.save-btn {
  background: #0891B2;
  border-color: #0891B2;
}

.save-btn:hover {
  background: #0E7490;
  border-color: #0E7490;
}

@media (max-width: 768px) {
  .page-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 16px;
  }
  .page-title { font-size: 20px; }
  .stats-cards :deep(.el-col) { margin-bottom: 16px; }
  .charts-section :deep(.el-col) { margin-bottom: 16px; }
  .stat-card { height: auto; min-height: 80px; padding: 16px; }
  .stat-icon { width: 40px; height: 40px; margin-right: 12px; }
  .stat-icon svg { width: 20px; height: 20px; }
  .stat-value { font-size: 20px; }
}
</style>