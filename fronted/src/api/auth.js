import { api } from './request'

/**
 * 用户认证相关API
 */
export const authAPI = {
  // 用户登录
  login: (data) => {
    return api.post('/v1/auth/login', data)
  },

  // 用户注册
  register: (data) => {
    return api.post('/v1/auth/register', data)
  },

  // 发送邮箱验证码
  sendEmailCode: (data) => {
    return api.post('/v1/auth/send-email-code', data)
  },

  // 用户登出
  logout: () => {
    return api.post('/v1/auth/logout')
  },

  // 刷新token
  refreshToken: (refreshToken) => {
    return api.post('/v1/auth/refresh-token', { refreshToken })
  },

  // 获取当前用户信息
  getCurrentUser: () => {
    return api.get('/v1/users/current')
  },

  // 修改密码
  changePassword: (data) => {
    return api.post('/v1/auth/change-password', data)
  },

  // 重置密码
  resetPassword: (data) => {
    return api.post('/v1/auth/reset-password', data)
  },

  // 验证token有效性
  validateToken: () => {
    return api.get('/v1/auth/validate-token')
  }
}

// 导出便捷函数（保持兼容性）
export const userLogin = authAPI.login
export const doctorLogin = authAPI.login  // 医生和用户使用相同的登录接口
export const userRegister = authAPI.register

export default authAPI 