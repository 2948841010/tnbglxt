<template>
  <div class="avatar-upload">
    <!-- 头像显示区域 -->
    <div class="avatar-container" @click="handleAvatarClick">
      <!-- 有头像时显示头像 -->
      <el-image
        v-if="currentAvatarUrl"
        :src="currentAvatarUrl"
        class="avatar-image"
        fit="cover"
        :loading="loading"
        @error="handleImageError"
      >
        <template #error>
          <div class="avatar-placeholder">
            <el-icon><User /></el-icon>
          </div>
        </template>
      </el-image>
      
      <!-- 没有头像时显示默认占位符 -->
      <div v-else class="avatar-placeholder">
        <el-icon><User /></el-icon>
        <div class="upload-text">点击上传头像</div>
      </div>
      
      <!-- 悬停时的遮罩层 -->
      <div class="avatar-overlay">
        <el-icon><Camera /></el-icon>
        <div class="overlay-text">更换头像</div>
      </div>
      
      <!-- 上传进度 -->
      <div v-if="uploading" class="upload-progress">
        <el-progress 
          type="circle" 
          :percentage="uploadProgress" 
          :width="60"
          :stroke-width="4"
        />
      </div>
    </div>

    <!-- 隐藏的文件输入框 -->
    <input
      ref="fileInput"
      type="file"
      accept="image/*"
      style="display: none"
      @change="handleFileChange"
    />

    <!-- 操作按钮 -->
    <div v-if="showButtons" class="avatar-actions">
      <el-button type="primary" size="small" @click="handleAvatarClick" :loading="uploading">
        <el-icon><Upload /></el-icon>
        上传头像
      </el-button>
      <el-button 
        v-if="currentAvatarUrl" 
        type="danger" 
        size="small" 
        @click="handleRemoveAvatar"
        :loading="removing"
      >
        <el-icon><Delete /></el-icon>
        删除头像
      </el-button>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { User, Camera, Upload, Delete } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import request from '@/api/request'

// Props
const props = defineProps({
  // 是否显示操作按钮
  showButtons: {
    type: Boolean,
    default: true
  },
  // 头像尺寸
  size: {
    type: String,
    default: 'large' // small, medium, large
  },
  // 是否只读模式
  readonly: {
    type: Boolean,
    default: false
  }
})

// Emits
const emit = defineEmits(['avatar-updated', 'avatar-removed'])

// Store
const userStore = useUserStore()

// Refs
const fileInput = ref(null)
const loading = ref(false)
const uploading = ref(false)
const removing = ref(false)
const uploadProgress = ref(0)
const currentAvatarUrl = ref('')

// Computed
const avatarSize = computed(() => {
  const sizeMap = {
    small: '60px',
    medium: '80px',
    large: '120px'
  }
  return sizeMap[props.size] || sizeMap.large
})

// Methods
const handleAvatarClick = () => {
  if (props.readonly || uploading.value) return
  fileInput.value?.click()
}

const handleFileChange = async (event) => {
  const file = event.target.files[0]
  if (!file) return

  // 验证文件类型
  if (!file.type.startsWith('image/')) {
    ElMessage.error('请选择图片文件')
    return
  }

  // 验证文件大小（5MB）
  if (file.size > 5 * 1024 * 1024) {
    ElMessage.error('图片大小不能超过5MB')
    return
  }

  await uploadAvatar(file)
  
  // 清空input的值，这样可以重复选择同一个文件
  event.target.value = ''
}

