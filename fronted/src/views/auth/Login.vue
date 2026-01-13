<template>
  <div class="auth-container">
    <div class="auth-card">
      <div class="auth-title">欢迎回来</div>
      <div class="auth-subtitle">登录您的账户以继续</div>

      <el-form
        ref="loginFormRef"
        :model="loginForm"
        :rules="loginRules"
        @submit.prevent="handleLogin"
      >
        <!-- 用户类型选择 -->
        <el-form-item prop="userType">
          <el-radio-group v-model="loginForm.userType" size="large" style="width: 100%; justify-content: center;">
            <el-radio-button :label="0" style="width: 50%;">
              <el-icon style="margin-right: 4px;"><User /></el-icon>
              患者登录
            </el-radio-button>
            <el-radio-button :label="1" style="width: 50%;">
              <el-icon style="margin-right: 4px;"><Monitor /></el-icon>
              医生登录
            </el-radio-button>
          </el-radio-group>
        </el-form-item>

        <el-form-item prop="username">
          <el-input
            v-model="loginForm.username"
            size="large"
            :placeholder="loginForm.userType === 1 ? '请输入医生工号或邮箱' : '请输入用户名或邮箱'"
            prefix-icon="User"
            clearable
            :disabled="loading"
          />
        </el-form-item>

        <el-form-item prop="password">
          <el-input
            v-model="loginForm.password"
            type="password"
            size="large"
            placeholder="请输入密码"
            prefix-icon="Lock"
            show-password
            clearable
            :disabled="loading"
            @keyup.enter="handleLogin"
          />
        </el-form-item>

        <div class="flex-between mb-24">
          <el-checkbox v-model="rememberMe" :disabled="loading">
            记住我
          </el-checkbox>
          <el-link type="primary" :underline="false" @click="$router.push('/forgot-password')">
            忘记密码？
          </el-link>
        </div>

        <el-button
          type="primary"
          size="large"
          style="width: 100%; margin-bottom: 16px"
          :loading="loading"
          @click="handleLogin"
        >
          {{ loading ? '登录中...' : '登录' }}
        </el-button>

        <div class="text-center" v-if="loginForm.userType === 0">
          <span style="color: #666">还没有账户？</span>
          <el-link type="primary" :underline="false" @click="$router.push('/register')">
            立即注册
          </el-link>
        </div>
        <div class="text-center" v-else>
          <span style="color: #666; font-size: 14px;">医生账号由系统管理员分配</span>
        </div>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'
import { User, Monitor } from '@element-plus/icons-vue'

const router = useRouter()
const userStore = useUserStore()

const loginFormRef = ref()
const loading = ref(false)
const rememberMe = ref(true)

const loginForm = reactive({
  username: '',
  password: '',
  userType: 0 // 默认选择患者登录
})

const loginRules = {
  userType: [
    { required: true, message: '请选择用户类型', trigger: 'change' }
  ],
  username: [
    { required: true, message: '请输入用户名或邮箱', trigger: 'blur' },
    { min: 2, max: 50, message: '用户名长度在 2 到 50 个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度在 6 到 20 个字符', trigger: 'blur' }
  ]
}

const handleLogin = async () => {
  if (loading.value) return

  try {
    const valid = await loginFormRef.value.validate()
    if (!valid) return

    loading.value = true

    const result = await userStore.login(loginForm)
    
    if (result.success) {
      // 登录成功，根据用户类型跳转到对应页面
      const userType = userStore.userInfo?.userType
      let defaultPath = '/dashboard' // 普通用户默认页面
      
      // 如果是医生用户，跳转到医生工作台
      if (userType === 1) {
        defaultPath = '/doctor/dashboard'
      }
      
      const redirect = router.currentRoute.value.query.redirect || defaultPath
      router.push(redirect)
    }
  } catch (error) {
    console.error('登录失败:', error)
  } finally {
    loading.value = false
  }
}

// 页面加载时检查是否已登录
onMounted(() => {
  if (userStore.isAuthenticated) {
    // 根据用户类型跳转到对应页面
    const defaultPath = userStore.userInfo?.userType === 1 ? '/doctor/dashboard' : '/dashboard'
    router.push(defaultPath)
  }
})
</script>

<style scoped>
.auth-container {
  position: relative;
  overflow: hidden;
}

.auth-container::before {
  content: '';
  position: absolute;
  top: -50%;
  left: -50%;
  width: 200%;
  height: 200%;
  background: radial-gradient(circle, rgba(102, 126, 234, 0.1) 0%, transparent 50%);
  animation: rotate 20s linear infinite;
}

@keyframes rotate {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.auth-card {
  position: relative;
  z-index: 1;
  transition: transform 0.3s ease, box-shadow 0.3s ease;
}

.auth-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.15);
}

:deep(.el-input__wrapper) {
  box-shadow: 0 0 0 1px #dcdfe6 inset;
  border-radius: 8px;
}

:deep(.el-input__wrapper:hover) {
  box-shadow: 0 0 0 1px #c0c4cc inset;
}

:deep(.el-input.is-focus .el-input__wrapper) {
  box-shadow: 0 0 0 2px rgba(102, 126, 234, 0.2) inset;
}

:deep(.el-button--large) {
  height: 48px;
  border-radius: 8px;
  font-size: 16px;
  font-weight: 500;
}
</style> 