<template>
  <div class="health-overview">
    <!-- 概览统计卡片 -->
    <el-row :gutter="20" class="overview-stats">
      <el-col :xs="24" :sm="12" :md="6">
        <div class="stat-card glucose-card" @click="$router.push('/health/glucose')">
          <div class="stat-icon">
            <el-icon><Odometer /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-title">血糖记录</div>
            <div class="stat-value">{{ overview.glucoseCount || 0 }}</div>
            <div class="stat-sub">平均值: {{ overview.avgGlucose || '--' }} mmol/L</div>
          </div>
        </div>
      </el-col>
      
      <el-col :xs="24" :sm="12" :md="6">
        <div class="stat-card pressure-card" @click="$router.push('/health/pressure')">
          <div class="stat-icon">
                          <el-icon><Monitor /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-title">血压记录</div>
            <div class="stat-value">{{ overview.pressureCount || 0 }}</div>
            <div class="stat-sub">平均值: {{ formatPressure(overview.avgSystolic, overview.avgDiastolic) }}</div>
          </div>
        </div>
      </el-col>
      
      <el-col :xs="24" :sm="12" :md="6">
        <div class="stat-card weight-card" @click="$router.push('/health/weight')">
          <div class="stat-icon">
            <el-icon><Grid /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-title">体重记录</div>
            <div class="stat-value">{{ overview.weightCount || 0 }}</div>
            <div class="stat-sub">当前BMI: {{ overview.currentBmi || '--' }}</div>
          </div>
        </div>
      </el-col>
      
      <el-col :xs="24" :sm="12" :md="6">
        <div class="stat-card total-card" @click="$router.push('/health/statistics')">
          <div class="stat-icon">
            <el-icon><TrendCharts /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-title">总记录数</div>
            <div class="stat-value">{{ overview.totalRecords || 0 }}</div>
            <div class="stat-sub">查看统计分析</div>
          </div>
        </div>
      </el-col>
    </el-row>

    <!-- 快速操作 -->
    <el-card class="quick-actions-card" header="快速操作">
      <el-row :gutter="16">
        <el-col :xs="12" :sm="8" :md="6">
          <el-button 
            type="primary" 
            class="action-btn"
            @click="showGlucoseDialog = true"
          >
            <el-icon><Plus /></el-icon>
            <span>记录血糖</span>
          </el-button>
        </el-col>
        <el-col :xs="12" :sm="8" :md="6">
          <el-button 
            type="success" 
            class="action-btn"
            @click="showPressureDialog = true"
          >
            <el-icon><Plus /></el-icon>
            <span>记录血压</span>
          </el-button>
        </el-col>
        <el-col :xs="12" :sm="8" :md="6">
          <el-button 
            type="info" 
            class="action-btn"
            @click="showWeightDialog = true"
          >
            <el-icon><Plus /></el-icon>
            <span>记录体重</span>
          </el-button>
        </el-col>
        <el-col :xs="12" :sm="8" :md="6">
          <el-button 
            type="warning" 
            class="action-btn"
            @click="loadOverviewData"
          >
            <el-icon><Refresh /></el-icon>
            <span>刷新数据</span>
          </el-button>
        </el-col>
      </el-row>
    </el-card>

    <!-- 最近记录 -->
    <el-row :gutter="20" class="recent-records-section">
      <el-col :xs="24" :md="12">
        <el-card header="最近血糖记录">
          <div v-loading="loadingGlucose">
            <div v-if="recentGlucose.length === 0" class="empty-state">
              <el-icon class="empty-icon"><Document /></el-icon>
              <p>暂无血糖记录</p>
              <el-button type="primary" size="small" @click="$router.push('/health/glucose')">
                添加记录
              </el-button>
            </div>
            <div v-else class="record-list">
              <div 
                v-for="record in recentGlucose" 
                :key="record.measureTime"
                class="record-item"
              >
                <div class="record-value glucose-value">
                  {{ record.value }} mmol/L
                </div>
                <div class="record-meta">
                  <span class="record-type">{{ getMeasureTypeLabel(record.measureType) }}</span>
                  <span class="record-time">{{ formatTime(record.measureTime) }}</span>
                </div>
                <div class="record-level">
                  <el-tag :type="getLevelTagType(record.level)" size="small">
                    {{ getLevelLabel(record.level) }}
                  </el-tag>
                </div>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :xs="24" :md="12">
        <el-card header="最近血压记录">
          <div v-loading="loadingPressure">
            <div v-if="recentPressure.length === 0" class="empty-state">
              <el-icon class="empty-icon"><Monitor /></el-icon>
              <p>暂无血压记录</p>
              <el-button type="success" size="small" @click="$router.push('/health/pressure')">
                添加记录
              </el-button>
            </div>
            <div v-else class="record-list">
              <div 
                v-for="record in recentPressure" 
                :key="record.measureTime"
                class="record-item"
              >
                <div class="record-value pressure-value">
                  {{ record.systolic }}/{{ record.diastolic }} mmHg
                </div>
                <div class="record-meta">
                  <span class="record-type">{{ getMeasureStateLabel(record.measureState) }}</span>
                  <span class="record-time">{{ formatTime(record.measureTime) }}</span>
                </div>
                <div class="record-level">
                  <el-tag :type="getLevelTagType(record.level)" size="small">
                    {{ getLevelLabel(record.level) }}
                  </el-tag>
                </div>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 快速添加对话框 -->
    <!-- 血糖记录对话框 -->
    <el-dialog v-model="showGlucoseDialog" title="快速记录血糖" width="400px">
      <el-form ref="glucoseFormRef" :model="glucoseForm" :rules="glucoseRules" label-width="80px">
        <el-form-item label="血糖值" prop="value">
          <el-input-number
            v-model="glucoseForm.value"
            :min="0.1"
            :max="50"
            :step="0.1"
            :precision="1"
            placeholder="请输入血糖值"
            style="width: 100%"
          />
          <span class="input-suffix">mmol/L</span>
        </el-form-item>
        <el-form-item label="测量类型" prop="measureType">
          <el-select v-model="glucoseForm.measureType" placeholder="请选择测量类型" style="width: 100%">
            <el-option label="空腹" value="fasting" />
            <el-option label="餐后" value="after_meal" />
            <el-option label="随机" value="random" />
          </el-select>
        </el-form-item>
        <el-form-item label="测量时间" prop="measureTime">
          <el-date-picker
            v-model="glucoseForm.measureTime"
            type="datetime"
            placeholder="请选择测量时间"
            format="YYYY-MM-DD HH:mm"
            value-format="YYYY-MM-DD HH:mm:ss"
            style="width: 100%"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showGlucoseDialog = false">取消</el-button>
        <el-button type="primary" @click="saveGlucoseRecord" :loading="saving">确定</el-button>
      </template>
    </el-dialog>

    <!-- 血压记录对话框 -->
    <el-dialog v-model="showPressureDialog" title="快速记录血压" width="400px">
      <el-form ref="pressureFormRef" :model="pressureForm" :rules="pressureRules" label-width="80px">
        <el-form-item label="收缩压" prop="systolic">
          <el-input-number
            v-model="pressureForm.systolic"
            :min="50"
            :max="300"
            placeholder="收缩压"
            style="width: 100%"
          />
          <span class="input-suffix">mmHg</span>
        </el-form-item>
        <el-form-item label="舒张压" prop="diastolic">
          <el-input-number
            v-model="pressureForm.diastolic"
            :min="30"
            :max="200"
            placeholder="舒张压"
            style="width: 100%"
          />
          <span class="input-suffix">mmHg</span>
        </el-form-item>
        <el-form-item label="心率">
          <el-input-number
            v-model="pressureForm.heartRate"
            :min="30"
            :max="250"
            placeholder="心率（可选）"
            style="width: 100%"
          />
          <span class="input-suffix">bpm</span>
        </el-form-item>
        <el-form-item label="测量时间" prop="measureTime">
          <el-date-picker
            v-model="pressureForm.measureTime"
            type="datetime"
            placeholder="请选择测量时间"
            format="YYYY-MM-DD HH:mm"
            value-format="YYYY-MM-DD HH:mm:ss"
            style="width: 100%"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showPressureDialog = false">取消</el-button>
        <el-button type="primary" @click="savePressureRecord" :loading="saving">确定</el-button>
      </template>
    </el-dialog>

    <!-- 体重记录对话框 -->
    <el-dialog v-model="showWeightDialog" title="快速记录体重" width="400px">
      <el-form ref="weightFormRef" :model="weightForm" :rules="weightRules" label-width="80px">
        <el-form-item label="体重" prop="weight">
          <el-input-number
            v-model="weightForm.weight"
            :min="1"
            :max="500"
            :step="0.1"
            :precision="1"
            placeholder="请输入体重"
            style="width: 100%"
          />
          <span class="input-suffix">kg</span>
        </el-form-item>
        <el-form-item label="身高">
          <el-input-number
            v-model="weightForm.height"
            :min="50"
            :max="250"
            :step="0.1"
            :precision="1"
            placeholder="身高（可选）"
            style="width: 100%"
          />
          <span class="input-suffix">cm</span>
        </el-form-item>
        <el-form-item label="测量时间" prop="measureTime">
          <el-date-picker
            v-model="weightForm.measureTime"
            type="datetime"
            placeholder="请选择测量时间"
            format="YYYY-MM-DD HH:mm"
            value-format="YYYY-MM-DD HH:mm:ss"
            style="width: 100%"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showWeightDialog = false">取消</el-button>
        <el-button type="primary" @click="saveWeightRecord" :loading="saving">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Plus, Refresh, Odometer, Monitor, Grid, TrendCharts, Document } from '@element-plus/icons-vue'
