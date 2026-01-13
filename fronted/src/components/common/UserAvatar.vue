<template>
  <div class="user-avatar" :class="[`avatar-${size}`, { 'avatar-clickable': clickable }]" @click="handleClick">
    <!-- 有头像时显示头像 -->
    <el-image
      v-if="avatarUrl"
      :src="avatarUrl"
      class="avatar-image"
      fit="cover"
      @error="handleImageError"
    >
      <template #error>
        <div class="avatar-fallback">
          <el-icon><User /></el-icon>
        </div>
      </template>
    </el-image>
    
    <!-- 没有头像时显示默认头像 -->
    <div v-else class="avatar-fallback">
      <el-icon><User /></el-icon>
    </div>
    
    <!-- 在线状态指示器 -->
    <div v-if="showOnlineStatus" class="online-status" :class="{ 'online': isOnline }"></div>
  </div>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { User } from '@element-plus/icons-vue'

// Props
const props = defineProps({
  // 头像URL
  src: {
    type: String,
    default: ''
  },
  // 头像尺寸
  size: {
    type: String,
    default: 'medium', // small, medium, large, extra-large
    validator: (value) => ['small', 'medium', 'large', 'extra-large'].includes(value)
  },
  // 是否可点击
  clickable: {
    type: Boolean,
    default: false
  },
  // 是否显示在线状态
  showOnlineStatus: {
    type: Boolean,
    default: false
  },
  // 是否在线
  isOnline: {
    type: Boolean,
    default: false
  },
  // 用户名（用于生成默认头像）
  username: {
    type: String,
    default: ''
  }
})

// Emits
const emit = defineEmits(['click'])

// Refs
const avatarUrl = ref(props.src)

// 监听props.src变化，同步更新avatarUrl
watch(() => props.src, (newSrc) => {
  avatarUrl.value = newSrc
}, { immediate: true })

// Computed
const initials = computed(() => {
  if (!props.username) return ''
  const names = props.username.split(' ')
  if (names.length >= 2) {
    return (names[0][0] + names[1][0]).toUpperCase()
  }
  return props.username.slice(0, 2).toUpperCase()
})

// Methods
const handleClick = () => {
  if (props.clickable) {
    emit('click')
  }
}

const handleImageError = () => {
  avatarUrl.value = ''
}
</script>

<style scoped>
.user-avatar {
  position: relative;
  display: inline-block;
  border-radius: 50%;
  overflow: hidden;
  background: #f5f7fa;
  border: 2px solid #e4e7ed;
  transition: all 0.3s ease;
}

.avatar-clickable {
  cursor: pointer;
}

.avatar-clickable:hover {
  border-color: #409eff;
  transform: scale(1.05);
}

.avatar-image {
  width: 100%;
  height: 100%;
  border-radius: 50%;
}

.avatar-fallback {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  font-weight: 600;
}

/* 尺寸样式 */
.avatar-small {
  width: 32px;
  height: 32px;
}

.avatar-small .avatar-fallback {
  font-size: 12px;
}

.avatar-small .avatar-fallback .el-icon {
  font-size: 16px;
}

.avatar-medium {
  width: 40px;
  height: 40px;
}

.avatar-medium .avatar-fallback {
  font-size: 14px;
}

.avatar-medium .avatar-fallback .el-icon {
  font-size: 20px;
}

.avatar-large {
  width: 60px;
  height: 60px;
}

.avatar-large .avatar-fallback {
  font-size: 18px;
}

.avatar-large .avatar-fallback .el-icon {
  font-size: 30px;
}

.avatar-extra-large {
  width: 80px;
  height: 80px;
}

.avatar-extra-large .avatar-fallback {
  font-size: 24px;
}

.avatar-extra-large .avatar-fallback .el-icon {
  font-size: 40px;
}

/* 在线状态指示器 */
.online-status {
  position: absolute;
  bottom: 2px;
  right: 2px;
  width: 12px;
  height: 12px;
  border-radius: 50%;
  border: 2px solid white;
  background: #ccc;
  transition: background-color 0.3s ease;
}

.online-status.online {
  background: #67c23a;
}

.avatar-small .online-status {
  width: 8px;
  height: 8px;
  bottom: 1px;
  right: 1px;
  border-width: 1px;
}

.avatar-large .online-status,
.avatar-extra-large .online-status {
  width: 16px;
  height: 16px;
  bottom: 3px;
  right: 3px;
  border-width: 3px;
}
</style> 