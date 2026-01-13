import request from './request'

/**
 * 医生相关API
 */
export const doctorAPI = {
  
  // ============================== 医生信息查询 ==============================
  
  /**
   * 获取当前医生信息
   */
            getCurrentProfile() {
            return request({
              url: '/v1/doctor/profile',
              method: 'get'
            })
          },
  
  /**
   * 根据医生编号获取医生信息
   * @param {string} doctorNo 医生编号
   */
            getDoctorInfo(doctorNo) {
            return request({
              url: `/v1/doctor/info/${doctorNo}`,
              method: 'get'
            })
          },
  
  // ============================== 医生信息更新 ==============================
  
  /**
   * 更新医生个人信息
   * @param {Object} profileData 医生信息数据
   * @param {Object} profileData.user 用户基础信息
   * @param {Object} profileData.doctorInfo 医生专业信息
   */
            updateProfile(profileData) {
            return request({
              url: '/v1/doctor/profile',
              method: 'put',
              data: profileData
            })
          },
  
  /**
   * 更新医生在线状态
   * @param {number} onlineStatus 在线状态（0-离线，1-在线，2-忙碌）
   */
            updateOnlineStatus(onlineStatus) {
            return request({
              url: '/v1/doctor/online-status',
              method: 'put',
              data: {
                onlineStatus
              }
            })
          }
} 