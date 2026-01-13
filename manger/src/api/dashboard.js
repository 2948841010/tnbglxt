import { api } from './request'

/**
 * 管理员数据看板相关API
 */
export const dashboardAPI = {
  // 获取系统统计数据
  getSystemStats: () => {
    return api.get('/v1/admin/dashboard/stats')
  },

  // 获取用户统计数据
  getUserStats: () => {
    return api.get('/v1/admin/dashboard/users')
  },

  // 获取医生统计数据
  getDoctorStats: () => {
    return api.get('/v1/admin/dashboard/doctors')
  },

  // 获取咨询统计数据
  getConsultationStats: () => {
    return api.get('/v1/admin/dashboard/consultations')
  },

  // 获取趋势数据
  getTrends: (params = {}) => {
    return api.get('/v1/admin/dashboard/trends', params)
  },

  // 获取实时在线数据
  getRealTimeData: () => {
    return api.get('/v1/admin/dashboard/realtime')
  }
}

export default dashboardAPI 