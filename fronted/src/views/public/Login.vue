<template>
  <div class="login-page">
    <div class="login-container">
      <!-- 左侧登录表单 -->
      <div class="login-card">
        <div class="login-header">
          <div class="logo">🏥</div>
          <h1>糖尿病智能管理系统</h1>
          <p>专业的健康管理与医疗咨询平台</p>
        </div>
        
        <!-- 用户类型选择 -->
        <div class="user-type-selector">
          <div 
            class="type-option" 
            :class="{ active: userType === 'patient' }"
            @click="userType = 'patient'"
          >
            <div class="type-icon">👤</div>
            <span>患者登录</span>
          </div>
          <div 
            class="type-option" 
            :class="{ active: userType === 'doctor' }"
            @click="userType = 'doctor'"
          >
            <div class="type-icon">👨‍⚕️</div>
            <span>医生登录</span>
          </div>
        </div>

        <!-- 登录表单 -->
        <form @submit.prevent="handleLogin" class="login-form">
          <div class="form-group">
            <label>账号</label>
            <input
              type="text"
              v-model="loginForm.account"
              placeholder="请输入账号"
              required
            />
          </div>
          
          <div class="form-group">
            <label>密码</label>
            <input
              type="password"
              v-model="loginForm.password"
              placeholder="请输入密码"
              required
            />
          </div>
          
          <div class="form-options">
            <label class="remember-me">
              <input type="checkbox" v-model="loginForm.rememberMe" />
              记住我
            </label>
            <a href="#" class="forgot-password">忘记密码？</a>
          </div>

          <button 
            type="submit" 
            class="login-btn" 
            :disabled="loading"
          >
            {{ loading ? '登录中...' : '立即登录' }}
          </button>
        </form>

        <!-- 注册链接 -->
        <div class="register-section">
          <p>还没有账号？ 
            <a href="#" @click.prevent="goToRegister" class="register-link">
              立即注册
            </a>
          </p>
        </div>

        <!-- 快速访问 -->
        <div class="quick-actions">
          <p>快速体验</p>
          <div class="quick-buttons">
            <button class="quick-btn" @click="quickLogin('patient')">
              体验患者端
            </button>
            <button class="quick-btn" @click="quickLogin('doctor')">
              体验医生端
            </button>
          </div>
        </div>
      </div>

      <!-- 右侧特色展示 -->
      <div class="features-side">
        <div class="features-content">
          <h2>为什么选择我们？</h2>
          
          <div class="feature">
            <div class="feature-icon">🩺</div>
            <div class="feature-text">
              <h3>智能健康监测</h3>
              <p>实时记录血糖、血压等健康数据</p>
            </div>
          </div>
          
          <div class="feature">
            <div class="feature-icon">🤖</div>
            <div class="feature-text">
              <h3>AI风险评估</h3>
              <p>基于大数据的个性化健康评估</p>
            </div>
          </div>
          
          <div class="feature">
            <div class="feature-icon">👨‍⚕️</div>
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

const userType = ref('patient') // patient 或 doctor
const loading = ref(false)

const loginForm = reactive({
  account: '',
  password: '',
  rememberMe: false
})

const handleLogin = async () => {
  try {
    loading.value = true
    
    const loginData = {
      username: loginForm.account,
      password: loginForm.password,
      userType: userType.value === 'patient' ? 0 : 1
    }
    
    const result = await userStore.login(loginData)
    
    if (result.success || result.code === 200) {
      // 移除重复的登录成功提示，userStore中已经显示了
      // 根据用户类型跳转
      if (userType.value === 'patient') {
        router.push('/dashboard')
      } else {
        router.push('/doctor/dashboard')
      }
    }
  } catch (error) {
    console.error('登录失败:', error)
    ElMessage.error(error.message || '登录失败，请检查账号密码')
  } finally {
    loading.value = false
  }
}

const quickLogin = (type) => {
  userType.value = type
  if (type === 'patient') {
    loginForm.account = 'test'
    loginForm.password = '123456'
  } else {
    loginForm.account = 'doctor'
    loginForm.password = '123456'
  }
}

const goToRegister = () => {
  router.push('/register')
}
</script>

<style scoped>
.login-page {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 20px;
}

.login-container {
  max-width: 1000px;
  margin: 20px auto;
  background: white;
  border-radius: 20px;
  overflow: hidden;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
  display: grid;
  grid-template-columns: 1fr 1fr;
}

