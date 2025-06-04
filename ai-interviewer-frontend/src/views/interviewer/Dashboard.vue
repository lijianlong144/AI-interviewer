<template>
  <div class="dashboard-container">
    <el-row :gutter="20">
      <el-col :span="8">
        <el-card class="data-card">
          <div class="data-header">
            <div class="data-title">今日面试</div>
            <el-icon class="data-icon" color="#409EFF"><Calendar /></el-icon>
          </div>
          <div class="data-number">{{ todayInterviews }}</div>
          <div class="data-desc">今日待进行的面试数量</div>
        </el-card>
      </el-col>
      
      <el-col :span="8">
        <el-card class="data-card">
          <div class="data-header">
            <div class="data-title">即将开始</div>
            <el-icon class="data-icon" color="#67C23A"><Timer /></el-icon>
          </div>
          <div class="data-number">{{ upcomingInterviews }}</div>
          <div class="data-desc">最近2小时内的面试</div>
        </el-card>
      </el-col>
      
      <el-col :span="8">
        <el-card class="data-card">
          <div class="data-header">
            <div class="data-title">总面试数</div>
            <el-icon class="data-icon" color="#E6A23C"><DataLine /></el-icon>
          </div>
          <div class="data-number">{{ totalInterviews }}</div>
          <div class="data-desc">已完成的面试总数</div>
        </el-card>
      </el-col>
    </el-row>
    
    <el-card class="recent-card">
      <template #header>
        <div class="card-header">
          <span>最近面试</span>
        </div>
      </template>
      
      <el-table :data="recentInterviews" style="width: 100%">
        <el-table-column prop="interviewNo" label="面试编号" width="150" />
        <el-table-column prop="candidateName" label="候选人" width="120" />
        <el-table-column prop="position" label="应聘职位" width="150" />
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
            <el-button 
              v-if="scope.row.status === 'PENDING'" 
              type="primary" 
              link 
              @click="startInterview(scope.row)"
            >
              开始面试
            </el-button>
            <el-button 
              v-if="scope.row.status === 'ONGOING'" 
              type="success" 
              link 
              @click="enterRoom(scope.row)"
            >
              进入房间
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
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import dayjs from 'dayjs'

const router = useRouter()

// 数据统计
const todayInterviews = ref(0)
const upcomingInterviews = ref(0)
const totalInterviews = ref(0)

// 最近面试列表
const recentInterviews = ref([])

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

// 开始面试
const startInterview = (interview) => {
  // 调用开始面试接口
  console.log('开始面试', interview)
  ElMessage.success('面试已开始')
  // 刷新数据
  loadDashboardData()
}

// 进入面试房间
const enterRoom = (interview) => {
  router.push(`/interviewer/interview-room/${interview.roomCode}`)
}

// 跳转到面试列表
const goToInterviewList = () => {
  router.push('/interviewer/interview')
}

// 加载仪表盘数据
const loadDashboardData = async () => {
  try {
    // 模拟数据，实际项目中需要调用后端接口
    todayInterviews.value = 3
    upcomingInterviews.value = 1
    totalInterviews.value = 25
    
    recentInterviews.value = [
      {
        id: 1,
        interviewNo: 'IV20241220001',
        candidateName: '张三',
        position: 'Java开发工程师',
        scheduledTime: '2024-12-21T14:00:00',
        status: 'PENDING',
        roomCode: '123456'
      },
      {
        id: 2,
        interviewNo: 'IV20241220002',
        candidateName: '李四',
        position: '前端开发工程师',
        scheduledTime: '2024-12-21T16:00:00',
        status: 'PENDING',
        roomCode: '234567'
      },
      {
        id: 3,
        interviewNo: 'IV20241219001',
        candidateName: '王五',
        position: '算法工程师',
        scheduledTime: '2024-12-19T10:00:00',
        status: 'COMPLETED',
        roomCode: '345678'
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