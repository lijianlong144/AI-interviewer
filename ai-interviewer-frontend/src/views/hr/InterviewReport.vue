<template>
  <div class="interview-report-container">
    <el-row :gutter="20">
      <el-col :span="24">
        <el-page-header @back="goBack">
          <template #content>
            <div class="page-title">面试评估报告</div>
          </template>
        </el-page-header>
      </el-col>
    </el-row>
    
    <div v-loading="loading" class="report-content">
      <!-- 候选人基本信息 -->
      <el-card class="candidate-card">
        <template #header>
          <div class="card-header">
            <span>候选人信息</span>
            <el-button type="primary" size="small" @click="downloadReport">下载报告</el-button>
          </div>
        </template>
        
        <el-row :gutter="20">
          <el-col :span="6">
            <div class="candidate-avatar">
              <el-avatar :size="100" :src="candidateInfo.avatar || defaultAvatar"></el-avatar>
            </div>
          </el-col>
          <el-col :span="18">
            <el-descriptions :column="2" border>
              <el-descriptions-item label="姓名">{{ candidateInfo.name }}</el-descriptions-item>
              <el-descriptions-item label="应聘职位">{{ candidateInfo.position }}</el-descriptions-item>
              <el-descriptions-item label="邮箱">{{ candidateInfo.email }}</el-descriptions-item>
              <el-descriptions-item label="电话">{{ candidateInfo.phone }}</el-descriptions-item>
              <el-descriptions-item label="面试时间">{{ formatDateTime(interviewInfo.interviewTime) }}</el-descriptions-item>
              <el-descriptions-item label="面试时长">{{ formatDuration(interviewInfo.duration) }}</el-descriptions-item>
            </el-descriptions>
          </el-col>
        </el-row>
      </el-card>
      
      <!-- 总体评分卡片 -->
      <el-card class="score-card">
        <template #header>
          <div class="card-header">
            <span>总体评估</span>
            <div class="recommendation">
              <span class="recommendation-label">推荐等级:</span>
              <el-tag :type="getRecommendationType(recommendation)">{{ recommendation }}</el-tag>
            </div>
          </div>
        </template>
        
        <div class="score-overview">
          <el-row :gutter="20">
            <el-col :span="8">
              <div class="score-circle">
                <el-progress 
                  type="dashboard" 
                  :percentage="overallScore" 
                  :color="getScoreColor"
                  :stroke-width="12"
                />
                <div class="score-text">{{ overallScore }}/100</div>
              </div>
              <div class="score-label">总分</div>
            </el-col>
            <el-col :span="16">
              <div class="radar-chart-container" ref="radarChartContainer"></div>
            </el-col>
          </el-row>
        </div>
      </el-card>
      
      <!-- 能力评估卡片 -->
      <el-card class="abilities-card">
        <template #header>
          <div class="card-header">
            <span>能力评估</span>
          </div>
        </template>
        
        <div class="abilities-content">
          <div v-for="(ability, index) in abilityScores" :key="index" class="ability-item">
            <div class="ability-header">
              <h4>{{ ability.name }}</h4>
              <div class="ability-score">
                <el-progress 
                  :percentage="ability.value" 
                  :color="getScoreColor"
                  :stroke-width="8"
                  :format="(val) => val + '/100'"
                />
              </div>
            </div>
            <div class="ability-details">
              <p>{{ ability.evaluation }}</p>
              <div class="ability-tags">
                <el-tag v-for="(tag, tagIndex) in ability.tags" :key="tagIndex" :type="getTagType(tagIndex)">
                  {{ tag }}
                </el-tag>
              </div>
            </div>
          </div>
        </div>
      </el-card>
      
      <!-- 总体评价卡片 -->
      <el-card class="evaluation-card">
        <template #header>
          <div class="card-header">
            <span>总体评价</span>
          </div>
        </template>
        
        <div class="evaluation-content">
          <p>{{ overallEvaluation }}</p>
          
          <div class="strengths-weaknesses">
            <div class="strengths">
              <h4>优势:</h4>
              <ul>
                <li v-for="(item, index) in strengths" :key="index">{{ item }}</li>
              </ul>
            </div>
            <div class="weaknesses">
              <h4>待提升:</h4>
              <ul>
                <li v-for="(item, index) in weaknesses" :key="index">{{ item }}</li>
              </ul>
            </div>
          </div>
        </div>
      </el-card>
      
      <!-- 问题回答卡片 -->
      <el-card class="questions-card">
        <template #header>
          <div class="card-header">
            <span>问题回答详情</span>
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
                <h4>候选人回答:</h4>
                <p class="answer-content">{{ item.answer }}</p>
                <div class="answer-meta">
                  <span>回答用时: {{ formatDuration(item.timeSpent) }}</span>
                  <span>得分: <strong>{{ item.score }}</strong>/100</span>
                </div>
              </div>
              
              <div class="evaluation-section">
                <h4>AI评价:</h4>
                <p>{{ item.evaluation }}</p>
              </div>
            </div>
          </el-collapse-item>
        </el-collapse>
      </el-card>
      
      <!-- 面试决策卡片 -->
      <el-card class="decision-card">
        <template #header>
          <div class="card-header">
            <span>面试决策</span>
          </div>
        </template>
        
        <div class="decision-content">
          <el-form :model="decisionForm" label-width="100px" ref="decisionFormRef">
            <el-form-item label="录用决定">
              <el-radio-group v-model="decisionForm.decision">
                <el-radio label="PASS">通过</el-radio>
                <el-radio label="NEXT_ROUND">进入下一轮</el-radio>
                <el-radio label="REJECT">拒绝</el-radio>
              </el-radio-group>
            </el-form-item>
            
            <el-form-item label="HR评语">
              <el-input 
                v-model="decisionForm.comment" 
                type="textarea" 
                :rows="4"
                placeholder="请输入您对此次面试的评价或建议..."
              ></el-input>
            </el-form-item>
            
            <el-form-item>
              <el-button type="primary" @click="submitDecision" :loading="submitting">提交决定</el-button>
              <el-button @click="sendEmail">发送邮件通知</el-button>
            </el-form-item>
          </el-form>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, nextTick, onBeforeUnmount } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import dayjs from 'dayjs'
