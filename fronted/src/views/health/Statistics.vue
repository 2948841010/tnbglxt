<template>
  <div class="health-statistics-page">
    <!-- 综合统计卡片 -->
    <el-row :gutter="16" class="overview-section">
      <el-col :xs="24" :sm="12" :md="8">
        <el-card class="overview-card glucose-card">
          <div class="card-content">
            <div class="card-header">
              <h3>血糖管理</h3>
              <el-icon class="card-icon"><Odometer /></el-icon>
            </div>
            <div class="stats-grid">
              <div class="stat-item">
                <div class="stat-value">{{ glucoseStats.avgValue || '--' }}</div>
                <div class="stat-label">平均血糖 (mmol/L)</div>
              </div>
              <div class="stat-item">
                <div class="stat-value">{{ glucoseStats.normalCount || 0 }}</div>
                <div class="stat-label">正常记录</div>
              </div>
            </div>
            <el-button type="primary" size="small" @click="$router.push('/health/glucose')">
              查看详情
            </el-button>
          </div>
        </el-card>
      </el-col>
      
      <el-col :xs="24" :sm="12" :md="8">
        <el-card class="overview-card pressure-card">
          <div class="card-content">
            <div class="card-header">
              <h3>血压管理</h3>
                             <el-icon class="card-icon"><Monitor /></el-icon>
            </div>
            <div class="stats-grid">
              <div class="stat-item">
                <div class="stat-value">
                  {{ pressureStats.avgSystolic ? `${pressureStats.avgSystolic}/${pressureStats.avgDiastolic}` : '--' }}
                </div>
                <div class="stat-label">平均血压 (mmHg)</div>
              </div>
              <div class="stat-item">
                <div class="stat-value">{{ pressureStats.normalCount || 0 }}</div>
                <div class="stat-label">正常记录</div>
              </div>
            </div>
            <el-button type="success" size="small" @click="$router.push('/health/pressure')">
              查看详情
            </el-button>
          </div>
        </el-card>
      </el-col>
      
      <el-col :xs="24" :sm="12" :md="8">
        <el-card class="overview-card weight-card">
          <div class="card-content">
            <div class="card-header">
              <h3>体重管理</h3>
                             <el-icon class="card-icon"><Grid /></el-icon>
            </div>
            <div class="stats-grid">
              <div class="stat-item">
                <div class="stat-value">{{ weightStats.avgWeight || '--' }}</div>
                <div class="stat-label">平均体重 (kg)</div>
              </div>
              <div class="stat-item">
                <div class="stat-value">{{ weightStats.currentBmi || '--' }}</div>
                <div class="stat-label">当前BMI</div>
              </div>
            </div>
            <el-button type="info" size="small" @click="$router.push('/health/weight')">
              查看详情
            </el-button>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 趋势分析 -->
    <el-row :gutter="16" class="trend-section">
      <el-col :xs="24" :md="12">
        <el-card>
          <template #header>
            <div class="card-header-inline">
              <span>血糖趋势</span>
              <el-select v-model="glucosePeriod" @change="loadGlucoseTrend" size="small">
                <el-option label="7天" value="7" />
                <el-option label="30天" value="30" />
                <el-option label="90天" value="90" />
              </el-select>
            </div>
          </template>
          <div class="chart-container" v-loading="loadingGlucose">
            <div 
              ref="glucoseChartRef" 
              class="trend-chart"
              style="width: 100%; height: 280px;"
            ></div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :xs="24" :md="12">
        <el-card>
          <template #header>
            <div class="card-header-inline">
              <span>血压趋势</span>
              <el-select v-model="pressurePeriod" @change="loadPressureTrend" size="small">
                <el-option label="7天" value="7" />
                <el-option label="30天" value="30" />
                <el-option label="90天" value="90" />
              </el-select>
            </div>
          </template>
          <div class="chart-container" v-loading="loadingPressure">
            <div 
              ref="pressureChartRef" 
              class="trend-chart"
              style="width: 100%; height: 280px;"
            ></div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 健康总结 -->
    <el-row :gutter="16" class="summary-section">
      <el-col :xs="24" :md="16">
        <el-card>
          <template #header>
            <span>健康总结</span>
          </template>
          <div class="summary-content">
            <div class="summary-item">
              <div class="summary-title">
                <el-icon><CircleCheck /></el-icon>
                整体健康状况
              </div>
              <div class="summary-desc">
                基于您的健康记录，目前血糖控制良好，血压保持在正常范围内。
                建议继续保持健康的生活方式。
              </div>
            </div>
            
            <div class="summary-item">
              <div class="summary-title">
                <el-icon><Warning /></el-icon>
                注意事项
              </div>
              <div class="summary-desc">
                近期血糖波动较大，建议注意饮食控制，按时服药，
                如有异常及时咨询医生。
              </div>
            </div>
            
            <div class="summary-item">
              <div class="summary-title">
                <el-icon><Star /></el-icon>
                建议
              </div>
              <div class="summary-desc">
                1. 保持规律的血糖监测<br>
                2. 合理控制饮食，少食多餐<br>
                3. 坚持适量运动<br>
                4. 按时服药，定期复查
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :xs="24" :md="8">
        <el-card>
          <template #header>
            <span>快速操作</span>
          </template>
          <div class="quick-actions">
            <el-button 
              type="primary" 
              :icon="Plus" 
              class="action-btn"
              @click="$router.push('/health/glucose')"
            >
              添加血糖记录
            </el-button>
            <el-button 
              type="success" 
              :icon="Plus" 
              class="action-btn"
              @click="$router.push('/health/pressure')"
            >
              添加血压记录
            </el-button>
            <el-button 
              type="info" 
              :icon="Plus" 
              class="action-btn"
              @click="$router.push('/health/weight')"
            >
              添加体重记录
            </el-button>
            <el-button 
              type="warning" 
              :icon="Document" 
              class="action-btn"
              @click="exportData"
            >
              导出健康报告
            </el-button>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import { 
  Plus, 
  Document, 
  Odometer, 
  Monitor, 
  Grid, 
  TrendCharts,
  CircleCheck,
  Warning,
  Star
} from '@element-plus/icons-vue'
import dayjs from 'dayjs'
import * as echarts from 'echarts'
import * as healthApi from '@/api/health'

