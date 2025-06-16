<template>
  <div class="application-detail-container">
    <el-row :gutter="20">
      <el-col :span="24">
        <el-page-header @back="goBack">
          <template #content>
            <div class="page-title">申请详情</div>
          </template>
        </el-page-header>
      </el-col>
    </el-row>

    <div v-loading="loading" class="detail-content">
      <!-- 申请信息 -->
      <el-card class="info-card">
        <template #header>
          <div class="card-header">
            <span>申请信息</span>
            <el-tag :type="getStatusType(applicationInfo.status)">
              {{ getStatusText(applicationInfo.status) }}
            </el-tag>
          </div>
        </template>

        <el-descriptions :column="2" border>
          <el-descriptions-item label="申请编号">{{ applicationInfo.applicationNo }}</el-descriptions-item>
          <el-descriptions-item label="申请时间">{{ formatDateTime(applicationInfo.applyTime) }}</el-descriptions-item>
          <el-descriptions-item label="申请职位">{{ applicationInfo.positionTitle }}</el-descriptions-item>
          <el-descriptions-item label="来源">{{ applicationInfo.source || '网站申请' }}</el-descriptions-item>
          <el-descriptions-item label="审核时间" v-if="applicationInfo.screenTime">
            {{ formatDateTime(applicationInfo.screenTime) }}
          </el-descriptions-item>
          <el-descriptions-item label="HR备注" v-if="applicationInfo.hrComment">
            {{ applicationInfo.hrComment }}
          </el-descriptions-item>
        </el-descriptions>
      </el-card>

      <!-- 候选人信息 -->
      <el-card class="candidate-card">
        <template #header>
          <div class="card-header">
            <span>候选人信息</span>
          </div>
        </template>

        <el-descriptions :column="3" border>
          <el-descriptions-item label="姓名">{{ resumeInfo.realName }}</el-descriptions-item>
          <el-descriptions-item label="性别">{{ resumeInfo.gender }}</el-descriptions-item>
          <el-descriptions-item label="年龄">{{ calculateAge(resumeInfo.birthDate) }}岁</el-descriptions-item>
          <el-descriptions-item label="手机">{{ resumeInfo.phone }}</el-descriptions-item>
          <el-descriptions-item label="邮箱">{{ resumeInfo.email }}</el-descriptions-item>
          <el-descriptions-item label="现居地">{{ resumeInfo.currentLocation }}</el-descriptions-item>
        </el-descriptions>
      </el-card>

      <!-- 简历详情 -->
      <el-card class="resume-card">
        <template #header>
          <div class="card-header">
            <span>简历详情</span>
            <el-button
                v-if="resumeInfo.resumeType === 'UPLOAD' && resumeInfo.resumeUrl"
                type="primary"
                size="small"
                @click="downloadResume"
            >
              下载简历
            </el-button>
          </div>
        </template>

        <div v-if="resumeInfo.resumeType === 'ONLINE'">
          <!-- 教育背景 -->
          <div class="resume-section">
            <h4>教育背景</h4>
            <el-descriptions :column="2">
              <el-descriptions-item label="最高学历">{{ resumeInfo.highestEducation }}</el-descriptions-item>
              <el-descriptions-item label="毕业院校">{{ resumeInfo.graduationSchool }}</el-descriptions-item>
              <el-descriptions-item label="专业">{{ resumeInfo.major }}</el-descriptions-item>
              <el-descriptions-item label="毕业年份">{{ resumeInfo.graduationYear }}</el-descriptions-item>
            </el-descriptions>
          </div>

          <!-- 工作经历 -->
          <div class="resume-section">
            <h4>工作经历</h4>
            <el-descriptions :column="2">
              <el-descriptions-item label="工作年限">{{ resumeInfo.workYears }}年</el-descriptions-item>
              <el-descriptions-item label="当前公司">{{ resumeInfo.currentCompany }}</el-descriptions-item>
              <el-descriptions-item label="当前职位" :span="2">{{ resumeInfo.currentPosition }}</el-descriptions-item>
            </el-descriptions>
            <div class="work-experience" v-if="resumeInfo.workExperience">
              <div class="section-label">详细经历：</div>
              <div class="section-content">{{ resumeInfo.workExperience }}</div>
            </div>
          </div>

          <!-- 技能特长 -->
          <div class="resume-section">
            <h4>技能特长</h4>
            <div class="section-content">{{ resumeInfo.skills || '暂无' }}</div>
          </div>

          <!-- 自我评价 -->
          <div class="resume-section">
            <h4>自我评价</h4>
            <div class="section-content">{{ resumeInfo.selfEvaluation || '暂无' }}</div>
          </div>

          <!-- 求职意向 -->
          <div class="resume-section">
            <h4>求职意向</h4>
            <el-descriptions :column="2">
              <el-descriptions-item label="期望地点">{{ resumeInfo.expectedLocation }}</el-descriptions-item>
              <el-descriptions-item label="期望薪资">
                {{ formatSalary(resumeInfo.expectedSalaryMin, resumeInfo.expectedSalaryMax) }}
              </el-descriptions-item>
              <el-descriptions-item label="到岗时间">{{ formatDate(resumeInfo.availableDate) }}</el-descriptions-item>
            </el-descriptions>
          </div>
        </div>

        <div v-else-if="resumeInfo.resumeType === 'UPLOAD'">
          <el-empty description="该候选人上传了PDF简历，请点击上方按钮下载查看" />
        </div>
      </el-card>

      <!-- 操作按钮 -->
      <div class="action-buttons" v-if="applicationInfo.status === 'PENDING'">
        <el-button type="success" size="large" @click="screenApplication('PASSED')">
          通过初筛
        </el-button>
        <el-button type="danger" size="large" @click="screenApplication('REJECTED')">
          拒绝申请
        </el-button>
      </div>
    </div>

    <!-- 初筛对话框 -->
    <el-dialog
        v-model="screenDialogVisible"
        :title="screenStatus === 'PASSED' ? '通过初筛' : '拒绝申请'"
        width="500px"
    >
      <el-form :model="screenForm" label-width="80px">
        <el-form-item label="备注" prop="remark">
          <el-input
              v-model="screenForm.remark"
              type="textarea"
              :rows="4"
              :placeholder="screenStatus === 'PASSED' ? '请输入通过理由或备注信息' : '请输入拒绝理由'"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="screenDialogVisible = false">取消</el-button>
          <el-button
              :type="screenStatus === 'PASSED' ? 'success' : 'danger'"
              @click="confirmScreen"
              :loading="screenLoading"
          >
            确认
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getApplicationDetail, updateApplicationStatus } from '@/api/application'
import { getResumeDetail } from '@/api/resume'
import dayjs from 'dayjs'

