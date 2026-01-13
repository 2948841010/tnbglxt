<template>
  <div class="home-page">
    <!-- 导航栏 -->
    <nav class="navbar">
      <div class="nav-container">
        <div class="nav-brand">
          <svg class="brand-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M22 12h-4l-3 9L9 3l-3 9H2"/>
          </svg>
          <span>糖尿病智能管理</span>
        </div>
        <div class="nav-links">
          <a href="#features" class="nav-link">功能介绍</a>
          <a href="#advantages" class="nav-link">我们的优势</a>
          <router-link to="/assessment" class="nav-link">智能评测</router-link>
          <router-link to="/chat" class="nav-link">智能对话</router-link>
        </div>
        <div class="nav-actions">
          <router-link to="/login" class="btn-outline-nav">登录</router-link>
          <router-link to="/register" class="btn-primary-nav">免费注册</router-link>
        </div>
      </div>
    </nav>

    <!-- Hero 区域 -->
    <section class="hero-section">
      <div class="hero-bg-pattern"></div>
      <div class="container">
        <div class="hero-content">
          <div class="hero-text">
            <div class="hero-badge">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="16" height="16">
                <path d="M13 2L3 14h9l-1 8 10-12h-9l1-8z"/>
              </svg>
              <span>AI驱动的智能健康管理</span>
            </div>
            <h1 class="hero-title">
              专业的糖尿病<br/>
              <span class="highlight">智能健康管理系统</span>
            </h1>
            <p class="hero-subtitle">
              结合人工智能与专业医学知识，为糖尿病患者提供个性化的健康监测、智能风险评估和专业医疗咨询服务
            </p>
            <div class="hero-buttons">
              <button class="btn-primary" @click="startAssessment">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="20" height="20">
                  <path d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z"/>
                </svg>
                开始智能评测
              </button>
              <button class="btn-outline" @click="startChat">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="20" height="20">
                  <path d="M8 12h.01M12 12h.01M16 12h.01M21 12c0 4.418-4.03 8-9 8a9.863 9.863 0 01-4.255-.949L3 20l1.395-3.72C3.512 15.042 3 13.574 3 12c0-4.418 4.03-8 9-8s9 3.582 9 8z"/>
                </svg>
                AI智能咨询
              </button>
            </div>
            <div class="hero-stats">
              <div class="stat-item">
                <span class="stat-value">10,000+</span>
                <span class="stat-label">服务用户</span>
              </div>
              <div class="stat-divider"></div>
              <div class="stat-item">
                <span class="stat-value">98%</span>
                <span class="stat-label">满意度</span>
              </div>
              <div class="stat-divider"></div>
              <div class="stat-item">
                <span class="stat-value">24/7</span>
                <span class="stat-label">在线服务</span>
              </div>
            </div>
          </div>
          <div class="hero-visual">
            <div class="visual-card main-card">
              <div class="card-header">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="24" height="24">
                  <path d="M9 19v-6a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2a2 2 0 002-2zm0 0V9a2 2 0 012-2h2a2 2 0 012 2v10m-6 0a2 2 0 002 2h2a2 2 0 002-2m0 0V5a2 2 0 012-2h2a2 2 0 012 2v14a2 2 0 01-2 2h-2a2 2 0 01-2-2z"/>
                </svg>
                <span>健康数据监测</span>
              </div>
              <div class="card-chart">
                <div class="chart-bar" style="height: 60%"></div>
                <div class="chart-bar" style="height: 80%"></div>
                <div class="chart-bar" style="height: 45%"></div>
                <div class="chart-bar" style="height: 90%"></div>
                <div class="chart-bar" style="height: 70%"></div>
              </div>
              <div class="card-footer">
                <span class="status-dot"></span>
                <span>血糖趋势正常</span>
              </div>
            </div>
            <div class="visual-card float-card-1">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="20" height="20">
                <path d="M4.318 6.318a4.5 4.5 0 000 6.364L12 20.364l7.682-7.682a4.5 4.5 0 00-6.364-6.364L12 7.636l-1.318-1.318a4.5 4.5 0 00-6.364 0z"/>
              </svg>
              <span>心率 72 bpm</span>
            </div>
            <div class="visual-card float-card-2">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="20" height="20">
                <path d="M13 10V3L4 14h7v7l9-11h-7z"/>
              </svg>
              <span>血糖 5.8 mmol/L</span>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- 功能特色区域 -->
    <section id="features" class="features-section">
      <div class="container">
        <div class="section-header">
          <span class="section-badge">核心功能</span>
          <h2 class="section-title">全方位的健康管理服务</h2>
          <p class="section-desc">基于AI技术，为您提供专业、便捷、个性化的糖尿病健康管理解决方案</p>
        </div>
        <div class="features-grid">
          <div class="feature-card" @click="startAssessment">
            <div class="feature-icon">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M9 12l2 2 4-4m5.618-4.016A11.955 11.955 0 0112 2.944a11.955 11.955 0 01-8.618 3.04A12.02 12.02 0 003 9c0 5.591 3.824 10.29 9 11.622 5.176-1.332 9-6.03 9-11.622 0-1.042-.133-2.052-.382-3.016z"/>
              </svg>
            </div>
            <h3>智能风险评估</h3>
            <p>基于AI算法分析多维健康数据，精准评估糖尿病风险等级，提供个性化预防建议</p>
            <div class="feature-tags">
              <span class="tag">风险预测</span>
              <span class="tag">数据分析</span>
            </div>
          </div>
          <div class="feature-card">
            <div class="feature-icon">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M9 19v-6a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2a2 2 0 002-2zm0 0V9a2 2 0 012-2h2a2 2 0 012 2v10m-6 0a2 2 0 002 2h2a2 2 0 002-2m0 0V5a2 2 0 012-2h2a2 2 0 012 2v14a2 2 0 01-2 2h-2a2 2 0 01-2-2z"/>
              </svg>
            </div>
            <h3>健康数据监测</h3>
            <p>实时记录血糖、血压、体重等健康指标，智能分析数据趋势，及时预警异常情况</p>
            <div class="feature-tags">
              <span class="tag">实时监测</span>
              <span class="tag">趋势分析</span>
            </div>
          </div>
          <div class="feature-card" @click="startChat">
            <div class="feature-icon">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M8 12h.01M12 12h.01M16 12h.01M21 12c0 4.418-4.03 8-9 8a9.863 9.863 0 01-4.255-.949L3 20l1.395-3.72C3.512 15.042 3 13.574 3 12c0-4.418 4.03-8 9-8s9 3.582 9 8z"/>
              </svg>
            </div>
            <h3>AI智能问诊</h3>
            <p>24小时AI健康助手在线服务，基于专业医学知识库，解答您的健康疑问</p>
            <div class="feature-tags">
              <span class="tag">24小时在线</span>
              <span class="tag">专业解答</span>
            </div>
          </div>
          <div class="feature-card">
            <div class="feature-icon">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M17 20h5v-2a3 3 0 00-5.356-1.857M17 20H7m10 0v-2c0-.656-.126-1.283-.356-1.857M7 20H2v-2a3 3 0 015.356-1.857M7 20v-2c0-.656.126-1.283.356-1.857m0 0a5.002 5.002 0 019.288 0M15 7a3 3 0 11-6 0 3 3 0 016 0zm6 3a2 2 0 11-4 0 2 2 0 014 0zM7 10a2 2 0 11-4 0 2 2 0 014 0z"/>
              </svg>
            </div>
            <h3>专家在线咨询</h3>
            <p>连接专业内分泌科医生，提供一对一在线咨询服务，获取权威医疗建议</p>
            <div class="feature-tags">
              <span class="tag">专家团队</span>
              <span class="tag">一对一服务</span>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- 优势展示区域 -->
    <section id="advantages" class="advantages-section">
      <div class="container">
        <div class="section-header light">
          <span class="section-badge light">我们的优势</span>
          <h2 class="section-title">为什么选择我们？</h2>
          <p class="section-desc">专业、可靠、便捷的糖尿病健康管理平台</p>
        </div>
        <div class="advantages-grid">
          <div class="advantage-card">
            <div class="advantage-number">01</div>
            <h3>AI技术驱动</h3>
            <p>采用先进的人工智能算法，结合大数据分析，提供精准的健康评估和个性化建议</p>
          </div>
          <div class="advantage-card">
            <div class="advantage-number">02</div>
            <h3>专业医学支持</h3>
            <p>整合权威医学指南和最新研究成果，确保健康建议的科学性和可靠性</p>
          </div>
          <div class="advantage-card">
            <div class="advantage-number">03</div>
            <h3>全程健康管理</h3>
            <p>从风险评估到日常监测，从健康咨询到医生问诊，提供一站式健康管理服务</p>
          </div>
          <div class="advantage-card">
            <div class="advantage-number">04</div>
            <h3>数据安全保障</h3>
            <p>采用医疗级数据加密技术，严格保护用户隐私，确保健康数据安全</p>
          </div>
        </div>
      </div>
    </section>

    <!-- 行动号召区域 -->
    <section class="cta-section">
      <div class="container">
        <div class="cta-content">
          <h2>开启您的智能健康管理之旅</h2>
          <p>立即注册，免费体验AI智能评测和健康咨询服务</p>
          <div class="cta-buttons">
            <button class="btn-cta-primary" @click="goToRegister">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="20" height="20">
                <path d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z"/>
              </svg>
              免费注册
            </button>
            <button class="btn-cta-outline" @click="startAssessment">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="20" height="20">
                <path d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z"/>
              </svg>
              免费评测
            </button>
          </div>
        </div>
      </div>
    </section>

    <!-- 页脚 -->
    <footer class="footer">
      <div class="container">
        <div class="footer-content">
          <div class="footer-brand">
            <svg class="brand-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M22 12h-4l-3 9L9 3l-3 9H2"/>
            </svg>
            <span>糖尿病智能管理系统</span>
          </div>
          <p class="footer-desc">专业的糖尿病健康管理平台，为您的健康保驾护航</p>
          <p class="footer-copyright">© 2025 糖尿病智能管理系统. All rights reserved.</p>
        </div>
      </div>
    </footer>
  </div>
