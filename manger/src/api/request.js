import axios from 'axios'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '@/stores/user'
import router from '@/router'
import Cookies from 'js-cookie'

// 创建axios实例
const request = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '/api',
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json;charset=UTF-8'
  }
})

// 请求拦截器
request.interceptors.request.use(
  config => {
    const userStore = useUserStore()
    
    // 添加管理员token到请求头
    const token = userStore.token || Cookies.get('admin_access_token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }

    // 添加管理端标识
    config.headers['Client-Type'] = 'admin'

    // 添加时间戳避免缓存
    if (config.method === 'get') {
      config.params = {
        ...config.params,
        _t: Date.now()
      }
    }

    return config
  },
  error => {
    console.error('请求错误:', error)
    return Promise.reject(error)
  }
)

// 响应拦截器
request.interceptors.response.use(
  response => {
    const { data } = response
    
    // 如果是文件流，直接返回
    if (response.config.responseType === 'blob') {
      return response
    }

    // 统一处理响应数据
    if (data.success === true || data.code === 200) {
      return data
    }

    // 处理业务错误
    const message = data.message || '请求失败'
    ElMessage.error(message)
    return Promise.reject(new Error(message))
  },
  async error => {
    const userStore = useUserStore()
    
    if (error.response) {
      const { status, data } = error.response
      
      switch (status) {
        case 401:
          // Token失效或未认证
          ElMessage.error(data.message || '登录已过期，请重新登录')
          await userStore.logout(false)
          router.push('/login')
          break
        case 403:
          ElMessage.error('权限不足，无法访问')
          break
        case 404:
          ElMessage.error('请求的资源不存在')
          break
        case 422:
          // 参数验证错误
          ElMessage.error(data.message || '参数验证失败')
          break
        case 429:
          ElMessage.error('请求过于频繁，请稍后重试')
          break
        case 500:
          ElMessage.error(data.message || '服务器内部错误')
          break
        default:
          ElMessage.error(data.message || `请求失败 (${status})`)
      }
    } else if (error.request) {
      // 网络错误
      if (error.code === 'ECONNABORTED' || error.message.includes('timeout')) {
        // 如果是文件上传超时，给出更友好的提示
        if (error.config?.headers?.['Content-Type']?.includes('multipart/form-data')) {
          ElMessage.warning('文件上传时间较长，请稍等片刻后刷新页面查看结果')
        } else {
          ElMessage.error('请求超时，请检查网络连接')
        }
      } else {
        ElMessage.error('网络连接失败，请检查网络设置')
      }
    } else {
      ElMessage.error('请求配置错误')
    }

    return Promise.reject(error)
  }
)

// 封装常用请求方法
export const api = {
  get: (url, params = {}, config = {}) => {
    return request.get(url, { params, ...config })
  },
  
  post: (url, data = {}, config = {}) => {
    return request.post(url, data, config)
  },
  
  put: (url, data = {}, config = {}) => {
    return request.put(url, data, config)
  },
  
  delete: (url, config = {}) => {
    return request.delete(url, config)
  },
  
  upload: (url, formData, onUploadProgress, timeout = 60000) => {
    return request.post(url, formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      },
      timeout: timeout, // 默认60秒超时
      onUploadProgress
    })
  },
  
  download: (url, params = {}, filename) => {
    return request.get(url, {
      params,
      responseType: 'blob'
    }).then(response => {
      const blob = new Blob([response.data])
      const link = document.createElement('a')
      link.href = URL.createObjectURL(blob)
      link.download = filename || 'download'
      link.click()
      URL.revokeObjectURL(link.href)
    })
  }
}

export default request 