const route = useRoute()
const router = useRouter()
const applicationId = route.params.id

// 申请信息
const applicationInfo = ref({})
const resumeInfo = ref({})
const loading = ref(false)

// 初筛对话框
const screenDialogVisible = ref(false)
const screenLoading = ref(false)
const screenStatus = ref('')
const screenForm = reactive({
  remark: ''
})

// 返回
const goBack = () => {
  router.push('/hr/applications')
}

// 格式化日期时间
const formatDateTime = (datetime) => {
  if (!datetime) return '-'
  return dayjs(datetime).format('YYYY-MM-DD HH:mm')
}

// 格式化日期
const formatDate = (date) => {
  if (!date) return '-'
  return dayjs(date).format('YYYY-MM-DD')
}

// 计算年龄
const calculateAge = (birthDate) => {
  if (!birthDate) return '-'
  return dayjs().diff(dayjs(birthDate), 'year')
}

// 格式化薪资
const formatSalary = (min, max) => {
  if (!min && !max) return '面议'
  if (min && max) {
    return `${min/1000}k-${max/1000}k`
  }
  return min ? `${min/1000}k起` : `${max/1000}k以内`
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
    'PASSED': '已通过',
    'REJECTED': '已拒绝',
    'INTERVIEW_SCHEDULED': '已安排面试'
  }
  return textMap[status] || status
}

// 下载简历
const downloadResume = () => {
  if (resumeInfo.value.resumeUrl) {
    window.open(resumeInfo.value.resumeUrl, '_blank')
  } else {
    ElMessage.warning('简历文件不存在')
  }
}

// 初筛申请
const screenApplication = (status) => {
  screenStatus.value = status
  screenForm.remark = ''
  screenDialogVisible.value = true
}

// 确认初筛
const confirmScreen = async () => {
  try {
    screenLoading.value = true

    const response = await updateApplicationStatus(
        applicationId,
        screenStatus.value,
        screenForm.remark
    )

    if (screenStatus.value === 'PASSED') {
      ElMessage({
        message: '已通过初筛，系统已自动创建面试',
        type: 'success',
        duration: 3000
      })
    } else {
      ElMessage.success('已拒绝申请')
    }
    
    screenDialogVisible.value = false

    // 刷新页面
    loadApplicationDetail()
  } catch (error) {
    console.error('操作失败:', error)
    ElMessage.error('操作失败')
  } finally {
    screenLoading.value = false
  }
}

// 加载申请详情
const loadApplicationDetail = async () => {
  try {
    loading.value = true

    // 获取申请信息
    const res = await getApplicationDetail(applicationId)

    if (res.data) {
      applicationInfo.value = res.data

      // 获取简历信息
      if (res.data.resumeId) {
        const resumeRes = await getResumeDetail(res.data.resumeId)
        if (resumeRes.data) {
          resumeInfo.value = resumeRes.data
        }
      }
    }
  } catch (error) {
    console.error('获取申请详情失败:', error)
    ElMessage.error('获取申请详情失败')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadApplicationDetail()
})
</script>

<style scoped>
.application-detail-container {
  padding: 20px 0;
}

.page-title {
  font-size: 18px;
  font-weight: bold;
}

.detail-content {
  margin-top: 20px;
}

.info-card,
.candidate-card,
.resume-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.resume-section {
  margin-bottom: 30px;
  padding-bottom: 20px;
  border-bottom: 1px solid #EBEEF5;
}

.resume-section:last-child {
  border-bottom: none;
}

.resume-section h4 {
  margin-bottom: 15px;
  color: #303133;
}

.section-label {
  font-weight: bold;
  margin-bottom: 10px;
  color: #606266;
}

.section-content {
  line-height: 1.8;
  color: #606266;
  white-space: pre-wrap;
}

.work-experience {
  margin-top: 15px;
}

.action-buttons {
  text-align: center;
  margin: 30px 0;
}

.action-buttons .el-button {
  margin: 0 10px;
}
</style>