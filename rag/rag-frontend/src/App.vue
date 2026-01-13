<template>
  <div class="app-container">
    <!-- 侧边栏 -->
    <aside class="sidebar">
      <div class="sidebar-header">
        <div class="sidebar-logo">
          <el-icon :size="28"><DataAnalysis /></el-icon>
          <span>RAG知识库</span>
        </div>
      </div>
      <nav class="sidebar-nav">
        <router-link 
          v-for="item in navItems" 
          :key="item.path"
          :to="item.path"
          custom
          v-slot="{ isActive, navigate }"
        >
          <div 
            class="nav-item" 
            :class="{ active: isActive }"
            @click="navigate"
          >
            <el-icon :size="20"><component :is="item.icon" /></el-icon>
            <span>{{ item.title }}</span>
          </div>
        </router-link>
      </nav>
    </aside>

    <!-- 主内容 -->
    <main class="main-content">
      <header class="header">
        <h1 class="header-title">{{ currentTitle }}</h1>
        <div class="header-actions">
          <el-tag type="success" v-if="healthStatus.status === 'healthy'">
            <el-icon><CircleCheck /></el-icon> 服务正常
          </el-tag>
          <el-tag type="danger" v-else>
            <el-icon><CircleClose /></el-icon> 服务异常
          </el-tag>
        </div>
      </header>
      <div class="content">
        <router-view />
      </div>
    </main>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { useRagStore } from '@/stores/rag'

const route = useRoute()
const ragStore = useRagStore()

const navItems = [
  { path: '/', title: '知识检索', icon: 'Search' },
  { path: '/documents', title: '文档管理', icon: 'Document' },
  { path: '/stats', title: '系统状态', icon: 'DataLine' }
]

const currentTitle = computed(() => {
  const item = navItems.find(n => n.path === route.path)
  return item?.title || 'RAG知识库'
})

const healthStatus = computed(() => ragStore.healthStatus)

onMounted(() => {
  ragStore.fetchHealth()
})
</script>
