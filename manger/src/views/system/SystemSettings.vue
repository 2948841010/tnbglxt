<template>
  <div class="system-settings">
    <div class="page-header">
    <h1 class="page-title">系统设置</h1>
    </div>

    <el-row :gutter="20">
      <!-- 配置菜单 -->
      <el-col :span="6">
        <el-card shadow="never" class="menu-card">
          <el-menu :default-active="activeMenu" @select="handleMenuSelect">
            <el-menu-item index="basic">
              <el-icon><Setting /></el-icon>
              <span>基础配置</span>
            </el-menu-item>
            <el-menu-item index="email">
              <el-icon><Message /></el-icon>
              <span>邮箱配置</span>
            </el-menu-item>
            <el-menu-item index="storage">
              <el-icon><FolderOpened /></el-icon>
              <span>文件存储</span>
            </el-menu-item>
            <el-menu-item index="database">
              <el-icon><DataBoard /></el-icon>
              <span>数据库配置</span>
            </el-menu-item>
            <el-menu-item index="security">
              <el-icon><Lock /></el-icon>
              <span>安全设置</span>
            </el-menu-item>
            <el-menu-item index="notification">
              <el-icon><Bell /></el-icon>
              <span>通知设置</span>
            </el-menu-item>
          </el-menu>
        </el-card>
      </el-col>

      <!-- 配置内容 -->
      <el-col :span="18">
        <el-card shadow="never">
          <!-- 基础配置 -->
          <div v-if="activeMenu === 'basic'" class="config-section">
            <div class="section-header">
              <h3>基础配置</h3>
              <p class="section-desc">系统基本参数设置</p>
            </div>

            <el-form :model="basicConfig" label-width="140px" class="config-form">
              <el-form-item label="系统名称">
                <el-input v-model="basicConfig.systemName" placeholder="糖尿病智能服务管理系统" />
              </el-form-item>
              <el-form-item label="系统版本">
                <el-input v-model="basicConfig.systemVersion" placeholder="1.0.0" />
              </el-form-item>
              <el-form-item label="系统描述">
                <el-input 
                  v-model="basicConfig.systemDescription" 
                  type="textarea" 
                  :rows="3"
                  placeholder="系统功能描述"
                />
              </el-form-item>
              <el-form-item label="系统Logo">
                <el-upload
                  class="logo-uploader"
                  :show-file-list="false"
                  :before-upload="beforeLogoUpload"
                  action="#"
                >
                  <img v-if="basicConfig.systemLogo" :src="basicConfig.systemLogo" class="logo">
                  <el-icon v-else class="logo-uploader-icon"><Plus /></el-icon>
                </el-upload>
              </el-form-item>
              <el-form-item label="用户注册">
                <el-switch v-model="basicConfig.allowRegister" />
                <span class="form-hint">是否允许用户自主注册</span>
              </el-form-item>
              <el-form-item label="医生注册">
                <el-switch v-model="basicConfig.allowDoctorRegister" />
                <span class="form-hint">是否允许医生自主注册</span>
              </el-form-item>
              <el-form-item label="默认页面大小">
                <el-input-number v-model="basicConfig.defaultPageSize" :min="10" :max="100" />
                <span class="form-hint">列表页面默认显示数量</span>
              </el-form-item>
              <el-form-item label="文件上传限制">
                <el-input-number v-model="basicConfig.maxFileSize" :min="1" :max="100" />
                <span class="form-hint">单个文件最大尺寸(MB)</span>
              </el-form-item>
            </el-form>
          </div>

          <!-- 邮箱配置 -->
          <div v-else-if="activeMenu === 'email'" class="config-section">
            <div class="section-header">
              <h3>邮箱配置</h3>
              <p class="section-desc">系统邮件发送相关配置</p>
            </div>

            <el-form :model="emailConfig" label-width="140px" class="config-form">
              <el-form-item label="SMTP服务器">
                <el-input v-model="emailConfig.smtpHost" placeholder="smtp.example.com" />
              </el-form-item>
              <el-form-item label="SMTP端口">
                <el-input-number v-model="emailConfig.smtpPort" :min="1" :max="65535" />
              </el-form-item>
              <el-form-item label="发件人邮箱">
                <el-input v-model="emailConfig.fromEmail" placeholder="noreply@example.com" />
              </el-form-item>
              <el-form-item label="发件人名称">
                <el-input v-model="emailConfig.fromName" placeholder="糖尿病智能服务系统" />
              </el-form-item>
              <el-form-item label="邮箱用户名">
                <el-input v-model="emailConfig.username" placeholder="邮箱登录用户名" />
              </el-form-item>
              <el-form-item label="邮箱密码">
                <el-input v-model="emailConfig.password" type="password" placeholder="邮箱密码或授权码" show-password />
              </el-form-item>
              <el-form-item label="启用SSL">
                <el-switch v-model="emailConfig.enableSsl" />
                <span class="form-hint">是否启用SSL加密</span>
              </el-form-item>
              <el-form-item label="启用邮件">
                <el-switch v-model="emailConfig.enableEmail" />
                <span class="form-hint">是否启用邮件发送功能</span>
              </el-form-item>
            </el-form>

            <div class="test-section">
              <el-button type="primary" @click="testEmail" :loading="testingEmail">测试邮件发送</el-button>
            </div>
          </div>

          <!-- 文件存储配置 -->
          <div v-else-if="activeMenu === 'storage'" class="config-section">
            <div class="section-header">
              <h3>文件存储配置</h3>
              <p class="section-desc">文件上传和存储相关设置</p>
            </div>

            <el-form :model="storageConfig" label-width="140px" class="config-form">
              <el-form-item label="存储方式">
                <el-radio-group v-model="storageConfig.storageType">
                  <el-radio value="local">本地存储</el-radio>
                  <el-radio value="oss">阿里云OSS</el-radio>
                  <el-radio value="qiniu">七牛云</el-radio>
                </el-radio-group>
              </el-form-item>

              <!-- 本地存储配置 -->
              <template v-if="storageConfig.storageType === 'local'">
                <el-form-item label="存储路径">
                  <el-input v-model="storageConfig.localPath" placeholder="/uploads/" />
                </el-form-item>
                <el-form-item label="访问域名">
                  <el-input v-model="storageConfig.localDomain" placeholder="http://localhost:8080" />
                </el-form-item>
              </template>

              <!-- OSS配置 -->
              <template v-if="storageConfig.storageType === 'oss'">
                <el-form-item label="Endpoint">
                  <el-input v-model="storageConfig.ossEndpoint" placeholder="oss-cn-hangzhou.aliyuncs.com" />
                </el-form-item>
                <el-form-item label="Access Key ID">
                  <el-input v-model="storageConfig.ossAccessKeyId" placeholder="OSS Access Key ID" />
                </el-form-item>
                <el-form-item label="Access Key Secret">
                  <el-input v-model="storageConfig.ossAccessKeySecret" type="password" placeholder="OSS Access Key Secret" show-password />
                </el-form-item>
                <el-form-item label="Bucket名称">
                  <el-input v-model="storageConfig.ossBucketName" placeholder="my-bucket" />
                </el-form-item>
                <el-form-item label="自定义域名">
                  <el-input v-model="storageConfig.ossCustomDomain" placeholder="https://cdn.example.com" />
                </el-form-item>
              </template>

              <el-form-item label="允许的文件类型">
                <el-input v-model="storageConfig.allowedFileTypes" placeholder="jpg,jpeg,png,gif,pdf,doc,docx" />
                <span class="form-hint">用逗号分隔多个文件类型</span>
              </el-form-item>
            </el-form>
          </div>

          <!-- 数据库配置 -->
          <div v-else-if="activeMenu === 'database'" class="config-section">
            <div class="section-header">
              <h3>数据库配置</h3>
              <p class="section-desc">数据库连接和性能相关设置</p>
            </div>

            <el-alert
              title="注意"
              type="warning"
              description="数据库配置修改后需要重启系统才能生效，请谨慎操作"
              show-icon
              :closable="false"
              style="margin-bottom: 20px;"
            />

            <el-form :model="databaseConfig" label-width="140px" class="config-form">
              <el-form-item label="数据库类型">
                <el-select v-model="databaseConfig.type" disabled>
                  <el-option label="MySQL" value="mysql" />
                </el-select>
                <span class="form-hint">当前只支持MySQL数据库</span>
              </el-form-item>
              <el-form-item label="数据库地址">
                <el-input v-model="databaseConfig.host" placeholder="localhost" />
              </el-form-item>
              <el-form-item label="端口">
                <el-input-number v-model="databaseConfig.port" :min="1" :max="65535" />
              </el-form-item>
              <el-form-item label="数据库名">
                <el-input v-model="databaseConfig.database" placeholder="tlbglxt" />
              </el-form-item>
              <el-form-item label="用户名">
                <el-input v-model="databaseConfig.username" placeholder="root" />
              </el-form-item>
              <el-form-item label="密码">
                <el-input v-model="databaseConfig.password" type="password" placeholder="数据库密码" show-password />
              </el-form-item>
              <el-form-item label="连接池大小">
                <el-input-number v-model="databaseConfig.poolSize" :min="5" :max="100" />
                <span class="form-hint">数据库连接池最大连接数</span>
              </el-form-item>
              <el-form-item label="MongoDB地址">
                <el-input v-model="databaseConfig.mongoUri" placeholder="mongodb://localhost:27017/tlbglxt" />
                <span class="form-hint">用于存储聊天记录</span>
              </el-form-item>
            </el-form>

            <div class="test-section">
              <el-button type="primary" @click="testDatabase" :loading="testingDatabase">测试数据库连接</el-button>
            </div>
          </div>

          <!-- 安全设置 -->
          <div v-else-if="activeMenu === 'security'" class="config-section">
            <div class="section-header">
              <h3>安全设置</h3>
              <p class="section-desc">系统安全相关配置</p>
            </div>

            <el-form :model="securityConfig" label-width="140px" class="config-form">
              <el-form-item label="JWT过期时间">
                <el-input-number v-model="securityConfig.jwtExpiration" :min="1" :max="168" />
                <span class="form-hint">JWT令牌过期时间(小时)</span>
              </el-form-item>
              <el-form-item label="密码最小长度">
                <el-input-number v-model="securityConfig.minPasswordLength" :min="6" :max="20" />
                <span class="form-hint">用户密码最小长度</span>
              </el-form-item>
              <el-form-item label="密码复杂度">
                <el-switch v-model="securityConfig.passwordComplexity" />
                <span class="form-hint">是否要求密码包含数字、字母、特殊字符</span>
              </el-form-item>
              <el-form-item label="登录失败锁定">
                <el-switch v-model="securityConfig.enableAccountLock" />
                <span class="form-hint">多次登录失败后是否锁定账户</span>
              </el-form-item>
              <el-form-item label="最大失败次数" v-if="securityConfig.enableAccountLock">
                <el-input-number v-model="securityConfig.maxLoginFailures" :min="3" :max="10" />
                <span class="form-hint">账户锁定前的最大失败次数</span>
              </el-form-item>
              <el-form-item label="锁定时间(分钟)" v-if="securityConfig.enableAccountLock">
                <el-input-number v-model="securityConfig.lockDuration" :min="5" :max="1440" />
                <span class="form-hint">账户锁定持续时间</span>
              </el-form-item>
              <el-form-item label="会话超时">
                <el-input-number v-model="securityConfig.sessionTimeout" :min="30" :max="480" />
                <span class="form-hint">用户会话超时时间(分钟)</span>
              </el-form-item>
            </el-form>
          </div>

          <!-- 通知设置 -->
          <div v-else-if="activeMenu === 'notification'" class="config-section">
            <div class="section-header">
              <h3>通知设置</h3>
              <p class="section-desc">系统通知和提醒相关配置</p>
            </div>

            <el-form :model="notificationConfig" label-width="140px" class="config-form">
              <el-form-item label="邮件通知">
                <el-switch v-model="notificationConfig.enableEmailNotification" />
                <span class="form-hint">启用邮件通知功能</span>
              </el-form-item>
              <el-form-item label="短信通知">
                <el-switch v-model="notificationConfig.enableSmsNotification" />
                <span class="form-hint">启用短信通知功能</span>
              </el-form-item>
              <el-form-item label="用户注册通知">
                <el-switch v-model="notificationConfig.notifyOnUserRegister" />
                <span class="form-hint">新用户注册时通知管理员</span>
              </el-form-item>
              <el-form-item label="医生注册通知">
                <el-switch v-model="notificationConfig.notifyOnDoctorRegister" />
                <span class="form-hint">新医生注册时通知管理员</span>
              </el-form-item>
              <el-form-item label="咨询完成通知">
                <el-switch v-model="notificationConfig.notifyOnConsultationComplete" />
                <span class="form-hint">咨询完成时通知相关人员</span>
              </el-form-item>
              <el-form-item label="系统异常通知">
                <el-switch v-model="notificationConfig.notifyOnSystemError" />
                <span class="form-hint">系统出现异常时通知管理员</span>
              </el-form-item>
              <el-form-item label="通知邮箱">
                <el-input v-model="notificationConfig.adminEmail" placeholder="admin@example.com" />
                <span class="form-hint">接收系统通知的邮箱地址</span>
              </el-form-item>
            </el-form>
          </div>

          <!-- 保存按钮 -->
          <div class="save-section">
            <el-button type="primary" @click="saveConfig" :loading="saving">
              <el-icon><Check /></el-icon>
              保存配置
            </el-button>
            <el-button @click="resetConfig">
              <el-icon><RefreshRight /></el-icon>
              重置
            </el-button>
          </div>
    </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import {
  Setting,
  Message,
  FolderOpened,
  DataBoard,
  Lock,
  Bell,
  Plus,
  Check,
  RefreshRight
} from '@element-plus/icons-vue'

