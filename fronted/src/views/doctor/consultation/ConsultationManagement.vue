<template>
  <div class="consultation-management">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-content">
        <h1 class="page-title">
          <el-icon><ChatLineRound /></el-icon>
          咨询管理
        </h1>
        <p class="page-desc">统一管理和处理患者咨询</p>
      </div>
    </div>

    <!-- 统计看板 -->
    <div class="stats-dashboard">
      <div class="stats-grid">
        <div class="stat-card today" @click="filterByToday">
          <div class="stat-icon">
            <el-icon><Calendar /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ statusCounts.today || 0 }}</div>
            <div class="stat-label">今日新增</div>
          </div>
        </div>
        
        <div class="stat-card pending" @click="filterByStatus(1)">
          <div class="stat-icon">
            <el-icon><Clock /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ statusCounts.pending || 0 }}</div>
            <div class="stat-label">待接诊</div>
          </div>
        </div>
        
        <div class="stat-card ongoing" @click="filterByStatus(2)">
          <div class="stat-icon">
            <el-icon><ChatDotRound /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ statusCounts.ongoing || 0 }}</div>
            <div class="stat-label">进行中</div>
          </div>
        </div>
        
        <div class="stat-card completed" @click="filterByStatus(3)">
          <div class="stat-icon">
            <el-icon><CircleCheck /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ statusCounts.completed || 0 }}</div>
            <div class="stat-label">已完成</div>
          </div>
        </div>
        
        <div class="stat-card total" @click="filterByStatus('all')">
          <div class="stat-icon">
            <el-icon><DataBoard /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ statusCounts.total || 0 }}</div>
            <div class="stat-label">总咨询数</div>
          </div>
        </div>
      </div>
    </div>

    <!-- 筛选和搜索栏 -->
    <div class="filter-section">
      <div class="filter-left">
        <!-- 状态筛选 -->
        <div class="filter-tabs">
          <el-radio-group v-model="activeStatusFilter" @change="onStatusFilterChange">
            <el-radio-button label="all">全部</el-radio-button>
            <el-radio-button label="pending">待接诊</el-radio-button>
            <el-radio-button label="ongoing">进行中</el-radio-button>
            <el-radio-button label="completed">已完成</el-radio-button>
            <el-radio-button label="cancelled">已取消</el-radio-button>
          </el-radio-group>
        </div>
        
        <!-- 时间筛选 -->
        <el-select v-model="timeFilter" placeholder="时间筛选" @change="onTimeFilterChange" style="width: 120px;">
          <el-option label="全部" value="all" />
          <el-option label="今日" value="today" />
          <el-option label="本周" value="week" />
          <el-option label="本月" value="month" />
        </el-select>
      </div>
      
      <div class="filter-right">
        <!-- 搜索框 -->
        <el-input
          v-model="searchKeyword"
          placeholder="搜索患者姓名或咨询编号"
          @input="onSearch"
          clearable
          style="width: 300px;"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
        
        <!-- 视图切换 -->
        <el-radio-group v-model="viewMode" class="view-mode-toggle">
          <el-radio-button label="list">
            <el-icon><List /></el-icon>
          </el-radio-button>
          <el-radio-button label="card">
            <el-icon><Grid /></el-icon>
          </el-radio-button>
        </el-radio-group>
        
        <!-- 刷新按钮 -->
        <el-button @click="refreshData" :loading="loading">
          <el-icon><Refresh /></el-icon>
        </el-button>
      </div>
    </div>

    <!-- 咨询列表 -->
    <div class="consultation-list" v-loading="loading">
      <!-- 列表视图 -->
      <div v-if="viewMode === 'list'" class="list-view">
        <div v-if="consultations.length === 0" class="empty-state">
          <el-empty description="暂无咨询记录" />
        </div>
        
        <div v-else class="consultation-items">
          <div 
            v-for="consultation in filteredConsultations" 
            :key="consultation.consultationNo"
            class="consultation-item"
            :class="{ 'unread': hasUnreadMessages(consultation) }"
          >
            <!-- 咨询信息头部 -->
            <div class="item-header">
              <div class="patient-info">
                <UserAvatar 
                  :src="consultation.patientInfo?.avatar"
                  :username="consultation.patientInfo?.name"
                  size="medium"
                />
                <div class="patient-details">
                  <div class="patient-name">
                    {{ consultation.patientInfo?.name || '未知患者' }}
                    <el-tag :type="getGenderType(consultation.patientInfo?.gender)" size="small">
                      {{ consultation.patientInfo?.gender || '未知' }}·{{ consultation.patientInfo?.age || '未知' }}岁
                    </el-tag>
                  </div>
                  <div class="consultation-no">咨询编号：{{ consultation.consultationNo }}</div>
                </div>
              </div>
              
              <div class="item-meta">
                <div class="status-with-indicator">
                  <el-tag :type="getStatusType(consultation.status)" size="small">
                    {{ getStatusText(consultation.status) }}
                  </el-tag>
                  <!-- 🔥 新消息红点提示 -->
                  <div 
                    v-if="hasUnreadMessages(consultation)" 
                    class="unread-dot">
                  </div>
                </div>
                <div class="time-info">
                  <div class="create-time">{{ formatTime(consultation.createTime) }}</div>
                  <div v-if="getLastReplyTime(consultation)" class="reply-time">
                    回复时间：{{ formatTime(getLastReplyTime(consultation)) }}
                  </div>
                </div>
              </div>
            </div>
            
            <!-- 主诉内容 -->
            <div class="item-content">
              <div class="chief-complaint">
                <strong>主诉：</strong>{{ consultation.chiefComplaint || '无' }}
              </div>
              
              <!-- 🔥 消息统计信息 -->
              <div class="message-stats">
                <div class="message-count">
                  <el-icon><ChatDotRound /></el-icon>
                  <span>{{ consultation.messages?.length || 0 }} 条消息</span>
                </div>
                <div class="reply-count">
                  <el-icon><EditPen /></el-icon>
                  <span>{{ getReplyCount(consultation) }} 条回复</span>
                </div>
                <!-- 未读消息计数 -->
                <div v-if="hasUnreadMessages(consultation)" class="unread-count">
                  <el-badge :value="getUnreadCount(consultation)" type="danger">
                    <el-icon><Message /></el-icon>
                  </el-badge>
                  <span>有新消息</span>
                </div>
              </div>
              
              <!-- 🔥 最后回复预览 -->
              <div v-if="getLastDoctorReply(consultation)" class="last-reply-preview">
                <div class="reply-label">最后回复：</div>
                <div class="reply-content">{{ formatLastReplyContent(getLastDoctorReply(consultation)) }}</div>
              </div>
            </div>
            
            <!-- 操作按钮 -->
            <div class="item-actions">
              <el-button 
                v-if="consultation.status === 1" 
                type="primary" 
                size="small"
                @click="acceptConsultation(consultation)"
                :loading="consultation.accepting"
              >
                接诊
              </el-button>
              
              <el-button 
                type="primary" 
                plain 
                size="small"
                @click="viewConsultation(consultation)"
              >
                查看详情
              </el-button>
              
              <el-button 
                v-if="consultation.status === 2" 
                type="success" 
                plain 
                size="small"
                @click="completeConsultation(consultation)"
              >
                结束问诊
              </el-button>
              
              <el-dropdown @command="(cmd) => handleMoreAction(cmd, consultation)">
                <el-button size="small" type="text">
                  更多<el-icon><ArrowDown /></el-icon>
                </el-button>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item command="history">查看历史</el-dropdown-item>
                    <el-dropdown-item command="export">导出记录</el-dropdown-item>
                    <el-dropdown-item v-if="consultation.status !== 4" command="cancel" divided>
                      取消咨询
                    </el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
            </div>
          </div>
        </div>
      </div>
      
      <!-- 卡片视图 -->
      <div v-else class="card-view">
        <div class="consultation-cards">
          <div 
            v-for="consultation in filteredConsultations" 
            :key="consultation.consultationNo"
            class="consultation-card"
            :class="{ 'has-unread': hasUnreadMessages(consultation) }"
            @click="viewConsultation(consultation)"
          >
            <div class="card-header">
              <UserAvatar 
                :src="consultation.patientInfo?.avatar"
                :username="consultation.patientInfo?.name"
                size="small"
              />
              <div class="card-title">
                <div class="patient-name">{{ consultation.patientInfo?.name }}</div>
                <div class="consultation-time">{{ formatTime(consultation.createTime) }}</div>
              </div>
              <div class="status-with-indicator">
                <el-tag :type="getStatusType(consultation.status)" size="small">
                  {{ getStatusText(consultation.status) }}
                </el-tag>
                <!-- 红点提示 -->
                <div 
                  v-if="hasUnreadMessages(consultation)" 
                  class="unread-dot">
                </div>
              </div>
            </div>
            
            <div class="card-content">
              <p class="chief-complaint">{{ consultation.chiefComplaint }}</p>
              
              <!-- 消息统计 -->
              <div class="card-stats">
                <span class="stat-item">
                  <el-icon><ChatDotRound /></el-icon>
                  {{ consultation.messages?.length || 0 }}
                </span>
                <span class="stat-item">
                  <el-icon><EditPen /></el-icon>
                  {{ getReplyCount(consultation) }}
                </span>
                <span v-if="hasUnreadMessages(consultation)" class="stat-item unread">
                  <el-icon><Message /></el-icon>
                  {{ getUnreadCount(consultation) }}
                </span>
              </div>
              
              <!-- 最后回复预览 -->
              <div v-if="getLastDoctorReply(consultation)" class="last-reply">
                最后回复：{{ formatLastReplyContent(getLastDoctorReply(consultation)) }}
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 分页 -->
    <div v-if="pagination.total > 0" class="pagination-section">
      <el-pagination
        v-model:current-page="pagination.currentPage"
        v-model:page-size="pagination.pageSize"
        :total="pagination.total"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed, onBeforeUnmount, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  ChatLineRound,
  Calendar,
  Clock,
  ChatDotRound,
  CircleCheck,
  DataBoard,
  Search,
  List,
  Grid,
  Refresh,
  Message,
  ArrowDown,
  EditPen
} from '@element-plus/icons-vue'
import UserAvatar from '@/components/common/UserAvatar.vue'
import { useUserStore } from '@/stores/user'
import { useChatStore } from '@/stores/chat'
import chatWebSocketManager from '@/utils/chatWebSocket'
import {
  getDoctorConsultationsPage,
  acceptConsultation as acceptConsultationAPI,
  completeConsultation as completeConsultationAPI,
  getTodayConsultationCount,
  getWaitingConsultationsCount,
  getOngoingConsultationsCount,
  getCompletedConsultationsCount,
  getPendingConsultationsCount
} from '@/api/chat'

