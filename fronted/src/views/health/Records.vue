<template>
  <div class="health-records">
    <el-card>
      <template #header>
        <div class="flex-between">
          <span>健康记录</span>
          <div class="header-actions">
            <el-button 
              v-if="selectedRecords.length > 0" 
              type="danger" 
              :disabled="loading"
              @click="batchDeleteRecords"
            >
              <el-icon><Delete /></el-icon>
              批量删除 ({{ selectedRecords.length }})
            </el-button>
            <el-button type="primary" @click="showAddDialog">
              <el-icon><Plus /></el-icon>
              添加记录
            </el-button>
          </div>
        </div>
      </template>

      <!-- 筛选区域 -->
      <div class="filter-section mb-16">
        <el-row :gutter="16">
          <el-col :xs="24" :sm="12" :md="6">
            <el-select v-model="filters.type" placeholder="记录类型" clearable @change="searchRecords">
              <el-option label="血糖" value="glucose" />
              <el-option label="血压" value="pressure" />
              <el-option label="体重" value="weight" />
            </el-select>
          </el-col>
          <el-col :xs="24" :sm="12" :md="8">
            <el-date-picker
              v-model="filters.dateRange"
              type="datetimerange"
              range-separator="至"
              start-placeholder="开始时间"
              end-placeholder="结束时间"
              format="YYYY-MM-DD HH:mm"
              value-format="YYYY-MM-DD HH:mm:ss"
              @change="searchRecords"
            />
          </el-col>
          <el-col :xs="24" :sm="12" :md="4">
            <el-button @click="searchRecords" :loading="loading">搜索</el-button>
            <el-button @click="resetFilters">重置</el-button>
          </el-col>
        </el-row>
      </div>

      <!-- 记录列表 -->
      <el-table
        :data="records"
        v-loading="loading"
        empty-text="暂无记录"
        style="width: 100%"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55" />
        
        <el-table-column prop="type" label="类型" width="100">
          <template #default="{ row }">
            <el-tag :type="getTypeTagType(row.type)">
              {{ getTypeLabel(row.type) }}
            </el-tag>
          </template>
        </el-table-column>
        
        <el-table-column prop="value" label="数值" min-width="150">
          <template #default="{ row }">
            <div class="record-value">
              <div>{{ formatValue(row) }}</div>
              <div v-if="row.level" class="health-level">
                <el-tag :type="getLevelTagType(row.level)" size="small">
                  {{ getLevelLabel(row.level) }}
                </el-tag>
              </div>
            </div>
          </template>
        </el-table-column>
        
        <el-table-column prop="measureTime" label="记录时间" width="180">
          <template #default="{ row }">
            {{ dayjs(row.measureTime).format('YYYY-MM-DD HH:mm') }}
          </template>
        </el-table-column>
        
        <el-table-column prop="note" label="备注" min-width="120" show-overflow-tooltip />
        
        <el-table-column label="操作" width="140" fixed="right">
          <template #default="{ row }">
            <el-button
              text
              type="primary"
              size="small"
              @click="editRecord(row)"
            >
              编辑
            </el-button>
            <el-button
              text
              type="danger"
              size="small"
              @click="deleteRecord(row)"
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
          :total="pagination.total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <!-- 添加/编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEditing ? '编辑记录' : '添加记录'"
      width="600px"
      @close="resetForm"
    >
      <el-form
        ref="recordFormRef"
        :model="recordForm"
        :rules="recordRules"
        label-width="100px"
      >
        <el-form-item label="记录类型" prop="type">
          <el-select v-model="recordForm.type" placeholder="请选择记录类型" :disabled="isEditing">
            <el-option label="血糖" value="glucose" />
            <el-option label="血压" value="pressure" />
            <el-option label="体重" value="weight" />
          </el-select>
        </el-form-item>

        <!-- 血糖记录表单 -->
        <template v-if="recordForm.type === 'glucose'">
          <el-form-item label="血糖值" prop="value">
            <el-input-number
              v-model="recordForm.value"
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
            <el-select v-model="recordForm.measureType" placeholder="请选择测量类型">
              <el-option label="空腹血糖" value="fasting" />
              <el-option label="餐后血糖" value="after_meal" />
              <el-option label="随机血糖" value="random" />
            </el-select>
          </el-form-item>
          
          <el-form-item label="餐次">
            <el-select v-model="recordForm.mealType" placeholder="请选择餐次（可选）" clearable>
              <el-option label="早餐" value="breakfast" />
              <el-option label="午餐" value="lunch" />
              <el-option label="晚餐" value="dinner" />
            </el-select>
          </el-form-item>
        </template>

        <!-- 血压记录表单 -->
        <template v-if="recordForm.type === 'pressure'">
          <el-row :gutter="16">
            <el-col :span="12">
              <el-form-item label="收缩压" prop="systolic">
                <el-input-number
                  v-model="recordForm.systolic"
                  :min="50"
                  :max="300"
                  placeholder="收缩压"
                  style="width: 100%"
                />
                <span class="input-suffix">mmHg</span>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="舒张压" prop="diastolic">
                <el-input-number
                  v-model="recordForm.diastolic"
                  :min="30"
                  :max="200"
                  placeholder="舒张压"
                  style="width: 100%"
                />
                <span class="input-suffix">mmHg</span>
              </el-form-item>
            </el-col>
          </el-row>
          
          <el-form-item label="心率">
            <el-input-number
              v-model="recordForm.heartRate"
              :min="30"
              :max="200"
              placeholder="心率（可选）"
              style="width: 100%"
            />
            <span class="input-suffix">bpm</span>
          </el-form-item>
          
          <el-form-item label="测量状态">
            <el-select v-model="recordForm.measureState" placeholder="请选择测量状态（可选）" clearable>
              <el-option label="休息时" value="rest" />
              <el-option label="活动后" value="activity" />
              <el-option label="晨起" value="morning" />
              <el-option label="晚间" value="evening" />
            </el-select>
          </el-form-item>
        </template>

        <!-- 体重记录表单 -->
        <template v-if="recordForm.type === 'weight'">
          <el-form-item label="体重" prop="weight">
            <el-input-number
              v-model="recordForm.weight"
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
              v-model="recordForm.height"
              :min="50"
              :max="250"
              :step="0.1"
              :precision="1"
              placeholder="身高（可选，用于计算BMI）"
              style="width: 100%"
            />
            <span class="input-suffix">cm</span>
          </el-form-item>
          
          <el-form-item label="体脂率">
            <el-input-number
              v-model="recordForm.bodyFatRate"
              :min="0"
              :max="100"
              :step="0.1"
              :precision="1"
              placeholder="体脂率（可选）"
              style="width: 100%"
            />
            <span class="input-suffix">%</span>
          </el-form-item>
        </template>

        <el-form-item label="测量时间" prop="measureTime">
          <el-date-picker
            v-model="recordForm.measureTime"
            type="datetime"
            placeholder="请选择测量时间"
            format="YYYY-MM-DD HH:mm"
            value-format="YYYY-MM-DD HH:mm:ss"
            style="width: 100%"
          />
        </el-form-item>

        <el-form-item label="备注">
          <el-input
            v-model="recordForm.note"
            type="textarea"
            :rows="3"
            placeholder="请输入备注（可选）"
            maxlength="200"
            show-word-limit
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveRecord" :loading="saving">
          确定
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Delete } from '@element-plus/icons-vue'
import dayjs from 'dayjs'
import * as healthApi from '@/api/health'

