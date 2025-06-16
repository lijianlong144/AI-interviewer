<template>
  <div class="position-management-container">
    <div class="page-header">
      <div class="page-title">
        <h1>职位管理</h1>
        <p class="page-subtitle">发布和管理企业招聘职位</p>
      </div>
      <el-button type="primary" @click="showCreateDialog" class="create-button">
        <el-icon><Plus /></el-icon>
        <span>发布新职位</span>
      </el-button>
    </div>

    <!-- 搜索和筛选 -->
    <el-card class="search-card" shadow="hover">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="职位名称">
          <el-input 
            v-model="searchForm.title" 
            placeholder="请输入职位名称"
            clearable
            @keyup.enter="handleSearch"
            prefix-icon="Search"
          />
        </el-form-item>
        <el-form-item label="部门">
          <el-select v-model="searchForm.department" placeholder="选择部门" clearable>
            <el-option label="技术部" value="技术部" />
            <el-option label="产品部" value="产品部" />
            <el-option label="市场部" value="市场部" />
            <el-option label="人力资源部" value="人力资源部" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="选择状态" clearable>
            <el-option label="招聘中" :value="1" />
            <el-option label="已关闭" :value="2" />
            <el-option label="已暂停" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">
            <el-icon><Search /></el-icon>
            搜索
          </el-button>
          <el-button @click="resetSearch">
            <el-icon><Refresh /></el-icon>
            重置
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 职位列表 -->
    <el-card class="position-list-card" shadow="hover" v-loading="loading">
      <div class="card-header">
        <div class="card-title">职位列表</div>
        <div class="card-actions">
          <el-button size="small" @click="handleRefresh">
            <el-icon><Refresh /></el-icon>
            刷新
          </el-button>
          <el-button size="small" type="success">
            <el-icon><Download /></el-icon>
            导出
          </el-button>
        </div>
      </div>

      <el-table 
        :data="positionList" 
        style="width: 100%" 
        border 
        stripe
        :header-cell-style="{background:'#f5f7fa'}"
      >
        <el-table-column prop="positionCode" label="职位编号" width="100" />
        <el-table-column prop="title" label="职位名称" min-width="120">
          <template #default="scope">
            <div class="position-title">
              <span>{{ scope.row.title }}</span>
              <el-tag v-if="scope.row.priority >= 4" type="danger" size="small" effect="plain">急招</el-tag>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="department" label="所属部门" width="100" />
        <el-table-column prop="workLocation" label="工作地点" width="100" />
        <el-table-column label="工作类型" width="80" align="center">
          <template #default="scope">
            <el-tag :type="getJobTypeTag(scope.row.jobType)" effect="light">
              {{ getJobTypeText(scope.row.jobType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="薪资范围" width="100" align="center">
          <template #default="scope">
            <span class="salary-range">{{ formatSalary(scope.row.salaryMin, scope.row.salaryMax) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="headcount" label="招聘人数" width="80" align="center" />
        <el-table-column label="状态" width="80" align="center">
          <template #default="scope">
            <el-tag v-if="scope.row.status === 1" type="success" effect="dark">招聘中</el-tag>
            <el-tag v-else-if="scope.row.status === 2" type="danger" effect="dark">已关闭</el-tag>
            <el-tag v-else type="warning" effect="dark">已暂停</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="发布时间" width="100" align="center">
          <template #default="scope">
            {{ formatDate(scope.row.publishTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" min-width="320" fixed="right" align="center">
          <template #default="scope">
            <div class="action-buttons">
              <el-button size="small" type="warning" @click.stop="editPosition(scope.row)">
                <el-icon><Edit /></el-icon>
                编辑
              </el-button>
              <el-button v-if="scope.row.status !== 1" size="small" type="success" @click="updateStatus(scope.row.id, 1)">
                <el-icon><VideoPlay /></el-icon>
                发布
              </el-button>
              <el-button v-if="scope.row.status !== 2" size="small" type="info" @click="updateStatus(scope.row.id, 2)">
                <el-icon><CircleClose /></el-icon>
                关闭
              </el-button>
              <el-button v-if="scope.row.status !== 3" size="small" type="warning" @click="updateStatus(scope.row.id, 3)">
                <el-icon><VideoPause /></el-icon>
                暂停
              </el-button>
              <el-button size="small" type="danger" @click="confirmDelete(scope.row)">
                <el-icon><Delete /></el-icon>
                删除
              </el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>

      <!-- 空状态 -->
      <el-empty 
        v-if="!loading && positionList.length === 0" 
        description="暂无职位信息" 
        :image-size="200"
      >
        <el-button type="primary" @click="showCreateDialog">立即创建</el-button>
      </el-empty>
    </el-card>

    <!-- 分页 -->
    <div class="pagination-container" v-if="total > 0">
      <el-pagination
        :current-page="currentPage"
        :page-size="pageSize"
        :page-sizes="[10, 20, 50]"
        :total="total"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
        background
      />
    </div>

    <!-- 职位表单对话框 -->
    <el-dialog 
      v-model="dialogVisible" 
      :title="isEdit ? '编辑职位' : '发布新职位'"
      width="700px"
    >
      <el-form 
        ref="positionFormRef" 
        :model="positionForm" 
        :rules="positionRules" 
        label-width="100px"
        label-position="left"
      >
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="职位名称" prop="title">
              <el-input v-model="positionForm.title" placeholder="请输入职位名称" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="职位编号" prop="positionCode">
              <el-input v-model="positionForm.positionCode" placeholder="请输入职位编号" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="所属部门" prop="department">
              <el-select v-model="positionForm.department" placeholder="请选择部门" style="width: 100%">
                <el-option label="技术部" value="技术部" />
                <el-option label="产品部" value="产品部" />
                <el-option label="市场部" value="市场部" />
                <el-option label="人力资源部" value="人力资源部" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="工作地点" prop="workLocation">
              <el-input v-model="positionForm.workLocation" placeholder="请输入工作地点" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="工作类型" prop="jobType">
              <el-select v-model="positionForm.jobType" placeholder="请选择工作类型" style="width: 100%">
                <el-option label="全职" value="FULL_TIME" />
                <el-option label="兼职" value="PART_TIME" />
                <el-option label="实习" value="INTERN" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="招聘人数" prop="headcount">
              <el-input-number v-model="positionForm.headcount" :min="1" :max="100" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="最低薪资" prop="salaryMin">
              <el-input-number v-model="positionForm.salaryMin" :min="0" :step="1000" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="最高薪资" prop="salaryMax">
              <el-input-number v-model="positionForm.salaryMax" :min="0" :step="1000" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="经验要求" prop="experienceRequired">
              <el-input v-model="positionForm.experienceRequired" placeholder="如：3年以上相关经验" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="学历要求" prop="educationRequired">
              <el-input v-model="positionForm.educationRequired" placeholder="如：本科及以上" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="截止日期" prop="closeTime">
              <el-date-picker 
                v-model="positionForm.closeTime" 
                type="date" 
                placeholder="选择截止日期"
                :disabled-date="disabledDate"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="优先级" prop="priority">
              <el-rate v-model="positionForm.priority" :max="5" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="职位描述" prop="description">
          <el-input 
            v-model="positionForm.description" 
            type="textarea" 
            :rows="4" 
            placeholder="请输入职位描述"
          />
        </el-form-item>

        <el-form-item label="职位要求" prop="requirements">
          <el-input 
            v-model="positionForm.requirements" 
            type="textarea" 
            :rows="4" 
            placeholder="请输入职位要求"
          />
        </el-form-item>

        <el-form-item label="福利待遇" prop="benefits">
          <el-input 
            v-model="positionForm.benefits" 
            type="textarea" 
            :rows="3" 
            placeholder="请输入福利待遇"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitPosition" :loading="submitLoading">
            {{ isEdit ? '保存' : '发布' }}
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onActivated } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  getPositionPage, 
  createPosition, 
  updatePosition, 
  getPositionDetail, 
  deletePosition, 
  updatePositionStatus 
} from '@/api/position'
import { getUserInfo } from '@/utils/auth'
import dayjs from 'dayjs'
import { 
  Search, 
  Edit, 
  Delete, 
  Plus, 
  Refresh, 
  Download,
  VideoPlay,
  CircleClose,
  VideoPause
} from '@element-plus/icons-vue'

const router = useRouter()
const userInfo = getUserInfo()

// 搜索表单
const searchForm = reactive({
  title: '',
  department: '',
  status: ''
})

// 分页参数
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

// 职位列表
const positionList = ref([])
const loading = ref(false)

// 对话框控制
const dialogVisible = ref(false)
const isEdit = ref(false)
const submitLoading = ref(false)
const positionFormRef = ref(null)

// 职位表单
const positionForm = reactive({
  id: undefined,
  positionCode: '',
  title: '',
  department: '',
  jobType: 'FULL_TIME',
  workLocation: '',
  headcount: 1,
  salaryMin: 0,
  salaryMax: 0,
  experienceRequired: '',
  educationRequired: '',
  description: '',
  requirements: '',
  benefits: '',
  priority: 3,
  closeTime: '',
  hrId: userInfo?.id
})

// 表单验证规则
const positionRules = {
  title: [{ required: true, message: '请输入职位名称', trigger: 'blur' }],
  department: [{ required: true, message: '请选择所属部门', trigger: 'change' }],
  workLocation: [{ required: true, message: '请输入工作地点', trigger: 'blur' }],
  jobType: [{ required: true, message: '请选择工作类型', trigger: 'change' }]
}

// 获取工作类型标签
const getJobTypeTag = (type) => {
  const typeMap = {
    'FULL_TIME': 'primary',
    'PART_TIME': 'success',
    'INTERN': 'warning'
  }
  return typeMap[type] || 'info'
}

// 获取工作类型文本
const getJobTypeText = (type) => {
  const textMap = {
    'FULL_TIME': '全职',
    'PART_TIME': '兼职',
    'INTERN': '实习'
  }
  return textMap[type] || type
}

// 格式化薪资
const formatSalary = (min, max) => {
  if (!min && !max) return '面议'
  if (min && max) {
    return `${min/1000}k-${max/1000}k`
  }
  return min ? `${min/1000}k起` : `${max/1000}k以内`
}

// 格式化日期
const formatDate = (date) => {
  if (!date) return '-'
  return dayjs(date).format('YYYY-MM-DD')
}

// 禁用今天之前的日期
const disabledDate = (time) => {
  return time.getTime() < Date.now() - 8.64e7 // 不能选择今天之前的日期
}

// 搜索
const handleSearch = () => {
  currentPage.value = 1
  loadPositions()
}

// 重置搜索
const resetSearch = () => {
  searchForm.title = ''
  searchForm.department = ''
  searchForm.status = ''
  handleSearch()
}

// 分页大小改变
const handleSizeChange = (val) => {
  pageSize.value = val
  currentPage.value = 1
  loadPositions()
}

// 页码改变
const handleCurrentChange = (val) => {
  currentPage.value = val
  loadPositions()
}

// 加载职位列表
const loadPositions = async () => {
  try {
    loading.value = true
    
    const params = {
      current: currentPage.value,
      size: pageSize.value,
      // 添加时间戳参数，避免请求被缓存
      _t: Date.now()
    }
    
    // 添加搜索条件
    if (searchForm.title) {
      params.title = searchForm.title
    }
    if (searchForm.department) {
      params.department = searchForm.department
    }
    if (searchForm.status) {
      params.status = searchForm.status
    }
    
    const res = await getPositionPage(params)
    
    if (res.data) {
      positionList.value = res.data.records || []
      total.value = res.data.total || 0
      
      console.log('加载职位列表成功，总数:', total.value)
    }
  } catch (error) {
    console.error('获取职位列表失败:', error)
    ElMessage.error('获取职位列表失败')
  } finally {
    loading.value = false
  }
}

// 显示创建对话框
const showCreateDialog = () => {
  isEdit.value = false
  resetForm()
  dialogVisible.value = true
}

// 编辑职位
const editPosition = async (position) => {
  try {
    const res = await getPositionDetail(position.id)
    if (res.data) {
      isEdit.value = true
      Object.assign(positionForm, res.data)
      dialogVisible.value = true
    }
  } catch (error) {
    console.error('获取职位详情失败:', error)
    ElMessage.error('获取职位详情失败')
  }
}

// 重置表单
const resetForm = () => {
  positionForm.id = undefined
  positionForm.positionCode = ''
  positionForm.title = ''
  positionForm.department = ''
  positionForm.jobType = 'FULL_TIME'
  positionForm.workLocation = ''
  positionForm.headcount = 1
  positionForm.salaryMin = 0
  positionForm.salaryMax = 0
  positionForm.experienceRequired = ''
  positionForm.educationRequired = ''
  positionForm.description = ''
  positionForm.requirements = ''
  positionForm.benefits = ''
  positionForm.priority = 3
  positionForm.closeTime = ''
  positionForm.hrId = userInfo?.id
  
  if (positionFormRef.value) {
    positionFormRef.value.resetFields()
  }
}

// 提交职位
const submitPosition = async () => {
  if (!positionFormRef.value) return
  
  await positionFormRef.value.validate(async (valid) => {
    if (!valid) return
    
    try {
      submitLoading.value = true
      
      // 处理日期格式
      if (positionForm.closeTime) {
        positionForm.closeTime = dayjs(positionForm.closeTime).format('YYYY-MM-DDTHH:mm:ss')
      }
      
      let res
      if (isEdit.value) {
        res = await updatePosition(positionForm)
      } else {
        res = await createPosition(positionForm)
      }
      
      if (res.code === 200) {
        ElMessage.success(isEdit.value ? '职位更新成功' : '职位发布成功')
        dialogVisible.value = false
        
        // 重置页码到第一页，确保能看到新添加的数据
        currentPage.value = 1
        await loadPositions()
      } else {
        ElMessage.error(res.message || (isEdit.value ? '职位更新失败' : '职位发布失败'))
      }
    } catch (error) {
      console.error(isEdit.value ? '更新职位失败:' : '创建职位失败:', error)
      ElMessage.error(isEdit.value ? '更新职位失败' : '创建职位失败')
    } finally {
      submitLoading.value = false
    }
  })
}

// 更新职位状态
const updateStatus = async (id, status) => {
  try {
    const statusText = status === 1 ? '发布' : status === 2 ? '关闭' : '暂停'
    
    const res = await updatePositionStatus(id, status)
    if (res.code === 200) {
      ElMessage.success(`职位${statusText}成功`)
      
      // 重新加载数据，确保状态更新
      await loadPositions()
    } else {
      ElMessage.error(res.message || `职位${statusText}失败`)
    }
  } catch (error) {
    console.error('更新职位状态失败:', error)
    ElMessage.error('更新职位状态失败')
  }
}

// 确认删除
const confirmDelete = (position) => {
  ElMessageBox.confirm(
    `确定要删除职位 "${position.title}" 吗？`,
    '删除确认',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      const res = await deletePosition(position.id)
      if (res.code === 200) {
        ElMessage.success('职位删除成功')
        
        // 检查当前页是否还有数据，如果没有则回到上一页
        if (positionList.value.length === 1 && currentPage.value > 1) {
          currentPage.value--
        }
        
        // 重新加载数据
        await loadPositions()
      } else {
        ElMessage.error(res.message || '职位删除失败')
      }
    } catch (error) {
      console.error('删除职位失败:', error)
      ElMessage.error('删除职位失败')
    }
  }).catch(() => {})
}

// 刷新数据
const handleRefresh = async () => {
  // 重置页码到第一页
  currentPage.value = 1
  
  // 清除可能的缓存影响
  positionList.value = []
  total.value = 0
  
  // 重新加载数据
  await loadPositions()
}

// 初始化
onMounted(() => {
  console.log('组件挂载，加载职位数据')
  loadPositions()
})

// 当组件被缓存后重新激活时也重新加载数据
onActivated(() => {
  console.log('组件激活，重新加载职位数据')
  loadPositions()
})
</script>

<style scoped>
.position-management-container {
  padding: 16px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.page-header h1 {
  margin: 0;
  font-size: 24px;
  font-weight: 600;
  color: #1a1a1a;
}

.search-card {
  margin-bottom: 20px;
  border-radius: 8px;
}

.search-form {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.position-list-card {
  margin-bottom: 20px;
  border-radius: 8px;
}

.pagination-container {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}

.page-title {
  display: flex;
  flex-direction: column;
}

.page-subtitle {
  margin: 5px 0 0 0;
  font-size: 14px;
  color: #606266;
}

.create-button {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 10px 20px;
  font-weight: 500;
  border-radius: 6px;
  transition: all 0.3s;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.1);
}

.create-button:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 1px solid #ebeef5;
}

.card-title {
  font-size: 18px;
  font-weight: 600;
  color: #1a1a1a;
}

.card-actions {
  display: flex;
  gap: 10px;
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

.position-title {
  display: flex;
  align-items: center;
  gap: 8px;
}

.salary-range {
  color: #f56c6c;
  font-weight: 500;
}

:deep(.el-table) {
  border-radius: 6px;
  overflow: hidden;
}

:deep(.el-table__row) {
  transition: all 0.2s;
}

:deep(.el-table__row:hover) {
  background-color: #f0f9ff !important;
}

:deep(.el-table__header-wrapper) {
  background-color: #f5f7fa;
}

:deep(.el-table__header) {
  font-weight: 600;
}

:deep(.el-tag) {
  border-radius: 4px;
}

:deep(.el-button) {
  transition: all 0.3s;
}

:deep(.el-button:hover) {
  transform: translateY(-2px);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

:deep(.el-pagination) {
  padding: 0;
}
</style> 