import dayjs from 'dayjs'
import * as healthApi from '@/api/health'

// 响应式数据
const loading = ref(false)
const loadingGlucose = ref(false)
const loadingPressure = ref(false)
const saving = ref(false)

const showGlucoseDialog = ref(false)
const showPressureDialog = ref(false)
const showWeightDialog = ref(false)

const glucoseFormRef = ref()
const pressureFormRef = ref()
const weightFormRef = ref()

const overview = ref({})
const recentGlucose = ref([])
const recentPressure = ref([])

// 表单数据
const glucoseForm = reactive({
  value: null,
  measureType: '',
  measureTime: new Date()
})

const pressureForm = reactive({
  systolic: null,
  diastolic: null,
  heartRate: null,
  measureTime: new Date()
})

const weightForm = reactive({
  weight: null,
  height: null,
  measureTime: new Date()
})

// 表单验证规则
const glucoseRules = {
  value: [
    { required: true, message: '请输入血糖值', trigger: 'blur' },
    { type: 'number', min: 0.1, max: 50, message: '血糖值应在0.1-50之间', trigger: 'blur' }
  ],
  measureType: [
    { required: true, message: '请选择测量类型', trigger: 'change' }
  ],
  measureTime: [
    { required: true, message: '请选择测量时间', trigger: 'change' }
  ]
}

