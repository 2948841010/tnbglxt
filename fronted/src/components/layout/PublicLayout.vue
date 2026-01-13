<template>
  <div class="public-layout">
    <!-- 顶部导航栏 -->
    <header class="navbar">
      <div class="navbar-container">
        <!-- Logo和系统名称 -->
        <div class="navbar-brand">
          <div class="logo">
            <i class="el-icon-medicine-box"></i>
          </div>
          <span class="system-name">糖尿病智能管理系统</span>
        </div>

        <!-- 导航菜单 -->
        <nav class="navbar-nav">
          <router-link 
            to="/home" 
            class="nav-link"
            :class="{ active: $route.path === '/home' }"
          >
            <i class="el-icon-house"></i>
            首页
          </router-link>
          <router-link 
            to="/assessment" 
            class="nav-link"
            :class="{ active: $route.path === '/assessment' }"
          >
            <i class="el-icon-document-checked"></i>
            智能评测
          </router-link>
          <router-link 
            to="/chat" 
            class="nav-link"
            :class="{ active: $route.path === '/chat' }"
          >
            <i class="el-icon-chat-line-round"></i>
            智能对话
          </router-link>
        </nav>

        <!-- 登录按钮 -->
        <div class="navbar-actions">
          <el-button 
            type="primary" 
            @click="goToLogin"
            class="login-btn"
          >
            <i class="el-icon-user"></i>
            登录
          </el-button>
        </div>
      </div>
    </header>

    <!-- 主要内容区域 -->
    <main class="main-content">
      <router-view />
    </main>

    <!-- 页脚 -->
    <footer class="footer">
      <div class="footer-content">
        <p>&copy; 2025 糖尿病智能管理系统. 版权所有.</p>
        <p>专业的糖尿病管理与健康咨询平台</p>
      </div>
    </footer>
  </div>
</template>

<script>
export default {
  name: 'PublicLayout',
  methods: {
    goToLogin() {
      this.$router.push('/login')
    }
  }
}
</script>

<style scoped>
.public-layout {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

/* 导航栏样式 */
.navbar {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
  position: sticky;
  top: 0;
  z-index: 1000;
}

.navbar-container {
  max-width: 1200px;
  margin: 0 auto;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
  height: 70px;
}

.navbar-brand {
  display: flex;
  align-items: center;
  color: white;
  font-size: 20px;
  font-weight: bold;
}

.logo {
  width: 40px;
  height: 40px;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 12px;
  font-size: 20px;
}

.system-name {
  background: linear-gradient(45deg, #fff, #e0e7ff);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.navbar-nav {
  display: flex;
  gap: 30px;
}

.nav-link {
  display: flex;
  align-items: center;
  gap: 8px;
  color: rgba(255, 255, 255, 0.9);
  text-decoration: none;
  padding: 10px 16px;
  border-radius: 25px;
  transition: all 0.3s ease;
  font-size: 16px;
}

.nav-link:hover {
  background: rgba(255, 255, 255, 0.1);
  color: white;
  transform: translateY(-2px);
}

.nav-link.active {
  background: rgba(255, 255, 255, 0.15);
  color: white;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.1);
}

.navbar-actions .login-btn {
  background: linear-gradient(45deg, #42d392, #42a5f5);
  border: none;
  border-radius: 25px;
  padding: 10px 24px;
  font-size: 16px;
  font-weight: 500;
  box-shadow: 0 4px 15px rgba(66, 211, 146, 0.3);
  transition: all 0.3s ease;
}

.navbar-actions .login-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(66, 211, 146, 0.4);
}

/* 主要内容区域 */
.main-content {
  flex: 1;
}

/* 页脚样式 */
.footer {
  background: #2c3e50;
  color: white;
  text-align: center;
  padding: 30px 20px;
  margin-top: auto;
}

.footer-content p {
  margin: 5px 0;
  font-size: 14px;
}

.footer-content p:first-child {
  font-weight: 600;
}

.footer-content p:last-child {
  color: rgba(255, 255, 255, 0.7);
}

/* 响应式设计 */
@media (max-width: 768px) {
  .navbar-container {
    padding: 0 15px;
    height: 60px;
  }
  
  .system-name {
    display: none;
  }
  
  .navbar-nav {
    gap: 15px;
  }
  
  .nav-link {
    padding: 8px 12px;
    font-size: 14px;
  }
  
  .nav-link span {
    display: none;
  }
}
</style> 