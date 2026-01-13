import request from './request'

/**
 * 问诊聊天API
 */

/**
 * 创建问诊
 * @param {Object} data - 创建问诊请求数据
 * @returns {Promise}
 */
export function createConsultation(data) {
  return request({
    url: '/v1/chat/consultation',
    method: 'POST',
    data
  })
}

/**
 * 发送消息
 * @param {Object} data - 发送消息请求数据
 * @returns {Promise}
 */
export function sendMessage(data) {
  return request({
    url: '/v1/chat/message',
    method: 'POST',
    data
  })
}

/**
 * 上传聊天图片
 * @param {File} file - 图片文件
 * @param {string} consultationNo - 咨询编号
 * @param {Function} onUploadProgress - 上传进度回调
 * @returns {Promise}
 */
export function uploadChatImage(file, consultationNo, onUploadProgress) {
  const formData = new FormData()
  formData.append('file', file)
  formData.append('consultationNo', consultationNo)
  
  return request({
    url: '/v1/chat/upload/image',
    method: 'POST',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    },
    timeout: 60000, // 60秒超时
    onUploadProgress
  })
}

/**
 * 🔥 上传聊天文件（通用）
 * @param {File} file - 文件
 * @param {string} consultationNo - 咨询编号
 * @param {Function} onUploadProgress - 上传进度回调
 * @returns {Promise}
 */
export function uploadChatFile(file, consultationNo, onUploadProgress) {
  const formData = new FormData()
  formData.append('file', file)
  formData.append('consultationNo', consultationNo)
  
  return request({
    url: '/v1/chat/upload/file',
    method: 'POST',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    },
    timeout: 120000, // 120秒超时（文件可能较大）
    onUploadProgress
  })
}

/**
 * 🔥 下载聊天文件
 * @param {string} fileUrl - 文件URL
 * @param {string} fileName - 文件名
 * @returns {Promise}
 */
export function downloadChatFile(fileUrl, fileName) {
  // 如果是外部URL（如OSS），直接创建下载链接
  if (fileUrl.startsWith('http://') || fileUrl.startsWith('https://')) {
    return new Promise((resolve) => {
      const link = document.createElement('a')
      link.href = fileUrl
      link.download = fileName || 'download'
      link.target = '_blank' // 在新标签页打开，如果下载失败会显示文件
      document.body.appendChild(link)
      link.click()
      document.body.removeChild(link)
      resolve()
    })
  } else {
    // 如果是相对路径，通过API代理下载
    return request({
      url: fileUrl,
      method: 'GET',
      responseType: 'blob'
    }).then(response => {
      const blob = new Blob([response.data])
      const link = document.createElement('a')
      link.href = URL.createObjectURL(blob)
      link.download = fileName || 'download'
      document.body.appendChild(link)
      link.click()
      document.body.removeChild(link)
      URL.revokeObjectURL(link.href)
    })
  }
}

/**
 * 获取问诊详情
 * @param {string} consultationNo - 问诊编号
 * @returns {Promise}
 */
export function getConsultationDetail(consultationNo) {
  return request({
    url: `/v1/chat/consultation/${consultationNo}`,
    method: 'GET'
  })
}

/**
 * 获取我的问诊列表
 * @returns {Promise}
 */
export function getMyConsultations() {
  return request({
    url: '/v1/chat/consultations/mine',
    method: 'GET'
  })
}

/**
 * 分页获取我的问诊列表
 * @param {number} page - 页码
 * @param {number} size - 每页大小
 * @returns {Promise}
 */
export function getMyConsultationsPage(page = 1, size = 10) {
  return request({
    url: '/v1/chat/consultations/mine/page',
    method: 'GET',
    params: { page, size }
  })
}

/**
 * 医生接诊
 * @param {string} consultationNo - 问诊编号
 * @returns {Promise}
 */
export function acceptConsultation(consultationNo) {
  return request({
    url: `/v1/chat/consultation/${consultationNo}/accept`,
    method: 'POST'
  })
}

/**
 * 完成问诊
 * @param {string} consultationNo - 问诊编号
 * @returns {Promise}
 */
export function completeConsultation(consultationNo) {
  return request({
    url: `/v1/chat/consultation/${consultationNo}/complete`,
    method: 'POST'
  })
}

/**
 * 取消问诊
 * @param {string} consultationNo - 问诊编号
 * @returns {Promise}
 */
export function cancelConsultation(consultationNo) {
  return request({
    url: `/v1/chat/consultation/${consultationNo}/cancel`,
    method: 'POST'
  })
}

/**
 * 标记消息已读
 * @param {string} consultationNo - 问诊编号
 * @param {string} messageId - 消息ID
 * @returns {Promise}
 */
export function markMessageAsRead(consultationNo, messageId) {
  return request({
    url: `/v1/chat/consultation/${consultationNo}/message/${messageId}/read`,
    method: 'POST'
  })
}

/**
 * 获取活跃问诊
 * @returns {Promise}
 */
export function getActiveConsultations() {
  return request({
    url: '/v1/chat/consultations/active',
    method: 'GET'
  })
}

// ============================== 医生端专用API ==============================

/**
 * 获取医生的咨询列表（分页）
 * @param {number} page - 页码
 * @param {number} size - 每页大小
 * @param {string} status - 状态筛选（可选）
 * @returns {Promise}
 */
export function getDoctorConsultationsPage(page = 1, size = 10, status = '') {
  return request({
    url: '/v1/chat/consultations/mine/page',
    method: 'GET',
    params: { page, size, status }
  })
}

/**
 * 获取医生已回复的咨询列表（分页）
 * @param {number} page - 页码
 * @param {number} size - 每页大小
 * @returns {Promise}
 */
export function getDoctorRepliedConsultations(page = 1, size = 10) {
  return request({
    url: '/v1/chat/consultations/mine/page',
    method: 'GET',
    params: { page, size, status: '2,3' } // 进行中和已完成
  })
}

/**
 * 获取今日问诊数量
 * @returns {Promise}
 */
export function getTodayConsultationCount() {
  return request({
    url: '/v1/chat/consultations/today/count',
    method: 'GET'
  })
}

/**
 * 获取待回复咨询数量
 * @returns {Promise}
 */
export function getPendingConsultationsCount() {
  return request({
    url: '/v1/chat/consultations/pending/count',
    method: 'GET'
  })
}

/**
 * 获取待接诊咨询数量
 * @returns {Promise}
 */
export function getWaitingConsultationsCount() {
  return request({
    url: '/v1/chat/consultations/waiting/count',
    method: 'GET'
  })
}

/**
 * 获取正在接诊咨询数量
 * @returns {Promise}
 */
export function getOngoingConsultationsCount() {
  return request({
    url: '/v1/chat/consultations/ongoing/count',
    method: 'GET'
  })
}

/**
 * 获取已完成咨询数量
 * @returns {Promise}
 */
export function getCompletedConsultationsCount() {
  return request({
    url: '/v1/chat/consultations/completed/count',
    method: 'GET'
  })
}

/**
 * 获取最近咨询列表
 * @param {number} limit - 限制数量
 * @returns {Promise}
 */
export function getRecentConsultations(limit = 5) {
  return request({
    url: '/v1/chat/consultations/recent',
    method: 'GET',
    params: { limit }
  })
} 