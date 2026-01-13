<template>
  <div class="my-consultations">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-content">
        <div class="header-icon">
          <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke-width="1.5" stroke="currentColor" class="icon">
            <path stroke-linecap="round" stroke-linejoin="round" d="M9 12h3.75M9 15h3.75M9 18h3.75m3 .75H18a2.25 2.25 0 0 0 2.25-2.25V6.108c0-1.135-.845-2.098-1.976-2.192a48.424 48.424 0 0 0-1.123-.08m-5.801 0c-.065.21-.1.433-.1.664 0 .414.336.75.75.75h4.5a.75.75 0 0 0 .75-.75 2.25 2.25 0 0 0-.1-.664m-5.8 0A2.251 2.251 0 0 1 13.5 2.25H15c1.012 0 1.867.668 2.15 1.586m-5.8 0c-.376.023-.75.05-1.124.08C9.095 4.01 8.25 4.973 8.25 6.108V8.25m0 0H4.875c-.621 0-1.125.504-1.125 1.125v11.25c0 .621.504 1.125 1.125 1.125h9.75c.621 0 1.125-.504 1.125-1.125V9.375c0-.621-.504-1.125-1.125-1.125H8.25ZM6.75 12h.008v.008H6.75V12Zm0 3h.008v.008H6.75V15Zm0 3h.008v.008H6.75V18Z" />
          </svg>
        </div>
        <div class="header-text">
          <h1 class="page-title">我的咨询</h1>
          <p class="page-subtitle">查看和管理您的问诊记录</p>
        </div>
      </div>
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
          <el-button type="primary" @click="goToDoctorList" class="start-btn">开始问诊</el-button>
        </el-empty>
      </div>

      <div v-else class="consultation-list">
        <div class="consultation-item" v-for="consultation in filteredConsultations" :key="consultation.id" @click="viewConsultationDetail(consultation)">
          <div class="consultation-header">
            <span class="consultation-no">{{ consultation.consultationNo }}</span>
            <el-tag :type="getStatusType(consultation.status)" class="status-tag">{{ getStatusText(consultation.status) }}</el-tag>
          </div>
          
          <div class="consultation-info">
            <div class="doctor-info">
              <UserAvatar :src="consultation.doctorInfo?.avatar" :username="consultation.doctorInfo?.name" size="medium" />
              <div class="doctor-details">
                <p class="doctor-name">{{ consultation.doctorInfo?.name }}</p>
                <p class="doctor-dept">{{ consultation.doctorInfo?.department }} · {{ consultation.doctorInfo?.title }}</p>
              </div>
            </div>
            
            <div class="consultation-details">
              <p><strong>主诉：</strong>{{ consultation.chiefComplaint }}</p>
              <p><strong>问诊类型：</strong>{{ getConsultationType(consultation.consultationType) }}</p>
              <p><strong>创建时间：</strong>{{ formatTime(consultation.createTime) }}</p>
              <p v-if="consultation.fee && consultation.fee > 0"><strong>费用：</strong><span class="fee">¥{{ consultation.fee }}</span></p>
            </div>
          </div>
          
          <div class="consultation-actions">
            <el-button v-if="consultation.status === 2" type="primary" size="small" @click.stop="enterChat(consultation)" class="chat-btn">
              <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke-width="1.5" stroke="currentColor" class="btn-icon">
                <path stroke-linecap="round" stroke-linejoin="round" d="M8.625 12a.375.375 0 1 1-.75 0 .375.375 0 0 1 .75 0Zm0 0H8.25m4.125 0a.375.375 0 1 1-.75 0 .375.375 0 0 1 .75 0Zm0 0H12m4.125 0a.375.375 0 1 1-.75 0 .375.375 0 0 1 .75 0Zm0 0h-.375M21 12c0 4.556-4.03 8.25-9 8.25a9.764 9.764 0 0 1-2.555-.337A5.972 5.972 0 0 1 5.41 20.97a5.969 5.969 0 0 1-.474-.065 4.48 4.48 0 0 0 .978-2.025c.09-.457-.133-.901-.467-1.226C3.93 16.178 3 14.189 3 12c0-4.556 4.03-8.25 9-8.25s9 3.694 9 8.25Z" />
              </svg>
              进入聊天
            </el-button>
            <el-button v-if="consultation.status === 1" type="warning" size="small" @click.stop="cancelConsultation(consultation)" class="cancel-btn">取消问诊</el-button>
            <el-button size="small" @click.stop="viewConsultationDetail(consultation)" class="detail-btn">查看详情</el-button>
          </div>
        </div>
      </div>

      <!-- 分页 -->
      <div v-if="consultations.length > 0" class="pagination">
        <el-pagination v-model:current-page="currentPage" v-model:page-size="pageSize" :total="total" :page-sizes="[10, 20, 50]" layout="total, sizes, prev, pager, next, jumper" @size-change="handleSizeChange" @current-change="handlePageChange" />
      </div>
    </div>
  </div>
