<template>
  <div class="profile-page">
    <!-- 个人信息头部 -->
    <div class="profile-header">
      <div class="user-card">
        <div class="avatar-section">
          <AvatarUpload ref="avatarUploadRef" size="large" :show-buttons="false" @avatar-updated="handleAvatarUpdated" @avatar-removed="handleAvatarRemoved"/>
          <div class="avatar-actions">
            <button class="avatar-btn primary" @click="triggerAvatarUpload">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="16" height="16"><path d="M3 9a2 2 0 012-2h.93a2 2 0 001.664-.89l.812-1.22A2 2 0 0110.07 4h3.86a2 2 0 011.664.89l.812 1.22A2 2 0 0018.07 7H19a2 2 0 012 2v9a2 2 0 01-2 2H5a2 2 0 01-2-2V9z"/><circle cx="12" cy="13" r="3"/></svg>
            </button>
            <button class="avatar-btn danger" @click="triggerAvatarRemove" v-if="userStore.userInfo?.avatar">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="16" height="16"><path d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16"/></svg>
            </button>
          </div>
        </div>
        <div class="user-info">
          <h2>{{ userStore.userInfo?.realName || userStore.userInfo?.username || '未设置' }}</h2>
          <p class="user-email">{{ userStore.userInfo?.email || '未设置邮箱' }}</p>
          <div class="user-tags">
            <span class="tag success" v-if="userStore.userInfo?.userType === 0">普通用户</span>
            <span class="tag warning" v-else-if="userStore.userInfo?.userType === 1">医生</span>
            <span class="tag danger" v-else-if="userStore.userInfo?.userType === 2">管理员</span>
            <span class="tag info" v-if="userStore.userInfo?.phone">已绑定手机</span>
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
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="22" height="22"><path d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z"/></svg>
              <h3>基本信息</h3>
            </div>
            <el-form ref="profileFormRef" :model="profileForm" :rules="profileRules" label-width="80px" class="profile-form">
              <div class="form-grid">
                <el-form-item label="用户名" prop="username">
                  <el-input v-model="profileForm.username" disabled prefix-icon="User"/>
                </el-form-item>
                <el-form-item label="真实姓名" prop="realName">
                  <el-input v-model="profileForm.realName" placeholder="请输入真实姓名" clearable/>
                </el-form-item>
                <el-form-item label="邮箱" prop="email">
                  <el-input v-model="profileForm.email" placeholder="请输入邮箱" clearable/>
                </el-form-item>
                <el-form-item label="手机号码" prop="phone">
                  <el-input v-model="profileForm.phone" placeholder="请输入手机号码" clearable/>
                </el-form-item>
                <el-form-item label="性别" prop="gender">
                  <el-radio-group v-model="profileForm.gender">
                    <el-radio label="男">男</el-radio>
                    <el-radio label="女">女</el-radio>
                  </el-radio-group>
                </el-form-item>
                <el-form-item label="生日" prop="birthday">
                  <el-date-picker v-model="profileForm.birthday" type="date" placeholder="请选择生日" style="width: 100%"/>
                </el-form-item>
              </div>
              <el-form-item label="身份证号" prop="idCard">
                <el-input v-model="profileForm.idCard" placeholder="请输入身份证号" clearable/>
              </el-form-item>
              <el-form-item label="个人简介">
                <el-input v-model="profileForm.remark" type="textarea" :rows="3" placeholder="介绍一下自己吧..." maxlength="200" show-word-limit/>
              </el-form-item>
              <div class="form-actions">
                <el-button type="primary" @click="saveProfile" :loading="saving" class="save-btn">保存修改</el-button>
                <el-button @click="resetForm">重置</el-button>
              </div>
            </el-form>
          </div>
        </el-col>

        <!-- 安全设置 -->
        <el-col :xs="24" :lg="10">
          <div class="form-card">
            <div class="card-header">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="22" height="22"><path d="M12 15v2m-6 4h12a2 2 0 002-2v-6a2 2 0 00-2-2H6a2 2 0 00-2 2v6a2 2 0 002 2zm10-10V7a4 4 0 00-8 0v4h8z"/></svg>
              <h3>安全设置</h3>
            </div>
            <el-form ref="passwordFormRef" :model="passwordForm" :rules="passwordRules" label-width="80px" class="profile-form">
              <el-form-item label="当前密码" prop="oldPassword">
                <el-input v-model="passwordForm.oldPassword" type="password" placeholder="请输入当前密码" show-password clearable/>
              </el-form-item>
              <el-form-item label="新密码" prop="newPassword">
                <el-input v-model="passwordForm.newPassword" type="password" placeholder="请输入新密码" show-password clearable/>
              </el-form-item>
              <el-form-item label="确认密码" prop="confirmPassword">
                <el-input v-model="passwordForm.confirmPassword" type="password" placeholder="请再次输入新密码" show-password clearable/>
              </el-form-item>
              <div class="form-actions">
                <el-button type="danger" @click="changePassword" :loading="changingPassword" class="change-pwd-btn">修改密码</el-button>
              </div>
            </el-form>

            <div class="security-tips">
              <div class="tips-header">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="18" height="18"><path d="M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"/></svg>
                <span>安全提示</span>
              </div>
              <ul>
                <li>密码长度应在6-20个字符之间</li>
                <li>建议包含字母、数字和特殊字符</li>
                <li>定期更换密码保护账户安全</li>
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
import AvatarUpload from '@/components/common/AvatarUpload.vue'

