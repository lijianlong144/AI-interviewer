<template>
  <div class="resume-list-container">
    <div class="page-header">
      <h1 class="page-title">我的简历库</h1>
      <el-button type="primary" class="create-btn" @click="createResume">
        <el-icon><Plus /></el-icon>
        <span>创建新简历</span>
      </el-button>
    </div>

    <div v-loading="loading" class="resume-content">
      <!-- 空状态 -->
      <div v-if="!loading && resumeList.length === 0" class="empty-state">
        <div class="empty-illustration">
          <el-icon><Document /></el-icon>
        </div>
        <h3>您还没有创建任何简历</h3>
        <p>创建一份专业的简历，展示您的技能和经验</p>
        <el-button type="primary" class="create-empty-btn" @click="createResume">开始创建</el-button>
      </div>

      <!-- 简历列表 -->
      <el-row :gutter="24" v-else>
        <el-col :xs="24" :sm="12" :md="8" :lg="8" v-for="resume in resumeList" :key="resume.id">
          <div class="resume-card">
            <div class="resume-header">
              <div class="resume-name">{{ resume.resumeName || '未命名简历' }}</div>
              <div class="resume-tag">
                <el-tag v-if="resume.resumeType === 'ONLINE'" effect="light" class="resume-type online">在线简历</el-tag>
                <el-tag v-else effect="light" class="resume-type upload">上传简历</el-tag>
              </div>
            </div>
            
            <div class="resume-body">
              <div class="resume-info">
                <div class="info-group">
                  <div class="info-item">
                    <el-icon><User /></el-icon>
                    <span>{{ resume.realName }}</span>
                  </div>
                  <div class="info-item">
                    <el-icon><Phone /></el-icon>
                    <span>{{ resume.phone }}</span>
                  </div>
                  <div class="info-item">
                    <el-icon><Message /></el-icon>
                    <span>{{ resume.email }}</span>
                  </div>
                </div>
                <div class="info-group">
                  <div class="info-item">
                    <el-icon><OfficeBuilding /></el-icon>
                    <span>{{ resume.currentPosition || '暂无职位' }}</span>
                  </div>
                  <div class="info-item">
                    <el-icon><School /></el-icon>
                    <span>{{ resume.highestEducation || '学历未填写' }}</span>
                  </div>
                  <div class="info-item">
                    <el-icon><Calendar /></el-icon>
                    <span>工作经验: {{ resume.workYears || 0 }}年</span>
                  </div>
                </div>
              </div>
            </div>

            <div class="resume-footer">
              <div class="update-time">
                更新于 {{ formatDate(resume.updateTime) }}
              </div>
              <div class="resume-actions">
                <TableActionButtons :buttons="getActionButtons(resume)" />
              </div>
            </div>
          </div>
        </el-col>
      </el-row>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getMyResumes, deleteResume as apiDeleteResume } from '@/api/resume'
import dayjs from 'dayjs'
import { View, EditPen, Download, Delete, Document } from '@element-plus/icons-vue'
import TableActionButtons from '@/components/TableActionButtons.vue'

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

// 获取操作按钮配置
const getActionButtons = (resume) => {
  const buttons = [
    {
      text: '预览',
      type: 'primary',
      icon: View,
      onClick: () => previewResume(resume)
    }
  ]
  
  if (resume.resumeType === 'ONLINE') {
    buttons.push({
      text: '编辑',
      type: 'warning',
      icon: EditPen,
      onClick: () => editResume(resume)
    })
  }
  
  if (resume.resumeType === 'UPLOAD' && resume.resumeUrl) {
    buttons.push({
      text: '下载',
      type: 'success',
      icon: Download,
      onClick: () => downloadResume(resume)
    })
  }
  
  buttons.push({
    text: '删除',
    type: 'danger',
    icon: Delete,
    onClick: () => deleteResume(resume)
  })
  
  return buttons
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
  padding: 16px 0;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 32px;
}

.page-title {
  font-size: 28px;
  font-weight: 700;
  color: var(--text-primary);
  margin: 0;
}

