<template>
  <div class="home-page">
    <!-- Hero 区域 -->
    <section class="hero-section">
      <div class="container">
        <div class="hero-content">
          <div class="hero-text">
            <h1 class="hero-title">
              智能化糖尿病 <span class="highlight">健康管理系统</span>
            </h1>
            <p class="hero-subtitle">
              结合人工智能技术，为糖尿病患者提供个性化的健康监测、智能评估和专业医疗咨询服务
            </p>
            <div class="hero-buttons">
              <button class="btn btn-primary" @click="startAssessment">开始智能评测</button>
              <button class="btn btn-outline" @click="startChat">智能咨询</button>
            </div>
          </div>
          <div class="hero-image">
            <div class="feature-card">
              <div class="feature-icon">📊</div>
              <h3>智能监测</h3>
              <p>实时记录血糖、血压等健康数据</p>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- 功能特色区域 -->
    <section class="features-section">
      <div class="container">
        <h2 class="section-title">核心功能</h2>
        <div class="features-grid">
          <div class="feature-item">
            <div class="feature-icon">🩺</div>
            <h3>智能健康监测</h3>
            <p>实时记录血糖、血压等健康数据，智能分析健康趋势</p>
          </div>
          <div class="feature-item">
            <div class="feature-icon">🤖</div>
            <h3>AI风险评估</h3>
            <p>基于大数据的个性化健康评估，提供专业的风险预警</p>
          </div>
          <div class="feature-item">
            <div class="feature-icon">👨‍⚕️</div>
            <h3>专业医疗咨询</h3>
            <p>7x24小时在线医疗咨询服务，专业医生团队实时解答</p>
          </div>
        </div>
      </div>
    </section>

    <!-- 优势展示区域 -->
    <section class="advantages-section">
      <div class="container">
        <h2 class="section-title">为什么选择我们？</h2>
        <div class="advantages-grid">
          <div class="advantage-item">
            <h3>智能健康监测</h3>
            <p>实时记录血糖、血压等健康数据</p>
          </div>
          <div class="advantage-item">
            <h3>AI风险评估</h3>
            <p>基于大数据的个性化健康评估</p>
          </div>
          <div class="advantage-item">
            <h3>专业医疗咨询</h3>
            <p>7x24小时在线咨询服务</p>
          </div>
        </div>
      </div>
    </section>

    <!-- 行动号召区域 -->
    <section class="cta-section">
      <div class="container">
        <div class="cta-content">
          <h2>立即开始您的健康管理之旅</h2>
          <p>加入我们，体验智能化的糖尿病管理服务</p>
          <div class="cta-buttons">
            <button class="btn btn-primary btn-large" @click="goToLogin">立即注册</button>
            <button class="btn btn-outline btn-large" @click="startAssessment">免费评测</button>
          </div>
        </div>
      </div>
    </section>
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
    goToLogin() {
      this.$router.push('/login')
    }
  }
}
</script>

<style scoped>
.home-page {
  background: #fff;
}

.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
}

.section-title {
  text-align: center;
  font-size: 2.5rem;
  font-weight: 700;
  margin-bottom: 50px;
  color: #333;
}

/* Hero 区域 */
.hero-section {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  padding: 100px 0;
}

.hero-content {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 60px;
  align-items: center;
}

.hero-title {
  font-size: 3rem;
  font-weight: 700;
  line-height: 1.2;
  margin-bottom: 20px;
}

.highlight {
  background: linear-gradient(45deg, #42d392, #42a5f5);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.hero-subtitle {
  font-size: 1.2rem;
  line-height: 1.6;
  margin-bottom: 40px;
  color: rgba(255, 255, 255, 0.9);
}

.hero-buttons {
  display: flex;
  gap: 20px;
}

.btn {
  padding: 15px 30px;
  border: none;
  border-radius: 30px;
  font-size: 1.1rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
}

.btn-primary {
  background: linear-gradient(45deg, #42d392, #42a5f5);
  color: white;
}

.btn-primary:hover {
  transform: translateY(-3px);
  box-shadow: 0 10px 25px rgba(66, 211, 146, 0.4);
}

.btn-outline {
  background: transparent;
  color: white;
  border: 2px solid rgba(255, 255, 255, 0.3);
}

.btn-outline:hover {
  background: rgba(255, 255, 255, 0.1);
  border-color: rgba(255, 255, 255, 0.5);
}

.hero-image .feature-card {
  background: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(10px);
  border-radius: 20px;
  padding: 40px;
  text-align: center;
}

.hero-image .feature-icon {
  font-size: 4rem;
  margin-bottom: 20px;
}

/* 功能特色区域 */
.features-section {
  padding: 100px 0;
  background: #f8fafc;
}

.features-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 40px;
}

.feature-item {
  background: white;
  padding: 40px;
  border-radius: 20px;
  text-align: center;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.1);
  transition: transform 0.3s ease;
}

.feature-item:hover {
  transform: translateY(-5px);
}

.feature-item .feature-icon {
  font-size: 3rem;
  margin-bottom: 20px;
}

.feature-item h3 {
  font-size: 1.5rem;
  font-weight: 600;
  margin-bottom: 15px;
  color: #333;
}

.feature-item p {
  color: #666;
  line-height: 1.6;
}

/* 优势展示区域 */
.advantages-section {
  padding: 100px 0;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.advantages-section .section-title {
  color: white;
}

.advantages-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 40px;
}

.advantage-item {
  text-align: center;
  padding: 30px 20px;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 15px;
  backdrop-filter: blur(10px);
}

.advantage-item h3 {
  font-size: 1.3rem;
  font-weight: 600;
  margin-bottom: 15px;
}

.advantage-item p {
  color: rgba(255, 255, 255, 0.9);
  line-height: 1.6;
}

/* 行动号召区域 */
.cta-section {
  padding: 100px 0;
  background: #f8fafc;
}

.cta-content {
  text-align: center;
}

.cta-content h2 {
  font-size: 2.5rem;
  font-weight: 700;
  margin-bottom: 20px;
  color: #333;
}

.cta-content p {
  font-size: 1.2rem;
  color: #666;
  margin-bottom: 40px;
}

.cta-buttons {
  display: flex;
  justify-content: center;
  gap: 20px;
}

.btn-large {
  padding: 18px 40px;
  font-size: 1.2rem;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .hero-content {
    grid-template-columns: 1fr;
    text-align: center;
    gap: 40px;
  }
  
  .hero-title {
    font-size: 2.5rem;
  }
  
  .section-title {
    font-size: 2rem;
  }
  
  .hero-buttons,
  .cta-buttons {
    flex-direction: column;
    align-items: center;
  }
  
  .features-grid,
  .advantages-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 480px) {
  .hero-section {
    padding: 60px 0;
  }
  
  .features-section,
  .advantages-section,
  .cta-section {
    padding: 60px 0;
  }
  
  .hero-title {
    font-size: 2rem;
  }
  
  .section-title {
    font-size: 1.8rem;
  }
}
</style> 