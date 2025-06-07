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
            <div class="salary">{{ formatSalary(positionInfo.salaryMin, positionInfo.salaryMax) }}</div>
          </div>
        </div>

        <el-divider />

        <el-row :gutter="20">
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
        </el-row>

        <el-row :gutter="20" style="margin-top: 20px;">
          <el-col :span="6">
            <div class="info-item">
              <div class="info-label">招聘人数</div>
              <div class="info-value">{{ positionInfo.headcount || 1 }} 人</div>
            </div>
          </el-col>
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

      <!-- 申请按钮 -->
      <div class="action-buttons" v-if="positionInfo.status === 1">
        <el-button 
          v-if="!hasApplied" 
          type="primary" 
          size="large"
          @click="applyPosition"
          :loading="applyLoading"
        >
          申请职位
        </el-button>
        <el-button 
          v-else 
          type="success" 
          size="large"
          disabled
        >
          已申请
        </el-button>
      </div>
    </div>

    <!-- 选择简历对话框 -->
    <el-dialog
      v-model="resumeDialogVisible"
      title="选择简历"
      width="600px"
    >
      <div v-loading="resumeLoading">
        <el-radio-group v-model="selectedResumeId" class="resume-list">
          <el-card 
            v-for="resume in resumeList" 
            :key="resume.id"
            class="resume-item"
            :class="{ 'selected': selectedResumeId === resume.id }"
            @click="selectedResumeId = resume.id"
          >
            <el-radio :value="resume.id">
              <div class="resume-info">
                <div class="resume-name">{{ resume.resumeName || '未命名简历' }}</div>
                <div class="resume-meta">
                  <span>{{ resume.realName }}</span>
                  <span>{{ resume.currentPosition || '暂无职位' }}</span>
                  <span>更新于: {{ formatDate(resume.updateTime) }}</span>
                </div>
              </div>
            </el-radio>
          </el-card>
        </el-radio-group>

        <el-empty v-if="resumeList.length === 0" description="您还没有创建简历">
          <el-button type="primary" @click="goCreateResume">创建简历</el-button>
        </el-empty>
      </div>

      <template #footer v-if="resumeList.length > 0">
        <span class="dialog-footer">
          <el-button @click="resumeDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="confirmApply" :disabled="!selectedResumeId">
            确认申请
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
import { getMyResumes } from '@/api/resume'
import { createApplication, checkApplication } from '@/api/application'
import dayjs from 'dayjs'

const route = useRoute()
const router = useRouter()
const positionId = route.params.id

// 职位信息
const positionInfo = ref({})
const loading = ref(false)

// 申请相关
const hasApplied = ref(false)
const applyLoading = ref(false)
const resumeDialogVisible = ref(false)
const resumeList = ref([])
const selectedResumeId = ref(null)
const resumeLoading = ref(false)

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

// 格式化薪资
const formatSalary = (min, max) => {
  if (!min && !max) return '面议'
  if (min && max) {
    return `￥${min/1000}k-${max/1000}k/月`
  }
  return min ? `￥${min/1000}k起` : `￥${max/1000}k以内`
}

// 格式化日期
const formatDate = (date) => {
  if (!date) return '-'
  return dayjs(date).format('YYYY-MM-DD')
}

// 返回
const goBack = () => {
  router.push('/candidate/positions')
}

// 申请职位
const applyPosition = async () => {
  // 先获取简历列表
  try {
    resumeLoading.value = true
    const res = await getMyResumes()
    resumeList.value = res.data || []
    
    if (resumeList.value.length === 0) {
      ElMessageBox.confirm(
        '您还没有创建简历，是否前往创建？',
        '提示',
        {
          confirmButtonText: '去创建',
          cancelButtonText: '取消',
          type: 'info'
        }
      ).then(() => {
        router.push('/candidate/resume/create')
      })
    } else {
      resumeDialogVisible.value = true
    }
  } catch (error) {
    console.error('获取简历列表失败:', error)
    ElMessage.error('获取简历列表失败')
  } finally {
    resumeLoading.value = false
  }
}

// 去创建简历
const goCreateResume = () => {
  resumeDialogVisible.value = false
  router.push('/candidate/resume/create')
}

// 确认申请
const confirmApply = async () => {
  if (!selectedResumeId.value) {
    ElMessage.warning('请选择一份简历')
    return
  }

  try {
    applyLoading.value = true
    
    await createApplication({
      positionId: Number(positionId),
      resumeId: selectedResumeId.value
    })
    
    ElMessage.success('申请成功')
    resumeDialogVisible.value = false
    hasApplied.value = true
  } catch (error) {
    console.error('申请失败:', error)
    ElMessage.error(error.message || '申请失败')
  } finally {
    applyLoading.value = false
  }
}

// 检查是否已申请
const checkHasApplied = async () => {
  try {
    const res = await checkApplication({ positionId })
    hasApplied.value = res.data || false
  } catch (error) {
    console.error('检查申请状态失败:', error)
  }
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

onMounted(() => {
  loadPositionDetail()
  checkHasApplied()
})
</script>

<style scoped>
.position-detail-container {
  padding: 20px 0;
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
  font-size: 24px;
  margin: 0 0 15px 0;
}

.position-tags {
  display: flex;
  gap: 10px;
}

.salary {
  font-size: 24px;
  color: #f56c6c;
  font-weight: bold;
}

.info-item {
  text-align: center;
}

.info-label {
  color: #909399;
  font-size: 14px;
  margin-bottom: 5px;
}

.info-value {
  color: #303133;
  font-size: 16px;
  font-weight: 500;
}

.desc-card,
.requirements-card,
.benefits-card {
  margin-bottom: 20px;
}

.card-header {
  font-size: 16px;
  font-weight: bold;
}

.desc-content,
.requirements-content,
.benefits-content {
  line-height: 1.8;
  color: #606266;
  white-space: pre-wrap;
}

.action-buttons {
  text-align: center;
  margin: 30px 0;
}

.resume-list {
  width: 100%;
}

.resume-item {
  margin-bottom: 10px;
  cursor: pointer;
  transition: all 0.3s;
}

.resume-item:hover {
  border-color: #409EFF;
}

.resume-item.selected {
  border-color: #409EFF;
  background-color: #ecf5ff;
}

.resume-info {
  margin-left: 10px;
}

.resume-name {
  font-size: 16px;
  font-weight: bold;
  margin-bottom: 5px;
}

.resume-meta {
  font-size: 14px;
  color: #909399;
}

.resume-meta span {
  margin-right: 15px;
}
</style>