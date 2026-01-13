<template>
  <div class="doctor-list-page">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-content">
        <h1 class="page-title">
          <el-icon><UserFilled /></el-icon>
          选择医生
        </h1>
        <p class="page-subtitle">选择合适的医生进行在线咨询</p>
      </div>
    </div>

    <!-- 搜索和筛选 -->
    <div class="search-section">
      <el-row :gutter="16">
        <el-col :xs="24" :sm="8">
          <el-input
            v-model="searchForm.keyword"
            placeholder="搜索医生姓名、科室或专长"
            clearable
            @input="handleSearch"
            @clear="handleSearch"
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
        </el-col>
        <el-col :xs="24" :sm="6">
          <el-select
            v-model="searchForm.department"
            placeholder="选择科室"
            clearable
            @change="handleSearch"
          >
            <el-option
              v-for="dept in departments"
              :key="dept"
              :label="dept"
              :value="dept"
            />
          </el-select>
        </el-col>
        <el-col :xs="24" :sm="6">
          <el-button type="primary" @click="handleSearch" :loading="loading">
            <el-icon><Search /></el-icon>
            搜索
          </el-button>
        </el-col>
      </el-row>
    </div>

    <!-- 医生卡片列表 -->
    <div class="doctor-grid">
      <el-row :gutter="20">
        <el-col 
          :xs="24" :sm="12" :md="8" :lg="6" :xl="6"
          v-for="doctor in doctorList" 
          :key="doctor.userInfo.id"
        >
          <div class="doctor-card">
            <!-- 医生头像和在线状态 -->
            <div class="doctor-avatar-section">
              <div class="avatar-container">
                <el-avatar :size="80" :src="doctor.userInfo.avatar">
                  {{ doctor.userInfo.realName?.charAt(0) || 'D' }}
                </el-avatar>
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
                <el-rate 
                  :model-value="doctor.rating || 5.0" 
                  disabled 
                  show-score
                  text-color="#ff9900"
                  size="small"
                />
                <span class="rating-text">({{ doctor.consultationCount || 0 }}次咨询)</span>
              </div>

              <!-- 医生专长 -->
              <div class="doctor-specialty" v-if="doctor.speciality">
                <el-icon><Medal /></el-icon>
                <span>{{ doctor.speciality }}</span>
              </div>

              <!-- 工作年限和医院 -->
              <div class="doctor-details">
                <div class="detail-item" v-if="doctor.workYears">
                  <el-icon><Calendar /></el-icon>
                  <span>{{ doctor.workYears }}年经验</span>
                </div>
                <div class="detail-item" v-if="doctor.hospital">
                  <el-icon><OfficeBuilding /></el-icon>
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
              <el-button type="default" size="small" @click="viewDoctorDetail(doctor)">
                查看详情
              </el-button>
              <el-button 
                type="primary" 
                size="small" 
                @click="startConsultation(doctor)"
                :disabled="doctor.onlineStatus === 0"
              >
                <el-icon><ChatDotRound /></el-icon>
                {{ doctor.onlineStatus === 0 ? '医生离线' : '立即咨询' }}
              </el-button>
            </div>
          </div>
        </el-col>
      </el-row>

      <!-- 空状态 -->
      <div v-if="!loading && doctorList.length === 0" class="empty-state">
        <el-icon class="empty-icon"><UserFilled /></el-icon>
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
      <el-pagination
        v-model:current-page="searchForm.page"
        :page-size="searchForm.size"
        :total="total"
        layout="prev, pager, next, sizes, total"
        :page-sizes="[10, 20, 30, 50]"
        @current-change="handlePageChange"
        @size-change="handleSizeChange"
      />
    </div>

    <!-- 发起咨询对话框 -->
    <el-dialog 
      v-model="consultationDialog.visible" 
      title="发起咨询" 
      :width="500"
      @closed="resetConsultationForm">
      
      <div class="consultation-form">
        <!-- 医生信息 -->
        <div class="doctor-info-card">
          <el-avatar :size="60" :src="consultationDialog.doctor?.userInfo?.avatar">
            {{ consultationDialog.doctor?.userInfo?.realName?.charAt(0) }}
          </el-avatar>
          <div class="doctor-details">
            <h3>{{ consultationDialog.doctor?.userInfo?.realName }}</h3>
            <p>{{ consultationDialog.doctor?.department }} · {{ consultationDialog.doctor?.title }}</p>
            <p class="doctor-speciality">擅长：{{ consultationDialog.doctor?.speciality }}</p>
          </div>
        </div>

        <!-- 咨询表单 -->
        <el-form :model="consultationForm" :rules="consultationRules" ref="consultationFormRef" label-width="100px">
          <el-form-item label="咨询类型">
            <el-tag type="primary" size="large">对话问诊</el-tag>
            <p style="margin: 8px 0 0 0; font-size: 12px; color: #6b7280;">
              向医生简要介绍症状，医生回复后开启对话
            </p>
          </el-form-item>

          <el-form-item label="症状简述" prop="chiefComplaint" required>
            <el-input 
              v-model="consultationForm.chiefComplaint"
              type="textarea"
              :rows="4"
              placeholder="请简要描述您的主要症状或想要咨询的问题，医生会根据您的描述进行初步回复..."
              maxlength="300"
              show-word-limit
            />
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
        <el-alert 
          title="问诊流程说明" 
          type="info" 
          :closable="false"
          show-icon>
          <p>1. 您提交症状描述后，医生会收到咨询通知</p>
          <p>2. 医生查看您的症状并进行初步回复</p>
          <p>3. 医生回复后，对话正式开启</p>
          <p>4. 您可以继续与医生进行详细交流</p>
        </el-alert>
      </div>

      <template #footer>
        <div class="dialog-footer">
          <el-button @click="consultationDialog.visible = false">取消</el-button>
          <el-button type="primary" @click="submitConsultation" :loading="submitting">
            提交症状给医生
          </el-button>
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
import {
  UserFilled, Search, ChatDotRound, Medal, Calendar, OfficeBuilding
} from '@element-plus/icons-vue'
import webSocketManager from '@/utils/websocket'