import * as echarts from 'echarts/core'
import { RadarChart } from 'echarts/charts'
import { CanvasRenderer } from 'echarts/renderers'
import { TitleComponent, TooltipComponent, LegendComponent } from 'echarts/components'

// 注册 ECharts 组件
echarts.use([RadarChart, CanvasRenderer, TitleComponent, TooltipComponent, LegendComponent])

const route = useRoute()
const router = useRouter()
const interviewId = route.params.id
const radarChartContainer = ref(null)
let radarChart = null

// 加载状态
const loading = ref(false)
const submitting = ref(false)

// 默认头像
const defaultAvatar = 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'

// 候选人信息
const candidateInfo = reactive({
  name: '张三',
  position: '高级前端开发工程师',
  email: 'zhangsan@example.com',
  phone: '13800138000',
  avatar: ''
})

// 面试信息
const interviewInfo = reactive({
  interviewTime: '2023-06-15 14:30:00',
  duration: 2700,
  questionCount: 10
})

// 评分数据
const overallScore = ref(85)
const recommendation = ref('A-')
const abilityScores = ref([
  { 
    name: '技术能力', 
    value: 88,
    evaluation: '候选人展示了扎实的技术基础，对所应聘岗位的核心技术有深入理解。能够清晰地解释技术概念并应用到实际问题中。',
    tags: ['框架理解深入', '代码质量高', '技术广度佳']
  },
  { 
    name: '沟通表达', 
    value: 82,
    evaluation: '候选人表达清晰，能够将复杂的技术概念简化解释。回答问题条理分明，但在某些专业术语的使用上可以更加准确。',
    tags: ['表达清晰', '逻辑性强', '需提升专业术语']
  },
  { 
    name: '问题解决', 
    value: 90,
    evaluation: '候选人展示了出色的问题分析和解决能力。能够快速识别问题核心，提出多种解决方案并权衡利弊。思维灵活，解决方案实用。',
    tags: ['分析透彻', '思路清晰', '解决方案优秀']
  },
  { 
    name: '团队协作', 
    value: 85,
    evaluation: '从候选人描述的项目经历中，可以看出其具备良好的团队合作精神。能够理解团队成员的需求，积极参与团队决策。',
    tags: ['协作意识强', '角色转换自如', '沟通有效']
  },
  { 
    name: '学习能力', 
    value: 80,
    evaluation: '候选人展示了良好的学习能力和求知欲。能够快速适应新技术，但在某些前沿技术领域的了解还可以进一步加强。',
    tags: ['学习主动', '适应性好', '需拓展前沿知识']
  }
])

