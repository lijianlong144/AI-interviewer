<template>
  <div class="result-container">
    <el-row :gutter="20">
      <el-col :span="24">
        <el-page-header @back="goBack">
          <template #content>
            <div class="page-title">面试结果</div>
          </template>
        </el-page-header>
      </el-col>
    </el-row>
    
    <div v-loading="loading" class="result-content">
      <!-- 总体评分卡片 -->
      <el-card class="score-card">
        <template #header>
          <div class="card-header">
            <span>总体评分</span>
          </div>
        </template>
        
        <div class="score-overview">
          <div class="score-circle">
            <el-progress 
              type="dashboard" 
              :percentage="overallScore" 
              :color="getScoreColor"
              :stroke-width="12"
            />
            <div class="score-text">{{ overallScore }}/100</div>
          </div>
          
          <div class="score-detail">
            <el-row :gutter="20">
              <el-col :span="8" v-for="(score, index) in categoryScores" :key="index">
                <div class="score-item">
                  <div class="score-category">{{ score.category }}</div>
                  <el-progress 
                    :percentage="score.value" 
                    :color="getScoreColor"
                    :stroke-width="8"
                    :format="(val) => val + '/100'"
                  />
                </div>
              </el-col>
            </el-row>
          </div>
        </div>
      </el-card>
      
      <!-- 面试信息卡片 -->
      <el-card class="info-card">
        <template #header>
          <div class="card-header">
            <span>面试信息</span>
          </div>
        </template>
        
        <el-descriptions :column="3" border>
          <el-descriptions-item label="面试编号">{{ interviewInfo.interviewNo }}</el-descriptions-item>
          <el-descriptions-item label="应聘职位">{{ interviewInfo.position }}</el-descriptions-item>
          <el-descriptions-item label="面试时间">{{ formatDateTime(interviewInfo.interviewTime) }}</el-descriptions-item>
          <el-descriptions-item label="面试时长">{{ formatDuration(interviewInfo.duration) }}</el-descriptions-item>
          <el-descriptions-item label="问题数量">{{ interviewInfo.questionCount }} 个</el-descriptions-item>
          <el-descriptions-item label="完成状态">
            <el-tag type="success">已完成</el-tag>
          </el-descriptions-item>
        </el-descriptions>
      </el-card>
      
      <!-- 总体评价卡片 -->
      <el-card class="evaluation-card">
        <template #header>
          <div class="card-header">
            <span>总体评价</span>
          </div>
        </template>
        
        <div class="evaluation-content" v-if="overallEvaluation">
          <p>{{ overallEvaluation }}</p>
        </div>
        
        <el-empty v-else description="暂无评价"></el-empty>
      </el-card>
      
      <!-- 问题和回答明细 -->
      <el-card class="questions-card">
        <template #header>
          <div class="card-header">
            <span>问题详情</span>
          </div>
        </template>
        
        <el-collapse>
          <el-collapse-item 
            v-for="(item, index) in questionResults" 
            :key="index"
            :title="`问题 ${index + 1}: ${item.question}`"
            :name="index"
          >
            <div class="question-detail">
              <div class="answer-section">
                <h4>您的回答:</h4>
                <p class="answer-content">{{ item.answer }}</p>
                <div class="answer-meta">
                  <span>回答用时: {{ formatDuration(item.timeSpent) }}</span>
                  <span>得分: <strong>{{ item.score }}</strong>/100</span>
                </div>
              </div>
              
              <div class="evaluation-section">
                <h4>评价:</h4>
                <p>{{ item.evaluation }}</p>
              </div>
            </div>
          </el-collapse-item>
        </el-collapse>
      </el-card>
      
      <!-- 改进建议卡片 -->
      <el-card class="suggestions-card">
        <template #header>
          <div class="card-header">
            <span>改进建议</span>
          </div>
        </template>
        
        <div class="suggestions-content" v-if="suggestions.length > 0">
          <ul class="suggestion-list">
            <li v-for="(suggestion, index) in suggestions" :key="index">
              {{ suggestion }}
            </li>
          </ul>
        </div>
        
        <el-empty v-else description="暂无改进建议"></el-empty>
      </el-card>
      
      <!-- 反馈表单卡片 -->
      <el-card class="feedback-card">
        <template #header>
          <div class="card-header">
            <span>您的反馈</span>
          </div>
        </template>
        
        <el-form :model="feedbackForm" label-width="100px" ref="feedbackFormRef">
          <el-form-item label="体验评分">
            <el-rate v-model="feedbackForm.rating" :max="5" show-score></el-rate>
          </el-form-item>
          
          <el-form-item label="反馈意见">
            <el-input 
              v-model="feedbackForm.content" 
              type="textarea" 
              :rows="4"
              placeholder="请输入您对此次面试体验的反馈或建议..."
            ></el-input>
          </el-form-item>
          
          <el-form-item>
            <el-button type="primary" @click="submitFeedbackHandler" :loading="submitting">提交反馈</el-button>
          </el-form-item>
        </el-form>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getInterviewResult, getQuestionResults, getOverallEvaluation, getInterviewScores, submitFeedback } from '@/api/result'