// 响应式数据
const loadingGlucose = ref(false)
const loadingPressure = ref(false)
const glucosePeriod = ref('30')
const pressurePeriod = ref('30')

const glucoseStats = ref({})
const pressureStats = ref({})
const weightStats = ref({})

// 图表相关
const glucoseChartRef = ref(null)
const pressureChartRef = ref(null)
let glucoseChart = null
let pressureChart = null

// 加载血糖统计
const loadGlucoseStats = async () => {
  try {
    const response = await healthApi.getBloodGlucoseRecords({ current: 1, size: 1000 })
    
    if (response.data && response.data.statistics) {
      glucoseStats.value = response.data.statistics
    } else {
      glucoseStats.value = {
        avgValue: 0,
        normalCount: 0,
        highCount: 0,
        lowCount: 0
      }
    }
  } catch (error) {
    console.error('加载血糖统计失败:', error)
    glucoseStats.value = {
      avgValue: 0,
      normalCount: 0,
      highCount: 0,
      lowCount: 0
    }
  }
}

// 加载血压统计
const loadPressureStats = async () => {
  try {
    const response = await healthApi.getBloodPressureRecords({ current: 1, size: 1000 })
    
    if (response.data && response.data.statistics) {
      pressureStats.value = response.data.statistics
    } else {
      pressureStats.value = {
        avgSystolic: 0,
        avgDiastolic: 0,
        normalCount: 0,
        highCount: 0,
        lowCount: 0
      }
    }
  } catch (error) {
    console.error('加载血压统计失败:', error)
    pressureStats.value = {
      avgSystolic: 0,
      avgDiastolic: 0,
      normalCount: 0,
      highCount: 0,
      lowCount: 0
    }
  }
}

// 加载体重统计
const loadWeightStats = async () => {
  try {
    const response = await healthApi.getWeightRecords({ current: 1, size: 1000 })
    
    if (response.data && response.data.statistics) {
      weightStats.value = response.data.statistics
    } else {
      weightStats.value = {
        avgWeight: 0,
        currentBmi: 0,
        weightChange30Days: 0
      }
    }
  } catch (error) {
    console.error('加载体重统计失败:', error)
    weightStats.value = {
      avgWeight: 0,
      currentBmi: 0,
      weightChange30Days: 0
    }
  }
}

