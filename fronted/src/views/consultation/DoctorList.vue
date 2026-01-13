<template>
  <div class="doctor-list-page">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-content">
        <div class="header-icon">
          <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke-width="1.5" stroke="currentColor" class="icon">
            <path stroke-linecap="round" stroke-linejoin="round" d="M18 18.72a9.094 9.094 0 0 0 3.741-.479 3 3 0 0 0-4.682-2.72m.94 3.198.001.031c0 .225-.012.447-.037.666A11.944 11.944 0 0 1 12 21c-2.17 0-4.207-.576-5.963-1.584A6.062 6.062 0 0 1 6 18.719m12 0a5.971 5.971 0 0 0-.941-3.197m0 0A5.995 5.995 0 0 0 12 12.75a5.995 5.995 0 0 0-5.058 2.772m0 0a3 3 0 0 0-4.681 2.72 8.986 8.986 0 0 0 3.74.477m.94-3.197a5.971 5.971 0 0 0-.94 3.197M15 6.75a3 3 0 1 1-6 0 3 3 0 0 1 6 0Zm6 3a2.25 2.25 0 1 1-4.5 0 2.25 2.25 0 0 1 4.5 0Zm-13.5 0a2.25 2.25 0 1 1-4.5 0 2.25 2.25 0 0 1 4.5 0Z" />
          </svg>
        </div>
        <div class="header-text">
          <h1 class="page-title">选择医生</h1>
          <p class="page-subtitle">选择合适的医生进行在线咨询</p>
        </div>
      </div>
    </div>

    <!-- 搜索和筛选 -->
    <div class="search-section">
      <el-row :gutter="16">
        <el-col :xs="24" :sm="8">
          <el-input v-model="searchForm.keyword" placeholder="搜索医生姓名、科室或专长" clearable @input="handleSearch" @clear="handleSearch">
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
        </el-col>
        <el-col :xs="24" :sm="6">
          <el-select v-model="searchForm.department" placeholder="选择科室" clearable @change="handleSearch">
            <el-option v-for="dept in departments" :key="dept" :label="dept" :value="dept" />
          </el-select>
        </el-col>
        <el-col :xs="24" :sm="6">
          <el-button type="primary" @click="handleSearch" :loading="loading" class="search-btn">
            <el-icon><Search /></el-icon>
            搜索
          </el-button>
        </el-col>
      </el-row>
    </div>

    <!-- 医生卡片列表 -->
    <div class="doctor-grid">
      <el-row :gutter="20">
        <el-col :xs="24" :sm="12" :md="8" :lg="6" :xl="6" v-for="doctor in doctorList" :key="doctor.userInfo.id">
          <div class="doctor-card">
            <!-- 医生头像和在线状态 -->
            <div class="doctor-avatar-section">
              <div class="avatar-container">
                <el-avatar :size="80" :src="doctor.userInfo.avatar">{{ doctor.userInfo.realName?.charAt(0) || 'D' }}</el-avatar>
                <div class="online-status" :class="getOnlineStatusClass(doctor.onlineStatus)">
                  <span class="status-dot"></span>
                  {{ getOnlineStatusText(doctor.onlineStatus) }}
                </div>
              </div>
            </div>

            <!-- 医生基本信息 -->
            <div class="doctor-info">
              <h3 class="doctor-name">{{ doctor.userInfo.realName }}</h3>
              <div class="doctor-title">{{ doctor.title || '医师' }} · {{ doctor.department || '内科' }}</div>
              
              <!-- 医生评分 -->
              <div class="doctor-rating">
                <el-rate :model-value="doctor.rating || 5.0" disabled show-score text-color="#0891B2" size="small" />
                <span class="rating-text">({{ doctor.consultationCount || 0 }}次咨询)</span>
              </div>

              <!-- 医生专长 -->
              <div class="doctor-specialty" v-if="doctor.speciality">
                <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke-width="1.5" stroke="currentColor" class="specialty-icon">
                  <path stroke-linecap="round" stroke-linejoin="round" d="M11.48 3.499a.562.562 0 0 1 1.04 0l2.125 5.111a.563.563 0 0 0 .475.345l5.518.442c.499.04.701.663.321.988l-4.204 3.602a.563.563 0 0 0-.182.557l1.285 5.385a.562.562 0 0 1-.84.61l-4.725-2.885a.562.562 0 0 0-.586 0L6.982 20.54a.562.562 0 0 1-.84-.61l1.285-5.386a.562.562 0 0 0-.182-.557l-4.204-3.602a.562.562 0 0 1 .321-.988l5.518-.442a.563.563 0 0 0 .475-.345L11.48 3.5Z" />
                </svg>
                <span>{{ doctor.speciality }}</span>
              </div>

              <!-- 工作年限和医院 -->
              <div class="doctor-details">
                <div class="detail-item" v-if="doctor.workYears">
                  <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke-width="1.5" stroke="currentColor" class="detail-icon">
                    <path stroke-linecap="round" stroke-linejoin="round" d="M6.75 3v2.25M17.25 3v2.25M3 18.75V7.5a2.25 2.25 0 0 1 2.25-2.25h13.5A2.25 2.25 0 0 1 21 7.5v11.25m-18 0A2.25 2.25 0 0 0 5.25 21h13.5A2.25 2.25 0 0 0 21 18.75m-18 0v-7.5A2.25 2.25 0 0 1 5.25 9h13.5A2.25 2.25 0 0 1 21 11.25v7.5" />
                  </svg>
                  <span>{{ doctor.workYears }}年经验</span>
                </div>
                <div class="detail-item" v-if="doctor.hospital">
                  <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke-width="1.5" stroke="currentColor" class="detail-icon">
                    <path stroke-linecap="round" stroke-linejoin="round" d="M2.25 21h19.5m-18-18v18m10.5-18v18m6-13.5V21M6.75 6.75h.75m-.75 3h.75m-.75 3h.75m3-6h.75m-.75 3h.75m-.75 3h.75M6.75 21v-3.375c0-.621.504-1.125 1.125-1.125h2.25c.621 0 1.125.504 1.125 1.125V21M3 3h12m-.75 4.5H21m-3.75 3.75h.008v.008h-.008v-.008Zm0 3h.008v.008h-.008v-.008Zm0 3h.008v.008h-.008v-.008Z" />
                  </svg>
                  <span>{{ doctor.hospital }}</span>
                </div>
              </div>

              <!-- 咨询费用 -->
              <div class="consultation-fee" v-if="doctor.consultationFee">
                <span class="fee-label">咨询费用：</span>
                <span class="fee-amount">¥{{ doctor.consultationFee }}/次</span>
              </div>
            </div>

            <!-- 操作按钮 -->
            <div class="doctor-actions">
              <el-button size="small" class="detail-btn" @click="viewDoctorDetail(doctor)">查看详情</el-button>
              <el-button type="primary" size="small" class="consult-btn" @click="startConsultation(doctor)" :disabled="doctor.onlineStatus === 0">
                <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke-width="1.5" stroke="currentColor" class="btn-icon">
                  <path stroke-linecap="round" stroke-linejoin="round" d="M8.625 12a.375.375 0 1 1-.75 0 .375.375 0 0 1 .75 0Zm0 0H8.25m4.125 0a.375.375 0 1 1-.75 0 .375.375 0 0 1 .75 0Zm0 0H12m4.125 0a.375.375 0 1 1-.75 0 .375.375 0 0 1 .75 0Zm0 0h-.375M21 12c0 4.556-4.03 8.25-9 8.25a9.764 9.764 0 0 1-2.555-.337A5.972 5.972 0 0 1 5.41 20.97a5.969 5.969 0 0 1-.474-.065 4.48 4.48 0 0 0 .978-2.025c.09-.457-.133-.901-.467-1.226C3.93 16.178 3 14.189 3 12c0-4.556 4.03-8.25 9-8.25s9 3.694 9 8.25Z" />
                </svg>
                {{ doctor.onlineStatus === 0 ? '医生离线' : '立即咨询' }}
              </el-button>
            </div>
          </div>
        </el-col>
      </el-row>

      <!-- 空状态 -->
      <div v-if="!loading && doctorList.length === 0" class="empty-state">
        <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke-width="1.5" stroke="currentColor" class="empty-icon">
          <path stroke-linecap="round" stroke-linejoin="round" d="M18 18.72a9.094 9.094 0 0 0 3.741-.479 3 3 0 0 0-4.682-2.72m.94 3.198.001.031c0 .225-.012.447-.037.666A11.944 11.944 0 0 1 12 21c-2.17 0-4.207-.576-5.963-1.584A6.062 6.062 0 0 1 6 18.719m12 0a5.971 5.971 0 0 0-.941-3.197m0 0A5.995 5.995 0 0 0 12 12.75a5.995 5.995 0 0 0-5.058 2.772m0 0a3 3 0 0 0-4.681 2.72 8.986 8.986 0 0 0 3.74.477m.94-3.197a5.971 5.971 0 0 0-.94 3.197M15 6.75a3 3 0 1 1-6 0 3 3 0 0 1 6 0Zm6 3a2.25 2.25 0 1 1-4.5 0 2.25 2.25 0 0 1 4.5 0Zm-13.5 0a2.25 2.25 0 1 1-4.5 0 2.25 2.25 0 0 1 4.5 0Z" />
        </svg>
        <h3>暂无医生</h3>
        <p>请尝试调整搜索条件</p>
      </div>

      <!-- 加载状态 -->
      <div v-if="loading" class="loading-state">
        <el-skeleton :rows="3" animated />
        <el-skeleton :rows="3" animated />
        <el-skeleton :rows="3" animated />
      </div>
    </div>

    <!-- 分页 -->
    <div class="pagination-section" v-if="doctorList.length > 0">
      <el-pagination v-model:current-page="searchForm.page" :page-size="searchForm.size" :total="total" layout="prev, pager, next, sizes, total" :page-sizes="[10, 20, 30, 50]" @current-change="handlePageChange" @size-change="handleSizeChange" />
    </div>

    <!-- 发起咨询对话框 -->
    <el-dialog v-model="consultationDialog.visible" title="发起咨询" :width="500" @closed="resetConsultationForm" class="consultation-dialog">
      <div class="consultation-form">
        <!-- 医生信息 -->
        <div class="doctor-info-card">
          <el-avatar :size="60" :src="consultationDialog.doctor?.userInfo?.avatar">{{ consultationDialog.doctor?.userInfo?.realName?.charAt(0) }}</el-avatar>
          <div class="doctor-details">
            <h3>{{ consultationDialog.doctor?.userInfo?.realName }}</h3>
            <p>{{ consultationDialog.doctor?.department }} · {{ consultationDialog.doctor?.title }}</p>
            <p class="doctor-speciality">擅长：{{ consultationDialog.doctor?.speciality }}</p>
          </div>
        </div>

        <!-- 咨询表单 -->
        <el-form :model="consultationForm" :rules="consultationRules" ref="consultationFormRef" label-width="100px">
          <el-form-item label="咨询类型">
            <el-tag type="primary" size="large" class="type-tag">对话问诊</el-tag>
            <p class="type-desc">向医生简要介绍症状，医生回复后开启对话</p>
          </el-form-item>

          <el-form-item label="症状简述" prop="chiefComplaint" required>
            <el-input v-model="consultationForm.chiefComplaint" type="textarea" :rows="4" placeholder="请简要描述您的主要症状或想要咨询的问题，医生会根据您的描述进行初步回复..." maxlength="300" show-word-limit />
          </el-form-item>

          <el-form-item label="咨询费用">
            <div class="fee-info">
              <span class="fee-amount">¥{{ consultationDialog.doctor?.consultationFee || '免费' }}</span>
              <span class="fee-desc">对话问诊费用</span>
            </div>
          </el-form-item>
        </el-form>
      </div>

      <div class="process-tips">
        <el-alert title="问诊流程说明" type="info" :closable="false" show-icon>
          <p>1. 您提交症状描述后，医生会收到咨询通知</p>
          <p>2. 医生查看您的症状并进行初步回复</p>
          <p>3. 医生回复后，对话正式开启</p>
          <p>4. 您可以继续与医生进行详细交流</p>
        </el-alert>
      </div>

      <template #footer>
        <div class="dialog-footer">
          <el-button @click="consultationDialog.visible = false">取消</el-button>
          <el-button type="primary" @click="submitConsultation" :loading="submitting" class="submit-btn">提交症状给医生</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onBeforeUnmount } from 'vue'
