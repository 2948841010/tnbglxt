<template>
  <div class="dashboard-page">
    <!-- 欢迎区域 -->
    <div class="welcome-section">
      <div class="welcome-content">
        <h1>欢迎回来，{{ userStore.userName }}！</h1>
        <p>{{ currentDate }}，祝您身体健康！</p>
      </div>
      <div class="welcome-illustration">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" width="80" height="80">
          <path d="M4.318 6.318a4.5 4.5 0 000 6.364L12 20.364l7.682-7.682a4.5 4.5 0 00-6.364-6.364L12 7.636l-1.318-1.318a4.5 4.5 0 00-6.364 0z"/>
        </svg>
      </div>
    </div>

    <!-- 统计卡片 -->
    <div class="stats-grid">
      <div class="stat-card" v-for="stat in stats" :key="stat.title" :class="stat.class">
        <div class="stat-icon">
          <component :is="stat.icon" />
        </div>
        <div class="stat-info">
          <span class="stat-title">{{ stat.title }}</span>
          <span class="stat-value">{{ stat.value }}</span>
        </div>
      </div>
    </div>

    <!-- 快捷操作 -->
    <div class="section-card">
      <div class="section-header">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="20" height="20">
          <path d="M13 10V3L4 14h7v7l9-11h-7z"/>
        </svg>
        <h3>快捷操作</h3>
      </div>
      <div class="quick-actions">
        <button class="action-btn glucose" @click="$router.push('/health/glucose')">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="24" height="24">
            <path d="M12 9v3m0 0v3m0-3h3m-3 0H9m12 0a9 9 0 11-18 0 9 9 0 0118 0z"/>
          </svg>
          <span>记录血糖</span>
        </button>
        <button class="action-btn pressure" @click="$router.push('/health/pressure')">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="24" height="24">
            <path d="M12 9v3m0 0v3m0-3h3m-3 0H9m12 0a9 9 0 11-18 0 9 9 0 0118 0z"/>
          </svg>
          <span>记录血压</span>
        </button>
        <button class="action-btn weight" @click="$router.push('/health/weight')">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="24" height="24">
            <path d="M12 9v3m0 0v3m0-3h3m-3 0H9m12 0a9 9 0 11-18 0 9 9 0 0118 0z"/>
          </svg>
          <span>记录体重</span>
        </button>
        <button class="action-btn refresh" @click="loadDashboardData">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="24" height="24">
            <path d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15"/>
          </svg>
          <span>刷新数据</span>
        </button>
      </div>
    </div>

    <!-- 最近活动 -->
    <div class="activity-grid">
      <div class="section-card">
        <div class="section-header">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="20" height="20">
            <path d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z"/>
          </svg>
          <h3>最近记录</h3>
        </div>
        <div class="records-list" v-if="recentRecords.length > 0">
          <div class="record-item" v-for="record in recentRecords" :key="record.id">
            <div class="record-type">{{ record.type }}</div>
            <div class="record-value">{{ record.value }}</div>
            <div class="record-time">{{ record.time }}</div>
          </div>
        </div>
        <div class="empty-state" v-else>
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" width="48" height="48">
            <path d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z"/>
          </svg>
          <p>暂无记录</p>
        </div>
      </div>
      
      <div class="section-card">
        <div class="section-header">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="20" height="20">
            <path d="M15 17h5l-1.405-1.405A2.032 2.032 0 0118 14.158V11a6.002 6.002 0 00-4-5.659V5a2 2 0 10-4 0v.341C7.67 6.165 6 8.388 6 11v3.159c0 .538-.214 1.055-.595 1.436L4 17h5m6 0v1a3 3 0 11-6 0v-1m6 0H9"/>
          </svg>
          <h3>健康提醒</h3>
        </div>
        <div class="tips-list" v-if="healthTips.length > 0">
          <div class="tip-item" v-for="tip in healthTips" :key="tip.id" :class="tip.level">
            <span class="tip-badge">{{ tip.title }}</span>
            <p>{{ tip.content }}</p>
          </div>
        </div>
        <div class="empty-state" v-else>
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" width="48" height="48">
            <path d="M15 17h5l-1.405-1.405A2.032 2.032 0 0118 14.158V11a6.002 6.002 0 00-4-5.659V5a2 2 0 10-4 0v.341C7.67 6.165 6 8.388 6 11v3.159c0 .538-.214 1.055-.595 1.436L4 17h5m6 0v1a3 3 0 11-6 0v-1m6 0H9"/>
          </svg>
          <p>暂无提醒</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import dayjs from 'dayjs'
import * as healthApi from '@/api/health'
import { Document, DataAnalysis, Odometer, Bell } from '@element-plus/icons-vue'

const router = useRouter()
const userStore = useUserStore()

const currentDate = computed(() => dayjs().format('YYYY年MM月DD日'))

