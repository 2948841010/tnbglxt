<template>
  <div class="my-consultations">
    <div class="header">
      <h2>我的咨询</h2>
      <p class="subtitle">查看和管理您的问诊记录</p>
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

      <!-- 问诊列表 -->
      <div v-if="loading" class="loading">
        <el-skeleton :rows="3" animated />
      </div>

      <div v-else-if="consultations.length === 0" class="empty">
        <el-empty description="暂无问诊记录">
          <el-button type="primary" @click="goToDoctorList">开始问诊</el-button>
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
            <el-tag :type="getStatusType(consultation.status)">
              {{ getStatusText(consultation.status) }}
            </el-tag>
          </div>
          
          <div class="consultation-info">
            <div class="doctor-info">
              <UserAvatar 
                :src="consultation.doctorInfo?.avatar"
                :username="consultation.doctorInfo?.name"
                size="medium"
              />
              <div class="doctor-details">
                <p class="doctor-name">{{ consultation.doctorInfo?.name }}</p>
                <p class="doctor-dept">{{ consultation.doctorInfo?.department }} · {{ consultation.doctorInfo?.title }}</p>
              </div>
            </div>
            
            <div class="consultation-details">
              <p><strong>主诉：</strong>{{ consultation.chiefComplaint }}</p>
              <p><strong>问诊类型：</strong>{{ getConsultationType(consultation.consultationType) }}</p>
              <p><strong>创建时间：</strong>{{ formatTime(consultation.createTime) }}</p>
              <p v-if="consultation.fee && consultation.fee > 0">
                <strong>费用：</strong>¥{{ consultation.fee }}
              </p>
            </div>
          </div>
          
          <div class="consultation-actions">
            <el-button 
              v-if="consultation.status === 2" 
              type="primary" 
              size="small" 
              @click.stop="enterChat(consultation)">
              进入聊天
            </el-button>
            <el-button 
              v-if="consultation.status === 1" 
              type="warning" 
              size="small" 
              @click.stop="cancelConsultation(consultation)">
              取消问诊
            </el-button>
            <el-button 
              size="small" 
              @click.stop="viewConsultationDetail(consultation)">
              查看详情
            </el-button>
          </div>
        </div>
      </div>

      <!-- 分页 -->
      <div v-if="consultations.length > 0" class="pagination">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :total="total"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handlePageChange"
        />
      </div>
    </div>
  </div>
</template>

<script>
import { getMyConsultationsPage, cancelConsultation } from '@/api/chat'
import { ElMessage, ElMessageBox } from 'element-plus'
import { mapStores } from 'pinia'
import { useChatStore } from '@/stores/chat'
import { useUserStore } from '@/stores/user'
import UserAvatar from '@/components/common/UserAvatar.vue'

