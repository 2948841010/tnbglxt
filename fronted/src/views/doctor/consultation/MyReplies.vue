<template>
  <div class="doctor-my-replies">
    <div class="header">
      <h2>我的回复</h2>
      <p class="subtitle">查看您已回复的咨询记录和患者反馈</p>
    </div>

    <div class="content">
      <!-- 状态筛选 -->
      <div class="filters">
        <el-radio-group v-model="statusFilter" @change="handleStatusChange">
          <el-radio-button label="">全部回复 ({{ totalReplied }})</el-radio-button>
          <el-radio-button label="2">进行中 ({{ ongoingCount }})</el-radio-button>
          <el-radio-button label="3">已完成 ({{ completedCount }})</el-radio-button>
        </el-radio-group>
      </div>

      <!-- 统计信息 -->
      <div class="stats-section">
        <div class="stat-item">
          <div class="stat-number">{{ totalReplied }}</div>
          <div class="stat-label">总回复数</div>
        </div>
        <div class="stat-item">
          <div class="stat-number">{{ ongoingCount }}</div>
          <div class="stat-label">进行中</div>
        </div>
        <div class="stat-item">
          <div class="stat-number">{{ completedCount }}</div>
          <div class="stat-label">已完成</div>
        </div>
      </div>

      <!-- 回复列表 -->
      <div v-if="loading" class="loading">
        <el-skeleton :rows="3" animated />
      </div>

      <div v-else-if="consultations.length === 0" class="empty">
        <el-empty description="暂无回复记录">
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
                <p class="reply-time">
                  回复时间：{{ formatTime(getLastReplyTime(consultation)) }}
                </p>
                <p v-if="consultation.endTime" class="end-time">
                  完成时间：{{ formatTime(consultation.endTime) }}
                </p>
              </div>
              <div class="message-stats">
                <div class="message-count">
                  <el-icon><ChatDotRound /></el-icon>
                  <span>{{ consultation.messages?.length || 0 }} 条消息</span>
                </div>
                <div class="reply-count">
                  <el-icon><EditPen /></el-icon>
                  <span>{{ getReplyCount(consultation) }} 条回复</span>
                </div>
              </div>
            </div>
          </div>

          <!-- 最后一条回复预览 -->
          <div class="last-reply-preview" v-if="getLastDoctorReply(consultation)">
            <div class="reply-label">最后回复：</div>
            <div class="reply-content">{{ getLastDoctorReply(consultation).content }}</div>
          </div>
          
          <div class="consultation-actions">
            <!-- 进行中状态的操作 -->
            <template v-if="consultation.status === 2">
              <el-button 
                type="primary" 
                size="small" 
                @click.stop="enterChat(consultation)">
                <el-icon><ChatDotRound /></el-icon>
                继续对话
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
              <div class="rating-display" v-if="consultation.rating">
                <el-rate 
                  v-model="consultation.rating.score" 
                  disabled 
                  size="small"
                  :colors="['#F56C6C', '#E6A23C', '#67C23A']">
                </el-rate>
                <span class="rating-score">{{ consultation.rating.score }}.0</span>
              </div>
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
import { getDoctorRepliedConsultations, completeConsultation, getOngoingConsultationsCount, getCompletedConsultationsCount } from '@/api/chat'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  ChatDotRound, 
  EditPen,
  CircleCheck, 
  View 
} from '@element-plus/icons-vue'
import { mapStores } from 'pinia'
import { useChatStore } from '@/stores/chat'
import { useUserStore } from '@/stores/user'
import UserAvatar from '@/components/common/UserAvatar.vue'

