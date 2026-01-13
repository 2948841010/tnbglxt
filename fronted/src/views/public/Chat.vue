<template>
  <div class="chat-page">
    <!-- 导航栏 -->
    <nav class="navbar">
      <div class="nav-container">
        <router-link to="/" class="nav-brand">
          <svg class="brand-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M22 12h-4l-3 9L9 3l-3 9H2"/>
          </svg>
          <span>糖尿病智能管理</span>
        </router-link>
        <div class="nav-links">
          <router-link to="/" class="nav-link">首页</router-link>
          <router-link to="/assessment" class="nav-link">智能评测</router-link>
          <router-link to="/chat" class="nav-link active">智能对话</router-link>
        </div>
        <div class="nav-actions">
          <router-link to="/login" class="btn-outline-nav">登录</router-link>
          <router-link to="/register" class="btn-primary-nav">免费注册</router-link>
        </div>
      </div>
    </nav>

    <!-- Hero区域 -->
    <section class="hero-section">
      <div class="hero-bg"></div>
      <div class="container">
        <div class="hero-content">
          <div class="hero-badges">
            <span class="hero-badge">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="16" height="16">
                <path d="M9.75 17L9 20l-1 1h8l-1-1-.75-3M3 13h18M5 17h14a2 2 0 002-2V5a2 2 0 00-2-2H5a2 2 0 00-2 2v10a2 2 0 002 2z"/>
              </svg>
              AI技术驱动
            </span>
            <span class="hero-badge coming-soon">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="16" height="16">
                <path d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z"/>
              </svg>
              即将上线
            </span>
          </div>
          <h1 class="hero-title">AI智能健康对话</h1>
          <p class="hero-subtitle">24小时智能健康助手，为您提供专业的糖尿病健康咨询和日常护理指导</p>
        </div>
      </div>
    </section>

    <!-- 对话演示区域 -->
    <section class="demo-section">
      <div class="container">
        <div class="section-header">
          <span class="section-badge">智能对话</span>
          <h2 class="section-title">对话演示</h2>
        </div>
        <div class="chat-demo">
          <div class="chat-container">
            <div class="chat-header">
              <div class="chat-avatar">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="24" height="24">
                  <path d="M9.75 17L9 20l-1 1h8l-1-1-.75-3M3 13h18M5 17h14a2 2 0 002-2V5a2 2 0 00-2-2H5a2 2 0 00-2 2v10a2 2 0 002 2z"/>
                </svg>
              </div>
              <div class="chat-info">
                <h3>AI健康助手</h3>
                <span class="status"><span class="status-dot"></span>在线服务中</span>
              </div>
            </div>
            <div class="chat-messages">
              <div class="message bot-message">
                <div class="avatar bot">
                  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="20" height="20">
                    <path d="M9.75 17L9 20l-1 1h8l-1-1-.75-3M3 13h18M5 17h14a2 2 0 002-2V5a2 2 0 00-2-2H5a2 2 0 00-2 2v10a2 2 0 002 2z"/>
                  </svg>
                </div>
                <div class="content">您好！我是您的AI健康助手。我可以为您提供糖尿病相关的健康咨询，包括饮食建议、运动指导、血糖管理等。请问有什么可以帮助您的吗？</div>
              </div>
              <div class="message user-message">
                <div class="content">我最近血糖控制不太好，早晨空腹血糖总是偏高，应该怎么办？</div>
                <div class="avatar user">
                  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="20" height="20">
                    <path d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z"/>
                  </svg>
                </div>
              </div>
              <div class="message bot-message">
                <div class="avatar bot">
                  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="20" height="20">
                    <path d="M9.75 17L9 20l-1 1h8l-1-1-.75-3M3 13h18M5 17h14a2 2 0 002-2V5a2 2 0 00-2-2H5a2 2 0 00-2 2v10a2 2 0 002 2z"/>
                  </svg>
                </div>
                <div class="content">
                  晨间高血糖是常见问题，可能与以下因素有关：<br><br>
                  1. <strong>黎明现象</strong>：激素水平变化导致<br>
                  2. <strong>晚餐影响</strong>：晚餐时间或食物选择<br>
                  3. <strong>睡眠质量</strong>：睡眠不足会影响血糖<br><br>
                  建议：调整晚餐时间提前到6-7点，选择低升糖指数食物，保证充足睡眠。
                </div>
              </div>
              <div class="typing-indicator">
                <div class="avatar bot">
                  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="20" height="20">
                    <path d="M9.75 17L9 20l-1 1h8l-1-1-.75-3M3 13h18M5 17h14a2 2 0 002-2V5a2 2 0 00-2-2H5a2 2 0 00-2 2v10a2 2 0 002 2z"/>
                  </svg>
                </div>
                <div class="typing-dots"><span></span><span></span><span></span></div>
              </div>
            </div>
            <div class="chat-input-area">
              <input type="text" placeholder="输入您的问题..." disabled class="demo-input"/>
              <button class="send-btn" disabled>
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="20" height="20">
                  <path d="M12 19l9 2-9-18-9 18 9-2zm0 0v-8"/>
                </svg>
              </button>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- 功能特色区域 -->
    <section class="features-section">
      <div class="container">
        <div class="section-header">
          <span class="section-badge">核心特色</span>
          <h2 class="section-title">智能对话功能</h2>
        </div>
        <div class="features-grid">
          <div class="feature-card">
            <div class="feature-icon">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M9.75 17L9 20l-1 1h8l-1-1-.75-3M3 13h18M5 17h14a2 2 0 002-2V5a2 2 0 00-2-2H5a2 2 0 00-2 2v10a2 2 0 002 2z"/>
              </svg>
            </div>
            <h3>智能理解</h3>
            <p>基于自然语言处理技术，准确理解用户的健康咨询需求</p>
          </div>
          <div class="feature-card">
            <div class="feature-icon">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M12 6.253v13m0-13C10.832 5.477 9.246 5 7.5 5S4.168 5.477 3 6.253v13C4.168 18.477 5.754 18 7.5 18s3.332.477 4.5 1.253m0-13C13.168 5.477 14.754 5 16.5 5c1.747 0 3.332.477 4.5 1.253v13C19.832 18.477 18.247 18 16.5 18c-1.746 0-3.332.477-4.5 1.253"/>
              </svg>
            </div>
            <h3>专业知识库</h3>
            <p>整合权威医学资料，提供准确可靠的健康指导信息</p>
          </div>
          <div class="feature-card">
            <div class="feature-icon">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z"/>
              </svg>
            </div>
            <h3>24小时服务</h3>
            <p>全天候在线服务，随时为您解答健康疑问</p>
          </div>
          <div class="feature-card">
            <div class="feature-icon">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z"/>
              </svg>
            </div>
            <h3>个性化服务</h3>
            <p>基于个人健康档案，提供定制化的健康管理建议</p>
          </div>
        </div>
      </div>
    </section>

    <!-- 即将上线区域 -->
    <section class="coming-soon-section">
      <div class="container">
        <div class="coming-soon-card">
          <div class="coming-soon-icon">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M8 12h.01M12 12h.01M16 12h.01M21 12c0 4.418-4.03 8-9 8a9.863 9.863 0 01-4.255-.949L3 20l1.395-3.72C3.512 15.042 3 13.574 3 12c0-4.418 4.03-8 9-8s9 3.582 9 8z"/>
            </svg>
          </div>
          <h2>AI智能对话即将上线</h2>
          <p>我们正在进行最后的优化和测试，确保为您提供最专业的健康咨询服务</p>
          <div class="progress-list">
            <div class="progress-item done"><svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="20" height="20"><path d="M5 13l4 4L19 7"/></svg><span>核心算法开发完成</span></div>
            <div class="progress-item done"><svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="20" height="20"><path d="M5 13l4 4L19 7"/></svg><span>医学知识库建设完成</span></div>
            <div class="progress-item current"><svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="20" height="20" class="spin"><path d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15"/></svg><span>安全测试与优化中</span></div>
            <div class="progress-item"><svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="20" height="20"><path d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z"/></svg><span>预计本月内上线</span></div>
          </div>
          <div class="action-buttons">
            <button class="btn-outline" @click="goToAssessment">体验智能评测</button>
            <button class="btn-primary" @click="goToLogin">咨询真人医生</button>
          </div>
        </div>
      </div>
    </section>
  </div>
