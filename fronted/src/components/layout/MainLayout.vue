<template>
  <div class="main-layout">
    <!-- 侧边栏 -->
    <div class="main-sidebar" :class="{ collapsed: sidebarCollapsed }">
      <div class="sidebar-header">
        <router-link to="/dashboard" class="logo-link">
          <div class="logo" v-show="!sidebarCollapsed">
            <svg class="logo-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M22 12h-4l-3 9L9 3l-3 9H2"/>
            </svg>
            <span>糖尿病管理</span>
          </div>
          <div class="logo-mini" v-show="sidebarCollapsed">
            <svg class="logo-icon-mini" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M22 12h-4l-3 9L9 3l-3 9H2"/>
            </svg>
          </div>
        </router-link>
      </div>
      
      <!-- 动态菜单组件 -->
      <dynamic-menu :collapsed="sidebarCollapsed" />
    </div>

    <!-- 主内容区域 -->
    <div class="main-content">
      <!-- 头部 -->
      <div class="main-header">
        <div class="header-left">
          <button class="toggle-btn" @click="toggleSidebar">
            <svg v-if="sidebarCollapsed" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="20" height="20">
              <path d="M4 6h16M4 12h16M4 18h16"/>
            </svg>
            <svg v-else viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="20" height="20">
              <path d="M4 6h16M4 12h10M4 18h16"/>
            </svg>
          </button>
          <el-breadcrumb separator="/">
            <el-breadcrumb-item :to="{ path: '/dashboard' }">首页</el-breadcrumb-item>
            <el-breadcrumb-item v-for="item in breadcrumbItems" :key="item.path" :to="item.path ? { path: item.path } : null">
              {{ item.title }}
            </el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        
        <div class="header-right">
          <button class="header-btn" @click="showNotifications">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="20" height="20">
              <path d="M15 17h5l-1.405-1.405A2.032 2.032 0 0118 14.158V11a6.002 6.002 0 00-4-5.659V5a2 2 0 10-4 0v.341C7.67 6.165 6 8.388 6 11v3.159c0 .538-.214 1.055-.595 1.436L4 17h5m6 0v1a3 3 0 11-6 0v-1m6 0H9"/>
            </svg>
          </button>
          
          <el-dropdown @command="handleUserCommand">
            <span class="user-dropdown">
              <UserAvatar :src="userStore.userAvatar" :username="userStore.userInfo?.username" size="small" clickable/>
              <span class="username">{{ userStore.userInfo?.realName || userStore.userInfo?.username }}</span>
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="16" height="16">
                <path d="M19 9l-7 7-7-7"/>
              </svg>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">
                  <el-icon><User /></el-icon>个人资料
                </el-dropdown-item>
                <el-dropdown-item command="settings">
                  <el-icon><Setting /></el-icon>系统设置
                </el-dropdown-item>
                <el-dropdown-item divided command="logout">
                  <el-icon><DocumentRemove /></el-icon>退出登录
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
import { User, Setting, DocumentRemove } from '@element-plus/icons-vue'
import DynamicMenu from './DynamicMenu.vue'
import UserAvatar from '@/components/common/UserAvatar.vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const permissionStore = usePermissionStore()
const chatStore = useChatStore()

const sidebarCollapsed = ref(false)

const activeMenu = computed(() => {
  const path = route.path
  if (path.startsWith('/health/')) { return path }
  return path
})

const breadcrumbItems = computed(() => {
  const path = route.path
  const items = []
  if (path === '/dashboard') { items.push({ title: '仪表板' }) }
  else if (path === '/profile') { items.push({ title: '个人资料' }) }
  else if (path.startsWith('/health/')) {
    items.push({ title: '健康数据', path: '/health/overview' })
    if (path === '/health/overview') { items.push({ title: '健康概览' }) }
    else if (path === '/health/glucose') { items.push({ title: '血糖记录' }) }
    else if (path === '/health/pressure') { items.push({ title: '血压记录' }) }
    else if (path === '/health/weight') { items.push({ title: '体重记录' }) }
    else if (path === '/health/statistics') { items.push({ title: '健康统计' }) }
  }
  else if (path === '/settings') { items.push({ title: '系统设置' }) }
  return items
})

const toggleSidebar = () => { sidebarCollapsed.value = !sidebarCollapsed.value }

const showNotifications = () => { ElMessage.info('暂无新通知') }

