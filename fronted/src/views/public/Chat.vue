<template>
  <div class="chat-page">
    <!-- Hero区域 -->
    <section class="hero-section">
      <div class="container">
        <div class="hero-content">
          <h1 class="hero-title">AI智能健康对话</h1>
          <p class="hero-subtitle">
            24小时智能健康助手，为您提供专业的糖尿病健康咨询和日常护理指导
          </p>
          <div class="hero-badge">
            <el-tag type="warning" size="large" effect="light">
              <i class="el-icon-cpu"></i>
              AI技术驱动
            </el-tag>
            <el-tag type="info" size="large" effect="light">
              <i class="el-icon-time"></i>
              即将上线
            </el-tag>
          </div>
        </div>
      </div>
    </section>

    <!-- 对话演示区域 -->
    <section class="demo-section">
      <div class="container">
        <h2 class="section-title">智能对话演示</h2>
        <div class="chat-demo">
          <div class="chat-container">
            <div class="chat-header">
              <div class="chat-avatar">
                <i class="el-icon-robot"></i>
              </div>
              <div class="chat-info">
                <h3>AI健康助手</h3>
                <span class="status">在线服务中</span>
              </div>
            </div>
            <div class="chat-messages">
              <div class="message bot-message">
                <div class="avatar">
                  <i class="el-icon-robot"></i>
                </div>
                <div class="content">
                  您好！我是您的AI健康助手。我可以为您提供糖尿病相关的健康咨询，包括饮食建议、运动指导、血糖管理等。请问有什么可以帮助您的吗？
                </div>
              </div>
              <div class="message user-message">
                <div class="content">
                  我最近血糖控制不太好，早晨空腹血糖总是偏高，应该怎么办？
                </div>
                <div class="avatar">
                  <i class="el-icon-user"></i>
                </div>
              </div>
              <div class="message bot-message">
                <div class="avatar">
                  <i class="el-icon-robot"></i>
                </div>
                <div class="content">
                  晨间高血糖是常见问题，可能与以下因素有关：<br><br>
                  1. <strong>黎明现象</strong>：激素水平变化导致<br>
                  2. <strong>晚餐影响</strong>：晚餐时间或食物选择<br>
                  3. <strong>睡眠质量</strong>：睡眠不足会影响血糖<br><br>
                  建议：调整晚餐时间提前到6-7点，选择低升糖指数食物，保证充足睡眠。如持续异常，建议咨询医生调整用药。
                </div>
              </div>
              <div class="typing-indicator">
                <div class="avatar">
                  <i class="el-icon-robot"></i>
                </div>
                <div class="typing-dots">
                  <span></span>
                  <span></span>
                  <span></span>
                </div>
              </div>
            </div>
            <div class="chat-input-area">
              <div class="input-container">
                <el-input
                  v-model="demoMessage"
                  placeholder="输入您的问题..."
                  disabled
                  class="demo-input"
                >
                  <template #append>
                    <el-button icon="el-icon-s-promotion" disabled />
                  </template>
                </el-input>
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- 功能特色区域 -->
    <section class="features-section">
      <div class="container">
        <h2 class="section-title">核心特色</h2>
        <div class="features-grid">
          <div class="feature-card">
            <div class="feature-icon">
              <i class="el-icon-cpu"></i>
            </div>
            <h3>智能理解</h3>
            <p>基于自然语言处理技术，准确理解用户的健康咨询需求</p>
            <ul>
              <li>多轮对话理解</li>
              <li>上下文关联分析</li>
              <li>意图智能识别</li>
            </ul>
          </div>

          <div class="feature-card">
            <div class="feature-icon">
              <i class="el-icon-document"></i>
            </div>
            <h3>专业知识库</h3>
            <p>整合权威医学资料，提供准确可靠的健康指导信息</p>
            <ul>
              <li>医学指南数据库</li>
              <li>最新研究成果</li>
              <li>个性化建议生成</li>
            </ul>
          </div>

          <div class="feature-card">
            <div class="feature-icon">
              <i class="el-icon-clock"></i>
            </div>
            <h3>24小时服务</h3>
            <p>全天候在线服务，随时为您解答健康疑问</p>
            <ul>
              <li>实时响应咨询</li>
              <li>无等待时间</li>
              <li>多设备同步</li>
            </ul>
          </div>

          <div class="feature-card">
            <div class="feature-icon">
              <i class="el-icon-user"></i>
            </div>
            <h3>个性化服务</h3>
            <p>基于个人健康档案，提供定制化的健康管理建议</p>
            <ul>
              <li>个人健康画像</li>
              <li>定制化方案</li>
              <li>持续优化建议</li>
            </ul>
          </div>
        </div>
      </div>
    </section>

    <!-- 使用场景区域 -->
    <section class="scenarios-section">
      <div class="container">
        <h2 class="section-title">适用场景</h2>
        <div class="scenarios-grid">
          <div class="scenario-item">
            <div class="scenario-icon">
              <i class="el-icon-question"></i>
            </div>
            <h3>日常咨询</h3>
            <p>血糖异常、饮食疑问、运动指导等日常健康问题</p>
          </div>
          <div class="scenario-item">
            <div class="scenario-icon">
              <i class="el-icon-warning"></i>
            </div>
            <h3>紧急指导</h3>
            <p>血糖急剧变化、不适症状的初步处理建议</p>
          </div>
          <div class="scenario-item">
            <div class="scenario-icon">
              <i class="el-icon-notebook-2"></i>
            </div>
            <h3>知识学习</h3>
            <p>糖尿病相关知识普及、最新治疗方法了解</p>
          </div>
          <div class="scenario-item">
            <div class="scenario-icon">
              <i class="el-icon-data-line"></i>
            </div>
            <h3>数据解读</h3>
            <p>检查报告解读、血糖趋势分析、用药指导</p>
          </div>
        </div>
      </div>
    </section>

    <!-- 即将上线区域 -->
    <section class="coming-soon-section">
      <div class="container">
        <div class="coming-soon-content">
          <div class="coming-soon-icon">
            <i class="el-icon-chat-dot-round"></i>
          </div>
          <h2>AI智能对话即将上线</h2>
          <p>我们正在进行最后的优化和测试，确保为您提供最专业、最准确的健康咨询服务</p>
          
          <div class="progress-info">
            <div class="progress-item">
              <i class="el-icon-check"></i>
              <span>核心算法开发完成</span>
            </div>
            <div class="progress-item">
              <i class="el-icon-check"></i>
              <span>医学知识库建设完成</span>
            </div>
            <div class="progress-item current">
              <i class="el-icon-loading"></i>
              <span>安全测试与优化中</span>
            </div>
            <div class="progress-item">
              <i class="el-icon-time"></i>
              <span>预计本月内上线</span>
            </div>
          </div>

          <div class="temporary-alternatives">
            <h3>现在您可以</h3>
            <div class="action-buttons">
              <el-button size="large" @click="goToAssessment">
                <i class="el-icon-document-checked"></i>
                体验智能评测
              </el-button>
              <el-button size="large" type="success" @click="goToLogin">
                <i class="el-icon-user"></i>
                咨询真人医生
              </el-button>
            </div>
          </div>
        </div>
      </div>
    </section>
  </div>