</template>

<script>
export default {
  name: 'Chat',
  methods: {
    goToAssessment() { this.$router.push('/assessment') },
    goToLogin() { this.$router.push('/login') }
  }
}
</script>

<style scoped>
.chat-page { font-family: 'Noto Sans', sans-serif; color: #164E63; background: #ECFEFF; min-height: 100vh; }
.container { max-width: 1200px; margin: 0 auto; padding: 0 24px; }
.navbar { position: fixed; top: 0; left: 0; right: 0; background: rgba(255,255,255,0.95); backdrop-filter: blur(10px); border-bottom: 1px solid #A5F3FC; z-index: 1000; padding: 0 24px; }
.nav-container { max-width: 1200px; margin: 0 auto; display: flex; align-items: center; justify-content: space-between; height: 72px; }
.nav-brand { display: flex; align-items: center; gap: 10px; font-size: 1.25rem; font-weight: 700; color: #0891B2; text-decoration: none; }
.brand-icon { width: 32px; height: 32px; color: #0891B2; }
.nav-actions { display: flex; gap: 12px; }
.nav-links { display: flex; gap: 32px; }
.nav-link { color: #164E63; text-decoration: none; font-weight: 500; font-size: 0.95rem; transition: color 0.2s; }
.nav-link:hover, .nav-link.active { color: #0891B2; }
.btn-outline-nav { padding: 10px 20px; border: 2px solid #0891B2; border-radius: 8px; color: #0891B2; text-decoration: none; font-weight: 600; font-size: 0.9rem; transition: all 0.2s; }
.btn-outline-nav:hover { background: #0891B2; color: white; }
.btn-primary-nav { padding: 10px 20px; background: #059669; border: 2px solid #059669; border-radius: 8px; color: white; text-decoration: none; font-weight: 600; font-size: 0.9rem; transition: all 0.2s; }
.btn-primary-nav:hover { background: #047857; border-color: #047857; }
.hero-section { padding: 140px 0 80px; background: linear-gradient(135deg, #0891B2 0%, #0E7490 100%); position: relative; overflow: hidden; }
.hero-bg { position: absolute; top: 0; left: 0; right: 0; bottom: 0; background-image: radial-gradient(circle at 20% 80%, rgba(255,255,255,0.1) 0%, transparent 50%); }
.hero-content { position: relative; z-index: 1; text-align: center; max-width: 800px; margin: 0 auto; }
.hero-badges { display: flex; gap: 12px; justify-content: center; margin-bottom: 24px; }
.hero-badge { display: inline-flex; align-items: center; gap: 8px; background: rgba(255,255,255,0.2); padding: 8px 16px; border-radius: 50px; font-size: 0.875rem; color: white; font-weight: 500; }
.hero-badge.coming-soon { background: rgba(5,150,105,0.3); }
.hero-title { font-size: 3rem; font-weight: 800; color: white; margin: 0 0 20px; }
.hero-subtitle { font-size: 1.125rem; line-height: 1.7; color: rgba(255,255,255,0.9); margin: 0; }
.demo-section { padding: 100px 0; background: white; }
.section-header { text-align: center; margin-bottom: 48px; }
.section-badge { display: inline-block; background: rgba(8,145,178,0.1); color: #0891B2; padding: 8px 20px; border-radius: 50px; font-size: 0.875rem; font-weight: 600; margin-bottom: 16px; }
.section-title { font-size: 2.25rem; font-weight: 700; color: #164E63; margin: 0; }
.chat-demo { max-width: 700px; margin: 0 auto; }
.chat-container { background: white; border-radius: 20px; box-shadow: 0 20px 60px rgba(8,145,178,0.15); border: 1px solid #A5F3FC; overflow: hidden; }
.chat-header { background: linear-gradient(135deg, #0891B2 0%, #0E7490 100%); color: white; padding: 20px; display: flex; align-items: center; gap: 16px; }
.chat-avatar { width: 48px; height: 48px; background: rgba(255,255,255,0.2); border-radius: 12px; display: flex; align-items: center; justify-content: center; }
.chat-info h3 { margin: 0 0 4px; font-size: 1.1rem; font-weight: 600; }
.status { display: flex; align-items: center; gap: 6px; font-size: 0.875rem; opacity: 0.9; }
.status-dot { width: 8px; height: 8px; background: #22D3EE; border-radius: 50%; animation: pulse 2s infinite; }
@keyframes pulse { 0%, 100% { opacity: 1; } 50% { opacity: 0.5; } }
.chat-messages { padding: 24px; min-height: 300px; max-height: 400px; overflow-y: auto; background: #F8FAFC; }
.message { display: flex; margin-bottom: 20px; align-items: flex-start; gap: 12px; }
.message.user-message { flex-direction: row-reverse; }
.avatar { width: 36px; height: 36px; border-radius: 10px; display: flex; align-items: center; justify-content: center; flex-shrink: 0; }
.avatar.bot { background: linear-gradient(135deg, #0891B2 0%, #22D3EE 100%); color: white; }
.avatar.user { background: linear-gradient(135deg, #059669 0%, #10B981 100%); color: white; }
.message .content { background: white; padding: 14px 18px; border-radius: 16px; max-width: 75%; line-height: 1.6; font-size: 0.95rem; box-shadow: 0 2px 8px rgba(0,0,0,0.05); }
.user-message .content { background: linear-gradient(135deg, #059669 0%, #10B981 100%); color: white; }
.typing-indicator { display: flex; align-items: center; gap: 12px; }
.typing-dots { display: flex; gap: 4px; background: white; padding: 14px 18px; border-radius: 16px; box-shadow: 0 2px 8px rgba(0,0,0,0.05); }
.typing-dots span { width: 8px; height: 8px; background: #A5F3FC; border-radius: 50%; animation: typing 1.4s infinite ease-in-out; }
.typing-dots span:nth-child(2) { animation-delay: 0.2s; }
.typing-dots span:nth-child(3) { animation-delay: 0.4s; }
@keyframes typing { 0%, 60%, 100% { transform: translateY(0); opacity: 0.3; } 30% { transform: translateY(-8px); opacity: 1; } }
.chat-input-area { padding: 16px 20px; background: white; border-top: 1px solid #E2E8F0; display: flex; gap: 12px; }
.demo-input { flex: 1; border: 2px solid #E2E8F0; border-radius: 12px; padding: 12px 16px; font-size: 0.95rem; outline: none; opacity: 0.6; }
.send-btn { width: 48px; height: 48px; background: #0891B2; color: white; border: none; border-radius: 12px; display: flex; align-items: center; justify-content: center; cursor: pointer; opacity: 0.6; }
.features-section { padding: 100px 0; background: #ECFEFF; }
.features-grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: 24px; }
.feature-card { background: white; border: 1px solid #E2E8F0; border-radius: 20px; padding: 32px 24px; text-align: center; transition: all 0.3s; }
.feature-card:hover { border-color: #A5F3FC; box-shadow: 0 20px 40px rgba(8,145,178,0.1); transform: translateY(-4px); }
.feature-icon { width: 56px; height: 56px; background: linear-gradient(135deg, #0891B2 0%, #22D3EE 100%); border-radius: 14px; display: flex; align-items: center; justify-content: center; margin: 0 auto 20px; }
.feature-icon svg { width: 28px; height: 28px; color: white; }
.feature-card h3 { font-size: 1.1rem; font-weight: 700; color: #164E63; margin: 0 0 12px; }
.feature-card p { font-size: 0.875rem; color: #64748B; line-height: 1.6; margin: 0; }
.coming-soon-section { padding: 100px 0; background: white; }
.coming-soon-card { max-width: 500px; margin: 0 auto; text-align: center; background: #F8FAFC; border: 1px solid #E2E8F0; border-radius: 24px; padding: 48px; }
.coming-soon-icon { width: 80px; height: 80px; background: linear-gradient(135deg, #0891B2 0%, #22D3EE 100%); border-radius: 20px; display: flex; align-items: center; justify-content: center; margin: 0 auto 24px; }
.coming-soon-icon svg { width: 40px; height: 40px; color: white; }
.coming-soon-card h2 { font-size: 1.75rem; font-weight: 700; color: #164E63; margin: 0 0 16px; }
.coming-soon-card > p { font-size: 1rem; color: #64748B; margin: 0 0 32px; }
.progress-list { text-align: left; margin-bottom: 32px; }
.progress-item { display: flex; align-items: center; gap: 12px; padding: 12px 0; border-bottom: 1px solid #E2E8F0; font-size: 0.95rem; color: #64748B; }
.progress-item:last-child { border-bottom: none; }
.progress-item.done { color: #059669; }
.progress-item.done svg { color: #059669; }
.progress-item.current { color: #0891B2; font-weight: 600; }
.progress-item.current svg { color: #0891B2; }
.spin { animation: spin 2s linear infinite; }
@keyframes spin { from { transform: rotate(0deg); } to { transform: rotate(360deg); } }
.action-buttons { display: flex; gap: 12px; justify-content: center; }
.btn-outline { padding: 14px 24px; background: white; color: #0891B2; border: 2px solid #0891B2; border-radius: 12px; font-size: 0.95rem; font-weight: 600; cursor: pointer; transition: all 0.2s; }
.btn-outline:hover { background: #0891B2; color: white; }
.btn-primary { padding: 14px 24px; background: #059669; color: white; border: 2px solid #059669; border-radius: 12px; font-size: 0.95rem; font-weight: 600; cursor: pointer; transition: all 0.2s; }
.btn-primary:hover { background: #047857; border-color: #047857; }
@media (max-width: 1024px) { .features-grid { grid-template-columns: repeat(2, 1fr); } }
@media (max-width: 768px) {
  .hero-title { font-size: 2rem; }
  .features-grid { grid-template-columns: 1fr; }
  .action-buttons { flex-direction: column; }
  .hero-badges { flex-direction: column; align-items: center; }
  .nav-links { display: none; }
}
</style>
