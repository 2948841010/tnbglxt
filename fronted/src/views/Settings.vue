<template>
  <div class="settings-page">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>系统设置</span>
        </div>
      </template>
      
      <div class="settings-content">
        <el-empty 
          description="系统设置功能开发中"
          :image-size="200"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup>
// 设置页面逻辑
</script>

<style scoped>
.settings-page {
  padding: 0;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: 600;
}

.settings-content {
  min-height: 400px;
  display: flex;
  align-items: center;
  justify-content: center;
}
</style> 