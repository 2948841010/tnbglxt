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
          <h1>欢迎回来</h1>
          <p>登录您的账户以继续</p>
        </div>

        <el-form ref="loginFormRef" :model="loginForm" :rules="loginRules" @submit.prevent="handleLogin">
          <!-- 用户类型选择 -->
          <div class="user-type-selector">
            <div class="type-option" :class="{ active: loginForm.userType === 0 }" @click="loginForm.userType = 0">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="20" height="20">
                <path d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z"/>
              </svg>
              <span>患者登录</span>
            </div>
            <div class="type-option" :class="{ active: loginForm.userType === 1 }" @click="loginForm.userType = 1">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="20" height="20">
                <path d="M5.121 17.804A13.937 13.937 0 0112 16c2.5 0 4.847.655 6.879 1.804M15 10a3 3 0 11-6 0 3 3 0 016 0zm6 2a9 9 0 11-18 0 9 9 0 0118 0z"/>
              </svg>
              <span>医生登录</span>
            </div>
          </div>

          <el-form-item prop="username">
            <el-input v-model="loginForm.username" size="large" :placeholder="loginForm.userType === 1 ? '请输入医生工号或邮箱' : '请输入用户名或邮箱'" prefix-icon="User" clearable :disabled="loading"/>
          </el-form-item>

          <el-form-item prop="password">
            <el-input v-model="loginForm.password" type="password" size="large" placeholder="请输入密码" prefix-icon="Lock" show-password clearable :disabled="loading" @keyup.enter="handleLogin"/>
          </el-form-item>

          <div class="form-options">
            <el-checkbox v-model="rememberMe" :disabled="loading">记住我</el-checkbox>
            <el-link type="primary" :underline="false" @click="$router.push('/forgot-password')">忘记密码？</el-link>
          </div>

          <el-button type="primary" size="large" class="login-btn" :loading="loading" @click="handleLogin">
            {{ loading ? '登录中...' : '登录' }}
          </el-button>

          <div class="register-link" v-if="loginForm.userType === 0">
            <span>还没有账户？</span>
            <el-link type="primary" :underline="false" @click="$router.push('/register')">立即注册</el-link>
          </div>
          <div class="register-link" v-else>
            <span class="doctor-tip">医生账号由系统管理员分配</span>
          </div>
        </el-form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()
const loginFormRef = ref()
const loading = ref(false)
const rememberMe = ref(true)

const loginForm = reactive({ username: '', password: '', userType: 0 })

const loginRules = {
  userType: [{ required: true, message: '请选择用户类型', trigger: 'change' }],
  username: [{ required: true, message: '请输入用户名或邮箱', trigger: 'blur' }, { min: 2, max: 50, message: '用户名长度在 2 到 50 个字符', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }, { min: 6, max: 20, message: '密码长度在 6 到 20 个字符', trigger: 'blur' }]
}

const handleLogin = async () => {
  if (loading.value) return
  try {
    const valid = await loginFormRef.value.validate()
    if (!valid) return
    loading.value = true
    const result = await userStore.login(loginForm)
    if (result.success) {
      const userType = userStore.userInfo?.userType
      let defaultPath = '/dashboard'
      if (userType === 1) { defaultPath = '/doctor/dashboard' }
      const redirect = router.currentRoute.value.query.redirect || defaultPath
      router.push(redirect)
    }
  } catch (error) { console.error('登录失败:', error) } 
  finally { loading.value = false }
}

onMounted(() => {
  if (userStore.isAuthenticated) {
    const defaultPath = userStore.userInfo?.userType === 1 ? '/doctor/dashboard' : '/dashboard'
    router.push(defaultPath)
  }
})
</script>

<style scoped>
.auth-page { min-height: 100vh; background: linear-gradient(135deg, #ECFEFF 0%, #CFFAFE 100%); display: flex; align-items: center; justify-content: center; padding: 24px; font-family: 'Noto Sans', sans-serif; position: relative; overflow: hidden; }
.auth-page::before { content: ''; position: absolute; top: -50%; left: -50%; width: 200%; height: 200%; background: radial-gradient(circle, rgba(8,145,178,0.08) 0%, transparent 50%); animation: rotate 30s linear infinite; }
@keyframes rotate { 0% { transform: rotate(0deg); } 100% { transform: rotate(360deg); } }
.auth-container { position: relative; z-index: 1; width: 100%; max-width: 420px; }
.auth-card { background: white; border-radius: 24px; padding: 40px; box-shadow: 0 20px 60px rgba(8,145,178,0.15); border: 1px solid #A5F3FC; }
.auth-header { text-align: center; margin-bottom: 32px; }
.logo { display: inline-flex; align-items: center; justify-content: center; width: 56px; height: 56px; background: linear-gradient(135deg, #0891B2 0%, #22D3EE 100%); border-radius: 14px; color: white; margin-bottom: 20px; }
.auth-header h1 { font-size: 1.75rem; font-weight: 700; color: #164E63; margin: 0 0 8px; }
.auth-header p { color: #64748B; margin: 0; font-size: 0.95rem; }
.user-type-selector { display: flex; gap: 12px; margin-bottom: 24px; }
.type-option { flex: 1; display: flex; align-items: center; justify-content: center; gap: 8px; padding: 14px; border: 2px solid #E2E8F0; border-radius: 12px; cursor: pointer; transition: all 0.2s; color: #64748B; font-weight: 500; font-size: 0.9rem; }
.type-option:hover { border-color: #0891B2; color: #0891B2; }
.type-option.active { border-color: #0891B2; background: linear-gradient(135deg, #0891B2 0%, #0E7490 100%); color: white; }
.form-options { display: flex; justify-content: space-between; align-items: center; margin-bottom: 24px; }
.login-btn { width: 100%; height: 48px; font-size: 1rem; font-weight: 600; border-radius: 12px; background: linear-gradient(135deg, #059669 0%, #047857 100%); border: none; }
.login-btn:hover { background: linear-gradient(135deg, #047857 0%, #065F46 100%); }
.register-link { text-align: center; margin-top: 24px; font-size: 0.9rem; color: #64748B; }
.doctor-tip { font-size: 0.85rem; color: #94A3B8; }
:deep(.el-input__wrapper) { box-shadow: 0 0 0 2px #E2E8F0 inset; border-radius: 12px; padding: 4px 12px; }
:deep(.el-input__wrapper:hover) { box-shadow: 0 0 0 2px #A5F3FC inset; }
:deep(.el-input.is-focus .el-input__wrapper) { box-shadow: 0 0 0 2px #0891B2 inset; }
:deep(.el-checkbox__label) { color: #64748B; }
:deep(.el-link) { color: #0891B2; font-weight: 500; }
</style>
