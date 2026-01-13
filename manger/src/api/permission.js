import { api } from './request'

/**
 * 权限管理相关API
 */
export const permissionAPI = {
  // 获取用户权限信息
  getUserPermissionInfo: (userId) => {
    return api.get(`/v1/admin/permissions/user/${userId}`)
  },

  // 获取所有菜单
  getAllMenus: () => {
    return api.get('/v1/admin/permissions/menus')
  },

  // 获取角色列表
  getRoles: (params = {}) => {
    return api.get('/v1/admin/permissions/roles', params)
  },

  // 获取权限列表
  getPermissions: (params = {}) => {
    return api.get('/v1/admin/permissions', params)
  },

  // 创建角色
  createRole: (data) => {
    return api.post('/v1/admin/permissions/roles', data)
  },

  // 更新角色
  updateRole: (id, data) => {
    return api.put(`/v1/admin/permissions/roles/${id}`, data)
  },

  // 删除角色
  deleteRole: (id) => {
    return api.delete(`/v1/admin/permissions/roles/${id}`)
  },

  // 分配角色权限
  assignRolePermissions: (roleId, permissions) => {
    return api.post(`/v1/admin/permissions/roles/${roleId}/permissions`, permissions)
  },

  // 分配用户角色
  assignUserRoles: (userId, roles) => {
    return api.post(`/v1/admin/users/${userId}/roles`, { roles })
  }
}

export default permissionAPI 