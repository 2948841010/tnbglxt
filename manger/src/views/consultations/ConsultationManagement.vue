<template>
  <div class="consultation-management">
    <div class="page-header">
    <h1 class="page-title">咨询管理</h1>
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
            placeholder="搜索咨询编号、医生、患者"
            @keyup.enter="handleSearch"
            clearable
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
        </el-col>
        <el-col :span="5">
          <el-select v-model="searchForm.status" placeholder="咨询状态" clearable style="width: 100%">
            <el-option label="全部" value="" />
            <el-option label="进行中" value="ongoing" />
            <el-option label="已完成" value="completed" />
            <el-option label="已取消" value="cancelled" />
            <el-option label="待接诊" value="pending" />
          </el-select>
        </el-col>
        <el-col :span="5">
          <el-select v-model="searchForm.department" placeholder="科室" clearable style="width: 100%">
            <el-option label="全部" value="" />
            <el-option label="内科" value="内科" />
            <el-option label="外科" value="外科" />
            <el-option label="儿科" value="儿科" />
            <el-option label="妇产科" value="妇产科" />
            <el-option label="眼科" value="眼科" />
            <el-option label="耳鼻喉科" value="耳鼻喉科" />
            <el-option label="骨科" value="骨科" />
            <el-option label="皮肤科" value="皮肤科" />
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
            start-placeholder="开始时间"
            end-placeholder="结束时间"
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
            <div class="stat-icon consultation-total">
              <el-icon><ChatDotRound /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.total }}</div>
              <div class="stat-label">总咨询数</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-item">
            <div class="stat-icon consultation-today">
              <el-icon><Calendar /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.today }}</div>
              <div class="stat-label">今日咨询</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-item">
            <div class="stat-icon consultation-ongoing">
              <el-icon><Loading /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.ongoing }}</div>
              <div class="stat-label">进行中</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-item">
            <div class="stat-icon consultation-completed">
              <el-icon><CircleCheck /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.completed }}</div>
              <div class="stat-label">已完成</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 咨询表格 -->
    <el-card shadow="never">
      <el-table 
        :data="consultationData" 
        v-loading="loading" 
        stripe
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55" />
        <el-table-column prop="consultationNo" label="咨询编号" width="180" />
        <el-table-column prop="patientName" label="患者姓名" width="100" />
        <el-table-column prop="doctorName" label="医生姓名" width="100" />
        <el-table-column prop="department" label="科室" width="100" />
        <el-table-column label="咨询状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusColor(row.status)">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="messageCount" label="消息数" width="80" />
        <el-table-column prop="duration" label="咨询时长" width="100" />
        <el-table-column label="评分" width="100">
          <template #default="{ row }">
            <el-rate
              v-if="row.rating"
              :model-value="row.rating"
              disabled
              show-score
              text-color="#ff9900"
              score-template="{value}"
              :max="5"
            />
            <span v-else class="text-muted">暂无评分</span>
          </template>
        </el-table-column>
        <el-table-column prop="startTime" label="开始时间" width="160">
          <template #default="{ row }">
            {{ formatDateTime(row.startTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="endTime" label="结束时间" width="160">
          <template #default="{ row }">
            {{ formatDateTime(row.endTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button 
              link 
              type="primary" 
              @click="handleView(row)"
              v-permission="'admin:consultation:view'"
            >
              查看详情
            </el-button>
            <el-button 
              link 
              type="danger" 
              @click="handleDelete(row)"
              v-permission="'admin:consultation:delete'"
              v-if="row.status === 'cancelled'"
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

    <!-- 咨询详情对话框 -->
    <el-dialog 
      title="咨询详情" 
      v-model="dialogVisible" 
      width="800px"
      @close="handleDialogClose"
    >
      <el-descriptions :column="2" border>
        <el-descriptions-item label="咨询编号">{{ consultationDetail.consultationNo }}</el-descriptions-item>
        <el-descriptions-item label="咨询状态">
          <el-tag :type="getStatusColor(consultationDetail.status)">
            {{ getStatusText(consultationDetail.status) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="患者姓名">{{ consultationDetail.patientName }}</el-descriptions-item>
        <el-descriptions-item label="医生姓名">{{ consultationDetail.doctorName }}</el-descriptions-item>
        <el-descriptions-item label="科室">{{ consultationDetail.department }}</el-descriptions-item>
        <el-descriptions-item label="咨询时长">{{ consultationDetail.duration }}</el-descriptions-item>
        <el-descriptions-item label="消息数量">{{ consultationDetail.messageCount }}</el-descriptions-item>
        <el-descriptions-item label="患者评分">
          <el-rate
            v-if="consultationDetail.rating"
            :model-value="consultationDetail.rating"
            disabled
            show-score
            text-color="#ff9900"
            score-template="{value}"
            :max="5"
          />
          <span v-else>暂无评分</span>
        </el-descriptions-item>
        <el-descriptions-item label="开始时间">{{ formatDateTime(consultationDetail.startTime) }}</el-descriptions-item>
        <el-descriptions-item label="结束时间">{{ formatDateTime(consultationDetail.endTime) }}</el-descriptions-item>
      </el-descriptions>

      <!-- 患者主诉 -->
      <el-divider content-position="left">患者主诉</el-divider>
      <el-card shadow="never" class="complaint-card">
        <p>{{ consultationDetail.complaint || '无' }}</p>
      </el-card>

      <!-- 医生诊断 -->
      <el-divider content-position="left">医生诊断</el-divider>
      <el-card shadow="never" class="diagnosis-card" v-if="consultationDetail.diagnosis">
        <p><strong>诊断结果：</strong>{{ consultationDetail.diagnosis.result }}</p>
        <p><strong>治疗建议：</strong>{{ consultationDetail.diagnosis.suggestion }}</p>
      </el-card>
      <el-card shadow="never" v-else>
        <p class="text-muted">医生暂未填写诊断信息</p>
      </el-card>

      <!-- 评价信息 -->
      <el-divider content-position="left">患者评价</el-divider>
      <el-card shadow="never" class="rating-card" v-if="consultationDetail.ratingComment">
        <div class="rating-info">
          <el-rate
            :model-value="consultationDetail.rating"
            disabled
            show-score
            text-color="#ff9900"
            score-template="{value}分"
            :max="5"
          />
        </div>
        <p class="rating-comment">{{ consultationDetail.ratingComment }}</p>
      </el-card>
      <el-card shadow="never" v-else>
        <p class="text-muted">患者暂未评价</p>
      </el-card>

      <template #footer>
        <el-button @click="dialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Search,
  Refresh,
  ChatDotRound,
  Calendar,
  Loading,
  CircleCheck
} from '@element-plus/icons-vue'
import dayjs from 'dayjs'

// 响应式数据
const loading = ref(false)
const consultationData = ref([])
const selectedConsultations = ref([])

// 搜索表单
const searchForm = reactive({
  keyword: '',
  status: '',
  department: '',
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
  today: 0,
  ongoing: 0,
  completed: 0
})

// 对话框相关
const dialogVisible = ref(false)
const consultationDetail = reactive({})

// 页面初始化
onMounted(() => {
  loadConsultationData()
})

// 加载咨询数据（硬编码）
const loadConsultationData = () => {
  loading.value = true
  
  // 模拟API延迟
  setTimeout(() => {
    // 硬编码咨询数据
    const mockConsultations = [
      {
        id: 1,
        consultationNo: 'C202401150001',
        patientName: '张三',
        doctorName: '李医生',
        department: '内科',
        status: 'completed',
        messageCount: 25,
        duration: '45分钟',
        rating: 4.8,
        startTime: '2024-01-15 09:30:00',
        endTime: '2024-01-15 10:15:00',
        complaint: '最近血糖控制不好，经常感到疲劳，想咨询一下饮食和用药方面的建议。',
        diagnosis: {
          result: '2型糖尿病血糖控制不佳',
          suggestion: '调整饮食结构，增加运动量，按医嘱服药'
        },
        ratingComment: '医生很专业，解答详细，非常满意！'
      },
      {
        id: 2,
        consultationNo: 'C202401150002',
        patientName: '王五',
        doctorName: '张教授',
        department: '儿科',
        status: 'ongoing',
        messageCount: 12,
        duration: '22分钟',
        rating: null,
        startTime: '2024-01-15 14:20:00',
        endTime: null,
        complaint: '孩子发烧已经两天了，体温38.5度，有点咳嗽，想问问需要去医院吗？',
        diagnosis: null,
        ratingComment: null
      },
      {
        id: 3,
        consultationNo: 'C202401140003',
        patientName: '赵六',
        doctorName: '王主任',
        department: '外科',
        status: 'completed',
        messageCount: 18,
        duration: '32分钟',
        rating: 4.9,
        startTime: '2024-01-14 16:45:00',
        endTime: '2024-01-14 17:17:00',
        complaint: '右下腹疼痛，怀疑是阑尾炎，想先咨询一下症状。',
        diagnosis: {
          result: '疑似急性阑尾炎',
          suggestion: '建议立即到医院急诊科就诊，进行相关检查'
        },
        ratingComment: '医生判断准确，建议及时，非常感谢！'
      },
      {
        id: 4,
        consultationNo: 'C202401140004',
        patientName: '李四',
        doctorName: '陈医生',
        department: '妇产科',
        status: 'cancelled',
        messageCount: 3,
        duration: '5分钟',
        rating: null,
        startTime: '2024-01-14 11:20:00',
        endTime: '2024-01-14 11:25:00',
        complaint: '怀孕初期，想咨询孕期注意事项。',
        diagnosis: null,
        ratingComment: null
      },
      {
        id: 5,
        consultationNo: 'C202401130005',
        patientName: '周七',
        doctorName: '刘医生',
        department: '眼科',
        status: 'pending',
        messageCount: 1,
        duration: '1分钟',
        rating: null,
        startTime: '2024-01-13 20:30:00',
        endTime: null,
        complaint: '眼睛干涩，视力模糊，工作时间长，想问问是什么情况。',
        diagnosis: null,
        ratingComment: null
      }
    ]
    
    consultationData.value = mockConsultations
    pagination.total = mockConsultations.length
    
    // 统计数据
    stats.total = mockConsultations.length
    stats.today = mockConsultations.filter(c => 
      dayjs(c.startTime).isSame(dayjs(), 'day')
    ).length
    stats.ongoing = mockConsultations.filter(c => c.status === 'ongoing').length
    stats.completed = mockConsultations.filter(c => c.status === 'completed').length
    
    loading.value = false
  }, 1000)
}

// 工具函数
const getStatusText = (status) => {
  const statusMap = {
    pending: '待接诊',
    ongoing: '进行中',
    completed: '已完成',
    cancelled: '已取消'
  }
  return statusMap[status] || '未知'
}

const getStatusColor = (status) => {
  const colorMap = {
    pending: 'warning',
    ongoing: 'primary',
    completed: 'success',
    cancelled: 'info'
  }
  return colorMap[status] || ''
}

const formatDateTime = (dateTime) => {
  if (!dateTime) return '-'
  return dayjs(dateTime).format('YYYY-MM-DD HH:mm')
}

// 事件处理
const refreshData = () => {
  loadConsultationData()
  ElMessage.success('数据已刷新')
}

const handleSearch = () => {
  ElMessage.info('搜索功能暂未实现，使用硬编码数据')
  loadConsultationData()
}

const handleReset = () => {
  Object.keys(searchForm).forEach(key => {
    searchForm[key] = ''
  })
  loadConsultationData()
}

const handleSelectionChange = (selection) => {
  selectedConsultations.value = selection
}

const handleView = (row) => {
  Object.keys(consultationDetail).forEach(key => {
    consultationDetail[key] = row[key]
  })
  dialogVisible.value = true
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除咨询记录"${row.consultationNo}"吗？此操作不可恢复。`,
      '删除确认',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'error'
      }
    )
    
    ElMessage.success('咨询记录删除成功（硬编码演示）')
  } catch {
    // 用户取消
  }
}

const handleDialogClose = () => {
  Object.keys(consultationDetail).forEach(key => {
    consultationDetail[key] = ''
  })
}

const handleSizeChange = (size) => {
  pagination.size = size
  loadConsultationData()
}

const handleCurrentChange = (current) => {
  pagination.current = current
  loadConsultationData()
}
</script>

<style scoped>
.consultation-management {
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

.stat-icon.consultation-total {
  background: linear-gradient(135deg, #667eea, #764ba2);
}

.stat-icon.consultation-today {
  background: linear-gradient(135deg, #4facfe, #00f2fe);
}

.stat-icon.consultation-ongoing {
  background: linear-gradient(135deg, #f093fb, #f5576c);
}

.stat-icon.consultation-completed {
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

.text-muted {
  color: #999;
}

.complaint-card,
.diagnosis-card,
.rating-card {
  margin-bottom: 0;
}

.complaint-card p,
.diagnosis-card p {
  margin: 8px 0;
  line-height: 1.6;
}

.rating-info {
  margin-bottom: 12px;
}

.rating-comment {
  margin: 0;
  line-height: 1.6;
  color: #666;
}

.search-buttons {
  display: flex;
  gap: 10px;
  align-items: center;
}
</style> 