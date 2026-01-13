<template>
  <div class="health-overview">
    <!-- 概览统计卡片 -->
    <div class="stats-grid">
      <div class="stat-card glucose" @click="$router.push('/health/glucose')">
        <div class="stat-icon">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="28" height="28"><path d="M19.428 15.428a2 2 0 00-1.022-.547l-2.387-.477a6 6 0 00-3.86.517l-.318.158a6 6 0 01-3.86.517L6.05 15.21a2 2 0 00-1.806.547M8 4h8l-1 1v5.172a2 2 0 00.586 1.414l5 5c1.26 1.26.367 3.414-1.415 3.414H4.828c-1.782 0-2.674-2.154-1.414-3.414l5-5A2 2 0 009 10.172V5L8 4z"/></svg>
        </div>
        <div class="stat-content">
          <span class="stat-title">血糖记录</span>
          <span class="stat-value">{{ overview.glucoseCount || 0 }}</span>
          <span class="stat-sub">平均值: {{ overview.avgGlucose || '--' }} mmol/L</span>
        </div>
        <svg class="stat-arrow" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="20" height="20"><path d="M9 5l7 7-7 7"/></svg>
      </div>
      
      <div class="stat-card pressure" @click="$router.push('/health/pressure')">
        <div class="stat-icon">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="28" height="28"><path d="M4.318 6.318a4.5 4.5 0 000 6.364L12 20.364l7.682-7.682a4.5 4.5 0 00-6.364-6.364L12 7.636l-1.318-1.318a4.5 4.5 0 00-6.364 0z"/></svg>
        </div>
        <div class="stat-content">
          <span class="stat-title">血压记录</span>
          <span class="stat-value">{{ overview.pressureCount || 0 }}</span>
          <span class="stat-sub">平均值: {{ formatPressure(overview.avgSystolic, overview.avgDiastolic) }}</span>
        </div>
        <svg class="stat-arrow" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="20" height="20"><path d="M9 5l7 7-7 7"/></svg>
      </div>
      
      <div class="stat-card weight" @click="$router.push('/health/weight')">
        <div class="stat-icon">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="28" height="28"><path d="M3 6l3 1m0 0l-3 9a5.002 5.002 0 006.001 0M6 7l3 9M6 7l6-2m6 2l3-1m-3 1l-3 9a5.002 5.002 0 006.001 0M18 7l3 9m-3-9l-6-2m0-2v2m0 16V5m0 16H9m3 0h3"/></svg>
        </div>
        <div class="stat-content">
          <span class="stat-title">体重记录</span>
          <span class="stat-value">{{ overview.weightCount || 0 }}</span>
          <span class="stat-sub">当前BMI: {{ overview.currentBmi || '--' }}</span>
        </div>
        <svg class="stat-arrow" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="20" height="20"><path d="M9 5l7 7-7 7"/></svg>
      </div>
      
      <div class="stat-card total" @click="$router.push('/health/statistics')">
        <div class="stat-icon">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="28" height="28"><path d="M9 19v-6a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2a2 2 0 002-2zm0 0V9a2 2 0 012-2h2a2 2 0 012 2v10m-6 0a2 2 0 002 2h2a2 2 0 002-2m0 0V5a2 2 0 012-2h2a2 2 0 012 2v14a2 2 0 01-2 2h-2a2 2 0 01-2-2z"/></svg>
        </div>
        <div class="stat-content">
          <span class="stat-title">总记录数</span>
          <span class="stat-value">{{ overview.totalRecords || 0 }}</span>
          <span class="stat-sub">查看统计分析</span>
        </div>
        <svg class="stat-arrow" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="20" height="20"><path d="M9 5l7 7-7 7"/></svg>
      </div>
    </div>

    <!-- 快速操作 -->
    <div class="section-card">
      <div class="section-header">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="20" height="20"><path d="M13 10V3L4 14h7v7l9-11h-7z"/></svg>
        <h3>快速操作</h3>
      </div>
      <div class="quick-actions">
        <button class="action-btn glucose" @click="showGlucoseDialog = true">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="24" height="24"><path d="M12 9v3m0 0v3m0-3h3m-3 0H9m12 0a9 9 0 11-18 0 9 9 0 0118 0z"/></svg>
          <span>记录血糖</span>
        </button>
        <button class="action-btn pressure" @click="showPressureDialog = true">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="24" height="24"><path d="M12 9v3m0 0v3m0-3h3m-3 0H9m12 0a9 9 0 11-18 0 9 9 0 0118 0z"/></svg>
          <span>记录血压</span>
        </button>
        <button class="action-btn weight" @click="showWeightDialog = true">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="24" height="24"><path d="M12 9v3m0 0v3m0-3h3m-3 0H9m12 0a9 9 0 11-18 0 9 9 0 0118 0z"/></svg>
          <span>记录体重</span>
        </button>
        <button class="action-btn refresh" @click="loadOverviewData">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="24" height="24"><path d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15"/></svg>
          <span>刷新数据</span>
        </button>
      </div>
    </div>

    <!-- 最近记录 -->
    <div class="records-grid">
      <div class="section-card">
        <div class="section-header">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="20" height="20"><path d="M19.428 15.428a2 2 0 00-1.022-.547l-2.387-.477a6 6 0 00-3.86.517l-.318.158a6 6 0 01-3.86.517L6.05 15.21a2 2 0 00-1.806.547M8 4h8l-1 1v5.172a2 2 0 00.586 1.414l5 5c1.26 1.26.367 3.414-1.415 3.414H4.828c-1.782 0-2.674-2.154-1.414-3.414l5-5A2 2 0 009 10.172V5L8 4z"/></svg>
          <h3>最近血糖记录</h3>
        </div>
        <div v-loading="loadingGlucose">
          <div v-if="recentGlucose.length === 0" class="empty-state">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" width="48" height="48"><path d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z"/></svg>
            <p>暂无血糖记录</p>
            <button class="empty-btn" @click="$router.push('/health/glucose')">添加记录</button>
          </div>
          <div v-else class="record-list">
            <div v-for="record in recentGlucose" :key="record.measureTime" class="record-item">
              <div class="record-value glucose-value">{{ record.value }} mmol/L</div>
              <div class="record-meta">
                <span class="record-type">{{ getMeasureTypeLabel(record.measureType) }}</span>
                <span class="record-time">{{ formatTime(record.measureTime) }}</span>
              </div>
              <span class="record-level" :class="record.level">{{ getLevelLabel(record.level) }}</span>
            </div>
          </div>
        </div>
      </div>
      
      <div class="section-card">
        <div class="section-header">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="20" height="20"><path d="M4.318 6.318a4.5 4.5 0 000 6.364L12 20.364l7.682-7.682a4.5 4.5 0 00-6.364-6.364L12 7.636l-1.318-1.318a4.5 4.5 0 00-6.364 0z"/></svg>
          <h3>最近血压记录</h3>
        </div>
        <div v-loading="loadingPressure">
          <div v-if="recentPressure.length === 0" class="empty-state">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" width="48" height="48"><path d="M4.318 6.318a4.5 4.5 0 000 6.364L12 20.364l7.682-7.682a4.5 4.5 0 00-6.364-6.364L12 7.636l-1.318-1.318a4.5 4.5 0 00-6.364 0z"/></svg>
            <p>暂无血压记录</p>
            <button class="empty-btn pressure" @click="$router.push('/health/pressure')">添加记录</button>
          </div>
          <div v-else class="record-list">
            <div v-for="record in recentPressure" :key="record.measureTime" class="record-item">
              <div class="record-value pressure-value">{{ record.systolic }}/{{ record.diastolic }} mmHg</div>
              <div class="record-meta">
                <span class="record-type">{{ getMeasureStateLabel(record.measureState) }}</span>
                <span class="record-time">{{ formatTime(record.measureTime) }}</span>
              </div>
              <span class="record-level" :class="record.level">{{ getLevelLabel(record.level) }}</span>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 快速添加对话框 -->
    <el-dialog v-model="showGlucoseDialog" title="快速记录血糖" width="400px">
      <el-form ref="glucoseFormRef" :model="glucoseForm" :rules="glucoseRules" label-width="80px">
        <el-form-item label="血糖值" prop="value">
          <el-input-number v-model="glucoseForm.value" :min="0.1" :max="50" :step="0.1" :precision="1" placeholder="请输入血糖值" style="width: 100%"/>
          <span class="input-suffix">mmol/L</span>
        </el-form-item>
        <el-form-item label="测量类型" prop="measureType">
          <el-select v-model="glucoseForm.measureType" placeholder="请选择测量类型" style="width: 100%">
            <el-option label="空腹" value="fasting"/><el-option label="餐后" value="after_meal"/><el-option label="随机" value="random"/>
          </el-select>
        </el-form-item>
        <el-form-item label="测量时间" prop="measureTime">
          <el-date-picker v-model="glucoseForm.measureTime" type="datetime" placeholder="请选择测量时间" format="YYYY-MM-DD HH:mm" value-format="YYYY-MM-DD HH:mm:ss" style="width: 100%"/>
        </el-form-item>
      </el-form>
      <template #footer><el-button @click="showGlucoseDialog = false">取消</el-button><el-button type="primary" @click="saveGlucoseRecord" :loading="saving">确定</el-button></template>
    </el-dialog>

    <el-dialog v-model="showPressureDialog" title="快速记录血压" width="400px">
      <el-form ref="pressureFormRef" :model="pressureForm" :rules="pressureRules" label-width="80px">
        <el-form-item label="收缩压" prop="systolic"><el-input-number v-model="pressureForm.systolic" :min="50" :max="300" placeholder="收缩压" style="width: 100%"/><span class="input-suffix">mmHg</span></el-form-item>
        <el-form-item label="舒张压" prop="diastolic"><el-input-number v-model="pressureForm.diastolic" :min="30" :max="200" placeholder="舒张压" style="width: 100%"/><span class="input-suffix">mmHg</span></el-form-item>
        <el-form-item label="心率"><el-input-number v-model="pressureForm.heartRate" :min="30" :max="250" placeholder="心率（可选）" style="width: 100%"/><span class="input-suffix">bpm</span></el-form-item>
        <el-form-item label="测量时间" prop="measureTime"><el-date-picker v-model="pressureForm.measureTime" type="datetime" placeholder="请选择测量时间" format="YYYY-MM-DD HH:mm" value-format="YYYY-MM-DD HH:mm:ss" style="width: 100%"/></el-form-item>
      </el-form>
      <template #footer><el-button @click="showPressureDialog = false">取消</el-button><el-button type="primary" @click="savePressureRecord" :loading="saving">确定</el-button></template>
    </el-dialog>

    <el-dialog v-model="showWeightDialog" title="快速记录体重" width="400px">
      <el-form ref="weightFormRef" :model="weightForm" :rules="weightRules" label-width="80px">
        <el-form-item label="体重" prop="weight"><el-input-number v-model="weightForm.weight" :min="1" :max="500" :step="0.1" :precision="1" placeholder="请输入体重" style="width: 100%"/><span class="input-suffix">kg</span></el-form-item>
        <el-form-item label="身高"><el-input-number v-model="weightForm.height" :min="50" :max="250" :step="0.1" :precision="1" placeholder="身高（可选）" style="width: 100%"/><span class="input-suffix">cm</span></el-form-item>
        <el-form-item label="测量时间" prop="measureTime"><el-date-picker v-model="weightForm.measureTime" type="datetime" placeholder="请选择测量时间" format="YYYY-MM-DD HH:mm" value-format="YYYY-MM-DD HH:mm:ss" style="width: 100%"/></el-form-item>
      </el-form>
      <template #footer><el-button @click="showWeightDialog = false">取消</el-button><el-button type="primary" @click="saveWeightRecord" :loading="saving">确定</el-button></template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import dayjs from 'dayjs'
