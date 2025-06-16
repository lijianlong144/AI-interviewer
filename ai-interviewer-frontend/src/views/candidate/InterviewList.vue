<template>
  <div class="interview-list-container">
    <h2 class="page-title">我的面试</h2>
    
    <!-- 状态统计卡片 -->
    <div class="status-summary">
      <el-card class="status-card">
        <div class="status-item">
          <div class="status-label">待面试</div>
          <div class="status-count pending">{{ pendingCount }}</div>
        </div>
      </el-card>
      <el-card class="status-card">
        <div class="status-item">
          <div class="status-label">已完成</div>
          <div class="status-count completed">{{ completedCount }}</div>
        </div>
      </el-card>
      <el-card class="status-card">
        <div class="status-item">
          <div class="status-label">已取消</div>
          <div class="status-count cancelled">{{ cancelledCount }}</div>
        </div>
      </el-card>
    </div>
    
    <!-- 面试列表 -->
    <el-card class="table-card">
      <div class="filter-container">
        <el-input
          v-model="searchKeyword"
          placeholder="搜索面试职位或编号"
          clearable
          class="search-input"
          @keyup.enter="handleSearch"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
        
        <el-select v-model="statusFilter" placeholder="状态筛选" clearable class="status-select">
          <el-option label="待开始" value="PENDING" />
          <el-option label="进行中" value="ONGOING" />
          <el-option label="已完成" value="COMPLETED" />
          <el-option label="已取消" value="CANCELLED" />
        </el-select>
        
        <el-date-picker
          v-model="dateRange"
          type="daterange"
          range-separator="至"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
          class="date-picker"
        />
        
        <el-button type="primary" @click="handleSearch" :icon="Search">搜索</el-button>
      </div>
      
      <el-table
        :data="interviewList"
        style="width: 100%"
        :row-class-name="tableRowClassName"
        v-loading="loading"
      >
        <el-table-column prop="interviewNo" label="面试编号" min-width="120" />
        <el-table-column prop="position" label="职位" min-width="120" />
        <el-table-column label="面试时间" min-width="150">
          <template #default="{ row }">
            {{ formatDateTime(row.scheduledTime) }}
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)" effect="light">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="280" fixed="right">
          <template #default="{ row }">
            <el-button
              v-if="row.status === 'COMPLETED'"
              type="success"
              size="small"
              @click="viewResult(row.id)"
              :icon="Document"
            >
              查看结果
            </el-button>
            <el-button
              type="primary"
              size="small"
              @click="openRoomCodeDialog()"
              :icon="VideoPlay"
            >
              去面试
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <div class="pagination-container">
        <el-pagination
          :current-page="currentPage"
          :page-size="pageSize"
          :page-sizes="[10, 20, 30, 50]"
          layout="total, sizes, prev, pager, next, jumper"
          :total="total"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>
    
    <!-- 输入Room Code弹窗 -->
    <el-dialog
      v-model="roomCodeDialogVisible"
      title="进入面试房间"
      width="400px"
      center
    >
      <div class="room-code-dialog">
        <p class="dialog-tip">请输入面试房间码进入AI面试</p>
        <el-input
          v-model="roomCode"
          placeholder="请输入房间码"
          clearable
          class="room-code-input"
        >
          <template #prefix>
            <el-icon><Key /></el-icon>
          </template>
        </el-input>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="roomCodeDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="enterInterviewRoom">进入面试</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Search, Document, VideoPlay, Key } from '@element-plus/icons-vue'
import dayjs from 'dayjs'
import { getCandidateInterviewPage, getInterviewByRoomCode } from '@/api/interview'
import request from '@/utils/request'

const router = useRouter()

// 搜索和筛选
const searchKeyword = ref('')
const statusFilter = ref('')
const dateRange = ref([])

// 分页
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

// 数据
const interviewList = ref([])
const loading = ref(false)

// Room Code弹窗
const roomCodeDialogVisible = ref(false)
const roomCode = ref('')

// 计算统计数据
const pendingCount = computed(() => {
  return interviewList.value.filter(item => item.status === 'PENDING').length
})

const completedCount = computed(() => {
  return interviewList.value.filter(item => item.status === 'COMPLETED').length
})

const cancelledCount = computed(() => {
  return interviewList.value.filter(item => item.status === 'CANCELLED').length
})

// 格式化日期时间
const formatDateTime = (datetime) => {
  if (!datetime) return '-'
  return dayjs(datetime).format('YYYY-MM-DD HH:mm')
}