// 响应式数据
const loading = ref(false)
const saving = ref(false)
const dialogVisible = ref(false)
const isEditing = ref(false)
const recordFormRef = ref()
const selectedRecords = ref([])

const records = ref([])

const filters = reactive({
  type: '',
  dateRange: []
})

const pagination = reactive({
  current: 1,
  size: 20,
  total: 0
})

const recordForm = reactive({
  id: null,
  type: '',
  // 血糖字段
  value: null,
  measureType: '',
  mealType: '',
  // 血压字段
  systolic: null,
  diastolic: null,
  heartRate: null,
  measureState: '',
  // 体重字段
  weight: null,
  height: null,
  bodyFatRate: null,
  // 公共字段
  measureTime: new Date(),
  note: ''
})

// 表单验证规则
const recordRules = computed(() => {
  const baseRules = {
    type: [
      { required: true, message: '请选择记录类型', trigger: 'change' }
    ],
    measureTime: [
      { required: true, message: '请选择测量时间', trigger: 'change' }
    ]
  }

  if (recordForm.type === 'glucose') {
    baseRules.value = [
      { required: true, message: '请输入血糖值', trigger: 'blur' }
    ]
    baseRules.measureType = [
      { required: true, message: '请选择测量类型', trigger: 'change' }
    ]
  } else if (recordForm.type === 'pressure') {
    baseRules.systolic = [
      { required: true, message: '请输入收缩压', trigger: 'blur' }
    ]
    baseRules.diastolic = [
      { required: true, message: '请输入舒张压', trigger: 'blur' }
    ]
  } else if (recordForm.type === 'weight') {
    baseRules.weight = [
      { required: true, message: '请输入体重', trigger: 'blur' }
    ]
  }

  return baseRules
})

