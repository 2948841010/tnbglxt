<template>
  <div class="health-statistics-page">
    <!-- 页面标题 -->
    <div class="page-header">
      <div class="header-content">
        <div class="header-icon">
          <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke-width="1.5" stroke="currentColor" class="icon">
            <path stroke-linecap="round" stroke-linejoin="round" d="M3 13.125C3 12.504 3.504 12 4.125 12h2.25c.621 0 1.125.504 1.125 1.125v6.75C7.5 20.496 6.996 21 6.375 21h-2.25A1.125 1.125 0 0 1 3 19.875v-6.75ZM9.75 8.625c0-.621.504-1.125 1.125-1.125h2.25c.621 0 1.125.504 1.125 1.125v11.25c0 .621-.504 1.125-1.125 1.125h-2.25a1.125 1.125 0 0 1-1.125-1.125V8.625ZM16.5 4.125c0-.621.504-1.125 1.125-1.125h2.25C20.496 3 21 3.504 21 4.125v15.75c0 .621-.504 1.125-1.125 1.125h-2.25a1.125 1.125 0 0 1-1.125-1.125V4.125Z" />
          </svg>
        </div>
        <div class="header-text">
          <h1 class="page-title">健康数据统计</h1>
          <p class="page-subtitle">全面了解您的健康状况，科学管理健康生活</p>
        </div>
      </div>
    </div>

    <!-- 综合统计卡片 -->
    <el-row :gutter="16" class="overview-section">
      <el-col :xs="24" :sm="12" :md="8">
        <div class="overview-card glucose-card" @click="$router.push('/health/glucose')">
          <div class="card-content">
            <div class="card-header">
              <h3>血糖管理</h3>
              <div class="card-icon">
                <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke-width="1.5" stroke="currentColor">
                  <path stroke-linecap="round" stroke-linejoin="round" d="M9.75 3.104v5.714a2.25 2.25 0 0 1-.659 1.591L5 14.5M9.75 3.104c-.251.023-.501.05-.75.082m.75-.082a24.301 24.301 0 0 1 4.5 0m0 0v5.714c0 .597.237 1.17.659 1.591L19.8 15.3M14.25 3.104c.251.023.501.05.75.082M19.8 15.3l-1.57.393A9.065 9.065 0 0 1 12 15a9.065 9.065 0 0 0-6.23-.693L5 14.5m14.8.8 1.402 1.402c1.232 1.232.65 3.318-1.067 3.611A48.309 48.309 0 0 1 12 21c-2.773 0-5.491-.235-8.135-.687-1.718-.293-2.3-2.379-1.067-3.61L5 14.5" />
                </svg>
              </div>
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
            <el-button type="primary" size="small" class="detail-btn">查看详情</el-button>
          </div>
        </div>
      </el-col>
      
      <el-col :xs="24" :sm="12" :md="8">
        <div class="overview-card pressure-card" @click="$router.push('/health/pressure')">
          <div class="card-content">
            <div class="card-header">
              <h3>血压管理</h3>
              <div class="card-icon pressure-icon">
                <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke-width="1.5" stroke="currentColor">
                  <path stroke-linecap="round" stroke-linejoin="round" d="M21 8.25c0-2.485-2.099-4.5-4.688-4.5-1.935 0-3.597 1.126-4.312 2.733-.715-1.607-2.377-2.733-4.313-2.733C5.1 3.75 3 5.765 3 8.25c0 7.22 9 12 9 12s9-4.78 9-12Z" />
                </svg>
              </div>
            </div>
            <div class="stats-grid">
              <div class="stat-item">
                <div class="stat-value">{{ pressureStats.avgSystolic ? `${pressureStats.avgSystolic}/${pressureStats.avgDiastolic}` : '--' }}</div>
                <div class="stat-label">平均血压 (mmHg)</div>
              </div>
              <div class="stat-item">
                <div class="stat-value">{{ pressureStats.normalCount || 0 }}</div>
                <div class="stat-label">正常记录</div>
              </div>
            </div>
            <el-button type="success" size="small" class="detail-btn success-btn">查看详情</el-button>
          </div>
        </div>
      </el-col>
      
      <el-col :xs="24" :sm="12" :md="8">
        <div class="overview-card weight-card" @click="$router.push('/health/weight')">
          <div class="card-content">
            <div class="card-header">
              <h3>体重管理</h3>
              <div class="card-icon weight-icon">
                <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke-width="1.5" stroke="currentColor">
                  <path stroke-linecap="round" stroke-linejoin="round" d="M12 3v17.25m0 0c-1.472 0-2.882.265-4.185.75M12 20.25c1.472 0 2.882.265 4.185.75M18.75 4.97A48.416 48.416 0 0 0 12 4.5c-2.291 0-4.545.16-6.75.47m13.5 0c1.01.143 2.01.317 3 .52m-3-.52 2.62 10.726c.122.499-.106 1.028-.589 1.202a5.988 5.988 0 0 1-2.031.352 5.988 5.988 0 0 1-2.031-.352c-.483-.174-.711-.703-.59-1.202L18.75 4.971Zm-16.5.52c.99-.203 1.99-.377 3-.52m0 0 2.62 10.726c.122.499-.106 1.028-.589 1.202a5.989 5.989 0 0 1-2.031.352 5.989 5.989 0 0 1-2.031-.352c-.483-.174-.711-.703-.59-1.202L5.25 4.971Z" />
                </svg>
              </div>
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
            <el-button size="small" class="detail-btn info-btn">查看详情</el-button>
          </div>
        </div>
      </el-col>
    </el-row>

    <!-- 趋势分析 -->
    <el-row :gutter="16" class="trend-section">
      <el-col :xs="24" :md="12">
        <el-card class="chart-card">
          <template #header>
            <div class="card-header-inline">
              <span class="chart-title">血糖趋势</span>
              <el-select v-model="glucosePeriod" @change="loadGlucoseTrend" size="small" class="period-select">
                <el-option label="7天" value="7" />
                <el-option label="30天" value="30" />
                <el-option label="90天" value="90" />
              </el-select>
            </div>
          </template>
          <div class="chart-container" v-loading="loadingGlucose">
            <div ref="glucoseChartRef" class="trend-chart" style="width: 100%; height: 280px;"></div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :xs="24" :md="12">
        <el-card class="chart-card">
          <template #header>
            <div class="card-header-inline">
              <span class="chart-title">血压趋势</span>
              <el-select v-model="pressurePeriod" @change="loadPressureTrend" size="small" class="period-select">
                <el-option label="7天" value="7" />
                <el-option label="30天" value="30" />
                <el-option label="90天" value="90" />
              </el-select>
            </div>
          </template>
          <div class="chart-container" v-loading="loadingPressure">
            <div ref="pressureChartRef" class="trend-chart" style="width: 100%; height: 280px;"></div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 健康总结 -->
    <el-row :gutter="16" class="summary-section">
      <el-col :xs="24" :md="16">
        <el-card class="summary-card">
          <template #header>
            <span class="chart-title">健康总结</span>
          </template>
          <div class="summary-content">
            <div class="summary-item">
              <div class="summary-title">
                <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke-width="1.5" stroke="currentColor" class="summary-icon success">
                  <path stroke-linecap="round" stroke-linejoin="round" d="M9 12.75 11.25 15 15 9.75M21 12a9 9 0 1 1-18 0 9 9 0 0 1 18 0Z" />
                </svg>
                整体健康状况
              </div>
              <div class="summary-desc">基于您的健康记录，目前血糖控制良好，血压保持在正常范围内。建议继续保持健康的生活方式。</div>
            </div>
            
            <div class="summary-item warning">
              <div class="summary-title">
                <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke-width="1.5" stroke="currentColor" class="summary-icon warning">
                  <path stroke-linecap="round" stroke-linejoin="round" d="M12 9v3.75m-9.303 3.376c-.866 1.5.217 3.374 1.948 3.374h14.71c1.73 0 2.813-1.874 1.948-3.374L13.949 3.378c-.866-1.5-3.032-1.5-3.898 0L2.697 16.126ZM12 15.75h.007v.008H12v-.008Z" />
                </svg>
                注意事项
              </div>
              <div class="summary-desc">近期血糖波动较大，建议注意饮食控制，按时服药，如有异常及时咨询医生。</div>
            </div>
            
            <div class="summary-item info">
              <div class="summary-title">
                <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke-width="1.5" stroke="currentColor" class="summary-icon info">
                  <path stroke-linecap="round" stroke-linejoin="round" d="M11.48 3.499a.562.562 0 0 1 1.04 0l2.125 5.111a.563.563 0 0 0 .475.345l5.518.442c.499.04.701.663.321.988l-4.204 3.602a.563.563 0 0 0-.182.557l1.285 5.385a.562.562 0 0 1-.84.61l-4.725-2.885a.562.562 0 0 0-.586 0L6.982 20.54a.562.562 0 0 1-.84-.61l1.285-5.386a.562.562 0 0 0-.182-.557l-4.204-3.602a.562.562 0 0 1 .321-.988l5.518-.442a.563.563 0 0 0 .475-.345L11.48 3.5Z" />
                </svg>
                建议
              </div>
              <div class="summary-desc">1. 保持规律的血糖监测<br>2. 合理控制饮食，少食多餐<br>3. 坚持适量运动<br>4. 按时服药，定期复查</div>
            </div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :xs="24" :md="8">
        <el-card class="actions-card">
          <template #header>
            <span class="chart-title">快速操作</span>
          </template>
          <div class="quick-actions">
            <el-button type="primary" class="action-btn" @click="$router.push('/health/glucose')">
              <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke-width="1.5" stroke="currentColor" class="btn-icon">
                <path stroke-linecap="round" stroke-linejoin="round" d="M12 4.5v15m7.5-7.5h-15" />
              </svg>
              添加血糖记录
            </el-button>
            <el-button type="success" class="action-btn success-btn" @click="$router.push('/health/pressure')">
              <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke-width="1.5" stroke="currentColor" class="btn-icon">
                <path stroke-linecap="round" stroke-linejoin="round" d="M12 4.5v15m7.5-7.5h-15" />
              </svg>
              添加血压记录
            </el-button>
            <el-button class="action-btn info-btn" @click="$router.push('/health/weight')">
              <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke-width="1.5" stroke="currentColor" class="btn-icon">
                <path stroke-linecap="round" stroke-linejoin="round" d="M12 4.5v15m7.5-7.5h-15" />
              </svg>
              添加体重记录
            </el-button>
            <el-button class="action-btn export-btn" @click="exportData">
              <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke-width="1.5" stroke="currentColor" class="btn-icon">
                <path stroke-linecap="round" stroke-linejoin="round" d="M19.5 14.25v-2.625a3.375 3.375 0 0 0-3.375-3.375h-1.5A1.125 1.125 0 0 1 13.5 7.125v-1.5a3.375 3.375 0 0 0-3.375-3.375H8.25m0 12.75h7.5m-7.5 3H12M10.5 2.25H5.625c-.621 0-1.125.504-1.125 1.125v17.25c0 .621.504 1.125 1.125 1.125h12.75c.621 0 1.125-.504 1.125-1.125V11.25a9 9 0 0 0-9-9Z" />
              </svg>
              导出健康报告
            </el-button>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import dayjs from 'dayjs'
