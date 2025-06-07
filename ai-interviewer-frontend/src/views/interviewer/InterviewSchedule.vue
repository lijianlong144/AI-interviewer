<template>
  <div class="interview-schedule-container">
    <div class="page-header">
      <h2>面试安排</h2>
      <div class="header-info">
        <el-tag type="success">待安排: {{ pendingCount }}</el-tag>
        <el-tag type="primary">已安排: {{ scheduledCount }}</el-tag>
      </div>
    </div>

    <!-- 待安排的申请列表 -->
    <el-card class="application-card">
      <template #header>
        <div class="card-header">
          <span>待安排面试的申请</span>
        </div>
      </template>

      <el-table
          :data="applicationList"
          v-loading="loading"
          style="width: 100%"
      >
        <el-table-column prop="applicationNo" label="申请编号" width="150" />
        <el-table-column label="候选人" width="120">
          <template #default="scope">
            {{ scope.row.candidateName || '-' }}
          </template>
        </el-table-column>
        <el-table-column label="申请职位" min-width="200">
          <template #default="scope">
            {{ scope.row.positionTitle || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="applyTime" label="申请时间" width="180">
          <template #default="scope">
            {{ formatDateTime(scope.row.applyTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="hrComment" label="HR评语" min-width="200" show-overflow-tooltip />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="scope">
            <el-button
                type="primary"
                link
                @click="viewResume(scope.row)"
            >
              查看简历
            </el-button>
            <el-button
                type="success"
                link
                @click="scheduleInterview(scope.row)"
            >
              安排面试
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-container">
        <el-pagination
            v-model:current-page="currentPage"
            v-model:page-size="pageSize"
            :page-sizes="[10, 20, 50]"
            :total="total"
            layout="total, sizes, prev, pager, next, jumper"
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <!-- 查看简历对话框 -->
    <el-dialog
        v-model="resumeDialogVisible"
        title="候选人简历"
        width="800px"
    >
      <div v-loading="resumeLoading" class="resume-preview">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="姓名">{{ currentResume.realName }}</el-descriptions-item>
          <el-descriptions-item label="性别">{{ currentResume.gender }}</el-descriptions-item>
          <el-descriptions-item label="手机">{{ currentResume.phone }}</el-descriptions-item>
          <el-descriptions-item label="邮箱">{{ currentResume.email }}</el-descriptions-item>
          <el-descriptions-item label="学历">{{ currentResume.highestEducation }}</el-descriptions-item>
          <el-descriptions-item label="工作年限">{{ currentResume.workYears }}年</el-descriptions-item>
          <el-descriptions-item label="当前公司" :span="2">{{ currentResume.currentCompany }}</el-descriptions-item>
          <el-descriptions-item label="当前职位" :span="2">{{ currentResume.currentPosition }}</el-descriptions-item>
        </el-descriptions>

        <div class="resume-section" v-if="currentResume.workExperience">
          <h4>工作经历</h4>
          <div class="section-content">{{ currentResume.workExperience }}</div>
        </div>

        <div class="resume-section" v-if="currentResume.skills">
          <h4>技能特长</h4>
          <div class="section-content">{{ currentResume.skills }}</div>
        </div>
      </div>
    </el-dialog>

    <!-- 安排面试对话框 -->
    <el-dialog
        v-model="scheduleDialogVisible"
        title="安排面试"
        width="600px"
    >
      <el-form
          ref="scheduleFormRef"
          :model="scheduleForm"
          :rules="scheduleRules"
          label-width="100px"
      >
        <el-form-item label="候选人">
          <el-input v-model="currentApplication.candidateName" disabled />
        </el-form-item>

        <el-form-item label="应聘职位">
          <el-input v-model="currentApplication.positionTitle" disabled />
        </el-form-item>

        <el-form-item label="面试时间" prop="scheduledTime">
          <el-date-picker
              v-model="scheduleForm.scheduledTime"
              type="datetime"
              placeholder="选择日期时间"
              format="YYYY-MM-DD HH:mm"
              value-format="YYYY-MM-DD HH:mm:00"
              :disabled-date="disabledDate"
              style="width: 100%"
          />
        </el-form-item>

        <el-form-item label="面试时长" prop="duration">
          <el-select v-model="scheduleForm.duration" placeholder="请选择面试时长">
            <el-option label="30分钟" :value="30" />
            <el-option label="45分钟" :value="45" />
            <el-option label="60分钟" :value="60" />
            <el-option label="90分钟" :value="90" />
            <el-option label="120分钟" :value="120" />
          </el-select>
        </el-form-item>

        <el-form-item label="选择题目">
          <el-button type="primary" @click="selectQuestions">
            选择面试题目 (已选: {{ selectedQuestions.length }}题)
          </el-button>
        </el-form-item>

        <el-form-item label="面试说明" prop="description">
          <el-input
              v-model="scheduleForm.description"
              type="textarea"
              :rows="3"
              placeholder="请输入面试相关说明，如面试形式、注意事项等"
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <span class="dialog-footer">
          <el-button @click="scheduleDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="confirmSchedule" :loading="scheduleLoading">
            确认安排
          </el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 选择题目对话框 -->
    <el-dialog
        v-model="questionDialogVisible"
        title="选择面试题目"
        width="900px"
    >
      <div class="question-selector">
        <el-form :inline="true" class="search-form">
          <el-form-item label="题目类型">
            <el-select v-model="questionFilter.type" placeholder="全部" clearable>
              <el-option label="技术题" value="TECHNICAL" />
              <el-option label="行为题" value="BEHAVIORAL" />
              <el-option label="情境题" value="SITUATIONAL" />
            </el-select>
          </el-form-item>
          <el-form-item label="难度">
            <el-select v-model="questionFilter.difficulty" placeholder="全部" clearable>
              <el-option label="1星" :value="1" />
              <el-option label="2星" :value="2" />
              <el-option label="3星" :value="3" />
              <el-option label="4星" :value="4" />
              <el-option label="5星" :value="5" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="loadQuestions">搜索</el-button>
            <el-button type="success" @click="getRecommendQuestions">智能推荐</el-button>
          </el-form-item>
        </el-form>

        <el-table
            :data="questionList"
            v-loading="questionLoading"
            @selection-change="handleQuestionSelectionChange"
            style="width: 100%"
        >
          <el-table-column type="selection" width="55" />
          <el-table-column prop="title" label="题目标题" min-width="200" show-overflow-tooltip />
          <el-table-column prop="type" label="类型" width="100">
            <template #default="scope">
              {{ getQuestionTypeText(scope.row.type) }}
            </template>
          </el-table-column>
          <el-table-column prop="difficulty" label="难度" width="100">
            <template #default="scope">
              <el-rate v-model="scope.row.difficulty" disabled />
            </template>
          </el-table-column>
          <el-table-column prop="useCount" label="使用次数" width="100" />
        </el-table>
      </div>

      <template #footer>
        <span class="dialog-footer">
          <el-button @click="questionDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="confirmQuestionSelection">
            确认选择 ({{ tempSelectedQuestions.length }}题)
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getApplicationPage } from '@/api/application'
import { getResumeDetail } from '@/api/resume'
import { scheduleInterview as apiScheduleInterview, sendInterviewInvitation } from '@/api/interview-process'
import { getQuestionPage, getRecommendQuestions as apiGetRecommendQuestions } from '@/api/question'
import dayjs from 'dayjs'

const router = useRouter()

// 统计数据
const pendingCount = ref(0)
const scheduledCount = ref(0)

// 分页参数
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

// 列表数据
const applicationList = ref([])
const loading = ref(false)

// 简历对话框
const resumeDialogVisible = ref(false)
const resumeLoading = ref(false)
const currentResume = ref({})

// 安排面试对话框
const scheduleDialogVisible = ref(false)
const scheduleLoading = ref(false)
const scheduleFormRef = ref()
const currentApplication = ref({})
const scheduleForm = reactive({
  scheduledTime: '',
  duration: 60,
  description: '',
  questionIds: []
})

const scheduleRules = {
  scheduledTime: [
    { required: true, message: '请选择面试时间', trigger: 'change' }
  ],
  duration: [
    { required: true, message: '请选择面试时长', trigger: 'change' }
  ]
}

// 题目选择
const questionDialogVisible = ref(false)
const questionLoading = ref(false)
const questionList = ref([])
const selectedQuestions = ref([])
const tempSelectedQuestions = ref([])
const questionFilter = reactive({
  type: '',
  difficulty: null
})

// 格式化日期时间
const formatDateTime = (datetime) => {
  return dayjs(datetime).format('YYYY-MM-DD HH:mm')
}

// 禁用过去的日期
const disabledDate = (date) => {
  return date.valueOf() < Date.now() - 24 * 60 * 60 * 1000
}

// 获取题目类型文本
const getQuestionTypeText = (type) => {
  const textMap = {
    'TECHNICAL': '技术题',
    'BEHAVIORAL': '行为题',
    'SITUATIONAL': '情境题'
  }
  return textMap[type] || type
}

// 查看简历
const viewResume = async (application) => {
  try {
    resumeLoading.value = true
    const res = await getResumeDetail(application.resumeId)

    if (res.data) {
      currentResume.value = res.data
      resumeDialogVisible.value = true
    }
  } catch (error) {
    console.error('获取简历失败:', error)
    ElMessage.error('获取简历失败')
  } finally {
    resumeLoading.value = false
  }
}

// 安排面试
const scheduleInterview = (application) => {
  currentApplication.value = application
  scheduleForm.scheduledTime = ''
  scheduleForm.duration = 60
  scheduleForm.description = ''
  scheduleForm.questionIds = []
  selectedQuestions.value = []
  scheduleDialogVisible.value = true
}

// 选择题目
const selectQuestions = () => {
  questionFilter.type = ''
  questionFilter.difficulty = null
  loadQuestions()
  questionDialogVisible.value = true
}

// 加载题目
const loadQuestions = async () => {
  try {
    questionLoading.value = true

    const params = {
      current: 1,
      size: 50,
      position: currentApplication.value.positionTitle
    }

    if (questionFilter.type) {
      params.type = questionFilter.type
    }
    if (questionFilter.difficulty) {
      params.difficulty = questionFilter.difficulty
    }

    const res = await getQuestionPage(params)

    if (res.data) {
      questionList.value = res.data.records || []
    }
  } catch (error) {
    console.error('获取题目列表失败:', error)
    ElMessage.error('获取题目列表失败')
  } finally {
    questionLoading.value = false
  }
}

// 获取推荐题目
const getRecommendQuestions = async () => {
  try {
    questionLoading.value = true

    const res = await apiGetRecommendQuestions(currentApplication.value.positionTitle, 10)

    if (res.data) {
      questionList.value = res.data || []
      ElMessage.success('已为您推荐相关题目')
    }
  } catch (error) {
    console.error('获取推荐题目失败:', error)
    ElMessage.error('获取推荐题目失败')
  } finally {
    questionLoading.value = false
  }
}

// 题目选择变化
const handleQuestionSelectionChange = (selection) => {
  tempSelectedQuestions.value = selection
}

// 确认题目选择
const confirmQuestionSelection = () => {
  selectedQuestions.value = [...tempSelectedQuestions.value]
  scheduleForm.questionIds = selectedQuestions.value.map(q => q.id)
  questionDialogVisible.value = false
}

// 确认安排面试
const confirmSchedule = async () => {
  try {
    await scheduleFormRef.value.validate()

    if (selectedQuestions.value.length === 0) {
      ElMessage.warning('请选择面试题目')
      return
    }

    scheduleLoading.value = true

    // 调用安排面试接口
    const res = await apiScheduleInterview(
        currentApplication.value.id,
        1, // 面试官ID，实际应该从当前登录用户获取
        scheduleForm.scheduledTime,
        scheduleForm.duration
    )

    if (res.data && res.data.id) {
      // 发送面试邀请邮件
      await sendInterviewInvitation(res.data.id)

      ElMessage.success('面试安排成功，已发送邀请邮件')
      scheduleDialogVisible.value = false

      // 刷新列表
      loadApplicationList()
    }
  } catch (error) {
    console.error('安排面试失败:', error)
    if (error !== false) {
      ElMessage.error(error.message || '安排面试失败')
    }
  } finally {
    scheduleLoading.value = false
  }
}

// 分页大小改变
const handleSizeChange = (val) => {
  pageSize.value = val
  currentPage.value = 1
  loadApplicationList()
}

// 页码改变
const handleCurrentChange = (val) => {
  currentPage.value = val
  loadApplicationList()
}

// 加载申请列表
const loadApplicationList = async () => {
  try {
    loading.value = true

    const params = {
      current: currentPage.value,
      size: pageSize.value,
      status: 'PASSED' // 只查询初筛通过的申请
    }

    const res = await getApplicationPage(params)

    if (res.data) {
      applicationList.value = res.data.records || []
      total.value = res.data.total || 0
      pendingCount.value = res.data.total || 0
    }
  } catch (error) {
    console.error('获取申请列表失败:', error)
    ElMessage.error('获取申请列表失败')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadApplicationList()
})
</script>

<style scoped>
.interview-schedule-container {
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.header-info {
  display: flex;
  gap: 10px;
}

.application-card {
  margin-bottom: 20px;
}

.card-header {
  font-size: 16px;
  font-weight: bold;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.resume-preview {
  max-height: 600px;
  overflow-y: auto;
}

.resume-section {
  margin-top: 20px;
  padding-top: 20px;
  border-top: 1px solid #EBEEF5;
}

.resume-section h4 {
  margin-bottom: 10px;
  color: #303133;
}

.section-content {
  line-height: 1.8;
  color: #606266;
  white-space: pre-wrap;
}

.question-selector {
  max-height: 500px;
  overflow-y: auto;
}

.search-form {
  margin-bottom: 20px;
}
</style>