const pressureRules = {
  systolic: [
    { required: true, message: '请输入收缩压', trigger: 'blur' },
    { type: 'number', min: 50, max: 300, message: '收缩压应在50-300之间', trigger: 'blur' }
  ],
  diastolic: [
    { required: true, message: '请输入舒张压', trigger: 'blur' },
    { type: 'number', min: 30, max: 200, message: '舒张压应在30-200之间', trigger: 'blur' }
  ],
  measureTime: [
    { required: true, message: '请选择测量时间', trigger: 'change' }
  ]
}

const weightRules = {
  weight: [
    { required: true, message: '请输入体重', trigger: 'blur' },
    { type: 'number', min: 1, max: 500, message: '体重应在1-500之间', trigger: 'blur' }
  ],
  measureTime: [
    { required: true, message: '请选择测量时间', trigger: 'change' }
  ]
}

// 格式化血压显示
const formatPressure = (systolic, diastolic) => {
  if (systolic && diastolic) {
    return `${systolic}/${diastolic} mmHg`
  }
  return '--'
}

// 格式化时间
const formatTime = (time) => {
  return dayjs(time).format('MM-DD HH:mm')
}

// 获取测量类型标签
const getMeasureTypeLabel = (type) => {
  const typeMap = {
    fasting: '空腹',
    after_meal: '餐后',
    random: '随机'
  }
  return typeMap[type] || type
}

// 获取测量状态标签
const getMeasureStateLabel = (state) => {
  const stateMap = {
    rest: '休息',
    activity: '活动后',
    morning: '晨起',
    evening: '晚间'
  }
  return stateMap[state] || state
}

// 获取水平标签样式
const getLevelTagType = (level) => {
  const typeMap = {
    normal: 'success',
    high: 'danger',
    low: 'warning'
  }
  return typeMap[level] || 'info'
}

