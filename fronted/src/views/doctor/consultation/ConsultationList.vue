<template>
  <div class="doctor-consultation-list">
    <div class="header">
      <h2>咨询列表</h2>
      <p class="subtitle">查看和处理患者的在线咨询</p>
    </div>

    <div class="content">
      <!-- 状态筛选 -->
      <div class="filters">
        <el-radio-group v-model="statusFilter" @change="handleStatusChange">
          <el-radio-button label="">全部 ({{ totalCount }})</el-radio-button>
          <el-radio-button label="1">待接诊 ({{ pendingCount }})</el-radio-button>
          <el-radio-button label="2">进行中 ({{ ongoingCount }})</el-radio-button>
          <el-radio-button label="3">已完成 ({{ completedCount }})</el-radio-button>
          <el-radio-button label="4">已取消 ({{ cancelledCount }})</el-radio-button>
        </el-radio-group>
      </div>

      <!-- 咨询列表 -->
      <div v-if="loading" class="loading">
        <el-skeleton :rows="3" animated />
      </div>

      <div v-else-if="consultations.length === 0" class="empty">
        <el-empty description="暂无患者咨询">
          <el-button type="primary" @click="goToDashboard">返回工作台</el-button>
        </el-empty>
      </div>

      <div v-else class="consultation-list">
        <div 
          class="consultation-item" 
          v-for="consultation in filteredConsultations" 
          :key="consultation.id"
          @click="viewConsultationDetail(consultation)">
          
          <div class="consultation-header">
            <span class="consultation-no">{{ consultation.consultationNo }}</span>
            <div class="header-right">
              <div class="status-with-indicator">
                <el-tag :type="getStatusType(consultation.status)">
                  {{ getStatusText(consultation.status) }}
                </el-tag>
                <!-- 新消息红点提示 -->
                <div 
                  v-if="hasUnreadMessage(consultation.consultationNo)" 
                  class="unread-dot">
                </div>
              </div>
            </div>
          </div>
          
          <div class="consultation-info">
            <div class="patient-info">
              <UserAvatar 
                :src="consultation.patientInfo?.avatar"
                :username="consultation.patientInfo?.name"
                size="medium"
              />
              <div class="patient-details">
                <h4>{{ consultation.patientInfo?.name }}</h4>
                <p class="patient-meta">
                  {{ consultation.patientInfo?.gender }} · {{ consultation.patientInfo?.age }}岁
                </p>
                <p class="chief-complaint">主诉：{{ consultation.chiefComplaint }}</p>
              </div>
            </div>
            
            <div class="consultation-meta">
              <div class="time-info">
                <p class="create-time">{{ formatTime(consultation.createTime) }}</p>
                <p v-if="consultation.startTime" class="start-time">
                  开始时间：{{ formatTime(consultation.startTime) }}
                </p>
              </div>
              <div class="message-count">
                <el-icon><ChatDotRound /></el-icon>
                <span>{{ consultation.messages?.length || 0 }}</span>
              </div>
            </div>
          </div>
          
          <div class="consultation-actions">
            <!-- 待接诊状态的操作 -->
            <template v-if="consultation.status === 1">
              <el-button 
                type="primary" 
                size="small" 
                @click.stop="acceptConsultation(consultation)">
                <el-icon><Check /></el-icon>
                接诊
              </el-button>
              <el-button 
                type="danger" 
                size="small" 
                @click.stop="rejectConsultation(consultation)">
                <el-icon><Close /></el-icon>
                拒接
              </el-button>
            </template>
            
            <!-- 进行中状态的操作 -->
            <template v-else-if="consultation.status === 2">
              <el-button 
                type="primary" 
                size="small" 
                @click.stop="enterChat(consultation)">
                <el-icon><ChatDotRound /></el-icon>
                进入对话
              </el-button>
              <el-button 
                type="success" 
                size="small" 
                @click.stop="completeConsultation(consultation)">
                <el-icon><CircleCheck /></el-icon>
                完成问诊
              </el-button>
            </template>
            
            <!-- 已完成状态的操作 -->
            <template v-else-if="consultation.status === 3">
              <el-button 
                type="default" 
                size="small" 
                @click.stop="viewConsultationDetail(consultation)">
                <el-icon><View /></el-icon>
                查看详情
              </el-button>
            </template>
          </div>
        </div>
      </div>

      <!-- 分页 -->
      <div class="pagination" v-if="total > 0">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 30, 50]"
          :total="total"
          layout="prev, pager, next, sizes, total"
          @current-change="handlePageChange"
          @size-change="handleSizeChange"
        />
      </div>
    </div>
  </div>