</template>

<script>
export default {
  name: 'HomePage',
  methods: {
    startAssessment() {
      this.$router.push('/assessment')
    },
    startChat() {
      this.$router.push('/chat')
    },
    goToRegister() {
      this.$router.push('/register')
    }
  }
}
</script>

<style scoped>
/* Healthcare配色变量 */
:root {
  --primary: #0891B2;
  --secondary: #22D3EE;
  --cta: #059669;
  --background: #ECFEFF;
  --text: #164E63;
  --border: #A5F3FC;
}

/* 全局样式 */
.home-page {
  font-family: 'Noto Sans', -apple-system, BlinkMacSystemFont, sans-serif;
  color: #164E63;
  background: #ECFEFF;
}

.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 24px;
}

/* 导航栏 */
.navbar {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  border-bottom: 1px solid #A5F3FC;
  z-index: 1000;
  padding: 0 24px;
}

.nav-container {
  max-width: 1200px;
  margin: 0 auto;
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 72px;
}

.nav-brand {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 1.25rem;
  font-weight: 700;
  color: #0891B2;
  font-family: 'Figtree', sans-serif;
}

.brand-icon {
  width: 32px;
  height: 32px;
  color: #0891B2;
}

.nav-links {
  display: flex;
  gap: 32px;
}

