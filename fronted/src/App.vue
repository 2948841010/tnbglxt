<template>
  <div id="app">
    <router-view />
  </div>
</template>

<script setup>
import { onMounted } from 'vue'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'

const userStore = useUserStore()

onMounted(async () => {
  // 尝试从本地存储恢复用户状态
  try {
    await userStore.initAuth()
  } catch (error) {
    console.error('初始化认证失败:', error)
  }
})
</script>

<style>
#app {
  min-height: 100vh;
  width: 100%;
}

/* 为MainLayout提供固定高度布局 */
#app > .main-layout {
  height: 100vh;
  overflow: hidden;
}

* {
  box-sizing: border-box;
}

body {
  margin: 0;
  padding: 0;
}
</style> 