// 当前激活的菜单
const activeMenu = ref('basic')

// 保存状态
const saving = ref(false)
const testingEmail = ref(false)
const testingDatabase = ref(false)

// 基础配置
const basicConfig = reactive({
  systemName: '糖尿病智能服务管理系统',
  systemVersion: '1.0.0',
  systemDescription: '专为糖尿病患者提供智能化医疗服务的在线平台',
  systemLogo: '',
  allowRegister: true,
  allowDoctorRegister: false,
  defaultPageSize: 20,
  maxFileSize: 10
})

// 邮箱配置
const emailConfig = reactive({
  smtpHost: 'smtp.example.com',
  smtpPort: 587,
  fromEmail: 'noreply@example.com',
  fromName: '糖尿病智能服务系统',
  username: '',
  password: '',
  enableSsl: true,
  enableEmail: true
})

// 存储配置
const storageConfig = reactive({
  storageType: 'local',
  localPath: '/uploads/',
  localDomain: 'http://localhost:8080',
  ossEndpoint: '',
  ossAccessKeyId: '',
  ossAccessKeySecret: '',
  ossBucketName: '',
  ossCustomDomain: '',
  allowedFileTypes: 'jpg,jpeg,png,gif,pdf,doc,docx'
})

// 数据库配置
const databaseConfig = reactive({
  type: 'mysql',
  host: 'localhost',
  port: 3306,
  database: 'tlbglxt',
  username: 'root',
  password: '',
  poolSize: 20,
  mongoUri: 'mongodb://localhost:27017/tlbglxt'
})

