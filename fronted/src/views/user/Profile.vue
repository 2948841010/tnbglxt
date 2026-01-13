<template>
  <div class="profile-container">
    <!-- 个人信息头部 -->
    <div class="profile-header">
      <div class="header-background"></div>
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
            <el-button type="danger" size="small" circle @click="triggerAvatarRemove" v-if="userStore.userInfo?.avatar">
              <el-icon><Delete /></el-icon>
            </el-button>
          </div>
        </div>
        <div class="user-basic-info">
          <h2 class="user-name">{{ userStore.userInfo?.realName || userStore.userInfo?.username || '未设置' }}</h2>
          <p class="user-email">{{ userStore.userInfo?.email || '未设置邮箱' }}</p>
          <div class="user-tags">
            <el-tag v-if="userStore.userInfo?.userType === 0" type="success">普通用户</el-tag>
            <el-tag v-else-if="userStore.userInfo?.userType === 1" type="warning">医生</el-tag>
            <el-tag v-else-if="userStore.userInfo?.userType === 2" type="danger">管理员</el-tag>
            <el-tag v-if="userStore.userInfo?.phone" type="info">已绑定手机</el-tag>
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
              label-width="80px"
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

              <el-form-item label="个人简介">
                <el-input
                  v-model="profileForm.remark"
                  type="textarea"
                  :rows="4"
                  placeholder="介绍一下自己吧..."
                  maxlength="200"
                  show-word-limit
                  class="textarea-modern"
                />
              </el-form-item>

              <div class="form-actions">
                <el-button type="primary" @click="saveProfile" :loading="saving" size="large">
                  <el-icon><Check /></el-icon>
                  保存修改
                </el-button>
                <el-button @click="resetForm" size="large">
                  <el-icon><RefreshLeft /></el-icon>
                  重置
                </el-button>
              </div>
            </el-form>
          </div>
        </el-col>

        <!-- 安全设置 -->
        <el-col :xs="24" :lg="10">
          <div class="form-card">
            <div class="card-header">
              <el-icon class="header-icon"><Lock /></el-icon>
              <h3>安全设置</h3>
            </div>
            <el-form
              ref="passwordFormRef"
              :model="passwordForm"
              :rules="passwordRules"
              label-width="80px"
              class="modern-form"
            >
              <el-form-item label="当前密码" prop="oldPassword">
                <el-input
                  v-model="passwordForm.oldPassword"
                  type="password"
                  placeholder="请输入当前密码"
                  show-password
                  clearable
                  :prefix-icon="Lock"
                />
              </el-form-item>

              <el-form-item label="新密码" prop="newPassword">
                <el-input
                  v-model="passwordForm.newPassword"
                  type="password"
                  placeholder="请输入新密码"
                  show-password
                  clearable
                  :prefix-icon="Key"
                />
              </el-form-item>

              <el-form-item label="确认密码" prop="confirmPassword">
                <el-input
                  v-model="passwordForm.confirmPassword"
                  type="password"
                  placeholder="请再次输入新密码"
                  show-password
                  clearable
                  :prefix-icon="Key"
                />
              </el-form-item>

              <div class="form-actions">
                <el-button type="danger" @click="changePassword" :loading="changingPassword" size="large">
                  <el-icon><Edit /></el-icon>
                  修改密码
                </el-button>
              </div>
            </el-form>

            <!-- 安全提示 -->
            <div class="security-tips">
              <h4>
                <el-icon><InfoFilled /></el-icon>
                安全提示
              </h4>
              <ul>
                <li>密码长度应在6-20个字符之间</li>
                <li>建议包含字母、数字和特殊字符</li>
                <li>定期更换密码保护账户安全</li>
                <li>不要在公共设备上保存密码</li>
              </ul>
            </div>
          </div>
        </el-col>
      </el-row>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useUserStore } from '@/stores/user'
