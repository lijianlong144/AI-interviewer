<template>
  <div class="dashboard-container">
    <el-row :gutter="20">
      <el-col :span="6">
        <el-card class="data-card">
          <div class="data-header">
            <div class="data-title">今日面试</div>
            <el-icon class="data-icon" color="#409EFF"><Calendar /></el-icon>
          </div>
          <div class="data-number">{{ todayInterviews }}</div>
          <div class="data-desc">今日安排的面试数量</div>
        </el-card>
      </el-col>
      
      <el-col :span="6">
        <el-card class="data-card">
          <div class="data-header">
            <div class="data-title">面试官数量</div>
            <el-icon class="data-icon" color="#67C23A"><User /></el-icon>
          </div>
          <div class="data-number">{{ interviewerCount }}</div>
          <div class="data-desc">系统中的面试官数量</div>
        </el-card>
      </el-col>
      
      <el-col :span="6">
        <el-card class="data-card">
          <div class="data-header">
            <div class="data-title">候选人数量</div>
            <el-icon class="data-icon" color="#E6A23C"><Avatar /></el-icon>
          </div>
          <div class="data-number">{{ candidateCount }}</div>
          <div class="data-desc">系统中的候选人数量</div>
        </el-card>
      </el-col>
      
      <el-col :span="6">
        <el-card class="data-card">
          <div class="data-header">
            <div class="data-title">题库数量</div>
            <el-icon class="data-icon" color="#F56C6C"><Document /></el-icon>
          </div>
          <div class="data-number">{{ questionCount }}</div>
          <div class="data-desc">系统中的题目总数</div>
        </el-card>
      </el-col>
    </el-row>
    
    <el-row :gutter="20" class="chart-row">
      <el-col :span="12">
        <el-card class="chart-card">
          <template #header>
            <div class="card-header">
              <span>面试状态统计</span>
            </div>
          </template>
          <div class="chart-placeholder">
            <div class="status-legend">
              <div v-for="(item, index) in statusData" :key="index" class="legend-item">
                <div class="legend-color" :style="{backgroundColor: item.color}"></div>
                <div class="legend-text">{{ item.name }}: {{ item.value }}</div>
              </div>
            </div>
            <div class="chart-mock">
              <!-- 这里可以后续集成echarts或其他图表库 -->
              <div class="pie-chart-mock">
                <div v-for="(item, index) in statusData" :key="index" 
                  class="pie-slice" 
                  :style="{
                    transform: `rotate(${getRotation(index)}deg)`,
                    clip: getClipStyle(item.percentage),
                    backgroundColor: item.color
                  }">
                </div>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :span="12">
        <el-card class="chart-card">
          <template #header>
            <div class="card-header">
              <span>最近7天面试趋势</span>
            </div>
          </template>
          <div class="chart-placeholder">
            <div class="trend-chart">
              <!-- 后续可集成echarts等图表库 -->
              <div class="trend-bars">
                <div v-for="(item, index) in trendData" :key="index" class="trend-bar-group">
                  <div class="trend-bar" :style="{height: `${item.count * 10}px`, backgroundColor: '#409EFF'}"></div>
                  <div class="trend-label">{{ item.date }}</div>
                </div>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
    
    <el-card class="recent-card">
      <template #header>
        <div class="card-header">
          <span>最近安排的面试</span>
          <el-button type="primary" size="small" @click="createInterview">创建面试</el-button>
        </div>
      </template>
      
      <el-table :data="recentInterviews" style="width: 100%">
        <el-table-column prop="interviewNo" label="面试编号" width="150" />
        <el-table-column prop="candidateName" label="候选人" width="120" />
        <el-table-column prop="position" label="应聘职位" width="150" />
        <el-table-column prop="interviewerName" label="面试官" width="120" />
        <el-table-column prop="scheduledTime" label="预约时间" width="180">
          <template #default="scope">
            {{ formatDateTime(scope.row.scheduledTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
            <el-tag :type="getStatusType(scope.row.status)">{{ getStatusText(scope.row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作">
          <template #default="scope">
            <el-button type="primary" link @click="viewInterview(scope.row)">查看</el-button>
            <el-button 
              v-if="scope.row.status === 'PENDING'" 
              type="danger" 
              link 
              @click="cancelInterview(scope.row)"
            >
              取消
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <div class="more-link">
        <el-button type="primary" link @click="goToInterviewList">查看全部</el-button>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import dayjs from 'dayjs'

const router = useRouter()

// 数据统计
const todayInterviews = ref(0)
const interviewerCount = ref(0)
const candidateCount = ref(0)
const questionCount = ref(0)

// 面试状态数据
const statusData = ref([
  { name: '待开始', value: 8, percentage: 40, color: '#909399' },
  { name: '进行中', value: 2, percentage: 10, color: '#409EFF' },
  { name: '已完成', value: 9, percentage: 45, color: '#67C23A' },
  { name: '已取消', value: 1, percentage: 5, color: '#F56C6C' }
])

// 趋势数据
const trendData = ref([
  { date: '6-01', count: 3 },
  { date: '6-02', count: 5 },
  { date: '6-03', count: 2 },
  { date: '6-04', count: 7 },
  { date: '6-05', count: 4 },
  { date: '6-06', count: 6 },
  { date: '6-07', count: 8 }
])

// 最近面试列表
const recentInterviews = ref([])

// 获取饼图旋转角度
const getRotation = (index) => {
  let rotation = 0
  for (let i = 0; i < index; i++) {
    rotation += statusData.value[i].percentage * 3.6
  }
  return rotation
}

// 获取饼图切片样式
const getClipStyle = (percentage) => {
  return `clip-path: polygon(0 0, 100% 0, 100% 100%, 0 100%)`
}

// 格式化日期时间
const formatDateTime = (datetime) => {
  return dayjs(datetime).format('YYYY-MM-DD HH:mm')
}

// 获取状态类型
const getStatusType = (status) => {
  switch (status) {
    case 'PENDING': return 'info'
    case 'ONGOING': return 'success'
    case 'COMPLETED': return ''
    case 'CANCELLED': return 'danger'
    default: return 'info'
  }
}

// 获取状态文本
const getStatusText = (status) => {
  switch (status) {
    case 'PENDING': return '待开始'
    case 'ONGOING': return '进行中'
    case 'COMPLETED': return '已完成'
    case 'CANCELLED': return '已取消'
    default: return '未知'
  }
}

// 查看面试详情
const viewInterview = (interview) => {
  router.push(`/hr/interview?id=${interview.id}`)
}

// 取消面试
const cancelInterview = (interview) => {
  ElMessageBox.confirm(
    `确定要取消面试 ${interview.interviewNo} 吗？`,
    '取消面试',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(() => {
    // 调用取消面试接口
    ElMessage.success('面试已取消')
    // 刷新数据
    loadDashboardData()
  }).catch(() => {})
}

// 创建面试
const createInterview = () => {
  router.push('/hr/interview')
}

// 跳转到面试列表
const goToInterviewList = () => {
  router.push('/hr/interview')
}

// 加载仪表盘数据
const loadDashboardData = async () => {
  try {
    // 模拟数据，实际项目中需要调用后端接口
    todayInterviews.value = 5
    interviewerCount.value = 10
    candidateCount.value = 32
    questionCount.value = 120
    
    recentInterviews.value = [
      {
        id: 1,
        interviewNo: 'IV20241220001',
        candidateName: '张三',
        position: 'Java开发工程师',
        interviewerName: '李老师',
        scheduledTime: '2024-12-21T14:00:00',
        status: 'PENDING'
      },
      {
        id: 2,
        interviewNo: 'IV20241220002',
        candidateName: '李四',
        position: '前端开发工程师',
        interviewerName: '王老师',
        scheduledTime: '2024-12-21T16:00:00',
        status: 'PENDING'
      },
      {
        id: 3,
        interviewNo: 'IV20241219001',
        candidateName: '王五',
        position: '算法工程师',
        interviewerName: '赵老师',
        scheduledTime: '2024-12-19T10:00:00',
        status: 'COMPLETED'
      }
    ]
  } catch (error) {
    console.error('加载仪表盘数据失败:', error)
  }
}

onMounted(() => {
  loadDashboardData()
})
</script>

<style scoped>
.dashboard-container {
  padding: 20px 0;
}

.data-card {
  height: 180px;
  margin-bottom: 20px;
}

.data-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.data-title {
  font-size: 16px;
  color: #606266;
}

.data-icon {
  font-size: 24px;
}

.data-number {
  font-size: 36px;
  font-weight: bold;
  margin-bottom: 10px;
}

.data-desc {
  font-size: 14px;
  color: #909399;
}

.chart-row {
  margin-bottom: 20px;
}

.chart-card {
  height: 380px;
}

.chart-placeholder {
  height: 100%;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
}

.status-legend {
  display: flex;
  justify-content: space-around;
  width: 100%;
  margin-bottom: 20px;
}

.legend-item {
  display: flex;
  align-items: center;
}

.legend-color {
  width: 12px;
  height: 12px;
  margin-right: 5px;
  border-radius: 2px;
}

.pie-chart-mock {
  position: relative;
  width: 200px;
  height: 200px;
  border-radius: 50%;
  overflow: hidden;
}

.pie-slice {
  position: absolute;
  width: 100%;
  height: 100%;
  transform-origin: 50% 50%;
}

.trend-chart {
  width: 100%;
  height: 300px;
  display: flex;
  align-items: flex-end;
  justify-content: center;
}

.trend-bars {
  display: flex;
  justify-content: space-around;
  align-items: flex-end;
  width: 80%;
  height: 250px;
}

.trend-bar-group {
  display: flex;
  flex-direction: column;
  align-items: center;
  width: 40px;
}

.trend-bar {
  width: 30px;
  border-radius: 3px 3px 0 0;
}

.trend-label {
  margin-top: 10px;
  color: #909399;
}

.recent-card {
  margin-top: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.more-link {
  text-align: center;
  margin-top: 20px;
}
</style> 