// 获取类型标签样式
const getTypeTagType = (type) => {
  const typeMap = {
    glucose: 'primary',
    pressure: 'success',
    weight: 'info'
  }
  return typeMap[type] || ''
}

// 获取类型标签文本
const getTypeLabel = (type) => {
  const typeMap = {
    glucose: '血糖',
    pressure: '血压',
    weight: '体重'
  }
  return typeMap[type] || type
}

// 获取健康水平标签样式
const getLevelTagType = (level) => {
  const levelMap = {
    normal: 'success',
    high: 'danger',
    low: 'warning'
  }
  return levelMap[level] || ''
}

// 获取健康水平标签文本
const getLevelLabel = (level) => {
  const levelMap = {
    normal: '正常',
    high: '偏高',
    low: '偏低'
  }
  return levelMap[level] || level
}

// 格式化数值显示
const formatValue = (row) => {
  const { type } = row
  
  if (type === 'glucose') {
    return `${row.value} mmol/L`
  } else if (type === 'pressure') {
    let result = `${row.systolic}/${row.diastolic} mmHg`
    if (row.heartRate) {
      result += ` | 心率 ${row.heartRate} bpm`
    }
    return result
  } else if (type === 'weight') {
    let result = `${row.weight} kg`
    if (row.bmi) {
      result += ` | BMI ${row.bmi}`
    }
    return result
  }
  
  return row.value || '--'
}

// 处理选择变化
const handleSelectionChange = (selection) => {
  selectedRecords.value = selection
}

// 批量删除记录
const batchDeleteRecords = async () => {
  try {
    await ElMessageBox.confirm(
      `确定要删除选中的 ${selectedRecords.value.length} 条记录吗？`,
      '批量删除',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    // 按类型分组删除
    const glucoseRecords = selectedRecords.value.filter(r => r.type === 'glucose')
    const pressureRecords = selectedRecords.value.filter(r => r.type === 'pressure')
    const weightRecords = selectedRecords.value.filter(r => r.type === 'weight')

    const promises = []
    
    if (glucoseRecords.length > 0) {
      const recordIds = glucoseRecords.map(r => r.id)
      promises.push(healthApi.batchDeleteBloodGlucoseRecords(recordIds))
    }
    
    if (pressureRecords.length > 0) {
      const recordIds = pressureRecords.map(r => r.id)
      promises.push(healthApi.batchDeleteBloodPressureRecords(recordIds))
    }
    
    if (weightRecords.length > 0) {
      const recordIds = weightRecords.map(r => r.id)
      promises.push(healthApi.batchDeleteWeightRecords(recordIds))
    }

    await Promise.all(promises)
    
    ElMessage.success('批量删除成功')
    selectedRecords.value = []
    await loadRecords()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('批量删除失败:', error)
      ElMessage.error('批量删除失败')
    }
  }
}

// 显示添加对话框
const showAddDialog = () => {
  isEditing.value = false
  dialogVisible.value = true
  resetForm()
}

// 编辑记录
const editRecord = (row) => {
  isEditing.value = true
  dialogVisible.value = true
  
  // 根据记录类型设置表单数据
  Object.assign(recordForm, {
    id: row.id,
    type: row.type,
    measureTime: row.measureTime,
    note: row.note || ''
  })
  
  if (row.type === 'glucose') {
    recordForm.value = row.value
    recordForm.measureType = row.measureType
    recordForm.mealType = row.mealType
  } else if (row.type === 'pressure') {
    recordForm.systolic = row.systolic
    recordForm.diastolic = row.diastolic
    recordForm.heartRate = row.heartRate
    recordForm.measureState = row.measureState
  } else if (row.type === 'weight') {
    recordForm.weight = row.weight
    recordForm.height = row.height
    recordForm.bodyFatRate = row.bodyFatRate
  }
}

// 删除记录
const deleteRecord = async (row) => {
  try {
    await ElMessageBox.confirm(
      '确定要删除这条记录吗？',
      '提示',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    // 根据类型调用对应的删除API
    if (row.type === 'glucose') {
      await healthApi.deleteBloodGlucoseRecord(row.id)
    } else if (row.type === 'pressure') {
      await healthApi.deleteBloodPressureRecord(row.id)
    } else if (row.type === 'weight') {
      await healthApi.deleteWeightRecord(row.id)
    }

    ElMessage.success('删除成功')
    await loadRecords()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除失败:', error)
      ElMessage.error('删除失败')
    }
  }
}

