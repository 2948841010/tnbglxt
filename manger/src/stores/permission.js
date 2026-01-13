import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { permissionAPI } from '@/api/permission'
import { useUserStore } from '@/stores/user'

export const usePermissionStore = defineStore('permission', () => {
  // 状态
  const menus = ref([])
  const roles = ref([])
  const permissions = ref([])
  const menuMap = ref(new Map())
  const permissionLoaded = ref(false)

  const userStore = useUserStore()

  // 计算属性
  const hasPermissions = computed(() => {
    return permissions.value.length > 0
  })

  const userRoles = computed(() => {
    return roles.value.map(role => role.roleCode)
  })

  // 获取菜单权限
  const getMenuPermissions = async (userId) => {
    try {
      if (!userId) {
        userId = userStore.userInfo?.id
      }
      
      if (!userId) {
        throw new Error('用户ID不存在')
      }

      const response = await permissionAPI.getUserPermissionInfo(userId)
      
      if (response.code === 200 && response.data) {
        const { menus: userMenus, roles: userRoles } = response.data
        
        // 保存菜单和角色信息
        menus.value = userMenus || []
        roles.value = userRoles || []
        
        // 构建菜单映射表，用于快速查找
        buildMenuMap(menus.value)
        
        // 提取权限标识
        extractPermissions(menus.value)
        
        permissionLoaded.value = true
        
        return { success: true, data: response.data }
      } else {
        throw new Error(response.message || '获取权限信息失败')
      }
    } catch (error) {
      console.error('获取菜单权限失败:', error)
      return { success: false, error: error.message }
    }
  }

  // 构建菜单映射表
  const buildMenuMap = (menuList) => {
    const map = new Map()
    
    const traverse = (menus) => {
      menus.forEach(menu => {
        if (menu.path) {
          map.set(menu.path, menu)
        }
        if (menu.permission) {
          map.set(menu.permission, menu)
        }
        map.set(menu.id, menu)
        
        if (menu.children && menu.children.length > 0) {
          traverse(menu.children)
        }
      })
    }
    
    traverse(menuList)
    menuMap.value = map
  }

  // 提取权限标识
  const extractPermissions = (menuList) => {
    const perms = []
    
    const traverse = (menus) => {
      menus.forEach(menu => {
        if (menu.permission) {
          perms.push(menu.permission)
        }
        if (menu.path) {
          perms.push(menu.path)
        }
        
        if (menu.children && menu.children.length > 0) {
          traverse(menu.children)
        }
      })
    }
    
    traverse(menuList)
    permissions.value = [...new Set(perms)]
  }

  // 检查是否有菜单权限
  const hasMenuPermission = (menuPath) => {
    if (!menuPath) return false
    
    // 管理员拥有所有权限
    if (userStore.userInfo?.userType === 2) {
      return true
    }
    
    // 检查是否在权限列表中
    return permissions.value.includes(menuPath) || menuMap.value.has(menuPath)
  }

  // 检查是否有角色
  const hasRole = (roleCode) => {
    if (!roleCode) return false
    
    // 管理员默认拥有ADMIN角色
    if (userStore.userInfo?.userType === 2 && roleCode === 'ADMIN') {
      return true
    }
    
    return userRoles.value.includes(roleCode)
  }

  // 检查是否有任一角色
  const hasAnyRole = (roleCodes) => {
    if (!roleCodes || !Array.isArray(roleCodes)) return false
    return roleCodes.some(roleCode => hasRole(roleCode))
  }

  // 检查是否有所有角色
  const hasAllRoles = (roleCodes) => {
    if (!roleCodes || !Array.isArray(roleCodes)) return false
    return roleCodes.every(roleCode => hasRole(roleCode))
  }

  // 过滤菜单（根据权限）
  const filterMenus = (menuList) => {
    if (!menuList || !Array.isArray(menuList)) return []
    
    // 管理员显示所有菜单
    if (userStore.userInfo?.userType === 2) {
      return menuList.filter(menu => menu.menuType !== 3)
    }
    
    return menuList.filter(menu => {
      // 只显示菜单类型为1（目录）或2（页面）的菜单项，不显示类型3（功能权限）
      if (menu.menuType === 3) {
        return false
      }
      
      // 检查当前菜单权限
      const hasCurrentPermission = !menu.permission || hasMenuPermission(menu.permission)
      const hasCurrentPath = !menu.path || hasMenuPermission(menu.path)
      
      return hasCurrentPermission || hasCurrentPath
    }).map(menu => {
      // 创建菜单副本，避免修改原始数据
      const filteredMenu = { ...menu }
      
      // 递归过滤子菜单
      if (menu.children && menu.children.length > 0) {
        filteredMenu.children = filterMenus(menu.children)
      }
      
      return filteredMenu
    })
  }

  // 获取有权限的菜单（使用计算属性缓存结果）
  const accessibleMenus = computed(() => {
    if (!permissionLoaded.value || !menus.value || menus.value.length === 0) {
      return []
    }
    return filterMenus(menus.value)
  })

  // 获取有权限的菜单
  const getAccessibleMenus = () => {
    return accessibleMenus.value
  }

  // 根据路径查找菜单
  const findMenuByPath = (path) => {
    return menuMap.value.get(path)
  }

  // 根据权限标识查找菜单
  const findMenuByPermission = (permission) => {
    return menuMap.value.get(permission)
  }

  // 清除权限信息
  const clearPermissions = () => {
    menus.value = []
    roles.value = []
    permissions.value = []
    menuMap.value = new Map()
    permissionLoaded.value = false
  }

  // 刷新权限信息
  const refreshPermissions = async () => {
    clearPermissions()
    return await getMenuPermissions()
  }

  return {
    // 状态
    menus,
    roles,
    permissions,
    permissionLoaded,

    // 计算属性
    hasPermissions,
    userRoles,
    accessibleMenus,

    // 方法
    getMenuPermissions,
    hasMenuPermission,
    hasRole,
    hasAnyRole,
    hasAllRoles,
    filterMenus,
    getAccessibleMenus,
    findMenuByPath,
    findMenuByPermission,
    clearPermissions,
    refreshPermissions
  }
}) 