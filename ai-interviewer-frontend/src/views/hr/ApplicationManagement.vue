<template>
  <div class="application-management-container">
    <div class="page-header">
      <h2>申请管理</h2>
      <div class="header-stats">
        <el-tag type="info">待处理: {{ stats.pending }}</el-tag>
        <el-tag type="warning">审核中: {{ stats.reviewing }}</el-tag>
        <el-tag type="success">已通过: {{ stats.passed }}</el-tag>
        <el-tag type="danger">已拒绝: {{ stats.rejected }}</el-tag>
      </div>
    </div>

    <!-- 搜索筛选 -->
    <el-card class="search-card">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="候选人">
          <el-input
              v-model="searchForm.candidateName"
              placeholder="候选人姓名"
              clearable
          />
        </el-form-item>
        <el-form-item label="职位">
          <el-select v-model="searchForm.positionId" placeholder="选择职位" clearable>
            <el-option
                v-for="position in positionList"
                :key="position.id"
                :label="position.title"
                :value="position.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="选择状态" clearable>
            <el-option label="待处理" value="PENDING" />
            <el-option label="审核中" value="REVIEWING" />
            <el-option label="已通过" value="PASSED" />
            <el-option label="已拒绝" value="REJECTED" />
            <el-option label="已安排面试" value="INTERVIEW_SCHEDULED" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 申请列表 -->
    <el-card class="list-card">
      <el-table
          :data="applicationList"
          v-loading="loading"
          style="width: 100%"
          @row-click="viewDetail"
      >
        <el-table-column prop="applicationNo" label="申请编号" width="150" />
        <el-table-column label="候选人" width="120">
          <template #default="scope">
            {{ scope.row.candidateName || scope.row.candidateRealName || '-' }}
          </template>
        </el-table-column>
        <el-table-column label="申请职位" min-width="200">
          <template #default="scope">
            {{ scope.row.positionTitle || scope.row.positionName || scope.row.position || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="applyTime" label="申请时间" width="180">
          <template #default="scope">
            {{ formatDateTime(scope.row.applyTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="120">
          <template #default="scope">
            <el-tag :type="getStatusType(scope.row.status)">
              {{ getStatusText(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" min-width="280" fixed="right" align="center">
          <template #default="scope">
            <div class="action-buttons">
              <el-button
                  type="primary"
                  size="small"
                  @click.stop="viewDetail(scope.row)"
              >
                <el-icon><View /></el-icon>
                查看详情
              </el-button>
              <el-button
                  v-if="scope.row.status === 'PENDING'"
                  type="success"
                  size="small"
                  @click.stop="screenApplication(scope.row, 'PASSED')"
              >
                <el-icon><Check /></el-icon>
                通过
              </el-button>
              <el-button
                  v-if="scope.row.status === 'PENDING'"
                  type="danger"
                  size="small"
                  @click.stop="screenApplication(scope.row, 'REJECTED')"
              >
                <el-icon><Close /></el-icon>
                拒绝
              </el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-container">
        <el-pagination
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

    <!-- 初筛对话框 -->
    <el-dialog
        v-model="screenDialogVisible"
        :title="screenStatus === 'PASSED' ? '通过初筛' : '拒绝申请'"
        width="500px"
    >
      <el-form :model="screenForm" label-width="80px">
        <el-form-item label="备注" prop="remark">
          <el-input
              v-model="screenForm.remark"
              type="textarea"
              :rows="4"
              :placeholder="screenStatus === 'PASSED' ? '请输入通过理由或备注信息' : '请输入拒绝理由'"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="screenDialogVisible = false">取消</el-button>
          <el-button
              :type="screenStatus === 'PASSED' ? 'success' : 'danger'"
              @click="confirmScreen"
              :loading="screenLoading"
          >
            确认
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getApplicationPage, updateApplicationStatus, getApplicationStats } from '@/api/application'
import { getPositionPage } from '@/api/position'
import dayjs from 'dayjs'
import { View, Check, Close } from '@element-plus/icons-vue'

const router = useRouter()

// 搜索表单
const searchForm = reactive({
  candidateName: '',
  positionId: null,
  status: ''
})

// 统计数据
const stats = reactive({
  pending: 0,
  reviewing: 0,
  passed: 0,
  rejected: 0
})

// 分页参数
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

// 列表数据
const applicationList = ref([])
const positionList = ref([])
const loading = ref(false)

// 初筛对话框
const screenDialogVisible = ref(false)
const screenLoading = ref(false)
const currentApplication = ref(null)
const screenStatus = ref('')
const screenForm = reactive({
  remark: ''
})

// 格式化日期时间
const formatDateTime = (datetime) => {
  return dayjs(datetime).format('YYYY-MM-DD HH:mm')
}

// 获取状态类型
const getStatusType = (status) => {
  const typeMap = {
    'PENDING': 'info',
    'REVIEWING': 'warning',
    'PASSED': 'success',
    'REJECTED': 'danger',
    'INTERVIEW_SCHEDULED': 'primary'
  }
  return typeMap[status] || 'info'
}

// 获取状态文本
const getStatusText = (status) => {
  const textMap = {
    'PENDING': '待处理',
    'REVIEWING': '审核中',
    'PASSED': '已通过',
    'REJECTED': '已拒绝',
    'INTERVIEW_SCHEDULED': '已安排面试'
  }
  return textMap[status] || status
}

// 查看详情
const viewDetail = (row) => {
  router.push(`/hr/application/${row.id}`)
}

// 初筛申请
const screenApplication = (application, status) => {
  currentApplication.value = application
  screenStatus.value = status
  screenForm.remark = ''
  screenDialogVisible.value = true
}

// 确认初筛
const confirmScreen = async () => {
  try {
    screenLoading.value = true

    const response = await updateApplicationStatus(
        currentApplication.value.id,
        screenStatus.value,
        screenForm.remark
    )

    if (screenStatus.value === 'PASSED') {
      ElMessage({
        message: '已通过初筛，系统已自动创建面试',
        type: 'success',
        duration: 3000
      })
    } else {
      ElMessage.success('已拒绝申请')
    }
    
    screenDialogVisible.value = false

    // 刷新列表
    loadApplicationList()
    loadStats()
  } catch (error) {
    console.error('操作失败:', error)
    ElMessage.error('操作失败')
  } finally {
    screenLoading.value = false
  }
}

// 搜索
const handleSearch = () => {
  currentPage.value = 1
  loadApplicationList()
}

// 重置搜索
const resetSearch = () => {
  searchForm.candidateName = ''
  searchForm.positionId = null
  searchForm.status = ''
  handleSearch()
}

// 分页大小改变
const handleSizeChange = (val) => {
  pageSize.value = val
  currentPage.value = 1
  loadApplicationList()
}

// 页码改变
const handleCurrentChange = (val) => {
  currentPage.value = val
  loadApplicationList()
}

// 加载申请列表
const loadApplicationList = async () => {
  try {
    loading.value = true

    const params = {
      current: currentPage.value,
      size: pageSize.value
    }

    // 添加搜索条件
    if (searchForm.candidateName) {
      params.candidateName = searchForm.candidateName
    }
    if (searchForm.positionId) {
      params.positionId = searchForm.positionId
    }
    if (searchForm.status) {
      params.status = searchForm.status
    }

    const res = await getApplicationPage(params)

    if (res.data) {
      applicationList.value = res.data.records || []
      total.value = res.data.total || 0
    }
  } catch (error) {
    console.error('获取申请列表失败:', error)
    ElMessage.error('获取申请列表失败')
  } finally {
    loading.value = false
  }
}

// 加载职位列表
const loadPositionList = async () => {
  try {
    const res = await getPositionPage({ current: 1, size: 100, status: 1 })
    if (res.data) {
      positionList.value = res.data.records || []
    }
  } catch (error) {
    console.error('获取职位列表失败:', error)
  }
}

// 加载统计数据
const loadStats = async () => {
  try {
    const res = await getApplicationStats()
    if (res.data) {
      Object.assign(stats, res.data)
    }
  } catch (error) {
    console.error('获取统计数据失败:', error)
  }
}

onMounted(() => {
  loadApplicationList()
  loadPositionList()
  loadStats()
})
</script>

<style scoped>
.application-management-container {
  padding: 20px;
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

.search-card {
  margin-bottom: 20px;
}

.search-form {
  padding: 10px 0;
}

.list-card {
  margin-bottom: 20px;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.el-table {
  cursor: pointer;
}

.action-buttons {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  justify-content: center;
}

.action-buttons .el-button {
  margin: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 4px;
  padding: 6px 10px;
  font-size: 12px;
}
</style>