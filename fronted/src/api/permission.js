import request from '@/api/request'

/**
 * 权限相关API
 */
export const permissionAPI = {
  /**
   * 获取用户菜单权限
   */
  getUserMenus(userId) {
    return request({
      url: '/user/permission/menus',
      method: 'get',
      params: { userId }
    })
  },

  /**
   * 获取用户角色
   */
  getUserRoles(userId) {
    return request({
      url: '/user/permission/roles',
      method: 'get',
      params: { userId }
    })
  },

  /**
   * 获取用户权限信息（菜单 + 角色）
   */
  getUserPermissionInfo(userId) {
    return request({
      url: '/user/permission/info',
      method: 'get',
      params: { userId }
    })
  },

  /**
   * 检查用户是否有指定菜单权限
   */
  checkMenuPermission(userId, menuPath) {
    return request({
      url: '/user/permission/check-menu',
      method: 'get',
      params: { userId, menuPath }
    })
  },

  /**
   * 检查用户是否有指定角色
   */
  checkRole(userId, roleCode) {
    return request({
      url: '/user/permission/check-role',
      method: 'get',
      params: { userId, roleCode }
    })
  }
} 