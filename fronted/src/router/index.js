import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { usePermissionStore } from '@/stores/permission'
import { ElMessage } from 'element-plus'

const routes = [
  {
    path: '/',
    redirect: '/home'
  },
  
  // 公共页面路由（使用PublicLayout）
  {
    path: '/',
    component: () => import('@/components/layout/PublicLayout.vue'),
    meta: {
      requiresAuth: false
    },
    children: [
      {
        path: 'home',
        name: 'Home',
        component: () => import('@/views/public/Home.vue'),
        meta: {
          title: '首页',
          requiresAuth: false
        }
      },
      {
        path: 'assessment',
        name: 'Assessment',
        component: () => import('@/views/public/Assessment.vue'),
        meta: {
          title: '智能评测',
          requiresAuth: false
        }
      },
      {
        path: 'chat',
        name: 'PublicChat',
        component: () => import('@/views/public/Chat.vue'),
        meta: {
          title: '智能对话',
          requiresAuth: false
        }
      },
      {
        path: 'login',
        name: 'PublicLogin',
        component: () => import('@/views/public/Login.vue'),
        meta: {
          title: '用户登录',
          requiresAuth: false
        }
      }
    ]
  },

  // 保留原有的认证页面路由（兼容性）
  {
    path: '/auth/login',
    name: 'Login',
    component: () => import('@/views/auth/Login.vue'),
    meta: {
      title: '用户登录',
      requiresAuth: false
    }
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/auth/Register.vue'),
    meta: {
      title: '用户注册',
      requiresAuth: false
    }
  },
  {
    path: '/',
    component: () => import('@/components/layout/MainLayout.vue'),
    meta: {
      requiresAuth: true
    },
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/Dashboard.vue'),
        meta: {
          title: '仪表板',
          requiresAuth: true
        }
      },
      {
        path: 'profile',
        name: 'Profile',
        component: () => import('@/views/user/Profile.vue'),
        meta: {
          title: '个人资料',
          requiresAuth: true
        }
      },
      // 医生专用路由
      {
        path: 'doctor/dashboard',
        name: 'DoctorDashboard',
        component: () => import('@/views/doctor/Dashboard.vue'),
        meta: {
          title: '医生工作台',
          requiresAuth: true,
          userTypes: [1] // 只允许医生访问
        }
      },
      {
        path: 'doctor/profile',
        name: 'DoctorProfile',
        component: () => import('@/views/doctor/Profile.vue'),
        meta: {
          title: '医生个人信息',
          requiresAuth: true,
          userTypes: [1] // 只允许医生访问
        }
      },
      // 🔥 新增：统一咨询管理页面
      {
        path: 'doctor/consultation/management',
        name: 'ConsultationManagement',
        component: () => import('@/views/doctor/consultation/ConsultationManagement.vue'),
        meta: {
          title: '咨询管理',
          requiresAuth: true,
          userTypes: [1]
        }
      },
      // 🔄 兼容性重定向：原咨询列表页面
      {
        path: 'doctor/consultation/list',
        redirect: '/doctor/consultation/management',
        meta: {
          requiresAuth: true,
          userTypes: [1]
        }
      },
      // 🔄 兼容性重定向：原我的回复页面
      {
        path: 'doctor/consultation/replies',
        redirect: '/doctor/consultation/management',
        meta: {
          requiresAuth: true,
          userTypes: [1]
        }
      },
      {
        path: 'doctor/consultation/users',
        name: 'DoctorConsultationUsers',
        component: () => import('@/views/doctor/consultation/ConsultationUsers.vue'),
        meta: {
          title: '咨询用户',
          requiresAuth: true,
          userTypes: [1]
        }
      },
      {
        path: 'health/overview',
        name: 'HealthOverview',
        component: () => import('@/views/health/Overview.vue'),
        meta: {
          title: '健康概览',
          requiresAuth: true
        }
      },
      {
        path: 'health/glucose',
        name: 'BloodGlucose',
        component: () => import('@/views/health/BloodGlucose.vue'),
        meta: {
          title: '血糖记录',
          requiresAuth: true
        }
      },
      {
        path: 'health/pressure',
        name: 'BloodPressure',
        component: () => import('@/views/health/BloodPressure.vue'),
        meta: {
          title: '血压记录',
          requiresAuth: true
        }
      },
      {
        path: 'health/weight',
        name: 'WeightRecord',
        component: () => import('@/views/health/WeightRecord.vue'),
        meta: {
          title: '体重记录',
          requiresAuth: true
        }
      },
      {
        path: 'health/statistics',
        name: 'HealthStatistics',
        component: () => import('@/views/health/Statistics.vue'),
        meta: {
          title: '健康统计',
          requiresAuth: true
        }
      },
      // 用户端在线咨询路由
      {
        path: 'consultation/doctors',
        name: 'DoctorList',
        component: () => import('@/views/consultation/DoctorList.vue'),
        meta: {
          title: '医生列表',
          requiresAuth: true,
          userTypes: [0] // 只有普通用户可以访问
        }
      },
      {
        path: 'consultation/my',
        name: 'MyConsultations',
        component: () => import('@/views/consultation/MyConsultations.vue'),
        meta: {
          title: '我的咨询',
          requiresAuth: true,
          userTypes: [0] // 只有普通用户可以访问
        }
      },
      {
        path: 'consultation/chat/:id',
        name: 'ConsultationChat',
        component: () => import('@/views/consultation/ConsultationChat.vue'),
        meta: {
          title: '咨询对话',
          requiresAuth: true,
          userTypes: [0, 1] // 允许普通用户和医生访问
        }
      },
      {
        path: 'consultation/ai',
        name: 'AiConsultation',
        component: () => import('@/views/consultation/AiConsultation.vue'),
        meta: {
          title: '智能问诊',
          requiresAuth: true,
          userTypes: [0] // 只允许普通用户访问
        }
      },
      {
        path: 'settings',
        name: 'Settings',
        component: () => import('@/views/Settings.vue'),
        meta: {
          title: '系统设置',
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
    document.title = `${to.meta.title} - 糖尿病智能服务管理系统`
  }

  // 检查是否需要认证
  if (to.meta.requiresAuth) {
    if (!userStore.isAuthenticated) {
      ElMessage.warning('请先登录')
      next('/login')  // 跳转到公共登录页面
      return
    }
    
    // 验证令牌是否有效
    if (userStore.isTokenExpired) {
      ElMessage.error('登录已过期，请重新登录')
      await userStore.logout(false)  // 不显示退出消息，因为已经显示过期消息
      next('/login')
      return
    }

    // 检查用户类型权限
    if (to.meta.userTypes && Array.isArray(to.meta.userTypes)) {
      const userType = userStore.userInfo?.userType
      if (!to.meta.userTypes.includes(userType)) {
        ElMessage.warning('您没有访问此页面的权限')
        // 根据用户类型跳转到对应的默认页面
        const defaultPath = userType === 1 ? '/doctor/dashboard' : '/dashboard'
        next(defaultPath)
        return
      }
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

    // 检查菜单权限（除了基础页面）
    const allowedPaths = [
      '/dashboard', '/profile', '/doctor/profile', '/doctor/dashboard',
      '/doctor/consultation/list', '/doctor/consultation/replies', '/doctor/consultation/users',
      '/consultation/doctors', '/consultation/my',
      '/'
    ]
    if (!allowedPaths.includes(to.path) && !to.path.startsWith('/consultation/chat/')) {
      // 如果权限还没加载完成，暂时允许访问
      if (!permissionStore.permissionLoaded) {
        next()
        return
      }
      
      const hasPermission = permissionStore.hasMenuPermission(to.path)
      if (!hasPermission) {
        ElMessage.warning('您没有访问此页面的权限')
        // 根据用户类型跳转到对应的默认页面
        const userType = userStore.userInfo?.userType
        const defaultPath = userType === 1 ? '/doctor/dashboard' : '/dashboard'
        next(defaultPath)
        return
      }
    }
  }

  // 已登录用户访问登录页面，重定向到对应的默认页面
  if ((to.name === 'Login' || to.name === 'Register' || to.name === 'PublicLogin') && userStore.isAuthenticated) {
    // 根据用户类型跳转到对应的默认页面
    const userType = userStore.userInfo?.userType
    const defaultPath = userType === 1 ? '/doctor/dashboard' : '/dashboard'
    next(defaultPath)
    return
  }

  next()
})

export default router 