/* 左侧登录卡片 */
.login-card {
  padding: 50px 40px;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.login-header {
  text-align: center;
  margin-bottom: 30px;
}

.logo {
  width: 60px;
  height: 60px;
  background: linear-gradient(135deg, #667eea, #764ba2);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 15px;
  font-size: 30px;
  color: white;
}

.login-header h1 {
  font-size: 1.5rem;
  font-weight: 600;
  color: #333;
  margin: 0 0 8px 0;
}

.login-header p {
  color: #666;
  margin: 0;
}

/* 用户类型选择 */
.user-type-selector {
  display: flex;
  gap: 15px;
  margin-bottom: 30px;
}

.type-option {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 20px 15px;
  border: 2px solid #e2e8f0;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.type-option:hover {
  border-color: #667eea;
  background: #f8fafc;
}

.type-option.active {
  border-color: #667eea;
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: white;
}

.type-icon {
  font-size: 2rem;
  margin-bottom: 8px;
}

/* 登录表单 */
.login-form {
  margin-bottom: 30px;
}

.form-group {
  margin-bottom: 20px;
}

.form-group label {
  display: block;
  margin-bottom: 8px;
  font-weight: 500;
  color: #333;
}

.form-group input {
  width: 100%;
  padding: 12px 16px;
  border: 2px solid #e2e8f0;
  border-radius: 10px;
  font-size: 16px;
  transition: border-color 0.3s ease;
  box-sizing: border-box;
}

.form-group input:focus {
  outline: none;
  border-color: #667eea;
}

.form-options {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 25px;
}

.remember-me {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #666;
  font-size: 14px;
}

.forgot-password {
  color: #667eea;
  text-decoration: none;
  font-size: 14px;
}

.forgot-password:hover {
  text-decoration: underline;
}

.login-btn {
  width: 100%;
  padding: 15px;
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: white;
  border: none;
  border-radius: 10px;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
}

.login-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(102, 126, 234, 0.4);
}

.login-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

/* 注册链接 */
.register-section {
  text-align: center;
  margin-bottom: 20px;
  padding-bottom: 20px;
  border-bottom: 1px solid #e2e8f0;
}

.register-section p {
  margin: 0;
  color: #666;
  font-size: 14px;
}

.register-link {
  color: #667eea;
  text-decoration: none;
  font-weight: 500;
}

.register-link:hover {
  text-decoration: underline;
}

/* 快速访问 */
.quick-actions {
  text-align: center;
  padding-top: 20px;
  border-top: 1px solid #e2e8f0;
}

.quick-actions p {
  margin: 0 0 15px 0;
  color: #666;
  font-size: 14px;
}

.quick-buttons {
  display: flex;
  gap: 10px;
}

.quick-btn {
  flex: 1;
  padding: 10px 15px;
  background: #f8fafc;
  color: #667eea;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.quick-btn:hover {
  background: #667eea;
  color: white;
}

/* 右侧特色展示 */
.features-side {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  padding: 50px 40px;
  display: flex;
  align-items: center;
}

.features-content h2 {
  font-size: 1.8rem;
  font-weight: 600;
  margin-bottom: 30px;
  text-align: center;
}

.feature {
  display: flex;
  align-items: center;
  gap: 20px;
  margin-bottom: 25px;
}

.feature-icon {
  width: 50px;
  height: 50px;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.5rem;
  flex-shrink: 0;
}

.feature-text h3 {
  font-size: 1.1rem;
  font-weight: 600;
  margin: 0 0 5px 0;
}

.feature-text p {
  margin: 0;
  font-size: 0.9rem;
  opacity: 0.9;
  line-height: 1.4;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .login-container {
    grid-template-columns: 1fr;
    margin: 10px;
  }
  
  .features-side {
    display: none;
  }
  
  .login-card {
    padding: 40px 30px;
  }
  
  .user-type-selector {
    flex-direction: column;
  }
  
  .type-option {
    flex-direction: row;
    justify-content: center;
    padding: 15px;
  }
  
  .type-icon {
    margin-bottom: 0;
    margin-right: 10px;
  }
}

@media (max-width: 480px) {
  .login-page {
    padding: 10px;
  }
  
  .login-card {
    padding: 30px 20px;
  }
  
  .login-header h1 {
    font-size: 1.3rem;
  }
  
  .quick-buttons {
    flex-direction: column;
  }
}
</style> 