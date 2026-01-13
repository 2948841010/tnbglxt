<template>
  <div class="doctor-dashboard">
    <!-- 欢迎区域 -->
    <div class="welcome-section">
      <div class="welcome-content">
        <h1 class="welcome-title">
          欢迎回来，{{ doctorName }}！
        </h1>
        <p class="welcome-subtitle">
          今天是 {{ formatDate(new Date()) }}，祝您身体健康！
        </p>
      </div>
      <div class="welcome-actions">
        <el-button type="primary" size="large" @click="setOnlineStatus(1)">
          <el-icon><VideoPlay /></el-icon>
          开始接诊
        </el-button>
        <el-button type="warning" size="large" @click="setOnlineStatus(2)">
          <el-icon><Loading /></el-icon>
          忙碌状态
        </el-button>
        <el-button type="info" size="large" @click="setOnlineStatus(0)">
          <el-icon><VideoPause /></el-icon>
          离线休息
        </el-button>
      </div>
    </div>

    <!-- 数据统计卡片 -->
    <div class="stats-section">
      <el-row :gutter="20">
        <el-col :xs="12" :sm="6">
          <div class="stat-card waiting-card">
            <div class="stat-icon">
              <el-icon><Clock /></el-icon>
            </div>
            <div class="stat-content">
              <div class="stat-title">待接诊咨询</div>
              <div class="stat-number">
                <span v-if="!loading.waitingConsultations">{{ dashboardData.waitingConsultations }}</span>
                <el-icon v-else class="loading-icon is-loading"><Loading /></el-icon>
              </div>
            </div>
          </div>
        </el-col>
        <el-col :xs="12" :sm="6">
          <div class="stat-card ongoing-card">
            <div class="stat-icon">
              <el-icon><ChatDotRound /></el-icon>
            </div>
            <div class="stat-content">
              <div class="stat-title">正在接诊咨询</div>
              <div class="stat-number">
                <span v-if="!loading.ongoingConsultations">{{ dashboardData.ongoingConsultations }}</span>
                <el-icon v-else class="loading-icon is-loading"><Loading /></el-icon>
              </div>
            </div>
          </div>
        </el-col>
        <el-col :xs="12" :sm="6">
          <div class="stat-card patient-card">
            <div class="stat-icon">
              <el-icon><UserFilled /></el-icon>
            </div>
            <div class="stat-content">
              <div class="stat-title">今日咨询数量</div>
              <div class="stat-number">
                <span v-if="!loading.todayPatients">{{ dashboardData.todayPatients }}</span>
                <el-icon v-else class="loading-icon is-loading"><Loading /></el-icon>
              </div>
            </div>
          </div>
        </el-col>
        <el-col :xs="12" :sm="6">
          <div class="stat-card rating-card">
            <div class="stat-icon">
              <el-icon><StarFilled /></el-icon>
            </div>
            <div class="stat-content">
              <div class="stat-title">医生评分</div>
              <div class="stat-number">{{ dashboardData.rating }}</div>
            </div>
          </div>
        </el-col>
      </el-row>
    </div>

    <!-- 快捷操作 -->
    <div class="quick-actions">
      <h3 class="section-title">快捷操作</h3>
      <el-row :gutter="16">
        <el-col :xs="12" :sm="6" :md="3">
          <div class="action-card" @click="goToConsultations">
            <div class="action-icon consultation-icon">
              <el-icon><ChatLineRound /></el-icon>
            </div>
            <div class="action-text">处理咨询</div>
          </div>
        </el-col>
        <el-col :xs="12" :sm="6" :md="3">
          <div class="action-card" @click="goToPatients">
            <div class="action-icon patient-icon">
              <el-icon><User /></el-icon>
            </div>
            <div class="action-text">患者管理</div>
          </div>
        </el-col>
        <el-col :xs="12" :sm="6" :md="3">
          <div class="action-card" @click="goToProfile">
            <div class="action-icon profile-icon">
              <el-icon><Setting /></el-icon>
            </div>
            <div class="action-text">个人信息</div>
          </div>
        </el-col>
        <el-col :xs="12" :sm="6" :md="3">
          <div class="action-card" @click="refreshData">
            <div class="action-icon refresh-icon">
              <el-icon><Refresh /></el-icon>
            </div>
            <div class="action-text">刷新数据</div>
          </div>
        </el-col>
      </el-row>
    </div>

    <!-- 最近咨询和通知 -->
    <el-row :gutter="20">
      <!-- 最近咨询 -->
      <el-col :xs="24" :lg="14">
        <div class="content-card">
          <div class="card-header">
            <h3 class="card-title">
              <el-icon><Document /></el-icon>
              最近咨询
            </h3>
            <el-button type="primary" size="small" @click="goToConsultations">
              查看全部
            </el-button>
          </div>
          <div class="consultation-list">
            <!-- 加载状态 -->
            <div v-if="loading.recentConsultations" class="loading-state">
              <el-skeleton :rows="3" animated />
            </div>
            
            <!-- 咨询列表 -->
            <template v-else>
              <div 
                v-for="consultation in recentConsultations" 
                :key="consultation.consultationNo || consultation.id"
                class="consultation-item"
                @click="goToConsultationDetail(consultation)"
              >
                <div class="consultation-avatar">
                  <el-avatar :size="40" :src="consultation.patientAvatar">
                    {{ consultation.patientName.charAt(0) }}
                  </el-avatar>
                </div>
                <div class="consultation-content">
                  <div class="consultation-header">
                    <span class="patient-name">{{ consultation.patientName }}</span>
                    <span class="consultation-time">{{ consultation.time }}</span>
                  </div>
                  <div class="consultation-message">{{ consultation.message }}</div>
                  <div class="consultation-status">
                    <el-tag :type="consultation.statusType" size="small">
                      {{ consultation.status }}
                    </el-tag>
                  </div>
                </div>
              </div>
              <div v-if="recentConsultations.length === 0" class="empty-state">
                <el-icon><DocumentRemove /></el-icon>
                <p>暂无咨询记录</p>
              </div>
            </template>
          </div>
        </div>
      </el-col>

      <!-- 通知公告 -->
      <el-col :xs="24" :lg="10">
        <div class="content-card">
          <div class="card-header">
            <h3 class="card-title">
              <el-icon><Bell /></el-icon>
              通知公告
            </h3>
          </div>
          <div class="notice-list">
            <div 
              v-for="notice in notices" 
              :key="notice.id"
              class="notice-item"
            >
              <div class="notice-content">
                <div class="notice-title">{{ notice.title }}</div>
                <div class="notice-summary">{{ notice.summary }}</div>
                <div class="notice-time">{{ notice.time }}</div>
              </div>
              <div class="notice-badge" v-if="notice.isNew">
                <el-badge is-dot type="danger" />
              </div>
            </div>
            <div v-if="notices.length === 0" class="empty-state">
              <el-icon><Bell /></el-icon>
              <p>暂无通知</p>
            </div>
          </div>
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onBeforeUnmount, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { useChatStore } from '@/stores/chat'
import { doctorAPI } from '@/api/doctor'
import { getTodayConsultationCount, getWaitingConsultationsCount, getOngoingConsultationsCount, getRecentConsultations } from '@/api/chat'
import { ElMessage } from 'element-plus'
import {
  VideoPlay, Loading, VideoPause, ChatDotRound, UserFilled, StarFilled, Clock,
  ChatLineRound, User, Setting, Refresh, Document, DocumentRemove, Bell
} from '@element-plus/icons-vue'