</template>

<script>
export default {
  name: 'Chat',
  data() {
    return {
      demoMessage: ''
    }
  },
  methods: {
    goToAssessment() {
      this.$router.push('/assessment')
    },
    goToLogin() {
      this.$router.push('/login')
    }
  }
}
</script>

<style scoped>
.chat-page {
  background: #f8fafc;
}

.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
}

/* Hero区域样式 */
.hero-section {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  padding: 80px 0;
  text-align: center;
}

.hero-title {
  font-size: 3rem;
  font-weight: 700;
  margin-bottom: 20px;
}

.hero-subtitle {
  font-size: 1.25rem;
  line-height: 1.6;
  margin-bottom: 30px;
  opacity: 0.9;
  max-width: 800px;
  margin-left: auto;
  margin-right: auto;
}

.hero-badge {
  display: flex;
  justify-content: center;
  gap: 15px;
  flex-wrap: wrap;
}

.hero-badge .el-tag {
  padding: 12px 24px;
  font-size: 16px;
  border-radius: 25px;
}

/* 对话演示区域样式 */
.demo-section {
  padding: 80px 0;
  background: white;
}

.section-title {
  text-align: center;
  font-size: 2.5rem;
  font-weight: 700;
  color: #2d3748;
  margin-bottom: 60px;
}

.chat-demo {
  max-width: 800px;
  margin: 0 auto;
}

.chat-container {
  background: white;
  border-radius: 20px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.1);
  overflow: hidden;
}

.chat-header {
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: white;
  padding: 20px;
  display: flex;
  align-items: center;
  gap: 15px;
}

.chat-avatar {
  width: 50px;
  height: 50px;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
}

.chat-info h3 {
  margin: 0 0 5px 0;
  font-size: 18px;
  font-weight: 600;
}

.status {
  font-size: 14px;
  opacity: 0.9;
}

.chat-messages {
  padding: 30px;
  min-height: 400px;
  max-height: 500px;
  overflow-y: auto;
}

.message {
  display: flex;
  margin-bottom: 25px;
  align-items: flex-start;
  gap: 15px;
}

.message.user-message {
  flex-direction: row-reverse;
}

.message .avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
  flex-shrink: 0;
}

.bot-message .avatar {
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: white;
}

