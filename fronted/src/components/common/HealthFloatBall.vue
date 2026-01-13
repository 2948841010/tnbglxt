<template>
  <div class="health-float-ball" v-if="visible" :class="{ 'modal-open': showModal }">
    <!-- 悬浮球按钮 -->
    <div 
      class="float-ball"
      :class="{ 'dragging': isDragging }"
      @click="handleClick"
      @mousedown="startDrag"
      @touchstart="startDrag"
      :style="{ 
        left: position.x + 'px', 
        top: position.y + 'px' 
      }"
    >
      <div class="ball-icon">
        <el-icon>
          <TrendCharts />
        </el-icon>
      </div>
      <div class="ball-tooltip">查看健康数据</div>
    </div>

    <!-- 健康趋势弹窗 -->
    <HealthTrendModal
      v-model="showModal"
      :patient-id="patientId"
      :patient-info="patientInfo"
    />
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onBeforeUnmount, nextTick } from 'vue'
import { TrendCharts } from '@element-plus/icons-vue'
import HealthTrendModal from './HealthTrendModal.vue'

// Props
const props = defineProps({
  visible: {
    type: Boolean,
    default: true
  },
  patientId: {
    type: Number,
    required: true
  },
  patientInfo: {
    type: Object,
    default: () => ({})
  },
  initialPosition: {
    type: Object,
    default: () => ({ x: 20, y: 200 })
  }
})

// 响应式数据
const showModal = ref(false)
const isDragging = ref(false)
const position = reactive({
  x: props.initialPosition.x,
  y: props.initialPosition.y
})

// 拖拽相关
let dragStartPos = { x: 0, y: 0 }
let dragOffset = { x: 0, y: 0 }

// 点击事件处理
const handleClick = (e) => {
  // 如果是拖拽结束后的点击，不触发弹窗
  if (isDragging.value) {
    return
  }
  
  e.preventDefault()
  e.stopPropagation()
  showModal.value = true
}

// 开始拖拽
const startDrag = (e) => {
  isDragging.value = false
  
  const clientX = e.type === 'touchstart' ? e.touches[0].clientX : e.clientX
  const clientY = e.type === 'touchstart' ? e.touches[0].clientY : e.clientY
  
  dragStartPos.x = clientX
  dragStartPos.y = clientY
  dragOffset.x = clientX - position.x
  dragOffset.y = clientY - position.y
  
  document.addEventListener('mousemove', handleDrag)
  document.addEventListener('mouseup', stopDrag)
  document.addEventListener('touchmove', handleDrag, { passive: false })
  document.addEventListener('touchend', stopDrag)
  
  e.preventDefault()
}

// 拖拽过程
const handleDrag = (e) => {
  const clientX = e.type === 'touchmove' ? e.touches[0].clientX : e.clientX
  const clientY = e.type === 'touchmove' ? e.touches[0].clientY : e.clientY
  
  // 判断是否开始拖拽（移动距离超过阈值）
  const deltaX = Math.abs(clientX - dragStartPos.x)
  const deltaY = Math.abs(clientY - dragStartPos.y)
  
  if (!isDragging.value && (deltaX > 5 || deltaY > 5)) {
    isDragging.value = true
  }
  
  if (isDragging.value) {
    const newX = clientX - dragOffset.x
    const newY = clientY - dragOffset.y
    
    // 限制在窗口范围内
    const maxX = window.innerWidth - 60
    const maxY = window.innerHeight - 60
    
    position.x = Math.max(0, Math.min(maxX, newX))
    position.y = Math.max(0, Math.min(maxY, newY))
  }
  
  e.preventDefault()
}

// 停止拖拽
const stopDrag = () => {
  document.removeEventListener('mousemove', handleDrag)
  document.removeEventListener('mouseup', stopDrag)
  document.removeEventListener('touchmove', handleDrag)
  document.removeEventListener('touchend', stopDrag)
  
  // 延迟重置拖拽状态，防止拖拽后立即触发点击
  setTimeout(() => {
    isDragging.value = false
  }, 100)
}

// 自动吸边效果
const snapToEdge = () => {
  const ballWidth = 60
  const threshold = window.innerWidth / 2
  
  if (position.x < threshold) {
    // 吸附到左边
    position.x = 20
  } else {
    // 吸附到右边
    position.x = window.innerWidth - ballWidth - 20
  }
}

// 窗口大小改变时调整位置
const handleResize = () => {
  const maxX = window.innerWidth - 60
  const maxY = window.innerHeight - 60
  
  position.x = Math.max(0, Math.min(maxX, position.x))
  position.y = Math.max(0, Math.min(maxY, position.y))
}

// 生命周期
onMounted(() => {
  window.addEventListener('resize', handleResize)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
  document.removeEventListener('mousemove', handleDrag)
  document.removeEventListener('mouseup', stopDrag)
  document.removeEventListener('touchmove', handleDrag)
  document.removeEventListener('touchend', stopDrag)
})
</script>

<style scoped>
.health-float-ball {
  position: fixed;
  z-index: 10000;
  pointer-events: none;
}

/* 当弹窗打开时，降低悬浮球的层级 */
.health-float-ball.modal-open {
  z-index: 9000;
  pointer-events: none !important;
}

.health-float-ball.modal-open .float-ball {
  pointer-events: none !important;
  opacity: 0.3;
  transform: scale(0.8);
}

.float-ball {
  position: absolute;
  width: 60px;
  height: 60px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  box-shadow: 0 4px 20px rgba(102, 126, 234, 0.4);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  pointer-events: auto;
  user-select: none;
  -webkit-user-select: none;
  /* 确保可见 */
  opacity: 1;
  visibility: visible;
}

.float-ball:hover {
  transform: scale(1.1);
  box-shadow: 0 6px 25px rgba(102, 126, 234, 0.5);
}

.float-ball.dragging {
  transform: scale(0.95);
  box-shadow: 0 8px 30px rgba(102, 126, 234, 0.6);
  transition: none;
}

.ball-icon {
  color: white;
  font-size: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.ball-tooltip {
  position: absolute;
  bottom: 70px;
  left: 50%;
  transform: translateX(-50%);
  background: rgba(0, 0, 0, 0.8);
  color: white;
  padding: 8px 12px;
  border-radius: 6px;
  font-size: 12px;
  white-space: nowrap;
  opacity: 0;
  pointer-events: none;
  transition: opacity 0.3s ease;
}

.ball-tooltip::after {
  content: '';
  position: absolute;
  top: 100%;
  left: 50%;
  transform: translateX(-50%);
  border: 5px solid transparent;
  border-top-color: rgba(0, 0, 0, 0.8);
}

.float-ball:hover .ball-tooltip {
  opacity: 1;
}

/* 呼吸动画 */
@keyframes breathe {
  0%, 100% {
    box-shadow: 0 4px 20px rgba(102, 126, 234, 0.4);
  }
  50% {
    box-shadow: 0 4px 20px rgba(102, 126, 234, 0.6), 
                0 0 20px rgba(102, 126, 234, 0.3);
  }
}

.float-ball {
  animation: breathe 3s ease-in-out infinite;
}

.float-ball:hover {
  animation: none;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .float-ball {
    width: 50px;
    height: 50px;
  }
  
  .ball-icon {
    font-size: 20px;
  }
  
  .ball-tooltip {
    font-size: 11px;
    padding: 6px 10px;
  }
}

/* 确保在移动设备上可以拖拽 */
@media (pointer: coarse) {
  .float-ball {
    touch-action: none;
  }
}
</style> 