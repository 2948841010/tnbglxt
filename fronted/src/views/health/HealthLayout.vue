<template>
  <div class="health-layout">
    <!-- 侧边栏 -->
    <div class="health-sidebar" :class="{ collapsed: sidebarCollapsed }">
      <div class="sidebar-header">
        <div class="logo" v-show="!sidebarCollapsed">
          <h2>糖尿病管理</h2>
        </div>
        <div class="logo-mini" v-show="sidebarCollapsed">
          <h3>TL</h3>
        </div>
      </div>
      
      <el-menu
        :default-active="activeMenu"
        :collapse="sidebarCollapsed"
        background-color="#304156"
        text-color="#bfcbd9"
        active-text-color="#409EFF"
        router
        @select="handleMenuSelect"
      >
        <el-menu-item index="/dashboard">
          <el-icon><Odometer /></el-icon>
          <template #title>仪表板</template>
        </el-menu-item>
        
        <el-menu-item index="/profile">
          <el-icon><User /></el-icon>
          <template #title>个人资料</template>
        </el-menu-item>
        
        <el-sub-menu index="health">
          <template #title>
            <el-icon><DataAnalysis /></el-icon>
            <span>健康数据</span>
          </template>
          <el-menu-item index="/health/overview">
                         <el-icon><PieChart /></el-icon>
            <template #title>健康概览</template>
          </el-menu-item>
          <el-menu-item index="/health/glucose">
            <el-icon><Odometer /></el-icon>
            <template #title>血糖记录</template>
          </el-menu-item>
          <el-menu-item index="/health/pressure">
                         <el-icon><Monitor /></el-icon>
            <template #title>血压记录</template>
          </el-menu-item>
          <el-menu-item index="/health/weight">
            <el-icon><Grid /></el-icon>
            <template #title>体重记录</template>
          </el-menu-item>
          <el-menu-item index="/health/statistics">
            <el-icon><TrendCharts /></el-icon>
            <template #title>健康统计</template>
          </el-menu-item>
        </el-sub-menu>
        
        <el-menu-item index="/settings">
          <el-icon><Setting /></el-icon>
          <template #title>系统设置</template>
        </el-menu-item>
      </el-menu>
    </div>

    <!-- 主内容区域 -->
    <div class="health-main">
      <!-- 头部 -->
      <div class="health-header">
        <div class="header-left">
          <el-button
            text
            @click="toggleSidebar"
            style="margin-right: 16px"
          >
            <el-icon><Expand v-if="sidebarCollapsed" /><Fold v-else /></el-icon>
          </el-button>
          <el-breadcrumb separator="/">
            <el-breadcrumb-item :to="{ path: '/dashboard' }">首页</el-breadcrumb-item>
            <el-breadcrumb-item>健康数据</el-breadcrumb-item>
            <el-breadcrumb-item v-if="currentPageTitle">
              {{ currentPageTitle }}
            </el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        
        <div class="header-right">
          <el-dropdown @command="handleUserCommand">
            <div class="user-info">
              <el-avatar :src="userStore.userAvatar" :size="32" />
              <span class="username">{{ userStore.userName }}</span>
              <el-icon><CaretBottom /></el-icon>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">个人资料</el-dropdown-item>
                <el-dropdown-item command="settings">系统设置</el-dropdown-item>
                <el-dropdown-item divided command="logout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </div>

      <!-- 内容区域 -->
      <div class="health-content">
        <router-view />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'
import {
  Odometer,
  User,
  DataAnalysis,
  PieChart,
  Monitor,
  Grid,
  TrendCharts,
  Setting,
  Expand,
  Fold,
  CaretBottom
} from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const sidebarCollapsed = ref(false)
const activeMenu = ref('')

// 当前页面标题
const currentPageTitle = computed(() => {
  const titleMap = {
    '/health/overview': '健康概览',
    '/health/glucose': '血糖记录',
    '/health/pressure': '血压记录',
    '/health/weight': '体重记录',
    '/health/statistics': '健康统计'
  }
  return titleMap[route.path] || ''
})

// 切换侧边栏
const toggleSidebar = () => {
  sidebarCollapsed.value = !sidebarCollapsed.value
}

// 菜单选择处理
const handleMenuSelect = (index) => {
  activeMenu.value = index
}

// 用户菜单命令处理
const handleUserCommand = (command) => {
  switch (command) {
    case 'profile':
      router.push('/profile')
      break
    case 'settings':
      router.push('/settings')
      break
    case 'logout':
      userStore.logout()
      break
  }
}

// 监听路由变化
watch(() => route.path, (path) => {
  activeMenu.value = path
})

onMounted(() => {
  activeMenu.value = route.path
})
</script>

<style scoped>
.health-layout {
  display: flex;
  height: 100vh;
  overflow: hidden;
}

.health-sidebar {
  width: 250px;
  background-color: #304156;
  transition: width 0.3s ease;
  overflow: hidden;
  border-right: 1px solid #e4e7ed;
}

.health-sidebar.collapsed {
  width: 64px;
}

.sidebar-header {
  padding: 20px;
  text-align: center;
  border-bottom: 1px solid #434a50;
}

.logo h2 {
  color: white;
  margin: 0;
  font-size: 18px;
  font-weight: 600;
}

.logo-mini h3 {
  color: white;
  margin: 0;
  font-size: 16px;
  font-weight: 600;
}

.health-main {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.health-header {
  height: 60px;
  background: white;
  border-bottom: 1px solid #e4e7ed;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
  box-shadow: 0 1px 4px rgba(0,21,41,.08);
}

.header-left {
  display: flex;
  align-items: center;
}

.header-right {
  display: flex;
  align-items: center;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  padding: 8px 12px;
  border-radius: 6px;
  transition: background-color 0.3s;
}

.user-info:hover {
  background-color: #f5f7fa;
}

.username {
  font-weight: 500;
  color: #333;
}

.health-content {
  flex: 1;
  padding: 20px;
  overflow-y: auto;
  background-color: #f0f2f5;
}

:deep(.el-menu) {
  border-right: none;
}

:deep(.el-sub-menu__title) {
  color: #bfcbd9 !important;
}

:deep(.el-sub-menu.is-active .el-sub-menu__title) {
  color: #409EFF !important;
}

@media (max-width: 768px) {
  .health-layout {
    flex-direction: column;
  }
  
  .health-sidebar {
    width: 100% !important;
    height: auto;
  }
  
  .health-sidebar.collapsed {
    height: 60px;
    overflow: hidden;
  }
  
  .health-content {
    padding: 16px;
  }
}
</style> 