.user-message .avatar {
  background: linear-gradient(45deg, #42d392, #42a5f5);
  color: white;
}

.message .content {
  background: #f7fafc;
  padding: 15px 20px;
  border-radius: 18px;
  max-width: 70%;
  line-height: 1.6;
  position: relative;
}

.user-message .content {
  background: linear-gradient(45deg, #42d392, #42a5f5);
  color: white;
}

.bot-message .content::before {
  content: '';
  position: absolute;
  left: -8px;
  top: 15px;
  width: 0;
  height: 0;
  border-top: 8px solid transparent;
  border-bottom: 8px solid transparent;
  border-right: 8px solid #f7fafc;
}

.user-message .content::before {
  content: '';
  position: absolute;
  right: -8px;
  top: 15px;
  width: 0;
  height: 0;
  border-top: 8px solid transparent;
  border-bottom: 8px solid transparent;
  border-left: 8px solid #42d392;
}

.typing-indicator {
  display: flex;
  align-items: center;
  gap: 15px;
}

.typing-dots {
  display: flex;
  gap: 4px;
  background: #f7fafc;
  padding: 15px 20px;
  border-radius: 18px;
}

.typing-dots span {
  width: 8px;
  height: 8px;
  background: #cbd5e0;
  border-radius: 50%;
  animation: typing 1.4s infinite ease-in-out;
}

.typing-dots span:nth-child(2) {
  animation-delay: 0.2s;
}

.typing-dots span:nth-child(3) {
  animation-delay: 0.4s;
}

@keyframes typing {
  0%, 60%, 100% {
    transform: translateY(0);
    opacity: 0.3;
  }
  30% {
    transform: translateY(-10px);
    opacity: 1;
  }
}

.chat-input-area {
  padding: 20px;
  background: #f8fafc;
  border-top: 1px solid #e2e8f0;
}

.demo-input {
  opacity: 0.7;
}

/* 功能特色区域样式 */
.features-section {
  padding: 80px 0;
  background: #f8fafc;
}

.features-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: 30px;
}

.feature-card {
  background: white;
  padding: 40px 30px;
  border-radius: 20px;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.1);
  text-align: center;
  transition: transform 0.3s ease;
}

.feature-card:hover {
  transform: translateY(-10px);
}

.feature-icon {
  width: 80px;
  height: 80px;
  background: linear-gradient(135deg, #667eea, #764ba2);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 25px;
  font-size: 35px;
  color: white;
}

.feature-card h3 {
  font-size: 1.5rem;
  font-weight: 600;
  color: #2d3748;
  margin-bottom: 15px;
}

.feature-card p {
  color: #718096;
  line-height: 1.6;
  margin-bottom: 20px;
}

.feature-card ul {
  list-style: none;
  padding: 0;
  text-align: left;
}

.feature-card li {
  padding: 5px 0;
  color: #4a5568;
  position: relative;
  padding-left: 20px;
}

.feature-card li::before {
  content: '✓';
  position: absolute;
  left: 0;
  color: #42d392;
  font-weight: bold;
}

/* 使用场景区域样式 */
.scenarios-section {
  padding: 80px 0;
  background: white;
}

.scenarios-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 30px;
}

.scenario-item {
  text-align: center;
  padding: 30px 20px;
  border-radius: 15px;
  border: 2px solid #e2e8f0;
  transition: all 0.3s ease;
}

.scenario-item:hover {
  border-color: #667eea;
  transform: translateY(-5px);
}

.scenario-icon {
  width: 60px;
  height: 60px;
  background: linear-gradient(135deg, #667eea, #764ba2);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 20px;
  font-size: 30px;
  color: white;
}

.scenario-item h3 {
  font-size: 1.3rem;
  font-weight: 600;
  color: #2d3748;
  margin-bottom: 15px;
}

.scenario-item p {
  color: #718096;
  line-height: 1.6;
}

/* 即将上线区域样式 */
.coming-soon-section {
  padding: 80px 0;
  background: linear-gradient(135deg, #f7fafc 0%, #e2e8f0 100%);
}

.coming-soon-content {
  text-align: center;
  max-width: 600px;
  margin: 0 auto;
}

.coming-soon-icon {
  font-size: 80px;
  color: #667eea;
  margin-bottom: 30px;
}

.coming-soon-content h2 {
  font-size: 2.5rem;
  font-weight: 700;
  color: #2d3748;
  margin-bottom: 20px;
}

.coming-soon-content > p {
  font-size: 1.2rem;
  color: #718096;
  margin-bottom: 50px;
}

.progress-info {
  background: white;
  padding: 40px;
  border-radius: 20px;
  margin-bottom: 40px;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.1);
}

.progress-item {
  display: flex;
  align-items: center;
  gap: 15px;
  padding: 15px 0;
  border-bottom: 1px solid #e2e8f0;
  font-size: 16px;
}

.progress-item:last-child {
  border-bottom: none;
}

.progress-item i {
  font-size: 20px;
  width: 24px;
}

.progress-item.current {
  color: #667eea;
  font-weight: 600;
}

.progress-item.current i {
  animation: spin 2s linear infinite;
}

@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

.temporary-alternatives h3 {
  font-size: 1.5rem;
  font-weight: 600;
  color: #2d3748;
  margin-bottom: 25px;
}

.action-buttons {
  display: flex;
  gap: 20px;
  justify-content: center;
  flex-wrap: wrap;
}

.action-buttons .el-button {
  border-radius: 25px;
  padding: 15px 30px;
  font-size: 16px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .hero-title {
    font-size: 2rem;
  }
  
  .chat-messages {
    padding: 20px;
  }
  
  .message .content {
    max-width: 85%;
  }
  
  .hero-badge {
    flex-direction: column;
    align-items: center;
  }
  
  .action-buttons {
    flex-direction: column;
    align-items: stretch;
  }
}
</style> 