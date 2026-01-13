<template>
  <div class="blood-pressure-page">
    <!-- 页面标题 -->
    <div class="page-header">
      <div class="header-content">
        <div class="header-icon">
          <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke-width="1.5" stroke="currentColor" class="icon">
            <path stroke-linecap="round" stroke-linejoin="round" d="M21 8.25c0-2.485-2.099-4.5-4.688-4.5-1.935 0-3.597 1.126-4.312 2.733-.715-1.607-2.377-2.733-4.313-2.733C5.1 3.75 3 5.765 3 8.25c0 7.22 9 12 9 12s9-4.78 9-12Z" />
          </svg>
        </div>
        <div class="header-text">
          <h1 class="page-title">血压记录管理</h1>
          <p class="page-subtitle">监测血压变化，守护心血管健康</p>
        </div>
      </div>
      <div class="header-actions">
        <el-button type="primary" size="large" class="add-btn" @click="showAddDialog">
          <el-icon><Plus /></el-icon>
          添加血压记录
        </el-button>
      </div>
    </div>

    <!-- 统计卡片 -->
    <el-row :gutter="16" class="stats-section">
      <el-col :xs="24" :sm="12" :md="6">
        <div class="stat-card pressure-card">
          <div class="stat-icon">
            <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke-width="1.5" stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" d="M3 13.125C3 12.504 3.504 12 4.125 12h2.25c.621 0 1.125.504 1.125 1.125v6.75C7.5 20.496 6.996 21 6.375 21h-2.25A1.125 1.125 0 0 1 3 19.875v-6.75ZM9.75 8.625c0-.621.504-1.125 1.125-1.125h2.25c.621 0 1.125.504 1.125 1.125v11.25c0 .621-.504 1.125-1.125 1.125h-2.25a1.125 1.125 0 0 1-1.125-1.125V8.625ZM16.5 4.125c0-.621.504-1.125 1.125-1.125h2.25C20.496 3 21 3.504 21 4.125v15.75c0 .621-.504 1.125-1.125 1.125h-2.25a1.125 1.125 0 0 1-1.125-1.125V4.125Z" />
            </svg>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ statistics.avgSystolic ? `${statistics.avgSystolic}/${statistics.avgDiastolic}` : '--' }}</div>
            <div class="stat-label">平均血压 (mmHg)</div>
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
          <div class="stat-info">
            <div class="stat-value">{{ statistics.normalCount || 0 }}</div>
            <div class="stat-label">正常记录</div>
          </div>
        </div>
      </el-col>
      <el-col :xs="24" :sm="12" :md="6">
        <div class="stat-card high-card">
          <div class="stat-icon">
            <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke-width="1.5" stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" d="M2.25 18 9 11.25l4.306 4.306a11.95 11.95 0 0 1 5.814-5.518l2.74-1.22m0 0-5.94-2.281m5.94 2.28-2.28 5.941" />
            </svg>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ statistics.highCount || 0 }}</div>
            <div class="stat-label">高血压记录</div>
          </div>
        </div>
      </el-col>
      <el-col :xs="24" :sm="12" :md="6">
        <div class="stat-card heart-card">
          <div class="stat-icon">
            <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke-width="1.5" stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" d="M21 8.25c0-2.485-2.099-4.5-4.688-4.5-1.935 0-3.597 1.126-4.312 2.733-.715-1.607-2.377-2.733-4.313-2.733C5.1 3.75 3 5.765 3 8.25c0 7.22 9 12 9 12s9-4.78 9-12Z" />
            </svg>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ statistics.avgHeartRate || '--' }}</div>
            <div class="stat-label">平均心率 (bpm)</div>
          </div>
        </div>
      </el-col>
    </el-row>

    <!-- 趋势图表 -->
    <el-row :gutter="20" class="charts-section">
      <el-col :span="24">
        <el-card class="chart-card">
          <template #header>
            <div class="chart-header">
              <span class="chart-title">血压趋势分析</span>
              <el-select v-model="chartPeriod" @change="loadChartData" size="small" class="period-select">
                <el-option label="7天" value="7" />
                <el-option label="30天" value="30" />
                <el-option label="90天" value="90" />
              </el-select>
            </div>
          </template>
          <div class="chart-container" v-loading="loadingChart">
            <div ref="chartRef" class="trend-chart" style="width: 100%; height: 350px;"></div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 主要内容 -->
    <el-card class="main-card">
      <template #header>
        <div class="card-header">
          <div class="header-left">
            <span class="title">血压记录</span>
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
              <el-option label="休息时" value="rest" />
              <el-option label="活动后" value="activity" />
              <el-option label="晨起" value="morning" />
              <el-option label="晚间" value="evening" />
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

      <!-- 血压记录表格 -->
      <el-table :data="records" v-loading="loading" empty-text="暂无血压记录" style="width: 100%" @selection-change="handleSelectionChange" class="data-table">
        <el-table-column type="selection" width="55" />
        <el-table-column prop="systolic" label="收缩压" width="120">
          <template #default="{ row }">
            <div class="pressure-value"><span class="value systolic">{{ row.systolic }}</span><span class="unit">mmHg</span></div>
          </template>
        </el-table-column>
        <el-table-column prop="diastolic" label="舒张压" width="120">
          <template #default="{ row }">
            <div class="pressure-value"><span class="value diastolic">{{ row.diastolic }}</span><span class="unit">mmHg</span></div>
          </template>
        </el-table-column>
        <el-table-column prop="heartRate" label="心率" width="100">
          <template #default="{ row }">
            <span v-if="row.heartRate" class="heart-rate">{{ row.heartRate }} bpm</span>
            <span v-else class="text-gray">--</span>
          </template>
        </el-table-column>
        <el-table-column prop="level" label="健康状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getLevelTagType(row.level)" size="small">{{ getLevelLabel(row.level) }}</el-tag>
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

    <!-- 添加/编辑血压记录对话框 -->
    <el-dialog v-model="dialogVisible" :title="isEditing ? '编辑血压记录' : '添加血压记录'" width="500px" @close="resetForm" class="record-dialog">
      <el-form ref="recordFormRef" :model="recordForm" :rules="recordRules" label-width="100px">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="收缩压" prop="systolic">
              <el-input-number v-model="recordForm.systolic" :min="50" :max="300" placeholder="收缩压" style="width: 100%" />
              <span class="input-suffix">mmHg</span>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="舒张压" prop="diastolic">
              <el-input-number v-model="recordForm.diastolic" :min="30" :max="200" placeholder="舒张压" style="width: 100%" />
              <span class="input-suffix">mmHg</span>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="心率">
          <el-input-number v-model="recordForm.heartRate" :min="30" :max="200" placeholder="心率（可选）" style="width: 100%" />
          <span class="input-suffix">bpm</span>
        </el-form-item>
        <el-form-item label="测量状态">
          <el-select v-model="recordForm.measureState" placeholder="请选择测量状态（可选）" clearable style="width: 100%">
            <el-option label="休息时" value="rest" />
            <el-option label="活动后" value="activity" />
            <el-option label="晨起" value="morning" />
            <el-option label="晚间" value="evening" />
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