import { ElMessage } from 'element-plus'
import dayjs from 'dayjs'

const route = useRoute()
const router = useRouter()
const interviewId = route.params.id

// 加载状态
const loading = ref(true)
const submitting = ref(false)

// 面试结果数据
const overallScore = ref(0)
const categoryScores = ref([])
const interviewInfo = reactive({
  interviewNo: '',
  position: '',
  interviewTime: '',
  duration: 0,
  questionCount: 0
})
const overallEvaluation = ref('')
const questionResults = ref([])
const suggestions = ref([])

// 反馈表单
const feedbackForm = reactive({
  rating: 5,
  content: ''
})
const feedbackFormRef = ref()

// 返回上一页
const goBack = () => {
  router.push('/candidate/dashboard')
}

// 获取分数颜色
const getScoreColor = (percentage) => {
  if (percentage > 80) {
    return '#67c23a' // 绿色 - 优秀
  } else if (percentage > 60) {
    return '#e6a23c' // 黄色 - 良好
  } else {
    return '#f56c6c' // 红色 - 一般
  }
}

// 格式化日期时间
const formatDateTime = (datetime) => {
  return dayjs(datetime).format('YYYY-MM-DD HH:mm')
}

// 格式化时长
const formatDuration = (seconds) => {
  const minutes = Math.floor(seconds / 60)
  const remainingSeconds = seconds % 60
  return `${minutes}分${remainingSeconds}秒`
}

// 加载面试结果
const loadInterviewResult = async () => {
  try {
    loading.value = true
    
    // 获取面试基本信息
    const resultRes = await getInterviewResult(interviewId)
    
    if (resultRes.data) {
      const resultData = resultRes.data
      
      interviewInfo.interviewNo = resultData.interviewNo
      interviewInfo.position = resultData.position
      interviewInfo.interviewTime = resultData.startTime
      interviewInfo.duration = resultData.duration
      interviewInfo.questionCount = resultData.questionCount
    }
    
    // 获取问题详情
    const questionsRes = await getQuestionResults(interviewId)
    questionResults.value = questionsRes.data || []
    
    // 获取总体评价
    const evaluationRes = await getOverallEvaluation(interviewId)
    if (evaluationRes.data) {
      overallEvaluation.value = evaluationRes.data.content
      
      // 设置改进建议
      if (evaluationRes.data.suggestions) {
        suggestions.value = evaluationRes.data.suggestions
      }
    }
    
    // 获取评分详情
    const scoresRes = await getInterviewScores(interviewId)
    if (scoresRes.data) {
      overallScore.value = scoresRes.data.overall || 0
      
      if (scoresRes.data.categories) {
        categoryScores.value = Object.entries(scoresRes.data.categories).map(([key, value]) => ({
          category: key,
          value: value
        }))
      }
    }
  } catch (error) {
    console.error('加载面试结果失败:', error)
    ElMessage.error('加载面试结果失败')
  } finally {
    loading.value = false
  }
}

// 提交反馈
const submitFeedbackHandler = async () => {
  if (!feedbackForm.content.trim()) {
    ElMessage.warning('请输入反馈内容')
    return
  }
  
  try {
    submitting.value = true
    
    await submitFeedback({
      interviewId,
      rating: feedbackForm.rating,
      content: feedbackForm.content
    })
    
    ElMessage.success('感谢您的反馈')
    
    // 清空表单
    feedbackForm.rating = 5
    feedbackForm.content = ''
  } catch (error) {
    console.error('提交反馈失败:', error)
    ElMessage.error('提交反馈失败')
  } finally {
    submitting.value = false
  }
}

onMounted(() => {
  loadInterviewResult()
})
</script>

<style scoped>
.result-container {
  padding: 20px 0;
}

.page-title {
  font-size: 18px;
  font-weight: bold;
}

.result-content {
  margin-top: 20px;
}

.score-card,
.info-card,
.evaluation-card,
.questions-card,
.suggestions-card,
.feedback-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.score-overview {
  display: flex;
  align-items: center;
  padding: 20px 0;
}

.score-circle {
  position: relative;
  margin-right: 40px;
}

.score-text {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  font-size: 18px;
  font-weight: bold;
}

.score-detail {
  flex: 1;
}

.score-item {
  margin-bottom: 15px;
}

.score-category {
  margin-bottom: 5px;
  font-weight: bold;
}

.evaluation-content {
  line-height: 1.8;
  color: #303133;
  padding: 10px 0;
}

.question-detail {
  padding: 10px 0;
}

.answer-section {
  margin-bottom: 20px;
}

.answer-content {
  background-color: #f8f8f8;
  padding: 10px;
  border-radius: 4px;
  white-space: pre-wrap;
  line-height: 1.6;
}

.answer-meta {
  display: flex;
  justify-content: space-between;
  margin-top: 10px;
  color: #606266;
  font-size: 14px;
}

.evaluation-section {
  border-top: 1px solid #ebeef5;
  padding-top: 15px;
}

.suggestion-list {
  padding-left: 20px;
}

.suggestion-list li {
  margin-bottom: 10px;
  line-height: 1.6;
}
</style>
</rewritten_file>