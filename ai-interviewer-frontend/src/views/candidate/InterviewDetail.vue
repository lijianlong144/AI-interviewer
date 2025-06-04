<template>
  <div class="detail-container">
    <el-row :gutter="20">
      <el-col :span="24">
        <el-page-header @back="goBack">
          <template #content>
            <div class="page-title">面试详情</div>
          </template>
        </el-page-header>
      </el-col>
    </el-row>
    
    <div v-loading="loading" class="detail-content">
      <el-card class="info-card">
        <template #header>
          <div class="card-header">
            <span>基本信息</span>
            <el-tag :type="getStatusType(interviewInfo.status)">{{ getStatusText(interviewInfo.status) }}</el-tag>
          </div>
        </template>
        
        <el-descriptions :column="2" border>
          <el-descriptions-item label="面试编号">{{ interviewInfo.interviewNo }}</el-descriptions-item>
          <el-descriptions-item label="应聘职位">{{ interviewInfo.position }}</el-descriptions-item>
          <el-descriptions-item label="预约时间">{{ formatDateTime(interviewInfo.scheduledTime) }}</el-descriptions-item>
          <el-descriptions-item label="房间号">{{ interviewInfo.roomCode }}</el-descriptions-item>
        </el-descriptions>
      </el-card>
      
      <el-card class="desc-card">
        <template #header>
          <div class="card-header">
            <span>面试说明</span>
          </div>
        </template>
        
        <div class="description-content">
          <p v-if="interviewInfo.description">{{ interviewInfo.description }}</p>
          <el-empty v-else description="暂无面试说明"></el-empty>
        </div>
      </el-card>
      
      <el-card class="notice-card">
        <template #header>
          <div class="card-header">
            <span>注意事项</span>
          </div>
        </template>
        
        <div class="notice-content">
          <ul class="notice-list">
            <li>请提前5分钟进入面试房间，确保网络和设备正常工作。</li>
            <li>面试过程中请保持安静的环境，避免干扰。</li>
            <li>准备好相关证件和简历，可能需要在面试中展示。</li>
            <li>面试全程请保持摄像头开启，确保面部清晰可见。</li>
            <li>如遇技术问题，请立即通过聊天功能联系面试官。</li>
          </ul>
        </div>
      </el-card>
      
      <div class="action-buttons">
        <el-button 
          v-if="interviewInfo.status === 'ONGOING'" 
          type="success" 
          @click="enterRoom"
        >
          进入面试
        </el-button>
        <el-button 
          v-if="interviewInfo.status === 'PENDING'" 
          type="danger" 
          @click="confirmCancel"
        >
          取消面试
        </el-button>
      </div>
    </div>
    
    <!-- 取消面试确认对话框 -->
    <el-dialog
      v-model="cancelDialogVisible"
      title="取消面试"
      width="30%"
    >
      <p>您确定要取消此次面试吗？此操作不可撤销。</p>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="cancelDialogVisible = false">取消</el-button>
          <el-button type="danger" @click="cancelInterview" :loading="cancelLoading">确认取消</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getInterviewDetail, cancelInterview as apiCancelInterview } from '@/api/interview'
import { ElMessage } from 'element-plus'
import dayjs from 'dayjs'

const route = useRoute()
const router = useRouter()
const interviewId = route.params.id

// 加载状态
const loading = ref(true)
const cancelLoading = ref(false)

// 面试信息
const interviewInfo = reactive({
  id: '',
  interviewNo: '',
  position: '',
  scheduledTime: '',
  roomCode: '',
  status: '',
  description: ''
})

// 取消对话框
const cancelDialogVisible = ref(false)

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

// 返回上一页
const goBack = () => {
  router.push('/candidate/dashboard')
}

// 格式化日期时间
const formatDateTime = (datetime) => {
  return dayjs(datetime).format('YYYY-MM-DD HH:mm')
}

// 进入面试房间
const enterRoom = () => {
  router.push(`/candidate/interview-room/${interviewInfo.roomCode}`)
}

// 确认取消面试
const confirmCancel = () => {
  cancelDialogVisible.value = true
}

// 取消面试
const cancelInterview = async () => {
  try {
    cancelLoading.value = true
    
    await apiCancelInterview(interviewId)
    
    ElMessage.success('面试已取消')
    cancelDialogVisible.value = false
    
    // 刷新页面数据
    loadInterviewDetail()
  } catch (error) {
    console.error('取消面试失败:', error)
    ElMessage.error('取消面试失败')
  } finally {
    cancelLoading.value = false
  }
}

// 加载面试详情
const loadInterviewDetail = async () => {
  try {
    loading.value = true
    
    const res = await getInterviewDetail(interviewId)
    
    if (res.data) {
      const data = res.data
      
      interviewInfo.id = data.id
      interviewInfo.interviewNo = data.interviewNo
      interviewInfo.position = data.position
      interviewInfo.scheduledTime = data.scheduledTime
      interviewInfo.roomCode = data.roomCode
      interviewInfo.status = data.status
      interviewInfo.description = data.description
    }
  } catch (error) {
    console.error('加载面试详情失败:', error)
    ElMessage.error('加载面试详情失败')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadInterviewDetail()
})
</script>

<style scoped>
.detail-container {
  padding: 20px 0;
}

.page-title {
  font-size: 18px;
  font-weight: bold;
}

.detail-content {
  margin-top: 20px;
}

.info-card,
.desc-card,
.notice-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.description-content {
  line-height: 1.8;
  color: #303133;
  padding: 10px 0;
}

.notice-list {
  padding-left: 20px;
}

.notice-list li {
  margin-bottom: 10px;
  line-height: 1.6;
}

.action-buttons {
  display: flex;
  justify-content: center;
  margin-top: 30px;
  gap: 20px;
}
</style> 