import * as healthApi from '@/api/health'

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

const glucoseForm = reactive({ value: null, measureType: '', measureTime: new Date() })
const pressureForm = reactive({ systolic: null, diastolic: null, heartRate: null, measureTime: new Date() })
const weightForm = reactive({ weight: null, height: null, measureTime: new Date() })

const glucoseRules = { value: [{ required: true, message: '请输入血糖值', trigger: 'blur' }], measureType: [{ required: true, message: '请选择测量类型', trigger: 'change' }], measureTime: [{ required: true, message: '请选择测量时间', trigger: 'change' }] }
const pressureRules = { systolic: [{ required: true, message: '请输入收缩压', trigger: 'blur' }], diastolic: [{ required: true, message: '请输入舒张压', trigger: 'blur' }], measureTime: [{ required: true, message: '请选择测量时间', trigger: 'change' }] }
const weightRules = { weight: [{ required: true, message: '请输入体重', trigger: 'blur' }], measureTime: [{ required: true, message: '请选择测量时间', trigger: 'change' }] }

const formatPressure = (systolic, diastolic) => (systolic && diastolic) ? `${systolic}/${diastolic} mmHg` : '--'
const formatTime = (time) => dayjs(time).format('MM-DD HH:mm')
const getMeasureTypeLabel = (type) => ({ fasting: '空腹', after_meal: '餐后', random: '随机' }[type] || type)
const getMeasureStateLabel = (state) => ({ rest: '休息', activity: '活动后', morning: '晨起', evening: '晚间' }[state] || state)
const getLevelLabel = (level) => ({ normal: '正常', high: '偏高', low: '偏低' }[level] || level)