.create-btn {
  height: 44px;
  padding: 0 24px;
  border-radius: 8px;
  font-weight: 500;
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 15px;
  box-shadow: 0 4px 12px rgba(37, 99, 235, 0.2);
  transition: transform 0.2s ease;
}

.create-btn:hover {
  transform: translateY(-2px);
}

/* 空状态 */
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 80px 20px;
  background-color: rgba(37, 99, 235, 0.02);
  border-radius: 16px;
  text-align: center;
}

.empty-illustration {
  font-size: 64px;
  color: var(--primary-color);
  margin-bottom: 20px;
  opacity: 0.7;
}

.empty-state h3 {
  font-size: 20px;
  font-weight: 600;
  margin-bottom: 8px;
  color: var(--text-primary);
}

.empty-state p {
  font-size: 16px;
  color: var(--text-secondary);
  margin-bottom: 24px;
}

.create-empty-btn {
  height: 44px;
  padding: 0 32px;
  font-size: 16px;
  font-weight: 500;
}

/* 简历卡片 */
.resume-content {
  min-height: 400px;
}

.resume-card {
  background-color: white;
  border-radius: 16px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.05);
  margin-bottom: 24px;
  overflow: hidden;
  transition: all 0.3s ease;
  display: flex;
  flex-direction: column;
  height: 100%;
}

.resume-card:hover {
  transform: translateY(-8px);
  box-shadow: 0 12px 24px rgba(0, 0, 0, 0.08);
}

.resume-header {
  padding: 24px;
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  border-bottom: 1px solid rgba(0, 0, 0, 0.06);
}

.resume-name {
  font-size: 18px;
  font-weight: 700;
  color: var(--text-primary);
  line-height: 1.3;
  max-width: 70%;
  word-break: break-word;
}

.resume-tag {
  flex-shrink: 0;
}

.resume-type {
  border-radius: 4px;
  font-weight: 500;
  font-size: 12px;
}

.resume-type.online {
  color: var(--success-color);
  background-color: rgba(16, 185, 129, 0.1);
  border-color: rgba(16, 185, 129, 0.2);
}

.resume-type.upload {
  color: var(--info-color);
  background-color: rgba(99, 102, 241, 0.1);
  border-color: rgba(99, 102, 241, 0.2);
}

.resume-body {
  padding: 20px 24px;
  flex-grow: 1;
}

.resume-info {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.info-group {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.info-item {
  display: flex;
  align-items: center;
  gap: 12px;
  color: var(--text-secondary);
  font-size: 14px;
}

.info-item .el-icon {
  color: var(--primary-color);
  opacity: 0.7;
  font-size: 16px;
}

.resume-footer {
  padding: 16px 24px;
  border-top: 1px solid rgba(0, 0, 0, 0.06);
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.update-time {
  font-size: 13px;
  color: var(--text-secondary);
}

.resume-actions {
  display: flex;
  justify-content: flex-end;
}

.action-button-group {
  display: flex;
  gap: 12px;
}

.action-btn {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  border: none;
  cursor: pointer;
  transition: all 0.2s ease;
  color: white;
}

.preview-btn {
  background-color: #5e81f4;
}

.preview-btn:hover {
  background-color: #4a6eea;
}

.edit-btn {
  background-color: #ffa41b;
}

.edit-btn:hover {
  background-color: #f59506;
}

.download-btn {
  background-color: #10b981;
}

.download-btn:hover {
  background-color: #0b9e6e;
}

.delete-btn {
  background-color: #ef4444;
}

.delete-btn:hover {
  background-color: #dc2626;
}

@media (max-width: 768px) {
  .page-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 16px;
  }
  
  .resume-header {
    padding: 16px;
  }
  
  .resume-body {
    padding: 16px;
  }
  
  .resume-footer {
    flex-direction: column;
    gap: 12px;
    align-items: flex-start;
  }
  
  .resume-actions {
    width: 100%;
    justify-content: flex-start;
  }
}
</style>