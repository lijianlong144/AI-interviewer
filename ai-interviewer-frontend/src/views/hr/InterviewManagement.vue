<template>
  <div class="interview-management-container">
    <div class="page-title">
      <h2>面试管理</h2>
    </div>
    
    <div class="search-filter-container">
      <div class="search-box">
        <el-input
          v-model="searchKeyword"
          placeholder="搜索面试编号/候选人/职位"
          class="search-input"
          clearable
          @clear="handleSearch"
          @keyup.enter="handleSearch"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
      </div>
      
      <div class="filter-box">
        <el-select v-model="statusFilter" placeholder="面试状态" clearable @change="handleSearch" class="status-select">
          <el-option label="全部" value="" />
          <el-option label="待开始" value="PENDING" />
          <el-option label="进行中" value="ONGOING" />
          <el-option label="已完成" value="COMPLETED" />
          <el-option label="已取消" value="CANCELLED" />
        </el-select>
      </div>
      
      <div class="date-range-box">
        <el-date-picker
          v-model="dateRange"
          type="daterange"
          range-separator="至"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
          format="YYYY-MM-DD"
          value-format="YYYY-MM-DD"
          @change="handleSearch"
          class="date-picker"
        />
      </div>
      
      <div class="action-box">
        <el-button type="primary" @click="showCreateDialog">创建面试</el-button>
      </div>
    </div>

    <el-card class="table-card">
      <el-table
        v-loading="loading"
        :data="interviewList"
        border
        style="width: 100%"
        :fit="true"
        :header-cell-style="{background:'#f5f7fa'}"
        :cell-style="{padding: '8px 0'}"
      >
        <el-table-column prop="interviewNo" label="面试编号" width="120" show-overflow-tooltip />
        <el-table-column prop="candidateName" label="候选人" width="90" align="center" />
        <el-table-column prop="position" label="应聘职位" width="120" show-overflow-tooltip />
        <el-table-column prop="interviewerName" label="面试官" width="90" align="center" />
        <el-table-column prop="scheduledTime" label="预约时间" width="140" align="center">
          <template #default="scope">
            {{ formatDateTime(scope.row.scheduledTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="80" align="center">
          <template #default="scope">
            <el-tag :type="getStatusType(scope.row.status)" size="small">
              {{ getStatusText(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" fixed="right" width="220" align="center">
          <template #default="scope">
            <el-button
              link
              type="primary"
              size="small"
              @click="handleView(scope.row)"
            >
              查看
            </el-button>
            <el-button
              v-if="scope.row.status === 'PENDING'"
              link
              type="primary"
              size="small"
              @click="handleEdit(scope.row)"
            >
              编辑
            </el-button>
            <el-button
              v-if="scope.row.status === 'PENDING'"
              link
              type="success"
              size="small"
              @click="handleStart(scope.row)"
            >
              开始面试
            </el-button>
            <el-button
              v-if="scope.row.status === 'ONGOING'"
              link
              type="warning"
              size="small"
              @click="handleEnd(scope.row)"
            >
              结束面试
            </el-button>
            <el-button
              v-if="scope.row.status === 'PENDING'"
              link
              type="danger"
              size="small"
              @click="handleCancel(scope.row)"
            >
              取消面试
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-container">
        <el-pagination
          :current-page="currentPage"
          :page-size="pageSize"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          :total="total"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
          @update:current-page="currentPage = $event"
          @update:page-size="pageSize = $event"
        />
      </div>
    </el-card>

    <!-- 创建/编辑面试对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑面试' : '创建面试'"
      width="600px"
      @closed="resetForm"
    >
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="100px"
      >
        <el-form-item label="候选人" prop="candidateId" v-if="!isEdit">
          <el-select
            v-model="form.candidateId"
            filterable
            remote
            reserve-keyword
            placeholder="请输入候选人姓名"
            :remote-method="searchCandidates"
            :loading="candidateLoading"
          >
            <el-option
              v-for="item in candidateOptions"
              :key="item.id"
              :label="item.realName"
              :value="item.id"
            >
              <div class="candidate-option">
                <span>{{ item.realName }}</span>
                <span class="candidate-info">{{ item.username }}</span>
              </div>
            </el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="候选人" v-else>
          <el-input v-model="form.candidateName" disabled />
        </el-form-item>
        <el-form-item label="应聘职位" prop="position">
          <el-input v-model="form.position" />
        </el-form-item>
        <el-form-item label="面试官" prop="interviewerId">
          <el-select
            v-model="form.interviewerId"
            filterable
            remote
            reserve-keyword
            placeholder="请输入面试官姓名"
            :remote-method="searchInterviewers"
            :loading="interviewerLoading"
          >
            <el-option
              v-for="item in interviewerOptions"
              :key="item.id"
              :label="item.realName"
              :value="item.id"
            >
              <div class="interviewer-option">
                <span>{{ item.realName }}</span>
                <span class="interviewer-info">{{ item.username }}</span>
              </div>
            </el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="预约时间" prop="scheduledTime">
          <el-date-picker
            v-model="form.scheduledTime"
            type="datetime"
            placeholder="选择日期时间"
            format="YYYY-MM-DD HH:mm"
            value-format="YYYY-MM-DDT00:00:00"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitForm">确认</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 面试详情对话框 -->
    <el-dialog
      v-model="detailDialogVisible"
      title="面试详情"
      width="700px"
    >
      <el-descriptions
        v-if="currentInterview"
        :column="2"
        border
      >
        <el-descriptions-item label="面试编号">{{ currentInterview.interviewNo }}</el-descriptions-item>
        <el-descriptions-item label="候选人">{{ currentInterview.candidateName }}</el-descriptions-item>
        <el-descriptions-item label="应聘职位">{{ currentInterview.position }}</el-descriptions-item>
        <el-descriptions-item label="面试官">{{ currentInterview.interviewerName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="预约时间">{{ formatDateTime(currentInterview.scheduledTime) }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="getStatusType(currentInterview.status)">
            {{ getStatusText(currentInterview.status) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="开始时间">{{ currentInterview.startTime ? formatDateTime(currentInterview.startTime) : '-' }}</el-descriptions-item>
        <el-descriptions-item label="结束时间">{{ currentInterview.endTime ? formatDateTime(currentInterview.endTime) : '-' }}</el-descriptions-item>
        <el-descriptions-item label="面试时长">{{ currentInterview.duration ? `${currentInterview.duration}分钟` : '-' }}</el-descriptions-item>
        <el-descriptions-item label="面试评分">{{ currentInterview.totalScore || '-' }}</el-descriptions-item>
        <el-descriptions-item label="房间号">{{ currentInterview.roomCode || '-' }}</el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ formatDateTime(currentInterview.createTime) }}</el-descriptions-item>
      </el-descriptions>

      <div v-if="currentInterview && currentInterview.status === 'COMPLETED'" class="interview-result">
        <h3>面试结果</h3>
        <div class="result-content">
          <p v-if="!currentInterview.totalScore">暂无面试结果</p>
          <template v-else>
            <el-progress
              :percentage="(currentInterview.totalScore / 100) * 100"
              :format="percent => `${currentInterview.totalScore}分`"
              :color="getScoreColor(currentInterview.totalScore)"
            />
            <p class="result-comment">{{ getScoreComment(currentInterview.totalScore) }}</p>
          </template>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/utils/request'
import dayjs from 'dayjs'
import { 
  getInterviewPage, 
  createInterview, 
  updateInterview, 
  getInterviewDetail,
  startInterview,
  endInterview,
  cancelInterview
} from '@/api/interview'

// 搜索条件
const searchKeyword = ref('')
const statusFilter = ref('')
const dateRange = ref([])

// 分页参数
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

// 面试列表数据
const interviewList = ref([])
const loading = ref(false)

// 当前查看的面试
const currentInterview = ref(null)
const detailDialogVisible = ref(false)

// 创建/编辑面试对话框
const dialogVisible = ref(false)
const isEdit = ref(false)
const formRef = ref(null)
const form = reactive({
  id: null,
  candidateId: null,
  candidateName: '',
  position: '',
  interviewerId: null,
  scheduledTime: ''
})

// 表单验证规则
const rules = {
  candidateId: [{ required: true, message: '请选择候选人', trigger: 'change' }],
  position: [{ required: true, message: '请输入应聘职位', trigger: 'blur' }],
  interviewerId: [{ required: true, message: '请选择面试官', trigger: 'change' }],
  scheduledTime: [{ required: true, message: '请选择预约时间', trigger: 'change' }]
}

// 候选人和面试官选项
const candidateOptions = ref([])
const interviewerOptions = ref([])
const candidateLoading = ref(false)
const interviewerLoading = ref(false)

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

// 获取评分颜色
const getScoreColor = (score) => {
  if (score >= 80) return '#67C23A'
  if (score >= 60) return '#E6A23C'
  return '#F56C6C'
}

// 获取评分评语
const getScoreComment = (score) => {
  if (score >= 90) return '优秀，强烈推荐'
  if (score >= 80) return '良好，建议通过'
  if (score >= 70) return '一般，可以考虑'
  if (score >= 60) return '勉强及格，需谨慎考虑'
  return '不合格，不建议录用'
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
    
    const response = await getInterviewPage(params)
    
    if (response.success) {
      interviewList.value = response.data.records
      total.value = response.data.total
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

// 显示创建面试对话框
const showCreateDialog = () => {
  isEdit.value = false
  dialogVisible.value = true
}

// 重置表单
const resetForm = () => {
  formRef.value?.resetFields()
  Object.assign(form, {
    id: null,
    candidateId: null,
    candidateName: '',
    position: '',
    interviewerId: null,
    scheduledTime: ''
  })
}

// 搜索候选人
const searchCandidates = async (query) => {
  if (query) {
    candidateLoading.value = true
    try {
      const response = await request({
        url: '/user/page',
        method: 'get',
        params: {
          keyword: query,
          role: 'CANDIDATE',
          status: 1,
          size: 10
        }
      })
      
      if (response.success) {
        candidateOptions.value = response.data.records
      }
    } catch (error) {
      console.error('搜索候选人失败:', error)
    } finally {
      candidateLoading.value = false
    }
  } else {
    candidateOptions.value = []
  }
}

// 搜索面试官
const searchInterviewers = async (query) => {
  if (query) {
    interviewerLoading.value = true
    try {
      const response = await request({
        url: '/user/page',
        method: 'get',
        params: {
          keyword: query,
          role: 'INTERVIEWER',
          status: 1,
          size: 10
        }
      })
      
      if (response.success) {
        interviewerOptions.value = response.data.records
      }
    } catch (error) {
      console.error('搜索面试官失败:', error)
    } finally {
      interviewerLoading.value = false
    }
  } else {
    interviewerOptions.value = []
  }
}

// 提交表单
const submitForm = async () => {
  try {
    await formRef.value.validate()
    
    if (isEdit.value) {
      // 编辑面试
      const response = await updateInterview(form)
      
      if (response.success) {
        ElMessage.success('更新面试成功')
        dialogVisible.value = false
        loadInterviewList()
      } else {
        ElMessage.error(response.message || '更新面试失败')
      }
    } else {
      // 创建面试
      // 如果选择了候选人，获取候选人姓名
      if (form.candidateId) {
        const candidate = candidateOptions.value.find(item => item.id === form.candidateId)
        if (candidate) {
          form.candidateName = candidate.realName
        }
      }
      
      const response = await createInterview(form)
      
      if (response.success) {
        ElMessage.success('创建面试成功')
        dialogVisible.value = false
        loadInterviewList()
      } else {
        ElMessage.error(response.message || '创建面试失败')
      }
    }
  } catch (error) {
    console.error('提交表单失败:', error)
    ElMessage.error('表单验证失败或提交失败')
  }
}

// 查看面试详情
const handleView = async (row) => {
  try {
    const response = await getInterviewDetail(row.id)
    
    if (response.success) {
      currentInterview.value = response.data
      detailDialogVisible.value = true
    } else {
      ElMessage.error(response.message || '获取面试详情失败')
    }
  } catch (error) {
    console.error('获取面试详情失败:', error)
    ElMessage.error('获取面试详情失败')
  }
}

// 编辑面试
const handleEdit = (row) => {
  isEdit.value = true
  form.id = row.id
  form.candidateId = row.candidateId
  form.candidateName = row.candidateName
  form.position = row.position
  form.interviewerId = row.interviewerId
  form.scheduledTime = row.scheduledTime
  
  dialogVisible.value = true
}

// 开始面试
const handleStart = (row) => {
  ElMessageBox.confirm(
    `确定要开始面试 ${row.interviewNo} 吗？`,
    '开始面试',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'info'
    }
  ).then(async () => {
    try {
      const response = await startInterview(row.id)
      
      if (response.success) {
        ElMessage.success('面试已开始')
        loadInterviewList()
      } else {
        ElMessage.error(response.message || '开始面试失败')
      }
    } catch (error) {
      console.error('开始面试失败:', error)
      ElMessage.error('开始面试失败')
    }
  }).catch(() => {})
}

// 结束面试
const handleEnd = (row) => {
  ElMessageBox.confirm(
    `确定要结束面试 ${row.interviewNo} 吗？`,
    '结束面试',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      const response = await endInterview(row.id)
      
      if (response.success) {
        ElMessage.success('面试已结束')
        loadInterviewList()
      } else {
        ElMessage.error(response.message || '结束面试失败')
      }
    } catch (error) {
      console.error('结束面试失败:', error)
      ElMessage.error('结束面试失败')
    }
  }).catch(() => {})
}

// 取消面试
const handleCancel = (row) => {
  ElMessageBox.confirm(
    `确定要取消面试 ${row.interviewNo} 吗？`,
    '取消面试',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'danger'
    }
  ).then(async () => {
    try {
      const response = await cancelInterview(row.id)
      
      if (response.success) {
        ElMessage.success('面试已取消')
        loadInterviewList()
      } else {
        ElMessage.error(response.message || '取消面试失败')
      }
    } catch (error) {
      console.error('取消面试失败:', error)
      ElMessage.error('取消面试失败')
    }
  }).catch(() => {})
}

// 初始化加载数据
onMounted(() => {
  loadInterviewList()
})
</script>

<style scoped>
.interview-management-container {
  padding: 20px;
}

.page-title {
  margin-bottom: 20px;
}

.search-filter-container {
  display: flex;
  flex-wrap: wrap;
  gap: 15px;
  margin-bottom: 20px;
  align-items: center;
}

.search-box {
  flex: 2;
  min-width: 300px;
  max-width: 500px;
}

.search-input {
  width: 100%;
}

.filter-box {
  flex: 0 0 auto;
}

.status-select {
  width: 150px;
}

.date-range-box {
  flex: 0 0 auto;
}

.date-picker {
  width: 350px;
}

.action-box {
  flex: 0 0 auto;
  margin-left: auto;
}

.table-card {
  margin-bottom: 20px;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  width: 100%;
}

.candidate-option,
.interviewer-option {
  display: flex;
  justify-content: space-between;
}

.candidate-info,
.interviewer-info {
  color: #909399;
  font-size: 13px;
}

.interview-result {
  margin-top: 20px;
  border-top: 1px solid #ebeef5;
  padding-top: 15px;
}

.result-content {
  margin-top: 10px;
}

.result-comment {
  margin-top: 10px;
  color: #606266;
}
</style> 