const saveGlucoseRecord = async () => { if (saving.value) return; try { const valid = await glucoseFormRef.value.validate(); if (!valid) return; saving.value = true; await healthApi.addBloodGlucoseRecord(glucoseForm); ElMessage.success('血糖记录添加成功'); showGlucoseDialog.value = false; Object.assign(glucoseForm, { value: null, measureType: '', measureTime: new Date() }); await loadOverviewData(); await loadRecentRecords() } catch (error) { ElMessage.error('保存失败') } finally { saving.value = false } }
const savePressureRecord = async () => { if (saving.value) return; try { const valid = await pressureFormRef.value.validate(); if (!valid) return; saving.value = true; await healthApi.addBloodPressureRecord(pressureForm); ElMessage.success('血压记录添加成功'); showPressureDialog.value = false; Object.assign(pressureForm, { systolic: null, diastolic: null, heartRate: null, measureTime: new Date() }); await loadOverviewData(); await loadRecentRecords() } catch (error) { ElMessage.error('保存失败') } finally { saving.value = false } }
const saveWeightRecord = async () => { if (saving.value) return; try { const valid = await weightFormRef.value.validate(); if (!valid) return; saving.value = true; await healthApi.addWeightRecord(weightForm); ElMessage.success('体重记录添加成功'); showWeightDialog.value = false; Object.assign(weightForm, { weight: null, height: null, measureTime: new Date() }); await loadOverviewData() } catch (error) { ElMessage.error('保存失败') } finally { saving.value = false } }

