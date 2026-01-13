import { api } from './request'

/**
 * 健康记录相关API
 */

// ===== 血糖记录相关接口 =====

/**
 * 添加血糖记录
 * @param {Object} data 血糖记录数据
 */
export const addBloodGlucoseRecord = (data) => {
  // 转换时间格式
  const formattedData = {
    ...data,
    measureTime: data.measureTime ? data.measureTime.replace('T', ' ') : data.measureTime
  }
  return api.post('/v1/health/glucose', formattedData)
}

/**
 * 更新血糖记录
 * @param {string} recordId 记录ID
 * @param {Object} data 更新的血糖记录数据
 */
export const updateBloodGlucoseRecord = (recordId, data) => {
  // 转换时间格式
  const formattedData = {
    ...data,
    measureTime: data.measureTime ? data.measureTime.replace('T', ' ') : data.measureTime
  }
  return api.put('/v1/health/glucose', formattedData, {
    params: { recordId: recordId }
  })
}

/**
 * 获取血糖记录列表
 * @param {Object} params 查询参数
 */
export const getBloodGlucoseRecords = (params = {}) => {
  return api.get('/v1/health/glucose', params)
}

/**
 * 删除血糖记录
 * @param {string} recordId 记录ID
 */
export const deleteBloodGlucoseRecord = (recordId) => {
  return api.delete('/v1/health/glucose', { 
    params: { recordId: recordId }
  })
}

/**
 * 批量删除血糖记录
 * @param {Array} recordIds 记录ID列表
 */
export const batchDeleteBloodGlucoseRecords = (recordIds) => {
  return api.delete('/v1/health/glucose/batch', {
    data: recordIds
  })
}

// ===== 血压记录相关接口 =====

/**
 * 添加血压记录
 * @param {Object} data 血压记录数据
 */
export const addBloodPressureRecord = (data) => {
  // 转换时间格式
  const formattedData = {
    ...data,
    measureTime: data.measureTime ? data.measureTime.replace('T', ' ') : data.measureTime
  }
  return api.post('/v1/health/pressure', formattedData)
}

/**
 * 更新血压记录
 * @param {string} recordId 记录ID
 * @param {Object} data 更新的血压记录数据
 */
export const updateBloodPressureRecord = (recordId, data) => {
  // 转换时间格式
  const formattedData = {
    ...data,
    measureTime: data.measureTime ? data.measureTime.replace('T', ' ') : data.measureTime
  }
  return api.put('/v1/health/pressure', formattedData, {
    params: { recordId: recordId }
  })
}

/**
 * 获取血压记录列表
 * @param {Object} params 查询参数
 */
export const getBloodPressureRecords = (params = {}) => {
  return api.get('/v1/health/pressure', params)
}

/**
 * 删除血压记录
 * @param {string} recordId 记录ID
 */
export const deleteBloodPressureRecord = (recordId) => {
  return api.delete('/v1/health/pressure', { 
    params: { recordId: recordId }
  })
}

/**
 * 批量删除血压记录
 * @param {Array} recordIds 记录ID列表
 */
export const batchDeleteBloodPressureRecords = (recordIds) => {
  return api.delete('/v1/health/pressure/batch', {
    data: recordIds
  })
}

// ===== 体重记录相关接口 =====

/**
 * 添加体重记录
 * @param {Object} data 体重记录数据
 */
export const addWeightRecord = (data) => {
  // 转换时间格式
  const formattedData = {
    ...data,
    measureTime: data.measureTime ? data.measureTime.replace('T', ' ') : data.measureTime
  }
  return api.post('/v1/health/weight', formattedData)
}

/**
 * 更新体重记录
 * @param {string} recordId 记录ID
 * @param {Object} data 更新的体重记录数据
 */
export const updateWeightRecord = (recordId, data) => {
  // 转换时间格式
  const formattedData = {
    ...data,
    measureTime: data.measureTime ? data.measureTime.replace('T', ' ') : data.measureTime
  }
  return api.put('/v1/health/weight', formattedData, {
    params: { recordId: recordId }
  })
}