import * as echarts from 'echarts'
import * as healthApi from '@/api/health'

const loadingGlucose = ref(false)
const loadingPressure = ref(false)
const glucosePeriod = ref('30')
const pressurePeriod = ref('30')

const glucoseStats = ref({})
const pressureStats = ref({})
const weightStats = ref({})

const glucoseChartRef = ref(null)
const pressureChartRef = ref(null)
let glucoseChart = null
let pressureChart = null

const loadGlucoseStats = async () => {
  try {
    const response = await healthApi.getBloodGlucoseRecords({ current: 1, size: 1000 })
    if (response.data && response.data.statistics) glucoseStats.value = response.data.statistics
    else glucoseStats.value = { avgValue: 0, normalCount: 0, highCount: 0, lowCount: 0 }
  } catch (error) { console.error('加载血糖统计失败:', error); glucoseStats.value = { avgValue: 0, normalCount: 0, highCount: 0, lowCount: 0 } }
}

const loadPressureStats = async () => {
  try {
    const response = await healthApi.getBloodPressureRecords({ current: 1, size: 1000 })
    if (response.data && response.data.statistics) pressureStats.value = response.data.statistics
    else pressureStats.value = { avgSystolic: 0, avgDiastolic: 0, normalCount: 0, highCount: 0, lowCount: 0 }
  } catch (error) { console.error('加载血压统计失败:', error); pressureStats.value = { avgSystolic: 0, avgDiastolic: 0, normalCount: 0, highCount: 0, lowCount: 0 } }
}