// 总体评价
const overallEvaluation = ref('候选人在此次面试中表现优秀，展示了扎实的技术功底和良好的沟通能力。技术方面，对前端框架和工具链有深入理解，能够清晰解释复杂概念；问题解决能力突出，能够快速分析问题并提出有效解决方案；团队协作意识强，从项目经历中可以看出良好的团队合作精神。建议进入下一轮面试。')

// 优势和待提升
const strengths = ref([
  '扎实的前端技术基础，尤其在React和Vue框架方面有深入理解',
  '出色的问题分析和解决能力，思维逻辑清晰',
  '良好的团队协作精神和沟通能力',
  '丰富的项目经验和实际解决方案'
])

const weaknesses = ref([
  '在系统架构设计方面经验有限，需要进一步提升',
  '对某些前沿技术（如WebAssembly、微前端）的了解不够深入',
  '在性能优化方面的量化分析能力可以加强'
])

// 问题回答
const questionResults = ref([
  {
    question: '请介绍一下你的技术背景和项目经验',
    answer: '我有5年前端开发经验，精通React、Vue等主流框架。最近两年主要负责公司核心产品的前端架构设计和性能优化，将首屏加载时间减少了40%。我带领团队完成了从AngularJS到React的技术栈迁移，同时保证了业务的正常运行。',
    evaluation: '回答全面且有针对性，突出了技术专长和项目成就，能够量化自己的贡献。',
    score: 90,
    timeSpent: 120
  },
  {
    question: '描述一个你解决过的复杂技术问题',
    answer: '在一个大型电商项目中，我们遇到了严重的性能问题，特别是在移动端。经过分析，发现主要瓶颈在于大量的DOM操作和未优化的渲染流程。我实施了虚拟列表、组件懒加载和状态管理优化，最终使页面渲染速度提升了60%，用户体验显著改善。',
    evaluation: '回答结构清晰，问题分析到位，解决方案具体且有效，体现了良好的问题解决能力。',
    score: 95,
    timeSpent: 180
  },
  {
    question: '你如何看待团队协作？请举例说明你的团队合作经历',
    answer: '我认为有效的团队协作基于清晰的沟通和相互尊重。在上一个项目中，我作为前端负责人与后端、设计和产品团队紧密合作。我建立了定期的技术同步会议，创建了详细的API文档规范，并使用原型工具提前与设计师沟通，大大减少了开发过程中的沟通成本和返工。',
    evaluation: '回答体现了对团队协作的深刻理解，举例具体且有说服力，展示了良好的沟通和协调能力。',
    score: 85,
    timeSpent: 150
  }
])

// 决策表单
const decisionForm = reactive({
  decision: 'NEXT_ROUND',
  comment: ''
})
const decisionFormRef = ref()