const router = useRouter()
const userStore = useUserStore()
const chatStore = useChatStore()

// 响应式数据
const loading = ref(false)
const consultations = ref([])
const statusCounts = reactive({
  today: 0,
  pending: 0,
  ongoing: 0,
  completed: 0,
  cancelled: 0,
  total: 0
})

// 筛选和搜索
const activeStatusFilter = ref('all')
const timeFilter = ref('all')
const searchKeyword = ref('')
const viewMode = ref('list')

// 分页
const pagination = reactive({
  currentPage: 1,
  pageSize: 20,
  total: 0
})

// 计算属性：过滤后的咨询列表
const filteredConsultations = computed(() => {
  let filtered = [...consultations.value]
  
  // 状态筛选
  if (activeStatusFilter.value !== 'all') {
    const statusMap = {
      'pending': 1,
      'ongoing': 2,
      'completed': 3,
      'cancelled': 4
    }
    filtered = filtered.filter(c => c.status === statusMap[activeStatusFilter.value])
  }
  
  // 关键词搜索
  if (searchKeyword.value.trim()) {
    const keyword = searchKeyword.value.toLowerCase()
    filtered = filtered.filter(c => 
      c.patientInfo?.name?.toLowerCase().includes(keyword) ||
      c.consultationNo.toLowerCase().includes(keyword)
    )
  }
  
  return filtered
})