const stats = ref([
  { title: '健康记录', value: '0', icon: Document, class: 'primary' },
  { title: '血糖平均值', value: '--', icon: DataAnalysis, class: 'success' },
  { title: '血压记录', value: '0', icon: Odometer, class: 'warning' },
  { title: '服药提醒', value: '0', icon: Bell, class: 'danger' }
])

const recentRecords = ref([])
const healthTips = ref([])

const loadDashboardData = async () => {
  try {
    const statisticsRes = await healthApi.getHealthStatistics()
    const statisticsData = statisticsRes.data || {}
    const totalRecords = statisticsData.totalRecords || 0
    const glucoseData = statisticsData.glucose || {}
    const avgGlucose = glucoseData.avgValue ? `${glucoseData.avgValue} mmol/L` : '--'
    const pressureData = statisticsData.pressure || {}
    const pressureCount = pressureData.totalCount || 0

    stats.value = [
      { title: '健康记录', value: totalRecords.toString(), icon: Document, class: 'primary' },
      { title: '血糖平均值', value: avgGlucose, icon: DataAnalysis, class: 'success' },
      { title: '血压记录', value: pressureCount.toString(), icon: Odometer, class: 'warning' },
      { title: '服药提醒', value: '0', icon: Bell, class: 'danger' }
    ]

    await loadRecentRecords()
    generateHealthTips(statisticsData)
  } catch (error) {
    console.error('加载数据失败:', error)
  }
}

const loadRecentRecords = async () => {
  try {
    const records = []
    try {
      const glucoseRes = await healthApi.getBloodGlucoseRecords({ current: 1, size: 2 })
      if (glucoseRes.data?.records) {
        glucoseRes.data.records.forEach(record => {
          records.push({ id: `glucose_${record.measureTime}`, type: '血糖', value: `${record.value} mmol/L`, time: formatRecentTime(record.measureTime) })
        })
      }
    } catch (e) {}
    try {
      const pressureRes = await healthApi.getBloodPressureRecords({ current: 1, size: 2 })
      if (pressureRes.data?.records) {
        pressureRes.data.records.forEach(record => {
          records.push({ id: `pressure_${record.measureTime}`, type: '血压', value: `${record.systolic}/${record.diastolic} mmHg`, time: formatRecentTime(record.measureTime) })
        })
      }
    } catch (e) {}
    records.sort((a, b) => new Date(b.time) - new Date(a.time))
    recentRecords.value = records.slice(0, 3)
  } catch (error) { recentRecords.value = [] }
}

const generateHealthTips = (statisticsData) => {
  const tips = []
  const glucoseData = statisticsData.glucose || {}
  if (glucoseData.highCount > 0) {
    tips.push({ id: 1, title: '血糖监测', content: `您有${glucoseData.highCount}条偏高的血糖记录，建议注意饮食控制。`, level: 'warning' })
  } else if (glucoseData.totalCount > 0) {
    tips.push({ id: 1, title: '血糖监测', content: '血糖控制良好，请继续保持规律监测。', level: 'info' })
  }
  const pressureData = statisticsData.pressure || {}
  if (pressureData.highCount > 0) {
    tips.push({ id: 2, title: '血压监测', content: `您有${pressureData.highCount}条偏高的血压记录，建议咨询医生。`, level: 'warning' })
  }
  if (statisticsData.totalRecords === 0) {
    tips.push({ id: 3, title: '开始记录', content: '开始记录您的健康数据，建立健康档案。', level: 'info' })
  }
  healthTips.value = tips
}

const formatRecentTime = (timeStr) => {
  const time = dayjs(timeStr)
  const now = dayjs()
  if (time.isSame(now, 'day')) { return `今天 ${time.format('HH:mm')}` }
  else if (time.isSame(now.subtract(1, 'day'), 'day')) { return `昨天 ${time.format('HH:mm')}` }
  else { return time.format('MM-DD HH:mm') }
}

onMounted(() => { loadDashboardData() })
</script>

<style scoped>
.dashboard-page { max-width: 1200px; margin: 0 auto; }

