<template>
  <div class="doctor-profile-container">
    <!-- 医生信息头部 -->
    <div class="profile-header">
      <div class="user-info-card">
        <div class="avatar-wrapper">
          <AvatarUpload 
            ref="avatarUploadRef"
            size="large"
            :show-buttons="false"
            @avatar-updated="handleAvatarUpdated"
            @avatar-removed="handleAvatarRemoved"
          />
          <div class="avatar-actions">
            <el-button type="primary" size="small" circle @click="triggerAvatarUpload">
              <el-icon><Camera /></el-icon>
            </el-button>
            <el-button type="danger" size="small" circle @click="triggerAvatarRemove" v-if="doctorInfo?.userInfo?.avatar">
              <el-icon><Delete /></el-icon>
            </el-button>
          </div>
        </div>
        <div class="doctor-basic-info">
          <h2 class="doctor-name">{{ doctorInfo?.userInfo?.realName || doctorInfo?.userInfo?.username || '未设置' }}</h2>
          <p class="doctor-title">{{ doctorInfo?.title || '医师' }} | {{ doctorInfo?.department || '内分泌科' }}</p>
          <p class="doctor-hospital">{{ doctorInfo?.hospital || '默认医院' }}</p>
          <div class="doctor-tags">
            <el-tag type="warning" effect="dark">医生</el-tag>
            <el-tag v-if="doctorInfo?.userInfo?.phone" type="info">已绑定手机</el-tag>
            <el-tag v-if="doctorInfo?.doctorNo" type="success">工号: {{ doctorInfo.doctorNo }}</el-tag>
            <el-tag 
              :type="onlineStatusTag.type" 
              :effect="onlineStatusTag.effect"
            >
              {{ onlineStatusTag.text }}
            </el-tag>
          </div>
        </div>
      </div>
    </div>

    <!-- 表单内容区 -->
    <div class="profile-content">
      <el-row :gutter="24">
        <!-- 基本信息 -->
        <el-col :xs="24" :lg="14">
          <div class="form-card">
            <div class="card-header">
              <el-icon class="header-icon"><User /></el-icon>
              <h3>基本信息</h3>
            </div>
            <el-form
              ref="profileFormRef"
              :model="profileForm"
              :rules="profileRules"
              label-width="100px"
              class="modern-form"
            >
              <div class="form-grid">
                <el-form-item label="用户名" prop="username">
                  <el-input 
                    v-model="profileForm.username" 
                    disabled 
                    :prefix-icon="UserFilled"
                  />
                </el-form-item>

                <el-form-item label="真实姓名" prop="realName">
                  <el-input
                    v-model="profileForm.realName"
                    placeholder="请输入真实姓名"
                    clearable
                    :prefix-icon="User"
                  />
                </el-form-item>

                <el-form-item label="邮箱" prop="email">
                  <el-input
                    v-model="profileForm.email"
                    placeholder="请输入邮箱"
                    clearable
                    :prefix-icon="Message"
                  />
                </el-form-item>

                <el-form-item label="手机号码" prop="phone">
                  <el-input
                    v-model="profileForm.phone"
                    placeholder="请输入手机号码"
                    clearable
                    :prefix-icon="Phone"
                  />
                </el-form-item>

                <el-form-item label="性别" prop="gender">
                  <el-radio-group v-model="profileForm.gender" class="gender-group">
                    <el-radio label="男" class="gender-radio">
                      <el-icon><Male /></el-icon>
                      <span>男</span>
                    </el-radio>
                    <el-radio label="女" class="gender-radio">
                      <el-icon><Female /></el-icon>
                      <span>女</span>
                    </el-radio>
                  </el-radio-group>
                </el-form-item>

                <el-form-item label="生日" prop="birthday">
                  <el-date-picker
                    v-model="profileForm.birthday"
                    type="date"
                    placeholder="请选择生日"
                    style="width: 100%"
                    :prefix-icon="Calendar"
                  />
                </el-form-item>
              </div>

              <el-form-item label="身份证号" prop="idCard">
                <el-input
                  v-model="profileForm.idCard"
                  placeholder="请输入身份证号"
                  clearable
                  :prefix-icon="CreditCard"
                />
              </el-form-item>

              <div class="form-actions">
                <el-button type="primary" @click="saveProfile" :loading="saving" size="large">
                  <el-icon><Check /></el-icon>
                  保存基本信息
                </el-button>
                <el-button @click="resetForm" size="large">
                  <el-icon><RefreshLeft /></el-icon>
                  重置
                </el-button>
              </div>
            </el-form>
          </div>
        </el-col>

        <!-- 专业信息 -->
        <el-col :xs="24" :lg="10">
          <div class="form-card">
            <div class="card-header">
              <el-icon class="header-icon"><Files /></el-icon>
              <h3>专业信息</h3>
            </div>
            <el-form
              ref="doctorFormRef"
              :model="doctorForm"
              :rules="doctorRules"
              label-width="100px"
              class="modern-form"
            >
              <el-form-item label="医生编号" prop="doctorNo">
                <el-input
                  v-model="doctorForm.doctorNo"
                  disabled
                  :prefix-icon="UserFilled"
                />
              </el-form-item>

              <el-form-item label="所属科室" prop="department">
                <el-select
                  v-model="doctorForm.department"
                  placeholder="请选择科室"
                  style="width: 100%"
                  clearable
                >
                  <el-option label="内分泌科" value="内分泌科" />
                  <el-option label="心血管科" value="心血管科" />
                  <el-option label="神经内科" value="神经内科" />
                  <el-option label="消化内科" value="消化内科" />
                  <el-option label="呼吸内科" value="呼吸内科" />
                  <el-option label="肾内科" value="肾内科" />
                  <el-option label="普通内科" value="普通内科" />
                </el-select>
              </el-form-item>

              <el-form-item label="职称" prop="title">
                <el-select
                  v-model="doctorForm.title"
                  placeholder="请选择职称"
                  style="width: 100%"
                  clearable
                >
                  <el-option label="住院医师" value="住院医师" />
                  <el-option label="主治医师" value="主治医师" />
                  <el-option label="副主任医师" value="副主任医师" />
                  <el-option label="主任医师" value="主任医师" />
                </el-select>
              </el-form-item>

              <el-form-item label="执业证书" prop="qualificationCert">
                <el-input
                  v-model="doctorForm.qualificationCert"
                  placeholder="请输入执业资格证书号"
                  clearable
                />
              </el-form-item>

              <el-form-item label="从业年限" prop="workYears">
                <el-input-number
                  v-model="doctorForm.workYears"
                  :min="0"
                  :max="50"
                  style="width: 100%"
                  placeholder="请输入从业年限"
                />
              </el-form-item>

              <el-form-item label="所属医院" prop="hospital">
                <el-input
                  v-model="doctorForm.hospital"
                  placeholder="请输入所属医院"
                  clearable
                />
              </el-form-item>

              <el-form-item label="咨询费用" prop="consultationFee">
                <el-input-number
                  v-model="doctorForm.consultationFee"
                  :min="0"
                  :precision="2"
                  style="width: 100%"
                  placeholder="请输入咨询费用"
                />
                <template #suffix>元</template>
              </el-form-item>

              <el-form-item label="专业特长">
                <el-input
                  v-model="doctorForm.speciality"
                  type="textarea"
                  :rows="3"
                  placeholder="请描述您的专业特长..."
                  maxlength="200"
                  show-word-limit
                />
              </el-form-item>

              <el-form-item label="个人简介">
                <el-input
                  v-model="doctorForm.introduction"
                  type="textarea"
                  :rows="4"
                  placeholder="请介绍一下自己..."
                  maxlength="500"
                  show-word-limit
                />
              </el-form-item>

              <div class="form-actions">
                <el-button type="primary" @click="saveDoctorInfo" :loading="savingDoctor" size="large">
                  <el-icon><Check /></el-icon>
                  保存专业信息
                </el-button>
                <el-button @click="resetDoctorForm" size="large">
                  <el-icon><RefreshLeft /></el-icon>
                  重置
                </el-button>
              </div>
            </el-form>
          </div>

          <!-- 在线状态控制 -->
          <div class="form-card">
            <div class="card-header">
              <el-icon class="header-icon"><Connection /></el-icon>
              <h3>在线状态</h3>
            </div>
            <div class="online-status-control">
              <el-radio-group v-model="onlineStatus" @change="updateOnlineStatus" size="large">
                <el-radio-button :label="0">
                  <el-icon><VideoPause /></el-icon>
                  离线
                </el-radio-button>
                <el-radio-button :label="1">
                  <el-icon><VideoPlay /></el-icon>
                  在线
                </el-radio-button>
                <el-radio-button :label="2">
                  <el-icon><Loading /></el-icon>
                  忙碌
                </el-radio-button>
              </el-radio-group>
              <p class="status-tip">切换您的在线状态，患者可以看到您的当前状态</p>
            </div>
          </div>
        </el-col>
      </el-row>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useUserStore } from '@/stores/user'