const loadOverviewData = async () => { try { loading.value = true; const statisticsRes = await healthApi.getHealthStatistics(); const statisticsData = statisticsRes.data || {}; const glucoseData = statisticsData.glucose || {}; const pressureData = statisticsData.pressure || {}; const weightData = statisticsData.weight || {}; overview.value = { glucoseCount: glucoseData.totalCount || 0, avgGlucose: glucoseData.avgValue || 0, pressureCount: pressureData.totalCount || 0, avgSystolic: pressureData.avgSystolic || 0, avgDiastolic: pressureData.avgDiastolic || 0, weightCount: weightData.totalCount || 0, currentBmi: weightData.currentBmi || 0, totalRecords: statisticsData.totalRecords || 0 } } catch (error) { overview.value = { glucoseCount: 0, avgGlucose: 0, pressureCount: 0, avgSystolic: 0, avgDiastolic: 0, weightCount: 0, currentBmi: 0, totalRecords: 0 } } finally { loading.value = false } }

const loadRecentRecords = async () => { try { loadingGlucose.value = true; loadingPressure.value = true; try { const glucoseRes = await healthApi.getBloodGlucoseRecords({ current: 1, size: 5 }); if (glucoseRes.data?.records) { recentGlucose.value = glucoseRes.data.records.sort((a, b) => new Date(b.measureTime) - new Date(a.measureTime)) } else { recentGlucose.value = [] } } catch (e) { recentGlucose.value = [] } try { const pressureRes = await healthApi.getBloodPressureRecords({ current: 1, size: 5 }); if (pressureRes.data?.records) { recentPressure.value = pressureRes.data.records.sort((a, b) => new Date(b.measureTime) - new Date(a.measureTime)) } else { recentPressure.value = [] } } catch (e) { recentPressure.value = [] } } finally { loadingGlucose.value = false; loadingPressure.value = false } }

