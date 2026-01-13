<template>
  <el-dialog
    v-model="visible"
    title="患者健康数据趋势"
    width="90%"
    max-width="1200px"
    :before-close="handleClose"
    :z-index="12000"
    :close-on-click-modal="true"
    :close-on-press-escape="true"
    :append-to-body="true"
    modal-class="health-trend-modal-overlay"
    class="health-trend-modal"
  >
    <div class="modal-content" v-loading="loading">
      <!-- 患者信息 -->
      <div class="patient-info" v-if="patientInfo">
        <div class="patient-header">
          <UserAvatar 
            :src="patientInfo.avatar"
            :username="patientInfo.name"
            size="medium"
          />
          <div class="patient-details">
            <h3>{{ patientInfo.name }}</h3>
            <p>{{ patientInfo.gender }} · {{ patientInfo.age }}岁</p>
          </div>
        </div>
      </div>

      <!-- 时间范围选择 -->
      <div class="time-selector">
        <el-radio-group v-model="selectedDays" @change="loadHealthData" class="days-selector">
          <el-radio-button :label="7">近7天</el-radio-button>
          <el-radio-button :label="30">近30天</el-radio-button>
          <el-radio-button :label="90">近90天</el-radio-button>
        </el-radio-group>
      </div>

      <!-- 模拟数据提示 -->
      <div class="mock-data-notice" v-if="isUsingMockData">
        <el-icon><InfoFilled /></el-icon>
        <span>当前显示的是模拟数据，仅用于功能演示</span>
      </div>

      <!-- 图表区域 -->
      <div class="charts-container">
        <!-- 血糖趋势图 -->
        <div class="chart-section">
          <div class="chart-header">
            <h4>血糖趋势图</h4>
            <div class="chart-legend">
              <span class="legend-item">
                <i class="legend-color glucose"></i>血糖值 (mmol/L)
              </span>
            </div>
          </div>
          <div class="chart-wrapper">
            <div ref="glucoseChartRef" class="chart-container"></div>
            <div v-if="!glucoseData.length && !loading" class="no-data">暂无血糖数据</div>
            <div v-if="loading" class="loading-data">加载中...</div>
          </div>
        </div>

        <!-- 血压趋势图 -->
        <div class="chart-section">
          <div class="chart-header">
            <h4>血压趋势图</h4>
            <div class="chart-legend">
              <span class="legend-item">
                <i class="legend-color systolic"></i>收缩压 (mmHg)
              </span>
              <span class="legend-item">
                <i class="legend-color diastolic"></i>舒张压 (mmHg)
              </span>
            </div>
          </div>
          <div class="chart-wrapper">
            <div ref="pressureChartRef" class="chart-container"></div>
            <div v-if="!pressureData.length && !loading" class="no-data">暂无血压数据</div>
            <div v-if="loading" class="loading-data">加载中...</div>
          </div>
        </div>
      </div>

      <!-- 数据概览 -->
      <div class="data-summary" v-if="overview">
        <div class="summary-item">
          <h5>血糖统计</h5>
          <div class="stats">
            <span>平均值: {{ overview.glucose?.average || 'N/A' }} mmol/L</span>
            <span>最高值: {{ overview.glucose?.max || 'N/A' }} mmol/L</span>
            <span>最低值: {{ overview.glucose?.min || 'N/A' }} mmol/L</span>
          </div>
        </div>
        <div class="summary-item">
          <h5>血压统计</h5>
          <div class="stats">
            <span>平均收缩压: {{ overview.pressure?.avgSystolic || 'N/A' }} mmHg</span>
            <span>平均舒张压: {{ overview.pressure?.avgDiastolic || 'N/A' }} mmHg</span>
          </div>
        </div>
      </div>
    </div>

    <template #footer>
      <div class="dialog-footer">
        <el-button @click="handleClose">关闭</el-button>
        <el-button type="primary" @click="refreshData">刷新数据</el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, reactive, watch, nextTick, onBeforeUnmount } from 'vue'
import { ElMessage } from 'element-plus'
import { InfoFilled } from '@element-plus/icons-vue'
import * as echarts from 'echarts'
import UserAvatar from './UserAvatar.vue'
import { 
  getPatientBloodGlucoseTrend, 
  getPatientBloodPressureTrend,
  getPatientHealthOverview
} from '@/api/health'