// 加载咨询列表
const loadConsultations = async () => {
  loading.value = true
  try {
    // 根据activeStatusFilter构建状态参数
    let statusParam = ''
    if (activeStatusFilter.value !== 'all') {
      const statusMap = {
        'pending': '1',
        'ongoing': '2', 
        'completed': '3',
        'cancelled': '4'
      }
      statusParam = statusMap[activeStatusFilter.value] || ''
    }
    
    console.log('📋 加载咨询列表参数:', {
      page: pagination.currentPage,
      size: pagination.pageSize,
      status: statusParam
    })
    
    const response = await getDoctorConsultationsPage(
      pagination.currentPage,
      pagination.pageSize,
      statusParam
    )
    
    console.log('📋 咨询列表响应:', response)
    
    if (response.code === 200) {
      consultations.value = response.data.records || response.data || []
      pagination.total = response.data.total || 0
      console.log('✅ 咨询列表加载成功:', {
        count: consultations.value.length,
        total: pagination.total
      })
    } else {
      ElMessage.error(response.message || '获取咨询列表失败')
      console.error('❌ 咨询列表加载失败:', response)
    }
  } catch (error) {
    console.error('❌ 获取咨询列表异常:', error)
    ElMessage.error('网络异常，请稍后重试')
  } finally {
    loading.value = false
  }
}