// 获取状态标签类型
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

// 加载面试列表
const loadInterviewList = async () => {
  try {
    loading.value = true
    const params = {
      current: currentPage.value,
      size: pageSize.value
    }
    
    if (searchKeyword.value) {
      params.keyword = searchKeyword.value
    }
    
    if (statusFilter.value) {
      params.status = statusFilter.value
    }
    
    if (dateRange.value && dateRange.value.length === 2) {
      params.startDate = dateRange.value[0]
      params.endDate = dateRange.value[1]
    }
    
    const response = await getCandidateInterviewPage(params)
    
    if (response.success) {
      interviewList.value = response.data.records || []
      total.value = response.data.total || 0
    } else {
      ElMessage.error(response.message || '获取面试列表失败')
    }
  } catch (error) {
    console.error('获取面试列表失败:', error)
    ElMessage.error('获取面试列表失败')
  } finally {
    loading.value = false
  }
}

// 搜索处理
const handleSearch = () => {
  currentPage.value = 1
  loadInterviewList()
}

// 分页大小变化
const handleSizeChange = (val) => {
  pageSize.value = val
  loadInterviewList()
}

// 页码变化
const handleCurrentChange = (val) => {
  currentPage.value = val
  loadInterviewList()
}

// 查看结果
const viewResult = (interviewId) => {
  router.push(`/candidate/interview-result/${interviewId}`)
}

// 打开Room Code弹窗
const openRoomCodeDialog = () => {
  roomCode.value = ''
  roomCodeDialogVisible.value = true
}

// 进入面试房间
const enterInterviewRoom = async () => {
  if (!roomCode.value) {
    ElMessage.warning('请输入房间码')
    return
  }
  
  try {
    loading.value = true
    const response = await getInterviewByRoomCode(roomCode.value)
    
    if (response.success && response.data) {
      // 跳转到面试房间
      router.push(`/interview-room/${roomCode.value}`)
      roomCodeDialogVisible.value = false
    } else {
      ElMessage.error(response.message || '房间码无效，请重新输入')
    }
  } catch (error) {
    console.error('进入面试房间失败:', error)
    ElMessage.error('房间码无效或面试不存在')
  } finally {
    loading.value = false
  }
}

// 表格行样式
const tableRowClassName = ({ row }) => {
  if (row.status === 'ONGOING') {
    return 'ongoing-row';
  } else if (row.status === 'PENDING') {
    return 'pending-row';
  } else if (row.status === 'COMPLETED') {
    return 'completed-row';
  } else if (row.status === 'CANCELLED') {
    return 'cancelled-row';
  }
  return '';
}

// 初始化
onMounted(() => {
  loadInterviewList()
})
</script>

<style scoped>
.interview-list-container {
  padding: 20px;
}

.page-title {
  margin-bottom: 20px;
}

.status-summary {
  display: flex;
  gap: 20px;
  margin-bottom: 20px;
}

.status-card {
  flex: 1;
  text-align: center;
  transition: all 0.3s ease;
  border-radius: 8px;
  overflow: hidden;
}

.status-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 10px 20px rgba(0, 0, 0, 0.1);
}

.status-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 15px;
}

.status-label {
  font-size: 16px;
  color: #606266;
  margin-bottom: 10px;
}

.status-count {
  font-size: 28px;
  font-weight: bold;
}

.status-count.pending {
  color: #409EFF;
}

.status-count.completed {
  color: #67C23A;
}

.status-count.cancelled {
  color: #F56C6C;
}

.table-card {
  margin-bottom: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.filter-container {
  display: flex;
  gap: 15px;
  margin-bottom: 20px;
  flex-wrap: wrap;
  padding: 15px;
  background-color: #f9f9f9;
  border-radius: 8px;
}

.search-input {
  width: 300px;
}

.status-select {
  width: 150px;
}

.date-picker {
  width: 350px;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

/* Room Code弹窗样式 */
.room-code-dialog {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 10px 0;
}

.dialog-tip {
  margin-bottom: 20px;
  color: #606266;
}

.room-code-input {
  width: 100%;
  margin-bottom: 10px;
}

@media (max-width: 768px) {
  .status-summary {
    flex-direction: column;
    gap: 10px;
  }
  
  .filter-container {
    flex-direction: column;
  }
  
  .search-input,
  .status-select,
  .date-picker {
    width: 100%;
  }
}
</style> 