// 初始化血糖图表
const initGlucoseChart = async () => {
  await nextTick()
  if (!glucoseChartRef.value) return
  
  glucoseChart = echarts.init(glucoseChartRef.value)
  
  const option = {
    title: {
      text: `血糖趋势（最近${glucosePeriod.value}天）`,
      left: 'center',
      textStyle: {
        fontSize: 14,
        fontWeight: 'normal'
      }
    },
    tooltip: {
      trigger: 'axis',
      formatter: function (params) {
        const data = params[0]
        return `
          <div>
            <div>时间：${data.name}</div>
            <div>血糖值：${data.value} mmol/L</div>
            <div>状态：${getGlucoseLevel(data.value)}</div>
          </div>
        `
      }
    },
    legend: {
      data: ['血糖值'],
      bottom: 10
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '15%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: []
    },
    yAxis: {
      type: 'value',
      name: 'mmol/L',
      min: 3,
      max: 15,
      splitLine: {
        show: true,
        lineStyle: {
          color: '#E4E7ED',
          type: 'dashed'
        }
      }
    },
    visualMap: {
      type: 'piecewise',
      show: false,
      dimension: 1,
      pieces: [
        { gte: 0, lt: 3.9, color: '#5DADE2' },    // 低血糖
        { gte: 3.9, lt: 6.1, color: '#58D68D' },  // 正常
        { gte: 6.1, lt: 7.8, color: '#F7DC6F' },  // 餐后正常
        { gte: 7.8, lt: 11.1, color: '#F8C471' }, // 偏高
        { gte: 11.1, color: '#EC7063' }           // 高血糖
      ]
    },
    series: [
      {
        name: '血糖值',
        type: 'line',
        smooth: true,
        symbol: 'circle',
        symbolSize: 6,
        lineStyle: {
          width: 2
        },
        areaStyle: {
          opacity: 0.1
        },
        data: []
      }
    ]
  }
  
  glucoseChart.setOption(option)
  
  // 窗口大小变化时重新调整图表
  window.addEventListener('resize', () => {
    glucoseChart?.resize()
  })
}

// 加载血糖趋势
const loadGlucoseTrend = async () => {
  try {
    loadingGlucose.value = true
    
    // 获取血糖趋势数据
    const response = await healthApi.getHealthDataTrend('glucose', parseInt(glucosePeriod.value))
    
    if (response.data && response.data.data && Array.isArray(response.data.data)) {
      // 处理图表数据
      const chartData = response.data.data.map(item => ({
        name: dayjs(item.time).format('MM-DD'),
        value: item.value || 0
      }))
      
      // 更新图表
      if (glucoseChart) {
        glucoseChart.setOption({
          title: {
            text: `血糖趋势（最近${glucosePeriod.value}天）`
          },
          xAxis: {
            data: chartData.map(item => item.name)
          },
          series: [{
            data: chartData.map(item => item.value)
          }]
        })
      }
    } else {
      // 如果没有数据，显示空状态
      if (glucoseChart) {
        glucoseChart.setOption({
          title: {
            text: `血糖趋势（最近${glucosePeriod.value}天）`
          },
          xAxis: {
            data: []
          },
          series: [{
            data: []
          }]
        })
      }
    }
  } catch (error) {
    console.error('加载血糖趋势失败:', error)
    ElMessage.error('加载血糖趋势失败')
  } finally {
    loadingGlucose.value = false
  }
}

// 初始化血压图表
const initPressureChart = async () => {
  await nextTick()
  if (!pressureChartRef.value) return
  
  pressureChart = echarts.init(pressureChartRef.value)
  
  const option = {
    title: {
      text: `血压趋势（最近${pressurePeriod.value}天）`,
      left: 'center',
      textStyle: {
        fontSize: 14,
        fontWeight: 'normal'
      }
    },
    tooltip: {
      trigger: 'axis',
      formatter: function (params) {
        let result = `<div>时间：${params[0].name}<br/>`
        params.forEach(item => {
          const unit = item.seriesName === '心率' ? ' bpm' : ' mmHg'
          result += `${item.marker}${item.seriesName}：${item.value}${unit}<br/>`
        })
        result += '</div>'
        return result
      }
    },
    legend: {
      data: ['收缩压', '舒张压', '心率'],
      bottom: 10
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '15%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: []
    },
    yAxis: [
      {
        type: 'value',
        name: '血压 (mmHg)',
        position: 'left',
        min: 60,
        max: 180,
        splitLine: {
          show: true,
          lineStyle: {
            color: '#E4E7ED',
            type: 'dashed'
          }
        }
      },
      {
        type: 'value',
        name: '心率 (bpm)',
        position: 'right',
        min: 50,
        max: 120,
        splitLine: {
          show: false
        }
      }
    ],
    series: [
      {
        name: '收缩压',
        type: 'line',
        yAxisIndex: 0,
        smooth: true,
        symbol: 'circle',
        symbolSize: 6,
        lineStyle: {
          width: 2,
          color: '#FF6B6B'
        },
        itemStyle: {
          color: '#FF6B6B'
        },
        data: []
      },
      {
        name: '舒张压',
        type: 'line',
        yAxisIndex: 0,
        smooth: true,
        symbol: 'circle',
        symbolSize: 6,
        lineStyle: {
          width: 2,
          color: '#4ECDC4'
        },
        itemStyle: {
          color: '#4ECDC4'
        },
        data: []
      },
      {
        name: '心率',
        type: 'line',
        yAxisIndex: 1,
        smooth: true,
        symbol: 'diamond',
        symbolSize: 6,
        lineStyle: {
          width: 2,
          color: '#45B7D1',
          type: 'dashed'
        },
        itemStyle: {
          color: '#45B7D1'
        },
        data: []
      }
    ]
  }
  
  pressureChart.setOption(option)
  
  // 窗口大小变化时重新调整图表
  window.addEventListener('resize', () => {
    pressureChart?.resize()
  })
}