const router = useRouter()
const userStore = useUserStore()
const chatStore = useChatStore()

// 医生姓名
const doctorName = computed(() => {
  return userStore.userInfo?.realName || userStore.userInfo?.username || '医生'
})

// 仪表板数据
const dashboardData = reactive({
  waitingConsultations: 0,     // 待接诊咨询
  ongoingConsultations: 0,     // 正在接诊咨询
  todayPatients: 0,            // 今日咨询数量
  rating: 4.8                  // 医生评分（硬编码）
})

// 最近咨询数据
const recentConsultations = ref([])

// 加载状态
const loading = reactive({
  waitingConsultations: false,
  ongoingConsultations: false,
  todayPatients: false,
  recentConsultations: false
})

// 通知公告数据
const notices = ref([
  {
    id: 1,
    title: '系统维护通知',
    summary: '系统将于今晚22:00-23:00进行例行维护',
    time: '2小时前',
    isNew: true
  },
  {
    id: 2,
    title: '新功能上线',
    summary: '在线诊疗功能已上线，欢迎体验',
    time: '1天前',
    isNew: false
  },
  {
    id: 3,
    title: '医生培训通知',
    summary: '下周三将举行糖尿病诊疗规范培训',
    time: '2天前',
    isNew: false
  }
])

// 格式化日期
const formatDate = (date) => {
  const year = date.getFullYear()
  const month = date.getMonth() + 1
  const day = date.getDate()
  const weekdays = ['日', '一', '二', '三', '四', '五', '六']
  const weekday = weekdays[date.getDay()]
  
  return `${year}年${month}月${day}日 星期${weekday}`
}

