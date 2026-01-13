<template>
  <div class="auth-page">
    <div class="auth-container">
      <div class="auth-card">
        <div class="auth-header">
          <router-link to="/" class="logo">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="28" height="28">
              <path d="M22 12h-4l-3 9L9 3l-3 9H2"/>
            </svg>
          </router-link>
          <h1>创建账户</h1>
          <p>加入我们的健康管理平台</p>
        </div>

        <el-form ref="registerFormRef" :model="registerForm" :rules="registerRules" @submit.prevent="handleRegister">
          <el-form-item prop="username">
            <el-input v-model="registerForm.username" size="large" placeholder="请输入用户名" prefix-icon="User" clearable :disabled="loading"/>
          </el-form-item>

          <el-form-item prop="realName">
            <el-input v-model="registerForm.realName" size="large" placeholder="请输入真实姓名" prefix-icon="UserFilled" clearable :disabled="loading"/>
          </el-form-item>

          <el-form-item prop="email">
            <el-input v-model="registerForm.email" size="large" placeholder="请输入邮箱地址" prefix-icon="Message" clearable :disabled="loading"/>
          </el-form-item>

          <el-form-item prop="emailCode">
            <div class="code-input-group">
              <el-input v-model="registerForm.emailCode" size="large" placeholder="请输入邮箱验证码" prefix-icon="Key" clearable :disabled="loading"/>
              <el-button size="large" :disabled="!canSendCode || sendingCode" :loading="sendingCode" @click="sendEmailCode" class="code-btn">
                {{ codeButtonText }}
              </el-button>
            </div>
          </el-form-item>

          <el-form-item prop="phone">
            <el-input v-model="registerForm.phone" size="large" placeholder="请输入手机号码（可选）" prefix-icon="Phone" clearable :disabled="loading"/>
          </el-form-item>

          <el-form-item prop="password">
            <el-input v-model="registerForm.password" type="password" size="large" placeholder="请输入密码" prefix-icon="Lock" show-password clearable :disabled="loading"/>
          </el-form-item>

          <el-form-item prop="confirmPassword">
            <el-input v-model="registerForm.confirmPassword" type="password" size="large" placeholder="请确认密码" prefix-icon="Lock" show-password clearable :disabled="loading" @keyup.enter="handleRegister"/>
          </el-form-item>

          <el-form-item>
            <el-checkbox v-model="agreeTerms" :disabled="loading">
              我已阅读并同意
              <el-link type="primary" :underline="false" @click="showTerms">用户协议</el-link>
              和
              <el-link type="primary" :underline="false" @click="showPrivacy">隐私政策</el-link>
            </el-checkbox>
          </el-form-item>

          <el-button type="primary" size="large" class="register-btn" :loading="loading" :disabled="!agreeTerms" @click="handleRegister">
            {{ loading ? '注册中...' : '注册账户' }}
          </el-button>

          <div class="login-link">
            <span>已有账户？</span>
            <el-link type="primary" :underline="false" @click="$router.push('/login')">立即登录</el-link>
          </div>
        </el-form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onBeforeUnmount } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessage, ElMessageBox } from 'element-plus'

const router = useRouter()
const userStore = useUserStore()
const registerFormRef = ref()
const loading = ref(false)
const sendingCode = ref(false)
const agreeTerms = ref(false)
const countdown = ref(0)
let countdownTimer = null

const registerForm = reactive({ username: '', realName: '', email: '', emailCode: '', phone: '', password: '', confirmPassword: '' })

const validateEmail = (rule, value, callback) => {
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
  if (!value) { callback(new Error('请输入邮箱地址')) } 
  else if (!emailRegex.test(value)) { callback(new Error('请输入正确的邮箱格式')) } 
  else { callback() }
}

const validatePasswordConfirm = (rule, value, callback) => {
  if (!value) { callback(new Error('请确认密码')) } 
  else if (value !== registerForm.password) { callback(new Error('两次输入的密码不一致')) } 
  else { callback() }
}

const validatePhone = (rule, value, callback) => {
  if (value) {
    const phoneRegex = /^1[3-9]\d{9}$/
    if (!phoneRegex.test(value)) { callback(new Error('请输入正确的手机号码')) }
  }
  callback()
}

const registerRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }, { min: 3, max: 20, message: '用户名长度在 3 到 20 个字符', trigger: 'blur' }, { pattern: /^[a-zA-Z0-9_]+$/, message: '用户名只能包含字母、数字和下划线', trigger: 'blur' }],
  realName: [{ required: true, message: '请输入真实姓名', trigger: 'blur' }, { min: 2, max: 10, message: '姓名长度在 2 到 10 个字符', trigger: 'blur' }],
  email: [{ required: true, validator: validateEmail, trigger: 'blur' }],
  emailCode: [{ required: true, message: '请输入邮箱验证码', trigger: 'blur' }, { len: 6, message: '验证码应为6位数字', trigger: 'blur' }],
  phone: [{ validator: validatePhone, trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }, { min: 6, max: 20, message: '密码长度在 6 到 20 个字符', trigger: 'blur' }, { pattern: /^(?=.*[a-zA-Z])(?=.*\d)[a-zA-Z\d@$!%*?&]+$/, message: '密码必须包含字母和数字', trigger: 'blur' }],
  confirmPassword: [{ validator: validatePasswordConfirm, trigger: 'blur' }]
}