const uploadAvatar = async (file) => {
  try {
    uploading.value = true
    uploadProgress.value = 0

    const formData = new FormData()
    formData.append('file', file)

    const response = await request.post('/v1/avatar/upload', formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      },
      timeout: 60000, // 设置60秒超时，给文件上传足够的时间
      onUploadProgress: (progressEvent) => {
        // 使用真实的上传进度
        if (progressEvent.total) {
          const realProgress = Math.round((progressEvent.loaded * 100) / progressEvent.total)
          uploadProgress.value = Math.min(realProgress, 95) // 最多显示95%，留5%给后端处理
        }
      }
    })

    // 上传完成，显示100%
    uploadProgress.value = 100

    // 调试信息：打印响应数据结构
    console.log('头像上传响应:', response)

    if (response.code === 200) {
      currentAvatarUrl.value = response.data.fileUrl
      
      // 更新用户store中的头像
      userStore.updateAvatar(response.data.fileUrl)

      ElMessage.success('头像上传成功')
      emit('avatar-updated', response.data)
    } else {
      throw new Error(response.message || '上传失败')
    }

  } catch (error) {
    console.error('头像上传失败:', error)
    
    // 特殊处理超时错误
    if (error.code === 'ECONNABORTED' || error.message.includes('timeout')) {
      ElMessage.warning('文件上传时间较长，请稍等片刻后刷新页面查看结果')
    } else {
      ElMessage.error('头像上传失败: ' + (error.response?.data?.message || error.message))
    }
  } finally {
    uploading.value = false
    uploadProgress.value = 0
  }
}

const handleRemoveAvatar = async () => {
  try {
    await ElMessageBox.confirm(
      '确定要删除当前头像吗？',
      '删除头像',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    removing.value = true

    const response = await request.delete('/v1/avatar/remove')
    
    if (response.code === 200) {
      currentAvatarUrl.value = ''
      
      // 更新用户store中的头像
      userStore.updateAvatar(null)

      ElMessage.success('头像删除成功')
      emit('avatar-removed')
    } else {
      throw new Error(response.message || '删除失败')
    }

  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除头像失败:', error)
      ElMessage.error('删除头像失败: ' + error.message)
    }
  } finally {
    removing.value = false
  }
}

const loadAvatarInfo = async () => {
  try {
    loading.value = true
    const response = await request.get('/v1/avatar/info')
    
    if (response.code === 200) {
      currentAvatarUrl.value = response.data.avatarUrl || ''
    }
  } catch (error) {
    console.error('获取头像信息失败:', error)
  } finally {
    loading.value = false
  }
}

const handleImageError = () => {
  console.warn('头像加载失败')
  currentAvatarUrl.value = ''
}

// 初始化
onMounted(() => {
  // 如果用户store中有头像信息，直接使用
  if (userStore.userInfo?.avatar) {
    currentAvatarUrl.value = userStore.userInfo.avatar
  } else {
    // 否则从服务器获取
    loadAvatarInfo()
  }
})
</script>

<style scoped>
.avatar-upload {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16px;
}

.avatar-container {
  position: relative;
  width: v-bind(avatarSize);
  height: v-bind(avatarSize);
  border-radius: 50%;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.3s ease;
  border: 3px solid #e4e7ed;
}

.avatar-container:hover {
  border-color: #409eff;
  transform: scale(1.05);
}

.avatar-container:hover .avatar-overlay {
  opacity: 1;
}

.avatar-image {
  width: 100%;
  height: 100%;
  border-radius: 50%;
}

.avatar-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background: #f5f7fa;
  color: #909399;
  font-size: 24px;
}

.avatar-placeholder .upload-text {
  font-size: 12px;
  margin-top: 8px;
}

.avatar-overlay {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.6);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: opacity 0.3s ease;
  color: white;
  font-size: 20px;
}

.overlay-text {
  font-size: 12px;
  margin-top: 4px;
}

.upload-progress {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  background: rgba(255, 255, 255, 0.9);
  border-radius: 50%;
  padding: 10px;
}

.avatar-actions {
  display: flex;
  gap: 12px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .avatar-container {
    width: 100px;
    height: 100px;
  }
  
  .avatar-actions {
    flex-direction: column;
    width: 100%;
  }
  
  .avatar-actions .el-button {
    width: 100%;
  }
}
</style> 