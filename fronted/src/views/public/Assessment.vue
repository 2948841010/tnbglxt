<template>
  <div class="assessment-page">
    <!-- Hero区域 -->
    <section class="hero-section">
      <div class="container">
        <div class="hero-content">
          <h1 class="hero-title">糖尿病智能评测</h1>
          <p class="hero-subtitle">
            基于人工智能算法，通过多维度健康数据分析，为您提供个性化的糖尿病风险评估
          </p>
          <div class="hero-badge">
            <el-tag type="info" size="large" effect="light">
              <i class="el-icon-time"></i>
              即将上线
            </el-tag>
          </div>
        </div>
      </div>
    </section>

    <!-- 功能预览区域 -->
    <section class="preview-section">
      <div class="container">
        <h2 class="section-title">功能预览</h2>
        <div class="preview-grid">
          <div class="preview-card">
            <div class="preview-icon">
              <i class="el-icon-document-checked"></i>
            </div>
            <h3>智能问卷评估</h3>
            <p>通过标准化健康问卷，收集您的基础健康信息和生活习惯数据</p>
            <div class="preview-features">
              <span class="feature-tag">生活习惯评估</span>
              <span class="feature-tag">家族病史分析</span>
              <span class="feature-tag">症状自查</span>
            </div>
          </div>

          <div class="preview-card">
            <div class="preview-icon">
              <i class="el-icon-data-analysis"></i>
            </div>
            <h3>AI风险分析</h3>
            <p>运用先进的机器学习算法，综合分析多项健康指标，精准评估糖尿病风险</p>
            <div class="preview-features">
              <span class="feature-tag">风险等级评估</span>
              <span class="feature-tag">患病概率分析</span>
              <span class="feature-tag">趋势预测</span>
            </div>
          </div>

          <div class="preview-card">
            <div class="preview-icon">
              <i class="el-icon-document"></i>
            </div>
            <h3>个性化报告</h3>
            <p>生成详细的健康评估报告，包含风险分析、健康建议和预防措施</p>
            <div class="preview-features">
              <span class="feature-tag">可视化报告</span>
              <span class="feature-tag">健康建议</span>
              <span class="feature-tag">预防方案</span>
            </div>
          </div>

          <div class="preview-card">
            <div class="preview-icon">
              <i class="el-icon-monitor"></i>
            </div>
            <h3>持续监测</h3>
            <p>建立个人健康档案，支持长期健康数据追踪和风险变化监控</p>
            <div class="preview-features">
              <span class="feature-tag">数据追踪</span>
              <span class="feature-tag">定期复评</span>
              <span class="feature-tag">健康提醒</span>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- 评估流程区域 -->
    <section class="process-section">
      <div class="container">
        <h2 class="section-title">评估流程</h2>
        <div class="process-steps">
          <div class="step-item">
            <div class="step-number">1</div>
            <div class="step-content">
              <h3>基础信息填写</h3>
              <p>填写年龄、性别、身高、体重等基本信息</p>
            </div>
          </div>
          <div class="step-arrow">
            <i class="el-icon-arrow-right"></i>
          </div>
          <div class="step-item">
            <div class="step-number">2</div>
            <div class="step-content">
              <h3>健康问卷调查</h3>
              <p>完成生活习惯、饮食结构、运动频率等问卷</p>
            </div>
          </div>
          <div class="step-arrow">
            <i class="el-icon-arrow-right"></i>
          </div>
          <div class="step-item">
            <div class="step-number">3</div>
            <div class="step-content">
              <h3>健康数据录入</h3>
              <p>输入血糖、血压等已有的健康检查数据</p>
            </div>
          </div>
          <div class="step-arrow">
            <i class="el-icon-arrow-right"></i>
          </div>
          <div class="step-item">
            <div class="step-number">4</div>
            <div class="step-content">
              <h3>AI智能分析</h3>
              <p>系统自动分析数据并生成评估报告</p>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- 即将上线区域 -->
    <section class="coming-soon-section">
      <div class="container">
        <div class="coming-soon-content">
          <div class="coming-soon-icon">
            <i class="el-icon-rocket"></i>
          </div>
          <h2>功能即将上线</h2>
          <p>我们正在紧锣密鼓地开发这一创新功能，预计将在近期与您见面</p>
          <div class="notify-section">
            <h3>获取上线通知</h3>
            <div class="notify-form">
              <el-input
                v-model="email"
                placeholder="请输入您的邮箱地址"
                size="large"
                class="email-input"
              >
                <template #prepend>
                  <i class="el-icon-message"></i>
                </template>
              </el-input>
              <el-button 
                type="primary" 
                size="large"
                @click="subscribeNotification"
                :loading="subscribing"
                class="subscribe-btn"
              >
                订阅通知
              </el-button>
            </div>
            <p class="notify-tip">功能上线后我们将第一时间通知您</p>
          </div>
          <div class="alternative-actions">
            <p>现在您也可以：</p>
            <div class="action-buttons">
              <el-button @click="goToChat" size="large">
                <i class="el-icon-chat-line-round"></i>
                咨询专家
              </el-button>
              <el-button @click="goToLogin" size="large" type="success">
                <i class="el-icon-user"></i>
                注册使用其他功能
              </el-button>
            </div>
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
    return {
      email: '',
      subscribing: false
    }
  },
  methods: {
    async subscribeNotification() {
      if (!this.email) {
        ElMessage.warning('请输入有效的邮箱地址')
        return
      }

      // 验证邮箱格式
      const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
      if (!emailRegex.test(this.email)) {
        ElMessage.warning('请输入有效的邮箱格式')
        return
      }

      this.subscribing = true
      
      // 模拟订阅请求
      setTimeout(() => {
        this.subscribing = false
        ElMessage.success('订阅成功！功能上线后我们将第一时间通知您')
        this.email = ''
      }, 1500)
    },
    goToChat() {
      this.$router.push('/chat')
    },
    goToLogin() {
      this.$router.push('/login')
    }
  }
}
</script>