// Props
const props = defineProps({
  modelValue: {
    type: Boolean,
    default: false
  },
  patientId: {
    type: Number,
    required: true
  },
  patientInfo: {
    type: Object,
    default: () => ({})
  }
})

// Emits
const emit = defineEmits(['update:modelValue'])

// 响应式数据
const visible = ref(false)
const loading = ref(false)
const selectedDays = ref(30)
const glucoseData = ref([])
const pressureData = ref([])
const overview = ref(null)
const isUsingMockData = ref(false)

// 图表实例
const glucoseChartRef = ref(null)
const pressureChartRef = ref(null)
let glucoseChart = null
let pressureChart = null

// 安全的日期格式化函数
const formatSafeDate = (timeValue) => {
  if (!timeValue) {
    return ''
  }
  
  try {
    const date = new Date(timeValue)
    if (isNaN(date.getTime())) {
      console.warn('无效的时间值:', timeValue)
      return ''
    }
    return date.toISOString().split('T')[0]
  } catch (error) {
    console.error('时间格式化错误:', error, '原始值:', timeValue)
    return ''
  }
}

// 监听显示状态
watch(() => props.modelValue, (newVal) => {
  visible.value = newVal
  if (newVal && props.patientId) {
    loadHealthData()
  }
})

watch(visible, (newVal) => {
  emit('update:modelValue', newVal)
  if (!newVal) {
    // 清理图表
    if (glucoseChart) {
      glucoseChart.dispose()
      glucoseChart = null
    }
    if (pressureChart) {
      pressureChart.dispose() 
      pressureChart = null
    }
  }
})

// 生成模拟血糖数据
const generateMockGlucoseData = (days) => {
  const data = []
  const now = new Date()
  
  for (let i = days - 1; i >= 0; i--) {
    const date = new Date(now)
    date.setDate(date.getDate() - i)
    
    // 生成模拟血糖值 (4.0-10.0 mmol/L)
    const value = (4 + Math.random() * 6).toFixed(1)
    
    data.push({
      date: date.toISOString().split('T')[0],
      value: parseFloat(value),
      measureTime: date.toISOString()
    })
  }
  
  return data
}

// 生成模拟血压数据
const generateMockPressureData = (days) => {
  const data = []
  const now = new Date()
  
  for (let i = days - 1; i >= 0; i--) {
    const date = new Date(now)
    date.setDate(date.getDate() - i)
    
    // 生成模拟血压值 (收缩压: 90-140, 舒张压: 60-90)
    const systolic = Math.floor(90 + Math.random() * 50)
    const diastolic = Math.floor(60 + Math.random() * 30)
    
    data.push({
      date: date.toISOString().split('T')[0],
      systolic: systolic,
      diastolic: diastolic,
      measureTime: date.toISOString()
    })
  }
  
  return data
}

// 生成模拟概览数据
const generateMockOverview = (glucoseData, pressureData) => {
  if (glucoseData.length === 0 && pressureData.length === 0) {
    return null
  }
  
  let glucose = null
  let pressure = null
  
  if (glucoseData.length > 0) {
    const values = glucoseData.map(d => d.value)
    glucose = {
      average: (values.reduce((a, b) => a + b, 0) / values.length).toFixed(1),
      max: Math.max(...values).toFixed(1),
      min: Math.min(...values).toFixed(1)
    }
  }
  
  if (pressureData.length > 0) {
    const systolicValues = pressureData.map(d => d.systolic)
    const diastolicValues = pressureData.map(d => d.diastolic)
    
    pressure = {
      avgSystolic: Math.round(systolicValues.reduce((a, b) => a + b, 0) / systolicValues.length),
      avgDiastolic: Math.round(diastolicValues.reduce((a, b) => a + b, 0) / diastolicValues.length)
    }
  }
  
  return { glucose, pressure }
}