// 获取水平标签文本
const getLevelLabel = (level) => {
  const labelMap = {
    normal: '正常',
    high: '偏高',
    low: '偏低'
  }
  return labelMap[level] || level
}

// 保存血糖记录
const saveGlucoseRecord = async () => {
  if (saving.value) return

  try {
    const valid = await glucoseFormRef.value.validate()
    if (!valid) return

    saving.value = true
    await healthApi.addBloodGlucoseRecord(glucoseForm)
    
    ElMessage.success('血糖记录添加成功')
    showGlucoseDialog.value = false
    
    // 重置表单
    Object.assign(glucoseForm, {
      value: null,
      measureType: '',
      measureTime: new Date()
    })
    
    // 刷新数据
    await loadOverviewData()
    await loadRecentRecords()
    
  } catch (error) {
    console.error('保存血糖记录失败:', error)
    ElMessage.error('保存失败')
  } finally {
    saving.value = false
  }
}

// 保存血压记录
const savePressureRecord = async () => {
  if (saving.value) return

  try {
    const valid = await pressureFormRef.value.validate()
    if (!valid) return

    saving.value = true
    await healthApi.addBloodPressureRecord(pressureForm)
    
    ElMessage.success('血压记录添加成功')
    showPressureDialog.value = false
    
    // 重置表单
    Object.assign(pressureForm, {
      systolic: null,
      diastolic: null,
      heartRate: null,
      measureTime: new Date()
    })
    
    // 刷新数据
    await loadOverviewData()
    await loadRecentRecords()
    
  } catch (error) {
    console.error('保存血压记录失败:', error)
    ElMessage.error('保存失败')
  } finally {
    saving.value = false
  }
}

// 保存体重记录
const saveWeightRecord = async () => {
  if (saving.value) return

  try {
    const valid = await weightFormRef.value.validate()
    if (!valid) return

    saving.value = true
    await healthApi.addWeightRecord(weightForm)
    
    ElMessage.success('体重记录添加成功')
    showWeightDialog.value = false
    
    // 重置表单
    Object.assign(weightForm, {
      weight: null,
      height: null,
      measureTime: new Date()
    })
    
    // 刷新数据
    await loadOverviewData()
    
  } catch (error) {
    console.error('保存体重记录失败:', error)
    ElMessage.error('保存失败')
  } finally {
    saving.value = false
  }
}

// 加载概览数据
const loadOverviewData = async () => {
  try {
    loading.value = true
    
    // 获取健康统计数据
    const statisticsRes = await healthApi.getHealthStatistics()
    const statisticsData = statisticsRes.data || {}
    
    // 血糖数据
    const glucoseData = statisticsData.glucose || {}
    
    // 血压数据
    const pressureData = statisticsData.pressure || {}
    
    // 体重数据
    const weightData = statisticsData.weight || {}
    
    overview.value = {
      glucoseCount: glucoseData.totalCount || 0,
      avgGlucose: glucoseData.avgValue || 0,
      pressureCount: pressureData.totalCount || 0,
      avgSystolic: pressureData.avgSystolic || 0,
      avgDiastolic: pressureData.avgDiastolic || 0,
      weightCount: weightData.totalCount || 0,
      currentBmi: weightData.currentBmi || 0,
      totalRecords: statisticsData.totalRecords || 0
    }
    
  } catch (error) {
    console.error('加载概览数据失败:', error)
    ElMessage.error('加载数据失败')
    // 设置默认值
    overview.value = {
      glucoseCount: 0,
      avgGlucose: 0,
      pressureCount: 0,
      avgSystolic: 0,
      avgDiastolic: 0,
      weightCount: 0,
      currentBmi: 0,
      totalRecords: 0
    }
  } finally {
    loading.value = false
  }
}