const loadWeightStats = async () => {
  try {
    const response = await healthApi.getWeightRecords({ current: 1, size: 1000 })
    if (response.data && response.data.statistics) weightStats.value = response.data.statistics
    else weightStats.value = { avgWeight: 0, currentBmi: 0, weightChange30Days: 0 }
  } catch (error) { console.error('加载体重统计失败:', error); weightStats.value = { avgWeight: 0, currentBmi: 0, weightChange30Days: 0 } }
}

const initGlucoseChart = async () => {
  await nextTick()
  if (!glucoseChartRef.value) return
  glucoseChart = echarts.init(glucoseChartRef.value)
  const option = {
    tooltip: { trigger: 'axis', formatter: (params) => `<div><div>时间：${params[0].name}</div><div>血糖值：${params[0].value} mmol/L</div></div>` },
    legend: { data: ['血糖值'], bottom: 10 },
    grid: { left: '3%', right: '4%', bottom: '15%', containLabel: true },
    xAxis: { type: 'category', boundaryGap: false, data: [] },
    yAxis: { type: 'value', name: 'mmol/L', min: 3, max: 15, splitLine: { show: true, lineStyle: { color: '#A5F3FC', type: 'dashed' } } },
    series: [{ name: '血糖值', type: 'line', smooth: true, symbol: 'circle', symbolSize: 6, lineStyle: { width: 2, color: '#0891B2' }, itemStyle: { color: '#0891B2' }, areaStyle: { opacity: 0.1, color: '#22D3EE' }, data: [] }]
  }
  glucoseChart.setOption(option)
  window.addEventListener('resize', () => glucoseChart?.resize())
}