// 加载健康数据
const loadHealthData = async () => {
  if (!props.patientId) {
    ElMessage.warning('患者信息不完整，无法加载健康数据')
    return
  }
  
  loading.value = true
  isUsingMockData.value = false
  
  try {
    // 并行请求所有数据
    const [glucoseRes, pressureRes, overviewRes] = await Promise.allSettled([
      getPatientBloodGlucoseTrend(props.patientId, selectedDays.value),
      getPatientBloodPressureTrend(props.patientId, selectedDays.value), 
      getPatientHealthOverview(props.patientId)
    ])

    let hasRealData = false

    // 处理血糖数据
    if (glucoseRes.status === 'fulfilled' && glucoseRes.value.code === 200) {
      const responseData = glucoseRes.value.data
      console.log('血糖API返回数据:', responseData?.data?.length || 0, '条记录')
      
      if (responseData && responseData.data && Array.isArray(responseData.data) && responseData.data.length > 0) {
        glucoseData.value = responseData.data.map(item => ({
          date: formatSafeDate(item.time),
          value: item.value || 0,
          measureTime: item.time,
          measureType: item.type
        }))
        hasRealData = true
      } else {
        glucoseData.value = generateMockGlucoseData(selectedDays.value)
        isUsingMockData.value = true
      }
    } else {
      console.warn('血糖数据获取失败:', glucoseRes.reason || glucoseRes.value)
      glucoseData.value = generateMockGlucoseData(selectedDays.value)
      isUsingMockData.value = true
    }
    
    // 处理血压数据
    if (pressureRes.status === 'fulfilled' && pressureRes.value.code === 200) {
      const responseData = pressureRes.value.data
      console.log('血压API返回数据:', responseData?.data?.length || 0, '条记录')
      
      if (responseData && responseData.data && Array.isArray(responseData.data) && responseData.data.length > 0) {
        pressureData.value = responseData.data.map(item => ({
          date: formatSafeDate(item.time),
          systolic: item.systolic || 0,
          diastolic: item.diastolic || 0,
          measureTime: item.time,
          heartRate: item.heartRate
        }))
        hasRealData = true
      } else {
        pressureData.value = generateMockPressureData(selectedDays.value)
        isUsingMockData.value = true
      }
    } else {
      console.warn('血压数据获取失败:', pressureRes.reason || pressureRes.value)
      pressureData.value = generateMockPressureData(selectedDays.value)
      isUsingMockData.value = true
    }
    
    // 处理概览数据
    if (overviewRes.status === 'fulfilled' && overviewRes.value.code === 200 && overviewRes.value.data) {
      const responseData = overviewRes.value.data
      
      if (responseData.glucose || responseData.pressure) {
        overview.value = {
          glucose: responseData.glucose ? {
            average: responseData.glucose.avgValue?.toFixed(1) || 'N/A',
            max: responseData.glucose.maxValue?.toFixed(1) || 'N/A',
            min: responseData.glucose.minValue?.toFixed(1) || 'N/A'
          } : null,
          pressure: responseData.pressure ? {
            avgSystolic: responseData.pressure.avgSystolic || 'N/A',
            avgDiastolic: responseData.pressure.avgDiastolic || 'N/A'
          } : null
        }
      } else {
        overview.value = generateMockOverview(glucoseData.value, pressureData.value)
        isUsingMockData.value = true
      }
    } else {
      overview.value = generateMockOverview(glucoseData.value, pressureData.value)
      isUsingMockData.value = true
    }

    // 如果没有真实数据，显示提示
    if (!hasRealData) {
      ElMessage.info('该患者暂无健康记录或记录不在选定时间范围内')
    }

    await nextTick()
    initCharts()
    
  } catch (error) {
    console.error('加载健康数据失败:', error)
    ElMessage.error('加载健康数据失败，请检查网络连接')
    
    // 即使出错也显示模拟数据
    glucoseData.value = generateMockGlucoseData(selectedDays.value)
    pressureData.value = generateMockPressureData(selectedDays.value)
    overview.value = generateMockOverview(glucoseData.value, pressureData.value)
    isUsingMockData.value = true
    
    await nextTick()
    initCharts()
  } finally {
    loading.value = false
  }
}

// 初始化图表
const initCharts = () => {
  initGlucoseChart()
  initPressureChart()
}