// 加载统计数据
const loadStatusCounts = async () => {
  try {
    console.log('📊 开始加载统计数据...')
    
    // 并行获取各种统计数据
    const [todayCount, waitingCount, ongoingCount, completedCount] = await Promise.allSettled([
      getTodayConsultationCount(),
      getWaitingConsultationsCount(), 
      getOngoingConsultationsCount(),
      getCompletedConsultationsCount()
    ])
    
    console.log('📊 统计数据响应:', {
      todayCount: todayCount.status === 'fulfilled' ? todayCount.value : todayCount.reason,
      waitingCount: waitingCount.status === 'fulfilled' ? waitingCount.value : waitingCount.reason,
      ongoingCount: ongoingCount.status === 'fulfilled' ? ongoingCount.value : ongoingCount.reason,
      completedCount: completedCount.status === 'fulfilled' ? completedCount.value : completedCount.reason
    })
    
    // 更新统计数据 - 使用默认值防止错误
    statusCounts.today = (todayCount.status === 'fulfilled' && todayCount.value?.code === 200) ? todayCount.value.data : 0
    statusCounts.pending = (waitingCount.status === 'fulfilled' && waitingCount.value?.code === 200) ? waitingCount.value.data : 0
    statusCounts.ongoing = (ongoingCount.status === 'fulfilled' && ongoingCount.value?.code === 200) ? ongoingCount.value.data : 0
    statusCounts.completed = (completedCount.status === 'fulfilled' && completedCount.value?.code === 200) ? completedCount.value.data : 0
    
    // 计算总数
    statusCounts.total = statusCounts.pending + statusCounts.ongoing + statusCounts.completed
    
    console.log('✅ 统计数据更新完成:', statusCounts)
    
  } catch (error) {
    console.error('❌ 获取统计数据失败:', error)
    // 设置默认值，确保页面正常显示
    statusCounts.today = 0
    statusCounts.pending = 0
    statusCounts.ongoing = 0
    statusCounts.completed = 0
    statusCounts.total = 0
  }
}

// 状态相关方法
const getStatusType = (status) => {
  const types = { 1: 'warning', 2: 'primary', 3: 'success', 4: 'danger' }
  return types[status] || 'info'
}

const getStatusText = (status) => {
  const texts = { 1: '待接诊', 2: '进行中', 3: '已完成', 4: '已取消' }
  return texts[status] || '未知'
}

const getGenderType = (gender) => {
  return gender === '男' ? 'primary' : gender === '女' ? 'danger' : 'info'
}

// 时间格式化
const formatTime = (timeStr) => {
  if (!timeStr) return ''
  const date = new Date(timeStr)
  const now = new Date()
  const diff = now - date
  
  if (diff < 24 * 60 * 60 * 1000) { // 24小时内
    return date.toLocaleString('zh-CN', {
      hour: '2-digit',
      minute: '2-digit'
    })
  } else {
    return date.toLocaleString('zh-CN', {
      month: '2-digit',
      day: '2-digit',
      hour: '2-digit',
      minute: '2-digit'
    })
  }
}

// 🔥 完善的消息统计相关方法
const hasUnreadMessages = (consultation) => {
  return chatStore.getUnreadCount(consultation.consultationNo) > 0
}

const getUnreadCount = (consultation) => {
  return chatStore.getUnreadCount(consultation.consultationNo)
}

// 获取医生回复数量
const getReplyCount = (consultation) => {
  if (!consultation.messages) return 0
  return consultation.messages.filter(m => m.senderType === 'doctor').length
}

