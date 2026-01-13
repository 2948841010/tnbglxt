<template>
  <div class="auth-container">
    <div class="auth-card" style="max-width: 450px">
      <div class="auth-title">创建账户</div>
      <div class="auth-subtitle">加入我们的健康管理平台</div>

      <el-form
        ref="registerFormRef"
        :model="registerForm"
        :rules="registerRules"
        @submit.prevent="handleRegister"
      >
        <el-form-item prop="username">
          <el-input
            v-model="registerForm.username"
            size="large"
            placeholder="请输入用户名"
            prefix-icon="User"
            clearable
            :disabled="loading"
          />
        </el-form-item>

        <el-form-item prop="realName">
          <el-input
            v-model="registerForm.realName"
            size="large"
            placeholder="请输入真实姓名"
            prefix-icon="UserFilled"
            clearable
            :disabled="loading"
          />
        </el-form-item>

        <el-form-item prop="email">
          <el-input
            v-model="registerForm.email"
            size="large"
            placeholder="请输入邮箱地址"
            prefix-icon="Message"
            clearable
            :disabled="loading"
          />
        </el-form-item>

        <el-form-item prop="emailCode">
          <div style="display: flex; gap: 8px; width: 100%">
            <el-input
              v-model="registerForm.emailCode"
              size="large"
              placeholder="请输入邮箱验证码"
              prefix-icon="Key"
              clearable
              :disabled="loading"
              style="flex: 1"
            />
            <el-button
              size="large"
              :disabled="!canSendCode || sendingCode"
              :loading="sendingCode"
              @click="sendEmailCode"
            >
              {{ codeButtonText }}
            </el-button>
          </div>
        </el-form-item>

        <el-form-item prop="phone">
          <el-input
            v-model="registerForm.phone"
            size="large"
            placeholder="请输入手机号码（可选）"
            prefix-icon="Phone"
            clearable
            :disabled="loading"
          />
        </el-form-item>

        <el-form-item prop="password">
          <el-input
            v-model="registerForm.password"
            type="password"
            size="large"
            placeholder="请输入密码"
            prefix-icon="Lock"
            show-password
            clearable
            :disabled="loading"
          />
        </el-form-item>

        <el-form-item prop="confirmPassword">
          <el-input
            v-model="registerForm.confirmPassword"
            type="password"
            size="large"
            placeholder="请确认密码"
            prefix-icon="Lock"
            show-password
            clearable
            :disabled="loading"
            @keyup.enter="handleRegister"
          />
        </el-form-item>

        <el-form-item>
          <el-checkbox v-model="agreeTerms" :disabled="loading">
            我已阅读并同意
            <el-link type="primary" :underline="false" @click="showTerms">
              用户协议
            </el-link>
            和
            <el-link type="primary" :underline="false" @click="showPrivacy">
              隐私政策
            </el-link>
          </el-checkbox>
        </el-form-item>

        <el-button
          type="primary"
          size="large"
          style="width: 100%; margin-bottom: 16px"
          :loading="loading"
          :disabled="!agreeTerms"
          @click="handleRegister"
        >
          {{ loading ? '注册中...' : '注册账户' }}
        </el-button>

        <div class="text-center">
          <span style="color: #666">已有账户？</span>
          <el-link type="primary" :underline="false" @click="$router.push('/login')">
            立即登录
          </el-link>
        </div>
      </el-form>
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

const registerForm = reactive({
  username: '',
  realName: '',
  email: '',
  emailCode: '',
  phone: '',
  password: '',
  confirmPassword: ''
})

// 验证邮箱格式
const validateEmail = (rule, value, callback) => {
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
  if (!value) {
    callback(new Error('请输入邮箱地址'))
  } else if (!emailRegex.test(value)) {
    callback(new Error('请输入正确的邮箱格式'))
  } else {
    callback()
  }
}

// 验证密码确认
const validatePasswordConfirm = (rule, value, callback) => {
  if (!value) {
    callback(new Error('请确认密码'))
  } else if (value !== registerForm.password) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
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

const registerRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度在 3 到 20 个字符', trigger: 'blur' },
    { pattern: /^[a-zA-Z0-9_]+$/, message: '用户名只能包含字母、数字和下划线', trigger: 'blur' }
  ],
  realName: [
    { required: true, message: '请输入真实姓名', trigger: 'blur' },
    { min: 2, max: 10, message: '姓名长度在 2 到 10 个字符', trigger: 'blur' }
  ],
  email: [
    { required: true, validator: validateEmail, trigger: 'blur' }
  ],
  emailCode: [
    { required: true, message: '请输入邮箱验证码', trigger: 'blur' },
    { len: 6, message: '验证码应为6位数字', trigger: 'blur' }
  ],
  phone: [
    { validator: validatePhone, trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度在 6 到 20 个字符', trigger: 'blur' },
    { pattern: /^(?=.*[a-zA-Z])(?=.*\d)[a-zA-Z\d@$!%*?&]+$/, message: '密码必须包含字母和数字', trigger: 'blur' }
  ],
  confirmPassword: [
    { validator: validatePasswordConfirm, trigger: 'blur' }
  ]
}

// 是否可以发送验证码
const canSendCode = computed(() => {
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
  return emailRegex.test(registerForm.email) && countdown.value === 0
})

// 发送验证码按钮文本
const codeButtonText = computed(() => {
  if (sendingCode.value) return '发送中'
  if (countdown.value > 0) return `${countdown.value}s后重发`
  return '获取验证码'
})

// 发送邮箱验证码
const sendEmailCode = async () => {
  if (!canSendCode.value) return

  try {
    sendingCode.value = true
    const result = await userStore.sendEmailCode(registerForm.email, 'register')
    
    if (result.success) {
      // 开始倒计时
      countdown.value = 60
      countdownTimer = setInterval(() => {
        countdown.value--
        if (countdown.value <= 0) {
          clearInterval(countdownTimer)
        }
      }, 1000)
    }
  } catch (error) {
    console.error('发送验证码失败:', error)
  } finally {
    sendingCode.value = false
  }
}

// 处理注册
const handleRegister = async () => {
  if (loading.value || !agreeTerms.value) return

  try {
    const valid = await registerFormRef.value.validate()
    if (!valid) return

    loading.value = true

    const result = await userStore.register(registerForm)
    
    if (result.success) {
      ElMessage.success('注册成功！请登录您的账户')
      router.push('/login')
    }
  } catch (error) {
    console.error('注册失败:', error)
  } finally {
    loading.value = false
  }
}

// 显示用户协议
const showTerms = () => {
  ElMessageBox.alert(
    '这里是用户协议内容...',
    '用户协议',
    { confirmButtonText: '我知道了' }
  )
}

// 显示隐私政策
const showPrivacy = () => {
  ElMessageBox.alert(
    '这里是隐私政策内容...',
    '隐私政策',
    { confirmButtonText: '我知道了' }
  )
}

// 清理定时器
onBeforeUnmount(() => {
  if (countdownTimer) {
    clearInterval(countdownTimer)
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