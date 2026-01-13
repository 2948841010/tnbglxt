/**
 * Agent API - 智能问诊后端接口
 */

import axios from 'axios'
import { useUserStore } from '@/stores/user'
import Cookies from 'js-cookie'

// 创建专门的Agent请求实例，直接连接Agent后端
const agentRequest = axios.create({
  baseURL: 'http://localhost:8081', // 直接连接Agent后端
  timeout: 30000,    // 30秒超时
  headers: {
    'Content-Type': 'application/json;charset=UTF-8'
  }
})

// Agent请求拦截器 - 添加JWT token
agentRequest.interceptors.request.use(
  config => {
    const userStore = useUserStore()
    
    // 添加token到请求头
    const token = userStore.token || Cookies.get('access_token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }

    return config
  },
  error => {
    console.error('Agent请求错误:', error)
    return Promise.reject(error)
  }
)

// Agent响应拦截器
agentRequest.interceptors.response.use(
  response => {
    return response.data // 直接返回data部分
  },
  error => {
    console.error('Agent响应错误:', error)
    
    // 处理常见错误
    if (error.response?.status === 401) {
      console.warn('Agent认证失败')
    } else if (error.response?.status === 500) {
      console.error('Agent服务器错误')
    }
    
    return Promise.reject(error)
  }
)

/**
 * Agent API封装
 */
export const agentAPI = {
  /**
   * 智能对话
   * @param {string} message - 用户消息
   * @returns {Promise} API响应
   */
  chat: (message) => {
    return agentRequest({
      url: '/chat',
      method: 'post',
      data: {
        message: message
      }
    })
  },

  /**
   * 获取对话历史
   * @param {number} limit - 消息数量限制
   * @returns {Promise} API响应
   */
  getChatHistory: (limit = 20) => {
    return agentRequest({
      url: '/chat/history',
      method: 'get',
      params: {
        limit
      }
    })
  },

  /**
   * 清除对话历史
   * @returns {Promise} API响应
   */
  clearChatHistory: () => {
    return agentRequest({
      url: '/chat/history',
      method: 'delete'
    })
  },

  /**
   * 获取可用工具列表
   * @returns {Promise} API响应
   */
  getAvailableTools: () => {
    return agentRequest({
      url: '/tools',
      method: 'get'
    })
  },

  /**
   * 直接调用MCP工具
   * @param {string} toolName - 工具名称
   * @param {object} parameters - 工具参数
   * @returns {Promise} API响应
   */
  callTool: (toolName, parameters) => {
    return agentRequest({
      url: '/tools/call',
      method: 'post',
      params: {
        tool_name: toolName
      },
      data: parameters
    })
  },

  /**
   * 验证JWT token
   * @returns {Promise} API响应
   */
  verifyToken: () => {
    return agentRequest({
      url: '/auth/verify',
      method: 'get'
    })
  },

  /**
   * 获取token信息
   * @returns {Promise} API响应
   */
  getTokenInfo: () => {
    return agentRequest({
      url: '/auth/token-info',
      method: 'get'
    })
  },

  /**
   * 获取当前用户信息
   * @returns {Promise} API响应
   */
  getCurrentUserInfo: () => {
    return agentRequest({
      url: '/user/info',
      method: 'get'
    })
  },

  /**
   * 健康检查
   * @returns {Promise} API响应
   */
  healthCheck: () => {
    return agentRequest({
      url: '/health',
      method: 'get'
    })
  },

  /**
   * 获取会话列表
   * @param {number} limit - 返回数量限制
   * @returns {Promise} API响应
   */
  getChatSessions: (limit = 10) => {
    return agentRequest({
      url: '/chat/sessions',
      method: 'get',
      params: {
        limit
      }
    })
  },

  /**
   * 切换会话
   * @param {string} sessionId - 会话ID
   * @returns {Promise} API响应
   */
  switchChatSession: (sessionId) => {
    return agentRequest({
      url: '/chat/sessions/switch',
      method: 'post',
      data: {
        session_id: sessionId
      }
    })
  },

  /**
   * 创建新会话
   * @returns {Promise} API响应
   */
  createNewSession: () => {
    return agentRequest({
      url: '/chat/sessions/new',
      method: 'post'
    })
  },

  /**
   * 删除指定会话
   * @param {string} sessionId - 会话ID
   * @returns {Promise} API响应
   */
  deleteSession: (sessionId) => {
    return agentRequest({
      url: `/chat/sessions/${sessionId}`,
      method: 'delete'
    })
  }
} 