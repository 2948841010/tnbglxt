<template>
  <div class="consultation-chat-page">
    <!-- 聊天头部 -->
    <div class="chat-header">
      <div class="header-left">
        <el-button 
          type="text" 
          size="large" 
          @click="goBack"
          class="back-btn">
          <el-icon><ArrowLeft /></el-icon>
        </el-button>
        
        <UserAvatar 
          :src="getCurrentChatTargetInfo()?.avatar"
          :username="getCurrentChatTargetInfo()?.name"
          size="medium"
        />
        
        <div class="doctor-info">
          <div class="doctor-name">{{ getCurrentChatTargetInfo()?.name }}</div>
          <div class="doctor-desc">
            {{ getCurrentChatTargetDesc() }}
          </div>
        </div>
      </div>
      
      <div class="header-right">
        <el-tag :type="getStatusType(consultationInfo?.status)" size="small">
          {{ getStatusText(consultationInfo?.status) }}
        </el-tag>
      </div>
    </div>

    <!-- 聊天内容区域 -->
    <div class="chat-body" ref="chatBodyRef">
      <div class="chat-messages" v-if="messages.length > 0">
        <div 
          v-for="message in messages" 
          :key="message.messageId"
          :class="['message-item', isCurrentUserMessage(message) ? 'user-message' : 'doctor-message']">
          
          <div class="message-content">
            <div class="message-bubble">
              <!-- 文本消息 -->
              <div v-if="message.messageType === 'text'" class="text-content">
                {{ message.content }}
              </div>
              
              <!-- 🖼️ 图片消息 -->
              <div v-else-if="message.messageType === 'image'" class="image-content">
                <el-image 
                  :src="message.content" 
                  :preview-src-list="[message.content]"
                  fit="cover"
                  class="chat-image"
                  :loading="loading"
                  @error="handleImageError"
                >
                  <template #error>
                    <div class="image-error">
                      <el-icon><Picture /></el-icon>
                      <span>图片加载失败</span>
              </div>
                  </template>
                </el-image>
              </div>
              
              <!-- 🔥 文件消息 -->
              <div v-else-if="message.messageType === 'file'" class="file-content">
                <div class="file-info">
                  <div class="file-icon">
                    <el-icon><Document /></el-icon>
                  </div>
                  <div class="file-details">
                    <div class="file-name">{{ message.fileName || '未知文件' }}</div>
                    <div class="file-size">{{ formatFileSize(message.fileSize) }}</div>
                  </div>
                  <el-button 
                    type="primary" 
                    size="small" 
                    :icon="Download"
                    @click="handleFileDownload(message.content, message.fileName)"
                    class="download-button">
                    下载
                  </el-button>
                </div>
              </div>
              
              <!-- 其他消息类型 -->
              <div v-else class="other-content">
                <el-icon><Paperclip /></el-icon>
                <span>{{ message.content }}</span>
              </div>
            </div>
            
            <div class="message-info">
              <span class="message-time">{{ formatTime(message.sendTime) }}</span>
              <span v-if="isCurrentUserMessage(message)" class="message-status">
                <el-icon v-if="message.isRead" class="read-icon"><Select /></el-icon>
                <el-icon v-else class="sent-icon"><Check /></el-icon>
              </span>
            </div>
          </div>
        </div>
      </div>

      <!-- 空状态 -->
      <div v-if="messages.length === 0" class="empty-messages">
        <el-icon class="empty-icon"><ChatDotRound /></el-icon>
        <p>{{ getEmptyMessage() }}</p>
      </div>
    </div>

    <!-- 输入区域 -->
    <div class="chat-footer" v-if="consultationInfo?.status !== 3">
      <div class="input-container">
        <!-- 图片上传按钮 -->
        <el-button 
          type="info" 
          :icon="Picture"
          circle
          @click="handleImageUploadClick"
          :disabled="consultationInfo?.status === 1 || uploading"
          class="upload-button image-upload-button"
          title="发送图片"
        />
        
        <!-- 🔥 文件上传按钮 -->
        <el-button 
          type="warning" 
          :icon="Paperclip"
          circle
          @click="handleFileUploadClick"
          :disabled="consultationInfo?.status === 1 || uploading"
          class="upload-button file-upload-button"
          title="发送文件"
        />
        
        <!-- 隐藏的图片文件输入框 -->
        <input
          ref="imageFileInput"
          type="file"
          accept="image/*"
          style="display: none"
          @change="handleImageFileChange"
        />
        
        <!-- 🔥 隐藏的通用文件输入框 -->
        <input
          ref="fileInput"
          type="file"
          style="display: none"
          @change="handleFileChange"
        />
        
        <!-- 消息输入框 -->
      <el-input
        v-model="messageInput"
        placeholder="输入消息..."
        @keyup.enter.exact="sendMessage"
        :disabled="sending || consultationInfo?.status === 1"
        clearable
        class="message-input"
      />
        
        <!-- 发送按钮 -->
      <el-button 
        type="primary" 
        @click="sendMessage"
        :loading="sending"
        :disabled="!messageInput.trim() || consultationInfo?.status === 1"
        class="send-button">
        {{ consultationInfo?.status === 1 ? '等待接诊' : '发送' }}
      </el-button>
      </div>
      
      <!-- 🔥 文件上传进度 -->
      <div v-if="uploading || uploadingFile" class="upload-progress-container">
        <el-progress 
          :percentage="uploading ? uploadProgress : fileUploadProgress" 
          :status="(uploading ? uploadProgress : fileUploadProgress) === 100 ? 'success' : ''"
          :stroke-width="6"
        />
        <span class="progress-text">
          {{ 
            (uploading ? uploadProgress : fileUploadProgress) === 100 ? '上传完成' : 
            uploading ? '正在上传图片...' : '正在上传文件...' 
          }}
        </span>
      </div>
    </div>



    <!-- 问诊结束状态 -->
    <div v-else-if="consultationInfo?.status === 3" class="consultation-ended-footer">
      <div class="ended-content">
        <el-icon class="ended-icon"><CircleCheck /></el-icon>
        <span class="ended-text">问诊已结束</span>
        <el-button type="primary" @click="goToMyConsultations" size="small">{{ getBackButtonText() }}</el-button>
      </div>
    </div>

    <!-- 医生端健康数据悬浮球 -->
    <HealthFloatBall 
      v-if="showHealthFloatBall"
      :patient-id="getPatientUserId()"
      :patient-info="consultationInfo?.patientInfo"
    />

    <!-- 🌟 问诊评价组件 -->
    <ConsultationRating
      v-model:visible="showRatingDialog"
      :consultation-no="route.params.id"
      :doctor-info="consultationInfo?.doctorInfo"
      @rating-submitted="handleRatingSubmitted"
    />

  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onBeforeUnmount, nextTick, computed, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { 
  ArrowLeft, 
  ChatDotRound, 
  Select, 
  Check,
  CircleCheck,
  Picture,
  Paperclip,
  Document,
  Download
} from '@element-plus/icons-vue'
import { getConsultationDetail, sendMessage as sendMessageAPI, uploadChatImage, uploadChatFile, downloadChatFile } from '@/api/chat'
import { submitConsultationRating, getConsultationRating } from '@/api/consultation'
import chatWebSocketManager from '@/utils/chatWebSocket'
import { useUserStore } from '@/stores/user'
import { useChatStore } from '@/stores/chat'
import UserAvatar from '@/components/common/UserAvatar.vue'
import HealthFloatBall from '@/components/common/HealthFloatBall.vue'
import ConsultationRating from '@/components/ConsultationRating.vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const chatStore = useChatStore()