// 初始化血糖图表
const initGlucoseChart = () => {
  if (!glucoseChartRef.value || !glucoseData.value.length) return

  if (glucoseChart) {
    glucoseChart.dispose()
  }

  glucoseChart = echarts.init(glucoseChartRef.value)
  
  const dates = glucoseData.value.map(item => item.date)
  const values = glucoseData.value.map(item => item.value)

  const option = {
    tooltip: {
      trigger: 'axis',
      formatter: function(params) {
        const point = params[0]
        return `${point.axisValue}<br/>血糖值: ${point.value} mmol/L`
      }
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: dates,
      axisLine: {
        lineStyle: { color: '#e4e7ed' }
      }
    },
    yAxis: {
      type: 'value',
      name: 'mmol/L',
      axisLine: {
        lineStyle: { color: '#e4e7ed' }
      },
      splitLine: {
        lineStyle: { color: '#f5f7fa' }
      }
    },
    series: [{
      name: '血糖值',
      type: 'line',
      data: values,
      smooth: true,
      itemStyle: { color: '#e74c3c' },
      areaStyle: {
        color: {
          type: 'linear',
          x: 0, y: 0, x2: 0, y2: 1,
          colorStops: [
            { offset: 0, color: 'rgba(231, 76, 60, 0.3)' },
            { offset: 1, color: 'rgba(231, 76, 60, 0.1)' }
          ]
        }
      }
    }]
  }

  glucoseChart.setOption(option)
}

// 初始化血压图表
const initPressureChart = () => {
  if (!pressureChartRef.value || !pressureData.value.length) return

  if (pressureChart) {
    pressureChart.dispose()
  }

  pressureChart = echarts.init(pressureChartRef.value)
  
  const dates = pressureData.value.map(item => item.date)
  const systolicValues = pressureData.value.map(item => item.systolic)
  const diastolicValues = pressureData.value.map(item => item.diastolic)

  const option = {
    tooltip: {
      trigger: 'axis',
      formatter: function(params) {
        let result = `${params[0].axisValue}<br/>`
        params.forEach(param => {
          result += `${param.seriesName}: ${param.value} mmHg<br/>`
        })
        return result
      }
    },
    legend: {
      data: ['收缩压', '舒张压']
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: dates,
      axisLine: {
        lineStyle: { color: '#e4e7ed' }
      }
    },
    yAxis: {
      type: 'value',
      name: 'mmHg',
      axisLine: {
        lineStyle: { color: '#e4e7ed' }
      },
      splitLine: {
        lineStyle: { color: '#f5f7fa' }
      }
    },
    series: [
      {
        name: '收缩压',
        type: 'line',
        data: systolicValues,
        smooth: true,
        itemStyle: { color: '#3498db' },
        areaStyle: {
          color: {
            type: 'linear',
            x: 0, y: 0, x2: 0, y2: 1,
            colorStops: [
              { offset: 0, color: 'rgba(52, 152, 219, 0.3)' },
              { offset: 1, color: 'rgba(52, 152, 219, 0.1)' }
            ]
          }
        }
      },
      {
        name: '舒张压',
        type: 'line',
        data: diastolicValues,
        smooth: true,
        itemStyle: { color: '#2ecc71' }
      }
    ]
  }

  pressureChart.setOption(option)
}

// 刷新数据
const refreshData = () => {
  loadHealthData()
}

// 关闭弹窗
const handleClose = () => {
  visible.value = false
}

// 组件卸载时清理
onBeforeUnmount(() => {
  if (glucoseChart) {
    glucoseChart.dispose()
  }
  if (pressureChart) {
    pressureChart.dispose()
  }
})
</script>

<style>
/* 全局设置，确保健康趋势弹窗的层级最高 */
.el-overlay.health-trend-modal {
  z-index: 11500 !important;
}
.el-dialog__wrapper.health-trend-modal {
  z-index: 12000 !important;
}

/* 通过modal-class设置的样式 */
.health-trend-modal-overlay {
  z-index: 11500 !important;
}

/* 弹窗内容滚动设置 */
.health-trend-modal-overlay .el-dialog {
  margin: 2vh auto;
  max-height: 96vh;
  display: flex;
  flex-direction: column;
}

.health-trend-modal-overlay .el-dialog__body {
  flex: 1;
  overflow-y: auto;
  max-height: 70vh;
}

