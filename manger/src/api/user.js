import { api } from './request'

/**
 * 用户管理相关API
 */
export const userAPI = {
  
  /**
   * 分页查询用户列表
   * @param {Object} params 查询参数
   */
  getUserList: (params = {}) => {
    return api.get('/v1/admin/users/list', params)
  },

  /**
   * 获取用户详情
   * @param {number} userId 用户ID
   */
  getUserDetail: (userId) => {
    return api.get(`/v1/admin/users/${userId}`)
  },

  /**
   * 更新用户信息
   * @param {number} userId 用户ID
   * @param {Object} userData 用户数据
   */
  updateUser: (userId, userData) => {
    return api.put(`/v1/admin/users/${userId}`, userData)
  },

  /**
   * 更新用户状态
   * @param {number} userId 用户ID
   * @param {number} status 状态 (0-禁用, 1-启用)
   */
  updateUserStatus: (userId, status) => {
    return api.put(`/v1/admin/users/${userId}/status`, null, {
      params: { status }
    })
  },

  /**
   * 删除用户
   * @param {number} userId 用户ID
   */
  deleteUser: (userId) => {
    return api.delete(`/v1/admin/users/${userId}`)
  },

  /**
   * 批量删除用户
   * @param {Array} userIds 用户ID数组
   */
  batchDeleteUsers: (userIds) => {
    return api.delete('/v1/admin/users/batch', userIds)
  }
}

export default userAPI 