// 获取最后一条医生回复
const getLastDoctorReply = (consultation) => {
  if (!consultation.messages || consultation.messages.length === 0) return null
  
  const doctorMessages = consultation.messages.filter(m => m.senderType === 'doctor')
  if (doctorMessages.length === 0) return null
  
  return doctorMessages[doctorMessages.length - 1]
}

// 格式化最后回复内容的显示
const formatLastReplyContent = (message) => {
  if (!message) return ''
  
  switch (message.messageType) {
    case 'image':
      return '[图片]'
    case 'file':
      return message.fileName ? `[文件] ${message.fileName}` : '[文件]'
    case 'text':
    default:
      // 文本消息，如果内容太长则截断
      const content = message.content || ''
      return content.length > 30 ? content.substring(0, 30) + '...' : content
  }
}

// 获取最后回复时间
const getLastReplyTime = (consultation) => {
  if (!consultation.messages || consultation.messages.length === 0) {
    return consultation.startTime || consultation.createTime
  }
  
  // 找到最后一条医生发送的消息
  const doctorMessages = consultation.messages.filter(m => m.senderType === 'doctor')
  if (doctorMessages.length === 0) {
    return consultation.startTime || consultation.createTime
  }
  
  return doctorMessages[doctorMessages.length - 1].sendTime
}

// 筛选事件处理
const onStatusFilterChange = (value) => {
  pagination.currentPage = 1
  loadConsultations()
}

const onTimeFilterChange = (value) => {
  pagination.currentPage = 1
  loadConsultations()
}

const onSearch = () => {
  // 搜索在computed中处理，这里不需要额外操作
}

// 统计卡片点击事件
const filterByStatus = (status) => {
  if (status === 'all') {
    activeStatusFilter.value = 'all'
  } else {
    const statusMap = { 1: 'pending', 2: 'ongoing', 3: 'completed', 4: 'cancelled' }
    activeStatusFilter.value = statusMap[status]
  }
  pagination.currentPage = 1
  loadConsultations()
}

const filterByToday = () => {
  timeFilter.value = 'today'
  pagination.currentPage = 1
  loadConsultations()
}

// 咨询操作方法
const acceptConsultation = async (consultation) => {
  consultation.accepting = true
  try {
    const response = await acceptConsultationAPI(consultation.consultationNo)
    if (response.code === 200) {
      ElMessage.success('接诊成功')
      consultation.status = 2
      loadStatusCounts()
    } else {
      ElMessage.error(response.message || '接诊失败')
    }
  } catch (error) {
    console.error('接诊失败:', error)
    ElMessage.error('网络异常，请稍后重试')
  } finally {
    consultation.accepting = false
  }
}

const viewConsultation = async (consultation) => {
  console.log('🔗 跳转到咨询详情:', consultation.consultationNo)
  
  try {
    // 清空当前活跃会话，避免状态冲突
    chatStore.setActiveConsultation(null)
    
    // 等待一个小的延迟，确保状态清理完成
    await new Promise(resolve => setTimeout(resolve, 100))
    
    // 跳转到咨询详情页面
    await router.push({
      path: `/consultation/chat/${consultation.consultationNo}`,
      // 添加时间戳参数强制组件重新加载
      query: { 
        t: Date.now(),
        from: 'management' // 标识来源页面
      }
    })
    
    console.log('✅ 跳转成功到咨询详情页面')
    
  } catch (error) {
    if (error.name === 'NavigationDuplicated') {
      console.log('⚠️ 重复导航，尝试刷新页面')
      // 如果是重复导航，强制刷新当前页面
      window.location.href = `/consultation/chat/${consultation.consultationNo}?t=${Date.now()}`
    } else {
      console.error('❌ 路由跳转失败:', error)
      ElMessage.error('页面跳转失败，请重试')
    }
  }
}