const userStore = useUserStore()
const profileFormRef = ref()
const passwordFormRef = ref()
const avatarUploadRef = ref()
const saving = ref(false)
const changingPassword = ref(false)

const profileForm = reactive({ username: '', realName: '', email: '', phone: '', gender: null, birthday: null, idCard: '', remark: '' })
const passwordForm = reactive({ oldPassword: '', newPassword: '', confirmPassword: '' })

const validateEmail = (rule, value, callback) => { if (value) { const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/; if (!emailRegex.test(value)) { callback(new Error('请输入正确的邮箱格式')) } } callback() }
const validatePhone = (rule, value, callback) => { if (value) { const phoneRegex = /^1[3-9]\d{9}$/; if (!phoneRegex.test(value)) { callback(new Error('请输入正确的手机号码')) } } callback() }
const validateIdCard = (rule, value, callback) => { if (value) { const idCardRegex = /(^\d{15}$)|(^\d{18}$)|(^\d{17}(\d|X|x)$)/; if (!idCardRegex.test(value)) { callback(new Error('请输入正确的身份证号码')) } } callback() }
const validateConfirmPassword = (rule, value, callback) => { if (value !== passwordForm.newPassword) { callback(new Error('两次输入的密码不一致')) } callback() }

const profileRules = {
  realName: [{ required: true, message: '请输入真实姓名', trigger: 'blur' }, { min: 2, max: 10, message: '姓名长度在 2 到 10 个字符', trigger: 'blur' }],
  email: [{ required: true, message: '请输入邮箱地址', trigger: 'blur' }, { validator: validateEmail, trigger: 'blur' }],
  phone: [{ validator: validatePhone, trigger: 'blur' }],
  idCard: [{ validator: validateIdCard, trigger: 'blur' }]
}

const passwordRules = {
  oldPassword: [{ required: true, message: '请输入当前密码', trigger: 'blur' }],
  newPassword: [{ required: true, message: '请输入新密码', trigger: 'blur' }, { min: 6, max: 20, message: '密码长度在 6 到 20 个字符', trigger: 'blur' }],
  confirmPassword: [{ required: true, message: '请确认新密码', trigger: 'blur' }, { validator: validateConfirmPassword, trigger: 'blur' }]
}

const initProfileForm = () => {
  const userInfo = userStore.userInfo
  if (userInfo) { Object.assign(profileForm, { username: userInfo.username, realName: userInfo.realName || '', email: userInfo.email || '', phone: userInfo.phone || '', gender: userInfo.gender || null, birthday: userInfo.birthday || null, idCard: userInfo.idCard || '', remark: userInfo.remark || '' }) }
}

const saveProfile = async () => {
  if (saving.value) return
  try { const valid = await profileFormRef.value.validate(); if (!valid) return; saving.value = true; const response = await userAPI.updateProfile(profileForm); if (response.success) { ElMessage.success('个人资料已更新'); await userStore.getCurrentUser() } } catch (error) { console.error('保存个人资料失败:', error) } finally { saving.value = false }
}

const resetForm = () => { profileFormRef.value.clearValidate(); initProfileForm() }

const changePassword = async () => {
  if (changingPassword.value) return
  try { const valid = await passwordFormRef.value.validate(); if (!valid) return; changingPassword.value = true; const response = await authAPI.changePassword({ oldPassword: passwordForm.oldPassword, newPassword: passwordForm.newPassword }); if (response.success) { ElMessage.success('密码修改成功，请重新登录'); Object.assign(passwordForm, { oldPassword: '', newPassword: '', confirmPassword: '' }); passwordFormRef.value.clearValidate(); setTimeout(() => { userStore.logout() }, 1500) } } catch (error) { console.error('修改密码失败:', error) } finally { changingPassword.value = false }
}