import { doctorAPI } from '@/api/doctor'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  User, UserFilled, Message, Phone, Male, Female, Calendar, 
  CreditCard, Check, RefreshLeft, Files, Connection,
  VideoPause, VideoPlay, Loading, Camera, Delete
} from '@element-plus/icons-vue'
import AvatarUpload from '@/components/common/AvatarUpload.vue'

const userStore = useUserStore()

const profileFormRef = ref()
const doctorFormRef = ref()
const avatarUploadRef = ref()
const saving = ref(false)
const savingDoctor = ref(false)
const doctorInfo = ref(null)
const onlineStatus = ref(0)

// 基本信息表单
const profileForm = reactive({
  username: '',
  realName: '',
  email: '',
  phone: '',
  gender: null,
  birthday: null,
  idCard: ''
})

// 医生专业信息表单
const doctorForm = reactive({
  doctorNo: '',
  department: '',
  title: '',
  qualificationCert: '',
  speciality: '',
  workYears: null,
  hospital: '',
  introduction: '',
  consultationFee: null
})

// 在线状态标签计算属性
const onlineStatusTag = computed(() => {
  switch (onlineStatus.value) {
    case 1:
      return { type: 'success', effect: 'dark', text: '在线' }
    case 2:
      return { type: 'warning', effect: 'dark', text: '忙碌' }
    default:
      return { type: 'info', effect: 'plain', text: '离线' }
  }
})