onMounted(() => { loadOverviewData(); loadRecentRecords() })
</script>

<style scoped>
.health-overview { max-width: 1200px; margin: 0 auto; }

/* 统计卡片网格 */
.stats-grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: 20px; margin-bottom: 24px; }
.stat-card { background: white; border-radius: 16px; padding: 24px; display: flex; align-items: center; gap: 16px; border: 1px solid #E2E8F0; cursor: pointer; transition: all 0.3s; position: relative; }
.stat-card:hover { transform: translateY(-4px); box-shadow: 0 12px 24px rgba(8, 145, 178, 0.15); }
.stat-card.glucose:hover { border-color: #0891B2; }
.stat-card.pressure:hover { border-color: #059669; }
.stat-card.weight:hover { border-color: #D97706; }
.stat-card.total:hover { border-color: #7C3AED; }
.stat-icon { width: 60px; height: 60px; border-radius: 16px; display: flex; align-items: center; justify-content: center; color: white; flex-shrink: 0; }
.stat-card.glucose .stat-icon { background: linear-gradient(135deg, #0891B2 0%, #22D3EE 100%); }
.stat-card.pressure .stat-icon { background: linear-gradient(135deg, #059669 0%, #10B981 100%); }
.stat-card.weight .stat-icon { background: linear-gradient(135deg, #D97706 0%, #F59E0B 100%); }
.stat-card.total .stat-icon { background: linear-gradient(135deg, #7C3AED 0%, #A78BFA 100%); }
.stat-content { flex: 1; display: flex; flex-direction: column; }
.stat-title { font-size: 0.875rem; color: #64748B; margin-bottom: 4px; }
.stat-value { font-size: 1.75rem; font-weight: 700; color: #164E63; margin-bottom: 4px; }
.stat-sub { font-size: 0.8rem; color: #94A3B8; }
.stat-arrow { position: absolute; right: 20px; color: #CBD5E1; transition: transform 0.2s; }
.stat-card:hover .stat-arrow { transform: translateX(4px); color: #0891B2; }

/* 区块卡片 */
.section-card { background: white; border-radius: 16px; padding: 24px; border: 1px solid #E2E8F0; margin-bottom: 24px; }
.section-header { display: flex; align-items: center; gap: 10px; margin-bottom: 20px; padding-bottom: 16px; border-bottom: 1px solid #E2E8F0; }
.section-header svg { color: #0891B2; }
.section-header h3 { font-size: 1.1rem; font-weight: 600; color: #164E63; margin: 0; }

/* 快捷操作 */
.quick-actions { display: grid; grid-template-columns: repeat(4, 1fr); gap: 16px; }
.action-btn { display: flex; flex-direction: column; align-items: center; gap: 10px; padding: 20px; border: 2px solid #E2E8F0; border-radius: 14px; background: white; cursor: pointer; transition: all 0.2s; font-size: 0.9rem; font-weight: 500; color: #164E63; }
.action-btn:hover { transform: translateY(-2px); }
.action-btn.glucose { border-color: #A5F3FC; }
.action-btn.glucose:hover { background: #ECFEFF; color: #0891B2; }
.action-btn.glucose svg { color: #0891B2; }
.action-btn.pressure { border-color: #BBF7D0; }
.action-btn.pressure:hover { background: #F0FDF4; color: #059669; }
.action-btn.pressure svg { color: #059669; }
.action-btn.weight { border-color: #FED7AA; }
.action-btn.weight:hover { background: #FFF7ED; color: #D97706; }
.action-btn.weight svg { color: #D97706; }
.action-btn.refresh { border-color: #E2E8F0; }
.action-btn.refresh:hover { background: #F8FAFC; color: #64748B; }
.action-btn.refresh svg { color: #64748B; }

/* 记录网格 */
.records-grid { display: grid; grid-template-columns: repeat(2, 1fr); gap: 24px; }

/* 记录列表 */
.record-list { display: flex; flex-direction: column; gap: 12px; max-height: 320px; overflow-y: auto; }
.record-item { display: flex; align-items: center; justify-content: space-between; padding: 14px 16px; background: #F8FAFC; border-radius: 12px; transition: background 0.2s; }
.record-item:hover { background: #F0FDFA; }
.record-value { font-size: 1.1rem; font-weight: 700; }
.record-value.glucose-value { color: #0891B2; }
.record-value.pressure-value { color: #059669; }
.record-meta { flex: 1; margin: 0 16px; display: flex; flex-direction: column; gap: 2px; }
.record-type { font-size: 0.875rem; color: #164E63; font-weight: 500; }
.record-time { font-size: 0.75rem; color: #94A3B8; }
.record-level { padding: 4px 10px; border-radius: 6px; font-size: 0.75rem; font-weight: 600; }
.record-level.normal { background: #D1FAE5; color: #059669; }
.record-level.high { background: #FEE2E2; color: #DC2626; }
.record-level.low { background: #FEF3C7; color: #D97706; }

/* 空状态 */
.empty-state { text-align: center; padding: 40px 20px; color: #94A3B8; }
.empty-state svg { margin-bottom: 12px; }
.empty-state p { margin: 0 0 16px; font-size: 0.9rem; }
.empty-btn { padding: 10px 20px; background: #0891B2; color: white; border: none; border-radius: 10px; font-size: 0.875rem; font-weight: 500; cursor: pointer; transition: background 0.2s; }
.empty-btn:hover { background: #0E7490; }
.empty-btn.pressure { background: #059669; }
.empty-btn.pressure:hover { background: #047857; }

.input-suffix { margin-left: 8px; color: #94A3B8; font-size: 0.875rem; }

/* 对话框样式 */
:deep(.el-dialog) { border-radius: 16px; }
:deep(.el-dialog__header) { border-bottom: 1px solid #E2E8F0; padding: 20px 24px; }
:deep(.el-dialog__title) { font-weight: 600; color: #164E63; }
:deep(.el-dialog__body) { padding: 24px; }
:deep(.el-dialog__footer) { border-top: 1px solid #E2E8F0; padding: 16px 24px; }

/* 响应式 */
@media (max-width: 1024px) { .stats-grid { grid-template-columns: repeat(2, 1fr); } .quick-actions { grid-template-columns: repeat(2, 1fr); } }
@media (max-width: 768px) { .stats-grid { grid-template-columns: 1fr; } .quick-actions { grid-template-columns: 1fr; } .records-grid { grid-template-columns: 1fr; } .record-item { flex-direction: column; align-items: flex-start; gap: 8px; } .record-meta { margin: 0; } }
</style>
