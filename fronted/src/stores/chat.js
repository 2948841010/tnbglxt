import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import chatWebSocketManager from '@/utils/chatWebSocket'

export const useChatStore = defineStore('chat', () => {
  // 未读消息计数 Map<consultationNo, count>
  const unreadCounts = ref(new Map())
  
  // 当前活跃的聊天会话
  const activeConsultationNo = ref(null)
  
  // 已连接的WebSocket状态
  const wsConnected = ref(false)
  
  // 消息更新回调 Map<consultationNo, Set<callbacks>>
  const messageUpdateCallbacks = ref(new Map())

  // 计算总未读消息数
  const totalUnreadCount = computed(() => {
    let total = 0
    for (const count of unreadCounts.value.values()) {
      total += count
    }
    return total
  })

  // 获取特定问诊的未读消息数
  const getUnreadCount = (consultationNo) => {
    return unreadCounts.value.get(consultationNo) || 0
  }

  // 设置未读消息数
  const setUnreadCount = (consultationNo, count) => {
    if (count <= 0) {
      unreadCounts.value.delete(consultationNo)
    } else {
      unreadCounts.value.set(consultationNo, count)
    }
  }

  // 增加未读消息数
  const incrementUnreadCount = (consultationNo, force = false) => {
    // 如果force为true，强制增加计数（用于咨询管理页面）
    if (force || activeConsultationNo.value !== consultationNo) {
    const current = unreadCounts.value.get(consultationNo) || 0
    unreadCounts.value.set(consultationNo, current + 1)
      console.log(`[ChatStore] 未读计数增加: ${consultationNo} -> ${current + 1}`)
    }
  }

  // 清空特定问诊的未读消息
  const clearUnreadCount = (consultationNo) => {
    unreadCounts.value.delete(consultationNo)
  }

  // 清空所有未读消息
  const clearAllUnreadCount = () => {
    unreadCounts.value.clear()
  }

  // 设置当前活跃的聊天会话
  const setActiveConsultation = (consultationNo) => {
    activeConsultationNo.value = consultationNo
    // 当进入聊天页面时，清空该问诊的未读消息
    if (consultationNo) {
      clearUnreadCount(consultationNo)
    }
  }

  // 初始化WebSocket连接和全局消息监听
  const initializeWebSocket = async () => {
    try {
      if (!chatWebSocketManager.isConnected()) {
        await chatWebSocketManager.connect()
      }
      wsConnected.value = true
      console.log('[ChatStore] WebSocket连接已建立')
    } catch (error) {
      console.error('[ChatStore] WebSocket连接失败:', error)
      wsConnected.value = false
    }
  }

  // 订阅问诊消息（用于列表页面监听）
  const subscribeToConsultationUpdates = (consultationNo) => {
    if (!wsConnected.value) {
      console.warn('[ChatStore] WebSocket未连接，无法订阅')
      return
    }

    // 为列表页面订阅消息更新
    chatWebSocketManager.subscribeToConsultation(consultationNo, (message) => {
      handleGlobalMessage(consultationNo, message)
    })

    // 订阅咨询状态变更
    chatWebSocketManager.subscribeToConsultationStatus(consultationNo, (statusData) => {
      handleConsultationStatusChange(consultationNo, statusData)
    })
  }

  // 订阅医生咨询列表更新
  const subscribeToDoctorConsultationList = (doctorId, updateHandler) => {
    if (!wsConnected.value) {
      console.warn('[ChatStore] WebSocket未连接，无法订阅医生列表')
      return
    }

    chatWebSocketManager.subscribeToDoctorConsultations(doctorId, updateHandler)
  }

  // 处理全局消息（用于未读计数和消息更新）
  const handleGlobalMessage = (consultationNo, message) => {
    console.log('[ChatStore] 收到全局消息:', consultationNo, message)
    
    // 如果当前不在该问诊的聊天页面，增加未读计数
    if (activeConsultationNo.value !== consultationNo) {
      incrementUnreadCount(consultationNo)
    }
    
    // 通知所有注册的消息更新回调
    const callbacks = messageUpdateCallbacks.value.get(consultationNo)
    if (callbacks) {
      callbacks.forEach(callback => {
        try {
          callback(message)
        } catch (error) {
          console.error('[ChatStore] 消息更新回调执行失败:', error)
        }
      })
    }
  }
  
  // 注册消息更新回调
  const registerMessageUpdateCallback = (consultationNo, callback) => {
    if (!messageUpdateCallbacks.value.has(consultationNo)) {
      messageUpdateCallbacks.value.set(consultationNo, new Set())
    }
    messageUpdateCallbacks.value.get(consultationNo).add(callback)
    
    console.log(`[ChatStore] 注册消息更新回调: ${consultationNo}`)
  }
  
  // 取消消息更新回调
  const unregisterMessageUpdateCallback = (consultationNo, callback) => {
    const callbacks = messageUpdateCallbacks.value.get(consultationNo)
    if (callbacks) {
      callbacks.delete(callback)
      if (callbacks.size === 0) {
        messageUpdateCallbacks.value.delete(consultationNo)
      }
    }
    
    console.log(`[ChatStore] 取消消息更新回调: ${consultationNo}`)
  }

  // 处理咨询状态变更
  const handleConsultationStatusChange = (consultationNo, statusData) => {
    console.log('[ChatStore] 收到状态变更:', consultationNo, statusData)
    
    // 如果咨询已完成或已取消，清空未读消息
    if (statusData.status === 3 || statusData.status === 4) {
      clearUnreadCount(consultationNo)
    }
  }

  // 批量订阅多个问诊的消息更新
  const subscribeToMultipleConsultations = (consultationNos) => {
    consultationNos.forEach(consultationNo => {
      subscribeToConsultationUpdates(consultationNo)
    })
  }

  // 取消订阅
  const unsubscribeFromConsultation = (consultationNo) => {
    chatWebSocketManager.unsubscribe(`consultation_${consultationNo}`)
  }

  // 断开WebSocket连接
  const disconnectWebSocket = () => {
    chatWebSocketManager.disconnect()
    wsConnected.value = false
  }

  // 重置store状态
  const reset = () => {
    unreadCounts.value.clear()
    activeConsultationNo.value = null
    wsConnected.value = false
    messageUpdateCallbacks.value.clear()
  }

  return {
    // 状态
    unreadCounts,
    activeConsultationNo,
    wsConnected,
    messageUpdateCallbacks,
    
    // 计算属性
    totalUnreadCount,
    
    // 方法
    getUnreadCount,
    setUnreadCount,
    incrementUnreadCount,
    clearUnreadCount,
    clearAllUnreadCount,
    setActiveConsultation,
    initializeWebSocket,
    subscribeToConsultationUpdates,
    subscribeToMultipleConsultations,
    subscribeToDoctorConsultationList,
    unsubscribeFromConsultation,
    disconnectWebSocket,
    handleGlobalMessage,
    handleConsultationStatusChange,
    registerMessageUpdateCallback,
    unregisterMessageUpdateCallback,
    reset
  }
}) 