<style scoped>
.assessment-page {
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
}

.hero-badge .el-tag {
  padding: 12px 24px;
  font-size: 16px;
  border-radius: 25px;
}

/* 功能预览区域样式 */
.preview-section {
  padding: 80px 0;
}

.section-title {
  text-align: center;
  font-size: 2.5rem;
  font-weight: 700;
  color: #2d3748;
  margin-bottom: 60px;
}

.preview-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 30px;
}

.preview-card {
  background: white;
  padding: 40px 30px;
  border-radius: 20px;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.1);
  text-align: center;
  transition: transform 0.3s ease;
  position: relative;
  overflow: hidden;
}

.preview-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 4px;
  background: linear-gradient(135deg, #667eea, #764ba2);
}

.preview-card:hover {
  transform: translateY(-10px);
}

.preview-icon {
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

.preview-card h3 {
  font-size: 1.5rem;
  font-weight: 600;
  color: #2d3748;
  margin-bottom: 15px;
}

.preview-card p {
  color: #718096;
  line-height: 1.6;
  margin-bottom: 20px;
}

.preview-features {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  justify-content: center;
}

.feature-tag {
  background: #e2e8f0;
  color: #4a5568;
  padding: 4px 12px;
  border-radius: 15px;
  font-size: 12px;
  font-weight: 500;
}

/* 评估流程区域样式 */
.process-section {
  background: linear-gradient(135deg, #f7fafc 0%, #e2e8f0 100%);
  padding: 80px 0;
}

.process-steps {
  display: flex;
  align-items: center;
  justify-content: center;
  flex-wrap: wrap;
  gap: 20px;
}

.step-item {
  text-align: center;
  max-width: 200px;
}

.step-number {
  width: 60px;
  height: 60px;
  background: linear-gradient(135deg, #667eea, #764ba2);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  font-weight: bold;
  color: white;
  margin: 0 auto 20px;
}

.step-content h3 {
  font-size: 1.2rem;
  font-weight: 600;
  color: #2d3748;
  margin-bottom: 10px;
}

.step-content p {
  color: #718096;
  font-size: 14px;
  line-height: 1.5;
}

.step-arrow {
  font-size: 24px;
  color: #cbd5e0;
}

/* 即将上线区域样式 */
.coming-soon-section {
  padding: 80px 0;
  background: white;
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

.notify-section {
  background: #f7fafc;
  padding: 40px;
  border-radius: 20px;
  margin-bottom: 40px;
}

.notify-section h3 {
  font-size: 1.5rem;
  font-weight: 600;
  color: #2d3748;
  margin-bottom: 20px;
}

.notify-form {
  display: flex;
  gap: 15px;
  margin-bottom: 15px;
  flex-wrap: wrap;
  justify-content: center;
}

.email-input {
  flex: 1;
  max-width: 300px;
}

.subscribe-btn {
  background: linear-gradient(45deg, #42d392, #42a5f5);
  border: none;
  border-radius: 8px;
  min-width: 120px;
}

.notify-tip {
  font-size: 14px;
  color: #718096;
  margin: 0;
}

.alternative-actions {
  margin-top: 30px;
}

.alternative-actions p {
  font-size: 1.1rem;
  color: #4a5568;
  margin-bottom: 20px;
}

.action-buttons {
  display: flex;
  gap: 15px;
  justify-content: center;
  flex-wrap: wrap;
}

.action-buttons .el-button {
  border-radius: 25px;
  padding: 12px 24px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .hero-title {
    font-size: 2rem;
  }
  
  .process-steps {
    flex-direction: column;
  }
  
  .step-arrow {
    transform: rotate(90deg);
  }
  
  .notify-form {
    flex-direction: column;
    align-items: stretch;
  }
  
  .email-input {
    max-width: none;
  }
  
  .action-buttons {
    flex-direction: column;
    align-items: stretch;
  }
}
</style> 