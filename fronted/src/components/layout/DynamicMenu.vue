<template>
  <el-menu
    :default-active="activeMenu"
    :collapse="collapsed"
    background-color="#304156"
    text-color="#bfcbd9"
    active-text-color="#409EFF"
    router
    mode="vertical"
  >
    <template v-for="menu in accessibleMenus" :key="menu.id">
      <!-- 如果有子菜单，显示为子菜单 -->
      <el-sub-menu v-if="menu.children && menu.children.length > 0" :index="getMenuIndex(menu)">
        <template #title>
          <el-icon v-if="menu.icon">
            <component :is="getIconComponent(menu.icon)" />
          </el-icon>
          <span>{{ menu.menuName }}</span>
        </template>
        <template v-for="subMenu in menu.children" :key="subMenu.id">
          <!-- 递归渲染子菜单 -->
          <dynamic-menu-item :menu="subMenu" :level="1" />
        </template>
      </el-sub-menu>
      
      <!-- 普通菜单项 -->
      <el-menu-item v-else :index="getMenuIndex(menu)">
        <el-icon v-if="menu.icon">
          <component :is="getIconComponent(menu.icon)" />
        </el-icon>
        <template #title>{{ menu.menuName }}</template>
      </el-menu-item>
    </template>
  </el-menu>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import { usePermissionStore } from '@/stores/permission'
import { useUserStore } from '@/stores/user'
import DynamicMenuItem from './DynamicMenuItem.vue'
import * as ElementPlusIcons from '@element-plus/icons-vue'

const props = defineProps({
  collapsed: {
    type: Boolean,
    default: false
  }
})

const route = useRoute()
const permissionStore = usePermissionStore()
const userStore = useUserStore()

// 当前激活的菜单
const activeMenu = computed(() => {
  return route.path
})

// 有权限的菜单
const accessibleMenus = computed(() => {
  if (!permissionStore.permissionLoaded || !userStore.isAuthenticated) {
    return []
  }
  return permissionStore.accessibleMenus || []
})

// 获取菜单索引（用于路由）
const getMenuIndex = (menu) => {
  return menu.path || menu.id.toString()
}

// 获取图标组件
const getIconComponent = (iconName) => {
  // 如果是Element Plus图标
  if (ElementPlusIcons[iconName]) {
    return ElementPlusIcons[iconName]
  }
  
  // 默认图标映射
  const iconMap = {
    'dashboard': 'Odometer',
    'user': 'User',
    'health': 'DataAnalysis',
    'overview': 'PieChart',
    'glucose': 'Odometer',
    'pressure': 'Monitor',
    'weight': 'Grid',
    'statistics': 'TrendCharts',
    'settings': 'Setting'
  }
  
  const mappedIcon = iconMap[iconName.toLowerCase()]
  return ElementPlusIcons[mappedIcon] || ElementPlusIcons.Menu
}

// 权限初始化逻辑移到userStore中统一处理，这里不再重复处理
// 避免多处同时触发权限获取导致的竞态条件
</script>

<style scoped>
/* 菜单样式调整 */
:deep(.el-menu) {
  border-right: none;
  flex: 1;
}

:deep(.el-menu-item) {
  height: 48px;
  line-height: 48px;
}

:deep(.el-sub-menu .el-menu-item) {
  height: 44px;
  line-height: 44px;
  padding-left: 60px !important;
}
</style> 