import { useRouter } from 'vue-router'
import { consultationAPI } from '@/api/consultation'
import { createConsultation } from '@/api/chat'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search } from '@element-plus/icons-vue'
import webSocketManager from '@/utils/websocket'

const router = useRouter()

const loading = ref(false)
const doctorList = ref([])
const departments = ref([])
const total = ref(0)

const searchForm = reactive({ keyword: '', department: '', page: 1, size: 20 })
const consultationDialog = reactive({ visible: false, doctor: null })
const consultationForm = reactive({ consultationType: 1, chiefComplaint: '' })
const consultationRules = {
  chiefComplaint: [
    { required: true, message: '请简要描述症状', trigger: 'blur' },
    { min: 5, message: '症状描述至少5个字符', trigger: 'blur' },
    { max: 300, message: '症状描述不能超过300个字符', trigger: 'blur' }
  ]
}
const consultationFormRef = ref(null)
const submitting = ref(false)

const getDoctorList = async () => {
  loading.value = true
  try {
    const response = await consultationAPI.getDoctorList({ keyword: searchForm.keyword, department: searchForm.department, page: searchForm.page, size: searchForm.size })
    if (response.code === 200) {
      doctorList.value = response.data || []
      total.value = doctorList.value.length
    } else {
      ElMessage.error(response.message || '获取医生列表失败')
    }
  } catch (error) {
    console.error('获取医生列表失败:', error)
    ElMessage.error('网络异常，请稍后重试')
  } finally {
    loading.value = false
  }
}