const triggerAvatarUpload = () => { const avatarComponent = document.querySelector('.avatar-upload input[type="file"]'); if (avatarComponent) { avatarComponent.click() } }
const triggerAvatarRemove = async () => { try { await ElMessageBox.confirm('确定要删除当前头像吗？', '删除头像', { confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning' }); userStore.updateAvatar(null); ElMessage.success('头像删除成功') } catch (error) {} }
const handleAvatarUpdated = (fileInfo) => { ElMessage.success('头像更新成功') }
const handleAvatarRemoved = () => {}

onMounted(() => { initProfileForm() })
</script>

<style scoped>
.profile-page { max-width: 1200px; margin: 0 auto; }

/* 头部卡片 */
.profile-header { margin-bottom: 24px; }
.user-card { background: linear-gradient(135deg, #0891B2 0%, #0E7490 100%); border-radius: 20px; padding: 32px; display: flex; align-items: center; gap: 32px; }
.avatar-section { position: relative; }
.avatar-actions { position: absolute; bottom: -8px; right: -8px; display: flex; gap: 6px; }
.avatar-btn { width: 32px; height: 32px; border-radius: 50%; border: 2px solid white; cursor: pointer; display: flex; align-items: center; justify-content: center; transition: all 0.2s; }
.avatar-btn.primary { background: #0891B2; color: white; }
.avatar-btn.primary:hover { background: #0E7490; transform: scale(1.1); }
.avatar-btn.danger { background: #DC2626; color: white; }
.avatar-btn.danger:hover { background: #B91C1C; transform: scale(1.1); }
.user-info { flex: 1; color: white; }
.user-info h2 { font-size: 1.75rem; font-weight: 700; margin: 0 0 8px; }
.user-email { font-size: 1rem; margin: 0 0 16px; opacity: 0.9; }
.user-tags { display: flex; gap: 10px; flex-wrap: wrap; }
.tag { padding: 6px 14px; border-radius: 20px; font-size: 0.8rem; font-weight: 500; }
.tag.success { background: rgba(5, 150, 105, 0.2); color: #A7F3D0; border: 1px solid rgba(167, 243, 208, 0.3); }
.tag.warning { background: rgba(217, 119, 6, 0.2); color: #FDE68A; border: 1px solid rgba(253, 230, 138, 0.3); }
.tag.danger { background: rgba(220, 38, 38, 0.2); color: #FECACA; border: 1px solid rgba(254, 202, 202, 0.3); }
.tag.info { background: rgba(34, 211, 238, 0.2); color: #A5F3FC; border: 1px solid rgba(165, 243, 252, 0.3); }

/* 表单卡片 */
.profile-content { padding: 0; }
.form-card { background: white; border-radius: 20px; padding: 28px; border: 1px solid #E2E8F0; margin-bottom: 24px; }
.card-header { display: flex; align-items: center; gap: 12px; margin-bottom: 24px; padding-bottom: 16px; border-bottom: 1px solid #E2E8F0; }
.card-header svg { color: #0891B2; }
.card-header h3 { font-size: 1.1rem; font-weight: 600; color: #164E63; margin: 0; }

/* 表单样式 */
.form-grid { display: grid; grid-template-columns: repeat(2, 1fr); gap: 16px; }
.profile-form :deep(.el-form-item) { margin-bottom: 20px; }
.profile-form :deep(.el-form-item__label) { font-weight: 500; color: #374151; }
.profile-form :deep(.el-input__wrapper) { border-radius: 10px; border: 2px solid #E2E8F0; box-shadow: none; padding: 8px 12px; transition: all 0.2s; }
.profile-form :deep(.el-input__wrapper:hover) { border-color: #A5F3FC; }
.profile-form :deep(.el-input.is-focus .el-input__wrapper) { border-color: #0891B2; box-shadow: 0 0 0 3px rgba(8, 145, 178, 0.1); }
.profile-form :deep(.el-textarea__inner) { border-radius: 10px; border: 2px solid #E2E8F0; padding: 12px; transition: all 0.2s; }
.profile-form :deep(.el-textarea__inner:hover) { border-color: #A5F3FC; }
.profile-form :deep(.el-textarea__inner:focus) { border-color: #0891B2; box-shadow: 0 0 0 3px rgba(8, 145, 178, 0.1); }
.profile-form :deep(.el-radio__input.is-checked .el-radio__inner) { background-color: #0891B2; border-color: #0891B2; }
.profile-form :deep(.el-radio__input.is-checked + .el-radio__label) { color: #0891B2; }

/* 按钮 */
.form-actions { display: flex; gap: 12px; margin-top: 24px; padding-top: 20px; border-top: 1px solid #E2E8F0; }
.save-btn { background: linear-gradient(135deg, #059669 0%, #10B981 100%); border: none; border-radius: 10px; padding: 12px 28px; font-weight: 600; }
.save-btn:hover { background: linear-gradient(135deg, #047857 0%, #059669 100%); }
.change-pwd-btn { background: linear-gradient(135deg, #DC2626 0%, #EF4444 100%); border: none; border-radius: 10px; padding: 12px 28px; font-weight: 600; }
.change-pwd-btn:hover { background: linear-gradient(135deg, #B91C1C 0%, #DC2626 100%); }

/* 安全提示 */
.security-tips { margin-top: 24px; padding: 20px; background: #FFF7ED; border-radius: 12px; border: 1px solid #FED7AA; }
.tips-header { display: flex; align-items: center; gap: 8px; margin-bottom: 12px; font-weight: 600; color: #D97706; }
.security-tips ul { margin: 0; padding-left: 20px; color: #92400E; font-size: 0.875rem; line-height: 1.8; }

/* 响应式 */
@media (max-width: 1024px) { .form-grid { grid-template-columns: 1fr; } }
@media (max-width: 768px) { .user-card { flex-direction: column; text-align: center; } .user-info h2 { font-size: 1.5rem; } .user-tags { justify-content: center; } .form-actions { flex-direction: column; } .form-actions .el-button { width: 100%; } }
</style>
