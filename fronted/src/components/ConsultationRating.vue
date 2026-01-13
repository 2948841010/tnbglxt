<template>
  <el-dialog
    v-model="dialogVisible"
    title="问诊评价"
    width="500px"
    :before-close="handleClose"
    :close-on-click-modal="false"
  >
    <div class="rating-dialog">
      <div class="doctor-info">
        <div class="doctor-avatar">
          <el-avatar 
            :size="60" 
            :src="doctorInfo?.avatar" 
            :icon="UserFilled"
            class="avatar"
          />
        </div>
        <div class="doctor-details">
          <h3 class="doctor-name">{{ doctorInfo?.name || '医生' }}</h3>
          <p class="doctor-title">{{ doctorInfo?.department }} · {{ doctorInfo?.title }}</p>
        </div>
      </div>

      <div class="rating-section">
        <div class="rating-label">
          <el-icon><Star /></el-icon>
          <span>请为本次问诊评分</span>
        </div>
        <div class="rating-stars">
          <el-rate
            v-model="rating.score"
            :max="5"
            :colors="ratingColors"
            :texts="ratingTexts"
            show-text
            text-color="#ff9900"
            size="large"
          />
        </div>
      </div>

      <div class="comment-section">
        <div class="comment-label">
          <el-icon><ChatDotRound /></el-icon>
          <span>评价内容（选填）</span>
        </div>
        <el-input
          v-model="rating.comment"
          type="textarea"
          :rows="4"
          :maxlength="200"
          show-word-limit
          placeholder="请分享您对本次问诊的感受，您的评价将帮助医生提供更好的服务..."
        />
      </div>

      <div class="rating-tips">
        <el-icon><InfoFilled /></el-icon>
        <span>您的评价将匿名展示，帮助其他用户参考</span>
      </div>
    </div>

    <template #footer>
      <div class="dialog-footer">
        <el-button @click="handleClose">
          稍后评价
        </el-button>
        <el-button 
          type="primary" 
          @click="submitRating"
          :loading="submitting"
          :disabled="rating.score === 0"
        >
          提交评价
        </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, reactive, defineEmits, defineProps } from 'vue'
import { ElMessage } from 'element-plus'
import { 
  UserFilled, 
  Star, 
  ChatDotRound, 
  InfoFilled 
} from '@element-plus/icons-vue'
import { submitConsultationRating } from '@/api/consultation'

// 定义组件属性
const props = defineProps({
  visible: {
    type: Boolean,
    default: false
  },
  consultationNo: {
    type: String,
    required: true
  },
  doctorInfo: {
    type: Object,
    default: () => ({})
  }
})

// 定义事件
const emit = defineEmits(['update:visible', 'rating-submitted'])

// 响应式数据
const dialogVisible = ref(false)
const submitting = ref(false)

// 评价数据
const rating = reactive({
  score: 0,
  comment: ''
})

// 星级评分配置
const ratingColors = ['#99A9BF', '#F7BA2A', '#FF9900']
const ratingTexts = ['很差', '一般', '良好', '很好', '非常棒']

// 监听 visible 属性变化
import { watch } from 'vue'
watch(() => props.visible, (newVal) => {
  dialogVisible.value = newVal
  if (newVal) {
    // 重置评价数据
    rating.score = 0
    rating.comment = ''
  }
})

// 监听对话框显示状态变化
watch(dialogVisible, (newVal) => {
  emit('update:visible', newVal)
})

// 关闭对话框
const handleClose = () => {
  dialogVisible.value = false
}

// 提交评价
const submitRating = async () => {
  if (rating.score === 0) {
    ElMessage.warning('请先给出评分')
    return
  }

  submitting.value = true
  
  try {
    const response = await submitConsultationRating({
      consultationNo: props.consultationNo,
      score: rating.score,
      comment: rating.comment.trim()
    })

    if (response.code === 200) {
      ElMessage.success('评价提交成功，感谢您的反馈！')
      
      // 触发评价提交成功事件
      emit('rating-submitted', {
        score: rating.score,
        comment: rating.comment.trim()
      })
      
      // 关闭对话框
      handleClose()
    } else {
      ElMessage.error(response.message || '评价提交失败')
    }
  } catch (error) {
    console.error('提交评价失败:', error)
    ElMessage.error('网络异常，请稍后重试')
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped>
.rating-dialog {
  padding: 10px 0;
}

.doctor-info {
  display: flex;
  align-items: center;
  margin-bottom: 30px;
  padding: 20px;
  background: #f8f9fa;
  border-radius: 12px;
}

.doctor-avatar {
  margin-right: 15px;
}

.avatar {
  border: 3px solid #e1f5fe;
}

.doctor-details {
  flex: 1;
}

.doctor-name {
  margin: 0 0 5px 0;
  font-size: 18px;
  font-weight: 600;
  color: #333;
}

.doctor-title {
  margin: 0;
  font-size: 14px;
  color: #666;
}

.rating-section {
  margin-bottom: 25px;
}

.rating-label {
  display: flex;
  align-items: center;
  margin-bottom: 15px;
  font-size: 16px;
  font-weight: 500;
  color: #333;
}

.rating-label .el-icon {
  color: #ff9900;
  margin-right: 8px;
}

.rating-stars {
  display: flex;
  justify-content: center;
  padding: 10px 0;
}

:deep(.el-rate) {
  justify-content: center;
}

:deep(.el-rate__text) {
  font-size: 16px;
  margin-left: 10px;
}

.comment-section {
  margin-bottom: 20px;
}

.comment-label {
  display: flex;
  align-items: center;
  margin-bottom: 15px;
  font-size: 16px;
  font-weight: 500;
  color: #333;
}

.comment-label .el-icon {
  color: #409EFF;
  margin-right: 8px;
}

.rating-tips {
  display: flex;
  align-items: center;
  padding: 12px 16px;
  background: #f0f9ff;
  border-left: 4px solid #409EFF;
  border-radius: 6px;
  font-size: 14px;
  color: #606266;
}

.rating-tips .el-icon {
  color: #409EFF;
  margin-right: 8px;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

:deep(.el-dialog__header) {
  padding: 20px 20px 10px;
}

:deep(.el-dialog__body) {
  padding: 10px 20px;
}

:deep(.el-dialog__footer) {
  padding: 10px 20px 20px;
}
</style> 