const loading = ref(false)
const loadingChart = ref(false)
const saving = ref(false)
const dialogVisible = ref(false)
const isEditing = ref(false)
const recordFormRef = ref()
const selectedRecords = ref([])
const chartRef = ref(null)
const chartPeriod = ref('30')
let chart = null

const records = ref([])
const statistics = ref({})
const filters = reactive({ measureState: '', dateRange: [] })
const pagination = reactive({ current: 1, size: 20, total: 0 })
const recordForm = reactive({ id: null, systolic: null, diastolic: null, heartRate: null, measureState: '', measureTime: new Date(), note: '' })

const recordRules = {
  systolic: [{ required: true, message: '请输入收缩压', trigger: 'blur' }, { type: 'number', min: 50, max: 300, message: '收缩压应在50-300之间', trigger: 'blur' }],
  diastolic: [{ required: true, message: '请输入舒张压', trigger: 'blur' }, { type: 'number', min: 30, max: 200, message: '舒张压应在30-200之间', trigger: 'blur' }],
  measureTime: [{ required: true, message: '请选择测量时间', trigger: 'change' }]
}

const getLevelTagType = (level) => ({ normal: 'success', high: 'danger', low: 'warning' }[level] || '')
const getLevelLabel = (level) => ({ normal: '正常', high: '偏高', low: '偏低' }[level] || level)
const getMeasureStateLabel = (state) => ({ rest: '休息时', activity: '活动后', morning: '晨起', evening: '晚间' }[state] || state)
const handleSelectionChange = (selection) => { selectedRecords.value = selection }