import { userAPI } from '@/api/user'
import { authAPI } from '@/api/auth'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  Upload, User, UserFilled, Message, Phone, Male, Female, Calendar, 
  CreditCard, Check, RefreshLeft, Lock, Key, Edit, InfoFilled, Camera, Delete
} from '@element-plus/icons-vue'
import AvatarUpload from '@/components/common/AvatarUpload.vue'

const userStore = useUserStore()

const profileFormRef = ref()
const passwordFormRef = ref()
const avatarUploadRef = ref()
const saving = ref(false)
const changingPassword = ref(false)

// 个人资料表单
const profileForm = reactive({
  username: '',
  realName: '',
  email: '',
  phone: '',
  gender: null,
  birthday: null,
  idCard: '',
  remark: ''
})

// 修改密码表单
const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

// 验证邮箱
const validateEmail = (rule, value, callback) => {
  if (value) {
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
    if (!emailRegex.test(value)) {
      callback(new Error('请输入正确的邮箱格式'))
    }
  }
  callback()
}

// 验证手机号
const validatePhone = (rule, value, callback) => {
  if (value) {
    const phoneRegex = /^1[3-9]\d{9}$/
    if (!phoneRegex.test(value)) {
      callback(new Error('请输入正确的手机号码'))
    }
  }
  callback()
}

// 验证身份证
const validateIdCard = (rule, value, callback) => {
  if (value) {
    const idCardRegex = /(^\d{15}$)|(^\d{18}$)|(^\d{17}(\d|X|x)$)/
    if (!idCardRegex.test(value)) {
      callback(new Error('请输入正确的身份证号码'))
    }
  }
  callback()
}

// 验证新密码确认
const validateConfirmPassword = (rule, value, callback) => {
  if (value !== passwordForm.newPassword) {
    callback(new Error('两次输入的密码不一致'))
  }
  callback()
}

// 个人资料验证规则
const profileRules = {
  realName: [
    { required: true, message: '请输入真实姓名', trigger: 'blur' },
    { min: 2, max: 10, message: '姓名长度在 2 到 10 个字符', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱地址', trigger: 'blur' },
    { validator: validateEmail, trigger: 'blur' }
  ],
  phone: [
    { validator: validatePhone, trigger: 'blur' }
  ],
  idCard: [
    { validator: validateIdCard, trigger: 'blur' }
  ]
}

// 密码验证规则
const passwordRules = {
  oldPassword: [
    { required: true, message: '请输入当前密码', trigger: 'blur' }
  ],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度在 6 到 20 个字符', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认新密码', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' }
  ]
}

// 初始化表单数据
const initProfileForm = () => {
  const userInfo = userStore.userInfo
  
  if (userInfo) {
    Object.assign(profileForm, {
      username: userInfo.username,
      realName: userInfo.realName || '',
      email: userInfo.email || '',
      phone: userInfo.phone || '',
      gender: userInfo.gender || null,
      birthday: userInfo.birthday || null,
      idCard: userInfo.idCard || '',
      remark: userInfo.remark || ''
    })
  }
}

// 保存个人资料
const saveProfile = async () => {
  if (saving.value) return

  try {
    const valid = await profileFormRef.value.validate()
    if (!valid) return

    saving.value = true

    const response = await userAPI.updateProfile(profileForm)
    
    if (response.success) {
      ElMessage.success('个人资料已更新')
      // 更新本地用户信息
      await userStore.getCurrentUser()
    }
  } catch (error) {
    console.error('保存个人资料失败:', error)
  } finally {
    saving.value = false
  }
}

// 重置表单
const resetForm = () => {
  profileFormRef.value.clearValidate()
  initProfileForm()
}

