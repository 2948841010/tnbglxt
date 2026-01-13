<template>
  <div class="dashboard-page">
    <div class="welcome-section mb-24">
      <h1>欢迎回来，{{ userStore.userName }}！</h1>
      <p>今天是 {{ currentDate }}，祝您身体健康！</p>
    </div>

    <!-- 统计卡片 -->
    <el-row :gutter="24" class="mb-24">
      <el-col :xs="24" :sm="12" :md="6" v-for="stat in stats" :key="stat.title">
        <div class="stat-card">
          <div class="stat-card-icon" :style="{ backgroundColor: stat.color }">
            <el-icon>
              <component :is="stat.icon" />
            </el-icon>
          </div>
          <div class="stat-card-title">{{ stat.title }}</div>
          <div class="stat-card-value">{{ stat.value }}</div>
        </div>
      </el-col>
    </el-row>

    <!-- 快捷操作 -->
    <el-card class="mb-24" header="快捷操作">
      <el-row :gutter="16">
        <el-col :xs="12" :sm="8" :md="6" v-for="action in quickActions" :key="action.title">
          <el-button
            class="quick-action-btn"
            @click="action.handler"
            :type="action.type"
            :icon="action.icon"
          >
            {{ action.title }}
          </el-button>
        </el-col>
      </el-row>
    </el-card>

    <!-- 最近活动 -->
    <el-row :gutter="24">
      <el-col :xs="24" :md="12">
        <el-card header="最近记录">
          <div class="recent-records">
            <div class="empty-state" v-if="recentRecords.length === 0">
              <el-icon class="empty-state-icon"><DocumentRemove /></el-icon>
              <p>暂无记录</p>
            </div>
            <div v-else>
              <div
                v-for="record in recentRecords"
                :key="record.id"
                class="record-item"
              >
                <div class="record-type">{{ record.type }}</div>
                <div class="record-value">{{ record.value }}</div>
                <div class="record-time">{{ record.time }}</div>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :xs="24" :md="12">
        <el-card header="健康提醒">
          <div class="health-tips">
            <div class="empty-state" v-if="healthTips.length === 0">
              <el-icon class="empty-state-icon"><Bell /></el-icon>
              <p>暂无提醒</p>
            </div>
            <div v-else>
              <div
                v-for="tip in healthTips"
                :key="tip.id"
                class="tip-item"
              >
                <el-tag :type="tip.level === 'high' ? 'danger' : 'info'">
                  {{ tip.title }}
                </el-tag>
                <p>{{ tip.content }}</p>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import dayjs from 'dayjs'
import * as healthApi from '@/api/health'
import {
  DocumentRemove,
  Bell,
  Plus,
  Refresh,
  Document,
  DataAnalysis,
  Odometer
} from '@element-plus/icons-vue'

const router = useRouter()
const userStore = useUserStore()

// 当前日期
const currentDate = computed(() => {
  return dayjs().format('YYYY年MM月DD日')
})

// 统计数据
const stats = ref([
  {
    title: '健康记录',
    value: '0',
    icon: Document,
    color: '#409EFF'
  },
  {
    title: '血糖平均值',
    value: '--',
    icon: DataAnalysis,
    color: '#67C23A'
  },
  {
    title: '血压记录',
    value: '0',
    icon: Odometer,
    color: '#E6A23C'
  },
  {
    title: '服药提醒',
    value: '0',
    icon: Bell,
    color: '#F56C6C'
  }
])

// 快捷操作
const quickActions = ref([
  {
    title: '记录血糖',
    icon: Plus,
    type: 'primary',
    handler: () => router.push('/health/glucose')
  },
  {
    title: '记录血压',
    icon: Plus,
    type: 'success',
    handler: () => router.push('/health/pressure')
  },
  {
    title: '记录体重',
    icon: Plus,
    type: 'warning',
    handler: () => router.push('/health/weight')
  },
  {
    title: '刷新数据',
    icon: Refresh,
    type: 'info',
    handler: () => loadDashboardData()
  }
])

// 最近记录
const recentRecords = ref([])

// 健康提醒
const healthTips = ref([])

// 加载仪表板数据
const loadDashboardData = async () => {
  try {
    // 获取健康统计数据
    const statisticsRes = await healthApi.getHealthStatistics()
    const statisticsData = statisticsRes.data || {}

    // 计算总记录数
    const totalRecords = statisticsData.totalRecords || 0
    
    // 血糖数据
    const glucoseData = statisticsData.glucose || {}
    const avgGlucose = glucoseData.avgValue ? `${glucoseData.avgValue} mmol/L` : '--'
    
    // 血压数据
    const pressureData = statisticsData.pressure || {}
    const pressureCount = pressureData.totalCount || 0

    stats.value = [
      {
        title: '健康记录',
        value: totalRecords.toString(),
        icon: Document,
        color: '#409EFF'
      },
      {
        title: '血糖平均值',
        value: avgGlucose,
        icon: DataAnalysis,
        color: '#67C23A'
      },
      {
        title: '血压记录',
        value: pressureCount.toString(),
        icon: Odometer,
        color: '#E6A23C'
      },
      {
        title: '服药提醒',
        value: '0', // 暂时保留模拟数据，后续可扩展
        icon: Bell,
        color: '#F56C6C'
      }
    ]

    // 获取最近记录
    await loadRecentRecords()

    // 生成健康提醒
    generateHealthTips(statisticsData)

  } catch (error) {
    console.error('加载数据失败:', error)
    // 失败时显示默认数据
    stats.value = [
      { title: '健康记录', value: '0', icon: Document, color: '#409EFF' },
      { title: '血糖平均值', value: '--', icon: DataAnalysis, color: '#67C23A' },
      { title: '血压记录', value: '0', icon: Odometer, color: '#E6A23C' },
      { title: '服药提醒', value: '0', icon: Bell, color: '#F56C6C' }
    ]
  }
}

