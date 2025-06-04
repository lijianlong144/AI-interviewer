<template>
  <div class="dashboard-container">
    <el-row :gutter="20">
      <el-col :span="24">
        <el-card class="welcome-card">
          <div class="welcome-content">
            <h2 class="welcome-title">欢迎使用AI面试系统</h2>
            <p class="welcome-text">您可以在这里查看和管理您的所有面试安排</p>
          </div>
        </el-card>
      </el-col>
    </el-row>
    
    <el-row :gutter="20" class="status-row">
      <el-col :span="8">
        <el-card class="status-card pending">
          <el-icon class="status-icon"><Calendar /></el-icon>
          <div class="status-content">
            <div class="status-number">{{ pendingCount }}</div>
            <div class="status-text">待面试</div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :span="8">
        <el-card class="status-card ongoing">
          <el-icon class="status-icon"><VideoPlay /></el-icon>
          <div class="status-content">
            <div class="status-number">{{ ongoingCount }}</div>
            <div class="status-text">进行中</div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :span="8">
        <el-card class="status-card completed">
          <el-icon class="status-icon"><CircleCheckFilled /></el-icon>
          <div class="status-content">
            <div class="status-number">{{ completedCount }}</div>
            <div class="status-text">已完成</div>
          </div>
        </el-card>
      </el-col>
    </el-row>
    
    <el-card class="upcoming-card">
      <template #header>
        <div class="card-header">
          <span>最近面试安排</span>
        </div>
      </template>
      
      <div v-if="upcomingInterview" class="upcoming-content">
        <div class="interview-info">
          <div class="info-item">
            <span class="info-label">面试编号:</span>
            <span class="info-value">{{ upcomingInterview.interviewNo }}</span>
          </div>
          <div class="info-item">
            <span class="info-label">应聘职位:</span>
            <span class="info-value">{{ upcomingInterview.position }}</span>
          </div>
          <div class="info-item">
            <span class="info-label">预约时间:</span>
            <span class="info-value highlight">{{ formatDateTime(upcomingInterview.scheduledTime) }}</span>
          </div>
          <div class="info-item">
            <span class="info-label">状态:</span>
            <el-tag :type="getStatusType(upcomingInterview.status)">{{ getStatusText(upcomingInterview.status) }}</el-tag>
          </div>
        </div>
        
        <div class="interview-actions">
          <el-button 
            v-if="upcomingInterview.status === 'PENDING'" 
            type="primary"
            @click="checkInterview(upcomingInterview)"
          >
            查看详情
          </el-button>
          <el-button 
            v-if="upcomingInterview.status === 'ONGOING'" 
            type="success"
            @click="enterRoom(upcomingInterview)"
          >
            进入面试
          </el-button>
          <el-countdown 
            v-if="upcomingInterview.status === 'PENDING'"
            :value="getCountdownValue(upcomingInterview.scheduledTime)" 
            format="DD 天 HH:mm:ss"
          >
            <template #prefix>距离面试开始还有：</template>
          </el-countdown>
        </div>
      </div>
      
      <el-empty v-else description="暂无即将到来的面试安排"></el-empty>
    </el-card>
    
    <el-card class="interview-list-card">
      <template #header>
        <div class="card-header">
          <span>所有面试</span>
        </div>
      </template>
      
      <el-table :data="interviewList" style="width: 100%">
        <el-table-column prop="interviewNo" label="面试编号" width="150" />
        <el-table-column prop="position" label="应聘职位" width="180" />
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
              @click="checkInterview(scope.row)"
            >
              查看详情
            </el-button>
            <el-button 
              v-if="scope.row.status === 'ONGOING'" 
              type="success" 
              link 
              @click="enterRoom(scope.row)"
            >
              进入面试
            </el-button>
            <el-button 
              v-if="scope.row.status === 'COMPLETED'" 
              type="info" 
              link 
              @click="viewResult(scope.row)"
            >
              查看结果
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/userStore'
import dayjs from 'dayjs'

const router = useRouter()
const userStore = useUserStore()

// 面试统计
const pendingCount = ref(0)
const ongoingCount = ref(0)
const completedCount = ref(0)

// 面试列表
const interviewList = ref([])