const completeConsultation = async (consultation) => {
  try {
    await ElMessageBox.confirm('确定要结束这个问诊吗？', '确认操作', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    const response = await completeConsultationAPI(consultation.consultationNo)
    if (response.code === 200) {
      ElMessage.success('问诊已结束')
      consultation.status = 3
      loadStatusCounts()
    } else {
      ElMessage.error(response.message || '操作失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('结束问诊失败:', error)
      ElMessage.error('网络异常，请稍后重试')
    }
  }
}

const handleMoreAction = (command, consultation) => {
  switch (command) {
    case 'history':
      // 查看历史记录
      break
    case 'export':
      // 导出记录
      break
    case 'cancel':
      // 取消咨询
      break
  }
}

// 分页事件
const handleSizeChange = (size) => {
  pagination.pageSize = size
  pagination.currentPage = 1
  loadConsultations()
}

const handleCurrentChange = (page) => {
  pagination.currentPage = page
  loadConsultations()
}

// 刷新数据
const refreshData = () => {
  loadConsultations()
  loadStatusCounts()
}

// 🔥 处理新消息的回调函数
const handleNewMessage = async (consultationNo, message) => {
  console.log('💬 收到新消息:', consultationNo, message)
  
  try {
    // 查找对应的咨询记录
    const index = consultations.value.findIndex(c => c.consultationNo === consultationNo)
    if (index !== -1) {
      const consultation = consultations.value[index]
      
      // 更新消息列表
      if (!consultation.messages) {
        consultation.messages = []
      }
      consultation.messages.push(message)
      
      // 如果是患者消息，强制增加未读计数（即使医生在聊天页面也要在管理页面显示未读）
      if (message.senderType === 'patient') {
        chatStore.incrementUnreadCount(consultationNo, true) // force = true
        console.log('📩 患者新消息，强制未读计数+1:', consultationNo)
      }
      
      // 强制触发响应式更新
      consultations.value[index] = { ...consultation }
      
      // 确保DOM更新完成
      await nextTick()
      
      console.log('✅ 消息列表已更新，当前消息数:', consultation.messages.length)
    }
  } catch (error) {
    console.error('处理新消息失败:', error)
  }
}

// 处理实时更新的回调函数
const handleRepliesUpdate = (updateData) => {
  console.log('📡 收到咨询列表更新:', updateData)
  
  try {
    if (updateData.type === 'new_consultation') {
      // 新咨询 - 添加到列表开头
      consultations.value.unshift(updateData.consultation)
      console.log('➕ 新咨询已添加到列表')
      
      // 🔥 为新咨询订阅消息更新
      subscribeToConsultationMessages(updateData.consultation.consultationNo)
      
    } else if (updateData.type === 'consultation_status_changed') {
      // 状态变化 - 更新对应的咨询记录
      const index = consultations.value.findIndex(c => c.consultationNo === updateData.consultationNo)
      if (index !== -1) {
        if (updateData.consultation) {
          consultations.value[index] = updateData.consultation
        } else {
          consultations.value[index].status = updateData.newStatus
        }
        console.log('🔄 咨询状态已更新:', updateData.consultationNo)
      }
    } else if (updateData.type === 'consultation_completed') {
      // 咨询完成
      const index = consultations.value.findIndex(c => c.consultationNo === updateData.consultationNo)
      if (index !== -1) {
        consultations.value[index] = updateData.consultation || { 
          ...consultations.value[index], 
          status: 3 
        }
        console.log('✅ 咨询已完成:', updateData.consultationNo)
      }
    } else if (updateData.type === 'new_reply') {
      // 🔥 新回复消息 - 这个可能来自WebSocket推送
      const index = consultations.value.findIndex(c => c.consultationNo === updateData.consultationNo)
      if (index !== -1) {
        const consultation = consultations.value[index]
        if (updateData.consultation) {
          consultations.value[index] = updateData.consultation
        }
        console.log('💬 收到新回复消息:', updateData.consultationNo)
      }
    }
    
    // 实时更新统计数据
    loadStatusCounts()
  } catch (error) {
    console.error('处理实时更新失败:', error)
  }
}

// 🔥 订阅单个咨询的消息更新
const subscribeToConsultationMessages = (consultationNo) => {
  if (chatWebSocketManager.isConnected()) {
    chatWebSocketManager.subscribeToConsultation(consultationNo, (message) => {
      handleNewMessage(consultationNo, message)
    })
    console.log('🔔 已订阅咨询消息:', consultationNo)
  }
}

// 初始化WebSocket
const initializeWebSocket = async () => {
  try {
    console.log('🔌 初始化WebSocket连接...')
    await chatStore.initializeWebSocket()
    
    // 订阅医生咨询列表更新
    if (userStore?.userInfo?.id) {
      console.log('📡 订阅医生咨询列表更新:', userStore.userInfo.id)
      chatStore.subscribeToDoctorConsultationList(userStore.userInfo.id, handleRepliesUpdate)
    }
    
    // 🔥 订阅所有现有咨询的消息更新
    subscribeToAllConsultationMessages()
    
  } catch (error) {
    console.error('❌ 初始化WebSocket失败:', error)
  }
}

// 🔥 订阅所有咨询的消息更新
const subscribeToAllConsultationMessages = () => {
  if (consultations.value && consultations.value.length > 0) {
    consultations.value.forEach(consultation => {
      // 只为进行中的咨询订阅消息更新
      if (consultation.status === 1 || consultation.status === 2) {
        subscribeToConsultationMessages(consultation.consultationNo)
      }
    })
    console.log(`🔔 已订阅 ${consultations.value.length} 个咨询的消息更新`)
  }
}

// 删除重复的handleRepliesUpdate定义，使用更完善的版本

// 生命周期
onMounted(async () => {
  console.log('🚀 咨询管理页面已挂载')
  
  // 首先加载数据
  await loadConsultations()
  await loadStatusCounts()
  
  // 然后初始化WebSocket（此时consultations.value已有数据）
  await initializeWebSocket()
  
  console.log('✅ 咨询管理页面初始化完成')
})

onBeforeUnmount(() => {
  console.log('🗑️ 咨询管理组件即将卸载')
  
  // 清理WebSocket订阅
  if (chatStore.wsConnected) {
    try {
      // 🔥 清理所有咨询的消息订阅
      if (consultations.value && consultations.value.length > 0) {
        consultations.value.forEach(consultation => {
          chatWebSocketManager.unsubscribe(`consultation_${consultation.consultationNo}`)
        })
        console.log('🧹 已清理所有咨询的消息订阅')
      }
      
      // 取消订阅医生咨询列表更新
      if (userStore?.userInfo?.id) {
        console.log('🧹 清理医生咨询列表订阅:', userStore.userInfo.id)
      }
    } catch (error) {
      console.error('清理WebSocket订阅失败:', error)
    }
  }
  
  console.log('✅ 咨询管理组件已卸载')
})
</script>

<style scoped>
.consultation-management {
  padding: 20px;
  background: #f5f7fa;
  min-height: calc(100vh - 60px);
}

/* 页面头部 */
.page-header {
  margin-bottom: 24px;
}

.header-content {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.page-title {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 24px;
  font-weight: 600;
  color: #1e293b;
  margin: 0;
}

.page-desc {
  color: #64748b;
  margin: 0;
  font-size: 14px;
}

/* 统计看板 */
.stats-dashboard {
  margin-bottom: 24px;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 16px;
}

.stat-card {
  background: white;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  display: flex;
  align-items: center;
  gap: 16px;
  cursor: pointer;
  transition: all 0.3s ease;
  border: 2px solid transparent;
}

.stat-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1);
}

.stat-card.today { border-color: #10b981; }
.stat-card.pending { border-color: #f59e0b; }
.stat-card.ongoing { border-color: #3b82f6; }
.stat-card.completed { border-color: #10b981; }
.stat-card.total { border-color: #6b7280; }

.stat-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
}

.stat-card.today .stat-icon { background: #ecfdf5; color: #10b981; }
.stat-card.pending .stat-icon { background: #fef3c7; color: #f59e0b; }
.stat-card.ongoing .stat-icon { background: #dbeafe; color: #3b82f6; }
.stat-card.completed .stat-icon { background: #ecfdf5; color: #10b981; }
.stat-card.total .stat-icon { background: #f3f4f6; color: #6b7280; }

.stat-value {
  font-size: 28px;
  font-weight: 700;
  color: #1e293b;
  line-height: 1;
}

.stat-label {
  font-size: 14px;
  color: #64748b;
  margin-top: 4px;
}

/* 筛选区域 */
.filter-section {
  background: white;
  border-radius: 12px;
  padding: 20px;
  margin-bottom: 24px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.filter-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.filter-right {
  display: flex;
  align-items: center;
  gap: 12px;
}

.view-mode-toggle {
  border: 1px solid #e2e8f0;
  border-radius: 8px;
}

/* 咨询列表 */
.consultation-list {
  background: white;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  overflow: hidden;
}

/* 🔥 红点提示样式 */
.status-with-indicator {
  position: relative;
  display: inline-flex;
  align-items: center;
}

.unread-dot {
  position: absolute;
  top: -2px;
  right: -2px;
  width: 8px;
  height: 8px;
  background: #f56c6c;
  border-radius: 50%;
  border: 2px solid white;
  box-shadow: 0 0 0 1px rgba(0, 0, 0, 0.1);
}

/* 列表视图 */
.consultation-item {
  padding: 20px;
  border-bottom: 1px solid #f1f5f9;
  transition: all 0.3s ease;
  position: relative;
}

.consultation-item:hover {
  background: #f8fafc;
}

.consultation-item.unread {
  background: #f0f9ff;
  border-left: 4px solid #3b82f6;
}

.consultation-item:last-child {
  border-bottom: none;
}

.item-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 12px;
}

.patient-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.patient-details {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.patient-name {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  font-weight: 600;
  color: #1e293b;
}

.consultation-no {
  font-size: 12px;
  color: #64748b;
}

.item-meta {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 8px;
}

.time-info {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 2px;
  font-size: 12px;
  color: #64748b;
}

.item-content {
  margin-bottom: 16px;
}

.chief-complaint {
  color: #374151;
  margin-bottom: 8px;
  line-height: 1.5;
}

.last-reply {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #64748b;
  font-size: 14px;
  background: #f8fafc;
  padding: 8px 12px;
  border-radius: 8px;
  margin-bottom: 8px;
}

.reply-label {
  font-weight: 500;
}

.reply-content {
  color: #374151;
}

/* 🔥 消息统计样式 */
.message-stats {
  display: flex;
  gap: 16px;
  align-items: center;
  margin: 8px 0;
  font-size: 12px;
}

.message-count, .reply-count {
  display: flex;
  align-items: center;
  gap: 4px;
  color: #64748b;
}

.unread-count {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #dc2626;
  font-weight: 500;
}

/* 🔥 最后回复预览样式 */
.last-reply-preview {
  background: #f1f5f9;
  padding: 8px 12px;
  border-radius: 6px;
  margin-top: 8px;
  border-left: 3px solid #3b82f6;
  font-size: 13px;
}

.reply-label {
  font-weight: 500;
  color: #374151;
  margin-bottom: 2px;
}

.reply-content {
  color: #6b7280;
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.item-actions {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

/* 卡片视图 */
.consultation-cards {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 16px;
  padding: 20px;
}

.consultation-card {
  background: white;
  border: 1px solid #e2e8f0;
  border-radius: 12px;
  padding: 16px;
  cursor: pointer;
  transition: all 0.3s ease;
  position: relative;
}

.consultation-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1);
  border-color: #3b82f6;
}

.card-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
}

.card-title {
  flex: 1;
}

.card-content {
  color: #64748b;
  font-size: 14px;
  line-height: 1.5;
}

/* 🔥 卡片视图增强样式 */
.consultation-card.has-unread {
  border-color: #3b82f6;
  box-shadow: 0 2px 8px rgba(59, 130, 246, 0.15);
}

.card-stats {
  display: flex;
  gap: 12px;
  margin: 8px 0;
  font-size: 12px;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 4px;
  color: #64748b;
}

.stat-item.unread {
  color: #dc2626;
  font-weight: 600;
}

.card-content .last-reply {
  background: #f8fafc;
  padding: 6px 8px;
  border-radius: 4px;
  font-size: 12px;
  color: #6b7280;
  margin-top: 8px;
  border-left: 2px solid #e5e7eb;
  display: -webkit-box;
  -webkit-line-clamp: 1;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

/* 分页 */
.pagination-section {
  display: flex;
  justify-content: center;
  margin-top: 24px;
}

/* 空状态 */
.empty-state {
  padding: 60px 20px;
  text-align: center;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .consultation-management {
    padding: 16px;
  }
  
  .filter-section {
    flex-direction: column;
    gap: 16px;
    align-items: stretch;
  }
  
  .filter-right {
    justify-content: space-between;
  }
  
  .stats-grid {
    grid-template-columns: repeat(auto-fit, minmax(150px, 1fr));
  }
  
  .consultation-cards {
    grid-template-columns: 1fr;
  }
  
  .item-header {
    flex-direction: column;
    gap: 12px;
  }
  
  .item-meta {
    align-items: flex-start;
  }
}
</style> 