// 加载最近记录
const loadRecentRecords = async () => {
  try {
    loadingGlucose.value = true
    loadingPressure.value = true
    
    // 获取最近血糖记录
    try {
      const glucoseRes = await healthApi.getBloodGlucoseRecords({ current: 1, size: 5 })
      if (glucoseRes.data && glucoseRes.data.records) {
        // 🔥 修复：按时间倒序排列，最新的记录在最上面
        recentGlucose.value = glucoseRes.data.records.sort((a, b) => {
          return new Date(b.measureTime) - new Date(a.measureTime)
        })
      } else {
        recentGlucose.value = []
      }
    } catch (error) {
      console.warn('获取血糖记录失败:', error)
      recentGlucose.value = []
    }
    
    // 获取最近血压记录
    try {
      const pressureRes = await healthApi.getBloodPressureRecords({ current: 1, size: 5 })
      if (pressureRes.data && pressureRes.data.records) {
        // 🔥 修复：按时间倒序排列，最新的记录在最上面
        recentPressure.value = pressureRes.data.records.sort((a, b) => {
          return new Date(b.measureTime) - new Date(a.measureTime)
        })
      } else {
        recentPressure.value = []
      }
    } catch (error) {
      console.warn('获取血压记录失败:', error)
      recentPressure.value = []
    }
    
  } catch (error) {
    console.error('加载最近记录失败:', error)
  } finally {
    loadingGlucose.value = false
    loadingPressure.value = false
  }
}

onMounted(() => {
  loadOverviewData()
  loadRecentRecords()
})
</script>

<style scoped>
.health-overview {
  max-width: 1200px;
  margin: 0 auto;
}

.overview-stats {
  margin-bottom: 24px;
}

.stat-card {
  background: white;
  border-radius: 12px;
  padding: 20px;
  display: flex;
  align-items: center;
  cursor: pointer;
  transition: all 0.3s ease;
  border: 1px solid #e4e7ed;
  height: 120px;
}

.stat-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.12);
}

.glucose-card:hover {
  border-color: #409EFF;
}

.pressure-card:hover {
  border-color: #67C23A;
}

.weight-card:hover {
  border-color: #909399;
}

.total-card:hover {
  border-color: #E6A23C;
}

.stat-icon {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  color: white;
  margin-right: 20px;
}

.glucose-card .stat-icon {
  background: linear-gradient(135deg, #409EFF, #66B2FF);
}

.pressure-card .stat-icon {
  background: linear-gradient(135deg, #67C23A, #85CE61);
}

.weight-card .stat-icon {
  background: linear-gradient(135deg, #909399, #B1B3B8);
}

.total-card .stat-icon {
  background: linear-gradient(135deg, #E6A23C, #EBB563);
}

.stat-content {
  flex: 1;
}

.stat-title {
  font-size: 14px;
  color: #909399;
  margin-bottom: 8px;
}

.stat-value {
  font-size: 28px;
  font-weight: bold;
  color: #303133;
  margin-bottom: 4px;
}

.stat-sub {
  font-size: 12px;
  color: #606266;
}

.quick-actions-card {
  margin-bottom: 24px;
}

.action-btn {
  width: 100%;
  height: 60px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 4px;
}

.recent-records-section {
  margin-bottom: 24px;
}

.empty-state {
  text-align: center;
  padding: 40px 20px;
  color: #909399;
}

.empty-icon {
  font-size: 48px;
  margin-bottom: 16px;
  color: #C0C4CC;
}

.record-list {
  max-height: 300px;
  overflow-y: auto;
}

.record-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 0;
  border-bottom: 1px solid #f0f0f0;
}

.record-item:last-child {
  border-bottom: none;
}

.record-value {
  font-size: 18px;
  font-weight: bold;
  color: #303133;
}

.glucose-value {
  color: #409EFF;
}

.pressure-value {
  color: #67C23A;
}

.record-meta {
  flex: 1;
  margin: 0 16px;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.record-type {
  font-size: 14px;
  color: #303133;
  font-weight: 500;
}

.record-time {
  font-size: 12px;
  color: #909399;
}

.record-level {
  text-align: right;
}

.input-suffix {
  margin-left: 8px;
  color: #909399;
  font-size: 14px;
}

@media (max-width: 768px) {
  .overview-stats :deep(.el-col) {
    margin-bottom: 16px;
  }
  
  .recent-records-section :deep(.el-col) {
    margin-bottom: 16px;
  }
  
  .stat-card {
    height: auto;
    min-height: 100px;
    padding: 16px;
  }
  
  .stat-icon {
    width: 48px;
    height: 48px;
    font-size: 20px;
    margin-right: 16px;
  }
  
  .stat-value {
    font-size: 24px;
  }
  
  .record-item {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }
  
  .record-meta {
    margin: 0;
    width: 100%;
  }
  
  .record-level {
    text-align: left;
  }
}
</style> 