export default {
  name: 'MyConsultations',
  components: {
    UserAvatar
  },
  data() {
    return {
      loading: false,
      consultations: [],
      statusFilter: '',
      currentPage: 1,
      pageSize: 10,
      total: 0
    }
  },
  computed: {
    ...mapStores(useChatStore, useUserStore),
    
    filteredConsultations() {
      if (!this.statusFilter) {
        return this.consultations
      }
      return this.consultations.filter(item => item.status === parseInt(this.statusFilter))
    },

    // 实时统计数据
    pendingCount() {
      return this.consultations.filter(c => c.status === 1).length
    },

    ongoingCount() {
      return this.consultations.filter(c => c.status === 2).length
    },

    completedCount() {
      return this.consultations.filter(c => c.status === 3).length
    },

    cancelledCount() {
      return this.consultations.filter(c => c.status === 4).length
    },

    totalCount() {
      return this.consultations.length
    }
  },
  async mounted() {
    await this.loadConsultations()
    await this.initializeWebSocketForPatient()
  },
  methods: {
    async loadConsultations() {
      this.loading = true
      try {
        console.log('正在获取问诊列表...', { currentPage: this.currentPage, pageSize: this.pageSize })
        const response = await getMyConsultationsPage(this.currentPage, this.pageSize)
        console.log('API响应:', response)
        
        if (response.code === 200) {
          const result = response.data
          console.log('分页数据:', result)
          this.consultations = result.records || []  // 修正：使用 records 字段
          this.total = result.total || 0
          console.log('设置问诊数据:', this.consultations)
        } else {
          console.error('获取失败:', response)
          ElMessage.error(response.message || '获取问诊列表失败')
        }
      } catch (error) {
        console.error('获取问诊列表失败:', error)
        ElMessage.error('获取问诊列表失败')
      } finally {
        this.loading = false
      }
    },

    // 初始化WebSocket连接（用于患者端）
    async initializeWebSocketForPatient() {
      try {
        await this.chatStore.initializeWebSocket()
        
        // 订阅所有活跃咨询的消息更新
        const activeConsultations = this.consultations
          .filter(c => c.status === 1 || c.status === 2) // 待接诊和进行中
          .map(c => c.consultationNo)
        
        if (activeConsultations.length > 0) {
          this.chatStore.subscribeToMultipleConsultations(activeConsultations)
        }

        // 订阅患者咨询列表的实时更新
        if (this.userStore?.userInfo?.id) {
          // 患者端使用用户ID订阅自己的咨询更新
          this.chatStore.subscribeToDoctorConsultationList(`patient_${this.userStore.userInfo.id}`, this.handlePatientListUpdate)
        }
      } catch (error) {
        console.error('初始化WebSocket失败:', error)
      }
    },

    // 处理患者列表更新
    handlePatientListUpdate(updateData) {
      console.log('[MyConsultations] 收到患者列表更新:', updateData)
      
      if (updateData.type === 'consultation_status_changed') {
        this.handleConsultationStatusChange(updateData.consultationNo, updateData.newStatus, updateData.consultation)
      } else if (updateData.type === 'consultation_accepted') {
        this.handleConsultationAccepted(updateData.consultationNo, updateData.consultation)
      } else if (updateData.type === 'consultation_completed') {
        this.handleConsultationCompleted(updateData.consultationNo, updateData.consultation)
      }
    },

    // 处理咨询状态变更
    handleConsultationStatusChange(consultationNo, newStatus, updatedConsultation) {
      const index = this.consultations.findIndex(c => c.consultationNo === consultationNo)
      if (index !== -1) {
        // 更新现有咨询的状态
        if (updatedConsultation) {
          this.consultations.splice(index, 1, updatedConsultation)
        } else {
          this.consultations[index].status = newStatus
        }
      }
    },

    // 处理咨询被接诊
    handleConsultationAccepted(consultationNo, updatedConsultation) {
      const index = this.consultations.findIndex(c => c.consultationNo === consultationNo)
      if (index !== -1) {
        if (updatedConsultation) {
          this.consultations.splice(index, 1, updatedConsultation)
        } else {
          this.consultations[index].status = 2 // 进行中
        }
        
        // 显示接诊通知
        ElMessage.success('医生已接诊，可以开始对话了')
      }
    },

    // 处理问诊完成
    handleConsultationCompleted(consultationNo, updatedConsultation) {
      const index = this.consultations.findIndex(c => c.consultationNo === consultationNo)
      if (index !== -1) {
        if (updatedConsultation) {
          this.consultations.splice(index, 1, updatedConsultation)
        } else {
          this.consultations[index].status = 3 // 已完成
        }
        
        // 显示完成通知
        ElMessage.info('问诊已完成')
      }
    },

    handleStatusChange() {
      // 状态筛选在前端进行，不需要重新请求
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

    async cancelConsultation(consultation) {
      try {
        await ElMessageBox.confirm(
          '确定要取消这个问诊吗？取消后将无法恢复。',
          '确认取消',
          {
            confirmButtonText: '确定',
            cancelButtonText: '取消',
            type: 'warning',
          }
        )

        const response = await cancelConsultation(consultation.consultationNo)
        console.log('取消问诊响应:', response)
        if (response.code === 200) {
          ElMessage.success('问诊已取消')
          
          // 不需要手动刷新列表，WebSocket 会自动更新
          // this.loadConsultations()
          
          // 本地立即更新状态（WebSocket 更新可能有延迟）
          const index = this.consultations.findIndex(c => c.consultationNo === consultation.consultationNo)
          if (index !== -1) {
            this.consultations[index].status = 4 // 已取消
          }
        } else {
          ElMessage.error(response.message || '取消问诊失败')
        }
      } catch (error) {
        if (error !== 'cancel') {
          console.error('取消问诊失败:', error)
          ElMessage.error('取消问诊失败')
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

    goToDoctorList() {
      this.$router.push({ name: 'DoctorList' })
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

    getConsultationType(type) {
      const types = {
        1: '图文问诊',
        2: '语音问诊',
        3: '视频问诊'
      }
      return types[type] || '未知'
    },

    formatTime(timeStr) {
      if (!timeStr) return ''
      const date = new Date(timeStr)
      return date.toLocaleString('zh-CN', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit'
      })
    }
  }
}
</script>

<style scoped>
.my-consultations {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
}

.header {
  margin-bottom: 24px;
}

.header h2 {
  margin: 0 0 8px 0;
  color: #1f2937;
  font-size: 24px;
  font-weight: 600;
}

.subtitle {
  margin: 0;
  color: #6b7280;
  font-size: 14px;
}

.content {
  background: white;
  border-radius: 8px;
  padding: 24px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

.filters {
  margin-bottom: 20px;
}

.loading, .empty {
  padding: 40px;
  text-align: center;
}

.consultation-list {
  space-y: 16px;
}

.consultation-item {
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  padding: 20px;
  margin-bottom: 16px;
  background: #f9fafb;
  cursor: pointer;
  transition: all 0.2s ease;
}

.consultation-item:hover {
  background: #f3f4f6;
  border-color: #d1d5db;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.consultation-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.consultation-no {
  font-weight: 600;
  color: #1f2937;
  font-family: monospace;
  font-size: 16px;
}

.consultation-info {
  display: grid;
  grid-template-columns: auto 1fr;
  gap: 20px;
  margin-bottom: 16px;
}

.doctor-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.doctor-details {
  min-width: 0;
}

.doctor-name {
  margin: 0;
  font-weight: 600;
  color: #1f2937;
  font-size: 16px;
}

.doctor-dept {
  margin: 4px 0 0 0;
  color: #6b7280;
  font-size: 14px;
}

.consultation-details p {
  margin: 8px 0;
  color: #4b5563;
  font-size: 14px;
  line-height: 1.5;
}

.consultation-actions {
  display: flex;
  gap: 8px;
  justify-content: flex-end;
}

.pagination {
  margin-top: 24px;
  display: flex;
  justify-content: center;
}
</style> 