// 修改密码
const changePassword = async () => {
  if (changingPassword.value) return

  try {
    const valid = await passwordFormRef.value.validate()
    if (!valid) return

    changingPassword.value = true

    const response = await authAPI.changePassword({
      oldPassword: passwordForm.oldPassword,
      newPassword: passwordForm.newPassword
    })

    if (response.success) {
      ElMessage.success('密码修改成功，请重新登录')
      // 清空表单
      Object.assign(passwordForm, {
        oldPassword: '',
        newPassword: '',
        confirmPassword: ''
      })
      passwordFormRef.value.clearValidate()
      
      // 延迟登出，给用户看到成功消息
      setTimeout(() => {
        userStore.logout()
      }, 1500)
    }
  } catch (error) {
    console.error('修改密码失败:', error)
  } finally {
    changingPassword.value = false
  }
}

// 触发头像上传
const triggerAvatarUpload = () => {
  const avatarComponent = document.querySelector('.avatar-upload input[type="file"]')
  if (avatarComponent) {
    avatarComponent.click()
  }
}

// 触发头像删除
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
    // 触发AvatarUpload组件的删除方法
    userStore.updateAvatar(null)
    ElMessage.success('头像删除成功')
  } catch (error) {
    // 用户取消删除
  }
}

// 头像更新成功事件处理
const handleAvatarUpdated = (fileInfo) => {
  console.log('头像更新成功:', fileInfo)
  ElMessage.success('头像更新成功')
}

// 头像删除成功事件处理
const handleAvatarRemoved = () => {
  console.log('头像已删除')
}

onMounted(() => {
  initProfileForm()
})
</script>

<style scoped>
.profile-container {
  background: #f5f7fa;
  min-height: 100vh;
  padding: 24px;
}

/* 个人信息头部 */
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
  transition: all 0.3s ease;
}

.avatar-actions .el-button:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.2);
}

.user-basic-info {
  flex: 1;
  min-width: 0;
}

.user-name {
  font-size: 32px;
  font-weight: 700;
  color: #2c3e50;
  margin: 0 0 10px 0;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.user-email {
  font-size: 16px;
  color: #64748b;
  margin: 0 0 20px 0;
  display: flex;
  align-items: center;
  gap: 8px;
}

.user-tags {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.user-tags .el-tag {
  border-radius: 20px;
  padding: 8px 16px;
  font-weight: 500;
  border: none;
  font-size: 13px;
}

/* 表单内容区 */
.profile-content {
  padding: 40px 20px;
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
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.card-header h3 {
  font-size: 20px;
  font-weight: 600;
  color: #1e293b;
  margin: 0;
}

/* 现代化表单样式 */
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
  margin-bottom: 24px;
}

.modern-form :deep(.el-form-item__label) {
  font-weight: 600;
  color: #374151;
  margin-bottom: 8px;
}

.modern-form :deep(.el-input) {
  border-radius: 12px;
  transition: all 0.3s ease;
}

.modern-form :deep(.el-input__wrapper) {
  border-radius: 12px;
  border: 2px solid #e2e8f0;
  transition: all 0.3s ease;
  padding: 12px 16px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.02);
}

.modern-form :deep(.el-input__wrapper:hover) {
  border-color: #cbd5e1;
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.06);
}

.modern-form :deep(.el-input.is-focus .el-input__wrapper) {
  border-color: #667eea;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
}

.modern-form :deep(.el-input.is-disabled .el-input__wrapper) {
  background-color: #f8fafc;
  border-color: #e2e8f0;
}

.modern-form :deep(.el-date-editor) {
  width: 100%;
}

.modern-form :deep(.el-date-editor .el-input__wrapper) {
  border-radius: 12px;
}

/* 性别选择样式 */
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

.gender-radio :deep(.el-radio__input.is-checked .el-radio__inner) {
  background-color: #667eea;
  border-color: #667eea;
}

/* 文本域样式 */
.textarea-modern :deep(.el-textarea__inner) {
  border-radius: 12px;
  border: 2px solid #e2e8f0;
  padding: 16px;
  font-family: inherit;
  transition: all 0.3s ease;
  resize: vertical;
  min-height: 100px;
}

.textarea-modern :deep(.el-textarea__inner:hover) {
  border-color: #cbd5e1;
}

