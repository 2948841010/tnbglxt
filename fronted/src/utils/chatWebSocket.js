import SockJS from 'sockjs-client'
import { Client } from '@stomp/stompjs'

/**
 * 聊天WebSocket管理器
 * 专门用于处理问诊聊天的实时通信
 */
class ChatWebSocketManager {
  constructor() {
    this.client = null
    this.connected = false
    this.subscriptions = new Map() // 存储订阅信息
    this.messageHandlers = new Map() // 存储消息处理函数 Map<consultationNo, Set<handlers>>
    this.reconnectAttempts = 0
    this.maxReconnectAttempts = 5
    this.reconnectDelay = 3000
    this.heartbeatIncoming = 4000
    this.heartbeatOutgoing = 4000
  }

  /**
   * 连接WebSocket
   */
  async connect() {
    return new Promise((resolve, reject) => {
      try {
        if (this.connected) {
          resolve()
          return
        }

        // 创建SockJS连接
        const socket = new SockJS('http://localhost:8080/ws')
        
        // 创建STOMP客户端
        this.client = new Client({
          webSocketFactory: () => socket,
          heartbeatIncoming: this.heartbeatIncoming,
          heartbeatOutgoing: this.heartbeatOutgoing,
          reconnectDelay: this.reconnectDelay,
          debug: (str) => {
            console.log('[ChatWebSocket Debug]', str)
          },
          onConnect: (frame) => {
            console.log('[ChatWebSocket] 连接成功:', frame)
            this.connected = true
            this.reconnectAttempts = 0
            
            // 重新订阅之前的主题
            this.resubscribeAll()
            
            resolve()
          },
          onDisconnect: () => {
            console.log('[ChatWebSocket] 连接断开')
            this.connected = false
          },
          onStompError: (frame) => {
            console.error('[ChatWebSocket] 连接错误:', frame)
            this.connected = false
            reject(new Error('WebSocket连接失败'))
          },
          onWebSocketError: (event) => {
            console.error('[ChatWebSocket] WebSocket错误:', event)
            this.handleConnectionError()
            reject(event)
          }
        })

        // 激活客户端
        this.client.activate()

      } catch (error) {
        console.error('[ChatWebSocket] 连接初始化失败:', error)
        reject(error)
      }
    })
  }

  /**
   * 断开WebSocket连接
   */
  disconnect() {
    if (this.client && this.connected) {
      this.client.deactivate()
      this.connected = false
      this.subscriptions.clear()
      this.messageHandlers.clear()
      console.log('[ChatWebSocket] 已断开连接')
    }
  }

  /**
   * 订阅咨询状态变更
   * @param {string} consultationNo - 问诊编号
   * @param {Function} statusHandler - 状态变更处理函数
   */
  subscribeToConsultationStatus(consultationNo, statusHandler) {
    if (!this.connected) {
      console.warn('[ChatWebSocket] 未连接，无法订阅状态变更')
      return
    }

    const destination = `/topic/consultation/status/${consultationNo}`
    const subscriptionKey = `status_${consultationNo}`

    // 添加状态处理器到集合中
    if (!this.messageHandlers.has(subscriptionKey)) {
      this.messageHandlers.set(subscriptionKey, new Set())
    }
    this.messageHandlers.get(subscriptionKey).add(statusHandler)

    // 如果还没有订阅，创建订阅
    if (!this.subscriptions.has(subscriptionKey)) {
      try {
        const subscription = this.client.subscribe(destination, (message) => {
          try {
            const statusData = JSON.parse(message.body)
            console.log('[ChatWebSocket] 收到状态变更:', statusData)
            
            // 调用所有注册的状态处理器
            const handlers = this.messageHandlers.get(subscriptionKey)
            if (handlers) {
              handlers.forEach(handler => {
                try {
                  handler(statusData)
                } catch (error) {
                  console.error('[ChatWebSocket] 状态处理器执行失败:', error)
                }
              })
            }
          } catch (error) {
            console.error('[ChatWebSocket] 状态变更消息解析失败:', error)
          }
        })

        this.subscriptions.set(subscriptionKey, {
          subscription,
          destination
        })

        console.log(`[ChatWebSocket] 成功订阅咨询状态变更: ${consultationNo}`)

      } catch (error) {
        console.error(`[ChatWebSocket] 订阅咨询状态变更失败: ${consultationNo}`, error)
      }
    } else {
      console.log(`[ChatWebSocket] 已存在状态订阅，添加新的处理器: ${consultationNo}`)
    }
  }

