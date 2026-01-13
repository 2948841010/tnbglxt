<template>
  <div class="user-management">
    <div class="page-header">
    <h1 class="page-title">用户管理</h1>
      <div class="header-actions">
        <el-button @click="refreshData" :loading="loading">
          <el-icon><Refresh /></el-icon>
          刷新
        </el-button>
      </div>
    </div>

    <!-- 搜索区域 -->
    <el-card class="search-card" shadow="never">
      <el-row :gutter="20">
        <el-col :span="8">
          <el-input
            v-model="searchForm.keyword"
            placeholder="搜索用户名、真实姓名、邮箱"
            @keyup.enter="handleSearch"
            clearable
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
        </el-col>
        <el-col :span="5">
          <el-select v-model="searchForm.userType" placeholder="用户类型" clearable style="width: 100%">
            <el-option label="全部" value="" />
            <el-option label="普通用户" :value="0" />
            <el-option label="医生" :value="1" />
            <el-option label="管理员" :value="2" />
          </el-select>
        </el-col>
        <el-col :span="5">
          <el-select v-model="searchForm.status" placeholder="用户状态" clearable style="width: 100%">
            <el-option label="全部" value="" />
            <el-option label="启用" :value="1" />
            <el-option label="禁用" :value="0" />
          </el-select>
        </el-col>
        <el-col :span="6" class="search-buttons">
          <el-button type="primary" @click="handleSearch" :loading="loading">
            <el-icon><Search /></el-icon>
            搜索
          </el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-col>
      </el-row>
      <el-row :gutter="20" style="margin-top: 15px;">
        <el-col :span="8">
          <el-date-picker
            v-model="searchForm.dateRange"
            type="datetimerange"
            range-separator="至"
            start-placeholder="注册开始时间"
            end-placeholder="注册结束时间"
            format="YYYY-MM-DD HH:mm"
            value-format="YYYY-MM-DD HH:mm:ss"
            style="width: 100%"
          />
        </el-col>
      </el-row>
    </el-card>

    <!-- 数据统计 -->
    <el-row :gutter="20" class="stats-row">
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-item">
            <div class="stat-icon user-total">
              <el-icon><User /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.total }}</div>
              <div class="stat-label">总用户数</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-item">
            <div class="stat-icon user-active">
              <el-icon><UserFilled /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.active }}</div>
              <div class="stat-label">活跃用户</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-item">
            <div class="stat-icon user-new">
              <el-icon><Plus /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.newToday }}</div>
              <div class="stat-label">今日新增</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-item">
            <div class="stat-icon user-online">
              <el-icon><Connection /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.online }}</div>
              <div class="stat-label">在线用户</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 用户表格 -->
    <el-card shadow="never">
      <el-table 
        :data="userData" 
        v-loading="loading" 
        stripe
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55" />
        <el-table-column prop="id" label="用户ID" width="80" />
        <el-table-column label="头像" width="80">
          <template #default="{ row }">
            <el-avatar :size="40" :src="row.avatar">
              <el-icon><User /></el-icon>
            </el-avatar>
          </template>
        </el-table-column>
        <el-table-column prop="username" label="用户名" width="120" />
        <el-table-column prop="realName" label="真实姓名" width="100" />
        <el-table-column prop="email" label="邮箱" width="180" />
        <el-table-column prop="phone" label="手机号" width="120" />
        <el-table-column label="用户类型" width="100">
          <template #default="{ row }">
            <el-tag :type="getUserTypeColor(row.userType)">
              {{ getUserTypeText(row.userType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="80">
          <template #default="{ row }">
            <el-switch
              v-model="row.status"
              :active-value="1"
              :inactive-value="0"
              @change="handleStatusChange(row)"
              v-permission="'admin:user:status'"
            />
          </template>
        </el-table-column>
        <el-table-column prop="loginCount" label="登录次数" width="100" />
        <el-table-column prop="lastLoginTime" label="最后登录时间" width="160">
          <template #default="{ row }">
            {{ formatDateTime(row.lastLoginTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="注册时间" width="160">
          <template #default="{ row }">
            {{ formatDateTime(row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <el-button 
              link 
              type="primary" 
              @click="handleView(row)"
              v-permission="'admin:user:view'"
            >
              查看
            </el-button>
            <el-button 
              link 
              type="primary" 
              @click="handleEdit(row)"
              v-permission="'admin:user:edit'"
            >
              编辑
            </el-button>
            <el-button 
              link 
              type="danger" 
              @click="handleDelete(row)"
              v-permission="'admin:user:delete'"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="pagination.current"
          v-model:page-size="pagination.size"
          :page-sizes="[10, 20, 50, 100]"
          :total="pagination.total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <!-- 用户详情/编辑对话框 -->
    <el-dialog 
      :title="dialogTitle" 
      v-model="dialogVisible" 
      width="600px"
      @close="handleDialogClose"
    >
      <el-form :model="userForm" :rules="userRules" ref="userFormRef" label-width="100px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="userForm.username" :disabled="dialogMode === 'view'" />
        </el-form-item>
        <el-form-item label="真实姓名" prop="realName">
          <el-input v-model="userForm.realName" :disabled="dialogMode === 'view'" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="userForm.email" :disabled="dialogMode === 'view'" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="userForm.phone" :disabled="dialogMode === 'view'" />
        </el-form-item>
        <el-form-item label="用户类型" prop="userType">
          <el-select v-model="userForm.userType" :disabled="dialogMode === 'view'">
            <el-option label="普通用户" :value="0" />
            <el-option label="医生" :value="1" />
            <el-option label="管理员" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item label="性别" prop="gender">
          <el-radio-group v-model="userForm.gender" :disabled="dialogMode === 'view'">
            <el-radio value="男">男</el-radio>
            <el-radio value="女">女</el-radio>
            <el-radio value="未知">未知</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-switch
            v-model="userForm.status"
            :active-value="1"
            :inactive-value="0"
            :disabled="dialogMode === 'view'"
          />
        </el-form-item>
      </el-form>
      <template #footer v-if="dialogMode !== 'view'">
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSave" :loading="saving">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Search,
  Refresh,
  User,
  UserFilled,
  Plus,
  Connection
} from '@element-plus/icons-vue'
import dayjs from 'dayjs'
import { userAPI } from '@/api/user'
import { dashboardAPI } from '@/api/dashboard'

// 响应式数据
const loading = ref(false)
const saving = ref(false)
const userData = ref([])
const selectedUsers = ref([])

// 搜索表单
const searchForm = reactive({
  keyword: '',
  userType: '',
  status: '',
  dateRange: ''
})

// 分页
const pagination = reactive({
  current: 1,
  size: 20,
  total: 0
})

// 统计数据
const stats = reactive({
  total: 0,
  active: 0,
  newToday: 0,
  online: 0
})

// 对话框相关
const dialogVisible = ref(false)
const dialogMode = ref('view') // view, edit, create
const userFormRef = ref()
const userForm = reactive({
  id: '',
  username: '',
  realName: '',
  email: '',
  phone: '',
  userType: 0,
  gender: '未知',
  status: 1
})

const userRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度在 3 到 20 个字符', trigger: 'blur' }
  ],
  realName: [
    { required: true, message: '请输入真实姓名', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ]
}

// 计算属性
const dialogTitle = computed(() => {
  const titles = {
    view: '查看用户',
    edit: '编辑用户',
    create: '创建用户'
  }
  return titles[dialogMode.value]
})

// 页面初始化
onMounted(() => {
  loadUserData()
})

// 加载用户数据
const loadUserData = async () => {
  loading.value = true
  
  try {
    // 构建查询参数
    const params = {
      page: pagination.current,
      size: pagination.size
    }
    
    // 添加搜索条件
    if (searchForm.keyword) {
      params.keyword = searchForm.keyword
    }
    if (searchForm.userType !== '') {
      params.userType = searchForm.userType
    }
    if (searchForm.status !== '') {
      params.status = searchForm.status
    }
    if (searchForm.dateRange && searchForm.dateRange.length === 2) {
      params.startTime = searchForm.dateRange[0]
      params.endTime = searchForm.dateRange[1]
    }
    
    // 调用API获取用户列表
    const response = await userAPI.getUserList(params)
    
    if (response.success) {
      userData.value = response.data.records || []
      pagination.total = response.data.total || 0
      
      // 同时获取用户统计数据
      await loadUserStats()
      
    } else {
      ElMessage.error(response.message || '获取用户列表失败')
      userData.value = []
      pagination.total = 0
    }
    
  } catch (error) {
    console.error('获取用户列表失败:', error)
    ElMessage.error('获取用户列表失败')
    userData.value = []
    pagination.total = 0
  } finally {
    loading.value = false
  }
}

// 加载用户统计数据
const loadUserStats = async () => {
  try {
    const response = await dashboardAPI.getUserStats()
    
    if (response.success && response.data) {
      stats.total = response.data.total || 0
      stats.active = response.data.active || 0
      stats.newToday = response.data.newToday || 0
      stats.online = response.data.online || 0
    }
  } catch (error) {
    console.error('获取用户统计数据失败:', error)
  }
}

// 工具函数
const getUserTypeText = (type) => {
  const typeMap = {
    0: '普通用户',
    1: '医生',
    2: '管理员'
  }
  return typeMap[type] || '未知'
}

const getUserTypeColor = (type) => {
  const colorMap = {
    0: '',
    1: 'success',
    2: 'warning'
  }
  return colorMap[type] || ''
}

const formatDateTime = (dateTime) => {
  if (!dateTime) return '-'
  return dayjs(dateTime).format('YYYY-MM-DD HH:mm')
}

// 事件处理
const refreshData = () => {
  loadUserData()
  ElMessage.success('数据已刷新')
}

const handleSearch = () => {
  pagination.current = 1 // 搜索时重置到第一页
  loadUserData()
}

const handleReset = () => {
  Object.keys(searchForm).forEach(key => {
    searchForm[key] = ''
  })
  pagination.current = 1
  loadUserData()
}

const handleSelectionChange = (selection) => {
  selectedUsers.value = selection
}

const handleStatusChange = async (row) => {
  const originalStatus = row.status === 1 ? 0 : 1 // 获取原始状态
  
  try {
    await ElMessageBox.confirm(
      `确定要${row.status ? '启用' : '禁用'}用户"${row.realName}"吗？`,
      '状态变更确认',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    // 调用API更新用户状态
    const response = await userAPI.updateUserStatus(row.id, row.status)
    
    if (response.success) {
      ElMessage.success(`用户状态已${row.status ? '启用' : '禁用'}`)
      // 重新加载数据以确保状态同步
      loadUserData()
    } else {
      ElMessage.error(response.message || '状态更新失败')
      // 恢复原状态
      row.status = originalStatus
    }
    
  } catch (error) {
    if (error === 'cancel') {
      // 用户取消，恢复原状态
      row.status = originalStatus
    } else {
      console.error('更新用户状态失败:', error)
      ElMessage.error('状态更新失败')
      // 恢复原状态
      row.status = originalStatus
    }
  }
}

const handleView = async (row) => {
  try {
    // 获取用户详情
    const response = await userAPI.getUserDetail(row.id)
    
    if (response.success && response.data) {
      dialogMode.value = 'view'
      Object.keys(userForm).forEach(key => {
        userForm[key] = response.data[key] || ''
      })
      dialogVisible.value = true
    } else {
      ElMessage.error('获取用户详情失败')
    }
  } catch (error) {
    console.error('获取用户详情失败:', error)
    ElMessage.error('获取用户详情失败')
  }
}

const handleEdit = async (row) => {
  try {
    // 获取用户详情
    const response = await userAPI.getUserDetail(row.id)
    
    if (response.success && response.data) {
      dialogMode.value = 'edit'
      Object.keys(userForm).forEach(key => {
        userForm[key] = response.data[key] || ''
      })
      dialogVisible.value = true
    } else {
      ElMessage.error('获取用户详情失败')
    }
  } catch (error) {
    console.error('获取用户详情失败:', error)
    ElMessage.error('获取用户详情失败')
  }
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除用户"${row.realName}"吗？此操作不可恢复。`,
      '删除确认',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'error'
      }
    )
    
    const response = await userAPI.deleteUser(row.id)
    
    if (response.success) {
      ElMessage.success('用户删除成功')
      // 重新加载数据
      loadUserData()
    } else {
      ElMessage.error(response.message || '用户删除失败')
    }
    
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除用户失败:', error)
      ElMessage.error('删除用户失败')
    }
  }
}

const handleSave = async () => {
  try {
    await userFormRef.value.validate()
    saving.value = true
    
    const response = await userAPI.updateUser(userForm.id, userForm)
    
    if (response.success) {
      ElMessage.success('用户信息保存成功')
      dialogVisible.value = false
      // 重新加载数据
      loadUserData()
    } else {
      ElMessage.error(response.message || '保存失败')
    }
    
  } catch (error) {
    if (error?.constructor === Object) {
      ElMessage.error('请检查表单信息')
    } else {
      console.error('保存用户信息失败:', error)
      ElMessage.error('保存失败')
    }
  } finally {
    saving.value = false
  }
}

const handleDialogClose = () => {
  userFormRef.value?.resetFields()
}

const handleSizeChange = (size) => {
  pagination.size = size
  pagination.current = 1
  loadUserData()
}

const handleCurrentChange = (current) => {
  pagination.current = current
  loadUserData()
}
</script>

<style scoped>
.user-management {
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.page-title {
  margin: 0;
  font-size: 24px;
  font-weight: 600;
  color: #333;
}

.search-card {
  margin-bottom: 20px;
}

.search-buttons {
  display: flex;
  gap: 10px;
  align-items: center;
}

.stats-row {
  margin-bottom: 20px;
}

.stat-card {
  transition: transform 0.2s;
}

.stat-card:hover {
  transform: translateY(-2px);
}

.stat-item {
  display: flex;
  align-items: center;
}

.stat-icon {
  width: 50px;
  height: 50px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  color: white;
  margin-right: 16px;
}

.stat-icon.user-total {
  background: linear-gradient(135deg, #667eea, #764ba2);
}

.stat-icon.user-active {
  background: linear-gradient(135deg, #f093fb, #f5576c);
}

.stat-icon.user-new {
  background: linear-gradient(135deg, #4facfe, #00f2fe);
}

.stat-icon.user-online {
  background: linear-gradient(135deg, #43e97b, #38f9d7);
}

.stat-info {
  flex: 1;
}

.stat-value {
  font-size: 24px;
  font-weight: 600;
  color: #333;
  line-height: 1;
}

.stat-label {
  font-size: 14px;
  color: #666;
  margin-top: 4px;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}

.search-buttons {
  display: flex;
  gap: 10px;
  align-items: center;
}
</style> 