.nav-link {
  color: #164E63;
  text-decoration: none;
  font-weight: 500;
  font-size: 0.95rem;
  transition: color 0.2s;
}

.nav-link:hover {
  color: #0891B2;
}

.nav-actions {
  display: flex;
  gap: 12px;
}

.btn-outline-nav {
  padding: 10px 20px;
  border: 2px solid #0891B2;
  border-radius: 8px;
  color: #0891B2;
  text-decoration: none;
  font-weight: 600;
  font-size: 0.9rem;
  transition: all 0.2s;
}

.btn-outline-nav:hover {
  background: #0891B2;
  color: white;
}

.btn-primary-nav {
  padding: 10px 20px;
  background: #059669;
  border: 2px solid #059669;
  border-radius: 8px;
  color: white;
  text-decoration: none;
  font-weight: 600;
  font-size: 0.9rem;
  transition: all 0.2s;
}

.btn-primary-nav:hover {
  background: #047857;
  border-color: #047857;
}

/* Hero区域 */
.hero-section {
  padding: 140px 0 100px;
  background: linear-gradient(180deg, #ECFEFF 0%, #CFFAFE 100%);
  position: relative;
  overflow: hidden;
}

.hero-bg-pattern {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-image: radial-gradient(circle at 20% 80%, rgba(8, 145, 178, 0.08) 0%, transparent 50%),
                    radial-gradient(circle at 80% 20%, rgba(34, 211, 238, 0.08) 0%, transparent 50%);
  pointer-events: none;
}

.hero-content {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 60px;
  align-items: center;
  position: relative;
  z-index: 1;
}

.hero-badge {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  background: rgba(8, 145, 178, 0.1);
  border: 1px solid #A5F3FC;
  padding: 8px 16px;
  border-radius: 50px;
  font-size: 0.875rem;
  color: #0891B2;
  font-weight: 500;
  margin-bottom: 24px;
}

.hero-title {
  font-family: 'Figtree', sans-serif;
  font-size: 3.5rem;
  font-weight: 800;
  line-height: 1.1;
  color: #164E63;
  margin: 0 0 24px;
}

.hero-title .highlight {
  color: #0891B2;
}

.hero-subtitle {
  font-size: 1.125rem;
  line-height: 1.7;
  color: #475569;
  margin: 0 0 32px;
}

.hero-buttons {
  display: flex;
  gap: 16px;
  margin-bottom: 48px;
}

.btn-primary {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 16px 28px;
  background: #059669;
  color: white;
  border: none;
  border-radius: 12px;
  font-size: 1rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
}

.btn-primary:hover {
  background: #047857;
  transform: translateY(-2px);
  box-shadow: 0 8px 20px rgba(5, 150, 105, 0.3);
}

.btn-outline {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 16px 28px;
  background: white;
  color: #0891B2;
  border: 2px solid #0891B2;
  border-radius: 12px;
  font-size: 1rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
}

.btn-outline:hover {
  background: #0891B2;
  color: white;
  transform: translateY(-2px);
}

.hero-stats {
  display: flex;
  align-items: center;
  gap: 32px;
}

.stat-item {
  display: flex;
  flex-direction: column;
}

.stat-value {
  font-family: 'Figtree', sans-serif;
  font-size: 2rem;
  font-weight: 700;
  color: #0891B2;
}

.stat-label {
  font-size: 0.875rem;
  color: #64748B;
}

.stat-divider {
  width: 1px;
  height: 40px;
  background: #A5F3FC;
}

/* Hero视觉卡片 */
.hero-visual {
  position: relative;
  height: 400px;
}

.visual-card {
  background: white;
  border-radius: 16px;
  box-shadow: 0 20px 40px rgba(8, 145, 178, 0.15);
  border: 1px solid #A5F3FC;
}

.main-card {
  position: absolute;
  top: 20px;
  left: 20px;
  right: 20px;
  padding: 24px;
}

.card-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 24px;
  color: #164E63;
  font-weight: 600;
}

