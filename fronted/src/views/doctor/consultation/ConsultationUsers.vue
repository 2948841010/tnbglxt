<template>
  <div class="consultation-users-page">
    <div class="page-header">
      <h1 class="page-title">
        <el-icon><UserFilled /></el-icon>
        咨询用户
      </h1>
      <p class="page-subtitle">查看向您咨询的患者信息</p>
    </div>

    <div class="placeholder-content">
      <div class="placeholder-icon">
        <el-icon><User /></el-icon>
      </div>
      <h3>咨询用户功能</h3>
      <p>此页面将显示咨询患者的详细信息，您可以在这里：</p>
      <ul>
        <li>查看患者基本信息</li>
        <li>查看患者健康数据</li>
        <li>了解患者病史</li>
        <li>查看咨询历史</li>
      </ul>
      <el-button type="primary" @click="goBack">
        <el-icon><ArrowLeft /></el-icon>
        返回工作台
      </el-button>
    </div>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { UserFilled, User, ArrowLeft } from '@element-plus/icons-vue'

const router = useRouter()

const goBack = () => {
  router.push('/doctor/dashboard')
}
</script>

<style scoped>
.consultation-users-page {
  padding: 24px;
  background: #f5f7fa;
  min-height: 100vh;
}

.page-header {
  background: white;
  padding: 24px;
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  margin-bottom: 24px;
}

.page-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 24px;
  font-weight: 600;
  color: #1e293b;
  margin: 0 0 8px 0;
}

.page-subtitle {
  color: #64748b;
  margin: 0;
}

.placeholder-content {
  background: white;
  padding: 60px 40px;
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  text-align: center;
}

.placeholder-icon {
  font-size: 80px;
  color: #e2e8f0;
  margin-bottom: 24px;
}

.placeholder-content h3 {
  font-size: 20px;
  color: #1e293b;
  margin: 0 0 16px 0;
}

.placeholder-content p {
  color: #64748b;
  margin-bottom: 20px;
  font-size: 16px;
}

.placeholder-content ul {
  text-align: left;
  max-width: 400px;
  margin: 0 auto 32px;
  color: #64748b;
}

.placeholder-content li {
  margin-bottom: 8px;
}
</style> 