/* 强制确保弹窗可以交互 */
.health-trend-modal,
.health-trend-modal *,
.health-trend-modal-overlay,
.health-trend-modal-overlay * {
  pointer-events: auto !important;
}
</style>

<style scoped>
/* 确保弹窗在最高层级 */
.health-trend-modal {
  z-index: 12000 !important;
}

.health-trend-modal :deep(.el-overlay) {
  z-index: 11500 !important;
}

.health-trend-modal :deep(.el-dialog__wrapper) {
  z-index: 12000 !important;
}

.health-trend-modal :deep(.el-dialog) {
  margin-top: 2vh;
  margin-bottom: 2vh;
  z-index: 12000 !important;
  max-height: 96vh;
  pointer-events: auto !important;
}

.health-trend-modal :deep(.el-dialog__body) {
  max-height: 70vh;
  overflow-y: auto;
  padding: 20px;
  pointer-events: auto !important;
}

.modal-content {
  min-height: auto;
  max-height: none;
  pointer-events: auto !important;
}

/* 确保所有交互元素都可以正常工作 */
.health-trend-modal :deep(.el-button),
.health-trend-modal :deep(.el-radio-button),
.health-trend-modal :deep(.el-dialog__header),
.health-trend-modal :deep(.el-dialog__footer),
.health-trend-modal :deep(.el-icon) {
  pointer-events: auto !important;
  z-index: inherit;
}

.patient-info {
  margin-bottom: 20px;
  padding: 16px;
  background: #f8fafc;
  border-radius: 8px;
}

.patient-header {
  display: flex;
  align-items: center;
  gap: 12px;
}

.patient-details h3 {
  margin: 0 0 4px 0;
  font-size: 18px;
  font-weight: 600;
  color: #1e293b;
}

.patient-details p {
  margin: 0;
  font-size: 14px;
  color: #64748b;
}

.time-selector {
  margin-bottom: 24px;
  text-align: center;
}

.days-selector {
  display: inline-flex;
}

.charts-container {
  display: grid;
  grid-template-columns: 1fr;
  gap: 32px;
  margin-bottom: 24px;
}

.chart-section {
  background: white;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

.chart-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.chart-header h4 {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  color: #1e293b;
}

.chart-legend {
  display: flex;
  gap: 16px;
}

.legend-item {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  color: #64748b;
}

.legend-color {
  width: 12px;
  height: 12px;
  border-radius: 2px;
}

.legend-color.glucose {
  background: #e74c3c;
}

.legend-color.systolic {
  background: #3498db;
}

.legend-color.diastolic {
  background: #2ecc71;
}

.chart-wrapper {
  position: relative;
  height: 300px;
}

.chart-container {
  width: 100%;
  height: 100%;
}

.no-data {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  color: #94a3b8;
  font-size: 14px;
}

.loading-data {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  color: #64748b;
  font-size: 14px;
}

.data-summary {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 20px;
  padding: 20px;
  background: #f8fafc;
  border-radius: 8px;
}

.summary-item h5 {
  margin: 0 0 8px 0;
  font-size: 14px;
  font-weight: 600;
  color: #1e293b;
}

.stats {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.stats span {
  font-size: 13px;
  color: #64748b;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

.mock-data-notice {
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #e0f2fe;
  color: #2c7be5;
  padding: 10px 20px;
  border-radius: 6px;
  margin-bottom: 24px;
  font-size: 14px;
  font-weight: 500;
}

.mock-data-notice .el-icon {
  margin-right: 8px;
  font-size: 18px;
}

/* 响应式设计 */
@media (min-width: 768px) {
  .charts-container {
    grid-template-columns: 1fr 1fr;
  }
}

@media (max-width: 480px) {
  .health-trend-modal :deep(.el-dialog) {
    margin: 0;
    width: 100%;
    height: 100%;
    max-width: none;
  }
  
  .patient-header {
    flex-direction: column;
    text-align: center;
    gap: 8px;
  }
  
  .chart-legend {
    flex-direction: column;
    gap: 8px;
  }
  
  .data-summary {
    grid-template-columns: 1fr;
  }
}
</style> 