// 设置在线状态
const setOnlineStatus = async (status) => {
  try {
    const statusText = { 0: '离线', 1: '在线', 2: '忙碌' }[status]
    await doctorAPI.updateOnlineStatus(status)
    ElMessage.success(`已切换为${statusText}状态`)
  } catch (error) {
    console.error('更新在线状态失败:', error)
    ElMessage.error('状态更新失败')
  }
}

// 快捷操作
const goToConsultations = () => {
  router.push('/doctor/consultation/list')
}

const goToPatients = () => {
  router.push('/doctor/consultation/users')
}

const goToProfile = () => {
  router.push('/doctor/profile')
}

const goToConsultationDetail = (consultation) => {
  if (consultation.consultationNo) {
    router.push(`/consultation/chat/${consultation.consultationNo}`)
  }
}

// 加载待接诊咨询数量
const loadWaitingConsultationsCount = async () => {
  if (loading.waitingConsultations) return
  loading.waitingConsultations = true
  try {
    const response = await getWaitingConsultationsCount()
    if (response.code === 200) {
      dashboardData.waitingConsultations = response.data
    }
  } catch (error) {
    console.error('获取待接诊咨询数量失败:', error)
  } finally {
    loading.waitingConsultations = false
  }
}

// 加载正在接诊咨询数量
const loadOngoingConsultationsCount = async () => {
  if (loading.ongoingConsultations) return
  loading.ongoingConsultations = true
  try {
    const response = await getOngoingConsultationsCount()
    if (response.code === 200) {
      dashboardData.ongoingConsultations = response.data
    }
  } catch (error) {
    console.error('获取正在接诊咨询数量失败:', error)
  } finally {
    loading.ongoingConsultations = false
  }
}

// 加载今日咨询患者数量
const loadTodayPatientsCount = async () => {
  if (loading.todayPatients) return
  loading.todayPatients = true
  try {
    const response = await getTodayConsultationCount()
    if (response.code === 200) {
      dashboardData.todayPatients = response.data
    }
  } catch (error) {
    console.error('获取今日咨询患者数量失败:', error)
  } finally {
    loading.todayPatients = false
  }
}

// 加载最近咨询列表
const loadRecentConsultations = async () => {
  if (loading.recentConsultations) return
  loading.recentConsultations = true
  try {
    const response = await getRecentConsultations(3)
    if (response.code === 200) {
      recentConsultations.value = response.data.map(consultation => ({
        id: consultation.id,
        consultationNo: consultation.consultationNo,
        patientName: consultation.patientInfo?.name || '未知患者',
        patientAvatar: consultation.patientInfo?.avatar || '',
        message: consultation.chiefComplaint || '暂无主诉',
        time: formatTimeFromNow(consultation.updateTime || consultation.createTime),
        status: getStatusText(consultation.status),
        statusType: getStatusType(consultation.status)
      }))
    }
  } catch (error) {
    console.error('获取最近咨询列表失败:', error)
  } finally {
    loading.recentConsultations = false
  }
}

// 刷新所有数据
const refreshData = async () => {
  console.log('刷新工作台数据')
  await Promise.all([
    loadWaitingConsultationsCount(),
    loadOngoingConsultationsCount(),
    loadTodayPatientsCount(),
    loadRecentConsultations()
  ])
  ElMessage.success('数据已刷新')
}