// 响应式数据
const consultationInfo = ref(null)
const messages = ref([])
const messageInput = ref('')
const sending = ref(false)
const loading = ref(false)
const chatBodyRef = ref(null)

// 🖼️ 图片上传相关
const imageFileInput = ref(null)
const uploading = ref(false)
const uploadProgress = ref(0)

// 🔥 通用文件上传相关
const fileInput = ref(null)
const uploadingFile = ref(false)
const fileUploadProgress = ref(0)

// 🌟 评价相关
const showRatingDialog = ref(false)
const consultationRating = ref(null)

// 判断消息是否是当前用户发送的
const isCurrentUserMessage = (message) => {
  const currentUserType = userStore.userInfo?.userType
  // userType: 0-普通用户, 1-医生
  if (currentUserType === 1) {
    // 医生端：医生发送的消息是自己的
    return message.senderType === 'doctor'
  } else {
    // 患者端：患者发送的消息是自己的
    return message.senderType === 'patient'
  }
}

// 获取当前聊天对象的信息
const getCurrentChatTargetInfo = () => {
  const currentUserType = userStore.userInfo?.userType
  if (currentUserType === 1) {
    // 医生端：显示患者信息
    return consultationInfo.value?.patientInfo
  } else {
    // 患者端：显示医生信息
    return consultationInfo.value?.doctorInfo
  }
}