.card-header svg {
  color: #0891B2;
}

.card-chart {
  display: flex;
  align-items: flex-end;
  gap: 16px;
  height: 120px;
  padding: 16px 0;
}

.chart-bar {
  flex: 1;
  background: linear-gradient(180deg, #22D3EE 0%, #0891B2 100%);
  border-radius: 8px 8px 0 0;
  transition: height 0.3s;
}

.card-footer {
  display: flex;
  align-items: center;
  gap: 8px;
  padding-top: 16px;
  border-top: 1px solid #E2E8F0;
  font-size: 0.875rem;
  color: #059669;
}

.status-dot {
  width: 8px;
  height: 8px;
  background: #059669;
  border-radius: 50%;
  animation: pulse 2s infinite;
}

@keyframes pulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.5; }
}

.float-card-1, .float-card-2 {
  position: absolute;
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 14px 18px;
  font-size: 0.875rem;
  font-weight: 500;
  color: #164E63;
  animation: float 3s ease-in-out infinite;
}

.float-card-1 {
  bottom: 80px;
  left: -20px;
}

.float-card-1 svg {
  color: #EF4444;
}

.float-card-2 {
  bottom: 20px;
  right: -10px;
  animation-delay: 1.5s;
}

.float-card-2 svg {
  color: #0891B2;
}

@keyframes float {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-10px); }
}

/* 功能特色区域 */
.features-section {
  padding: 100px 0;
  background: white;
}

.section-header {
  text-align: center;
  margin-bottom: 60px;
}

.section-badge {
  display: inline-block;
  background: rgba(8, 145, 178, 0.1);
  color: #0891B2;
  padding: 8px 20px;
  border-radius: 50px;
  font-size: 0.875rem;
  font-weight: 600;
  margin-bottom: 16px;
}

.section-title {
  font-family: 'Figtree', sans-serif;
  font-size: 2.5rem;
  font-weight: 700;
  color: #164E63;
  margin: 0 0 16px;
}

.section-desc {
  font-size: 1.125rem;
  color: #64748B;
  max-width: 600px;
  margin: 0 auto;
}

.features-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 24px;
}

.feature-card {
  background: #F8FAFC;
  border: 1px solid #E2E8F0;
  border-radius: 20px;
  padding: 32px;
  transition: all 0.3s;
  cursor: pointer;
}

.feature-card:hover {
  background: white;
  border-color: #A5F3FC;
  box-shadow: 0 20px 40px rgba(8, 145, 178, 0.1);
  transform: translateY(-4px);
}

