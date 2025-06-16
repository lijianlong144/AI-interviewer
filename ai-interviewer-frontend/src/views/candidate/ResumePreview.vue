<template>
  <div class="resume-preview-container">
    <div class="page-header">
      <div @click="goBack" class="back-button">
        <el-icon><ArrowLeft /></el-icon>
        <span>返回</span>
      </div>
      <h1 class="page-title">简历预览</h1>
      <div class="actions">
        <el-button 
          type="default" 
          class="action-btn print-btn" 
          @click="handlePrint">
          <el-icon><Printer /></el-icon>
          <span>打印</span>
        </el-button>
        <el-button 
          type="primary" 
          class="action-btn" 
          @click="handleEdit">
          <el-icon><EditPen /></el-icon>
          <span>编辑</span>
        </el-button>
      </div>
    </div>

    <div class="preview-content" :class="{ 'is-loading': loading }">
      <div v-if="loading" class="loading-overlay">
        <el-icon class="loading-icon"><Loading /></el-icon>
        <span>加载中...</span>
      </div>

      <div class="resume-paper" :class="{ 'blur': loading }">
        <div class="resume-header">
          <div class="resume-name">{{ resumeData.realName || '姓名未填写' }}</div>
          <div class="resume-title">{{ resumeData.resumeName || '未命名简历' }}</div>
        </div>

        <div class="resume-quick-info">
          <div class="quick-info-item" v-if="resumeData.phone">
            <el-icon><Phone /></el-icon>
            <span>{{ resumeData.phone }}</span>
          </div>
          <div class="quick-info-item" v-if="resumeData.email">
            <el-icon><Message /></el-icon>
            <span>{{ resumeData.email }}</span>
          </div>
          <div class="quick-info-item" v-if="resumeData.currentLocation">
            <el-icon><Location /></el-icon>
            <span>{{ resumeData.currentLocation }}</span>
          </div>
          <div class="quick-info-item" v-if="resumeData.birthDate">
            <el-icon><Calendar /></el-icon>
            <span>{{ resumeData.birthDate }}</span>
          </div>
        </div>

        <!-- 教育背景 -->
        <section class="resume-section education-section">
          <div class="section-header">
            <el-icon class="section-icon"><School /></el-icon>
            <h2 class="section-title">教育背景</h2>
          </div>
          
          <div class="section-content">
            <div class="education-info">
              <div class="education-main">
                <div v-if="resumeData.graduationSchool" class="school">{{ resumeData.graduationSchool }}</div>
                <div v-if="resumeData.major" class="major">{{ resumeData.major }}</div>
              </div>
              <div class="education-details">
                <div v-if="resumeData.highestEducation" class="degree">{{ resumeData.highestEducation }}</div>
                <div v-if="resumeData.graduationYear" class="year">{{ resumeData.graduationYear }}</div>
              </div>
            </div>
          </div>
        </section>

        <!-- 工作经历 -->
        <section class="resume-section work-section">
          <div class="section-header">
            <el-icon class="section-icon"><OfficeBuilding /></el-icon>
            <h2 class="section-title">工作经历</h2>
          </div>
          
          <div class="section-content">
            <div v-if="resumeData.currentCompany || resumeData.currentPosition" class="current-position">
              <span v-if="resumeData.currentCompany" class="company">{{ resumeData.currentCompany }}</span>
              <span v-if="resumeData.currentCompany && resumeData.currentPosition" class="separator">|</span>
              <span v-if="resumeData.currentPosition" class="position">{{ resumeData.currentPosition }}</span>
              <span v-if="resumeData.workYears" class="years">({{ resumeData.workYears }}年经验)</span>
            </div>
            
            <div v-if="resumeData.workExperience" class="experience-text">
              <p>{{ resumeData.workExperience }}</p>
            </div>
          </div>
        </section>

        <!-- 技能特长 -->
        <section class="resume-section skills-section">
          <div class="section-header">
            <el-icon class="section-icon"><StarFilled /></el-icon>
            <h2 class="section-title">技能特长</h2>
          </div>
          
          <div class="section-content">
            <div v-if="resumeData.skills" class="skills-text">
              <p>{{ resumeData.skills }}</p>
            </div>
          </div>
        </section>

        <!-- 自我评价 -->
        <section class="resume-section evaluation-section">
          <div class="section-header">
            <el-icon class="section-icon"><UserFilled /></el-icon>
            <h2 class="section-title">自我评价</h2>
          </div>
          
          <div class="section-content">
            <div v-if="resumeData.selfEvaluation" class="evaluation-text">
              <p>{{ resumeData.selfEvaluation }}</p>
            </div>
          </div>
        </section>

        <!-- 求职意向 -->
        <section class="resume-section intention-section">
          <div class="section-header">
            <el-icon class="section-icon"><Aim /></el-icon>
            <h2 class="section-title">求职意向</h2>
          </div>
          
          <div class="section-content">
            <div class="intention-items">
              <div class="intention-item" v-if="resumeData.expectedLocation">
                <span class="label">期望地点: </span>
                <span class="value">{{ resumeData.expectedLocation }}</span>
              </div>
              
              <div class="intention-item" v-if="resumeData.expectedSalaryMin && resumeData.expectedSalaryMax">
                <span class="label">期望薪资: </span>
                <span class="value">{{ resumeData.expectedSalaryMin }} - {{ resumeData.expectedSalaryMax }}K</span>
              </div>
              
              <div class="intention-item" v-if="resumeData.availableDate">
                <span class="label">到岗时间: </span>
                <span class="value">{{ resumeData.availableDate }}</span>
              </div>
            </div>
          </div>
        </section>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getResumeDetail } from '@/api/resume'
