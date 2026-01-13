import { api } from './request'

/**
 * 用户管理相关API
 */
export const userAPI = {
  // 获取用户信息
  getUserById: (id) => {
    return api.get(`/v1/users/${id}`)
  },

  // 更新用户信息
  updateUser: (id, data) => {
    return api.put(`/v1/users/${id}`, data)
  },

  // 更新个人资料
  updateProfile: (data) => {
    return api.put('/v1/users/profile', data)
  },

  // 上传头像
  uploadAvatar: (formData) => {
    return api.upload('/v1/users/avatar', formData)
  },

  // 获取用户列表（管理员）
  getUserList: (params) => {
    return api.get('/v1/admin/users', params)
  },

  // 禁用/启用用户（管理员）
  toggleUserStatus: (id, status) => {
    return api.put(`/v1/admin/users/${id}/status`, { status })
  },

  // 删除用户（管理员）
  deleteUser: (id) => {
    return api.delete(`/v1/admin/users/${id}`)
  }
}

export default userAPI 