</template>


<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getMyConsultations as fetchMyConsultations, cancelConsultation as cancelConsultationAPI } from '@/api/chat'
import UserAvatar from '@/components/common/UserAvatar.vue'

const router = useRouter()

// 响应式数据
const loading = ref(false)
const consultations = ref([])
const statusFilter = ref('')
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

// 各状态数量统计
const totalCount = computed(() => consultations.value.length)
const pendingCount = computed(() => consultations.value.filter(c => c.status === 1).length)
const ongoingCount = computed(() => consultations.value.filter(c => c.status === 2).length)
const completedCount = computed(() => consultations.value.filter(c => c.status === 3).length)
const cancelledCount = computed(() => consultations.value.filter(c => c.status === 4).length)

// 筛选后的咨询列表
const filteredConsultations = computed(() => {
  if (!statusFilter.value) return consultations.value
  return consultations.value.filter(c => c.status === parseInt(statusFilter.value))
})

// 获取咨询列表
const getConsultations = async () => {
  loading.value = true
  try {
    const response = await fetchMyConsultations()
    if (response.code === 200) {
      consultations.value = response.data || []
      total.value = consultations.value.length
    } else {
      ElMessage.error(response.message || '获取咨询列表失败')
    }
  } catch (error) {
    console.error('获取咨询列表失败:', error)
    ElMessage.error('网络异常，请稍后重试')
  } finally {
    loading.value = false
  }
}

// 状态筛选变化
const handleStatusChange = () => {
  currentPage.value = 1
  getConsultations()
}

// 分页变化
const handlePageChange = (page) => {
  currentPage.value = page
  getConsultations()
}

const handleSizeChange = (size) => {
  pageSize.value = size
  currentPage.value = 1
  getConsultations()
}

// 获取状态类型
const getStatusType = (status) => {
  const types = { 1: 'warning', 2: 'primary', 3: 'success', 4: 'danger' }
  return types[status] || 'info'
}

// 获取状态文本
const getStatusText = (status) => {
  const texts = { 1: '待接诊', 2: '进行中', 3: '已完成', 4: '已取消' }
  return texts[status] || '未知'
}

// 获取咨询类型文本
const getConsultationType = (type) => {
  const types = { 1: '对话问诊', 2: '视频问诊', 3: '电话问诊' }
  return types[type] || '对话问诊'
}