// 返回上一页
const goBack = () => {
  router.push('/hr/interviews')
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

// 获取推荐等级类型
const getRecommendationType = (rec) => {
  if (rec.startsWith('A')) {
    return 'success'
  } else if (rec.startsWith('B')) {
    return 'warning'
  } else {
    return 'danger'
  }
}

// 获取标签类型
const getTagType = (index) => {
  const types = ['', 'success', 'warning', 'info', 'danger']
  return types[index % types.length]
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

// 初始化雷达图
const initRadarChart = () => {
  if (!radarChartContainer.value) return
  
  // 销毁已有的图表实例
  if (radarChart) {
    radarChart.dispose()
  }
  
  // 创建新的图表实例
  radarChart = echarts.init(radarChartContainer.value)
  
  // 准备数据
  const indicators = abilityScores.value.map(item => ({
    name: item.name,
    max: 100
  }))
  
  const data = abilityScores.value.map(item => item.value)
  
  // 配置项
  const option = {
    title: {
      text: '能力雷达图'
    },
    tooltip: {},
    radar: {
      indicator: indicators,
      shape: 'polygon',
      splitNumber: 5,
      axisName: {
        color: '#333',
        backgroundColor: '#eee',
        borderRadius: 3,
        padding: [3, 5]
      }
    },
    series: [{
      name: '能力评估',
      type: 'radar',
      data: [{
        value: data,
        name: '能力分数',
        areaStyle: {
          color: 'rgba(0, 128, 255, 0.3)'
        },
        lineStyle: {
          color: '#0080ff'
        },
        itemStyle: {
          color: '#0080ff'
        }
      }]
    }]
  }
  
  // 使用配置项设置图表
  radarChart.setOption(option)
}

// 下载报告
const downloadReport = () => {
  ElMessage.success('报告下载中...')
  // 实际应用中这里会调用API下载报告
}

// 提交决定
const submitDecision = () => {
  ElMessage.success('面试决定已提交')
  // 实际应用中这里会调用API提交决定
}

// 发送邮件
const sendEmail = () => {
  ElMessage.success('邮件已发送')
  // 实际应用中这里会调用API发送邮件
}

// 加载面试报告
const loadInterviewReport = async () => {
  try {
    loading.value = true
    
    // 在实际应用中，这里会从API获取数据
    // 现在使用模拟数据
    
    // 初始化雷达图
    nextTick(() => {
      initRadarChart()
      
      // 监听窗口大小变化，重绘图表
      window.addEventListener('resize', () => {
        radarChart && radarChart.resize()
      })
    })
  } catch (error) {
    console.error('获取面试报告失败:', error)
    ElMessage.error('获取面试报告失败')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadInterviewReport()
})

// 组件卸载前清理
onBeforeUnmount(() => {
  if (radarChart) {
    radarChart.dispose()
    radarChart = null
  }
  
  window.removeEventListener('resize', () => {
    radarChart && radarChart.resize()
  })
})
</script>

<style scoped>
.interview-report-container {
  padding: 20px;
}

.page-title {
  font-size: 20px;
  font-weight: bold;
}

.report-content {
  margin-top: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.candidate-card, .score-card, .abilities-card, .evaluation-card, .questions-card, .decision-card {
  margin-bottom: 20px;
}

.candidate-avatar {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100%;
}

.score-overview {
  padding: 20px;
}

.score-circle {
  position: relative;
  width: 200px;
  height: 200px;
  margin: 0 auto;
}

.score-text {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  font-size: 24px;
  font-weight: bold;
}

.score-label {
  text-align: center;
  margin-top: 10px;
  font-size: 16px;
  font-weight: bold;
}

.radar-chart-container {
  height: 300px;
  width: 100%;
}

.recommendation {
  display: flex;
  align-items: center;
}

.recommendation-label {
  margin-right: 8px;
}

.ability-item {
  margin-bottom: 20px;
  padding-bottom: 15px;
  border-bottom: 1px solid #eee;
}

.ability-item:last-child {
  border-bottom: none;
}

.ability-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.ability-score {
  width: 200px;
}

.ability-tags {
  margin-top: 10px;
}

.ability-tags .el-tag {
  margin-right: 8px;
  margin-bottom: 8px;
}

.strengths-weaknesses {
  display: flex;
  margin-top: 20px;
}

.strengths, .weaknesses {
  flex: 1;
  padding: 0 15px;
}

.strengths h4, .weaknesses h4 {
  margin-bottom: 10px;
}

.strengths ul, .weaknesses ul {
  padding-left: 20px;
}

.strengths li, .weaknesses li {
  margin-bottom: 8px;
}

.question-detail {
  padding: 10px 0;
}

.answer-section, .evaluation-section {
  margin-bottom: 15px;
}

.answer-content {
  background-color: #f9f9f9;
  padding: 10px;
  border-radius: 4px;
  white-space: pre-wrap;
}

.answer-meta {
  display: flex;
  justify-content: space-between;
  margin-top: 10px;
  color: #666;
}
</style> 