// 安全配置
const securityConfig = reactive({
  jwtExpiration: 24,
  minPasswordLength: 6,
  passwordComplexity: false,
  enableAccountLock: true,
  maxLoginFailures: 5,
  lockDuration: 30,
  sessionTimeout: 120
})

// 通知配置
const notificationConfig = reactive({
  enableEmailNotification: true,
  enableSmsNotification: false,
  notifyOnUserRegister: true,
  notifyOnDoctorRegister: true,
  notifyOnConsultationComplete: false,
  notifyOnSystemError: true,
  adminEmail: 'admin@example.com'
})

// 事件处理
const handleMenuSelect = (index) => {
  activeMenu.value = index
}

const beforeLogoUpload = (file) => {
  const isJPG = file.type === 'image/jpeg' || file.type === 'image/png'
  const isLt2M = file.size / 1024 / 1024 < 2

  if (!isJPG) {
    ElMessage.error('上传Logo只能是 JPG/PNG 格式!')
    return false
  }
  if (!isLt2M) {
    ElMessage.error('上传Logo大小不能超过 2MB!')
    return false
  }
  
  // 这里应该处理文件上传逻辑
  ElMessage.success('Logo上传成功（硬编码演示）')
  return false
}

const testEmail = () => {
  testingEmail.value = true
  
  setTimeout(() => {
    testingEmail.value = false
    ElMessage.success('邮件测试发送成功！请检查邮箱是否收到测试邮件。')
  }, 2000)
}

