<template>
  <div class="my-interviews-container">
    <div class="page-header">
      <h2>我的面试</h2>
      <div class="header-stats">
        <el-tag type="warning">待确认: {{ stats.pending }}</el-tag>
        <el-tag type="success">已确认: {{ stats.confirmed }}</el-tag>
        <el-tag type="primary">进行中: {{ stats.ongoing }}</el-tag>
        <el-tag>已完成: {{ stats.completed }}</el-tag>
      </div>
    </div>

    <!-- 即将开始的面试 -->
    <el-card class="upcoming-card" v-if="upcomingInterview">
      <template #header>
        <div class="card-header">
          <span>即将开始的面试</span>
          <el-tag type="warning">距离开始还有 {{ getTimeRemaining(upcomingInterview.scheduledTime) }}</el-tag>
        </div>
      </template>

      <div class="upcoming-content">
        <el-row :gutter="20">
          <el-col :span="6">
            <div class="info-item">
              <div class="info-label">面试编号</div>
              <div class="info-value">{{ upcomingInterview.interviewNo }}</div>
            </div>
          </el-col>
          <el-col :span="6">
            <div class="info-item">
              <div class="info-label">应聘职位</div>
              <div class="info-value">{{ upcomingInterview.position }}</div>
            </div>
          </el-col>
          <el-col :span="6">
            <div class="info-item">
              <div class="info-label">面试时间</div>
              <div class="info-value">{{ formatDateTime(upcomingInterview.scheduledTime) }}</div>
            </div>
          </el-col>
          <el-col :span="6">
            <div class="info-item">
              <div class="info-label">状态</div>
              <div class="info-value">
                <el-tag :type="getStatusType(upcomingInterview.status)">
                  {{ getStatusText(upcomingInterview.status) }}
                </el-tag>
              </div>
            </div>
          </el-col>
        </el-row>

        <div class="upcoming-actions">
          <el-button
              v-if="upcomingInterview.status === 'CONFIRMED' && canEnterRoom(upcomingInterview)"
              type="success"
              size="large"
              @click="enterRoom(upcomingInterview)"
          >
            进入面试房间
          </el-button>
          <el-button
              v-else-if="upcomingInterview.status === 'SCHEDULED'"
              type="primary"
              @click="handleConfirm(upcomingInterview)"
          >
            确认参加
          </el-button>
        </div>
      </div>
    </el-card>

    <!-- 面试列表 -->
    <el-card class="interview-list-card">
      <template #header>
        <div class="card-header">
          <span>面试列表</span>
        </div>
      </template>

      <el-table
          :data="interviewList"
          v-loading="loading"
          style="width: 100%"
      >
        <el-table-column prop="interviewNo" label="面试编号" width="150" />
        <el-table-column prop="position" label="应聘职位" min-width="200" />
        <el-table-column prop="scheduledTime" label="面试时间" width="180">
          <template #default="scope">
            {{ formatDateTime(scope.row.scheduledTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="duration" label="时长" width="100">
          <template #default="scope">
            {{ scope.row.duration }}分钟
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="120">
          <template #default="scope">
            <el-tag :type="getStatusType(scope.row.status)">
              {{ getStatusText(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="280" fixed="right">
          <template #default="scope">
            <el-button
                v-if="scope.row.status === 'SCHEDULED'"
                type="primary"
                link
                @click="handleConfirm(scope.row)"
            >
              确认参加
            </el-button>
            <el-button
                v-if="scope.row.status === 'SCHEDULED'"
                type="danger"
                link
                @click="handleReject(scope.row)"
            >
              拒绝面试
            </el-button>
            <el-button
                v-if="scope.row.status === 'CONFIRMED' && canEnterRoom(scope.row)"
                type="success"
                link
                @click="enterRoom(scope.row)"
            >
              进入房间
            </el-button>
            <el-button
                v-if="scope.row.status === 'COMPLETED'"
                type="info"
                link
                @click="viewResult(scope.row)"
            >
              查看结果
            </el-button>
            <el-button
                type="primary"
                link
                @click="viewDetail(scope.row)"
            >
              查看详情
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-container">
        <el-pagination
            v-model="currentPage"
            :current-page="currentPage"
            :page-size="pageSize"
            :page-sizes="[10, 20, 50]"
            :total="total"
            layout="total, sizes, prev, pager, next, jumper"
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <!-- 确认/拒绝面试对话框 -->
    <el-dialog
        v-model="confirmDialogVisible"
        :title="confirmType === 'confirm' ? '确认参加面试' : '拒绝面试'"
        width="500px"
    >
      <el-form :model="confirmForm" label-width="80px">
        <el-form-item label="面试编号">
          <el-input v-model="currentInterview.interviewNo" disabled />
        </el-form-item>
        <el-form-item label="应聘职位">
          <el-input v-model="currentInterview.position" disabled />
        </el-form-item>
        <el-form-item label="面试时间">
          <el-input :value="formatDateTime(currentInterview.scheduledTime)" disabled />
        </el-form-item>
        <el-form-item
            v-if="confirmType === 'reject'"
            label="拒绝理由"
            prop="reason"
        >
          <el-input
              v-model="confirmForm.reason"
              type="textarea"
              :rows="3"
              placeholder="请输入拒绝理由"
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <span class="dialog-footer">
          <el-button @click="confirmDialogVisible = false">取消</el-button>
          <el-button
              :type="confirmType === 'confirm' ? 'primary' : 'danger'"
              @click="submitConfirm"
              :loading="confirmLoading"
          >
            确认
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/userStore'
import { ElMessage } from 'element-plus'
import { getCandidateInterviews } from '@/api/interview'
import { confirmInterview } from '@/api/interview-process'
import dayjs from 'dayjs'

const router = useRouter()
const userStore = useUserStore()

// 统计数据
const stats = reactive({
  pending: 0,
  confirmed: 0,
  ongoing: 0,
  completed: 0
})

// 分页参数
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

// 列表数据
const interviewList = ref([])
const loading = ref(false)

// 确认对话框
const confirmDialogVisible = ref(false)
const confirmLoading = ref(false)
const confirmType = ref('confirm') // confirm or reject
const currentInterview = ref({})
const confirmForm = reactive({
  reason: ''
})

// 即将开始的面试
const upcomingInterview = computed(() => {
  const now = dayjs()
  const upcoming = interviewList.value
      .filter(item =>
          (item.status === 'SCHEDULED' || item.status === 'CONFIRMED') &&
          dayjs(item.scheduledTime).isAfter(now)
      )
      .sort((a, b) => dayjs(a.scheduledTime).diff(dayjs(b.scheduledTime)))

  return upcoming[0] || null
})

// 获取距离开始的时间
const getTimeRemaining = (scheduledTime) => {
  const now = dayjs()
  const scheduled = dayjs(scheduledTime)
  const diffMinutes = scheduled.diff(now, 'minute')

  if (diffMinutes < 60) {
    return `${diffMinutes}分钟`
  } else if (diffMinutes < 1440) {
    const hours = Math.floor(diffMinutes / 60)
    const minutes = diffMinutes % 60
    return `${hours}小时${minutes}分钟`
  } else {
    const days = Math.floor(diffMinutes / 1440)
    return `${days}天`
  }
}

// 是否可以进入房间
const canEnterRoom = (interview) => {
  const now = dayjs()
  const scheduled = dayjs(interview.scheduledTime)
  const diffMinutes = scheduled.diff(now, 'minute')

  // 提前10分钟可以进入
  return diffMinutes <= 10 && diffMinutes >= -interview.duration
}

// 格式化日期时间
const formatDateTime = (datetime) => {
  return dayjs(datetime).format('YYYY-MM-DD HH:mm')
}

// 获取状态类型
const getStatusType = (status) => {
  const typeMap = {
    'SCHEDULED': 'warning',
    'CONFIRMED': 'primary',
    'IN_PROGRESS': 'success',
    'COMPLETED': 'info',
    'CANCELLED': 'danger',
    'REJECTED': 'danger'
  }
  return typeMap[status] || 'info'
}

// 获取状态文本
const getStatusText = (status) => {
  const textMap = {
    'SCHEDULED': '待确认',
    'CONFIRMED': '已确认',
    'IN_PROGRESS': '进行中',
    'COMPLETED': '已完成',
    'CANCELLED': '已取消',
    'REJECTED': '已拒绝'
  }
  return textMap[status] || status
}

// 查看详情
const viewDetail = (interview) => {
  router.push(`/candidate/interview/${interview.id}`)
}

// 查看结果
const viewResult = (interview) => {
  router.push(`/candidate/interview-result/${interview.id}`)
}

// 进入房间
const enterRoom = async (interview) => {
  try {
    // 先调用开始面试接口
    await startInterview(interview.id)

    // 跳转到面试房间
    router.push(`/candidate/interview-room/${interview.roomCode}`)
  } catch (error) {
    console.error('进入房间失败:', error)
    ElMessage.error('进入房间失败')
  }
}

// 确认参加
const handleConfirm = (interview) => {
  confirmType.value = 'confirm'
  currentInterview.value = interview
  confirmForm.reason = ''
  confirmDialogVisible.value = true
}

// 拒绝面试
const handleReject = (interview) => {
  confirmType.value = 'reject'
  currentInterview.value = interview
  confirmForm.reason = ''
  confirmDialogVisible.value = true
}

// 提交确认
const submitConfirm = async () => {
  if (confirmType.value === 'reject' && !confirmForm.reason.trim()) {
    ElMessage.warning('请输入拒绝理由')
    return
  }

  try {
    confirmLoading.value = true

    await confirmInterview(
        currentInterview.value.id,
        confirmType.value === 'confirm',
        confirmForm.reason
    )

    ElMessage.success(confirmType.value === 'confirm' ? '已确认参加面试' : '已拒绝面试')
    confirmDialogVisible.value = false

    // 刷新列表
    loadInterviewList()
  } catch (error) {
    console.error('操作失败:', error)
    ElMessage.error('操作失败')
  } finally {
    confirmLoading.value = false
  }
}

// 分页大小改变
const handleSizeChange = (val) => {
  pageSize.value = val
  currentPage.value = 1
  loadInterviewList()
}

// 页码改变
const handleCurrentChange = (val) => {
  currentPage.value = val
  loadInterviewList()
}

// 加载面试列表
const loadInterviewList = async () => {
  try {
    loading.value = true

    const userId = userStore.userInfo?.id
    if (!userId) {
      ElMessage.warning('请先登录')
      return
    }

    const res = await getCandidateInterviews(userId)

    if (res.data) {
      interviewList.value = res.data || []
      total.value = res.data.length || 0

      // 计算统计数据
      stats.pending = interviewList.value.filter(item => item.status === 'SCHEDULED').length
      stats.confirmed = interviewList.value.filter(item => item.status === 'CONFIRMED').length
      stats.ongoing = interviewList.value.filter(item => item.status === 'IN_PROGRESS').length
      stats.completed = interviewList.value.filter(item => item.status === 'COMPLETED').length
    }
  } catch (error) {
    console.error('获取面试列表失败:', error)
    ElMessage.error('获取面试列表失败')
  } finally {
    loading.value = false
  }
}

// 开始面试（需要实现）
const startInterview = async (interviewId) => {
  // 这里需要调用开始面试的API
  // 暂时留空，等待后端接口
  console.log('开始面试:', interviewId)
}

onMounted(() => {
  loadInterviewList()
})
</script>

<style scoped>
.my-interviews-container {
  padding: 20px 0;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.header-stats {
  display: flex;
  gap: 10px;
}

.upcoming-card {
  margin-bottom: 20px;
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.upcoming-content {
  padding: 20px 0;
}

.info-item {
  text-align: center;
}

.info-label {
  color: #909399;
  font-size: 14px;
  margin-bottom: 5px;
}

.info-value {
  color: #303133;
  font-size: 16px;
  font-weight: 500;
}

.upcoming-actions {
  text-align: center;
  margin-top: 30px;
}

.interview-list-card {
  margin-bottom: 20px;
}

.pagination-container {
  margin-top: 20px;
  text-align: right;
}
</style> 