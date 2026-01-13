import { Client } from '@stomp/stompjs'
import SockJS from 'sockjs-client'

/**
 * WebSocket连接管理类
 */
class WebSocketManager {
  constructor() {
    this.stompClient = null
    this.connected = false
    this.reconnectAttempts = 0
    this.maxReconnectAttempts = 5
    this.reconnectInterval = 3000
    this.subscriptions = new Map()
    this.messageHandlers = new Map()
  }

  /**
   * 连接WebSocket
   */
  connect() {
    return new Promise((resolve, reject) => {
      try {
        // 创建STOMP客户端
        this.stompClient = new Client({
          webSocketFactory: () => new SockJS('http://localhost:8080/ws'),
          debug: (str) => {
            // 可以选择性地显示调试信息
            // console.log(str)
          },
          reconnectDelay: this.reconnectInterval,
          heartbeatIncoming: 4000,
          heartbeatOutgoing: 4000,
        })

        // 连接成功回调
        this.stompClient.onConnect = (frame) => {
          console.log('WebSocket连接成功:', frame)
          this.connected = true
          this.reconnectAttempts = 0
          
          // 自动重新订阅之前的主题
          this.resubscribeAll()
          
          resolve(frame)
        }

        // 连接失败回调
        this.stompClient.onStompError = (frame) => {
          console.error('WebSocket连接失败:', frame)
          this.connected = false
          this.handleConnectionError()
          reject(new Error('STOMP error: ' + frame.headers['message']))
        }

        // 连接断开回调
        this.stompClient.onWebSocketClose = (event) => {
          console.log('WebSocket连接断开:', event)
          this.connected = false
        }

        // 开始连接
        this.stompClient.activate()
      } catch (error) {
        console.error('WebSocket初始化失败:', error)
        reject(error)
      }
    })
  }

  /**
   * 断开WebSocket连接
   */
  disconnect() {
    if (this.stompClient && this.connected) {
      this.stompClient.deactivate()
      console.log('WebSocket已断开连接')
      this.connected = false
    }
  }

  /**
   * 订阅主题
   */
  subscribe(destination, handler, handlerName = 'default') {
    if (!this.connected) {
      console.warn('WebSocket未连接，尝试重新连接...')
      this.connect().then(() => {
        this.subscribe(destination, handler, handlerName)
      })
      return null
    }

    try {
      const subscription = this.stompClient.subscribe(destination, (message) => {
        try {
          const data = JSON.parse(message.body)
          handler(data)
        } catch (error) {
          console.error('解析WebSocket消息失败:', error)
        }
      })

      // 保存订阅信息用于重连时重新订阅
      this.subscriptions.set(destination, { handler, handlerName })
      console.log(`订阅主题成功: ${destination}`)
      
      return subscription
    } catch (error) {
      console.error(`订阅主题失败: ${destination}`, error)
      return null
    }
  }

  /**
   * 取消订阅
   */
  unsubscribe(destination) {
    if (this.subscriptions.has(destination)) {
      this.subscriptions.delete(destination)
      console.log(`取消订阅: ${destination}`)
    }
  }

  /**
   * 重新订阅所有主题
   */
  resubscribeAll() {
    this.subscriptions.forEach(({ handler }, destination) => {
      this.subscribe(destination, handler)
    })
  }

  /**
   * 处理连接错误和重连
   */
  handleConnectionError() {
    if (this.reconnectAttempts < this.maxReconnectAttempts) {
      this.reconnectAttempts++
      console.log(`尝试重连WebSocket (${this.reconnectAttempts}/${this.maxReconnectAttempts})`)
      
      setTimeout(() => {
        this.connect().catch(() => {
          // 重连失败，继续尝试
        })
      }, this.reconnectInterval * this.reconnectAttempts)
    } else {
      console.error('WebSocket重连次数超过限制，停止重连')
    }
  }

  /**
   * 发送消息
   */
  send(destination, message) {
    if (!this.connected) {
      console.warn('WebSocket未连接，无法发送消息')
      return false
    }

    try {
      this.stompClient.publish({
        destination: destination,
        body: JSON.stringify(message)
      })
      return true
    } catch (error) {
      console.error('发送WebSocket消息失败:', error)
      return false
    }
  }

  /**
   * 检查连接状态
   */
  isConnected() {
    return this.connected
  }
}

// 创建全局WebSocket管理实例
const webSocketManager = new WebSocketManager()

export default webSocketManager 