// 格式化时间
const formatTime = (timeStr) => {
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

// 进入聊天
const enterChat = (consultation) => {
  router.push({ name: 'ConsultationChat', params: { id: consultation.consultationNo } })
}

// 查看详情
const viewConsultationDetail = (consultation) => {
  router.push({ name: 'ConsultationChat', params: { id: consultation.consultationNo } })
}

// 取消咨询
const cancelConsultation = async (consultation) => {
  try {
    await ElMessageBox.confirm('确定要取消这次咨询吗？', '取消咨询', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    const response = await cancelConsultationAPI(consultation.consultationNo)
    if (response.code === 200) {
      ElMessage.success('咨询已取消')
      getConsultations()
    } else {
      ElMessage.error(response.message || '取消失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('取消咨询失败:', error)
      ElMessage.error('操作失败，请重试')
    }
  }
}

// 跳转到医生列表
const goToDoctorList = () => {
  router.push('/consultation/doctors')
}

onMounted(() => {
  getConsultations()
})
</script>

<style scoped>
.my-consultations {
  padding: 24px;
  background: #F0FDFA;
  min-height: 100vh;
}

.page-header {
  background: linear-gradient(135deg, #ECFEFF 0%, #CFFAFE 100%);
  padding: 24px;
  border-radius: 16px;
  border: 1px solid #A5F3FC;
  margin-bottom: 24px;
}

.header-content {
  display: flex;
  align-items: center;
  gap: 16px;
}

.header-icon {
  width: 56px;
  height: 56px;
  background: linear-gradient(135deg, #0891B2 0%, #22D3EE 100%);
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.header-icon .icon {
  width: 32px;
  height: 32px;
  color: white;
}

.page-title {
  font-size: 24px;
  font-weight: 700;
  color: #164E63;
  margin: 0 0 4px 0;
}

.page-subtitle {
  font-size: 14px;
  color: #0891B2;
  margin: 0;
}

.content {
  background: white;
  border-radius: 16px;
  padding: 24px;
  border: 1px solid #E0F2FE;
}

.filters {
  margin-bottom: 24px;
}

.filters :deep(.el-radio-button__inner) {
  border-color: #A5F3FC;
  color: #0891B2;
}

.filters :deep(.el-radio-button__original-radio:checked + .el-radio-button__inner) {
  background: linear-gradient(135deg, #0891B2 0%, #22D3EE 100%);
  border-color: #0891B2;
  color: white;
}

.loading {
  padding: 40px 0;
}

.empty {
  padding: 60px 0;
  text-align: center;
}

.start-btn {
  background: linear-gradient(135deg, #0891B2 0%, #22D3EE 100%);
  border: none;
}

.consultation-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.consultation-item {
  background: #F0FDFA;
  border-radius: 12px;
  padding: 20px;
  border: 1px solid #A5F3FC;
  cursor: pointer;
  transition: all 0.3s ease;
}

.consultation-item:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 16px rgba(8, 145, 178, 0.15);
  border-color: #22D3EE;
}

.consultation-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.consultation-no {
  font-size: 14px;
  color: #67E8F9;
  font-weight: 500;
}

.status-tag {
  font-size: 12px;
}

.consultation-info {
  display: flex;
  gap: 20px;
  margin-bottom: 16px;
}

.doctor-info {
  display: flex;
  align-items: center;
  gap: 12px;
  min-width: 200px;
}

.doctor-details {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.doctor-name {
  font-size: 16px;
  font-weight: 600;
  color: #164E63;
  margin: 0;
}

.doctor-dept {
  font-size: 13px;
  color: #0891B2;
  margin: 0;
}

.consultation-details {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.consultation-details p {
  margin: 0;
  font-size: 14px;
  color: #164E63;
}

.consultation-details strong {
  color: #67E8F9;
}

.fee {
  color: #DC2626;
  font-weight: 600;
}

.consultation-actions {
  display: flex;
  gap: 12px;
  justify-content: flex-end;
  padding-top: 16px;
  border-top: 1px solid #E0F2FE;
}

.chat-btn {
  background: linear-gradient(135deg, #0891B2 0%, #22D3EE 100%);
  border: none;
  display: flex;
  align-items: center;
  gap: 4px;
}

.chat-btn:hover {
  background: linear-gradient(135deg, #0E7490 0%, #06B6D4 100%);
}

.btn-icon {
  width: 14px;
  height: 14px;
}

.cancel-btn {
  background: #FEF3C7;
  border-color: #F59E0B;
  color: #D97706;
}

.cancel-btn:hover {
  background: #FDE68A;
  border-color: #D97706;
  color: #B45309;
}

.detail-btn {
  border-color: #A5F3FC;
  color: #0891B2;
}

.detail-btn:hover {
  background: #ECFEFF;
  border-color: #22D3EE;
}

.pagination {
  margin-top: 24px;
  display: flex;
  justify-content: center;
}

@media (max-width: 768px) {
  .my-consultations {
    padding: 16px;
  }
  
  .page-header {
    padding: 16px;
  }
  
  .page-title {
    font-size: 20px;
  }
  
  .content {
    padding: 16px;
  }
  
  .consultation-info {
    flex-direction: column;
    gap: 12px;
  }
  
  .doctor-info {
    min-width: auto;
  }
  
  .consultation-actions {
    flex-wrap: wrap;
  }
}
</style>
