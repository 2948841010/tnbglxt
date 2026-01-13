import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { usePermissionStore } from '@/stores/permission'
import { ElMessage } from 'element-plus'

const routes = [
  {
    path: '/',
    redirect: '/login'
  },
  
  // 登录页面
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/auth/Login.vue'),
    meta: {
      title: '管理员登录',
      requiresAuth: false
    }
  },
  
  // 主布局
  {
    path: '/',
    component: () => import('@/components/layout/MainLayout.vue'),
    meta: {
      requiresAuth: true
    },
    children: [
      {
        path: 'dashboard',
        name: 'AdminDashboard',
        component: () => import('@/views/Dashboard.vue'),
        meta: {
          title: '数据看板',
          requiresAuth: true,
          permission: 'admin:dashboard:view'
        }
      },
      {
        path: 'users',
        name: 'UserManagement',
        component: () => import('@/views/users/UserManagement.vue'),
        meta: {
          title: '用户管理',
          requiresAuth: true,
          permission: 'admin:user:view'
        }
      },
      {
        path: 'doctors',
        name: 'DoctorManagement',
        component: () => import('@/views/doctors/DoctorManagement.vue'),
        meta: {
          title: '医生管理',
          requiresAuth: true,
          permission: 'admin:doctor:view'
        }
      },
      {
        path: 'consultations',
        name: 'ConsultationManagement',
        component: () => import('@/views/consultations/ConsultationManagement.vue'),
        meta: {
          title: '咨询管理',
          requiresAuth: true,
          permission: 'admin:consultation:view'
        }
      },
      {
        path: 'settings',
        name: 'SystemSettings',
        component: () => import('@/views/system/SystemSettings.vue'),
        meta: {
          title: '系统设置',
          requiresAuth: true,
          permission: 'admin:setting:view'
        }
      },
      {
        path: 'permissions',
        name: 'PermissionManagement',
        component: () => import('@/views/permissions/PermissionManagement.vue'),
        meta: {
          title: '权限管理',
          requiresAuth: true,
          permission: 'admin:permission:view'
        }
      },
      {
        path: 'profile',
        name: 'AdminProfile',
        component: () => import('@/views/admin/Profile.vue'),
        meta: {
          title: '个人资料',
          requiresAuth: true
        }
      }
    ]
  },
  
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('@/views/error/NotFound.vue'),
    meta: {
      title: '页面未找到',
      requiresAuth: false
    }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 全局路由守卫
router.beforeEach(async (to, from, next) => {
  const userStore = useUserStore()
  const permissionStore = usePermissionStore()
  
  // 设置页面标题
  if (to.meta.title) {
    document.title = `${to.meta.title} - 糖尿病智能服务管理系统管理端`
  }

  // 检查是否需要认证
  if (to.meta.requiresAuth) {
    if (!userStore.isAuthenticated) {
      ElMessage.warning('请先登录')
      next('/login')
      return
    }
    
    // 验证令牌是否有效
    if (userStore.isTokenExpired) {
      ElMessage.error('登录已过期，请重新登录')
      await userStore.logout(false)
      next('/login')
      return
    }

    // 管理员权限检查（userType: 2）
    const userType = userStore.userInfo?.userType
    if (userType !== 2) {
      ElMessage.warning('您没有访问管理端的权限')
      await userStore.logout(false)
      next('/login')
      return
    }

    // 获取用户权限（如果还没有加载）
    if (!permissionStore.permissionLoaded) {
      try {
        await permissionStore.getMenuPermissions(userStore.userInfo?.id)
      } catch (error) {
        console.error('获取权限失败:', error)
        ElMessage.error('获取权限失败，请重新登录')
        await userStore.logout(false)
        next('/login')
        return
      }
    }

    // 检查路由权限
    if (to.meta.permission) {
      const hasPermission = permissionStore.hasMenuPermission(to.meta.permission)
      if (!hasPermission) {
        ElMessage.warning('您没有访问此页面的权限')
        next('/dashboard') // 重定向到首页
        return
      }
    }
  }

  // 已登录用户访问登录页面，重定向到仪表板
  if (to.name === 'Login' && userStore.isAuthenticated) {
    const userType = userStore.userInfo?.userType
    if (userType === 2) {
      next('/dashboard')
    } else {
      await userStore.logout(false)
      next()
    }
    return
  }

  next()
})

export default router 