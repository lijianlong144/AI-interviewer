<template>
  <div class="resume-preview-container">
    <el-row :gutter="20">
      <el-col :span="24">
        <el-page-header @back="goBack">
          <template #content>
            <div class="page-title">预览简历</div>
          </template>
        </el-page-header>
      </el-col>
    </el-row>

    <div class="preview-content" v-loading="loading">
      <el-card class="resume-card">
        <div class="resume-header">
          <h1>{{ resumeData.resumeName }}</h1>
          <div class="actions">
            <el-button type="primary" @click="handleEdit">编辑简历</el-button>
            <el-button type="success" @click="handlePrint">打印简历</el-button>
          </div>
        </div>

        <!-- 基本信息 -->
        <div class="section">
          <h2 class="section-title">基本信息</h2>
          <el-row :gutter="20">
            <el-col :span="8">
              <div class="info-item">
                <span class="label">真实姓名：</span>
                <span class="value">{{ resumeData.realName }}</span>
              </div>
            </el-col>
            <el-col :span="8">
              <div class="info-item">
                <span class="label">性别：</span>
                <span class="value">{{ resumeData.gender }}</span>
              </div>
            </el-col>
            <el-col :span="8">
              <div class="info-item">
                <span class="label">出生日期：</span>
                <span class="value">{{ resumeData.birthDate }}</span>
              </div>
            </el-col>
          </el-row>
          <el-row :gutter="20">
            <el-col :span="8">
              <div class="info-item">
                <span class="label">手机号码：</span>
                <span class="value">{{ resumeData.phone }}</span>
              </div>
            </el-col>
            <el-col :span="8">
              <div class="info-item">
                <span class="label">电子邮箱：</span>
                <span class="value">{{ resumeData.email }}</span>
              </div>
            </el-col>
            <el-col :span="8">
              <div class="info-item">
                <span class="label">现居住地：</span>
                <span class="value">{{ resumeData.currentLocation }}</span>
              </div>
            </el-col>
          </el-row>
        </div>

        <!-- 教育背景 -->
        <div class="section">
          <h2 class="section-title">教育背景</h2>
          <el-row :gutter="20">
            <el-col :span="8">
              <div class="info-item">
                <span class="label">最高学历：</span>
                <span class="value">{{ resumeData.highestEducation }}</span>
              </div>
            </el-col>
            <el-col :span="8">
              <div class="info-item">
                <span class="label">毕业院校：</span>
                <span class="value">{{ resumeData.graduationSchool }}</span>
              </div>
            </el-col>
            <el-col :span="8">
              <div class="info-item">
                <span class="label">毕业年份：</span>
                <span class="value">{{ resumeData.graduationYear }}</span>
              </div>
            </el-col>
          </el-row>
          <el-row :gutter="20">
            <el-col :span="24">
              <div class="info-item">
                <span class="label">专业：</span>
                <span class="value">{{ resumeData.major }}</span>
              </div>
            </el-col>
          </el-row>
        </div>

        <!-- 工作经历 -->
        <div class="section">
          <h2 class="section-title">工作经历</h2>
          <el-row :gutter="20">
            <el-col :span="8">
              <div class="info-item">
                <span class="label">工作年限：</span>
                <span class="value">{{ resumeData.workYears }} 年</span>
              </div>
            </el-col>
            <el-col :span="8">
              <div class="info-item">
                <span class="label">当前公司：</span>
                <span class="value">{{ resumeData.currentCompany }}</span>
              </div>
            </el-col>
            <el-col :span="8">
              <div class="info-item">
                <span class="label">当前职位：</span>
                <span class="value">{{ resumeData.currentPosition }}</span>
              </div>
            </el-col>
          </el-row>
          <div class="info-item">
            <span class="label">工作经历：</span>
            <div class="text-content">{{ resumeData.workExperience }}</div>
          </div>
        </div>

        <!-- 技能特长 -->
        <div class="section">
          <h2 class="section-title">技能特长</h2>
          <div class="info-item">
            <span class="label">技能特长：</span>
            <div class="text-content">{{ resumeData.skills }}</div>
          </div>
          <div class="info-item">
            <span class="label">自我评价：</span>
            <div class="text-content">{{ resumeData.selfEvaluation }}</div>
          </div>
        </div>

        <!-- 求职意向 -->
        <div class="section">
          <h2 class="section-title">求职意向</h2>
          <el-row :gutter="20">
            <el-col :span="8">
              <div class="info-item">
                <span class="label">期望地点：</span>
                <span class="value">{{ resumeData.expectedLocation }}</span>
              </div>
            </el-col>
            <el-col :span="8">
              <div class="info-item">
                <span class="label">期望薪资：</span>
                <span class="value" v-if="resumeData.expectedSalaryMin && resumeData.expectedSalaryMax">
                  {{ resumeData.expectedSalaryMin }} - {{ resumeData.expectedSalaryMax }} K
                </span>
              </div>
            </el-col>
            <el-col :span="8">
              <div class="info-item">
                <span class="label">到岗时间：</span>
                <span class="value">{{ resumeData.availableDate }}</span>
              </div>
            </el-col>
          </el-row>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getResumeDetail } from '@/api/resume'

const route = useRoute()
const router = useRouter()
const resumeId = route.params.id

const loading = ref(false)
const resumeData = ref({})

// 返回
const goBack = () => {
  router.push('/candidate/resume')
}

// 编辑简历
const handleEdit = () => {
  router.push(`/candidate/resume/edit/${resumeId}`)
}

// 打印简历
const handlePrint = () => {
  window.print()
}

// 加载简历详情
const loadResumeDetail = async () => {
  try {
    loading.value = true
    const res = await getResumeDetail(resumeId)
    
    if (res.data) {
      resumeData.value = res.data
    } else {
      ElMessage.warning('简历数据不存在')
      router.push('/candidate/resume')
    }
  } catch (error) {
    console.error('获取简历详情失败:', error)
    ElMessage.error('获取简历详情失败')
    router.push('/candidate/resume')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadResumeDetail()
})
</script>

<style scoped>
.resume-preview-container {
  padding: 20px 0;
}

.page-title {
  font-size: 18px;
  font-weight: bold;
}

.preview-content {
  margin-top: 20px;
  max-width: 1000px;
  margin-left: auto;
  margin-right: auto;
}

.resume-card {
  padding: 20px;
}

.resume-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30px;
  border-bottom: 1px solid #eee;
  padding-bottom: 20px;
}

.resume-header h1 {
  margin: 0;
  font-size: 24px;
  color: #409EFF;
}

.section {
  margin-bottom: 30px;
}

.section-title {
  font-size: 18px;
  color: #303133;
  margin-bottom: 15px;
  border-left: 4px solid #409EFF;
  padding-left: 10px;
}

.info-item {
  margin-bottom: 15px;
}

.label {
  font-weight: bold;
  color: #606266;
}

.value {
  color: #303133;
}

.text-content {
  margin-top: 10px;
  white-space: pre-line;
  line-height: 1.6;
  color: #303133;
  background-color: #f5f7fa;
  padding: 15px;
  border-radius: 4px;
}

@media print {
  .actions {
    display: none;
  }
  
  .el-page-header {
    display: none;
  }
}
</style> 