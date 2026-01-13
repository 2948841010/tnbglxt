<template>
  <div class="dashboard">
    <div class="page-header">
      <h1 class="page-title">管理员数据看板</h1>
      <el-button @click="refreshData" :loading="loading" type="primary">
        <el-icon><Refresh /></el-icon>
        刷新数据
      </el-button>
    </div>
    
    <!-- 统计卡片 -->
    <el-row :gutter="20" class="stat-cards" v-loading="loading">
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-card-content">
            <div class="stat-icon user-icon">
              <el-icon><User /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-title">用户总数</div>
              <div class="stat-value">{{ stats.users?.total || 0 }}</div>
              <div class="stat-sub">活跃: {{ stats.users?.active || 0 }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-card-content">
            <div class="stat-icon doctor-icon">
              <el-icon><UserFilled /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-title">医生总数</div>
              <div class="stat-value">{{ stats.doctors?.totalDoctors || 0 }}</div>
              <div class="stat-sub">在线: {{ stats.doctors?.onlineDoctors || 0 }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-card-content">
            <div class="stat-icon consultation-icon">
              <el-icon><ChatDotRound /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-title">今日咨询</div>
              <div class="stat-value">{{ stats.consultations?.todayConsultations || 0 }}</div>
              <div class="stat-sub">已完成: {{ stats.consultations?.todayCompletedConsultations || 0 }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-card-content">
            <div class="stat-icon progress-icon">
            <el-icon><DataAnalysis /></el-icon>
          </div>
          <div class="stat-info">
              <div class="stat-title">总咨询数</div>
              <div class="stat-value">{{ stats.consultations?.totalConsultations || 0 }}</div>
              <div class="stat-sub">进行中: {{ stats.consultations?.ongoingConsultations || 0 }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 实时数据卡片 -->
    <el-row :gutter="20" class="realtime-row">
      <el-col :span="8">
        <el-card class="realtime-card">
          <div class="realtime-header">
            <el-icon class="realtime-icon online"><Connection /></el-icon>
            <span>实时在线</span>
          </div>
          <div class="realtime-stats">
            <div class="realtime-item">
              <span class="label">在线用户:</span>
              <span class="value">{{ realTimeData.onlineUsers || 0 }}</span>
            </div>
            <div class="realtime-item">
              <span class="label">在线医生:</span>
              <span class="value">{{ realTimeData.onlineDoctors || 0 }}</span>
            </div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :span="8">
        <el-card class="realtime-card">
          <div class="realtime-header">
            <el-icon class="realtime-icon today"><Calendar /></el-icon>
            <span>今日数据</span>
          </div>
          <div class="realtime-stats">
            <div class="realtime-item">
              <span class="label">新用户:</span>
              <span class="value">{{ stats.users?.newToday || 0 }}</span>
            </div>
            <div class="realtime-item">
              <span class="label">咨询量:</span>
              <span class="value">{{ stats.consultations?.todayConsultations || 0 }}</span>
            </div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :span="8">
        <el-card class="realtime-card">
          <div class="realtime-header">
            <el-icon class="realtime-icon total"><PieChart /></el-icon>
            <span>总览统计</span>
          </div>
          <div class="realtime-stats">
            <div class="realtime-item">
              <span class="label">总用户:</span>
              <span class="value">{{ stats.users?.total || 0 }}</span>
            </div>
            <div class="realtime-item">
              <span class="label">总咨询:</span>
              <span class="value">{{ stats.consultations?.totalConsultations || 0 }}</span>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
    
    <!-- 图表区域 -->
    <el-row :gutter="20" class="chart-row">
      <el-col :span="12">
        <el-card>
          <template #header>
            <span>用户增长趋势</span>
          </template>
          <div class="chart-placeholder">
            <el-icon size="48" color="#ddd"><TrendCharts /></el-icon>
            <p>图表区域 - 待开发</p>
          </div>
        </el-card>
      </el-col>
      
      <el-col :span="12">
        <el-card>
          <template #header>
            <span>咨询统计</span>
          </template>
          <div class="chart-placeholder">
            <el-icon size="48" color="#ddd"><PieChart /></el-icon>
            <p>图表区域 - 待开发</p>
          </div>
        </el-card>
      </el-col>
    </el-row>
    
    <!-- 快速操作 -->
    <el-card class="quick-actions-card">
      <template #header>
        <span>快速操作</span>
      </template>
      <div class="quick-actions">
        <el-button 
          type="primary" 
          :icon="View" 
          @click="$router.push('/users')"
          v-permission="'admin:user:view'"
        >
          用户管理
        </el-button>
        <el-button 
          type="success" 
          :icon="View" 
          @click="$router.push('/doctors')"
          v-permission="'admin:doctor:view'"
        >
          医生管理
        </el-button>
        <el-button 
          type="info" 
          :icon="View" 
          @click="$router.push('/consultations')"
          v-permission="'admin:consultation:view'"
        >
          咨询管理
        </el-button>
        <el-button 
          type="warning" 
          :icon="Setting" 
          @click="$router.push('/settings')"
          v-permission="'admin:setting:view'"
        >
          系统设置
        </el-button>
        <el-button 
          type="danger" 
          :icon="Setting" 
          @click="$router.push('/permissions')"
          v-permission="'admin:permission:view'"
        >
          权限管理
        </el-button>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import {
  User,
  UserFilled,
  ChatDotRound,
  DataAnalysis,
  TrendCharts,
  PieChart,
  Plus,
  View,
  Setting,
  Refresh,
  Connection,
  Calendar
} from '@element-plus/icons-vue'
import { dashboardAPI } from '@/api/dashboard'

// 响应式数据
const loading = ref(false)
const stats = ref({
  users: {},
  doctors: {},
  consultations: {}
})
const realTimeData = ref({
  onlineUsers: 0,
  onlineDoctors: 0,
  ongoingConsultations: 0
})

onMounted(() => {
  loadDashboardData()
  // 定时刷新实时数据（每30秒）
  setInterval(loadRealTimeData, 30000)
})

// 加载仪表板数据
const loadDashboardData = async () => {
  try {
    loading.value = true
    
    // 并行加载所有统计数据
    const [systemStatsResult, realTimeResult] = await Promise.allSettled([
      dashboardAPI.getSystemStats(),
      dashboardAPI.getRealTimeData()
    ])

    // 处理系统统计数据
    if (systemStatsResult.status === 'fulfilled' && systemStatsResult.value.code === 200) {
      stats.value = systemStatsResult.value.data
      console.log('获取系统统计数据成功:', systemStatsResult.value.data)
    } else {
      console.error('获取系统统计数据失败:', systemStatsResult.reason || systemStatsResult.value)
    }

    // 处理实时数据
    if (realTimeResult.status === 'fulfilled' && realTimeResult.value.code === 200) {
      realTimeData.value = realTimeResult.value.data
    } else {
      console.error('获取实时数据失败:', realTimeResult.reason)
    }

  } catch (error) {
    console.error('加载仪表板数据失败:', error)
    ElMessage.error('加载数据失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

// 只加载实时数据（用于定时刷新）
const loadRealTimeData = async () => {
  try {
    const response = await dashboardAPI.getRealTimeData()
    if (response.code === 200) {
      realTimeData.value = response.data
    }
  } catch (error) {
    console.error('刷新实时数据失败:', error)
    // 实时数据刷新失败不显示错误提示，避免干扰用户
  }
}

// 手动刷新数据
const refreshData = () => {
  loadDashboardData()
  ElMessage.success('数据已刷新')
}
</script>

<style scoped>
.dashboard {
  padding: 20px;
  min-height: 100%;
  box-sizing: border-box;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.page-title {
  margin: 0;
  font-size: 24px;
  font-weight: 600;
  color: #333;
}

.stat-cards {
  margin-bottom: 30px;
}

.realtime-row {
  margin-bottom: 30px;
}

.chart-row {
  margin-bottom: 30px;
}

.stat-card {
  transition: transform 0.2s, box-shadow 0.2s;
  height: 120px;
}

.stat-card :deep(.el-card__body) {
  height: 100%;
  padding: 20px;
  display: flex;
  align-items: center;
}

.stat-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
}

.stat-card-content {
  display: flex;
  align-items: center;
  width: 100%;
}

.stat-icon {
  width: 60px;
  height: 60px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  margin-right: 16px;
  color: white;
}

.user-icon {
  background: linear-gradient(135deg, #667eea, #764ba2);
}

.doctor-icon {
  background: linear-gradient(135deg, #f093fb, #f5576c);
}

.consultation-icon {
  background: linear-gradient(135deg, #4facfe, #00f2fe);
}

.health-icon {
  background: linear-gradient(135deg, #43e97b, #38f9d7);
}

.stat-info {
  flex: 1;
}

.stat-title {
  font-size: 14px;
  color: #666;
  margin-bottom: 8px;
}

.stat-value {
  font-size: 24px;
  font-weight: 600;
  color: #333;
}

.stat-sub {
  font-size: 12px;
  color: #999;
  margin-top: 4px;
}

.progress-icon {
  background: linear-gradient(135deg, #ffa726, #ff7043);
}

/* 实时数据卡片样式 */
.realtime-card {
  transition: transform 0.2s, box-shadow 0.2s;
  height: 160px;
}

.realtime-card :deep(.el-card__body) {
  height: 100%;
  padding: 20px;
}

.realtime-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
}

.realtime-header {
  display: flex;
  align-items: center;
  margin-bottom: 16px;
  font-weight: 600;
  color: #333;
}

.realtime-icon {
  font-size: 18px;
  margin-right: 8px;
}

.realtime-icon.online {
  color: #67c23a;
}

.realtime-icon.today {
  color: #409eff;
}

.realtime-icon.total {
  color: #e6a23c;
}

.realtime-stats {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.realtime-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 0;
  border-bottom: 1px solid #f0f0f0;
}

.realtime-item:last-child {
  border-bottom: none;
}

.realtime-item .label {
  font-size: 14px;
  color: #666;
}

.realtime-item .value {
  font-size: 18px;
  font-weight: 600;
  color: #333;
}

.chart-placeholder {
  height: 300px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #999;
  background-color: #fafafa;
  border-radius: 8px;
}

.quick-actions {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}

.realtime-row {
  margin-top: 30px;
  margin-bottom: 30px;
}

.chart-row {
  margin-top: 30px;
  margin-bottom: 30px;
}

.quick-actions-card {
  margin-top: 30px;
}

.quick-actions-card {
  margin-top: 30px;
}
</style> 