/**
 * 获取体重记录列表
 * @param {Object} params 查询参数
 */
export const getWeightRecords = (params = {}) => {
  return api.get('/v1/health/weight', params)
}

/**
 * 删除体重记录
 * @param {string} recordId 记录ID
 */
export const deleteWeightRecord = (recordId) => {
  return api.delete('/v1/health/weight', { 
    params: { recordId: recordId }
  })
}

/**
 * 批量删除体重记录
 * @param {Array} recordIds 记录ID列表
 */
export const batchDeleteWeightRecords = (recordIds) => {
  return api.delete('/v1/health/weight/batch', {
    data: recordIds
  })
}

// ===== 综合功能接口 =====

/**
 * 获取健康统计信息
 */
export const getHealthStatistics = () => {
  return api.get('/v1/health/statistics')
}

/**
 * 获取健康数据趋势
 * @param {string} dataType 数据类型 (glucose/pressure/weight)
 * @param {number} days 天数
 */
export const getHealthDataTrend = (dataType, days = 30) => {
  return api.get('/v1/health/trend', { 
    dataType: dataType,
    days: days 
  })
}

/**
 * 初始化用户健康档案
 */
export const initUserHealthProfile = () => {
  return api.post('/v1/health/profile/init')
}

// ===== 通用健康记录接口（兼容旧版本） =====

/**
 * 获取所有类型的健康记录
 * @param {Object} params 查询参数
 */
export const getAllHealthRecords = async (params = {}) => {
  const { recordType, ...otherParams } = params
  
  try {
    let result = []
    
    if (!recordType || recordType === 'glucose') {
      const glucoseData = await getBloodGlucoseRecords(otherParams)
      if (glucoseData.data && glucoseData.data.records) {
        result = result.concat(
          glucoseData.data.records.map(record => ({
            ...record,
            type: 'glucose',
            id: `glucose_${record.measureTime}`
          }))
        )
      }
    }
    
    if (!recordType || recordType === 'pressure') {
      const pressureData = await getBloodPressureRecords(otherParams)
      if (pressureData.data && pressureData.data.records) {
        result = result.concat(
          pressureData.data.records.map(record => ({
            ...record,
            type: 'pressure',
            id: `pressure_${record.measureTime}`
          }))
        )
      }
    }
    
    if (!recordType || recordType === 'weight') {
      const weightData = await getWeightRecords(otherParams)
      if (weightData.data && weightData.data.records) {
        result = result.concat(
          weightData.data.records.map(record => ({
            ...record,
            type: 'weight',
            id: `weight_${record.measureTime}`
          }))
        )
      }
    }
    
    // 按时间排序
    result.sort((a, b) => new Date(b.measureTime) - new Date(a.measureTime))
    
    return {
      code: 200,
      data: result,
      message: '查询成功'
    }
  } catch (error) {
    console.error('获取健康记录失败:', error)
    throw error
  }
}

// ===== 医生端专用接口 =====

/**
 * 医生查询患者血糖趋势
 * @param {number} patientId 患者用户ID  
 * @param {number} days 天数
 */
export const getPatientBloodGlucoseTrend = (patientId, days = 30) => {
  return api.get(`/v1/health/doctor/patient/${patientId}/glucose/trend`, { 
    days: days 
  })
}

/**
 * 医生查询患者血压趋势
 * @param {number} patientId 患者用户ID
 * @param {number} days 天数
 */
export const getPatientBloodPressureTrend = (patientId, days = 30) => {
  return api.get(`/v1/health/doctor/patient/${patientId}/pressure/trend`, { 
    days: days 
  })
}

/**
 * 医生查询患者健康数据概览
 * @param {number} patientId 患者用户ID
 */
export const getPatientHealthOverview = (patientId) => {
  return api.get(`/v1/health/doctor/patient/${patientId}/overview`)
} 