const canSendCode = computed(() => {
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
  return emailRegex.test(registerForm.email) && countdown.value === 0
})

const codeButtonText = computed(() => {
  if (sendingCode.value) return '发送中'
  if (countdown.value > 0) return `${countdown.value}s后重发`
  return '获取验证码'
})

const sendEmailCode = async () => {
  if (!canSendCode.value) return
  try {
    sendingCode.value = true
    const result = await userStore.sendEmailCode(registerForm.email, 'register')
    if (result.success) {
      countdown.value = 60
      countdownTimer = setInterval(() => { countdown.value--; if (countdown.value <= 0) { clearInterval(countdownTimer) } }, 1000)
    }
  } catch (error) { console.error('发送验证码失败:', error) } 
  finally { sendingCode.value = false }
}

const handleRegister = async () => {
  if (loading.value || !agreeTerms.value) return
  try {
    const valid = await registerFormRef.value.validate()
    if (!valid) return
    loading.value = true
    const result = await userStore.register(registerForm)
    if (result.success) { ElMessage.success('注册成功！请登录您的账户'); router.push('/login') }
  } catch (error) { console.error('注册失败:', error) } 
  finally { loading.value = false }
}

const showTerms = () => { ElMessageBox.alert('这里是用户协议内容...', '用户协议', { confirmButtonText: '我知道了' }) }
const showPrivacy = () => { ElMessageBox.alert('这里是隐私政策内容...', '隐私政策', { confirmButtonText: '我知道了' }) }

onBeforeUnmount(() => { if (countdownTimer) { clearInterval(countdownTimer) } })
</script>

<style scoped>
.auth-page { min-height: 100vh; background: linear-gradient(135deg, #ECFEFF 0%, #CFFAFE 100%); display: flex; align-items: center; justify-content: center; padding: 24px; font-family: 'Noto Sans', sans-serif; position: relative; overflow: hidden; }
.auth-page::before { content: ''; position: absolute; top: -50%; left: -50%; width: 200%; height: 200%; background: radial-gradient(circle, rgba(8,145,178,0.08) 0%, transparent 50%); animation: rotate 30s linear infinite; }
@keyframes rotate { 0% { transform: rotate(0deg); } 100% { transform: rotate(360deg); } }
.auth-container { position: relative; z-index: 1; width: 100%; max-width: 450px; }
.auth-card { background: white; border-radius: 24px; padding: 40px; box-shadow: 0 20px 60px rgba(8,145,178,0.15); border: 1px solid #A5F3FC; }
.auth-header { text-align: center; margin-bottom: 28px; }
.logo { display: inline-flex; align-items: center; justify-content: center; width: 56px; height: 56px; background: linear-gradient(135deg, #0891B2 0%, #22D3EE 100%); border-radius: 14px; color: white; margin-bottom: 16px; }
.auth-header h1 { font-size: 1.75rem; font-weight: 700; color: #164E63; margin: 0 0 8px; }
.auth-header p { color: #64748B; margin: 0; font-size: 0.95rem; }
.code-input-group { display: flex; gap: 12px; width: 100%; }
.code-input-group .el-input { flex: 1; }
.code-btn { white-space: nowrap; background: #0891B2; border-color: #0891B2; color: white; }
.code-btn:hover { background: #0E7490; border-color: #0E7490; }
.code-btn:disabled { background: #E2E8F0; border-color: #E2E8F0; color: #94A3B8; }
.register-btn { width: 100%; height: 48px; font-size: 1rem; font-weight: 600; border-radius: 12px; background: linear-gradient(135deg, #059669 0%, #047857 100%); border: none; margin-top: 8px; }
.register-btn:hover:not(:disabled) { background: linear-gradient(135deg, #047857 0%, #065F46 100%); }
.register-btn:disabled { background: #E2E8F0; color: #94A3B8; }
.login-link { text-align: center; margin-top: 24px; font-size: 0.9rem; color: #64748B; }
:deep(.el-input__wrapper) { box-shadow: 0 0 0 2px #E2E8F0 inset; border-radius: 12px; padding: 4px 12px; }
:deep(.el-input__wrapper:hover) { box-shadow: 0 0 0 2px #A5F3FC inset; }
:deep(.el-input.is-focus .el-input__wrapper) { box-shadow: 0 0 0 2px #0891B2 inset; }
:deep(.el-checkbox__label) { color: #64748B; font-size: 0.9rem; }
:deep(.el-link) { color: #0891B2; font-weight: 500; }
:deep(.el-form-item) { margin-bottom: 18px; }
</style>