// 获取最近的一个面试
const upcomingInterview = computed(() => {
  const pending = interviewList.value.filter(item => item.status === 'PENDING')
  const ongoing = interviewList.value.filter(item => item.status === 'ONGOING')
  
  // 优先显示进行中的
  if (ongoing.length > 0) {
    return ongoing[0]
  }
  
  // 其次显示最近的待面试
  if (pending.length > 0) {
    return pending.sort((a, b) => {
      return new Date(a.scheduledTime) - new Date(b.scheduledTime)
    })[0]
  }
  
  return null
})

// 格式化日期时间
const formatDateTime = (datetime) => {
  return dayjs(datetime).format('YYYY-MM-DD HH:mm')
}

// 获取倒计时值
const getCountdownValue = (datetime) => {
  return dayjs(datetime).valueOf()
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
const checkInterview = (interview) => {
  router.push(`/candidate/interview-detail/${interview.id}`)
}

// 进入面试房间
const enterRoom = (interview) => {
  router.push(`/candidate/interview-room/${interview.roomCode}`)
}

// 查看面试结果
const viewResult = (interview) => {
  router.push(`/candidate/interview-result/${interview.id}`)
}

// 加载面试数据
const loadInterviewData = async () => {
  try {
    // 模拟数据，实际项目中需要调用后端接口
    const userId = userStore.userInfo?.id || 0
    
    // 模拟数据
    interviewList.value = [
      {
        id: 1,
        interviewNo: 'IV20241220001',
        position: 'Java开发工程师',
        scheduledTime: '2024-12-21T14:00:00',
        status: 'PENDING',
        roomCode: '123456'
      },
      {
        id: 2,
        interviewNo: 'IV20241219001',
        position: '前端开发工程师',
        scheduledTime: '2024-12-19T16:00:00',
        status: 'COMPLETED',
        roomCode: '234567'
      },
      {
        id: 3,
        interviewNo: 'IV20241218001',
        position: 'Python开发工程师',
        scheduledTime: '2024-12-18T10:00:00',
        status: 'COMPLETED',
        roomCode: '345678'
      }
    ]
    
    // 计算统计数据
    pendingCount.value = interviewList.value.filter(item => item.status === 'PENDING').length
    ongoingCount.value = interviewList.value.filter(item => item.status === 'ONGOING').length
    completedCount.value = interviewList.value.filter(item => item.status === 'COMPLETED').length
    
  } catch (error) {
    console.error('加载面试数据失败:', error)
  }
}

onMounted(() => {
  loadInterviewData()
})
</script>

<style scoped>
.dashboard-container {
  padding: 20px 0;
}

.welcome-card {
  margin-bottom: 20px;
  background: linear-gradient(to right, #4facfe, #00f2fe);
  color: white;
}

.welcome-content {
  text-align: center;
  padding: 20px 0;
}

.welcome-title {
  font-size: 24px;
  margin-bottom: 10px;
}

.welcome-text {
  font-size: 16px;
  opacity: 0.9;
}

.status-row {
  margin-bottom: 20px;
}

.status-card {
  height: 120px;
  display: flex;
  align-items: center;
  padding: 20px;
}

.status-card.pending {
  background-color: #e6f7ff;
  border-color: #91d5ff;
}

.status-card.ongoing {
  background-color: #f6ffed;
  border-color: #b7eb8f;
}

.status-card.completed {
  background-color: #f9f0ff;
  border-color: #d3adf7;
}

.status-icon {
  font-size: 36px;
  margin-right: 20px;
}

.status-content {
  flex: 1;
}

.status-number {
  font-size: 28px;
  font-weight: bold;
  margin-bottom: 5px;
}

.status-text {
  font-size: 16px;
  color: #606266;
}

.upcoming-card {
  margin-bottom: 20px;
}

.upcoming-content {
  padding: 20px;
  background-color: #fafafa;
  border-radius: 4px;
}

.interview-info {
  margin-bottom: 20px;
}

.info-item {
  margin-bottom: 10px;
  display: flex;
}

.info-label {
  font-weight: bold;
  width: 100px;
}

.info-value {
  flex: 1;
}

.info-value.highlight {
  color: #409EFF;
  font-weight: bold;
}

.interview-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.interview-list-card {
  margin-top: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style> 