import request from './request'

/**
 * 获取可咨询的医生列表
 */
export function getDoctorList(params = {}) {
  return request({
    url: '/user/consultation/doctors',
    method: 'GET',
    params: {
      department: params.department,
      keyword: params.keyword,
      page: params.page || 1,
      size: params.size || 20
    }
  })
}

/**
 * 获取所有科室列表
 */
export function getDepartments() {
  return request({
    url: '/user/consultation/departments',
    method: 'GET'
  })
}

/**
 * 获取问诊详情
 */
export function getConsultationDetail(consultationNo) {
  return request({
    url: `/v1/consultation/chat/${consultationNo}`,
    method: 'GET'
  })
}

/**
 * 获取问诊消息列表
 */
export function getConsultationMessages(consultationNo) {
  return request({
    url: `/v1/consultation/chat/${consultationNo}/messages`,
    method: 'GET'
  })
}

/**
 * 发送消息
 */
export function sendMessage(data) {
  return request({
    url: '/v1/consultation/chat/send',
    method: 'POST',
    data
  })
}

/**
 * 接受问诊
 */
export function acceptConsultation(consultationNo) {
  return request({
    url: `/v1/consultation/${consultationNo}/accept`,
    method: 'POST'
  })
}

/**
 * 结束问诊
 */
export function completeConsultation(consultationNo) {
  return request({
    url: `/v1/consultation/${consultationNo}/complete`,
    method: 'POST'
  })
}

/**
 * 取消问诊
 */
export function cancelConsultation(consultationNo, reason) {
  return request({
    url: `/v1/consultation/${consultationNo}/cancel`,
    method: 'POST',
    data: { reason }
  })
}

/**
 * 获取医生咨询列表
 */
export function getDoctorConsultations(params = {}) {
  return request({
    url: '/v1/consultation/doctor/list',
    method: 'GET',
    params
  })
}

/**
 * 获取医生最近咨询列表
 */
export function getRecentConsultations(limit = 10) {
  return request({
    url: '/v1/consultation/chat/recent',
    method: 'GET',
    params: { limit }
  })
}

/**
 * 提交问诊评价
 */
export function submitConsultationRating(data) {
  return request({
    url: '/v1/consultation/rating',
    method: 'POST',
    data: {
      consultationNo: data.consultationNo,
      score: data.score,
      comment: data.comment || ''
    }
  })
}

/**
 * 获取问诊评价
 */
export function getConsultationRating(consultationNo) {
  return request({
    url: `/v1/consultation/${consultationNo}/rating`,
    method: 'GET'
  })
}

// 为了兼容性，也导出一个consultationAPI对象
export const consultationAPI = {
  getDoctorList,
  getDepartments,
  getConsultationDetail,
  getConsultationMessages,
  sendMessage,
  acceptConsultation,
  completeConsultation,
  cancelConsultation,
  getDoctorConsultations,
  getRecentConsultations,
  submitConsultationRating,
  getConsultationRating
} 