const handleUserCommand = async (command) => {
  switch (command) {
    case 'profile': router.push('/profile'); break
    case 'settings': router.push('/settings'); break
    case 'logout':
      try {
        await ElMessageBox.confirm('确定要退出登录吗？', '提示', { confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning' })
        chatStore.disconnectWebSocket()
        chatStore.reset()
        await userStore.logout()
      } catch (error) {}
      break
  }
}

watch(() => route.path, () => {})

onMounted(async () => {
  if (userStore.userInfo?.userType === 1) {
    try { await chatStore.initializeWebSocket() } catch (error) { console.error('[MainLayout] WebSocket初始化失败:', error) }
  }
})
</script>

<style scoped>
.main-layout { display: flex; height: 100vh; overflow: hidden; font-family: 'Noto Sans', sans-serif; }

/* 侧边栏 - Healthcare风格 */
.main-sidebar { width: 260px; background: linear-gradient(180deg, #0E7490 0%, #164E63 100%); transition: width 0.3s ease; flex-shrink: 0; display: flex; flex-direction: column; box-shadow: 4px 0 20px rgba(8, 145, 178, 0.15); }
.main-sidebar.collapsed { width: 72px; }

.sidebar-header { height: 72px; display: flex; align-items: center; justify-content: center; border-bottom: 1px solid rgba(165, 243, 252, 0.2); }
.logo-link { text-decoration: none; }
.logo { display: flex; align-items: center; gap: 12px; color: white; }
.logo-icon { width: 32px; height: 32px; color: #22D3EE; }
.logo span { font-size: 1.1rem; font-weight: 700; letter-spacing: 0.5px; }
.logo-mini { display: flex; align-items: center; justify-content: center; }
.logo-icon-mini { width: 28px; height: 28px; color: #22D3EE; }

/* 主内容区域 */
.main-content { flex: 1; display: flex; flex-direction: column; overflow: hidden; background: #ECFEFF; }

/* 头部 - Healthcare风格 */
.main-header { height: 72px; background: white; border-bottom: 1px solid #A5F3FC; display: flex; align-items: center; justify-content: space-between; padding: 0 24px; flex-shrink: 0; box-shadow: 0 2px 8px rgba(8, 145, 178, 0.08); }
.header-left { display: flex; align-items: center; gap: 16px; }
.toggle-btn { width: 40px; height: 40px; border: none; background: #F0FDFA; border-radius: 10px; cursor: pointer; display: flex; align-items: center; justify-content: center; color: #0891B2; transition: all 0.2s; }
.toggle-btn:hover { background: #CFFAFE; }
.header-right { display: flex; align-items: center; gap: 16px; }
.header-btn { width: 40px; height: 40px; border: none; background: #F0FDFA; border-radius: 10px; cursor: pointer; display: flex; align-items: center; justify-content: center; color: #0891B2; transition: all 0.2s; }
.header-btn:hover { background: #CFFAFE; }

.user-dropdown { display: flex; align-items: center; gap: 10px; cursor: pointer; padding: 8px 12px; border-radius: 12px; transition: background-color 0.2s; }
.user-dropdown:hover { background-color: #F0FDFA; }
.username { font-size: 0.9rem; font-weight: 500; color: #164E63; }

/* 页面内容 */
.page-content { flex: 1; padding: 24px; overflow-y: auto; }

/* 面包屑样式 */
:deep(.el-breadcrumb__item) { font-size: 0.9rem; }
:deep(.el-breadcrumb__inner) { color: #64748B; }
:deep(.el-breadcrumb__inner.is-link) { color: #0891B2; font-weight: 500; }
:deep(.el-breadcrumb__inner.is-link:hover) { color: #0E7490; }

/* 下拉菜单样式 */
:deep(.el-dropdown-menu__item) { padding: 10px 20px; }
:deep(.el-dropdown-menu__item:hover) { background: #F0FDFA; color: #0891B2; }

/* 菜单样式调整 */
:deep(.el-menu) { border-right: none; flex: 1; background: transparent; }
:deep(.el-menu-item), :deep(.el-sub-menu__title) { color: rgba(255, 255, 255, 0.85); height: 52px; line-height: 52px; margin: 4px 12px; border-radius: 10px; transition: all 0.2s; }
:deep(.el-menu-item:hover), :deep(.el-sub-menu__title:hover) { background: rgba(34, 211, 238, 0.15); color: white; }
:deep(.el-menu-item.is-active) { background: linear-gradient(135deg, #0891B2 0%, #22D3EE 100%); color: white; font-weight: 600; }
:deep(.el-sub-menu .el-menu-item) { height: 48px; line-height: 48px; padding-left: 56px !important; }
:deep(.el-menu--collapse .el-menu-item), :deep(.el-menu--collapse .el-sub-menu__title) { margin: 4px 8px; }

/* 响应式设计 */
@media (max-width: 768px) {
  .main-sidebar { position: absolute; left: 0; top: 0; height: 100vh; z-index: 1000; transform: translateX(-100%); transition: transform 0.3s ease; }
  .main-sidebar:not(.collapsed) { transform: translateX(0); }
  .main-content { width: 100%; }
  .page-content { padding: 16px; }
  .header-left .el-breadcrumb { display: none; }
}
</style>