const getDepartments = async () => {
  try {
    const allDepts = new Set()
    doctorList.value.forEach(doctor => { if (doctor.department) allDepts.add(doctor.department) })
    departments.value = Array.from(allDepts)
  } catch (error) {
    console.error('获取科室列表失败:', error)
  }
}

const handleSearch = () => { searchForm.page = 1; getDoctorList() }
const handlePageChange = (page) => { searchForm.page = page; getDoctorList() }
const handleSizeChange = (size) => { searchForm.size = size; searchForm.page = 1; getDoctorList() }

const getOnlineStatusClass = (status) => ({ 0: 'offline', 1: 'online', 2: 'busy' }[status] || 'offline')
const getOnlineStatusText = (status) => ({ 0: '离线', 1: '在线', 2: '忙碌' }[status] || '离线')

const viewDoctorDetail = (doctor) => {
  ElMessageBox.alert(`<div style="text-align: left;"><p><strong>医生姓名：</strong>${doctor.userInfo.realName}</p><p><strong>科室职称：</strong>${doctor.title} · ${doctor.department}</p><p><strong>从业年限：</strong>${doctor.workYears || '未知'}年</p><p><strong>所属医院：</strong>${doctor.hospital || '未知'}</p><p><strong>专业特长：</strong>${doctor.speciality || '暂无'}</p><p><strong>个人简介：</strong>${doctor.introduction || '暂无'}</p><p><strong>咨询费用：</strong>¥${doctor.consultationFee || '0'}/次</p><p><strong>服务评分：</strong>${doctor.rating || 5.0}分 (${doctor.consultationCount || 0}次咨询)</p></div>`, `${doctor.userInfo.realName}医生详情`, { dangerouslyUseHTMLString: true, confirmButtonText: '立即咨询', cancelButtonText: '关闭', showCancelButton: true, type: 'info' }).then(() => { startConsultation(doctor) }).catch(() => {})
}