const loadGlucoseTrend = async () => {
  try {
    loadingGlucose.value = true
    const response = await healthApi.getHealthDataTrend('glucose', parseInt(glucosePeriod.value))
    if (response.data && response.data.data && Array.isArray(response.data.data)) {
      const chartData = response.data.data.map(item => ({ name: dayjs(item.time).format('MM-DD'), value: item.value || 0 }))
      if (glucoseChart) glucoseChart.setOption({ xAxis: { data: chartData.map(item => item.name) }, series: [{ data: chartData.map(item => item.value) }] })
    } else { if (glucoseChart) glucoseChart.setOption({ xAxis: { data: [] }, series: [{ data: [] }] }) }
  } catch (error) { console.error('加载血糖趋势失败:', error); ElMessage.error('加载血糖趋势失败') }
  finally { loadingGlucose.value = false }
}

const initPressureChart = async () => {
  await nextTick()
  if (!pressureChartRef.value) return
  pressureChart = echarts.init(pressureChartRef.value)
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
  pressureChart.setOption(option)
  window.addEventListener('resize', () => pressureChart?.resize())
}

const loadPressureTrend = async () => {
  try {
    loadingPressure.value = true
    const response = await healthApi.getHealthDataTrend('pressure', parseInt(pressurePeriod.value))
    if (response.data && response.data.data && Array.isArray(response.data.data)) {
      const chartData = response.data.data.map(item => ({ name: dayjs(item.time).format('MM-DD'), systolic: item.systolic || 0, diastolic: item.diastolic || 0, heartRate: item.heartRate || 0 }))
      if (pressureChart) {
        pressureChart.setOption({
          xAxis: { data: chartData.map(item => item.name) },
          series: [{ data: chartData.map(item => item.systolic) }, { data: chartData.map(item => item.diastolic) }, { data: chartData.map(item => item.heartRate) }]
        })
      }
    } else { if (pressureChart) pressureChart.setOption({ xAxis: { data: [] }, series: [{ data: [] }, { data: [] }, { data: [] }] }) }
  } catch (error) { console.error('加载血压趋势失败:', error); ElMessage.error('加载血压趋势失败') }
  finally { loadingPressure.value = false }
}

const exportData = () => { ElMessage.info('健康报告导出功能开发中...') }

onMounted(async () => {
  loadGlucoseStats()
  loadPressureStats()
  loadWeightStats()
  await initGlucoseChart()
  await initPressureChart()
  loadGlucoseTrend()
  loadPressureTrend()
})
</script>

