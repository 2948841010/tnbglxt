<template>
  <div class="login-page">
    <div class="login-container">
      <!-- 左侧登录表单 -->
      <div class="login-card">
        <div class="login-header">
          <div class="logo">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="32" height="32">
              <path d="M22 12h-4l-3 9L9 3l-3 9H2"/>
            </svg>
          </div>
          <h1>糖尿病智能管理系统</h1>
          <p>专业的健康管理与医疗咨询平台</p>
        </div>
        
        <!-- 用户类型选择 -->
        <div class="user-type-selector">
          <div class="type-option" :class="{ active: userType === 'patient' }" @click="userType = 'patient'">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="24" height="24">
              <path d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z"/>
            </svg>
            <span>患者登录</span>
          </div>
          <div class="type-option" :class="{ active: userType === 'doctor' }" @click="userType = 'doctor'">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="24" height="24">
              <path d="M5.121 17.804A13.937 13.937 0 0112 16c2.5 0 4.847.655 6.879 1.804M15 10a3 3 0 11-6 0 3 3 0 016 0zm6 2a9 9 0 11-18 0 9 9 0 0118 0z"/>
            </svg>
            <span>医生登录</span>
          </div>
        </div>

        <!-- 登录表单 -->
        <form @submit.prevent="handleLogin" class="login-form">
          <div class="form-group">
            <label>账号</label>
            <div class="input-wrapper">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="20" height="20">
                <path d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z"/>
              </svg>
              <input type="text" v-model="loginForm.account" placeholder="请输入账号" required/>
            </div>
          </div>
          
          <div class="form-group">
            <label>密码</label>
            <div class="input-wrapper">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="20" height="20">
                <path d="M12 15v2m-6 4h12a2 2 0 002-2v-6a2 2 0 00-2-2H6a2 2 0 00-2 2v6a2 2 0 002 2zm10-10V7a4 4 0 00-8 0v4h8z"/>
              </svg>
              <input type="password" v-model="loginForm.password" placeholder="请输入密码" required/>
            </div>
          </div>
          
          <div class="form-options">
            <label class="remember-me">
              <input type="checkbox" v-model="loginForm.rememberMe"/>
              <span>记住我</span>
            </label>
            <a href="#" class="forgot-password">忘记密码？</a>
          </div>

          <button type="submit" class="login-btn" :disabled="loading">
            {{ loading ? '登录中...' : '立即登录' }}
          </button>
        </form>

        <!-- 注册链接 -->
        <div class="register-section">
          <p>还没有账号？<a href="#" @click.prevent="goToRegister" class="register-link">立即注册</a></p>
        </div>

        <!-- 快速访问 -->
        <div class="quick-actions">
          <p>快速体验</p>
          <div class="quick-buttons">
            <button class="quick-btn" @click="quickLogin('patient')">体验患者端</button>
            <button class="quick-btn" @click="quickLogin('doctor')">体验医生端</button>
          </div>
        </div>
      </div>

      <!-- 右侧特色展示 -->
      <div class="features-side">
        <div class="features-content">
          <h2>为什么选择我们？</h2>
          <div class="feature">
            <div class="feature-icon">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="24" height="24">
                <path d="M9 19v-6a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2a2 2 0 002-2zm0 0V9a2 2 0 012-2h2a2 2 0 012 2v10m-6 0a2 2 0 002 2h2a2 2 0 002-2m0 0V5a2 2 0 012-2h2a2 2 0 012 2v14a2 2 0 01-2 2h-2a2 2 0 01-2-2z"/>
              </svg>
            </div>
            <div class="feature-text">
              <h3>智能健康监测</h3>
              <p>实时记录血糖、血压等健康数据</p>
            </div>
          </div>
          <div class="feature">
            <div class="feature-icon">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="24" height="24">
                <path d="M9.75 17L9 20l-1 1h8l-1-1-.75-3M3 13h18M5 17h14a2 2 0 002-2V5a2 2 0 00-2-2H5a2 2 0 00-2 2v10a2 2 0 002 2z"/>
              </svg>
            </div>
            <div class="feature-text">
              <h3>AI风险评估</h3>
              <p>基于大数据的个性化健康评估</p>
            </div>
          </div>
          <div class="feature">
            <div class="feature-icon">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="24" height="24">
                <path d="M17 20h5v-2a3 3 0 00-5.356-1.857M17 20H7m10 0v-2c0-.656-.126-1.283-.356-1.857M7 20H2v-2a3 3 0 015.356-1.857M7 20v-2c0-.656.126-1.283.356-1.857m0 0a5.002 5.002 0 019.288 0M15 7a3 3 0 11-6 0 3 3 0 016 0zm6 3a2 2 0 11-4 0 2 2 0 014 0zM7 10a2 2 0 11-4 0 2 2 0 014 0z"/>
              </svg>
            </div>
            <div class="feature-text">
              <h3>专业医疗咨询</h3>
              <p>7x24小时在线咨询服务</p>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'

const router = useRouter()
const userStore = useUserStore()
const userType = ref('patient')
const loading = ref(false)
const loginForm = reactive({ account: '', password: '', rememberMe: false })

const handleLogin = async () => {
  try {
    loading.value = true
    const loginData = { username: loginForm.account, password: loginForm.password, userType: userType.value === 'patient' ? 0 : 1 }
    const result = await userStore.login(loginData)
    if (result.success || result.code === 200) {
      if (userType.value === 'patient') { router.push('/dashboard') } 
      else { router.push('/doctor/dashboard') }
    }
  } catch (error) {
    console.error('登录失败:', error)
    ElMessage.error(error.message || '登录失败，请检查账号密码')
  } finally { loading.value = false }
}