import { 
  ArrowLeft, 
  EditPen, 
  Printer, 
  Loading, 
  Phone, 
  Message, 
  Location, 
  Calendar,
  School,
  OfficeBuilding,
  StarFilled,
  UserFilled,
  Aim
} from '@element-plus/icons-vue'

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
  padding: 0;
}

.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 24px;
}

.back-button {
  display: flex;
  align-items: center;
  gap: 6px;
  cursor: pointer;
  color: var(--text-secondary);
  font-size: 14px;
  transition: color 0.2s ease;
}

.back-button:hover {
  color: var(--primary-color);
}

.page-title {
  font-size: 20px;
  font-weight: 600;
  margin: 0;
  color: var(--text-primary);
}

.actions {
  display: flex;
  gap: 12px;
}

.action-btn {
  display: flex;
  align-items: center;
  gap: 6px;
}

.print-btn {
  color: var(--text-secondary);
  border-color: var(--border-color);
}

.preview-content {
  position: relative;
}

.loading-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(255, 255, 255, 0.7);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  z-index: 10;
  font-size: 14px;
  color: var(--text-secondary);
  gap: 12px;
}

.loading-icon {
  font-size: 32px;
  color: var(--primary-color);
  animation: spin 1.5s infinite linear;
}

@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

.resume-paper {
  max-width: 800px;
  margin: 0 auto;
  background: white;
  border-radius: 12px;
  box-shadow: 0 5px 20px rgba(0, 0, 0, 0.08);
  padding: 40px;
  transition: filter 0.3s ease;
}

.resume-paper.blur {
  filter: blur(3px);
}

.resume-header {
  text-align: center;
  margin-bottom: 32px;
}

.resume-name {
  font-size: 32px;
  font-weight: 700;
  color: var(--text-primary);
  margin-bottom: 6px;
}

.resume-title {
  font-size: 16px;
  color: var(--text-secondary);
}

.resume-quick-info {
  display: flex;
  flex-wrap: wrap;
  justify-content: center;
  gap: 20px;
  margin-bottom: 36px;
  padding-bottom: 24px;
  border-bottom: 1px dashed var(--border-color);
}

.quick-info-item {
  display: flex;
  align-items: center;
  gap: 8px;
  color: var(--text-secondary);
  font-size: 14px;
}

.resume-section {
  margin-bottom: 36px;
}

.section-header {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 16px;
}

.section-icon {
  font-size: 20px;
  color: var(--primary-color);
  opacity: 0.8;
}

.section-title {
  font-size: 20px;
  font-weight: 600;
  color: var(--text-primary);
  margin: 0;
  position: relative;
}

.section-content {
  padding-left: 30px;
}

/* 教育背景 */
.education-info {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
}

.education-main {
  flex: 1;
}

.school {
  font-size: 16px;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 4px;
}

.major {
  font-size: 14px;
  color: var(--text-secondary);
}

.education-details {
  text-align: right;
}

.degree {
  font-size: 14px;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 4px;
}

.year {
  font-size: 14px;
  color: var(--text-secondary);
}

/* 工作经历 */
.current-position {
  font-size: 16px;
  margin-bottom: 12px;
}

.company {
  font-weight: 600;
  color: var(--text-primary);
}

.position {
  color: var(--text-primary);
}

.separator {
  margin: 0 8px;
  color: var(--text-secondary);
}

.years {
  color: var(--text-secondary);
  font-size: 14px;
  margin-left: 8px;
}

.experience-text, .skills-text, .evaluation-text {
  font-size: 14px;
  line-height: 1.7;
  color: var(--text-primary);
}

/* 求职意向 */
.intention-items {
  display: flex;
  flex-wrap: wrap;
  gap: 16px;
}

.intention-item {
  min-width: 200px;
  font-size: 14px;
}

.intention-item .label {
  color: var(--text-secondary);
  margin-right: 4px;
}

.intention-item .value {
  font-weight: 500;
  color: var(--text-primary);
}

@media print {
  .page-header {
    display: none !important;
  }
  
  .resume-paper {
    box-shadow: none !important;
    padding: 0 !important;
    max-width: 100% !important;
  }
}

@media (max-width: 768px) {
  .resume-paper {
    padding: 20px;
  }
  
  .resume-name {
    font-size: 24px;
  }
  
  .resume-quick-info {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }
  
  .education-info {
    flex-direction: column;
  }
  
  .education-details {
    text-align: left;
    margin-top: 8px;
  }
  
  .intention-items {
    flex-direction: column;
    gap: 8px;
  }
}
</style> 