<style scoped>
.health-statistics-page { padding: 0; }

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
  background: linear-gradient(135deg, #0891B2 0%, #22D3EE 100%);
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.header-icon .icon { width: 32px; height: 32px; color: white; }
.page-title { font-size: 24px; font-weight: 700; color: #164E63; margin: 0 0 4px 0; }
.page-subtitle { font-size: 14px; color: #0891B2; margin: 0; }

.overview-section { margin-bottom: 24px; }

.overview-card {
  background: white;
  border-radius: 16px;
  padding: 20px;
  cursor: pointer;
  transition: all 0.3s ease;
  border: 1px solid #E0F2FE;
  height: 200px;
}

.overview-card:hover { transform: translateY(-4px); box-shadow: 0 12px 24px rgba(8, 145, 178, 0.15); }

.glucose-card { border-left: 4px solid #0891B2; }
.pressure-card { border-left: 4px solid #059669; }
.weight-card { border-left: 4px solid #8B5CF6; }

.card-content { height: 100%; display: flex; flex-direction: column; justify-content: space-between; }

.card-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.card-header h3 { margin: 0; font-size: 16px; font-weight: 600; color: #164E63; }

.card-icon {
  width: 40px;
  height: 40px;
  background: linear-gradient(135deg, #0891B2 0%, #22D3EE 100%);
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.card-icon svg { width: 20px; height: 20px; color: white; }
.card-icon.pressure-icon { background: linear-gradient(135deg, #059669 0%, #34D399 100%); }
.card-icon.weight-icon { background: linear-gradient(135deg, #8B5CF6 0%, #A78BFA 100%); }

.stats-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 16px; margin-bottom: 16px; }
.stat-item { text-align: center; }
.stat-value { font-size: 20px; font-weight: bold; color: #164E63; line-height: 1; margin-bottom: 4px; }
.stat-label { font-size: 12px; color: #0891B2; }

.detail-btn { background: #0891B2; border-color: #0891B2; width: 100%; }
.detail-btn:hover { background: #0E7490; border-color: #0E7490; }
.detail-btn.success-btn { background: #059669; border-color: #059669; }
.detail-btn.success-btn:hover { background: #047857; border-color: #047857; }
.detail-btn.info-btn { background: #8B5CF6; border-color: #8B5CF6; color: white; }
.detail-btn.info-btn:hover { background: #7C3AED; border-color: #7C3AED; }

.trend-section { margin-bottom: 24px; }

.chart-card { border-radius: 16px; border: 1px solid #E0F2FE; }
.card-header-inline { display: flex; justify-content: space-between; align-items: center; }
.chart-title { font-weight: 600; color: #164E63; }
.chart-container { height: 300px; display: flex; align-items: center; justify-content: center; }
.trend-chart { width: 100%; height: 100%; }

.summary-section { margin-bottom: 24px; }
.summary-card, .actions-card { border-radius: 16px; border: 1px solid #E0F2FE; }

.summary-content { display: flex; flex-direction: column; gap: 20px; }

.summary-item {
  padding: 16px;
  background: #F0FDFA;
  border-radius: 12px;
  border-left: 4px solid #0891B2;
}

.summary-item.warning { border-left-color: #F59E0B; background: #FFFBEB; }
.summary-item.info { border-left-color: #8B5CF6; background: #F5F3FF; }

.summary-title { display: flex; align-items: center; gap: 8px; font-weight: 600; color: #164E63; margin-bottom: 8px; }
.summary-icon { width: 20px; height: 20px; }
.summary-icon.success { color: #059669; }
.summary-icon.warning { color: #F59E0B; }
.summary-icon.info { color: #8B5CF6; }
.summary-desc { color: #475569; line-height: 1.6; font-size: 14px; }

.quick-actions { display: flex; flex-direction: column; gap: 12px; }

.action-btn {
  width: 100%;
  justify-content: flex-start;
  background: #0891B2;
  border-color: #0891B2;
  padding: 12px 16px;
  height: auto;
}

.action-btn:hover { background: #0E7490; border-color: #0E7490; }
.action-btn.success-btn { background: #059669; border-color: #059669; }
.action-btn.success-btn:hover { background: #047857; border-color: #047857; }
.action-btn.info-btn { background: #8B5CF6; border-color: #8B5CF6; color: white; }
.action-btn.info-btn:hover { background: #7C3AED; border-color: #7C3AED; }
.action-btn.export-btn { background: #F59E0B; border-color: #F59E0B; color: white; }
.action-btn.export-btn:hover { background: #D97706; border-color: #D97706; }

.btn-icon { width: 18px; height: 18px; margin-right: 8px; }

@media (max-width: 768px) {
  .page-header { flex-direction: column; align-items: flex-start; gap: 16px; }
  .page-title { font-size: 20px; }
  .overview-section :deep(.el-col) { margin-bottom: 16px; }
  .trend-section :deep(.el-col) { margin-bottom: 16px; }
  .summary-section :deep(.el-col) { margin-bottom: 16px; }
  .overview-card { height: auto; min-height: 180px; }
  .stats-grid { grid-template-columns: 1fr; gap: 12px; }
  .chart-container { height: 250px; }
}
</style>