import { api } from './request'

/**
 * 管理员认证相关API
 */
export const authAPI = {
  // 管理员登录
  login: (data) => {
    return api.post('/v1/admin/auth/login', data)
  },

  // 管理员登出
  logout: () => {
    return api.post('/v1/admin/auth/logout')
  },

  // 刷新token
  refreshToken: (refreshToken) => {
    return api.post('/v1/admin/auth/refresh-token', { refreshToken })
  },

  // 获取当前管理员信息
  getCurrentUser: () => {
    return api.get('/v1/admin/auth/current')
  },

  // 修改密码
  changePassword: (data) => {
    return api.post('/v1/admin/auth/change-password', data)
  },

  // 验证token有效性
  validateToken: () => {
    return api.get('/v1/admin/auth/validate')
  }
}

export default authAPI 