<template>
  <div class="main-layout">
    <!-- 侧边栏 -->
    <div class="main-sidebar" :class="{ collapsed: sidebarCollapsed }">
      <div class="sidebar-header">
        <div class="logo" v-show="!sidebarCollapsed">
          <h2>糖尿病管理</h2>
        </div>
        <div class="logo-mini" v-show="sidebarCollapsed">
          <h3>TL</h3>
        </div>
      </div>
      
      <!-- 动态菜单组件 -->
      <dynamic-menu 
        :collapsed="sidebarCollapsed" 
      />
    </div>

    <!-- 主内容区域 -->
    <div class="main-content">
      <!-- 头部 -->
      <div class="main-header">
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
            <el-breadcrumb-item 
              v-for="item in breadcrumbItems" 
              :key="item.path" 
              :to="item.path ? { path: item.path } : null"
            >
              {{ item.title }}
            </el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        
        <div class="header-right">
          <el-button text @click="showNotifications">
            <el-icon><Bell /></el-icon>
          </el-button>
          
          <el-dropdown @command="handleUserCommand">
            <span class="user-dropdown">
              <UserAvatar 
                :src="userStore.userAvatar" 
                :username="userStore.userInfo?.username" 
                size="small" 
                clickable
              />
              <span class="username">{{ userStore.userInfo?.realName || userStore.userInfo?.username }}</span>
              <el-icon><CaretBottom /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">
                  <el-icon><User /></el-icon>
                  个人资料
                </el-dropdown-item>
                <el-dropdown-item command="settings">
                  <el-icon><Setting /></el-icon>
                  系统设置
                </el-dropdown-item>
                <el-dropdown-item divided command="logout">
                  <el-icon><DocumentRemove /></el-icon>
                  退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </div>

      <!-- 页面内容 -->
      <div class="page-content">
        <router-view />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { usePermissionStore } from '@/stores/permission'
import { useChatStore } from '@/stores/chat'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  User,
  Setting,
  Expand,
  Fold,
  CaretBottom,
  DocumentRemove,
  Bell
} from '@element-plus/icons-vue'
import DynamicMenu from './DynamicMenu.vue'
import UserAvatar from '@/components/common/UserAvatar.vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const permissionStore = usePermissionStore()
const chatStore = useChatStore()

const sidebarCollapsed = ref(false)

// 当前激活的菜单项
const activeMenu = computed(() => {
  const path = route.path
  
  // 健康数据子页面
  if (path.startsWith('/health/')) {
    return path
  }
  
  return path
})

// 面包屑导航
const breadcrumbItems = computed(() => {
  const path = route.path
  const items = []
  
  if (path === '/dashboard') {
    items.push({ title: '仪表板' })
  } else if (path === '/profile') {
    items.push({ title: '个人资料' })
  } else if (path.startsWith('/health/')) {
    items.push({ title: '健康数据', path: '/health/overview' })
    
    if (path === '/health/overview') {
      items.push({ title: '健康概览' })
    } else if (path === '/health/glucose') {
      items.push({ title: '血糖记录' })
    } else if (path === '/health/pressure') {
      items.push({ title: '血压记录' })
    } else if (path === '/health/weight') {
      items.push({ title: '体重记录' })
    } else if (path === '/health/statistics') {
      items.push({ title: '健康统计' })
    }
  } else if (path === '/settings') {
    items.push({ title: '系统设置' })
  }
  
  return items
})

// 切换侧边栏
const toggleSidebar = () => {
  sidebarCollapsed.value = !sidebarCollapsed.value
}

// Element Plus的菜单router模式会自动处理路由跳转

// 显示通知
const showNotifications = () => {
  ElMessage.info('暂无新通知')
}

// 用户下拉菜单处理
const handleUserCommand = async (command) => {
  switch (command) {
    case 'profile':
      router.push('/profile')
      break
    case 'settings':
      router.push('/settings')
      break
    case 'logout':
      try {
        await ElMessageBox.confirm(
          '确定要退出登录吗？',
          '提示',
          {
            confirmButtonText: '确定',
            cancelButtonText: '取消',
            type: 'warning'
          }
        )
        // 清理WebSocket连接
        chatStore.disconnectWebSocket()
        chatStore.reset()
        
        await userStore.logout() // logout方法内部已经有成功消息和跳转逻辑
      } catch (error) {
        // 用户取消操作
      }
      break
  }
}

// 监听路由变化，更新活跃菜单
watch(
  () => route.path,
  () => {
    // 路由变化时的处理逻辑
  }
)

onMounted(async () => {
  // 初始化逻辑
  
  // 如果是医生用户，初始化WebSocket连接
  if (userStore.userInfo?.userType === 1) {
    try {
      await chatStore.initializeWebSocket()
      console.log('[MainLayout] 医生WebSocket连接已初始化')
    } catch (error) {
      console.error('[MainLayout] WebSocket初始化失败:', error)
    }
  }
})
</script>

<style scoped>
.main-layout {
  display: flex;
  height: 100vh;
  overflow: hidden;
}

.main-sidebar {
  width: 240px;
  background-color: #304156;
  transition: width 0.3s ease;
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
}

.main-sidebar.collapsed {
  width: 64px;
}

.sidebar-header {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-bottom: 1px solid #434a5a;
  color: white;
}

.logo h2 {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
}

.logo-mini h3 {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
}

.main-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.main-header {
  height: 60px;
  background-color: white;
  border-bottom: 1px solid #e6e6e6;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
  flex-shrink: 0;
}

.header-left {
  display: flex;
  align-items: center;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 16px;
}

.user-dropdown {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  padding: 4px 8px;
  border-radius: 4px;
  transition: background-color 0.2s;
}

.user-dropdown:hover {
  background-color: #f5f5f5;
}

.username {
  font-size: 14px;
  color: #303133;
}

.page-content {
  flex: 1;
  padding: 20px;
  overflow-y: auto;
  background-color: #f5f5f5;
}

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

:deep(.el-breadcrumb__item) {
  font-size: 14px;
}

:deep(.el-breadcrumb__inner) {
  color: #606266;
}

:deep(.el-breadcrumb__inner.is-link) {
  color: #409EFF;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .main-sidebar {
    position: absolute;
    left: 0;
    top: 0;
    height: 100vh;
    z-index: 1000;
    transform: translateX(-100%);
    transition: transform 0.3s ease;
  }

  .main-sidebar:not(.collapsed) {
    transform: translateX(0);
  }

  .main-content {
    width: 100%;
  }

  .page-content {
    padding: 16px;
  }

  .header-left .el-breadcrumb {
    display: none;
  }
}
</style> 