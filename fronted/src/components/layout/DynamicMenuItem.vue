<template>
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
      <dynamic-menu-item :menu="subMenu" :level="level + 1" />
    </template>
  </el-sub-menu>
  
  <!-- 普通菜单项 -->
  <el-menu-item v-else :index="getMenuIndex(menu)" :style="getMenuItemStyle()">
    <el-icon v-if="menu.icon">
      <component :is="getIconComponent(menu.icon)" />
    </el-icon>
    <template #title>{{ menu.menuName }}</template>
  </el-menu-item>
</template>

<script setup>
import { computed } from 'vue'
import * as ElementPlusIcons from '@element-plus/icons-vue'

const props = defineProps({
  menu: {
    type: Object,
    required: true
  },
  level: {
    type: Number,
    default: 0
  }
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

// 获取菜单项样式
const getMenuItemStyle = () => {
  return {
    paddingLeft: `${20 + props.level * 20}px`
  }
}
</script> 