import api from './request'

// 医生管理 API
const doctorAPI = {
  // 分页查询医生列表
  getDoctorList: (params) => {
    return api.get('/v1/admin/doctors/list', { params })
  },

  // 获取医生统计数据
  getDoctorStatistics: () => {
    return api.get('/v1/admin/doctors/statistics')
  },

  // 获取医生详情
  getDoctorDetail: (doctorId) => {
    return api.get(`/v1/admin/doctors/${doctorId}`)
  },

  // 更新医生信息
  updateDoctor: (doctorId, data) => {
    return api.put(`/v1/admin/doctors/${doctorId}`, data)
  },

  // 更新医生可接诊状态
  updateDoctorAvailable: (doctorId, available) => {
    return api.put(`/v1/admin/doctors/${doctorId}/available`, null, {
      params: { available }
    })
  },

  // 更新医生用户状态
  updateDoctorStatus: (doctorId, status) => {
    return api.put(`/v1/admin/doctors/${doctorId}/status`, null, {
      params: { status }
    })
  },

  // 删除医生
  deleteDoctor: (doctorId) => {
    return api.delete(`/v1/admin/doctors/${doctorId}`)
  },

  // 批量删除医生
  batchDeleteDoctors: (doctorIds) => {
    return api.delete('/v1/admin/doctors/batch', { data: doctorIds })
  }
}

export default doctorAPI 