.textarea-modern :deep(.el-textarea__inner:focus) {
  border-color: #667eea;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
}

/* 按钮样式 */
.form-actions {
  display: flex;
  gap: 16px;
  margin-top: 30px;
  padding-top: 20px;
  border-top: 1px solid #f1f5f9;
}

.form-actions .el-button {
  border-radius: 12px;
  padding: 12px 24px;
  font-weight: 600;
  transition: all 0.3s ease;
  border: 2px solid transparent;
}

.form-actions .el-button--primary {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
  color: white;
}

.form-actions .el-button--primary:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 20px rgba(102, 126, 234, 0.3);
}

.form-actions .el-button--danger {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
  border: none;
  color: white;
}

.form-actions .el-button--danger:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 20px rgba(245, 87, 108, 0.3);
}

.form-actions .el-button:not(.el-button--primary):not(.el-button--danger) {
  background: #f8fafc;
  color: #64748b;
  border-color: #e2e8f0;
}

.form-actions .el-button:not(.el-button--primary):not(.el-button--danger):hover {
  background: #f1f5f9;
  border-color: #cbd5e1;
  transform: translateY(-1px);
}

/* 安全提示 */
.security-tips {
  margin-top: 30px;
  padding: 20px;
  background: linear-gradient(135deg, #fef3c7 0%, #fde68a 100%);
  border-radius: 12px;
  border: 1px solid #f59e0b;
}

.security-tips h4 {
  display: flex;
  align-items: center;
  gap: 8px;
  margin: 0 0 15px 0;
  font-size: 16px;
  font-weight: 600;
  color: #92400e;
}

.security-tips ul {
  margin: 0;
  padding-left: 20px;
  color: #92400e;
}

.security-tips li {
  margin-bottom: 8px;
  font-size: 14px;
  line-height: 1.6;
}

/* 响应式设计 */
@media (max-width: 1024px) {
  .user-info-card {
    flex-direction: column;
    text-align: center;
    padding: 30px 20px;
  }
  
  .form-grid {
    grid-template-columns: 1fr;
  }
  
  .form-actions {
    flex-direction: column;
  }
  
  .form-actions .el-button {
    width: 100%;
  }
}

@media (max-width: 768px) {
  .profile-header {
    padding: 20px 10px 100px;
  }
  
  .profile-content {
    padding: 20px 10px;
  }
  
  .form-card {
    padding: 20px;
    margin: 0 10px 20px;
  }
  
  .user-name {
    font-size: 24px;
  }
  
  .gender-group {
    flex-direction: column;
    gap: 10px;
  }
}

@media (max-width: 480px) {
  .user-info-card {
    margin: 0 10px;
    padding: 20px 15px;
  }
  
  .avatar-actions {
    bottom: -3px;
    right: -3px;
  }
  
  .avatar-actions .el-button {
    width: 28px;
    height: 28px;
  }
}

/* 深色模式兼容 */
@media (prefers-color-scheme: dark) {
  .form-card {
    background: #1f2937;
    border-color: #374151;
    color: #f9fafb;
  }
  
  .card-header h3 {
    color: #f9fafb;
  }
  
  .modern-form :deep(.el-form-item__label) {
    color: #d1d5db;
  }
}

/* 动画效果 */
@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(30px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.form-card {
  animation: fadeInUp 0.6s ease-out;
}

.user-info-card {
  animation: fadeInUp 0.8s ease-out;
}

/* 滚动条样式 */
:deep(.el-textarea__inner)::-webkit-scrollbar {
  width: 6px;
}

:deep(.el-textarea__inner)::-webkit-scrollbar-track {
  background: #f1f5f9;
  border-radius: 3px;
}

:deep(.el-textarea__inner)::-webkit-scrollbar-thumb {
  background: #cbd5e1;
  border-radius: 3px;
}

:deep(.el-textarea__inner)::-webkit-scrollbar-thumb:hover {
  background: #94a3b8;
}
</style> 