const batchDeleteRecords = async () => {
  try {
    await ElMessageBox.confirm(`确定要删除选中的 ${selectedRecords.value.length} 条血压记录吗？`, '批量删除', { confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning' })
    const recordIds = selectedRecords.value.map(r => r.id)
    await healthApi.batchDeleteBloodPressureRecords(recordIds)
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
  Object.assign(recordForm, { id: row.id, systolic: row.systolic, diastolic: row.diastolic, heartRate: row.heartRate, measureState: row.measureState, measureTime: row.measureTime, note: row.note || '' })
}

const deleteRecord = async (row) => {
  try {
    await ElMessageBox.confirm('确定要删除这条血压记录吗？', '提示', { confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning' })
    await healthApi.deleteBloodPressureRecord(row.id)
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
    const data = { systolic: recordForm.systolic, diastolic: recordForm.diastolic, heartRate: recordForm.heartRate, measureState: recordForm.measureState, measureTime: recordForm.measureTime, note: recordForm.note }
    if (isEditing.value) { await healthApi.updateBloodPressureRecord(recordForm.id, data) }
    else { await healthApi.addBloodPressureRecord(data) }
    ElMessage.success(isEditing.value ? '血压记录已更新' : '血压记录已添加')
    dialogVisible.value = false
    await loadRecords()
    await loadStatistics()
  } catch (error) { console.error('保存失败:', error); ElMessage.error('保存失败') }
  finally { saving.value = false }
}

const resetForm = () => {
  if (recordFormRef.value) recordFormRef.value.clearValidate()
  Object.assign(recordForm, { id: null, systolic: null, diastolic: null, heartRate: null, measureState: '', measureTime: new Date(), note: '' })
}

const searchRecords = () => { pagination.current = 1; loadRecords() }
const resetFilters = () => { Object.assign(filters, { measureState: '', dateRange: [] }); searchRecords() }
const handleSizeChange = (size) => { pagination.size = size; loadRecords() }
const handleCurrentChange = (current) => { pagination.current = current; loadRecords() }

const loadRecords = async () => {
  try {
    loading.value = true
    const params = { current: pagination.current, size: pagination.size, sortField: 'measureTime', sortOrder: 'desc' }
    if (filters.measureState) params.measureState = filters.measureState
    if (filters.dateRange && filters.dateRange.length === 2) { params.startTime = filters.dateRange[0]; params.endTime = filters.dateRange[1] }
    const response = await healthApi.getBloodPressureRecords(params)
    if (response.data && response.data.records) {
      records.value = response.data.records
      pagination.total = response.data.records.length
      if (response.data.statistics) statistics.value = response.data.statistics
    } else { records.value = []; pagination.total = 0 }
  } catch (error) { console.error('加载血压记录失败:', error); ElMessage.error('加载血压记录失败') }
  finally { loading.value = false }
}

const loadStatistics = async () => {
  try {
    const response = await healthApi.getBloodPressureRecords({})
    if (response.data && response.data.statistics) statistics.value = response.data.statistics
  } catch (error) { console.error('加载统计信息失败:', error) }
}

const initChart = async () => {
  await nextTick()
  if (!chartRef.value) return
  chart = echarts.init(chartRef.value)
  const option = {
    tooltip: { trigger: 'axis', formatter: (params) => { let result = `<div>时间：${params[0].name}<br/>`; params.forEach(item => { const unit = item.seriesName === '心率' ? ' bpm' : ' mmHg'; result += `${item.marker}${item.seriesName}：${item.value}${unit}<br/>` }); return result + '</div>' } },
    legend: { data: ['收缩压', '舒张压', '心率'], bottom: 10 },
    grid: { left: '3%', right: '4%', bottom: '15%', containLabel: true },
    xAxis: { type: 'category', boundaryGap: false, data: [] },
    yAxis: [
      { type: 'value', name: '血压 (mmHg)', position: 'left', min: 60, max: 180, splitLine: { show: true, lineStyle: { color: '#A5F3FC', type: 'dashed' } } },
      { type: 'value', name: '心率 (bpm)', position: 'right', min: 50, max: 120, splitLine: { show: false } }
    ],
    series: [
      { name: '收缩压', type: 'line', yAxisIndex: 0, smooth: true, symbol: 'circle', symbolSize: 6, lineStyle: { width: 2, color: '#DC2626' }, itemStyle: { color: '#DC2626' }, data: [] },
      { name: '舒张压', type: 'line', yAxisIndex: 0, smooth: true, symbol: 'circle', symbolSize: 6, lineStyle: { width: 2, color: '#0891B2' }, itemStyle: { color: '#0891B2' }, data: [] },
      { name: '心率', type: 'line', yAxisIndex: 1, smooth: true, symbol: 'diamond', symbolSize: 6, lineStyle: { width: 2, color: '#059669', type: 'dashed' }, itemStyle: { color: '#059669' }, data: [] }
    ]
  }
  chart.setOption(option)
  window.addEventListener('resize', () => chart?.resize())
}

const loadChartData = async () => {
  try {
    loadingChart.value = true
    const response = await healthApi.getHealthDataTrend('pressure', parseInt(chartPeriod.value))
    if (response.data && response.data.data && Array.isArray(response.data.data)) {
      const chartData = response.data.data.map(item => ({ name: dayjs(item.time).format('MM-DD'), systolic: item.systolic || 0, diastolic: item.diastolic || 0, heartRate: item.heartRate || 0 }))
      if (chart) {
        chart.setOption({
          xAxis: { data: chartData.map(item => item.name) },
          series: [{ data: chartData.map(item => item.systolic) }, { data: chartData.map(item => item.diastolic) }, { data: chartData.map(item => item.heartRate) }]
        })
      }
    } else {
      if (chart) chart.setOption({ xAxis: { data: [] }, series: [{ data: [] }, { data: [] }, { data: [] }] })
    }
  } catch (error) { console.error('加载图表数据失败:', error); ElMessage.error('加载图表数据失败') }
  finally { loadingChart.value = false }
}

onMounted(async () => {
  loadRecords()
  loadStatistics()
  await initChart()
  loadChartData()
})
</script>

<style scoped>
.blood-pressure-page { padding: 0; }

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
  background: linear-gradient(135deg, #DC2626 0%, #F87171 100%);
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

.pressure-card .stat-icon { background: linear-gradient(135deg, #0891B2 0%, #22D3EE 100%); }
.normal-card .stat-icon { background: linear-gradient(135deg, #059669 0%, #34D399 100%); }
.high-card .stat-icon { background: linear-gradient(135deg, #DC2626 0%, #F87171 100%); }
.heart-card .stat-icon { background: linear-gradient(135deg, #EC4899 0%, #F472B6 100%); }

.stat-info { flex: 1; }
.stat-value { font-size: 24px; font-weight: bold; color: #164E63; line-height: 1; }
.stat-label { font-size: 14px; color: #0891B2; margin-top: 4px; }

.charts-section { margin-bottom: 16px; }

.chart-card { border: 1px solid #E0F2FE; border-radius: 16px; }
.chart-header { display: flex; justify-content: space-between; align-items: center; }
.chart-title { font-weight: 600; color: #164E63; }
.chart-container { min-height: 350px; display: flex; align-items: center; justify-content: center; }
.trend-chart { width: 100%; height: 100%; }

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

.pressure-value { display: flex; align-items: baseline; gap: 4px; }
.value { font-size: 16px; font-weight: 600; }
.systolic { color: #DC2626; }
.diastolic { color: #0891B2; }
.unit { font-size: 12px; color: #67E8F9; }
.heart-rate { font-weight: 600; color: #059669; }
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