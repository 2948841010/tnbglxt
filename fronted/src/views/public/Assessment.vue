<template>
  <div class="assessment-page">
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
          <router-link to="/assessment" class="nav-link active">智能评测</router-link>
          <router-link to="/chat" class="nav-link">智能对话</router-link>
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
          <div class="hero-badge">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="16" height="16">
              <path d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z"/>
            </svg>
            <span>即将上线</span>
          </div>
          <h1 class="hero-title">糖尿病智能风险评测</h1>
          <p class="hero-subtitle">基于人工智能算法，通过多维度健康数据分析，为您提供个性化的糖尿病风险评估</p>
        </div>
      </div>
    </section>

    <!-- 功能预览区域 -->
    <section class="preview-section">
      <div class="container">
        <div class="section-header">
          <span class="section-badge">核心功能</span>
          <h2 class="section-title">智能评测功能预览</h2>
        </div>
        <div class="preview-grid">
          <div class="preview-card">
            <div class="preview-icon">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z"/>
              </svg>
            </div>
            <h3>智能问卷评估</h3>
            <p>通过标准化健康问卷，收集您的基础健康信息和生活习惯数据</p>
            <div class="preview-tags">
              <span class="tag">生活习惯</span>
              <span class="tag">家族病史</span>
            </div>
          </div>
          <div class="preview-card">
            <div class="preview-icon">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M9 19v-6a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2a2 2 0 002-2zm0 0V9a2 2 0 012-2h2a2 2 0 012 2v10m-6 0a2 2 0 002 2h2a2 2 0 002-2m0 0V5a2 2 0 012-2h2a2 2 0 012 2v14a2 2 0 01-2 2h-2a2 2 0 01-2-2z"/>
              </svg>
            </div>
            <h3>AI风险分析</h3>
            <p>运用先进的机器学习算法，综合分析多项健康指标，精准评估风险</p>
            <div class="preview-tags">
              <span class="tag">风险评估</span>
              <span class="tag">趋势预测</span>
            </div>
          </div>
          <div class="preview-card">
            <div class="preview-icon">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M9 17v-2m3 2v-4m3 4v-6m2 10H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z"/>
              </svg>
            </div>
            <h3>个性化报告</h3>
            <p>生成详细的健康评估报告，包含风险分析、健康建议和预防措施</p>
            <div class="preview-tags">
              <span class="tag">可视化报告</span>
              <span class="tag">健康建议</span>
            </div>
          </div>
          <div class="preview-card">
            <div class="preview-icon">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15"/>
              </svg>
            </div>
            <h3>持续监测</h3>
            <p>建立个人健康档案，支持长期健康数据追踪和风险变化监控</p>
            <div class="preview-tags">
              <span class="tag">数据追踪</span>
              <span class="tag">定期复评</span>
            </div>
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
              <path d="M13 10V3L4 14h7v7l9-11h-7z"/>
            </svg>
          </div>
          <h2>功能即将上线</h2>
          <p>我们正在紧锣密鼓地开发这一创新功能，预计将在近期与您见面</p>
          <div class="notify-form">
            <div class="input-group">
              <input type="email" v-model="email" placeholder="请输入您的邮箱地址" class="email-input"/>
            </div>
            <button class="subscribe-btn" @click="subscribeNotification" :disabled="subscribing">
              {{ subscribing ? '订阅中...' : '订阅通知' }}
            </button>
          </div>
          <div class="action-buttons">
            <button class="btn-outline" @click="goToChat">AI智能咨询</button>
            <button class="btn-primary" @click="goToLogin">注册使用</button>
          </div>
        </div>
      </div>
    </section>
  </div>
</template>

<script>
import { ElMessage } from 'element-plus'
export default {
  name: 'Assessment',
  data() {
    return { email: '', subscribing: false }
  },
  methods: {
    async subscribeNotification() {
      if (!this.email) { ElMessage.warning('请输入有效的邮箱地址'); return }
      const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
      if (!emailRegex.test(this.email)) { ElMessage.warning('请输入有效的邮箱格式'); return }
      this.subscribing = true
      setTimeout(() => { this.subscribing = false; ElMessage.success('订阅成功！'); this.email = '' }, 1500)
    },
    goToChat() { this.$router.push('/chat') },
    goToLogin() { this.$router.push('/login') }
  }
}
</script>