// 加载最近记录
const loadRecentRecords = async () => {
  try {
    const records = []
    
    // 获取最近的血糖记录
    try {
      const glucoseRes = await healthApi.getBloodGlucoseRecords({ current: 1, size: 2 })
      if (glucoseRes.data && glucoseRes.data.records) {
        glucoseRes.data.records.forEach(record => {
          records.push({
            id: `glucose_${record.measureTime}`,
            type: '血糖',
            value: `${record.value} mmol/L`,
            time: formatRecentTime(record.measureTime)
          })
        })
      }
    } catch (error) {
      console.warn('获取血糖记录失败:', error)
    }

    // 获取最近的血压记录
    try {
      const pressureRes = await healthApi.getBloodPressureRecords({ current: 1, size: 2 })
      if (pressureRes.data && pressureRes.data.records) {
        pressureRes.data.records.forEach(record => {
          records.push({
            id: `pressure_${record.measureTime}`,
            type: '血压',
            value: `${record.systolic}/${record.diastolic} mmHg`,
            time: formatRecentTime(record.measureTime)
          })
        })
      }
    } catch (error) {
      console.warn('获取血压记录失败:', error)
    }

    // 获取最近的体重记录
    try {
      const weightRes = await healthApi.getWeightRecords({ current: 1, size: 2 })
      if (weightRes.data && weightRes.data.records) {
        weightRes.data.records.forEach(record => {
          records.push({
            id: `weight_${record.measureTime}`,
            type: '体重',
            value: `${record.weight} kg`,
            time: formatRecentTime(record.measureTime)
          })
        })
      }
    } catch (error) {
      console.warn('获取体重记录失败:', error)
    }

    // 按时间排序，最新的在前面
    records.sort((a, b) => new Date(b.time) - new Date(a.time))
    recentRecords.value = records.slice(0, 3) // 只显示最近3条

  } catch (error) {
    console.error('加载最近记录失败:', error)
    recentRecords.value = []
  }
}

// 生成健康提醒
const generateHealthTips = (statisticsData) => {
  const tips = []
  
  // 血糖相关提醒
  const glucoseData = statisticsData.glucose || {}
  if (glucoseData.highCount > 0) {
    tips.push({
      id: 1,
      title: '血糖监测',
      content: `您有${glucoseData.highCount}条偏高的血糖记录，建议注意饮食控制，按时服药。`,
      level: 'warning'
    })
  } else if (glucoseData.totalCount > 0) {
    tips.push({
      id: 1,
      title: '血糖监测',
      content: '血糖控制良好，请继续保持规律监测。',
      level: 'info'
    })
  }

  // 血压相关提醒
  const pressureData = statisticsData.pressure || {}
  if (pressureData.highCount > 0) {
    tips.push({
      id: 2,
      title: '血压监测',
      content: `您有${pressureData.highCount}条偏高的血压记录，建议咨询医生并注意休息。`,
      level: 'warning'
    })
  }

  // 记录数量提醒
  if (statisticsData.totalRecords === 0) {
    tips.push({
      id: 3,
      title: '开始记录',
      content: '开始记录您的健康数据，建立健康档案。',
      level: 'info'
    })
  }

  healthTips.value = tips
}

// 格式化最近记录的时间显示
const formatRecentTime = (timeStr) => {
  const time = dayjs(timeStr)
  const now = dayjs()
  
  if (time.isSame(now, 'day')) {
    return `今天 ${time.format('HH:mm')}`
  } else if (time.isSame(now.subtract(1, 'day'), 'day')) {
    return `昨天 ${time.format('HH:mm')}`
  } else {
    return time.format('MM-DD HH:mm')
  }
}

onMounted(() => {
  loadDashboardData()
})
</script>

<style scoped>
.dashboard-page {
  padding: 0;
}

.mb-24 {
  margin-bottom: 24px;
}

.welcome-section h1 {
  margin: 0 0 8px 0;
  font-size: 24px;
  color: #333;
}

.welcome-section p {
  margin: 0;
  color: #666;
  font-size: 14px;
}

.stat-card {
  background: white;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  transition: transform 0.2s, box-shadow 0.2s;
}

.stat-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.15);
}

.stat-card-icon {
  width: 48px;
  height: 48px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 16px;
  color: white;
}

.stat-card-title {
  font-size: 14px;
  color: #666;
  margin-bottom: 8px;
}

.stat-card-value {
  font-size: 24px;
  font-weight: 600;
  color: #333;
}

.quick-action-btn {
  width: 100%;
  height: 60px;
  margin-bottom: 12px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 4px;
}

.recent-records,
.health-tips {
  max-height: 300px;
  overflow-y: auto;
}

.empty-state {
  text-align: center;
  padding: 40px 20px;
  color: #999;
}

.empty-state-icon {
  font-size: 48px;
  margin-bottom: 16px;
}

.record-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 0;
  border-bottom: 1px solid #f0f0f0;
}

.record-item:last-child {
  border-bottom: none;
}

.record-type {
  font-weight: 500;
  color: #333;
}

.record-value {
  color: #409EFF;
  font-weight: 600;
}

.record-time {
  font-size: 12px;
  color: #999;
}

.tip-item {
  margin-bottom: 16px;
}

.tip-item:last-child {
  margin-bottom: 0;
}

.tip-item p {
  margin: 8px 0 0 0;
  font-size: 14px;
  color: #666;
  line-height: 1.4;
}

@media (max-width: 768px) {
  .welcome-section h1 {
    font-size: 20px;
  }
  
  .stat-card {
    margin-bottom: 16px;
  }
  
  .quick-action-btn {
    height: 50px;
  }
}
</style> 