const testDatabase = () => {
  testingDatabase.value = true
  
  setTimeout(() => {
    testingDatabase.value = false
    ElMessage.success('数据库连接测试成功！')
  }, 2000)
}

const saveConfig = () => {
  saving.value = true
  
  setTimeout(() => {
    saving.value = false
    ElMessage.success('配置保存成功！')
  }, 1000)
}

const resetConfig = () => {
  ElMessage.info('配置重置功能暂未实现（硬编码演示）')
}
</script>

<style scoped>
.system-settings {
  padding: 20px;
}

.page-header {
  margin-bottom: 20px;
}

.page-title {
  margin: 0;
  font-size: 24px;
  font-weight: 600;
  color: #333;
}

.menu-card {
  height: fit-content;
}

.config-section {
  min-height: 600px;
}

.section-header {
  margin-bottom: 30px;
  border-bottom: 1px solid #ebeef5;
  padding-bottom: 15px;
}

.section-header h3 {
  margin: 0 0 8px 0;
  font-size: 18px;
  font-weight: 600;
  color: #333;
}

.section-desc {
  margin: 0;
  color: #666;
  font-size: 14px;
}

.config-form {
  max-width: 600px;
}

.form-hint {
  margin-left: 10px;
  color: #909399;
  font-size: 12px;
}

.logo-uploader .logo {
  width: 100px;
  height: 100px;
  display: block;
  border-radius: 6px;
}

.logo-uploader .logo-uploader-icon {
  font-size: 28px;
  color: #8c939d;
  width: 100px;
  height: 100px;
  text-align: center;
  line-height: 100px;
  border: 1px dashed #d9d9d9;
  border-radius: 6px;
  cursor: pointer;
  transition: border-color 0.2s;
}

.logo-uploader .logo-uploader-icon:hover {
  border-color: #409eff;
}

.test-section {
  margin-top: 30px;
  padding-top: 20px;
  border-top: 1px solid #ebeef5;
}

.save-section {
  margin-top: 40px;
  padding-top: 20px;
  border-top: 1px solid #ebeef5;
  text-align: center;
}

.save-section .el-button {
  margin: 0 10px;
}
</style> 