export default {
  name: 'DoctorMyReplies',
  components: {
    ChatDotRound,
    EditPen,
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
        ongoing: 0,    // 进行中
        completed: 0   // 已完成
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
    
    totalReplied() {
      return this.statusCounts.total
    },
    
    ongoingCount() {
      return this.statusCounts.ongoing
    },
    
    completedCount() {
      return this.statusCounts.completed
    }
  },
  async mounted() {
    await Promise.all([
      this.loadConsultations(),
      this.loadStatusCounts(),
      this.initializeWebSocketForReplies()
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
        console.log('正在获取医生回复列表...', { currentPage: this.currentPage, pageSize: this.pageSize })
        const response = await getDoctorRepliedConsultations(this.currentPage, this.pageSize)
        console.log('医生回复列表响应:', response)
        
        if (response.code === 200) {
          const result = response.data
          console.log('分页数据:', result)
          this.consultations = result.records || []
          this.total = result.total || 0
          console.log('设置回复数据:', this.consultations)
        } else {
          console.error('获取失败:', response)
          ElMessage.error(response.message || '获取回复列表失败')
        }
      } catch (error) {
        console.error('获取回复列表失败:', error)
        ElMessage.error('获取回复列表失败')
      } finally {
        this.loading = false
      }
    },

    // 加载各状态的统计数据
    async loadStatusCounts() {
      try {
        // 获取回复相关的统计数据
        const [ongoingRes, completedRes, totalRes] = await Promise.all([
          getOngoingConsultationsCount(),   // 进行中
          getCompletedConsultationsCount(), // 已完成
          getDoctorRepliedConsultations(1, 1) // 获取总的回复数（通过分页信息）
        ])

        // 更新统计数据
        if (ongoingRes.code === 200) {
          this.statusCounts.ongoing = ongoingRes.data  
        }

        if (completedRes.code === 200) {
          this.statusCounts.completed = completedRes.data
        }

        if (totalRes.code === 200) {
          this.statusCounts.total = totalRes.data.total || 0
        }

        console.log('[MyReplies] 统计数据已更新:', this.statusCounts)
        
      } catch (error) {
        console.error('获取回复统计失败:', error)
      }
    },

    // 初始化WebSocket连接（用于回复列表页面）
    async initializeWebSocketForReplies() {
      try {
        await this.chatStore.initializeWebSocket()
        
        // 订阅所有进行中的咨询消息更新
        const ongoingConsultations = this.consultations
          .filter(c => c.status === 2) // 只订阅进行中的咨询
          .map(c => c.consultationNo)
        
        if (ongoingConsultations.length > 0) {
          this.chatStore.subscribeToMultipleConsultations(ongoingConsultations)
        }

        // 订阅医生回复列表的实时更新
        if (this.userStore?.userInfo?.id) {
          this.chatStore.subscribeToDoctorConsultationList(this.userStore.userInfo.id, this.handleRepliesUpdate)
        }
      } catch (error) {
        console.error('初始化WebSocket失败:', error)
      }
    },

    // 处理回复列表更新
    handleRepliesUpdate(updateData) {
      console.log('[MyReplies] 收到回复列表更新:', updateData)
      
      if (updateData.type === 'consultation_status_changed') {
        this.handleReplyStatusChange(updateData.consultationNo, updateData.newStatus, updateData.consultation)
      } else if (updateData.type === 'consultation_completed') {
        this.handleReplyCompleted(updateData.consultationNo, updateData.consultation)
      } else if (updateData.type === 'new_reply') {
        this.handleNewReply(updateData.consultation)
      }
      
      // 实时更新统计数据
      this.loadStatusCounts()
    },

    // 处理回复状态变更
    handleReplyStatusChange(consultationNo, newStatus, updatedConsultation) {
      console.log('[MyReplies] 处理状态变更:', { consultationNo, newStatus, updatedConsultation })
      
      const index = this.consultations.findIndex(c => c.consultationNo === consultationNo)
      if (index !== -1) {
        // 更新现有咨询的状态
        if (updatedConsultation) {
          this.consultations[index] = updatedConsultation
        } else {
          this.consultations[index].status = newStatus
        }
        
        // 强制触发computed属性重新计算
        this.$nextTick(() => {
          console.log('[MyReplies] 状态更新完成，当前统计:', {
            total: this.totalReplied,
            ongoing: this.ongoingCount,
            completed: this.completedCount
          })
        })
        
        // 如果状态变为已取消，根据当前筛选条件决定是否移除
        if (newStatus === 4 && this.statusFilter && this.statusFilter !== '4') {
          this.consultations.splice(index, 1)
          this.total = Math.max(0, this.total - 1)
        }
      } else {
        console.warn('[MyReplies] 未找到要更新的咨询:', consultationNo)
      }
    },

    // 处理问诊完成
    handleReplyCompleted(consultationNo, updatedConsultation) {
      console.log('[MyReplies] 处理问诊完成:', { consultationNo, updatedConsultation })
      
      const index = this.consultations.findIndex(c => c.consultationNo === consultationNo)
      if (index !== -1) {
        if (updatedConsultation) {
          this.consultations[index] = updatedConsultation
        } else {
          this.consultations[index].status = 3 // 已完成
        }
        
        console.log('[MyReplies] 问诊完成后统计:', {
          total: this.totalReplied,
          ongoing: this.ongoingCount,
          completed: this.completedCount
        })
      } else {
        console.warn('[MyReplies] 未找到要完成的咨询:', consultationNo)
      }
    },

    // 处理新回复（医生回复了某个咨询）
    handleNewReply(consultation) {
      console.log('[MyReplies] 处理新回复:', consultation)
      
      // 检查是否已经在列表中
      const existingIndex = this.consultations.findIndex(c => c.consultationNo === consultation.consultationNo)
      
      if (existingIndex === -1) {
        // 不在列表中，检查是否符合筛选条件
        if (!this.statusFilter || consultation.status.toString() === this.statusFilter) {
          this.consultations.unshift(consultation)
          this.total += 1
          
          console.log('[MyReplies] 新回复已添加，当前统计:', {
            total: this.totalReplied,
            ongoing: this.ongoingCount,
            completed: this.completedCount
          })
          
          // 如果是进行中状态，订阅消息更新并注册消息回调
          if (consultation.status === 2) {
            this.chatStore.subscribeToConsultationUpdates(consultation.consultationNo)
            this.registerSingleMessageCallback(consultation)
          }
        }
      } else {
        // 已在列表中，更新数据
        this.consultations[existingIndex] = consultation
        console.log('[MyReplies] 已更新现有回复记录')
        
        // 如果状态变为进行中且还没有注册回调，注册一个
        if (consultation.status === 2 && this.messageCallbacks && !this.messageCallbacks.has(consultation.consultationNo)) {
          this.registerSingleMessageCallback(consultation)
        }
      }
    },

    // 为单个咨询注册消息回调
    registerSingleMessageCallback(consultation) {
      if (consultation.status === 2) {
        const callback = (newMessage) => {
          this.handleNewMessage(consultation.consultationNo, newMessage)
        }
        
        if (!this.messageCallbacks) {
          this.messageCallbacks = new Map()
        }
        this.messageCallbacks.set(consultation.consultationNo, callback)
        this.chatStore.registerMessageUpdateCallback(consultation.consultationNo, callback)
        
        console.log(`[MyReplies] 已为咨询 ${consultation.consultationNo} 注册消息回调`)
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

    getLastReplyTime(consultation) {
      if (!consultation.messages || consultation.messages.length === 0) {
        return consultation.startTime || consultation.createTime
      }
      
      // 找到最后一条医生发送的消息
      const doctorMessages = consultation.messages.filter(m => m.senderType === 'doctor')
      if (doctorMessages.length === 0) {
        return consultation.startTime || consultation.createTime
      }
      
      return doctorMessages[doctorMessages.length - 1].sendTime
    },

    getReplyCount(consultation) {
      if (!consultation.messages) return 0
      return consultation.messages.filter(m => m.senderType === 'doctor').length
    },

    getLastDoctorReply(consultation) {
      if (!consultation.messages || consultation.messages.length === 0) return null
      
      const doctorMessages = consultation.messages.filter(m => m.senderType === 'doctor')
      if (doctorMessages.length === 0) return null
      
      return doctorMessages[doctorMessages.length - 1]
    },

    // 注册消息更新回调
    registerMessageCallbacks() {
      this.consultations.forEach(consultation => {
        if (consultation.status === 2) { // 只对进行中的咨询注册回调
          const callback = (newMessage) => {
            this.handleNewMessage(consultation.consultationNo, newMessage)
          }
          
          // 保存回调引用，方便取消注册
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
      console.log('[MyReplies] 收到新消息:', { consultationNo, newMessage })
      
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
          console.log(`[MyReplies] 消息已添加到咨询 ${consultationNo}，当前消息数: ${this.consultations[index].messages.length}`)
        }
      }
    }
  }
}
</script>

<style scoped>
.doctor-my-replies {
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

.stats-section {
  display: flex;
  padding: 20px;
  gap: 40px;
  border-bottom: 1px solid #e2e8f0;
  background: #fafbfc;
}

.stat-item {
  text-align: center;
}

.stat-number {
  font-size: 24px;
  font-weight: 600;
  color: #1e293b;
  margin-bottom: 4px;
}

.stat-label {
  font-size: 12px;
  color: #64748b;
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
  margin-bottom: 12px;
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

.reply-time {
  font-weight: 500;
  color: #10b981 !important;
}

.message-stats {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.message-count, .reply-count {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: #64748b;
}

.last-reply-preview {
  background: #f1f5f9;
  padding: 12px;
  border-radius: 8px;
  margin-bottom: 16px;
  border-left: 4px solid #3b82f6;
}

.reply-label {
  font-size: 12px;
  color: #64748b;
  margin-bottom: 4px;
}

.reply-content {
  font-size: 13px;
  color: #1e293b;
  line-height: 1.4;
  max-height: 40px;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.consultation-actions {
  display: flex;
  gap: 8px;
  justify-content: space-between;
  align-items: center;
}

.rating-display {
  display: flex;
  align-items: center;
  gap: 8px;
}

.rating-score {
  font-size: 14px;
  color: #64748b;
  font-weight: 500;
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
  .doctor-my-replies {
    padding: 16px;
  }
  
  .stats-section {
    flex-direction: column;
    gap: 20px;
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