// 获取当前聊天对象的描述
const getCurrentChatTargetDesc = () => {
  const currentUserType = userStore.userInfo?.userType
  if (currentUserType === 1) {
    // 医生端：显示患者的性别和年龄
    const patientInfo = consultationInfo.value?.patientInfo
    if (patientInfo) {
      return `${patientInfo.gender || '未知'} · ${patientInfo.age || '未知'}岁`
    }
    return '患者信息'
  } else {
    // 患者端：显示医生的科室和职称
    const doctorInfo = consultationInfo.value?.doctorInfo
    if (doctorInfo) {
      return `${doctorInfo.department || ''} · ${doctorInfo.title || ''}`
    }
    return '医生信息'
  }
}

// 获取空状态消息
const getEmptyMessage = () => {
  const currentUserType = userStore.userInfo?.userType
  if (currentUserType === 1) {
    // 医生端
    return '等待患者发起对话'
  } else {
    // 患者端
    return '开始与医生的对话吧'
  }
}

// 获取返回按钮文本
const getBackButtonText = () => {
  const currentUserType = userStore.userInfo?.userType
  if (currentUserType === 1) {
    // 医生端
    return '查看更多回复'
  } else {
    // 患者端
    return '查看更多咨询'
  }
}

// 获取问诊详情
const loadConsultationDetail = async () => {
  loading.value = true
  try {
    const consultationNo = route.params.id
    const response = await getConsultationDetail(consultationNo)
    
    console.log('问诊详情响应:', response)
    if (response.code === 200) {
      consultationInfo.value = response.data
      messages.value = consultationInfo.value.messages || []
      
      // 滚动到底部
      await nextTick()
      scrollToBottom()
      
    } else {
      ElMessage.error(response.message || '获取问诊详情失败')
      router.push('/consultation/my')
    }
  } catch (error) {
    console.error('获取问诊详情失败:', error)
    ElMessage.error('网络异常，请稍后重试')
  } finally {
    loading.value = false
  }
  
  // 🌟 页面加载完成后检查是否需要显示评价对话框
  setTimeout(() => {
    checkAndShowRatingDialog()
  }, 1000)
}

// 发送消息
const sendMessage = async () => {
  const content = messageInput.value.trim()
  if (!content || sending.value) return
  
  // 检查咨询状态
  if (consultationInfo.value?.status !== 2) {
    ElMessage.warning('当前状态不允许发送消息')
    return
  }

  sending.value = true
  try {
    const response = await sendMessageAPI({
      consultationNo: route.params.id,
      messageType: 'text',
      content: content
    })

    if (response.code === 200) {
      const sentContent = content // 保存发送的内容
      messageInput.value = ''
      
      // 🔥 立即在本地添加文本消息，让发送者立即看到
      const localTextMessage = {
        messageId: `temp_${Date.now()}`, // 临时ID
        consultationNo: route.params.id,
        messageType: 'text',
        content: sentContent,
        sendTime: new Date().toISOString(),
        senderType: userStore.userInfo?.userType === 1 ? 'doctor' : 'patient',
        senderId: userStore.userInfo?.id,
        isRead: false
      }
      
      // 添加到消息列表
      messages.value.push(localTextMessage)
      
      // 滚动到底部
      await nextTick()
      scrollToBottom()
      
      console.log('✅ 文本消息已立即添加到本地列表')
      
    } else {
      ElMessage.error(response.message || '发送失败')
    }
  } catch (error) {
    console.error('发送消息失败:', error)
    ElMessage.error('发送失败，请重试')
  } finally {
    sending.value = false
  }
}

// 🖼️ 图片上传相关方法
const handleImageUploadClick = () => {
  if (consultationInfo.value?.status !== 2) {
    ElMessage.warning('当前状态不允许发送图片')
    return
  }
  
  if (uploading.value || uploadingFile.value) {
    ElMessage.warning('正在上传文件，请稍候')
    return
  }
  
  imageFileInput.value?.click()
}

// 🔥 通用文件上传相关方法
const handleFileUploadClick = () => {
  if (consultationInfo.value?.status !== 2) {
    ElMessage.warning('当前状态不允许发送文件')
    return
  }
  
  if (uploading.value || uploadingFile.value) {
    ElMessage.warning('正在上传文件，请稍候')
    return
  }
  
  fileInput.value?.click()
}

const handleImageFileChange = async (event) => {
  const file = event.target.files[0]
  if (!file) return

  // 验证文件类型
  if (!file.type.startsWith('image/')) {
    ElMessage.error('请选择图片文件')
    return
  }

  // 验证文件大小（10MB）
  if (file.size > 10 * 1024 * 1024) {
    ElMessage.error('图片大小不能超过10MB')
    return
  }

  await uploadImage(file)
  
  // 清空input的值，允许重复选择同一个文件
  event.target.value = ''
}