// 验证规则
const profileRules = {
  realName: [
    { required: true, message: '请输入真实姓名', trigger: 'blur' },
    { min: 2, max: 10, message: '姓名长度在 2 到 10 个字符', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱地址', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ],
  phone: [
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号码', trigger: 'blur' }
  ]
}

const doctorRules = {
  department: [
    { required: true, message: '请选择所属科室', trigger: 'change' }
  ],
  title: [
    { required: true, message: '请选择职称', trigger: 'change' }
  ]
}

// 初始化表单数据
const initProfileForm = () => {
  if (doctorInfo.value?.userInfo) {
    const userInfo = doctorInfo.value.userInfo
    
    Object.assign(profileForm, {
      username: userInfo.username,
      realName: userInfo.realName || '',
      email: userInfo.email || '',
      phone: userInfo.phone || '',
      gender: userInfo.gender,
      birthday: userInfo.birthday || null,
      idCard: userInfo.idCard || ''
    })
  }
}

const initDoctorForm = () => {
  if (doctorInfo.value) {
    Object.assign(doctorForm, {
      doctorNo: doctorInfo.value.doctorNo || '',
      department: doctorInfo.value.department || '',
      title: doctorInfo.value.title || '',
      qualificationCert: doctorInfo.value.qualificationCert || '',
      speciality: doctorInfo.value.speciality || '',
      workYears: doctorInfo.value.workYears,
      hospital: doctorInfo.value.hospital || '',
      introduction: doctorInfo.value.introduction || '',
      consultationFee: doctorInfo.value.consultationFee
    })
    onlineStatus.value = doctorInfo.value.onlineStatus || 0
  }
}

// 获取医生信息
const loadDoctorInfo = async () => {
  try {
    const response = await doctorAPI.getCurrentProfile()
    if (response.success) {
      doctorInfo.value = response.data
      initProfileForm()
      initDoctorForm()
    }
  } catch (error) {
    console.error('获取医生信息失败:', error)
    ElMessage.error('获取医生信息失败')
  }
}

// 保存基本信息
const saveProfile = async () => {
  if (saving.value) return

  try {
    const valid = await profileFormRef.value.validate()
    if (!valid) return

    saving.value = true

    const updateData = {
      user: {
        realName: profileForm.realName,
        email: profileForm.email,
        phone: profileForm.phone,
        gender: profileForm.gender,
        birthday: profileForm.birthday,
        idCard: profileForm.idCard
      }
    }

    const response = await doctorAPI.updateProfile(updateData)
    
    if (response.success) {
      ElMessage.success('基本信息已更新')
      // 更新本地用户信息
      await userStore.getCurrentUser()
      await loadDoctorInfo()
    }
  } catch (error) {
    console.error('保存基本信息失败:', error)
  } finally {
    saving.value = false
  }
}

// 保存医生专业信息
const saveDoctorInfo = async () => {
  if (savingDoctor.value) return

  try {
    const valid = await doctorFormRef.value.validate()
    if (!valid) return

    savingDoctor.value = true

    const response = await doctorAPI.updateProfile({
      doctorInfo: {
        doctorNo: doctorForm.doctorNo,
        department: doctorForm.department,
        title: doctorForm.title,
        qualificationCert: doctorForm.qualificationCert,
        speciality: doctorForm.speciality,
        workYears: doctorForm.workYears,
        hospital: doctorForm.hospital,
        introduction: doctorForm.introduction,
        consultationFee: doctorForm.consultationFee
      }
    })
    
    if (response.success) {
      ElMessage.success('专业信息已更新')
      await loadDoctorInfo()
    }
  } catch (error) {
    console.error('保存专业信息失败:', error)
  } finally {
    savingDoctor.value = false
  }
}

// 更新在线状态
const updateOnlineStatus = async (status) => {
  try {
    const response = await doctorAPI.updateOnlineStatus(status)
    if (response.success) {
      ElMessage.success('在线状态已更新')
    }
  } catch (error) {
    console.error('更新在线状态失败:', error)
    // 回滚状态
    onlineStatus.value = doctorInfo.value?.onlineStatus || 0
  }
}

// 重置表单
const resetForm = () => {
  profileFormRef.value.clearValidate()
  initProfileForm()
}

const resetDoctorForm = () => {
  doctorFormRef.value.clearValidate()
  initDoctorForm()
}

// 头像相关方法
const triggerAvatarUpload = () => {
  const avatarComponent = document.querySelector('.avatar-upload input[type="file"]')
  if (avatarComponent) {
    avatarComponent.click()
  }
}

const triggerAvatarRemove = async () => {
  try {
    await ElMessageBox.confirm(
      '确定要删除当前头像吗？',
      '删除头像',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    userStore.updateAvatar(null)
    ElMessage.success('头像删除成功')
  } catch (error) {
    // 用户取消删除
  }
}

const handleAvatarUpdated = (fileInfo) => {
  console.log('头像更新成功:', fileInfo)
  ElMessage.success('头像更新成功')
}

const handleAvatarRemoved = () => {
  console.log('头像已删除')
}

onMounted(() => {
  loadDoctorInfo()
})
</script>

<style scoped>
.doctor-profile-container {
  background: #f5f7fa;
  min-height: 100vh;
  padding: 24px;
}

/* 医生信息头部 */
.profile-header {
  margin-bottom: 24px;
}

.user-info-card {
  display: flex;
  align-items: center;
  gap: 24px;
  max-width: 1200px;
  margin: 0 auto;
  background: #fff;
  border-radius: 8px;
  padding: 24px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  border: 1px solid #e4e7ed;
}

.avatar-wrapper {
  position: relative;
  flex-shrink: 0;
}

.avatar-actions {
  position: absolute;
  bottom: -5px;
  right: -5px;
  display: flex;
  gap: 8px;
}

.avatar-actions .el-button {
  width: 32px;
  height: 32px;
  border: 2px solid #fff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
}

.doctor-basic-info {
  flex: 1;
  min-width: 0;
}

.doctor-name {
  font-size: 28px;
  font-weight: 700;
  color: #2c3e50;
  margin: 0 0 8px 0;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.doctor-title {
  font-size: 16px;
  color: #64748b;
  margin: 0 0 6px 0;
  font-weight: 500;
}

.doctor-hospital {
  font-size: 14px;
  color: #94a3b8;
  margin: 0 0 16px 0;
}

.doctor-tags {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.doctor-tags .el-tag {
  border-radius: 20px;
  padding: 6px 14px;
  font-weight: 500;
  border: none;
  font-size: 12px;
}

/* 表单内容区 */
.profile-content {
  max-width: 1200px;
  margin: 0 auto;
}

.form-card {
  background: #fff;
  border-radius: 16px;
  padding: 30px;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.08);
  border: 1px solid #e2e8f0;
  margin-bottom: 20px;
  transition: all 0.3s ease;
}

.form-card:hover {
  box-shadow: 0 15px 40px rgba(0, 0, 0, 0.12);
  transform: translateY(-2px);
}

.card-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 30px;
  padding-bottom: 20px;
  border-bottom: 2px solid #f1f5f9;
}

.header-icon {
  font-size: 24px;
  color: #667eea;
}

.card-header h3 {
  font-size: 20px;
  font-weight: 600;
  color: #1e293b;
  margin: 0;
}

/* 表单样式 */
.modern-form {
  --el-form-label-font-size: 14px;
}

.form-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: 20px;
  margin-bottom: 20px;
}

.modern-form :deep(.el-form-item) {
  margin-bottom: 20px;
}

.modern-form :deep(.el-form-item__label) {
  font-weight: 600;
  color: #374151;
}

.modern-form :deep(.el-input__wrapper) {
  border-radius: 8px;
  border: 2px solid #e2e8f0;
  transition: all 0.3s ease;
}

.modern-form :deep(.el-input__wrapper:hover) {
  border-color: #cbd5e1;
}

.modern-form :deep(.el-input.is-focus .el-input__wrapper) {
  border-color: #667eea;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
}

.gender-group {
  display: flex;
  gap: 20px;
}

.gender-radio :deep(.el-radio__label) {
  display: flex;
  align-items: center;
  gap: 6px;
  font-weight: 500;
  padding-left: 8px;
}

.form-actions {
  display: flex;
  gap: 16px;
  margin-top: 30px;
  padding-top: 20px;
  border-top: 1px solid #f1f5f9;
}

.form-actions .el-button {
  border-radius: 8px;
  padding: 12px 24px;
  font-weight: 600;
}

/* 在线状态控制 */
.online-status-control {
  text-align: center;
}

.online-status-control .el-radio-group {
  width: 100%;
  margin-bottom: 16px;
}

.online-status-control :deep(.el-radio-button) {
  flex: 1;
}

.online-status-control :deep(.el-radio-button__inner) {
  width: 100%;
  border-radius: 8px;
  margin: 0 4px;
  font-weight: 500;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
}

.status-tip {
  color: #64748b;
  font-size: 14px;
  margin: 0;
  line-height: 1.5;
}

/* 响应式设计 */
@media (max-width: 1024px) {
  .user-info-card {
    flex-direction: column;
    text-align: center;
  }
  
  .form-grid {
    grid-template-columns: 1fr;
  }
  
  .form-actions {
    flex-direction: column;
  }
}

@media (max-width: 768px) {
  .doctor-profile-container {
    padding: 16px;
  }
  
  .form-card {
    padding: 20px;
  }
  
  .doctor-name {
    font-size: 24px;
  }
  
  .gender-group {
    flex-direction: column;
    gap: 10px;
  }
}
</style> 