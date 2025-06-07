<template>
  <div class="resume-list-container">
    <el-row :gutter="20">
      <el-col :span="24">
        <div class="page-header">
          <h2>我的简历</h2>
          <el-button type="primary" @click="createResume">
            <el-icon><Plus /></el-icon>
            创建新简历
          </el-button>
        </div>
      </el-col>
    </el-row>

    <div v-loading="loading" class="resume-content">
      <el-row :gutter="20">
        <el-col :span="8" v-for="resume in resumeList" :key="resume.id">
          <el-card class="resume-card" shadow="hover">
            <template #header>
              <div class="card-header">
                <span class="resume-name">{{ resume.resumeName || '未命名简历' }}</span>
                <el-tag v-if="resume.resumeType === 'ONLINE'" type="success" size="small">在线简历</el-tag>
                <el-tag v-else type="info" size="small">上传简历</el-tag>
              </div>
            </template>

            <div class="resume-info">
              <div class="info-row">
                <el-icon><User /></el-icon>
                <span>{{ resume.realName }}</span>
              </div>
              <div class="info-row">
                <el-icon><Phone /></el-icon>
                <span>{{ resume.phone }}</span>
              </div>
              <div class="info-row">
                <el-icon><Message /></el-icon>
                <span>{{ resume.email }}</span>
              </div>
              <div class="info-row">
                <el-icon><OfficeBuilding /></el-icon>
                <span>{{ resume.currentPosition || '暂无职位' }}</span>
              </div>
              <div class="info-row">
                <el-icon><School /></el-icon>
                <span>{{ resume.highestEducation || '学历未填写' }}</span>
              </div>
              <div class="info-row">
                <el-icon><Calendar /></el-icon>
                <span>工作经验: {{ resume.workYears || 0 }}年</span>
              </div>
            </div>

            <div class="resume-meta">
              <div class="update-time">
                更新于: {{ formatDate(resume.updateTime) }}
              </div>
            </div>

            <div class="resume-actions">
              <el-button 
                v-if="resume.resumeType === 'ONLINE'" 
                type="primary" 
                link 
                @click="editResume(resume)"
              >
                编辑
              </el-button>
              <el-button 
                v-if="resume.resumeType === 'UPLOAD' && resume.resumeUrl"
                type="primary" 
                link 
                @click="downloadResume(resume)"
              >
                下载
              </el-button>
              <el-button type="primary" link @click="previewResume(resume)">
                预览
              </el-button>
              <el-button type="danger" link @click="deleteResume(resume)">
                删除
              </el-button>
            </div>
          </el-card>
        </el-col>
      </el-row>

      <!-- 空状态 -->
      <el-empty v-if="!loading && resumeList.length === 0" description="您还没有创建简历">
        <el-button type="primary" @click="createResume">创建第一份简历</el-button>
      </el-empty>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getMyResumes, deleteResume as apiDeleteResume } from '@/api/resume'
import dayjs from 'dayjs'

const router = useRouter()

// 简历列表
const resumeList = ref([])
const loading = ref(false)

// 格式化日期
const formatDate = (date) => {
  return dayjs(date).format('YYYY-MM-DD HH:mm')
}

// 创建简历
const createResume = () => {
  router.push('/candidate/resume/create')
}

// 编辑简历
const editResume = (resume) => {
  router.push(`/candidate/resume/edit/${resume.id}`)
}

// 预览简历
const previewResume = (resume) => {
  router.push(`/candidate/resume/preview/${resume.id}`)
}

// 下载简历
const downloadResume = (resume) => {
  if (resume.resumeUrl) {
    window.open(resume.resumeUrl, '_blank')
  } else {
    ElMessage.warning('简历文件不存在')
  }
}

// 删除简历
const deleteResume = (resume) => {
  ElMessageBox.confirm(
    `确定要删除简历"${resume.resumeName || '未命名简历'}"吗？`,
    '删除确认',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      await apiDeleteResume(resume.id)
      ElMessage.success('删除成功')
      loadResumeList()
    } catch (error) {
      console.error('删除简历失败:', error)
      ElMessage.error('删除简历失败')
    }
  })
}

// 加载简历列表
const loadResumeList = async () => {
  try {
    loading.value = true
    const res = await getMyResumes()
    resumeList.value = res.data || []
  } catch (error) {
    console.error('获取简历列表失败:', error)
    ElMessage.error('获取简历列表失败')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadResumeList()
})
</script>

<style scoped>
.resume-list-container {
  padding: 20px 0;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.resume-content {
  min-height: 400px;
}

.resume-card {
  margin-bottom: 20px;
  transition: all 0.3s;
}

.resume-card:hover {
  transform: translateY(-5px);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.resume-name {
  font-size: 16px;
  font-weight: bold;
}

.resume-info {
  margin-bottom: 15px;
}

.info-row {
  display: flex;
  align-items: center;
  margin-bottom: 10px;
  color: #606266;
}

.info-row .el-icon {
  margin-right: 8px;
  color: #909399;
}

.resume-meta {
  padding: 10px 0;
  border-top: 1px solid #EBEEF5;
  border-bottom: 1px solid #EBEEF5;
  margin-bottom: 15px;
}

.update-time {
  font-size: 12px;
  color: #909399;
}

.resume-actions {
  display: flex;
  justify-content: space-around;
}
</style>