// 🔥 通用文件处理
const handleFileChange = async (event) => {
  const file = event.target.files[0]
  if (!file) return

  // 验证文件大小（50MB）
  if (file.size > 50 * 1024 * 1024) {
    ElMessage.error('文件大小不能超过50MB')
    return
  }

  await uploadFile(file)
  
  // 清空input的值，允许重复选择同一个文件
  event.target.value = ''
}

const uploadImage = async (file) => {
  try {
    uploading.value = true
    uploadProgress.value = 0

    console.log('📸 开始上传图片:', file.name)

    const response = await uploadChatImage(
      file, 
      route.params.id,
      (progressEvent) => {
        if (progressEvent.total) {
          const progress = Math.round((progressEvent.loaded * 100) / progressEvent.total)
          uploadProgress.value = Math.min(progress, 95) // 最多显示95%
        }
      }
    )

    // 上传完成，显示100%
    uploadProgress.value = 100

    console.log('📸 图片上传响应:', response)

    if (response.code === 200) {
      // 发送图片消息
      const imageMessage = {
        consultationNo: route.params.id,
        messageType: 'image',
        content: response.data.fileUrl, // 图片URL
        fileName: file.name,
        fileSize: file.size
      }

      const messageResponse = await sendMessageAPI(imageMessage)
      
      if (messageResponse.code === 200) {
        ElMessage.success('图片发送成功')
        
        // 🔥 立即在本地添加图片消息，让发送者立即看到
        const localImageMessage = {
          messageId: `temp_${Date.now()}`, // 临时ID
          consultationNo: route.params.id,
          messageType: 'image',
          content: response.data.fileUrl,
          sendTime: new Date().toISOString(),
          senderType: userStore.userInfo?.userType === 1 ? 'doctor' : 'patient',
          senderId: userStore.userInfo?.id,
          isRead: false
        }
        
        // 添加到消息列表
        messages.value.push(localImageMessage)
        
        // 滚动到底部
        await nextTick()
        scrollToBottom()
        
        console.log('✅ 图片消息已立即添加到本地列表')
        
      } else {
        ElMessage.error(messageResponse.message || '图片发送失败')
      }
    } else {
      throw new Error(response.message || '图片上传失败')
    }

  } catch (error) {
    console.error('图片上传失败:', error)
    
    if (error.code === 'ECONNABORTED' || error.message.includes('timeout')) {
      ElMessage.warning('图片上传时间较长，请稍等片刻')
    } else {
      ElMessage.error('图片上传失败: ' + (error.response?.data?.message || error.message))
    }
  } finally {
    uploading.value = false
    uploadProgress.value = 0
  }
}

// 🔥 通用文件上传
const uploadFile = async (file) => {
  try {
    uploadingFile.value = true
    fileUploadProgress.value = 0

    console.log('📁 开始上传文件:', file.name)

    const response = await uploadChatFile(
      file, 
      route.params.id,
      (progressEvent) => {
        if (progressEvent.total) {
          const progress = Math.round((progressEvent.loaded * 100) / progressEvent.total)
          fileUploadProgress.value = Math.min(progress, 95) // 最多显示95%
        }
      }
    )

    // 上传完成，显示100%
    fileUploadProgress.value = 100

    console.log('📁 文件上传响应:', response)

    if (response.code === 200) {
      // 判断文件类型
      const isImage = file.type.startsWith('image/')
      const messageType = isImage ? 'image' : 'file'
      
      // 发送文件消息
      const fileMessage = {
        consultationNo: route.params.id,
        messageType: messageType,
        content: response.data.fileUrl, // 文件URL
        fileName: file.name,
        fileSize: file.size,
        contentType: file.type
      }

      const messageResponse = await sendMessageAPI(fileMessage)
      
      if (messageResponse.code === 200) {
        ElMessage.success(isImage ? '图片发送成功' : '文件发送成功')
        
        // 🔥 立即在本地添加文件消息，让发送者立即看到
        const localFileMessage = {
          messageId: `temp_${Date.now()}`, // 临时ID
          consultationNo: route.params.id,
          messageType: messageType,
          content: response.data.fileUrl,
          fileName: file.name,
          fileSize: file.size,
          contentType: file.type,
          sendTime: new Date().toISOString(),
          senderType: userStore.userInfo?.userType === 1 ? 'doctor' : 'patient',
          senderId: userStore.userInfo?.id,
          isRead: false
        }
        
        // 添加到消息列表
        messages.value.push(localFileMessage)
        
        // 滚动到底部
        await nextTick()
        scrollToBottom()
        
        console.log('✅ 文件消息已立即添加到本地列表')
        
      } else {
        ElMessage.error(messageResponse.message || '文件发送失败')
      }
    } else {
      throw new Error(response.message || '文件上传失败')
    }

  } catch (error) {
    console.error('文件上传失败:', error)
    
    if (error.code === 'ECONNABORTED' || error.message.includes('timeout')) {
      ElMessage.warning('文件上传时间较长，请稍等片刻')
    } else {
      ElMessage.error('文件上传失败: ' + (error.response?.data?.message || error.message))
    }
  } finally {
    uploadingFile.value = false
    fileUploadProgress.value = 0
  }
}