// 保存记录
const saveRecord = async () => {
  if (saving.value) return

  try {
    const valid = await recordFormRef.value.validate()
    if (!valid) return

    saving.value = true

    // 构建请求数据
    const data = {
      measureTime: recordForm.measureTime,
      note: recordForm.note
    }

    // 根据记录类型添加特定字段
    if (recordForm.type === 'glucose') {
      data.value = recordForm.value
      data.measureType = recordForm.measureType
      data.mealType = recordForm.mealType
    } else if (recordForm.type === 'pressure') {
      data.systolic = recordForm.systolic
      data.diastolic = recordForm.diastolic
      data.heartRate = recordForm.heartRate
      data.measureState = recordForm.measureState
    } else if (recordForm.type === 'weight') {
      data.weight = recordForm.weight
      data.height = recordForm.height
      data.bodyFatRate = recordForm.bodyFatRate
    }

    // 调用API
    if (isEditing.value) {
      // 更新记录
      if (recordForm.type === 'glucose') {
        await healthApi.updateBloodGlucoseRecord(recordForm.id, data)
      } else if (recordForm.type === 'pressure') {
        await healthApi.updateBloodPressureRecord(recordForm.id, data)
      } else if (recordForm.type === 'weight') {
        await healthApi.updateWeightRecord(recordForm.id, data)
      }
    } else {
      // 添加记录
      if (recordForm.type === 'glucose') {
        await healthApi.addBloodGlucoseRecord(data)
      } else if (recordForm.type === 'pressure') {
        await healthApi.addBloodPressureRecord(data)
      } else if (recordForm.type === 'weight') {
        await healthApi.addWeightRecord(data)
      }
    }

    ElMessage.success(isEditing.value ? '记录已更新' : '记录已添加')
    dialogVisible.value = false
    await loadRecords()
  } catch (error) {
    console.error('保存失败:', error)
    ElMessage.error('保存失败')
  } finally {
    saving.value = false
  }
}

// 重置表单
const resetForm = () => {
  if (recordFormRef.value) {
    recordFormRef.value.clearValidate()
  }
  Object.assign(recordForm, {
    id: null,
    type: '',
    value: null,
    measureType: '',
    mealType: '',
    systolic: null,
    diastolic: null,
    heartRate: null,
    measureState: '',
    weight: null,
    height: null,
    bodyFatRate: null,
    measureTime: new Date(),
    note: ''
  })
}

// 搜索记录
const searchRecords = () => {
  pagination.current = 1
  loadRecords()
}

// 重置筛选
const resetFilters = () => {
  Object.assign(filters, {
    type: '',
    dateRange: []
  })
  searchRecords()
}

// 分页大小变化
const handleSizeChange = (size) => {
  pagination.size = size
  loadRecords()
}

// 当前页变化
const handleCurrentChange = (current) => {
  pagination.current = current
  loadRecords()
}

// 加载记录数据
const loadRecords = async () => {
  try {
    loading.value = true

    const params = {
      current: pagination.current,
      size: pagination.size,
      sortField: 'measureTime',
      sortOrder: 'desc'
    }

    // 添加时间筛选
    if (filters.dateRange && filters.dateRange.length === 2) {
      params.startTime = filters.dateRange[0]
      params.endTime = filters.dateRange[1]
    }

    // 根据类型筛选调用不同API
    if (filters.type) {
      params.recordType = filters.type
    }

    const result = await healthApi.getAllHealthRecords(params)
    
    records.value = result.data || []
    pagination.total = result.total || result.data?.length || 0

  } catch (error) {
    console.error('加载数据失败:', error)
    ElMessage.error('加载数据失败')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadRecords()
})
</script>

<style scoped>
.health-records {
  padding: 0;
}

.flex-between {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
}

.header-actions {
  display: flex;
  gap: 8px;
}

.filter-section {
  background: #f8f9fa;
  padding: 16px;
  border-radius: 6px;
  border: 1px solid #e4e7ed;
}

.record-value {
  font-weight: 600;
  color: #409EFF;
}

.health-level {
  margin-top: 4px;
}

.input-suffix {
  margin-left: 8px;
  color: #909399;
  font-size: 14px;
}

.pagination-container {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}

.mb-16 {
  margin-bottom: 16px;
}

@media (max-width: 768px) {
  .filter-section {
    padding: 12px;
  }
  
  :deep(.el-table) {
    font-size: 12px;
  }
  
  .header-actions {
    flex-direction: column;
    align-items: flex-end;
  }
}</style> 