// 初始化WebSocket实时更新
const initializeWebSocketForDashboard = async () => {
  try {
    await chatStore.initializeWebSocket()
    
    if (userStore?.userInfo?.id) {
      // 订阅医生咨询列表更新
      chatStore.subscribeToDoctorConsultationList(userStore.userInfo.id, handleDashboardUpdate)
      console.log('[Dashboard] WebSocket订阅成功')
    }
  } catch (error) {
    console.error('[Dashboard] WebSocket初始化失败:', error)
  }
}

// 处理工作台数据更新
const handleDashboardUpdate = (updateData) => {
  console.log('[Dashboard] 收到更新:', updateData)
  
  if (updateData.type === 'new_consultation' || 
      updateData.type === 'consultation_status_changed' || 
      updateData.type === 'consultation_completed') {
    // 实时更新统计数据
    loadWaitingConsultationsCount()
    loadOngoingConsultationsCount()
    loadTodayPatientsCount()
    loadRecentConsultations()
  }
}

// 获取状态文本
const getStatusText = (status) => {
  const statusMap = {
    1: '待接诊',
    2: '进行中', 
    3: '已完成',
    4: '已取消'
  }
  return statusMap[status] || '未知'
}

// 获取状态类型
const getStatusType = (status) => {
  const typeMap = {
    1: 'warning',  // 待接诊
    2: 'primary',  // 进行中
    3: 'success',  // 已完成
    4: 'danger'    // 已取消
  }
  return typeMap[status] || 'info'
}

// 格式化相对时间
const formatTimeFromNow = (timeStr) => {
  if (!timeStr) return ''
  
  const date = new Date(timeStr)
  const now = new Date()
  const diffTime = now - date
  const diffMinutes = Math.floor(diffTime / (1000 * 60))
  const diffHours = Math.floor(diffTime / (1000 * 60 * 60))
  const diffDays = Math.floor(diffTime / (1000 * 60 * 60 * 24))
  
  if (diffMinutes < 1) return '刚刚'
  if (diffMinutes < 60) return `${diffMinutes}分钟前`
  if (diffHours < 24) return `${diffHours}小时前`
  if (diffDays < 7) return `${diffDays}天前`
  
  return date.toLocaleDateString('zh-CN', {
    month: '2-digit',
    day: '2-digit'
  })
}

onMounted(async () => {
  console.log('医生工作台页面加载完成')
  // 加载初始数据
  await refreshData()
  // 初始化WebSocket
  await initializeWebSocketForDashboard()
})

onBeforeUnmount(() => {
  // 清理WebSocket连接
  if (userStore?.userInfo?.id) {
    // 这里可以添加取消订阅的逻辑
    console.log('[Dashboard] 组件卸载，清理WebSocket')
  }
})
</script>

<style scoped>
.doctor-dashboard {
  padding: 24px;
  background: #f5f7fa;
  min-height: 100vh;
}

/* 欢迎区域 */
.welcome-section {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 30px;
  border-radius: 16px;
  color: white;
  margin-bottom: 24px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 20px;
}

.welcome-title {
  font-size: 28px;
  font-weight: 700;
  margin: 0 0 8px 0;
}

.welcome-subtitle {
  font-size: 16px;
  opacity: 0.9;
  margin: 0;
}

.welcome-actions {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}

.welcome-actions .el-button {
  background: rgba(255, 255, 255, 0.2);
  border: 1px solid rgba(255, 255, 255, 0.3);
  color: white;
  font-weight: 500;
}

.welcome-actions .el-button:hover {
  background: rgba(255, 255, 255, 0.3);
  transform: translateY(-2px);
}

/* 统计卡片 */
.stats-section {
  margin-bottom: 24px;
}

.stat-card {
  background: white;
  padding: 24px;
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  display: flex;
  align-items: center;
  gap: 16px;
  transition: all 0.3s ease;
  height: 100px;
}

.stat-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
}

.stat-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  color: white;
}