// 🔥 文件下载处理
const handleFileDownload = async (fileUrl, fileName) => {
  try {
    console.log('📥 开始下载文件:', fileName)
    await downloadChatFile(fileUrl, fileName)
    ElMessage.success('文件下载完成')
  } catch (error) {
    console.error('文件下载失败:', error)
    ElMessage.error('文件下载失败')
  }
}

// 🔥 格式化文件大小
const formatFileSize = (bytes) => {
  if (!bytes) return '0 B'
  
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  
  return Math.round((bytes / Math.pow(k, i)) * 100) / 100 + ' ' + sizes[i]
}

// 图片加载错误处理
const handleImageError = () => {
  console.warn('聊天图片加载失败')
}


// WebSocket消息处理
const handleNewMessage = (message) => {
  // 🔥 关键修复：如果是当前用户自己发送的消息，不添加（因为发送时已经本地添加了）
  const isMyMessage = isCurrentUserMessage(message)
  if (isMyMessage) {
    console.log('🚫 跳过自己发送的消息（已在本地添加）:', message.messageType, 
      message.messageType === 'file' ? message.fileName : message.content?.substring(0, 50))
    return
  }
  
  // 🔥 防止重复消息：检查是否已存在相同内容的消息
  const isDuplicate = messages.value.some(existingMessage => {
    // 基于发送者、消息类型、内容和时间判断是否重复
    const isSameSender = existingMessage.senderId === message.senderId
    const isSameType = existingMessage.messageType === message.messageType
    const isSameContent = existingMessage.content === message.content
    
    // 如果是图片或文件消息，主要比较URL
    if (message.messageType === 'image' || message.messageType === 'file') {
      return isSameSender && isSameType && isSameContent
    }
    
    // 文本消息比较内容和时间（允许5秒内的时间差）
    if (message.messageType === 'text') {
      const existingTime = new Date(existingMessage.sendTime).getTime()
      const newTime = new Date(message.sendTime).getTime()
      const timeDiff = Math.abs(existingTime - newTime)
      
      return isSameSender && isSameType && isSameContent && timeDiff < 5000
    }
    
    return false
  })
  
  if (!isDuplicate) {
  messages.value.push(message)
    console.log('📨 新消息已添加:', message.messageType, 
      message.messageType === 'file' ? message.fileName : message.content?.substring(0, 50))
  } else {
    console.log('🚫 跳过重复消息:', message.messageType, 
      message.messageType === 'file' ? message.fileName : message.content?.substring(0, 50))
  }
  
  nextTick(() => {
    scrollToBottom()
  })
}

// 处理咨询状态变化
const handleConsultationStatusChange = (statusUpdate) => {
  console.log('🔄 收到咨询状态变化通知:', statusUpdate)
  
  if (statusUpdate.consultationNo === route.params.id) {
    const oldStatus = consultationInfo.value?.status
    const newStatus = statusUpdate.newStatus
    
    console.log(`咨询状态变化: ${oldStatus} -> ${newStatus}`)
    
    // 更新咨询信息的状态
    if (consultationInfo.value) {
      consultationInfo.value.status = newStatus
      
      // 如果有完整的咨询信息，也更新一下
      if (statusUpdate.consultation) {
        Object.assign(consultationInfo.value, statusUpdate.consultation)
      }
    }
    
    // 根据状态变化显示相应的提示消息
    const currentUserType = userStore.userInfo?.userType
    
    if (newStatus === 2 && oldStatus === 1) {
      // 从待接诊变为进行中：医生已接受
      if (currentUserType === 0) { // 患者端
        ElMessage.success('医生已接受您的咨询，可以开始对话了！')
      }
    } else if (newStatus === 3) {
      // 变为已完成：问诊结束
      if (currentUserType === 0) { // 患者端
        ElMessage.info('问诊已结束，感谢您的使用！')
        // 延迟弹出评价对话框，让用户看到完成提示
        setTimeout(() => {
          checkAndShowRatingDialog()
        }, 1500)
      } else { // 医生端
        ElMessage.info('问诊已结束')
      }
    } else if (newStatus === 4) {
      // 变为已取消
      ElMessage.warning('咨询已被取消')
    }
    
    // 滚动到底部以确保用户看到最新状态
    nextTick(() => {
      scrollToBottom()
    })
  }
}