<style scoped>
.assessment-page { font-family: 'Noto Sans', sans-serif; color: #164E63; background: #ECFEFF; min-height: 100vh; }
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
.hero-badge { display: inline-flex; align-items: center; gap: 8px; background: rgba(255,255,255,0.2); padding: 8px 20px; border-radius: 50px; font-size: 0.875rem; color: white; font-weight: 500; margin-bottom: 24px; }
.hero-title { font-size: 3rem; font-weight: 800; color: white; margin: 0 0 20px; }
.hero-subtitle { font-size: 1.125rem; line-height: 1.7; color: rgba(255,255,255,0.9); margin: 0; }
.preview-section { padding: 100px 0; background: white; }
.section-header { text-align: center; margin-bottom: 60px; }
.section-badge { display: inline-block; background: rgba(8,145,178,0.1); color: #0891B2; padding: 8px 20px; border-radius: 50px; font-size: 0.875rem; font-weight: 600; margin-bottom: 16px; }
.section-title { font-size: 2.25rem; font-weight: 700; color: #164E63; margin: 0; }
.preview-grid { display: grid; grid-template-columns: repeat(2, 1fr); gap: 24px; }
.preview-card { background: #F8FAFC; border: 1px solid #E2E8F0; border-radius: 20px; padding: 32px; transition: all 0.3s; }
.preview-card:hover { background: white; border-color: #A5F3FC; box-shadow: 0 20px 40px rgba(8,145,178,0.1); transform: translateY(-4px); }
.preview-icon { width: 56px; height: 56px; background: linear-gradient(135deg, #0891B2 0%, #22D3EE 100%); border-radius: 14px; display: flex; align-items: center; justify-content: center; margin-bottom: 20px; }
.preview-icon svg { width: 28px; height: 28px; color: white; }
.preview-card h3 { font-size: 1.25rem; font-weight: 700; color: #164E63; margin: 0 0 12px; }
.preview-card p { font-size: 0.95rem; color: #64748B; line-height: 1.6; margin: 0 0 16px; }
.preview-tags { display: flex; flex-wrap: wrap; gap: 8px; }
.tag { background: rgba(8,145,178,0.1); color: #0891B2; padding: 6px 12px; border-radius: 6px; font-size: 0.75rem; font-weight: 500; }
.coming-soon-section { padding: 100px 0; background: #ECFEFF; }
.coming-soon-card { max-width: 500px; margin: 0 auto; text-align: center; background: white; border: 1px solid #E2E8F0; border-radius: 24px; padding: 48px; }
.coming-soon-icon { width: 80px; height: 80px; background: linear-gradient(135deg, #0891B2 0%, #22D3EE 100%); border-radius: 20px; display: flex; align-items: center; justify-content: center; margin: 0 auto 24px; }
.coming-soon-icon svg { width: 40px; height: 40px; color: white; }
.coming-soon-card h2 { font-size: 2rem; font-weight: 700; color: #164E63; margin: 0 0 16px; }
.coming-soon-card > p { font-size: 1rem; color: #64748B; margin: 0 0 32px; }
.notify-form { display: flex; gap: 12px; margin-bottom: 24px; }
.input-group { flex: 1; }
.email-input { width: 100%; border: 2px solid #E2E8F0; border-radius: 12px; padding: 14px 16px; font-size: 1rem; color: #164E63; outline: none; transition: border-color 0.2s; box-sizing: border-box; }
.email-input:focus { border-color: #0891B2; }
.subscribe-btn { padding: 14px 24px; background: #059669; color: white; border: none; border-radius: 12px; font-size: 1rem; font-weight: 600; cursor: pointer; transition: all 0.2s; white-space: nowrap; }
.subscribe-btn:hover:not(:disabled) { background: #047857; }
.subscribe-btn:disabled { opacity: 0.6; cursor: not-allowed; }
.action-buttons { display: flex; gap: 12px; justify-content: center; }
.btn-outline { padding: 14px 24px; background: white; color: #0891B2; border: 2px solid #0891B2; border-radius: 12px; font-size: 0.95rem; font-weight: 600; cursor: pointer; transition: all 0.2s; }
.btn-outline:hover { background: #0891B2; color: white; }
.btn-primary { padding: 14px 24px; background: #059669; color: white; border: 2px solid #059669; border-radius: 12px; font-size: 0.95rem; font-weight: 600; cursor: pointer; transition: all 0.2s; }
.btn-primary:hover { background: #047857; border-color: #047857; }
@media (max-width: 768px) {
  .hero-title { font-size: 2rem; }
  .preview-grid { grid-template-columns: 1fr; }
  .notify-form { flex-direction: column; }
  .action-buttons { flex-direction: column; }
  .nav-links { display: none; }
}
</style>