/* 欢迎区域 */
.welcome-section { background: linear-gradient(135deg, #0891B2 0%, #0E7490 100%); border-radius: 20px; padding: 32px; margin-bottom: 24px; display: flex; justify-content: space-between; align-items: center; color: white; }
.welcome-content h1 { font-size: 1.75rem; font-weight: 700; margin: 0 0 8px; }
.welcome-content p { font-size: 1rem; margin: 0; opacity: 0.9; }
.welcome-illustration { opacity: 0.3; }
.welcome-illustration svg { color: white; }

/* 统计卡片 */
.stats-grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: 20px; margin-bottom: 24px; }
.stat-card { background: white; border-radius: 16px; padding: 24px; display: flex; align-items: center; gap: 16px; border: 1px solid #E2E8F0; transition: all 0.3s; cursor: pointer; }
.stat-card:hover { transform: translateY(-4px); box-shadow: 0 12px 24px rgba(8, 145, 178, 0.15); border-color: #A5F3FC; }
.stat-icon { width: 56px; height: 56px; border-radius: 14px; display: flex; align-items: center; justify-content: center; font-size: 24px; color: white; }
.stat-card.primary .stat-icon { background: linear-gradient(135deg, #0891B2 0%, #22D3EE 100%); }
.stat-card.success .stat-icon { background: linear-gradient(135deg, #059669 0%, #10B981 100%); }
.stat-card.warning .stat-icon { background: linear-gradient(135deg, #D97706 0%, #F59E0B 100%); }
.stat-card.danger .stat-icon { background: linear-gradient(135deg, #DC2626 0%, #EF4444 100%); }
.stat-info { display: flex; flex-direction: column; }
.stat-title { font-size: 0.875rem; color: #64748B; margin-bottom: 4px; }
.stat-value { font-size: 1.5rem; font-weight: 700; color: #164E63; }

/* 区块卡片 */
.section-card { background: white; border-radius: 16px; padding: 24px; border: 1px solid #E2E8F0; margin-bottom: 24px; }
.section-header { display: flex; align-items: center; gap: 10px; margin-bottom: 20px; padding-bottom: 16px; border-bottom: 1px solid #E2E8F0; }
.section-header svg { color: #0891B2; }
.section-header h3 { font-size: 1.1rem; font-weight: 600; color: #164E63; margin: 0; }

/* 快捷操作 */
.quick-actions { display: grid; grid-template-columns: repeat(4, 1fr); gap: 16px; }
.action-btn { display: flex; flex-direction: column; align-items: center; gap: 10px; padding: 20px; border: 2px solid #E2E8F0; border-radius: 14px; background: white; cursor: pointer; transition: all 0.2s; font-size: 0.9rem; font-weight: 500; color: #164E63; }
.action-btn:hover { transform: translateY(-2px); }
.action-btn.glucose { border-color: #A5F3FC; }
.action-btn.glucose:hover { background: #ECFEFF; color: #0891B2; }
.action-btn.glucose svg { color: #0891B2; }
.action-btn.pressure { border-color: #BBF7D0; }
.action-btn.pressure:hover { background: #F0FDF4; color: #059669; }
.action-btn.pressure svg { color: #059669; }
.action-btn.weight { border-color: #FED7AA; }
.action-btn.weight:hover { background: #FFF7ED; color: #D97706; }
.action-btn.weight svg { color: #D97706; }
.action-btn.refresh { border-color: #E2E8F0; }
.action-btn.refresh:hover { background: #F8FAFC; color: #64748B; }
.action-btn.refresh svg { color: #64748B; }

/* 活动网格 */
.activity-grid { display: grid; grid-template-columns: repeat(2, 1fr); gap: 24px; }

/* 记录列表 */
.records-list { display: flex; flex-direction: column; gap: 12px; }
.record-item { display: flex; align-items: center; justify-content: space-between; padding: 14px 16px; background: #F8FAFC; border-radius: 12px; transition: background 0.2s; }
.record-item:hover { background: #F0FDFA; }
.record-type { font-weight: 600; color: #164E63; font-size: 0.9rem; }
.record-value { font-weight: 600; color: #0891B2; font-size: 0.95rem; }
.record-time { font-size: 0.8rem; color: #94A3B8; }

/* 提醒列表 */
.tips-list { display: flex; flex-direction: column; gap: 12px; }
.tip-item { padding: 14px 16px; border-radius: 12px; }
.tip-item.info { background: #F0FDFA; border-left: 4px solid #0891B2; }
.tip-item.warning { background: #FFF7ED; border-left: 4px solid #F59E0B; }
.tip-badge { display: inline-block; font-size: 0.8rem; font-weight: 600; margin-bottom: 6px; }
.tip-item.info .tip-badge { color: #0891B2; }
.tip-item.warning .tip-badge { color: #D97706; }
.tip-item p { margin: 0; font-size: 0.875rem; color: #64748B; line-height: 1.5; }

/* 空状态 */
.empty-state { text-align: center; padding: 40px 20px; color: #94A3B8; }
.empty-state svg { margin-bottom: 12px; }
.empty-state p { margin: 0; font-size: 0.9rem; }

/* 响应式 */
@media (max-width: 1024px) { .stats-grid { grid-template-columns: repeat(2, 1fr); } .quick-actions { grid-template-columns: repeat(2, 1fr); } }
@media (max-width: 768px) { .stats-grid { grid-template-columns: 1fr; } .quick-actions { grid-template-columns: 1fr; } .activity-grid { grid-template-columns: 1fr; } .welcome-illustration { display: none; } }
</style>