  /**
   * 订阅医生的咨询列表变更
   * @param {number} doctorId - 医生ID
   * @param {Function} listUpdateHandler - 列表更新处理函数
   */
  subscribeToDoctorConsultations(doctorId, listUpdateHandler) {
    if (!this.connected) {
      console.warn('[ChatWebSocket] 未连接，无法订阅医生咨询列表')
      return
    }

    const destination = `/topic/doctor/${doctorId}/consultations`
    const subscriptionKey = `doctor_${doctorId}_consultations`

    // 添加列表更新处理器到集合中
    if (!this.messageHandlers.has(subscriptionKey)) {
      this.messageHandlers.set(subscriptionKey, new Set())
    }
    this.messageHandlers.get(subscriptionKey).add(listUpdateHandler)

    // 如果还没有订阅，创建订阅
    if (!this.subscriptions.has(subscriptionKey)) {
      try {
        const subscription = this.client.subscribe(destination, (message) => {
          try {
            const updateData = JSON.parse(message.body)
            console.log('[ChatWebSocket] 收到医生咨询列表更新:', updateData)
            
            // 调用所有注册的列表更新处理器
            const handlers = this.messageHandlers.get(subscriptionKey)
            if (handlers) {
              handlers.forEach(handler => {
                try {
                  handler(updateData)
                } catch (error) {
                  console.error('[ChatWebSocket] 列表更新处理器执行失败:', error)
                }
              })
            }
          } catch (error) {
            console.error('[ChatWebSocket] 列表更新消息解析失败:', error)
          }
        })

        this.subscriptions.set(subscriptionKey, {
          subscription,
          destination
        })

        console.log(`[ChatWebSocket] 成功订阅医生咨询列表: ${doctorId}`)

      } catch (error) {
        console.error(`[ChatWebSocket] 订阅医生咨询列表失败: ${doctorId}`, error)
      }
    } else {
      console.log(`[ChatWebSocket] 已存在医生列表订阅，添加新的处理器: ${doctorId}`)
    }
  }

  /**
   * 订阅问诊聊天室
   * @param {string} consultationNo - 问诊编号
   * @param {Function} messageHandler - 消息处理函数
   */
  subscribeToConsultation(consultationNo, messageHandler) {
    if (!this.connected) {
      console.warn('[ChatWebSocket] 未连接，无法订阅')
      return
    }

    const destination = `/topic/consultation/${consultationNo}`
    const subscriptionKey = `consultation_${consultationNo}`

    // 添加消息处理器到集合中
    if (!this.messageHandlers.has(consultationNo)) {
      this.messageHandlers.set(consultationNo, new Set())
    }
    this.messageHandlers.get(consultationNo).add(messageHandler)

    // 如果还没有订阅，创建订阅
    if (!this.subscriptions.has(subscriptionKey)) {
      try {
        const subscription = this.client.subscribe(destination, (message) => {
          try {
            const messageData = JSON.parse(message.body)
            console.log('[ChatWebSocket] 收到聊天消息:', messageData)
            
            // 调用所有注册的消息处理器
            const handlers = this.messageHandlers.get(consultationNo)
            if (handlers) {
              handlers.forEach(handler => {
                try {
                  handler(messageData)
                } catch (error) {
                  console.error('[ChatWebSocket] 处理器执行失败:', error)
                }
              })
            }
          } catch (error) {
            console.error('[ChatWebSocket] 消息解析失败:', error)
          }
        })

        this.subscriptions.set(subscriptionKey, {
          subscription,
          destination
        })

        console.log(`[ChatWebSocket] 成功订阅问诊聊天室: ${consultationNo}`)

      } catch (error) {
        console.error(`[ChatWebSocket] 订阅问诊聊天室失败: ${consultationNo}`, error)
      }
    } else {
      console.log(`[ChatWebSocket] 已存在订阅，添加新的处理器: ${consultationNo}`)
    }
  }