// 滚动到底部
const scrollToBottom = () => {
  if (chatBodyRef.value) {
    chatBodyRef.value.scrollTop = chatBodyRef.value.scrollHeight
  }
}

// 🌟 检查并显示评价对话框
const checkAndShowRatingDialog = async () => {
  try {
    // 只有患者可以评价
    if (userStore.userInfo?.userType !== 0) return
    
    // 只有已完成的问诊可以评价
    if (consultationInfo.value?.status !== 3) return
    
    // 检查是否已经评价过
    const response = await getConsultationRating(route.params.id)
    if (response.code === 200 && response.data) {
      // 已经评价过了，不再显示评价对话框
      consultationRating.value = response.data
      return
    }
    
    // 显示评价对话框
    showRatingDialog.value = true
  } catch (error) {
    console.error('检查评价状态失败:', error)
  }
}

// 🌟 处理评价提交成功
const handleRatingSubmitted = (ratingData) => {
  consultationRating.value = ratingData
  ElMessage.success('感谢您的评价！')
}

// 格式化时间
const formatTime = (timeStr) => {
  if (!timeStr) return ''
  const date = new Date(timeStr)
  return date.toLocaleString('zh-CN', {
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

// 获取状态类型
const getStatusType = (status) => {
  const types = {
    1: 'warning', // 待接诊
    2: 'primary', // 进行中
    3: 'success', // 已完成
    4: 'danger'   // 已取消
  }
  return types[status] || 'info'
}

// 获取状态文本
const getStatusText = (status) => {
  const texts = {
    1: '待接诊',
    2: '进行中',
    3: '已完成',
    4: '已取消'
  }
  return texts[status] || '未知'
}

// 返回上一页
const goBack = () => {
  router.back()
}

// 跳转到咨询记录
const goToMyConsultations = () => {
  const currentUserType = userStore.userInfo?.userType
  if (currentUserType === 1) {
    // 医生端：跳转到我的回复页面
    router.push('/doctor/consultation/replies')
  } else {
    // 患者端：跳转到我的咨询页面
    router.push('/consultation/my')
  }
}

// 计算属性：是否显示健康数据悬浮球
const showHealthFloatBall = computed(() => {
  const currentUserType = userStore.userInfo?.userType
  // 只在医生端（userType === 1）且有患者信息时显示
  return currentUserType === 1 && consultationInfo.value?.patientInfo
})

// 获取患者的用户ID
const getPatientUserId = () => {
  const patientInfo = consultationInfo.value?.patientInfo
  const patientId = consultationInfo.value?.patientId // 真正的患者用户ID
  
  console.log('获取患者ID调试信息:', {
    consultationInfo: consultationInfo.value,
    patientInfo: patientInfo,
    patientId: patientId  // 这个才是真正的用户ID
  })
  
  // 首先尝试获取正确的患者用户ID
  const userId = patientId ||  // ConsultationChat.patientId 是患者的真实用户ID
                consultationInfo.value?.patientUserId ||
                consultationInfo.value?.patient_id ||
                patientInfo?.userId ||  // 这个字段实际不存在，但保留作为fallback
                patientInfo?.id || 
                patientInfo?.patientId ||
                patientInfo?.user_id ||
                1 // 最后的默认值
                
  console.log('最终获取的患者ID:', userId)
  
  if (userId === patientId && patientId) {
    console.log('✅ 成功获取患者真实用户ID:', patientId)
  } else if (userId !== patientId) {
    console.warn('⚠️ 使用备用方式获取患者ID，可能不准确. 预期ID:', patientId, '实际使用ID:', userId)
  }
  
  return userId
}

// 初始化咨询页面的方法
const initializeConsultationPage = async (consultationId = null) => {
  const currentId = consultationId || route.params.id
  console.log('🔄 初始化咨询页面:', currentId)
  
  // 重新加载咨询详情
  await loadConsultationDetail()
  
  // 设置当前活跃的聊天会话（这会清空未读消息）
  chatStore.setActiveConsultation(route.params.id)
  
  // 初始化WebSocket连接
  try {
    await chatStore.initializeWebSocket()
    
    // 订阅当前问诊的消息
    if (consultationInfo.value) {
      chatWebSocketManager.subscribeToConsultation(route.params.id, handleNewMessage)
      
      // 🔥 新增：订阅咨询状态变化
      chatWebSocketManager.subscribeToConsultationStatus(route.params.id, handleConsultationStatusChange)
      console.log('✅ 已订阅咨询状态变化:', route.params.id)
    }
  } catch (error) {
    console.error('WebSocket连接失败:', error)
  }
}

// 清理指定咨询的订阅
const cleanupConsultationSubscriptions = (consultationId) => {
  if (consultationId && chatWebSocketManager.isConnected()) {
    chatWebSocketManager.unsubscribe(`consultation_${consultationId}`)
    chatWebSocketManager.unsubscribe(`status_${consultationId}`)
    console.log('🧹 已清理咨询订阅:', consultationId)
  }
}

// 监听路由参数变化，支持在同一组件实例中切换不同的咨询
watch(
  () => route.params.id,
  (newId, oldId) => {
    if (newId && newId !== oldId) {
      console.log('🔄 路由参数变化，重新初始化页面:', { from: oldId, to: newId })
      
      // 清理旧的订阅
      if (oldId) {
        cleanupConsultationSubscriptions(oldId)
      }
      
      // 重新初始化新的咨询
      initializeConsultationPage()
    }
  }
)

// 生命周期
onMounted(async () => {
  console.log('🚀 咨询聊天页面已挂载')
  await initializeConsultationPage()
})

onBeforeUnmount(() => {
  console.log('🗑️ 咨询聊天页面即将卸载')
  
  // 清除当前活跃会话
  chatStore.setActiveConsultation(null)
  
  // 清理当前咨询的订阅
  cleanupConsultationSubscriptions(route.params.id)
})
</script>

<style scoped>
.consultation-chat-page {
  position: relative;
  display: flex;
  flex-direction: column;
  height: calc(100vh - 100px);
  background: #F0FDFA;
  margin: -20px;
  min-height: 500px;
}

/* 聊天头部 - Healthcare风格 */
.chat-header {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  z-index: 100;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  background: linear-gradient(135deg, #ECFEFF 0%, #CFFAFE 100%);
  border-bottom: 1px solid #A5F3FC;
  box-shadow: 0 2px 8px rgba(8, 145, 178, 0.1);
  height: 68px;
  box-sizing: border-box;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.back-btn {
  color: #0891B2;
}

.back-btn:hover {
  color: #0E7490;
}

.doctor-info {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.doctor-name {
  font-size: 16px;
  font-weight: 600;
  color: #164E63;
}

.doctor-desc {
  font-size: 12px;
  color: #0891B2;
}

/* 聊天内容区域 */
.chat-body {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
  padding-bottom: 80px;
  margin-top: 68px;
  background: #F0FDFA;
}

.chat-messages {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.message-item {
  display: flex;
  max-width: 70%;
}

.user-message {
  margin-left: auto;
}

.user-message .message-bubble {
  background: linear-gradient(135deg, #0891B2 0%, #22D3EE 100%);
  color: white;
  border-radius: 16px 16px 4px 16px;
}

.doctor-message .message-bubble {
  background: white;
  color: #164E63;
  border-radius: 16px 16px 16px 4px;
  border: 1px solid #A5F3FC;
}

.message-bubble {
  padding: 12px 16px;
  word-wrap: break-word;
  line-height: 1.4;
}

.message-info {
  display: flex;
  align-items: center;
  gap: 4px;
  margin-top: 4px;
  font-size: 12px;
  color: #67E8F9;
}

.user-message .message-info {
  justify-content: flex-end;
}

.message-status .read-icon {
  color: #059669;
}

.message-status .sent-icon {
  color: #67E8F9;
}

/* 空状态 */
.empty-messages {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 200px;
  color: #67E8F9;
}

.empty-icon {
  font-size: 48px;
  margin-bottom: 12px;
  color: #A5F3FC;
}

/* 输入区域 */
.chat-footer {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  z-index: 100;
  background: white;
  border-top: 1px solid #A5F3FC;
  padding: 12px 16px;
  box-shadow: 0 -2px 8px rgba(8, 145, 178, 0.1);
  min-height: 60px;
  box-sizing: border-box;
}

.input-container {
  display: flex;
  align-items: center;
  gap: 12px;
}

.upload-button {
  flex-shrink: 0;
  width: 36px;
  height: 36px;
  transition: all 0.3s ease;
  margin-right: 4px;
}

.upload-button:hover {
  transform: scale(1.05);
}

.image-upload-button {
  background: #ECFEFF;
  border-color: #A5F3FC;
  color: #0891B2;
}

.image-upload-button:hover {
  background: #CFFAFE;
  border-color: #22D3EE;
}

.file-upload-button {
  background: #FEF3C7;
  border-color: #F59E0B;
  color: #D97706;
}

.file-upload-button:hover {
  background: #FDE68A;
  border-color: #D97706;
}

.message-input {
  flex: 1;
  min-width: 0;
}

.message-input :deep(.el-input__wrapper) {
  border-color: #A5F3FC;
}

.message-input :deep(.el-input__wrapper:focus-within) {
  border-color: #0891B2;
  box-shadow: 0 0 0 1px #0891B2;
}

.send-button {
  flex-shrink: 0;
  min-width: 60px;
  height: 36px;
  background: linear-gradient(135deg, #0891B2 0%, #22D3EE 100%);
  border: none;
}

.send-button:hover {
  background: linear-gradient(135deg, #0E7490 0%, #06B6D4 100%);
}

/* 图片上传进度 */
.upload-progress-container {
  margin-top: 8px;
  padding: 8px 12px;
  background: #ECFEFF;
  border-radius: 8px;
  border: 1px solid #A5F3FC;
}

.progress-text {
  font-size: 12px;
  color: #0891B2;
  margin-left: 8px;
}

/* 图片消息样式 */
.image-content {
  padding: 0;
}

.chat-image {
  max-width: 200px;
  max-height: 200px;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.chat-image:hover {
  transform: scale(1.02);
  box-shadow: 0 4px 12px rgba(8, 145, 178, 0.2);
}

.image-error {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 20px;
  color: #67E8F9;
  background: #ECFEFF;
  border-radius: 8px;
  font-size: 12px;
}

.image-error .el-icon {
  font-size: 24px;
  margin-bottom: 4px;
}

/* 文件消息样式 */
.file-content {
  padding: 12px;
  min-width: 200px;
}

.file-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.file-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 40px;
  height: 40px;
  background: #ECFEFF;
  border-radius: 8px;
  color: #0891B2;
  font-size: 20px;
}

.file-details {
  flex: 1;
  min-width: 0;
}

.file-name {
  font-weight: 500;
  color: #164E63;
  margin-bottom: 4px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  max-width: 150px;
}

.file-size {
  font-size: 12px;
  color: #67E8F9;
}

.download-button {
  flex-shrink: 0;
  background: #0891B2;
  border-color: #0891B2;
}

.download-button:hover {
  background: #0E7490;
  border-color: #0E7490;
}

/* 用户发送的文件消息 */
.user-message .file-icon {
  background: rgba(255, 255, 255, 0.2);
  color: white;
}

.user-message .file-name {
  color: white;
}

.user-message .file-size {
  color: rgba(255, 255, 255, 0.8);
}

/* 问诊结束状态 */
.consultation-ended-footer {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  z-index: 100;
  background: linear-gradient(135deg, #ECFEFF 0%, #CFFAFE 100%);
  border-top: 1px solid #A5F3FC;
  box-shadow: 0 -2px 8px rgba(8, 145, 178, 0.1);
  padding: 12px 16px;
  min-height: 60px;
  box-sizing: border-box;
  display: flex;
  align-items: center;
}

.ended-content {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  width: 100%;
}

.ended-icon {
  font-size: 20px;
  color: #059669;
}

.ended-text {
  color: #164E63;
  font-size: 14px;
}

.ended-content .el-button {
  background: linear-gradient(135deg, #0891B2 0%, #22D3EE 100%);
  border: none;
}

.ended-content .el-button:hover {
  background: linear-gradient(135deg, #0E7490 0%, #06B6D4 100%);
}

/* 图片和文件内容样式 */
.image-content {
  padding: 4px;
}

.file-content {
  display: flex;
  align-items: center;
  gap: 8px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .consultation-chat-page {
    height: calc(100vh - 92px);
    margin: -16px;
  }
  
  .chat-header {
    padding: 12px 16px;
    height: 60px;
  }
  
  .chat-body {
    padding: 16px;
    padding-bottom: 72px;
    margin-top: 60px;
  }
  
  .chat-footer {
    padding: 8px 12px;
    min-height: 56px;
  }
  
  .consultation-ended-footer {
    padding: 8px 12px;
    min-height: 56px;
  }
  
  .ended-content {
    gap: 8px;
  }
  
  .ended-text {
    font-size: 13px;
  }
  
  .message-input {
    font-size: 14px;
  }
  
  .send-button {
    min-width: 50px;
    height: 32px;
    font-size: 13px;
  }
  
  .message-item {
    max-width: 85%;
  }
  
  .upload-button {
    width: 32px;
    height: 32px;
  }
}
</style> 