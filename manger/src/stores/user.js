import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import Cookies from 'js-cookie'
import { authAPI } from '@/api/auth'
import { ElMessage } from 'element-plus'
import router from '@/router'

export const useUserStore = defineStore('user', () => {
  // 状态
  const token = ref('')
  const refreshToken = ref('')
  const userInfo = ref(null)
  const permissions = ref([])
  const loginTime = ref(null)

  // 计算属性
  const isAuthenticated = computed(() => {
    return !!(token.value && userInfo.value && userInfo.value.userType === 2)
  })

  const userName = computed(() => {
    return userInfo.value?.realName || userInfo.value?.username || ''
  })

  const userAvatar = computed(() => {
    return userInfo.value?.avatar || '/default-avatar.png'
  })

  const isTokenExpired = computed(() => {
    if (!token.value || !loginTime.value) return true
    
    // 简单的过期检查（24小时）
    const now = Date.now()
    const expire = loginTime.value + (24 * 60 * 60 * 1000)
    return now > expire
  })

  // 动作
  const login = async (loginForm) => {
    try {
      // 为管理员登录添加用户类型
      const adminLoginForm = {
        ...loginForm,
        userType: 2  // 管理员用户类型
      }
      const response = await authAPI.login(adminLoginForm)
      
      if ((response.success || response.code === 200) && response.data) {
        const { accessToken, refreshToken: newRefreshToken, userInfo: userData, token } = response.data
        
        // 检查是否为管理员用户
        if (userData.userType !== 2) {
          throw new Error('您不是管理员，无法访问管理端')
        }
        
        // 保存认证信息（兼容不同的token字段名）
        const actualToken = accessToken || token
        setAuth(actualToken, newRefreshToken, userData)
        
        ElMessage.success('登录成功')
        return { success: true, data: response.data }
      } else {
        throw new Error(response.message || '登录失败')
      }
    } catch (error) {
      console.error('登录失败:', error)
      ElMessage.error(error.message || '登录失败')
      return { success: false, error: error.message }
    }
  }

  const logout = async (showMessage = true) => {
    try {
      // 调用后端登出接口（可选）
      if (token.value) {
        await authAPI.logout().catch(() => {
          // 忽略登出接口错误
        })
      }
    } finally {
      // 清除本地认证信息
      clearAuth()
      
      if (showMessage) {
        ElMessage.success('已退出登录')
      }
      
      // 跳转到登录页面
      if (router.currentRoute.value.path !== '/login') {
        router.push('/login')
      }
    }
  }

  const getCurrentUser = async () => {
    try {
      if (!token.value) {
        throw new Error('未登录')
      }

      const response = await authAPI.getCurrentUser()
      
      if (response.success && response.data) {
        // 再次检查用户类型
        if (response.data.userType !== 2) {
          throw new Error('您不是管理员用户')
        }
        
        userInfo.value = response.data
        saveToStorage()
        return { success: true, data: response.data }
      } else {
        throw new Error(response.message || '获取用户信息失败')
      }
    } catch (error) {
      console.error('获取用户信息失败:', error)
      if (error.message.includes('401') || error.message.includes('Token')) {
        await logout(false)
      }
      return { success: false, error: error.message }
    }
  }

  const refreshAuthToken = async () => {
    try {
      if (!refreshToken.value) {
        throw new Error('无刷新令牌')
      }

      const response = await authAPI.refreshToken(refreshToken.value)
      
      if (response.success && response.data) {
        const { accessToken, refreshToken: newRefreshToken } = response.data
        token.value = accessToken
        refreshToken.value = newRefreshToken
        loginTime.value = Date.now()
        saveToStorage()
        return { success: true }
      } else {
        throw new Error(response.message || '刷新令牌失败')
      }
    } catch (error) {
      console.error('刷新令牌失败:', error)
      await logout(false)
      return { success: false, error: error.message }
    }
  }

  const setAuth = (accessToken, newRefreshToken, userData) => {
    token.value = accessToken
    refreshToken.value = newRefreshToken
    userInfo.value = userData
    loginTime.value = Date.now()
    saveToStorage()
  }

  const clearAuth = () => {
    token.value = ''
    refreshToken.value = ''
    userInfo.value = null
    permissions.value = []
    loginTime.value = null
    clearStorage()
  }

  const saveToStorage = () => {
    try {
      if (token.value) {
        Cookies.set('admin_access_token', token.value, { expires: 1 })
        Cookies.set('admin_refresh_token', refreshToken.value, { expires: 7 })
        localStorage.setItem('admin_user_info', JSON.stringify(userInfo.value))
        localStorage.setItem('admin_login_time', loginTime.value?.toString())
      }
    } catch (error) {
      console.error('保存到本地存储失败:', error)
    }
  }

  const clearStorage = () => {
    try {
      Cookies.remove('admin_access_token')
      Cookies.remove('admin_refresh_token')
      localStorage.removeItem('admin_user_info')
      localStorage.removeItem('admin_login_time')
    } catch (error) {
      console.error('清除本地存储失败:', error)
    }
  }

  const initAuth = async () => {
    try {
      const accessToken = Cookies.get('admin_access_token')
      const storedRefreshToken = Cookies.get('admin_refresh_token')
      const storedUserInfo = localStorage.getItem('admin_user_info')
      const storedLoginTime = localStorage.getItem('admin_login_time')

      if (accessToken && storedUserInfo) {
        const userData = JSON.parse(storedUserInfo)
        
        // 检查是否为管理员用户
        if (userData.userType !== 2) {
          clearAuth()
          return
        }
        
        token.value = accessToken
        refreshToken.value = storedRefreshToken || ''
        userInfo.value = userData
        loginTime.value = storedLoginTime ? parseInt(storedLoginTime) : Date.now()

        // 验证token是否仍然有效
        if (isTokenExpired.value) {
          if (refreshToken.value) {
            await refreshAuthToken()
          } else {
            clearAuth()
          }
        } else {
          // 获取最新用户信息
          await getCurrentUser()
          
          // 初始化权限信息
          try {
            const permissionStore = await import('@/stores/permission').then(m => m.usePermissionStore())
            if (!permissionStore.permissionLoaded && userInfo.value?.id) {
              await permissionStore.getMenuPermissions(userInfo.value.id)
            }
          } catch (error) {
            console.error('权限初始化失败:', error)
          }
        }
      }
    } catch (error) {
      console.error('初始化认证失败:', error)
      clearAuth()
    }
  }

  // 更新用户头像
  const updateAvatar = (avatarUrl) => {
    if (userInfo.value) {
      userInfo.value.avatar = avatarUrl
      // 强制触发响应式更新
      userInfo.value = { ...userInfo.value }
    }
  }

  return {
    // 状态
    token,
    refreshToken,
    userInfo,
    permissions,
    loginTime,

    // 计算属性
    isAuthenticated,
    userName,
    userAvatar,
    isTokenExpired,

    // 动作
    login,
    logout,
    getCurrentUser,
    refreshAuthToken,
    setAuth,
    clearAuth,
    initAuth,
    updateAvatar
  }
}) 