const router = useRouter()

// 数据状态
const loading = ref(false)
const doctorList = ref([])
const departments = ref([])
const total = ref(0)

// 搜索表单
const searchForm = reactive({
  keyword: '',
  department: '',
  page: 1,
  size: 20
})

// 咨询对话框数据
const consultationDialog = reactive({
  visible: false,
  doctor: null
})

// 咨询表单数据
const consultationForm = reactive({
  consultationType: 1, // 固定为对话问诊
  chiefComplaint: ''
})

// 咨询表单验证规则
const consultationRules = {
  chiefComplaint: [
    { required: true, message: '请简要描述症状', trigger: 'blur' },
    { min: 5, message: '症状描述至少5个字符', trigger: 'blur' },
    { max: 300, message: '症状描述不能超过300个字符', trigger: 'blur' }
  ]
}

// 咨询表单引用
const consultationFormRef = ref(null)
const submitting = ref(false)

// 获取医生列表
const getDoctorList = async () => {
  loading.value = true
  try {
    const response = await consultationAPI.getDoctorList({
      keyword: searchForm.keyword,
      department: searchForm.department,
      page: searchForm.page,
      size: searchForm.size
    })
    
    if (response.code === 200) {
      doctorList.value = response.data || []
      // 这里暂时不处理分页总数，后续可以从响应头或另一个接口获取
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

// 获取科室列表
const getDepartments = async () => {
  try {
    // 从医生列表中提取科室信息
    const allDepts = new Set()
    doctorList.value.forEach(doctor => {
      if (doctor.department) {
        allDepts.add(doctor.department)
      }
    })
    departments.value = Array.from(allDepts)
  } catch (error) {
    console.error('获取科室列表失败:', error)
  }
}

// 搜索处理
const handleSearch = () => {
  searchForm.page = 1
  getDoctorList()
}

// 分页处理
const handlePageChange = (page) => {
  searchForm.page = page
  getDoctorList()
}

const handleSizeChange = (size) => {
  searchForm.size = size
  searchForm.page = 1
  getDoctorList()
}

// 获取在线状态样式
const getOnlineStatusClass = (status) => {
  const statusMap = {
    0: 'offline',
    1: 'online', 
    2: 'busy'
  }
  return statusMap[status] || 'offline'
}

// 获取在线状态文本
const getOnlineStatusText = (status) => {
  const statusMap = {
    0: '离线',
    1: '在线',
    2: '忙碌'
  }
  return statusMap[status] || '离线'
}

// 查看医生详情
const viewDoctorDetail = (doctor) => {
  ElMessageBox.alert(
    `
    <div style="text-align: left;">
      <p><strong>医生姓名：</strong>${doctor.userInfo.realName}</p>
      <p><strong>科室职称：</strong>${doctor.title} · ${doctor.department}</p>
      <p><strong>从业年限：</strong>${doctor.workYears || '未知'}年</p>
      <p><strong>所属医院：</strong>${doctor.hospital || '未知'}</p>
      <p><strong>专业特长：</strong>${doctor.speciality || '暂无'}</p>
      <p><strong>个人简介：</strong>${doctor.introduction || '暂无'}</p>
      <p><strong>咨询费用：</strong>¥${doctor.consultationFee || '0'}/次</p>
      <p><strong>服务评分：</strong>${doctor.rating || 5.0}分 (${doctor.consultationCount || 0}次咨询)</p>
    </div>
    `,
    `${doctor.userInfo.realName}医生详情`,
    {
      dangerouslyUseHTMLString: true,
      confirmButtonText: '立即咨询',
      cancelButtonText: '关闭',
      showCancelButton: true,
      type: 'info'
    }
  ).then(() => {
    startConsultation(doctor)
  }).catch(() => {
    // 用户点击取消，不做任何处理
  })
}

// 开始咨询
const startConsultation = async (doctor) => {
  if (doctor.onlineStatus === 0) {
    ElMessage.warning('该医生当前离线，暂无法咨询')
    return
  }
  
  // 显示发起咨询对话框
  showConsultationDialog(doctor)
}

// 显示咨询对话框
const showConsultationDialog = (doctor) => {
  consultationDialog.doctor = doctor
  consultationDialog.visible = true
}

// 重置咨询表单
const resetConsultationForm = () => {
  consultationForm.consultationType = 1
  consultationForm.chiefComplaint = ''
  consultationDialog.doctor = null
  
  // 清除表单验证
  if (consultationFormRef.value) {
    consultationFormRef.value.resetFields()
  }
}

// 提交咨询申请
const submitConsultation = async () => {
  // 简单验证
  if (!consultationForm.chiefComplaint || consultationForm.chiefComplaint.trim().length < 5) {
    ElMessage.error('请输入至少5个字符的症状描述')
    return
  }

  if (consultationForm.chiefComplaint.trim().length > 300) {
    ElMessage.error('症状描述不能超过300个字符')
    return
  }

  submitting.value = true

  try {
    // 准备请求数据
    const requestData = {
      doctorId: consultationDialog.doctor.userInfo.id,
      consultationType: consultationForm.consultationType,
      chiefComplaint: consultationForm.chiefComplaint.trim(),
      fee: consultationDialog.doctor.consultationFee || 0
    }

    console.log('发送请求数据:', requestData)

    // 发起咨询请求
    const response = await createConsultation(requestData)
    
    console.log('API响应:', response)

    if (response.code === 200) {
      const consultationData = response.data
      ElMessage.success('咨询发起成功！')
      
      // 关闭对话框
      consultationDialog.visible = false
      
      // 跳转到聊天页面
      router.push({
        name: 'ConsultationChat',
        params: { id: consultationData.consultationNo }
      })
      
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

// WebSocket连接和订阅
const initWebSocket = async () => {
  try {
    // 连接WebSocket
    await webSocketManager.connect()
    
    // 订阅医生状态变化主题
    webSocketManager.subscribe('/topic/doctor-status', (message) => {
      console.log('收到医生状态变化消息:', message)
      handleDoctorStatusChange(message)
    }, 'doctor-status')
    
    console.log('WebSocket连接和订阅成功')
  } catch (error) {
    console.error('WebSocket连接失败:', error)
    // WebSocket连接失败不影响页面正常使用
  }
}

// 处理医生状态变化
const handleDoctorStatusChange = (message) => {
  const { doctorId, onlineStatus, statusText } = message
  
  // 更新医生列表中对应医生的状态
  const doctorIndex = doctorList.value.findIndex(doctor => 
    doctor.userInfo.id === doctorId
  )
  
  if (doctorIndex !== -1) {
    // 更新状态
    doctorList.value[doctorIndex].onlineStatus = onlineStatus
    
    // 显示状态变化提示（可选）
    const doctorName = doctorList.value[doctorIndex].userInfo.realName
    ElMessage.info(`${doctorName}医生${statusText}了`)
    
    console.log(`更新医生状态: ${doctorName} -> ${statusText}`)
  }
}

// 页面初始化
onMounted(async () => {
  await getDoctorList()
  await getDepartments()
  
  // 初始化WebSocket连接
  await initWebSocket()
})

// 页面销毁前清理WebSocket连接
onBeforeUnmount(() => {
  if (webSocketManager.isConnected()) {
    webSocketManager.unsubscribe('/topic/doctor-status')
    webSocketManager.disconnect()
    console.log('WebSocket连接已清理')
  }
})
</script>

<style scoped>
.doctor-list-page {
  padding: 24px;
  background: #f5f7fa;
  min-height: 100vh;
}

/* 页面头部 */
.page-header {
  background: white;
  padding: 24px;
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  margin-bottom: 24px;
}

.header-content {
  text-align: center;
}

.page-title {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  font-size: 28px;
  font-weight: 600;
  color: #1e293b;
  margin: 0 0 8px 0;
}

.page-subtitle {
  color: #64748b;
  font-size: 16px;
  margin: 0;
}

/* 搜索区域 */
.search-section {
  background: white;
  padding: 20px;
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  margin-bottom: 24px;
}

/* 医生卡片网格 */
.doctor-grid {
  margin-bottom: 24px;
}

.doctor-card {
  background: white;
  border-radius: 16px;
  padding: 24px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  transition: all 0.3s ease;
  height: 100%;
  display: flex;
  flex-direction: column;
  margin-bottom: 20px;
}

.doctor-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.12);
}

/* 医生头像区域 */
.doctor-avatar-section {
  text-align: center;
  margin-bottom: 16px;
}

.avatar-container {
  position: relative;
  display: inline-block;
}

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

.online-status.online {
  background: #10b981;
  color: white;
}

.online-status.busy {
  background: #f59e0b;
  color: white;
}

.online-status.offline {
  background: #64748b;
  color: white;
}

.status-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: currentColor;
}

/* 医生信息区域 */
.doctor-info {
  flex: 1;
  text-align: center;
}

.doctor-name {
  font-size: 20px;
  font-weight: 600;
  color: #1e293b;
  margin: 0 0 8px 0;
}

.doctor-title {
  color: #4f46e5;
  font-weight: 500;
  font-size: 14px;
  margin-bottom: 12px;
}

.doctor-rating {
  margin-bottom: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}

.rating-text {
  font-size: 12px;
  color: #64748b;
}

.doctor-specialty {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 4px;
  color: #059669;
  font-size: 14px;
  margin-bottom: 12px;
  padding: 4px 8px;
  background: #ecfdf5;
  border-radius: 16px;
  max-width: fit-content;
  margin-left: auto;
  margin-right: auto;
}

.doctor-details {
  margin-bottom: 12px;
}

.detail-item {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 4px;
  color: #64748b;
  font-size: 13px;
  margin-bottom: 4px;
}

.consultation-fee {
  margin-bottom: 16px;
  padding: 8px 12px;
  background: #fef3c7;
  border-radius: 8px;
  font-size: 14px;
}

.fee-label {
  color: #78716c;
}

.fee-amount {
  color: #dc2626;
  font-weight: 600;
}

/* 操作按钮 */
.doctor-actions {
  display: flex;
  gap: 8px;
  justify-content: center;
}

.doctor-actions .el-button {
  flex: 1;
}

/* 空状态 */
.empty-state {
  text-align: center;
  padding: 80px 20px;
  color: #64748b;
}

.empty-icon {
  font-size: 64px;
  margin-bottom: 16px;
  opacity: 0.5;
}

.empty-state h3 {
  font-size: 20px;
  margin: 0 0 8px 0;
}

.empty-state p {
  margin: 0;
  font-size: 14px;
}

/* 加载状态 */
.loading-state {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

/* 分页 */
.pagination-section {
  display: flex;
  justify-content: center;
  padding: 20px;
  background: white;
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
}

/* 响应式设计 */
@media (max-width: 768px) {
  .doctor-list-page {
    padding: 16px;
  }
  
  .page-title {
    font-size: 24px;
  }
  
  .search-section {
    padding: 16px;
  }
  
  .search-section .el-col {
    margin-bottom: 12px;
  }
  
  .doctor-card {
    padding: 16px;
  }
  
  .doctor-actions {
    flex-direction: column;
  }
}

/* 咨询对话框样式 */
.consultation-form {
  padding: 20px 0;
}

.doctor-info-card {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 20px;
  margin-bottom: 20px;
  background: #f8f9fa;
  border-radius: 12px;
  border: 1px solid #e9ecef;
}

.doctor-info-card .doctor-details h3 {
  margin: 0 0 8px 0;
  font-size: 18px;
  font-weight: 600;
  color: #1e293b;
}

.doctor-info-card .doctor-details p {
  margin: 0 0 4px 0;
  color: #64748b;
  font-size: 14px;
}

.doctor-info-card .doctor-speciality {
  color: #10b981 !important;
  font-size: 13px;
}

.fee-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.fee-amount {
  font-size: 18px;
  font-weight: 600;
  color: #dc2626;
}

.fee-desc {
  font-size: 12px;
  color: #6b7280;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

.process-tips {
  margin: 20px 0;
}

.process-tips .el-alert {
  background-color: #f0f9ff;
  border-color: #bfdbfe;
}

.process-tips p {
  margin: 4px 0;
  font-size: 13px;
  color: #374151;
}
</style> 