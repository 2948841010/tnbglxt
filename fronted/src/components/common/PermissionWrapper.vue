<template>
  <div v-if="hasPermission">
    <slot />
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { usePermissionStore } from '@/stores/permission'

const props = defineProps({
  // 权限标识（可以是菜单路径或权限编码）
  permission: {
    type: [String, Array],
    default: null
  },
  // 角色编码
  role: {
    type: [String, Array],
    default: null
  },
  // 权限检查模式：'all' 需要所有权限，'any' 需要任一权限
  mode: {
    type: String,
    default: 'any',
    validator: (value) => ['all', 'any'].includes(value)
  },
  // 是否取反（用于隐藏有权限的内容）
  not: {
    type: Boolean,
    default: false
  }
})

const permissionStore = usePermissionStore()

// 检查权限
const hasPermission = computed(() => {
  let result = true

  // 检查菜单/功能权限
  if (props.permission) {
    const permissions = Array.isArray(props.permission) ? props.permission : [props.permission]
    
    if (props.mode === 'all') {
      result = permissions.every(perm => permissionStore.hasMenuPermission(perm))
    } else {
      result = permissions.some(perm => permissionStore.hasMenuPermission(perm))
    }
  }

  // 检查角色权限
  if (result && props.role) {
    const roles = Array.isArray(props.role) ? props.role : [props.role]
    
    if (props.mode === 'all') {
      result = permissionStore.hasAllRoles(roles)
    } else {
      result = permissionStore.hasAnyRole(roles)
    }
  }

  // 如果设置了取反，则返回相反结果
  return props.not ? !result : result
})
</script>

<script>
export default {
  name: 'PermissionWrapper'
}
</script> 