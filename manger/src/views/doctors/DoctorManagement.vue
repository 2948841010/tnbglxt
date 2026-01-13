<template>
  <div class="doctor-management">
    <div class="page-header">
    <h1 class="page-title">医生管理</h1>
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
        <el-col :span="5">
          <el-input
            v-model="searchForm.keyword"
            placeholder="搜索医生姓名、用户名"
            @keyup.enter="handleSearch"
            clearable
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
        </el-col>
        <el-col :span="4">
          <el-select v-model="searchForm.department" placeholder="科室" clearable>
            <el-option label="全部" value="" />
            <el-option label="内科" value="内科" />
            <el-option label="外科" value="外科" />
            <el-option label="儿科" value="儿科" />
            <el-option label="妇产科" value="妇产科" />
            <el-option label="眼科" value="眼科" />
            <el-option label="耳鼻喉科" value="耳鼻喉科" />
            <el-option label="骨科" value="骨科" />
            <el-option label="皮肤科" value="皮肤科" />
            <el-option label="精神科" value="精神科" />
          </el-select>
        </el-col>
        <el-col :span="4">
          <el-select v-model="searchForm.title" placeholder="职称" clearable>
            <el-option label="全部" value="" />
            <el-option label="主任医师" value="主任医师" />
            <el-option label="副主任医师" value="副主任医师" />
            <el-option label="主治医师" value="主治医师" />
            <el-option label="住院医师" value="住院医师" />
          </el-select>
        </el-col>
        <el-col :span="3">
          <el-select v-model="searchForm.onlineStatus" placeholder="在线状态" clearable>
            <el-option label="全部" value="" />
            <el-option label="在线" :value="1" />
            <el-option label="离线" :value="0" />
          </el-select>
        </el-col>
        <el-col :span="4">
          <el-select v-model="searchForm.status" placeholder="用户状态" clearable>
            <el-option label="全部" value="" />
            <el-option label="正常" :value="1" />
            <el-option label="禁用" :value="0" />
          </el-select>
        </el-col>
        <el-col :span="4">
          <el-button type="primary" @click="handleSearch" :loading="loading">
            <el-icon><Search /></el-icon>
            搜索
          </el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-col>
      </el-row>
    </el-card>

    <!-- 数据统计 -->
    <el-row :gutter="20" class="stats-row">
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-item">
            <div class="stat-icon doctor-total">
              <el-icon><UserFilled /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.total }}</div>
              <div class="stat-label">总医生数</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-item">
            <div class="stat-icon doctor-online">
              <el-icon><Connection /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.online }}</div>
              <div class="stat-label">在线医生</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-item">
            <div class="stat-icon doctor-active">
              <el-icon><ChatDotRound /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.consulting }}</div>
              <div class="stat-label">咨询中</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-item">
            <div class="stat-icon doctor-available">
              <el-icon><CircleCheck /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.available }}</div>
              <div class="stat-label">可接诊</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 医生表格 -->
    <el-card shadow="never">
      <el-table 
        :data="doctorData" 
        v-loading="loading" 
        stripe
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55" />
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column label="头像" width="80">
          <template #default="{ row }">
            <el-avatar :size="40" :src="row.avatar">
              <el-icon><UserFilled /></el-icon>
            </el-avatar>
          </template>
        </el-table-column>
        <el-table-column label="医生姓名" width="100">
          <template #default="{ row }">
            {{ row.realName || row.real_name }}
          </template>
        </el-table-column>
        <el-table-column prop="username" label="用户名" width="120" />
        <el-table-column prop="department" label="科室" width="100" />
        <el-table-column prop="title" label="职称" width="120" />
        <el-table-column label="在线状态" width="90">
          <template #default="{ row }">
            <el-tag :type="(row.onlineStatus || row.online_status) === 1 ? 'success' : 'info'">
              {{ (row.onlineStatus || row.online_status) === 1 ? '在线' : '离线' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="可接诊" width="90">
          <template #default="{ row }">
            <el-switch
              v-model="row.available"
              :active-value="1"
              :inactive-value="0"
              @change="handleAvailableChange(row)"
              v-permission="'admin:doctor:status'"
            />
          </template>
        </el-table-column>
        <el-table-column label="用户状态" width="90">
          <template #default="{ row }">
            <el-switch
              v-model="row.status"
              :active-value="1"
              :inactive-value="0"
              @change="handleStatusChange(row)"
              v-permission="'admin:doctor:status'"
            />
          </template>
        </el-table-column>
        <el-table-column label="总咨询数" width="100">
          <template #default="{ row }">
            {{ row.consultationCount || row.consultation_count || 0 }}
          </template>
        </el-table-column>
        <el-table-column prop="rating" label="评分" width="80">
          <template #default="{ row }">
            <el-rate
              :model-value="row.rating"
              disabled
              show-score
              text-color="#ff9900"
              score-template="{value}"
              :max="5"
            />
          </template>
        </el-table-column>
        <el-table-column prop="phone" label="手机号" width="130" />
        <el-table-column prop="email" label="邮箱" width="180" show-overflow-tooltip />
        <el-table-column label="最后登录" width="160">
          <template #default="{ row }">
            {{ formatDateTime(row.lastLoginTime || row.last_login_time) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <el-button 
              link 
              type="primary" 
              @click="handleView(row)"
              v-permission="'admin:doctor:view'"
            >
              查看
            </el-button>
            <el-button 
              link 
              type="primary" 
              @click="handleEdit(row)"
              v-permission="'admin:doctor:edit'"
            >
              编辑
            </el-button>
            <el-button 
              link 
              type="danger" 
              @click="handleDelete(row)"
              v-permission="'admin:doctor:delete'"
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

    <!-- 医生详情/编辑对话框 -->
    <el-dialog 
      :title="dialogTitle" 
      v-model="dialogVisible" 
      width="700px"
      @close="handleDialogClose"
    >
      <el-form :model="doctorForm" :rules="doctorRules" ref="doctorFormRef" label-width="120px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="用户名" prop="username">
              <el-input v-model="doctorForm.username" :disabled="dialogMode === 'view'" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="医生姓名" prop="realName">
              <el-input v-model="doctorForm.realName" :disabled="dialogMode === 'view'" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="邮箱" prop="email">
              <el-input v-model="doctorForm.email" :disabled="dialogMode === 'view'" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="手机号" prop="phone">
              <el-input v-model="doctorForm.phone" :disabled="dialogMode === 'view'" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="科室" prop="department">
              <el-select v-model="doctorForm.department" :disabled="dialogMode === 'view'" style="width: 100%">
                <el-option label="内科" value="内科" />
                <el-option label="外科" value="外科" />
                <el-option label="儿科" value="儿科" />
                <el-option label="妇产科" value="妇产科" />
                <el-option label="眼科" value="眼科" />
                <el-option label="耳鼻喉科" value="耳鼻喉科" />
                <el-option label="骨科" value="骨科" />
                <el-option label="皮肤科" value="皮肤科" />
                <el-option label="精神科" value="精神科" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="职称" prop="title">
              <el-select v-model="doctorForm.title" :disabled="dialogMode === 'view'" style="width: 100%">
                <el-option label="主任医师" value="主任医师" />
                <el-option label="副主任医师" value="副主任医师" />
                <el-option label="主治医师" value="主治医师" />
                <el-option label="住院医师" value="住院医师" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="性别" prop="gender">
              <el-radio-group v-model="doctorForm.gender" :disabled="dialogMode === 'view'">
                <el-radio value="男">男</el-radio>
                <el-radio value="女">女</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="从业年限" prop="experience">
              <el-input-number 
                v-model="doctorForm.experience" 
                :min="0" 
                :max="50"
                :disabled="dialogMode === 'view'" 
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="可接诊状态" prop="available">
              <el-switch
                v-model="doctorForm.available"
                :active-value="1"
                :inactive-value="0"
                :disabled="dialogMode === 'view'"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="用户状态" prop="status">
              <el-switch
                v-model="doctorForm.status"
                :active-value="1"
                :inactive-value="0"
                :disabled="dialogMode === 'view'"
              />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="医生简介" prop="introduction">
          <el-input 
            v-model="doctorForm.introduction"
            type="textarea"
            :rows="4"
            placeholder="请输入医生简介"
            :disabled="dialogMode === 'view'"
          />
        </el-form-item>
        <el-form-item label="专长" prop="speciality">
          <el-input 
            v-model="doctorForm.speciality"
            type="textarea"
            :rows="2"
            placeholder="请输入专业特长"
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
  UserFilled,
  Connection,
  ChatDotRound,
  CircleCheck
} from '@element-plus/icons-vue'
import dayjs from 'dayjs'
import doctorAPI from '@/api/doctor'

// 响应式数据
const loading = ref(false)
const saving = ref(false)
const doctorData = ref([])
const selectedDoctors = ref([])

// 搜索表单
const searchForm = reactive({
  keyword: '',
  department: '',
  title: '',
  onlineStatus: '',
  status: ''
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
  online: 0,
  consulting: 0,
  available: 0
})

// 对话框相关
const dialogVisible = ref(false)
const dialogMode = ref('view') // view, edit, create
const doctorFormRef = ref()
const doctorForm = reactive({
  id: '',
  username: '',
  realName: '',
  email: '',
  phone: '',
  department: '',
  title: '',
  gender: '男',
  experience: 0,
  available: 1,
  status: 1,
  introduction: '',
  speciality: ''
})

const doctorRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度在 3 到 20 个字符', trigger: 'blur' }
  ],
  realName: [
    { required: true, message: '请输入医生姓名', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ],
  department: [
    { required: true, message: '请选择科室', trigger: 'change' }
  ],
  title: [
    { required: true, message: '请选择职称', trigger: 'change' }
  ]
}

// 计算属性
const dialogTitle = computed(() => {
  const titles = {
    view: '查看医生',
    edit: '编辑医生',
    create: '创建医生'
  }
  return titles[dialogMode.value]
})

// 页面初始化
onMounted(() => {
  loadDoctorData()
})

// 加载医生数据
const loadDoctorData = async () => {
  loading.value = true
  
  try {
    // 构建查询参数
    const params = {
      page: pagination.current,
      size: pagination.size,
      keyword: searchForm.keyword || undefined,
      department: searchForm.department || undefined,
      title: searchForm.title || undefined,
      onlineStatus: searchForm.onlineStatus !== '' ? searchForm.onlineStatus : undefined,
      status: searchForm.status !== '' ? searchForm.status : undefined
    }

    // 调用API获取医生列表
    const response = await doctorAPI.getDoctorList(params)
    
    if (response.success) {
      const result = response.data
      doctorData.value = result.records || []  // 修复：数据在records字段中
      pagination.total = result.total || 0
      
      // 同时获取统计数据
      await loadStatistics()
    } else {
      ElMessage.error(response.message || '获取医生列表失败')
      doctorData.value = []
      pagination.total = 0
    }
  } catch (error) {
    console.error('加载医生数据失败:', error)
    ElMessage.error('获取医生列表失败')
    doctorData.value = []
    pagination.total = 0
  } finally {
    loading.value = false
  }
}

// 加载统计数据
const loadStatistics = async () => {
  try {
    const response = await doctorAPI.getDoctorStatistics()
    if (response.success) {
      const data = response.data
      stats.total = data.total || 0
      stats.online = data.online || 0
      stats.consulting = data.consulting || 0
      stats.available = data.available || 0
    }
  } catch (error) {
    console.error('获取统计数据失败:', error)
  }
}

// 工具函数
const formatDateTime = (dateTime) => {
  if (!dateTime) return '-'
  return dayjs(dateTime).format('YYYY-MM-DD HH:mm')
}

// 事件处理
const refreshData = () => {
  loadDoctorData()
  ElMessage.success('数据已刷新')
}

const handleSearch = () => {
  // 重置到第一页并重新加载数据
  pagination.current = 1
  loadDoctorData()
}

const handleReset = () => {
  // 重置搜索表单
  Object.keys(searchForm).forEach(key => {
    searchForm[key] = ''
  })
  // 重置到第一页并重新加载数据
  pagination.current = 1
  loadDoctorData()
}

const handleSelectionChange = (selection) => {
  selectedDoctors.value = selection
}

const handleAvailableChange = async (row) => {
  const originalValue = row.available
  
  try {
    await ElMessageBox.confirm(
      `确定要${row.available ? '开启' : '关闭'}医生"${row.realName || row.real_name}"的接诊状态吗？`,
      '接诊状态变更确认',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    // 调用API更新状态
    const response = await doctorAPI.updateDoctorAvailable(row.id, row.available)
    
    if (response.success) {
    ElMessage.success(`医生接诊状态已${row.available ? '开启' : '关闭'}`)
      // 刷新统计数据
      await loadStatistics()
    } else {
      ElMessage.error(response.message || '更新失败')
      row.available = originalValue // 恢复原状态
    }
  } catch (error) {
    // 用户取消或API调用失败，恢复原状态
    row.available = originalValue
    if (error !== 'cancel') {
      console.error('更新医生接诊状态失败:', error)
      ElMessage.error('更新失败')
    }
  }
}

const handleStatusChange = async (row) => {
  const originalValue = row.status
  
  try {
    await ElMessageBox.confirm(
      `确定要${row.status ? '启用' : '禁用'}医生"${row.realName || row.real_name}"吗？`,
      '状态变更确认',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    // 调用API更新状态
    const response = await doctorAPI.updateDoctorStatus(row.id, row.status)
    
    if (response.success) {
    ElMessage.success(`医生状态已${row.status ? '启用' : '禁用'}`)
      // 刷新统计数据
      await loadStatistics()
    } else {
      ElMessage.error(response.message || '更新失败')
      row.status = originalValue // 恢复原状态
    }
  } catch (error) {
    // 用户取消或API调用失败，恢复原状态
    row.status = originalValue
    if (error !== 'cancel') {
      console.error('更新医生用户状态失败:', error)
      ElMessage.error('更新失败')
    }
  }
}

const handleView = async (row) => {
  try {
  dialogMode.value = 'view'
    
    // 获取医生详细信息
    const response = await doctorAPI.getDoctorDetail(row.id)
    
    if (response.success) {
      const detail = response.data
      
      // 填充表单数据，兼容不同的字段名
      doctorForm.id = detail.id
      doctorForm.username = detail.username
      doctorForm.realName = detail.realName || detail.real_name
      doctorForm.email = detail.email
      doctorForm.phone = detail.phone
      doctorForm.department = detail.department
      doctorForm.title = detail.title
      // 处理性别字段：API返回数字，转换为字符串
      doctorForm.gender = detail.gender === 1 ? '男' : detail.gender === 0 ? '女' : '男'
      doctorForm.experience = detail.experience || detail.work_years || 0
      doctorForm.available = detail.available
      doctorForm.status = detail.status
      doctorForm.introduction = detail.introduction || ''
      doctorForm.speciality = detail.speciality || ''
      
  dialogVisible.value = true
    } else {
      ElMessage.error(response.message || '获取医生详情失败')
    }
  } catch (error) {
    console.error('获取医生详情失败:', error)
    ElMessage.error('获取医生详情失败')
  }
}

const handleEdit = async (row) => {
  try {
  dialogMode.value = 'edit'
    
    // 获取医生详细信息
    const response = await doctorAPI.getDoctorDetail(row.id)
    
    if (response.success) {
      const detail = response.data
      
      // 填充表单数据，兼容不同的字段名
      doctorForm.id = detail.id
      doctorForm.username = detail.username
      doctorForm.realName = detail.realName || detail.real_name
      doctorForm.email = detail.email
      doctorForm.phone = detail.phone
      doctorForm.department = detail.department
      doctorForm.title = detail.title
      // 处理性别字段：API返回数字，转换为字符串
      doctorForm.gender = detail.gender === 1 ? '男' : detail.gender === 0 ? '女' : '男'
      doctorForm.experience = detail.experience || detail.work_years || 0
      doctorForm.available = detail.available
      doctorForm.status = detail.status
      doctorForm.introduction = detail.introduction || ''
      doctorForm.speciality = detail.speciality || ''
      
  dialogVisible.value = true
    } else {
      ElMessage.error(response.message || '获取医生详情失败')
    }
  } catch (error) {
    console.error('获取医生详情失败:', error)
    ElMessage.error('获取医生详情失败')
  }
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除医生"${row.realName || row.real_name}"吗？此操作不可恢复。`,
      '删除确认',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'error'
      }
    )
    
    // 调用API删除医生
    const response = await doctorAPI.deleteDoctor(row.id)
    
    if (response.success) {
      ElMessage.success('医生删除成功')
      // 重新加载数据
      await loadDoctorData()
    } else {
      ElMessage.error(response.message || '删除失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除医生失败:', error)
      ElMessage.error('删除失败')
    }
  }
}

const handleSave = async () => {
  try {
    await doctorFormRef.value.validate()
    saving.value = true
    
    // 调用API保存医生信息
    const response = await doctorAPI.updateDoctor(doctorForm.id, doctorForm)
    
    if (response.success) {
      saving.value = false
      dialogVisible.value = false
      ElMessage.success('医生信息保存成功')
      // 重新加载数据
      await loadDoctorData()
    } else {
      saving.value = false
      ElMessage.error(response.message || '保存失败')
    }
  } catch (error) {
    saving.value = false
    if (error.message) {
      // 表单验证错误
    ElMessage.error('请检查表单信息')
    } else {
      console.error('保存医生信息失败:', error)
      ElMessage.error('保存失败')
    }
  }
}

const handleDialogClose = () => {
  doctorFormRef.value?.resetFields()
}

const handleSizeChange = (size) => {
  pagination.size = size
  pagination.current = 1 // 重置到第一页
  loadDoctorData()
}

const handleCurrentChange = (current) => {
  pagination.current = current
  loadDoctorData()
}
</script>

<style scoped>
.doctor-management {
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

.stat-icon.doctor-total {
  background: linear-gradient(135deg, #667eea, #764ba2);
}

.stat-icon.doctor-online {
  background: linear-gradient(135deg, #43e97b, #38f9d7);
}

.stat-icon.doctor-active {
  background: linear-gradient(135deg, #f093fb, #f5576c);
}

.stat-icon.doctor-available {
  background: linear-gradient(135deg, #4facfe, #00f2fe);
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
</style> 