const startConsultation = async (doctor) => {
  if (doctor.onlineStatus === 0) { ElMessage.warning('该医生当前离线，暂无法咨询'); return }
  showConsultationDialog(doctor)
}

const showConsultationDialog = (doctor) => { consultationDialog.doctor = doctor; consultationDialog.visible = true }

const resetConsultationForm = () => {
  consultationForm.consultationType = 1
  consultationForm.chiefComplaint = ''
  consultationDialog.doctor = null
  if (consultationFormRef.value) consultationFormRef.value.resetFields()
}

const submitConsultation = async () => {
  if (!consultationForm.chiefComplaint || consultationForm.chiefComplaint.trim().length < 5) { ElMessage.error('请输入至少5个字符的症状描述'); return }
  if (consultationForm.chiefComplaint.trim().length > 300) { ElMessage.error('症状描述不能超过300个字符'); return }
  submitting.value = true
  try {
    const requestData = { doctorId: consultationDialog.doctor.userInfo.id, consultationType: consultationForm.consultationType, chiefComplaint: consultationForm.chiefComplaint.trim(), fee: consultationDialog.doctor.consultationFee || 0 }
    const response = await createConsultation(requestData)
    if (response.code === 200) {
      const consultationData = response.data
      ElMessage.success('咨询发起成功！')
      consultationDialog.visible = false
      router.push({ name: 'ConsultationChat', params: { id: consultationData.consultationNo } })
    } else {
      ElMessage.error(response.message || '发起咨询失败')
    }
  } catch (error) {
    console.error('发起咨询失败:', error)
    ElMessage.error('网络异常，请稍后重试')
  } finally {
    submitting.value = false
  }
}

