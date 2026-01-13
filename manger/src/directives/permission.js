import { usePermissionStore } from '@/stores/permission'

/**
 * 权限指令
 * 用法：
 * v-permission="'menu:view'" - 单个权限
 * v-permission="['menu:view', 'menu:edit']" - 多个权限（任一满足）
 * v-permission.all="['menu:view', 'menu:edit']" - 多个权限（全部满足）
 * v-role="'ADMIN'" - 角色权限
 * v-role="['ADMIN', 'USER']" - 多个角色（任一满足）
 * v-role.all="['ADMIN', 'USER']" - 多个角色（全部满足）
 */

const permission = {
  mounted(el, binding) {
    checkPermission(el, binding)
  },
  updated(el, binding) {
    checkPermission(el, binding)
  }
}

const role = {
  mounted(el, binding) {
    checkRole(el, binding)
  },
  updated(el, binding) {
    checkRole(el, binding)
  }
}

function checkPermission(el, binding) {
  const { value, modifiers } = binding
  const permissionStore = usePermissionStore()

  if (!value) {
    return
  }

  const permissions = Array.isArray(value) ? value : [value]
  let hasPermission = false

  if (modifiers.all) {
    // 需要所有权限
    hasPermission = permissions.every(permission => 
      permissionStore.hasMenuPermission(permission)
    )
  } else {
    // 需要任一权限
    hasPermission = permissions.some(permission => 
      permissionStore.hasMenuPermission(permission)
    )
  }

  if (!hasPermission) {
    el.style.display = 'none'
    el.setAttribute('data-permission-hidden', 'true')
  } else {
    if (el.getAttribute('data-permission-hidden')) {
      el.style.display = ''
      el.removeAttribute('data-permission-hidden')
    }
  }
}

function checkRole(el, binding) {
  const { value, modifiers } = binding
  const permissionStore = usePermissionStore()

  if (!value) {
    return
  }

  const roles = Array.isArray(value) ? value : [value]
  let hasRole = false

  if (modifiers.all) {
    // 需要所有角色
    hasRole = permissionStore.hasAllRoles(roles)
  } else {
    // 需要任一角色
    hasRole = permissionStore.hasAnyRole(roles)
  }

  if (!hasRole) {
    el.style.display = 'none'
    el.setAttribute('data-role-hidden', 'true')
  } else {
    if (el.getAttribute('data-role-hidden')) {
      el.style.display = ''
      el.removeAttribute('data-role-hidden')
    }
  }
}

export { permission, role } 