</template>

<script>
import { getDoctorConsultationsPage, acceptConsultation, completeConsultation, cancelConsultation, getWaitingConsultationsCount, getOngoingConsultationsCount, getTodayConsultationCount } from '@/api/chat'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  ChatDotRound, 
  Check, 
  Close, 
  CircleCheck, 
  View 
} from '@element-plus/icons-vue'
import { mapStores } from 'pinia'
import { useChatStore } from '@/stores/chat'
import { useUserStore } from '@/stores/user'
import UserAvatar from '@/components/common/UserAvatar.vue'

export default {
  name: 'DoctorConsultationList',
  components: {
    ChatDotRound,
    Check,
    Close,
    CircleCheck,
    View,
    UserAvatar
  },
  data() {
    return {
      loading: false,
      consultations: [],
      statusFilter: '',
      currentPage: 1,
      pageSize: 10,
      total: 0,
      messageCallbacks: null, // 存储消息回调引用
      // 各状态统计数据
      statusCounts: {
        total: 0,
        pending: 0,    // 待接诊
        ongoing: 0,    // 进行中
        completed: 0,  // 已完成
        cancelled: 0   // 已取消
      }
    }
  },
  computed: {
    ...mapStores(useChatStore, useUserStore),
    
    filteredConsultations() {
      if (!this.statusFilter) {
        return this.consultations
      }
      return this.consultations.filter(c => c.status.toString() === this.statusFilter)
    },

    // 实时统计数据（使用独立统计而非基于当前列表）
    pendingCount() {
      return this.statusCounts.pending
    },

    ongoingCount() {
      return this.statusCounts.ongoing
    },

    completedCount() {
      return this.statusCounts.completed
    },

    cancelledCount() {
      return this.statusCounts.cancelled
    },

    totalCount() {
      return this.statusCounts.total
    }
  },
  async mounted() {
    await Promise.all([
      this.loadConsultations(),
      this.loadStatusCounts(),
      this.initializeWebSocketForList()
    ])
    this.registerMessageCallbacks()
  },
  
  beforeUnmount() {
    this.unregisterMessageCallbacks()
  },
  methods: {
    async loadConsultations() {
      this.loading = true
      try {
        console.log('正在获取医生咨询列表...', { currentPage: this.currentPage, pageSize: this.pageSize, statusFilter: this.statusFilter })
        const response = await getDoctorConsultationsPage(this.currentPage, this.pageSize, this.statusFilter)
        console.log('医生咨询列表响应:', response)
        
        if (response.code === 200) {
          const result = response.data
          console.log('分页数据:', result)
          this.consultations = result.records || []
          this.total = result.total || 0
          console.log('设置咨询数据:', this.consultations)
        } else {
          console.error('获取失败:', response)
          ElMessage.error(response.message || '获取咨询列表失败')
        }
      } catch (error) {
        console.error('获取咨询列表失败:', error)
        ElMessage.error('获取咨询列表失败')
      } finally {
        this.loading = false
      }
    },

    // 初始化WebSocket连接（用于列表页面）
    async initializeWebSocketForList() {
      try {
        await this.chatStore.initializeWebSocket()
        
        // 订阅所有活跃咨询的消息更新
        const activeConsultations = this.consultations
          .filter(c => c.status === 1 || c.status === 2) // 待接诊和进行中
          .map(c => c.consultationNo)
        
        if (activeConsultations.length > 0) {
          this.chatStore.subscribeToMultipleConsultations(activeConsultations)
        }

        // 订阅医生咨询列表的实时更新
        if (this.userStore?.userInfo?.id) {
          this.chatStore.subscribeToDoctorConsultationList(this.userStore.userInfo.id, this.handleListUpdate)
        }
      } catch (error) {
        console.error('初始化WebSocket失败:', error)
      }
    },

    // 处理列表更新
    handleListUpdate(updateData) {
      console.log('[ConsultationList] 收到列表更新:', updateData)
      
      if (updateData.type === 'consultation_status_changed') {
        this.handleConsultationStatusChange(updateData.consultationNo, updateData.newStatus, updateData.consultation)
      } else if (updateData.type === 'new_consultation') {
        this.handleNewConsultation(updateData.consultation)
      } else if (updateData.type === 'consultation_completed') {
        this.handleConsultationCompleted(updateData.consultationNo, updateData.consultation)
      }
      
      // 实时更新统计数据
      this.loadStatusCounts()
    },

    // 处理咨询状态变更
    handleConsultationStatusChange(consultationNo, newStatus, updatedConsultation) {
      console.log('[ConsultationList] 处理状态变更:', { consultationNo, newStatus, updatedConsultation })
      
      const index = this.consultations.findIndex(c => c.consultationNo === consultationNo)
      if (index !== -1) {
        // 更新现有咨询的状态
        if (updatedConsultation) {
          // 在Vue 3中直接赋值即可触发响应式更新
          this.consultations[index] = updatedConsultation
        } else {
          // 直接更新状态
          this.consultations[index].status = newStatus
        }
        
        // 强制触发computed属性重新计算
        this.$nextTick(() => {
          console.log('[ConsultationList] 状态更新完成，当前统计:', {
            total: this.totalCount,
            pending: this.pendingCount,
            ongoing: this.ongoingCount,
            completed: this.completedCount,
            cancelled: this.cancelledCount
          })
        })
        
        // 如果状态变为已完成或已取消，根据当前筛选条件决定是否移除
        if ((newStatus === 3 || newStatus === 4) && this.statusFilter && this.statusFilter !== newStatus.toString()) {
          this.consultations.splice(index, 1)
          this.total = Math.max(0, this.total - 1)
        }
      } else {
        console.warn('[ConsultationList] 未找到要更新的咨询:', consultationNo)
      }
    },

    // 处理新咨询
    handleNewConsultation(consultation) {
      console.log('[ConsultationList] 处理新咨询:', consultation)
      
      // 检查是否已存在，避免重复添加
      const existingIndex = this.consultations.findIndex(c => c.consultationNo === consultation.consultationNo)
      if (existingIndex !== -1) {
        console.log('[ConsultationList] 咨询已存在，跳过添加:', consultation.consultationNo)
        return
      }
      
      // 检查是否符合当前筛选条件
      if (!this.statusFilter || consultation.status.toString() === this.statusFilter) {
        // 添加到列表开头
        this.consultations.unshift(consultation)
        this.total += 1
        
        console.log('[ConsultationList] 新咨询已添加，当前统计:', {
          total: this.totalCount,
          pending: this.pendingCount,
          ongoing: this.ongoingCount
        })
        
        // 如果是待接诊状态，订阅消息更新并注册消息回调
        if (consultation.status === 1 || consultation.status === 2) {
          this.chatStore.subscribeToConsultationUpdates(consultation.consultationNo)
          this.registerSingleMessageCallback(consultation)
        }
      }
    },

    // 处理问诊完成
    handleConsultationCompleted(consultationNo, updatedConsultation) {
      console.log('[ConsultationList] 处理问诊完成:', { consultationNo, updatedConsultation })
      
      const index = this.consultations.findIndex(c => c.consultationNo === consultationNo)
      if (index !== -1) {
        if (updatedConsultation) {
          this.consultations[index] = updatedConsultation
        } else {
          this.consultations[index].status = 3 // 已完成
        }
        
        console.log('[ConsultationList] 问诊完成后统计:', {
          total: this.totalCount,
          ongoing: this.ongoingCount,
          completed: this.completedCount
        })
        
        // 如果当前筛选不是"已完成"，则从列表移除
        if (this.statusFilter && this.statusFilter !== '3') {
          this.consultations.splice(index, 1)
          this.total = Math.max(0, this.total - 1)
        }
      } else {
        console.warn('[ConsultationList] 未找到要完成的咨询:', consultationNo)
      }
    },

    // 检查是否有未读消息（红点显示）
    hasUnreadMessage(consultationNo) {
      return this.chatStore.getUnreadCount(consultationNo) > 0
    },

    handleStatusChange() {
      this.currentPage = 1
      this.loadConsultations()
      // 状态筛选变更时，不需要重新加载统计数据，因为统计数据是独立的
    },

    handlePageChange(page) {
      this.currentPage = page
      this.loadConsultations()
    },

    handleSizeChange(size) {
      this.pageSize = size
      this.currentPage = 1
      this.loadConsultations()
    },

    async acceptConsultation(consultation) {
      try {
        await ElMessageBox.confirm(
          '确定接诊这个患者吗？接诊后将开始问诊对话。',
          '确认接诊',
          {
            confirmButtonText: '确定',
            cancelButtonText: '取消',
            type: 'info',
          }
        )

        const response = await acceptConsultation(consultation.consultationNo)
        console.log('接诊响应:', response)
        if (response.code === 200) {
          ElMessage.success('接诊成功')
          
          // 不需要手动刷新列表，WebSocket 会自动更新
          // this.loadConsultations()
          
          // 本地立即更新状态（WebSocket 更新可能有延迟）
          const index = this.consultations.findIndex(c => c.consultationNo === consultation.consultationNo)
          if (index !== -1) {
            this.consultations[index].status = 2 // 进行中
            
            // 订阅该咨询的消息更新
            this.chatStore.subscribeToConsultationUpdates(consultation.consultationNo)
          }
          
          // 直接进入对话
          this.enterChat(consultation)
        } else {
          ElMessage.error(response.message || '接诊失败')
        }
      } catch (error) {
        if (error !== 'cancel') {
          console.error('接诊失败:', error)
          ElMessage.error('接诊失败')
        }
      }
    },

    async rejectConsultation(consultation) {
      try {
        await ElMessageBox.confirm(
          '确定拒绝这个患者的咨询吗？拒绝后咨询将被取消。',
          '确认拒绝',
          {
            confirmButtonText: '确定',
            cancelButtonText: '取消',
            type: 'warning',
          }
        )

        const response = await cancelConsultation(consultation.consultationNo)
        console.log('拒绝咨询响应:', response)
        if (response.code === 200) {
          ElMessage.success('已拒绝咨询')
          
          // 不需要手动刷新列表，WebSocket 会自动更新
          // this.loadConsultations()
          
          // 本地立即更新状态（WebSocket 更新可能有延迟）
          const index = this.consultations.findIndex(c => c.consultationNo === consultation.consultationNo)
          if (index !== -1) {
            this.consultations[index].status = 4 // 已取消
            
            // 如果当前筛选不是"已取消"，则从列表移除
            if (this.statusFilter && this.statusFilter !== '4') {
              this.consultations.splice(index, 1)
              this.total = Math.max(0, this.total - 1)
            }
          }
        } else {
          ElMessage.error(response.message || '拒绝咨询失败')
        }
      } catch (error) {
        if (error !== 'cancel') {
          console.error('拒绝咨询失败:', error)
          ElMessage.error('拒绝咨询失败')
        }
      }
    },

    async completeConsultation(consultation) {
      try {
        await ElMessageBox.confirm(
          '确定完成这个问诊吗？完成后患者将无法继续发送消息。',
          '确认完成',
          {
            confirmButtonText: '确定',
            cancelButtonText: '取消',
            type: 'success',
          }
        )

        const response = await completeConsultation(consultation.consultationNo)
        console.log('完成问诊响应:', response)
        if (response.code === 200) {
          ElMessage.success('问诊已完成')
          
          // 不需要手动刷新列表，WebSocket 会自动更新
          // this.loadConsultations()
          
          // 本地立即更新状态（WebSocket 更新可能有延迟）
          const index = this.consultations.findIndex(c => c.consultationNo === consultation.consultationNo)
          if (index !== -1) {
            this.consultations[index].status = 3
            
            // 如果当前筛选不是"已完成"，则从列表移除
            if (this.statusFilter && this.statusFilter !== '3') {
              this.consultations.splice(index, 1)
              this.total = Math.max(0, this.total - 1)
            }
          }
        } else {
          ElMessage.error(response.message || '完成问诊失败')
        }
      } catch (error) {
        if (error !== 'cancel') {
          console.error('完成问诊失败:', error)
          ElMessage.error('完成问诊失败')
        }
      }
    },

    enterChat(consultation) {
      this.$router.push({
        name: 'ConsultationChat',
        params: { id: consultation.consultationNo }
      })
    },

    viewConsultationDetail(consultation) {
      this.$router.push({
        name: 'ConsultationChat',
        params: { id: consultation.consultationNo }
      })
    },

    goToDashboard() {
      this.$router.push('/doctor/dashboard')
    },

    getStatusType(status) {
      const types = {
        1: 'warning', // 待接诊
        2: 'primary', // 进行中
        3: 'success', // 已完成
        4: 'danger'   // 已取消
      }
      return types[status] || 'info'
    },

    getStatusText(status) {
      const texts = {
        1: '待接诊',
        2: '进行中',
        3: '已完成',
        4: '已取消'
      }
      return texts[status] || '未知'
    },

    formatTime(timeStr) {
      if (!timeStr) return ''
      const date = new Date(timeStr)
      const now = new Date()
      const diffTime = now - date
      const diffDays = Math.floor(diffTime / (1000 * 60 * 60 * 24))
      
      if (diffDays === 0) {
        return date.toLocaleTimeString('zh-CN', {
          hour: '2-digit',
          minute: '2-digit'
        })
      } else if (diffDays === 1) {
        return '昨天 ' + date.toLocaleTimeString('zh-CN', {
          hour: '2-digit',
          minute: '2-digit'
        })
      } else if (diffDays < 7) {
        return diffDays + '天前'
      } else {
        return date.toLocaleDateString('zh-CN', {
          month: '2-digit',
          day: '2-digit',
          hour: '2-digit',
          minute: '2-digit'
        })
      }
    },

    // 加载各状态的统计数据
    async loadStatusCounts() {
      try {
        // 并行获取各种统计数据
        const [waitingRes, ongoingRes, totalRes] = await Promise.all([
          getWaitingConsultationsCount(),   // 待接诊
          getOngoingConsultationsCount(),   // 进行中  
          getDoctorConsultationsPage(1, 1, '') // 获取总数（通过分页信息）
        ])

        // 获取已完成和已取消的数量
        const [completedRes, cancelledRes] = await Promise.all([
          getDoctorConsultationsPage(1, 1, '3'), // 已完成
          getDoctorConsultationsPage(1, 1, '4')  // 已取消
        ])

        // 更新统计数据
        if (waitingRes.code === 200) {
          this.statusCounts.pending = waitingRes.data
        }
        
        if (ongoingRes.code === 200) {
          this.statusCounts.ongoing = ongoingRes.data  
        }

        if (totalRes.code === 200) {
          this.statusCounts.total = totalRes.data.total || 0
        }

        if (completedRes.code === 200) {
          this.statusCounts.completed = completedRes.data.total || 0
        }

        if (cancelledRes.code === 200) {
          this.statusCounts.cancelled = cancelledRes.data.total || 0
        }

        console.log('[ConsultationList] 统计数据已更新:', this.statusCounts)
        
      } catch (error) {
        console.error('获取状态统计失败:', error)
      }
    },

    // 注册消息更新回调
    registerMessageCallbacks() {
      this.consultations.forEach(consultation => {
        if (consultation.status === 1 || consultation.status === 2) { // 待接诊和进行中
          const callback = (newMessage) => {
            this.handleNewMessage(consultation.consultationNo, newMessage)
          }
          
          if (!this.messageCallbacks) {
            this.messageCallbacks = new Map()
          }
          this.messageCallbacks.set(consultation.consultationNo, callback)
          
          this.chatStore.registerMessageUpdateCallback(consultation.consultationNo, callback)
        }
      })
    },

    // 取消注册消息更新回调
    unregisterMessageCallbacks() {
      if (this.messageCallbacks) {
        this.messageCallbacks.forEach((callback, consultationNo) => {
          this.chatStore.unregisterMessageUpdateCallback(consultationNo, callback)
        })
        this.messageCallbacks.clear()
      }
    },

    // 处理新消息
    handleNewMessage(consultationNo, newMessage) {
      console.log('[ConsultationList] 收到新消息:', { consultationNo, newMessage })
      
      const index = this.consultations.findIndex(c => c.consultationNo === consultationNo)
      if (index !== -1) {
        // 更新消息列表
        if (!this.consultations[index].messages) {
          this.consultations[index].messages = []
        }
        
        // 检查消息是否已存在，避免重复添加
        const messageExists = this.consultations[index].messages.some(m => m.messageId === newMessage.messageId)
        if (!messageExists) {
          this.consultations[index].messages.push(newMessage)
          console.log(`[ConsultationList] 消息已添加到咨询 ${consultationNo}，当前消息数: ${this.consultations[index].messages.length}`)
        }
      }
    },

    // 为单个咨询注册消息回调
    registerSingleMessageCallback(consultation) {
      if (consultation.status === 1 || consultation.status === 2) {
        const callback = (newMessage) => {
          this.handleNewMessage(consultation.consultationNo, newMessage)
        }
        
        if (!this.messageCallbacks) {
          this.messageCallbacks = new Map()
        }
        this.messageCallbacks.set(consultation.consultationNo, callback)
        this.chatStore.registerMessageUpdateCallback(consultation.consultationNo, callback)
        
        console.log(`[ConsultationList] 已为咨询 ${consultation.consultationNo} 注册消息回调`)
      }
    }
  }
}
</script>