.waiting-card .stat-icon {
  background: linear-gradient(135deg, #ff9a9e 0%, #fecfef 100%);
}

.ongoing-card .stat-icon {
  background: linear-gradient(135deg, #a8edea 0%, #fed6e3 100%);
}

.patient-card .stat-icon {
  background: linear-gradient(135deg, #a8c8ec 0%, #abc4ff 100%);
}

.rating-card .stat-icon {
  background: linear-gradient(135deg, #ffecd2 0%, #fcb69f 100%);
}

.stat-content {
  flex: 1;
}

.stat-title {
  font-size: 14px;
  color: #64748b;
  margin-bottom: 4px;
}

.stat-number {
  font-size: 24px;
  font-weight: 700;
  color: #1e293b;
}

/* 快捷操作 */
.quick-actions {
  margin-bottom: 24px;
}

.section-title {
  font-size: 18px;
  font-weight: 600;
  color: #1e293b;
  margin: 0 0 16px 0;
}

.action-card {
  background: white;
  padding: 24px;
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  text-align: center;
  cursor: pointer;
  transition: all 0.3s ease;
  height: 120px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  gap: 12px;
}

.action-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
}

.action-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  color: white;
  margin: 0 auto;
}

.consultation-icon {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.patient-icon {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
}

.profile-icon {
  background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
}

.refresh-icon {
  background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%);
}

.action-text {
  font-size: 14px;
  font-weight: 500;
  color: #374151;
}

/* 内容卡片 */
.content-card {
  background: white;
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  margin-bottom: 20px;
  overflow: hidden;
}

.card-header {
  padding: 20px 24px;
  border-bottom: 1px solid #f1f5f9;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-title {
  font-size: 16px;
  font-weight: 600;
  color: #1e293b;
  margin: 0;
  display: flex;
  align-items: center;
  gap: 8px;
}

/* 咨询列表 */
.consultation-list {
  padding: 20px 24px;
}

.consultation-item {
  display: flex;
  gap: 12px;
  padding: 16px 0;
  border-bottom: 1px solid #f1f5f9;
  cursor: pointer;
  transition: all 0.3s ease;
  border-radius: 8px;
  margin: 0 -8px;
  padding-left: 8px;
  padding-right: 8px;
}

.consultation-item:hover {
  background: #f8fafc;
  transform: translateX(4px);
}

.consultation-item:last-child {
  border-bottom: none;
}

.consultation-avatar {
  flex-shrink: 0;
}

.consultation-content {
  flex: 1;
  min-width: 0;
}

.consultation-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.patient-name {
  font-weight: 500;
  color: #1e293b;
}

.consultation-time {
  font-size: 12px;
  color: #64748b;
}

.consultation-message {
  color: #64748b;
  font-size: 14px;
  margin-bottom: 8px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

/* 通知列表 */
.notice-list {
  padding: 20px 24px;
}

.notice-item {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  padding: 16px 0;
  border-bottom: 1px solid #f1f5f9;
}

.notice-item:last-child {
  border-bottom: none;
}

.notice-content {
  flex: 1;
}

.notice-title {
  font-weight: 500;
  color: #1e293b;
  margin-bottom: 4px;
}

.notice-summary {
  font-size: 14px;
  color: #64748b;
  margin-bottom: 8px;
}

.notice-time {
  font-size: 12px;
  color: #94a3b8;
}

.notice-badge {
  margin-left: 12px;
}

/* 空状态 */
.empty-state {
  text-align: center;
  padding: 40px 20px;
  color: #64748b;
}

.empty-state .el-icon {
  font-size: 48px;
  margin-bottom: 16px;
  opacity: 0.5;
}

.empty-state p {
  margin: 0;
  font-size: 14px;
}

/* 加载状态样式 */
.loading-icon {
  color: #409eff;
  animation: rotation 1s infinite linear;
}

@keyframes rotation {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}

.loading-state {
  padding: 20px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .doctor-dashboard {
    padding: 16px;
  }
  
  .welcome-section {
    flex-direction: column;
    text-align: center;
  }
  
  .welcome-title {
    font-size: 24px;
  }
  
  .stat-number {
    font-size: 20px;
  }
  
  .action-card {
    height: 100px;
    padding: 16px;
  }
  
  .action-icon {
    width: 40px;
    height: 40px;
    font-size: 20px;
  }
}

@media (max-width: 480px) {
  .welcome-actions {
    width: 100%;
  }
  
  .welcome-actions .el-button {
    flex: 1;
  }
}
</style> 