.feature-icon {
  width: 56px;
  height: 56px;
  background: linear-gradient(135deg, #0891B2 0%, #22D3EE 100%);
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 20px;
}

.feature-icon svg {
  width: 28px;
  height: 28px;
  color: white;
}

.feature-card h3 {
  font-family: 'Figtree', sans-serif;
  font-size: 1.25rem;
  font-weight: 700;
  color: #164E63;
  margin: 0 0 12px;
}

.feature-card p {
  font-size: 0.95rem;
  color: #64748B;
  line-height: 1.6;
  margin: 0 0 16px;
}

.feature-tags {
  display: flex;
  gap: 8px;
}

.tag {
  background: rgba(8, 145, 178, 0.1);
  color: #0891B2;
  padding: 6px 12px;
  border-radius: 6px;
  font-size: 0.75rem;
  font-weight: 500;
}

/* 优势展示区域 */
.advantages-section {
  padding: 100px 0;
  background: linear-gradient(135deg, #0891B2 0%, #0E7490 100%);
}

.section-header.light .section-badge {
  background: rgba(255, 255, 255, 0.2);
  color: white;
}

.section-header.light .section-badge.light {
  background: rgba(255, 255, 255, 0.2);
  color: white;
}

.section-header.light .section-title {
  color: white;
}

.section-header.light .section-desc {
  color: rgba(255, 255, 255, 0.8);
}

.advantages-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 24px;
}

.advantage-card {
  background: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 16px;
  padding: 32px 24px;
  text-align: center;
  transition: all 0.3s;
}

.advantage-card:hover {
  background: rgba(255, 255, 255, 0.15);
  transform: translateY(-4px);
}

.advantage-number {
  font-family: 'Figtree', sans-serif;
  font-size: 2.5rem;
  font-weight: 800;
  color: #22D3EE;
  margin-bottom: 16px;
}

.advantage-card h3 {
  font-family: 'Figtree', sans-serif;
  font-size: 1.125rem;
  font-weight: 700;
  color: white;
  margin: 0 0 12px;
}

.advantage-card p {
  font-size: 0.875rem;
  color: rgba(255, 255, 255, 0.8);
  line-height: 1.6;
  margin: 0;
}

/* CTA区域 */
.cta-section {
  padding: 80px 0;
  background: #ECFEFF;
}

.cta-content {
  text-align: center;
  max-width: 600px;
  margin: 0 auto;
}

.cta-content h2 {
  font-family: 'Figtree', sans-serif;
  font-size: 2rem;
  font-weight: 700;
  color: #164E63;
  margin: 0 0 16px;
}

.cta-content p {
  font-size: 1.125rem;
  color: #64748B;
  margin: 0 0 32px;
}

.cta-buttons {
  display: flex;
  gap: 16px;
  justify-content: center;
}

.btn-cta-primary {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 16px 32px;
  background: #059669;
  color: white;
  border: none;
  border-radius: 12px;
  font-size: 1rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
}

.btn-cta-primary:hover {
  background: #047857;
  transform: translateY(-2px);
  box-shadow: 0 8px 20px rgba(5, 150, 105, 0.3);
}

.btn-cta-outline {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 16px 32px;
  background: white;
  color: #0891B2;
  border: 2px solid #0891B2;
  border-radius: 12px;
  font-size: 1rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
}

.btn-cta-outline:hover {
  background: #0891B2;
  color: white;
}

/* 页脚 */
.footer {
  padding: 48px 0;
  background: #164E63;
  color: white;
}

.footer-content {
  text-align: center;
}

.footer-brand {
  display: inline-flex;
  align-items: center;
  gap: 10px;
  font-size: 1.25rem;
  font-weight: 700;
  color: white;
  margin-bottom: 16px;
}

.footer-brand .brand-icon {
  color: #22D3EE;
}

.footer-desc {
  color: rgba(255, 255, 255, 0.7);
  margin: 0 0 16px;
}

.footer-copyright {
  color: rgba(255, 255, 255, 0.5);
  font-size: 0.875rem;
  margin: 0;
}

/* 响应式设计 */
@media (max-width: 1024px) {
  .hero-content {
    grid-template-columns: 1fr;
    text-align: center;
  }
  
  .hero-visual {
    display: none;
  }
  
  .hero-stats {
    justify-content: center;
  }
  
  .hero-buttons {
    justify-content: center;
  }
  
  .advantages-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 768px) {
  .nav-links {
    display: none;
  }
  
  .hero-title {
    font-size: 2.5rem;
  }
  
  .features-grid {
    grid-template-columns: 1fr;
  }
  
  .advantages-grid {
    grid-template-columns: 1fr;
  }
  
  .hero-buttons {
    flex-direction: column;
    align-items: center;
  }
  
  .cta-buttons {
    flex-direction: column;
    align-items: center;
  }
}
</style>