<style scoped>
.doctor-consultation-list {
  padding: 20px;
  background: #f5f7fa;
  min-height: 100vh;
}

.header {
  background: white;
  padding: 24px;
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  margin-bottom: 20px;
}

.header h2 {
  margin: 0 0 8px 0;
  font-size: 24px;
  font-weight: 600;
  color: #1e293b;
}

.subtitle {
  margin: 0;
  color: #64748b;
  font-size: 14px;
}

.content {
  background: white;
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  overflow: hidden;
}

.filters {
  padding: 20px;
  border-bottom: 1px solid #e2e8f0;
}

.consultation-list {
  padding: 0;
}

.consultation-item {
  padding: 20px;
  border-bottom: 1px solid #e2e8f0;
  cursor: pointer;
  transition: all 0.3s ease;
}

.consultation-item:hover {
  background: #f8fafc;
}

.consultation-item:last-child {
  border-bottom: none;
}

.consultation-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.header-right {
  display: flex;
  align-items: center;
}

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

.consultation-no {
  font-size: 14px;
  color: #64748b;
  font-weight: 500;
}

.consultation-info {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 16px;
}

.patient-info {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  flex: 1;
}

.patient-details h4 {
  margin: 0 0 4px 0;
  font-size: 16px;
  font-weight: 600;
  color: #1e293b;
}

.patient-meta {
  margin: 0 0 8px 0;
  font-size: 12px;
  color: #94a3b8;
}

.chief-complaint {
  margin: 0;
  font-size: 13px;
  color: #64748b;
  line-height: 1.4;
  max-width: 400px;
}

.consultation-meta {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 8px;
}

.time-info {
  text-align: right;
}

.time-info p {
  margin: 0 0 4px 0;
  font-size: 12px;
  color: #94a3b8;
}

.create-time {
  font-weight: 500;
}

.message-count {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: #64748b;
}

.consultation-actions {
  display: flex;
  gap: 8px;
  justify-content: flex-end;
}

.loading, .empty {
  padding: 40px;
  text-align: center;
}

.pagination {
  padding: 20px;
  display: flex;
  justify-content: center;
  border-top: 1px solid #e2e8f0;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .doctor-consultation-list {
    padding: 16px;
  }
  
  .consultation-info {
    flex-direction: column;
    gap: 12px;
  }
  
  .consultation-meta {
    align-items: flex-start;
  }
  
  .consultation-actions {
    justify-content: flex-start;
    flex-wrap: wrap;
  }
}
</style> 