const quickLogin = (type) => {
  userType.value = type
  if (type === 'patient') { loginForm.account = 'test'; loginForm.password = '123456' } 
  else { loginForm.account = 'doctor'; loginForm.password = '123456' }
}

const goToRegister = () => { router.push('/register') }
</script>

<style scoped>
.login-page { min-height: 100vh; background: linear-gradient(135deg, #0891B2 0%, #0E7490 100%); display: flex; align-items: center; justify-content: center; padding: 24px; font-family: 'Noto Sans', sans-serif; }
.login-container { max-width: 1000px; width: 100%; background: white; border-radius: 24px; overflow: hidden; box-shadow: 0 25px 80px rgba(0,0,0,0.3); display: grid; grid-template-columns: 1fr 1fr; }
.login-card { padding: 48px 40px; display: flex; flex-direction: column; justify-content: center; }
.login-header { text-align: center; margin-bottom: 32px; }
.logo { width: 64px; height: 64px; background: linear-gradient(135deg, #0891B2 0%, #22D3EE 100%); border-radius: 16px; display: flex; align-items: center; justify-content: center; margin: 0 auto 16px; color: white; }
.login-header h1 { font-size: 1.5rem; font-weight: 700; color: #164E63; margin: 0 0 8px; }
.login-header p { color: #64748B; margin: 0; font-size: 0.95rem; }
.user-type-selector { display: flex; gap: 12px; margin-bottom: 28px; }
.type-option { flex: 1; display: flex; flex-direction: column; align-items: center; gap: 8px; padding: 16px; border: 2px solid #E2E8F0; border-radius: 12px; cursor: pointer; transition: all 0.2s; color: #64748B; }
.type-option:hover { border-color: #0891B2; color: #0891B2; }
.type-option.active { border-color: #0891B2; background: linear-gradient(135deg, #0891B2 0%, #0E7490 100%); color: white; }
.type-option span { font-weight: 600; font-size: 0.9rem; }
.login-form { margin-bottom: 24px; }
.form-group { margin-bottom: 20px; }
.form-group label { display: block; margin-bottom: 8px; font-weight: 600; color: #164E63; font-size: 0.9rem; }
.input-wrapper { display: flex; align-items: center; gap: 12px; border: 2px solid #E2E8F0; border-radius: 12px; padding: 0 16px; transition: border-color 0.2s; }
.input-wrapper:focus-within { border-color: #0891B2; }
.input-wrapper svg { color: #94A3B8; flex-shrink: 0; }
.input-wrapper input { flex: 1; border: none; outline: none; padding: 14px 0; font-size: 1rem; color: #164E63; }
.input-wrapper input::placeholder { color: #94A3B8; }
.form-options { display: flex; justify-content: space-between; align-items: center; margin-bottom: 24px; }
.remember-me { display: flex; align-items: center; gap: 8px; color: #64748B; font-size: 0.9rem; cursor: pointer; }
.remember-me input { width: 18px; height: 18px; accent-color: #0891B2; }
.forgot-password { color: #0891B2; text-decoration: none; font-size: 0.9rem; font-weight: 500; }
.forgot-password:hover { text-decoration: underline; }
.login-btn { width: 100%; padding: 16px; background: linear-gradient(135deg, #059669 0%, #047857 100%); color: white; border: none; border-radius: 12px; font-size: 1rem; font-weight: 600; cursor: pointer; transition: all 0.2s; }
.login-btn:hover:not(:disabled) { transform: translateY(-2px); box-shadow: 0 8px 20px rgba(5,150,105,0.3); }
.login-btn:disabled { opacity: 0.6; cursor: not-allowed; }
.register-section { text-align: center; padding-bottom: 20px; border-bottom: 1px solid #E2E8F0; margin-bottom: 20px; }
.register-section p { margin: 0; color: #64748B; font-size: 0.9rem; }
.register-link { color: #0891B2; text-decoration: none; font-weight: 600; margin-left: 4px; }
.register-link:hover { text-decoration: underline; }
.quick-actions { text-align: center; }
.quick-actions > p { margin: 0 0 12px; color: #64748B; font-size: 0.875rem; }
.quick-buttons { display: flex; gap: 12px; }
.quick-btn { flex: 1; padding: 12px; background: #F8FAFC; color: #0891B2; border: 1px solid #E2E8F0; border-radius: 10px; font-size: 0.9rem; font-weight: 500; cursor: pointer; transition: all 0.2s; }
.quick-btn:hover { background: #0891B2; color: white; border-color: #0891B2; }
.features-side { background: linear-gradient(135deg, #0891B2 0%, #0E7490 100%); color: white; padding: 48px 40px; display: flex; align-items: center; }
.features-content h2 { font-size: 1.75rem; font-weight: 700; margin: 0 0 32px; }
.feature { display: flex; align-items: flex-start; gap: 16px; margin-bottom: 24px; }
.feature-icon { width: 48px; height: 48px; background: rgba(255,255,255,0.2); border-radius: 12px; display: flex; align-items: center; justify-content: center; flex-shrink: 0; }
.feature-text h3 { font-size: 1.1rem; font-weight: 600; margin: 0 0 6px; }
.feature-text p { margin: 0; font-size: 0.9rem; opacity: 0.9; line-height: 1.5; }
@media (max-width: 768px) {
  .login-container { grid-template-columns: 1fr; }
  .features-side { display: none; }
  .login-card { padding: 40px 24px; }
  .user-type-selector { flex-direction: column; }
  .type-option { flex-direction: row; justify-content: center; padding: 14px; }
  .quick-buttons { flex-direction: column; }
}
</style>