const initWebSocket = async () => {
  try {
    await webSocketManager.connect()
    webSocketManager.subscribe('/topic/doctor-status', (message) => { handleDoctorStatusChange(message) }, 'doctor-status')
  } catch (error) {
    console.error('WebSocket连接失败:', error)
  }
}

const handleDoctorStatusChange = (message) => {
  const { doctorId, onlineStatus, statusText } = message
  const doctorIndex = doctorList.value.findIndex(doctor => doctor.userInfo.id === doctorId)
  if (doctorIndex !== -1) {
    doctorList.value[doctorIndex].onlineStatus = onlineStatus
    const doctorName = doctorList.value[doctorIndex].userInfo.realName
    ElMessage.info(`${doctorName}医生${statusText}了`)
  }
}

onMounted(async () => { await getDoctorList(); await getDepartments(); await initWebSocket() })
onBeforeUnmount(() => { if (webSocketManager.isConnected()) { webSocketManager.unsubscribe('/topic/doctor-status'); webSocketManager.disconnect() } })
</script>

<style scoped>
.doctor-list-page {
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

.header-icon .icon { width: 32px; height: 32px; color: white; }
.page-title { font-size: 24px; font-weight: 700; color: #164E63; margin: 0 0 4px 0; }
.page-subtitle { font-size: 14px; color: #0891B2; margin: 0; }

.search-section {
  background: white;
  padding: 20px;
  border-radius: 16px;
  border: 1px solid #E0F2FE;
  margin-bottom: 24px;
}

.search-btn {
  background: linear-gradient(135deg, #0891B2 0%, #22D3EE 100%);
  border: none;
}

.search-btn:hover { background: linear-gradient(135deg, #0E7490 0%, #06B6D4 100%); }

.doctor-grid { margin-bottom: 24px; }

.doctor-card {
  background: white;
  border-radius: 16px;
  padding: 24px;
  border: 1px solid #E0F2FE;
  transition: all 0.3s ease;
  height: 100%;
  display: flex;
  flex-direction: column;
  margin-bottom: 20px;
  cursor: pointer;
}

.doctor-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 12px 24px rgba(8, 145, 178, 0.15);
  border-color: #22D3EE;
}

.doctor-avatar-section { text-align: center; margin-bottom: 16px; }
.avatar-container { position: relative; display: inline-block; }

.online-status {
  position: absolute;
  bottom: -5px;
  right: -5px;
  padding: 2px 6px;
  border-radius: 12px;
  font-size: 10px;
  font-weight: 500;
  display: flex;
  align-items: center;
  gap: 2px;
  border: 2px solid white;
}

.online-status.online { background: #059669; color: white; }
.online-status.busy { background: #F59E0B; color: white; }
.online-status.offline { background: #64748B; color: white; }
.status-dot { width: 6px; height: 6px; border-radius: 50%; background: currentColor; }

.doctor-info { flex: 1; text-align: center; }
.doctor-name { font-size: 20px; font-weight: 600; color: #164E63; margin: 0 0 8px 0; }
.doctor-title { color: #0891B2; font-weight: 500; font-size: 14px; margin-bottom: 12px; }

.doctor-rating { margin-bottom: 12px; display: flex; align-items: center; justify-content: center; gap: 8px; }
.rating-text { font-size: 12px; color: #67E8F9; }

.doctor-specialty {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 4px;
  color: #059669;
  font-size: 14px;
  margin-bottom: 12px;
  padding: 4px 8px;
  background: #ECFDF5;
  border-radius: 16px;
  max-width: fit-content;
  margin-left: auto;
  margin-right: auto;
}

.specialty-icon { width: 16px; height: 16px; }

.doctor-details { margin-bottom: 12px; }

.detail-item {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 4px;
  color: #67E8F9;
  font-size: 13px;
  margin-bottom: 4px;
}

.detail-icon { width: 14px; height: 14px; }

.consultation-fee {
  margin-bottom: 16px;
  padding: 8px 12px;
  background: #FEF3C7;
  border-radius: 8px;
  font-size: 14px;
}

.fee-label { color: #78716C; }
.fee-amount { color: #DC2626; font-weight: 600; }

.doctor-actions { display: flex; gap: 8px; justify-content: center; }
.doctor-actions .el-button { flex: 1; }

.detail-btn { border-color: #A5F3FC; color: #0891B2; }
.detail-btn:hover { background: #ECFEFF; border-color: #22D3EE; }

.consult-btn {
  background: linear-gradient(135deg, #0891B2 0%, #22D3EE 100%);
  border: none;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 4px;
}

.consult-btn:hover { background: linear-gradient(135deg, #0E7490 0%, #06B6D4 100%); }
.btn-icon { width: 16px; height: 16px; }

.empty-state { text-align: center; padding: 80px 20px; color: #67E8F9; }
.empty-icon { width: 64px; height: 64px; margin-bottom: 16px; opacity: 0.5; color: #A5F3FC; }
.empty-state h3 { font-size: 20px; margin: 0 0 8px 0; color: #164E63; }
.empty-state p { margin: 0; font-size: 14px; }

.loading-state { display: flex; flex-direction: column; gap: 20px; }

.pagination-section {
  display: flex;
  justify-content: center;
  padding: 20px;
  background: white;
  border-radius: 16px;
  border: 1px solid #E0F2FE;
}

.consultation-dialog :deep(.el-dialog__header) {
  background: linear-gradient(135deg, #ECFEFF 0%, #CFFAFE 100%);
  border-radius: 8px 8px 0 0;
}

.consultation-dialog :deep(.el-dialog__title) { color: #164E63; font-weight: 600; }

.consultation-form { padding: 20px 0; }

.doctor-info-card {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 20px;
  margin-bottom: 20px;
  background: #F0FDFA;
  border-radius: 12px;
  border: 1px solid #A5F3FC;
}

.doctor-info-card .doctor-details h3 { margin: 0 0 8px 0; font-size: 18px; font-weight: 600; color: #164E63; }
.doctor-info-card .doctor-details p { margin: 0 0 4px 0; color: #67E8F9; font-size: 14px; }
.doctor-info-card .doctor-speciality { color: #059669 !important; font-size: 13px; }

.type-tag { background: #0891B2; border-color: #0891B2; }
.type-desc { margin: 8px 0 0 0; font-size: 12px; color: #67E8F9; }

.fee-info { display: flex; align-items: center; gap: 12px; }
.fee-amount { font-size: 18px; font-weight: 600; color: #DC2626; }
.fee-desc { font-size: 12px; color: #67E8F9; }

.dialog-footer { display: flex; justify-content: flex-end; gap: 12px; }

.submit-btn { background: #0891B2; border-color: #0891B2; }
.submit-btn:hover { background: #0E7490; border-color: #0E7490; }

.process-tips { margin: 20px 0; }
.process-tips .el-alert { background-color: #F0FDFA; border-color: #A5F3FC; }
.process-tips p { margin: 4px 0; font-size: 13px; color: #164E63; }

@media (max-width: 768px) {
  .doctor-list-page { padding: 16px; }
  .page-header { padding: 16px; }
  .page-title { font-size: 20px; }
  .search-section { padding: 16px; }
  .search-section .el-col { margin-bottom: 12px; }
  .doctor-card { padding: 16px; }
  .doctor-actions { flex-direction: column; }
}
</style>