  /**
   * 移除特定的消息处理器
   * @param {string} consultationNo - 问诊编号
   * @param {Function} messageHandler - 要移除的消息处理函数
   */
  removeMessageHandler(consultationNo, messageHandler) {
    const handlers = this.messageHandlers.get(consultationNo)
    if (handlers) {
      handlers.delete(messageHandler)
      
      // 如果没有处理器了，取消订阅
      if (handlers.size === 0) {
        this.messageHandlers.delete(consultationNo)
        this.unsubscribe(`consultation_${consultationNo}`)
      }
    }
  }

  /**
   * 取消订阅
   * @param {string} subscriptionKey - 订阅键
   */
  unsubscribe(subscriptionKey) {
    const subscriptionInfo = this.subscriptions.get(subscriptionKey)
    if (subscriptionInfo) {
      subscriptionInfo.subscription.unsubscribe()
      this.subscriptions.delete(subscriptionKey)
      
      // 清理对应的消息处理器
      const consultationNo = subscriptionKey.replace('consultation_', '')
      this.messageHandlers.delete(consultationNo)
      
      console.log(`[ChatWebSocket] 已取消订阅: ${subscriptionKey}`)
    }
  }

  /**
   * 发送聊天消息到指定问诊（通过WebSocket）
   * @param {string} consultationNo - 问诊编号
   * @param {Object} message - 消息对象
   */
  sendChatMessage(consultationNo, message) {
    if (!this.connected) {
      console.warn('[ChatWebSocket] 未连接，无法发送消息')
      return false
    }

    try {
      const destination = `/app/chat/${consultationNo}`
      this.client.publish({
        destination,
        body: JSON.stringify(message)
      })
      
      console.log(`[ChatWebSocket] 发送聊天消息成功: ${consultationNo}`, message)
      return true
    } catch (error) {
      console.error(`[ChatWebSocket] 发送聊天消息失败: ${consultationNo}`, error)
      return false
    }
  }

  /**
   * 重新订阅所有主题
   */
  resubscribeAll() {
    if (!this.connected) return

    console.log('[ChatWebSocket] 重新订阅所有主题')
    
    // 保存当前的消息处理器
    const handlersToRestore = new Map(this.messageHandlers)
    
    // 清理当前订阅
    this.subscriptions.clear()
    this.messageHandlers.clear()

    // 重新订阅
    for (const [consultationNo, handlers] of handlersToRestore) {
      try {
        // 为每个处理器重新订阅
        handlers.forEach(handler => {
          this.subscribeToConsultation(consultationNo, handler)
        })
      } catch (error) {
        console.error(`[ChatWebSocket] 重新订阅失败: consultation_${consultationNo}`, error)
      }
    }
  }

  /**
   * 处理连接错误
   */
  handleConnectionError() {
    if (this.reconnectAttempts < this.maxReconnectAttempts) {
      this.reconnectAttempts++
      console.log(`[ChatWebSocket] 尝试重连 (${this.reconnectAttempts}/${this.maxReconnectAttempts})`)
      
      setTimeout(() => {
        this.connect().catch(error => {
          console.error('[ChatWebSocket] 重连失败:', error)
        })
      }, this.reconnectDelay * this.reconnectAttempts)
    } else {
      console.error('[ChatWebSocket] 重连次数已达上限，停止重连')
    }
  }

  /**
   * 检查连接状态
   */
  isConnected() {
    return this.connected
  }

  /**
   * 获取当前订阅列表
   */
  getSubscriptions() {
    return Array.from(this.subscriptions.keys())
  }
}

// 创建全局实例
const chatWebSocketManager = new ChatWebSocketManager()

export default chatWebSocketManager 