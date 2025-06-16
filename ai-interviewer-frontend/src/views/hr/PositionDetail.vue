<template>
  <div class="position-detail-container">
    <el-row :gutter="20">
      <el-col :span="24">
        <el-page-header @back="goBack">
          <template #content>
            <div class="page-title">职位详情</div>
          </template>
        </el-page-header>
      </el-col>
    </el-row>

    <div v-loading="loading" class="detail-content">
      <!-- 职位基本信息 -->
      <el-card class="info-card">
        <div class="position-header">
          <div class="header-left">
            <h1 class="position-title">{{ positionInfo.title }}</h1>
            <div class="position-tags">
              <el-tag :type="getJobTypeTag(positionInfo.jobType)">
                {{ getJobTypeText(positionInfo.jobType) }}
              </el-tag>
              <el-tag v-if="positionInfo.status === 1" type="success">招聘中</el-tag>
              <el-tag v-else-if="positionInfo.status === 2" type="danger">已关闭</el-tag>
              <el-tag v-else type="warning">已暂停</el-tag>
            </div>
          </div>
          <div class="header-right">
            <el-button type="primary" @click="editPosition">编辑职位</el-button>
          </div>
        </div>

        <el-divider />

        <el-row :gutter="20">
          <el-col :span="6">
            <div class="info-item">
              <div class="info-label">职位编号</div>
              <div class="info-value">{{ positionInfo.positionCode || '-' }}</div>
            </div>
          </el-col>
          <el-col :span="6">
            <div class="info-item">
              <div class="info-label">工作地点</div>
              <div class="info-value">{{ positionInfo.workLocation }}</div>
            </div>
          </el-col>
          <el-col :span="6">
            <div class="info-item">
              <div class="info-label">所属部门</div>
              <div class="info-value">{{ positionInfo.department }}</div>
            </div>
          </el-col>
          <el-col :span="6">
            <div class="info-item">
              <div class="info-label">薪资范围</div>
              <div class="info-value">{{ formatSalary(positionInfo.salaryMin, positionInfo.salaryMax) }}</div>
            </div>
          </el-col>
        </el-row>

        <el-row :gutter="20" style="margin-top: 20px;">
          <el-col :span="6">
            <div class="info-item">
              <div class="info-label">经验要求</div>
              <div class="info-value">{{ positionInfo.experienceRequired || '不限' }}</div>
            </div>
          </el-col>
          <el-col :span="6">
            <div class="info-item">
              <div class="info-label">学历要求</div>
              <div class="info-value">{{ positionInfo.educationRequired || '不限' }}</div>
            </div>
          </el-col>
          <el-col :span="6">
            <div class="info-item">
              <div class="info-label">招聘人数</div>
              <div class="info-value">{{ positionInfo.headcount || 1 }} 人</div>
            </div>
          </el-col>
          <el-col :span="6">
            <div class="info-item">
              <div class="info-label">优先级</div>
              <div class="info-value">
                <el-rate v-model="positionInfo.priority" disabled />
              </div>
            </div>
          </el-col>
        </el-row>

        <el-row :gutter="20" style="margin-top: 20px;">
          <el-col :span="6">
            <div class="info-item">
              <div class="info-label">发布时间</div>
              <div class="info-value">{{ formatDate(positionInfo.publishTime) }}</div>
            </div>
          </el-col>
          <el-col :span="6">
            <div class="info-item">
              <div class="info-label">截止时间</div>
              <div class="info-value">{{ formatDate(positionInfo.closeTime) }}</div>
            </div>
          </el-col>
          <el-col :span="6">
            <div class="info-item">
              <div class="info-label">创建时间</div>
              <div class="info-value">{{ formatDate(positionInfo.createTime) }}</div>
            </div>
          </el-col>
          <el-col :span="6">
            <div class="info-item">
              <div class="info-label">更新时间</div>
              <div class="info-value">{{ formatDate(positionInfo.updateTime) }}</div>
            </div>
          </el-col>
        </el-row>
      </el-card>

      <!-- 职位描述 -->
      <el-card class="desc-card">
        <template #header>
          <div class="card-header">
            <span>职位描述</span>
          </div>
        </template>
        <div class="desc-content">
          {{ positionInfo.description || '暂无职位描述' }}
        </div>
      </el-card>

      <!-- 职位要求 -->
      <el-card class="requirements-card">
        <template #header>
          <div class="card-header">
            <span>职位要求</span>
          </div>
        </template>
        <div class="requirements-content">
          {{ positionInfo.requirements || '暂无职位要求' }}
        </div>
      </el-card>

      <!-- 福利待遇 -->
      <el-card class="benefits-card" v-if="positionInfo.benefits">
        <template #header>
          <div class="card-header">
            <span>福利待遇</span>
          </div>
        </template>
        <div class="benefits-content">
          {{ positionInfo.benefits }}
        </div>
      </el-card>

      <!-- 申请者列表 -->
      <el-card class="applicants-card">
        <template #header>
          <div class="card-header">
            <span>申请者列表</span>
            <div class="header-actions">
              <el-radio-group v-model="applicantStatus" size="small" @change="loadApplications">
                <el-radio-button label="">全部</el-radio-button>
                <el-radio-button label="PENDING">待处理</el-radio-button>
                <el-radio-button label="REVIEWING">审核中</el-radio-button>
                <el-radio-button label="PASSED">通过</el-radio-button>
                <el-radio-button label="REJECTED">拒绝</el-radio-button>
                <el-radio-button label="INTERVIEW_SCHEDULED">已安排面试</el-radio-button>
              </el-radio-group>
            </div>
          </div>
        </template>
        
        <el-table 
          :data="applicationList" 
          style="width: 100%" 
          v-loading="applicationLoading"
          border
        >
          <el-table-column prop="applicationNo" label="申请编号" width="120" />
          <el-table-column label="申请人" width="120">
            <template #default="scope">
              <div class="applicant-info">
                <el-avatar :size="30" :src="scope.row.candidateAvatar">
                  {{ scope.row.candidateName?.substring(0, 1) }}
                </el-avatar>
                <span>{{ scope.row.candidateName }}</span>
              </div>
            </template>
          </el-table-column>
          <el-table-column label="简历" min-width="200">
            <template #default="scope">
              <div class="resume-info">
                <span>{{ scope.row.resumeName || '未命名简历' }}</span>
                <el-button 
                  type="primary" 
                  link 
                  @click="viewResume(scope.row.resumeId)"
                >
                  查看简历
                </el-button>
              </div>
            </template>
          </el-table-column>
          <el-table-column label="申请时间" width="180">
            <template #default="scope">
              {{ formatDateTime(scope.row.applyTime) }}
            </template>
          </el-table-column>
          <el-table-column label="状态" width="150">
            <template #default="scope">
              <el-tag :type="getStatusType(scope.row.status)">
                {{ getStatusText(scope.row.status) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="250" fixed="right">
            <template #default="scope">
              <div class="operation-buttons">
                <el-button 
                  v-if="scope.row.status === 'PENDING'" 
                  type="primary" 
                  size="small"
                  @click="handleApplicationStatus(scope.row, 'PASSED')"
                >
                  通过初筛
                </el-button>
                <el-button 
                  v-if="scope.row.status === 'PENDING'" 
                  type="danger" 
                  size="small"
                  @click="handleApplicationStatus(scope.row, 'REJECTED')"
                >
                  拒绝
                </el-button>
                <el-button 
                  v-if="scope.row.status === 'PASSED'" 
                  type="success" 
                  size="small"
                  @click="scheduleInterview(scope.row)"
                >
                  安排面试
                </el-button>
                <el-button 
                  type="info" 
                  size="small"
                  @click="viewApplicationDetail(scope.row)"
                >
                  详情
                </el-button>
              </div>
            </template>
          </el-table-column>
        </el-table>
        
        <!-- 空状态 -->
        <el-empty v-if="!applicationLoading && applicationList.length === 0" description="暂无申请记录" />
        
        <!-- 分页 -->
        <div class="pagination-container" v-if="applicationTotal > 0">
          <el-pagination
            :current-page="applicationCurrentPage"
            :page-size="applicationPageSize"
            :page-sizes="[10, 20, 50]"
            :total="applicationTotal"
            layout="total, sizes, prev, pager, next, jumper"
            @size-change="handleApplicationSizeChange"
            @current-change="handleApplicationPageChange"
          />
        </div>
      </el-card>
    </div>

    <!-- 安排面试对话框 -->
    <el-dialog
      v-model="interviewDialogVisible"
      title="安排面试"
      width="500px"
    >
      <el-form
        ref="interviewFormRef"
        :model="interviewForm"
        :rules="interviewRules"
        label-width="100px"
      >
        <el-form-item label="面试官" prop="interviewerId">
          <el-select v-model="interviewForm.interviewerId" placeholder="请选择面试官" style="width: 100%">
            <el-option
              v-for="interviewer in interviewerList"
              :key="interviewer.id"
              :label="interviewer.realName"
              :value="interviewer.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="面试时间" prop="scheduledTime">
          <el-date-picker
            v-model="interviewForm.scheduledTime"
            type="datetime"
            placeholder="选择面试时间"
            style="width: 100%"
            :disabled-date="disabledDate"
          />
        </el-form-item>
        <el-form-item label="面试时长" prop="duration">
          <el-input-number
            v-model="interviewForm.duration"
            :min="30"
            :max="120"
            :step="15"
            style="width: 100%"
          />
          <div class="form-tip">单位：分钟</div>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="interviewDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitInterview" :loading="interviewSubmitLoading">
            确认安排
          </el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 拒绝原因对话框 -->
    <el-dialog
      v-model="rejectDialogVisible"
      title="拒绝申请"
      width="500px"
    >
      <el-form
        ref="rejectFormRef"
        :model="rejectForm"
        :rules="rejectRules"
        label-width="100px"
      >
        <el-form-item label="拒绝原因" prop="reason">
          <el-input
            v-model="rejectForm.reason"
            type="textarea"
            :rows="4"
            placeholder="请输入拒绝原因"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="rejectDialogVisible = false">取消</el-button>
          <el-button type="danger" @click="confirmReject" :loading="rejectSubmitLoading">
            确认拒绝
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getPositionDetail } from '@/api/position'
import { getApplicationsByPosition, updateApplicationStatus } from '@/api/application'
import { scheduleInterview } from '@/api/interview-process'
import { getUsersByRole } from '@/api/user'
import dayjs from 'dayjs'

const route = useRoute()
const router = useRouter()
const positionId = route.params.id

// 职位信息
const positionInfo = ref({})
const loading = ref(false)

// 申请列表
const applicationList = ref([])
const applicationLoading = ref(false)
const applicantStatus = ref('')
const applicationCurrentPage = ref(1)
const applicationPageSize = ref(10)
const applicationTotal = ref(0)

// 面试官列表
const interviewerList = ref([])

// 面试对话框
const interviewDialogVisible = ref(false)
const interviewSubmitLoading = ref(false)
const interviewFormRef = ref(null)
const interviewForm = reactive({
  applicationId: null,
  interviewerId: null,
  scheduledTime: '',
  duration: 60
})
const interviewRules = {
  interviewerId: [{ required: true, message: '请选择面试官', trigger: 'change' }],
  scheduledTime: [{ required: true, message: '请选择面试时间', trigger: 'change' }],
  duration: [{ required: true, message: '请输入面试时长', trigger: 'blur' }]
}

// 拒绝对话框
const rejectDialogVisible = ref(false)
const rejectSubmitLoading = ref(false)
const rejectFormRef = ref(null)
const rejectForm = reactive({
  applicationId: null,
  reason: ''
})
const rejectRules = {
  reason: [{ required: true, message: '请输入拒绝原因', trigger: 'blur' }]
}

// 获取工作类型标签
const getJobTypeTag = (type) => {
  const typeMap = {
    'FULL_TIME': 'primary',
    'PART_TIME': 'success',
    'INTERN': 'warning'
  }
  return typeMap[type] || 'info'
}

// 获取工作类型文本
const getJobTypeText = (type) => {
  const textMap = {
    'FULL_TIME': '全职',
    'PART_TIME': '兼职',
    'INTERN': '实习'
  }
  return textMap[type] || type
}

// 获取状态类型
const getStatusType = (status) => {
  const typeMap = {
    'PENDING': 'info',
    'REVIEWING': 'warning',
    'PASSED': 'success',
    'REJECTED': 'danger',
    'INTERVIEW_SCHEDULED': 'primary'
  }
  return typeMap[status] || 'info'
}

// 获取状态文本
const getStatusText = (status) => {
  const textMap = {
    'PENDING': '待处理',
    'REVIEWING': '审核中',
    'PASSED': '通过初筛',
    'REJECTED': '已拒绝',
    'INTERVIEW_SCHEDULED': '已安排面试'
  }
  return textMap[status] || status
}

// 格式化薪资
const formatSalary = (min, max) => {
  if (!min && !max) return '面议'
  if (min && max) {
    return `${min/1000}k-${max/1000}k`
  }
  return min ? `${min/1000}k起` : `${max/1000}k以内`
}

// 格式化日期
const formatDate = (date) => {
  if (!date) return '-'
  return dayjs(date).format('YYYY-MM-DD')
}

// 格式化日期时间
const formatDateTime = (date) => {
  if (!date) return '-'
  return dayjs(date).format('YYYY-MM-DD HH:mm')
}

// 禁用今天之前的日期
const disabledDate = (time) => {
  return time.getTime() < Date.now() - 8.64e7 // 不能选择今天之前的日期
}

// 返回
const goBack = () => {
  router.push('/hr/positions')
}

// 编辑职位
const editPosition = () => {
  router.push(`/hr/positions?edit=${positionId}`)
}

// 查看简历
const viewResume = (resumeId) => {
  window.open(`/hr/resume/${resumeId}`, '_blank')
}

// 查看申请详情
const viewApplicationDetail = (application) => {
  router.push(`/hr/application/${application.id}`)
}

// 处理申请状态
const handleApplicationStatus = (application, status) => {
  if (status === 'REJECTED') {
    // 显示拒绝原因对话框
    rejectForm.applicationId = application.id
    rejectDialogVisible.value = true
  } else {
    updateStatus(application.id, status)
  }
}

// 更新申请状态
const updateStatus = async (applicationId, status, remark) => {
  try {
    const res = await updateApplicationStatus(applicationId, status, remark)
    if (res.code === 200) {
      if (status === 'PASSED') {
        ElMessage({
          message: '已通过初筛，系统已自动创建面试',
          type: 'success',
          duration: 3000
        })
      } else {
        ElMessage.success('操作成功')
      }
      loadApplications()
    } else {
      ElMessage.error(res.message || '操作失败')
    }
  } catch (error) {
    console.error('更新申请状态失败:', error)
    ElMessage.error('更新申请状态失败')
  }
}

// 确认拒绝
const confirmReject = async () => {
  if (!rejectFormRef.value) return
  
  await rejectFormRef.value.validate(async (valid) => {
    if (!valid) return
    
    try {
      rejectSubmitLoading.value = true
      await updateStatus(rejectForm.applicationId, 'REJECTED', rejectForm.reason)
      rejectDialogVisible.value = false
      rejectForm.reason = ''
    } catch (error) {
      console.error('拒绝申请失败:', error)
    } finally {
      rejectSubmitLoading.value = false
    }
  })
}

// 安排面试
const scheduleInterview = (application) => {
  interviewForm.applicationId = application.id
  interviewDialogVisible.value = true
}

// 提交面试安排
const submitInterview = async () => {
  if (!interviewFormRef.value) return
  
  await interviewFormRef.value.validate(async (valid) => {
    if (!valid) return
    
    try {
      interviewSubmitLoading.value = true
      
      // 处理日期格式
      const scheduledTime = dayjs(interviewForm.scheduledTime).format('YYYY-MM-DDTHH:mm:ss')
      
      const res = await scheduleInterview({
        applicationId: interviewForm.applicationId,
        interviewerId: interviewForm.interviewerId,
        scheduledTime,
        duration: interviewForm.duration
      })
      
      if (res.code === 200) {
        ElMessage.success('面试安排成功')
        interviewDialogVisible.value = false
        loadApplications()
      } else {
        ElMessage.error(res.message || '面试安排失败')
      }
    } catch (error) {
      console.error('安排面试失败:', error)
      ElMessage.error('安排面试失败')
    } finally {
      interviewSubmitLoading.value = false
    }
  })
}

// 加载职位详情
const loadPositionDetail = async () => {
  try {
    loading.value = true
    const res = await getPositionDetail(positionId)
    if (res.data) {
      positionInfo.value = res.data
    }
  } catch (error) {
    console.error('获取职位详情失败:', error)
    ElMessage.error('获取职位详情失败')
  } finally {
    loading.value = false
  }
}

// 加载申请列表
const loadApplications = async () => {
  try {
    applicationLoading.value = true
    
    const params = {
      current: applicationCurrentPage.value,
      size: applicationPageSize.value,
      positionId
    }
    
    if (applicantStatus.value) {
      params.status = applicantStatus.value
    }
    
    const res = await getApplicationsByPosition(params)
    
    if (res.data) {
      applicationList.value = res.data.records || []
      applicationTotal.value = res.data.total || 0
    }
  } catch (error) {
    console.error('获取申请列表失败:', error)
    ElMessage.error('获取申请列表失败')
  } finally {
    applicationLoading.value = false
  }
}

// 加载面试官列表
const loadInterviewers = async () => {
  try {
    const res = await getUsersByRole('INTERVIEWER')
    if (res.data) {
      interviewerList.value = res.data || []
    }
  } catch (error) {
    console.error('获取面试官列表失败:', error)
    ElMessage.error('获取面试官列表失败')
  }
}

// 申请分页大小改变
const handleApplicationSizeChange = (val) => {
  applicationPageSize.value = val
  applicationCurrentPage.value = 1
  loadApplications()
}

// 申请页码改变
const handleApplicationPageChange = (val) => {
  applicationCurrentPage.value = val
  loadApplications()
}

onMounted(() => {
  loadPositionDetail()
  loadApplications()
  loadInterviewers()
})
</script>

<style scoped>
.position-detail-container {
  padding: 20px;
}

.page-title {
  font-size: 18px;
  font-weight: bold;
}

.detail-content {
  margin-top: 20px;
}

.info-card {
  margin-bottom: 20px;
}

.position-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
}

.position-title {
  margin: 0 0 10px 0;
  font-size: 22px;
}

.position-tags {
  display: flex;
  gap: 10px;
}

.info-item {
  margin-bottom: 10px;
}

.info-label {
  font-size: 14px;
  color: #606266;
  margin-bottom: 5px;
}

.info-value {
  font-size: 16px;
  color: #303133;
}

.desc-card,
.requirements-card,
.benefits-card,
.applicants-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.desc-content,
.requirements-content,
.benefits-content {
  white-space: pre-line;
  line-height: 1.6;
}

.applicant-info {
  display: flex;
  align-items: center;
  gap: 10px;
}

.resume-info {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.operation-buttons {
  display: flex;
  gap: 5px;
  flex-wrap: wrap;
}

.pagination-container {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}

.form-tip {
  font-size: 12px;
  color: #909399;
  margin-top: 5px;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
}
</style> 