// 加载血压趋势
const loadPressureTrend = async () => {
  try {
    loadingPressure.value = true
    
    // 获取血压趋势数据
    const response = await healthApi.getHealthDataTrend('pressure', parseInt(pressurePeriod.value))
    
    if (response.data && response.data.data && Array.isArray(response.data.data)) {
      // 处理图表数据
      const chartData = response.data.data.map(item => ({
        name: dayjs(item.time).format('MM-DD'),
        systolic: item.systolic || 0,
        diastolic: item.diastolic || 0,
        heartRate: item.heartRate || 0
      }))
      
      // 更新图表
      if (pressureChart) {
        pressureChart.setOption({
          title: {
            text: `血压趋势（最近${pressurePeriod.value}天）`
          },
          xAxis: {
            data: chartData.map(item => item.name)
          },
          series: [
            {
              data: chartData.map(item => item.systolic)
            },
            {
              data: chartData.map(item => item.diastolic)
            },
            {
              data: chartData.map(item => item.heartRate)
            }
          ]
        })
      }
    } else {
      // 如果没有数据，显示空状态
      if (pressureChart) {
        pressureChart.setOption({
          title: {
            text: `血压趋势（最近${pressurePeriod.value}天）`
          },
          xAxis: {
            data: []
          },
          series: [
            { data: [] },
            { data: [] },
            { data: [] }
          ]
        })
      }
    }
  } catch (error) {
    console.error('加载血压趋势失败:', error)
    ElMessage.error('加载血压趋势失败')
  } finally {
    loadingPressure.value = false
  }
}

// 获取血糖水平标签
const getGlucoseLevel = (value) => {
  if (value < 3.9) return '低血糖'
  if (value < 6.1) return '正常'
  if (value < 7.8) return '餐后正常'
  if (value < 11.1) return '偏高'
  return '高血糖'
}

// 导出数据
const exportData = () => {
  ElMessage.info('健康报告导出功能开发中...')
}

onMounted(async () => {
  // 加载统计数据
  loadGlucoseStats()
  loadPressureStats()
  loadWeightStats()
  
  // 初始化图表
  await initGlucoseChart()
  await initPressureChart()
  
  // 加载图表数据
  loadGlucoseTrend()
  loadPressureTrend()
})
</script>

<style scoped>
.health-statistics-page {
  padding: 0;
}

.overview-section {
  margin-bottom: 24px;
}

.overview-card {
  height: 200px;
  transition: all 0.3s ease;
}

.overview-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.12);
}

.glucose-card {
  border-left: 4px solid #409EFF;
}

.pressure-card {
  border-left: 4px solid #67C23A;
}

.weight-card {
  border-left: 4px solid #909399;
}

.card-content {
  height: 100%;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.card-header h3 {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.card-icon {
  font-size: 24px;
  color: #409EFF;
}

.stats-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
  margin-bottom: 16px;
}

.stat-item {
  text-align: center;
}

.stat-value {
  font-size: 20px;
  font-weight: bold;
  color: #303133;
  line-height: 1;
  margin-bottom: 4px;
}

.stat-label {
  font-size: 12px;
  color: #909399;
}

.trend-section {
  margin-bottom: 24px;
}

.card-header-inline {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.chart-container {
  height: 300px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.trend-chart {
  width: 100%;
  height: 100%;
}

.summary-section {
  margin-bottom: 24px;
}

.summary-content {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.summary-item {
  padding: 16px;
  background: #f8f9fa;
  border-radius: 8px;
  border-left: 4px solid #409EFF;
}

.summary-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 8px;
}

.summary-desc {
  color: #606266;
  line-height: 1.6;
}

.quick-actions {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.action-btn {
  width: 100%;
  justify-content: flex-start;
}

@media (max-width: 768px) {
  .overview-section :deep(.el-col) {
    margin-bottom: 16px;
  }
  
  .trend-section :deep(.el-col) {
    margin-bottom: 16px;
  }
  
  .summary-section :deep(.el-col) {
    margin-bottom: 16px;
  }
  
  .overview-card {
    height: auto;
    min-height: 180px;
  }
  
  .stats-grid {
    grid-template-columns: 1fr;
    gap: 12px;
  }
  
  .chart-container {
    height: 250px;
  }
}</style> 