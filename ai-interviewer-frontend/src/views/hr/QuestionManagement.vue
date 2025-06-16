<template>
  <div class="question-management-container">
    <div class="page-title">
      <h2>题库管理</h2>
    </div>
    
    <div class="search-filter-container">
      <div class="search-box">
        <el-input
          v-model="searchKeyword"
          placeholder="搜索题目标题/内容"
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
        <el-select v-model="typeFilter" placeholder="题目类型" clearable @change="handleSearch" class="type-select">
          <el-option label="全部" value="" />
          <el-option label="技术题" value="TECHNICAL" />
          <el-option label="行为题" value="BEHAVIORAL" />
          <el-option label="情景题" value="SITUATIONAL" />
        </el-select>
      </div>
      
      <div class="filter-box">
        <el-select v-model="difficultyFilter" placeholder="难度" clearable @change="handleSearch" class="difficulty-select">
          <el-option label="全部" value="" />
          <el-option label="1级" value="1" />
          <el-option label="2级" value="2" />
          <el-option label="3级" value="3" />
          <el-option label="4级" value="4" />
          <el-option label="5级" value="5" />
        </el-select>
      </div>
      
      <div class="action-box">
        <el-button type="primary" @click="showCreateDialog">创建题目</el-button>
      </div>
    </div>

    <el-card class="table-card">
      <el-table
        v-loading="loading"
        :data="questionList"
        border
        style="width: 100%"
        :fit="true"
        :header-cell-style="{background:'#f5f7fa'}"
        :cell-style="{padding: '8px 0'}"
      >
        <el-table-column prop="id" label="ID" width="60" align="center" />
        <el-table-column prop="title" label="标题" min-width="180">
          <template #default="scope">
            <el-tooltip
              :content="scope.row.title"
              placement="top"
              :show-after="500"
            >
              <span>{{ truncateText(scope.row.title, 30) }}</span>
            </el-tooltip>
          </template>
        </el-table-column>
        <el-table-column prop="type" label="类型" width="90" align="center">
          <template #default="scope">
            <el-tag :type="getTypeTagType(scope.row.type)" size="small">
              {{ getTypeText(scope.row.type) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="difficulty" label="难度" width="100" align="center">
          <template #default="scope">
            <el-rate
              v-model="scope.row.difficulty"
              disabled
              text-color="#ff9900"
              size="small"
            />
          </template>
        </el-table-column>
        <el-table-column prop="position" label="适用职位" width="130" align="center">
          <template #default="scope">
            <span>{{ truncateText(scope.row.position, 12) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="useCount" label="使用次数" width="80" align="center" />
        <el-table-column prop="status" label="状态" width="80" align="center">
          <template #default="scope">
            <el-tag
              :type="scope.row.status === 1 ? 'success' : 'danger'"
              effect="plain"
              size="small"
            >
              {{ scope.row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" fixed="right" width="180" align="center">
          <template #default="scope">
            <TableActionButtons :buttons="getActionButtons(scope.row)" />
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

    <!-- 创建/编辑题目对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑题目' : '创建题目'"
      width="800px"
      @closed="resetForm"
    >
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="100px"
      >
        <el-form-item label="标题" prop="title">
          <el-input v-model="form.title" placeholder="请输入题目标题" />
        </el-form-item>
        <el-form-item label="内容" prop="content">
          <el-input
            v-model="form.content"
            type="textarea"
            :rows="4"
            placeholder="请输入题目内容"
          />
        </el-form-item>
        <el-form-item label="题目类型" prop="type">
          <el-select v-model="form.type" placeholder="请选择题目类型">
            <el-option label="技术题" value="TECHNICAL" />
            <el-option label="行为题" value="BEHAVIORAL" />
            <el-option label="情景题" value="SITUATIONAL" />
          </el-select>
        </el-form-item>
        <el-form-item label="难度等级" prop="difficulty">
          <el-rate
            v-model="form.difficulty"
            :max="5"
            :texts="['简单', '较简单', '中等', '较难', '困难']"
            show-text
          />
        </el-form-item>
        <el-form-item label="适用职位" prop="position">
          <el-input v-model="form.position" placeholder="请输入适用职位" />
        </el-form-item>
        <el-form-item label="标签">
          <el-tag
            v-for="tag in form.tags"
            :key="tag"
            closable
            :disable-transitions="false"
            @close="handleTagClose(tag)"
          >
            {{ tag }}
          </el-tag>
          <el-input
            v-if="tagInputVisible"
            ref="tagInputRef"
            v-model="tagInputValue"
            class="tag-input"
            size="small"
            @keyup.enter="handleTagConfirm"
            @blur="handleTagConfirm"
          />
          <el-button v-else class="tag-button" size="small" @click="showTagInput">
            + 添加标签
          </el-button>
        </el-form-item>
        <el-form-item label="参考答案" prop="referenceAnswer">
          <el-input
            v-model="form.referenceAnswer"
            type="textarea"
            :rows="6"
            placeholder="请输入参考答案"
          />
        </el-form-item>
        <el-form-item label="关键词">
          <el-tag
            v-for="keyword in form.keywords"
            :key="keyword"
            closable
            :disable-transitions="false"
            @close="handleKeywordClose(keyword)"
          >
            {{ keyword }}
          </el-tag>
          <el-input
            v-if="keywordInputVisible"
            ref="keywordInputRef"
            v-model="keywordInputValue"
            class="tag-input"
            size="small"
            @keyup.enter="handleKeywordConfirm"
            @blur="handleKeywordConfirm"
          />
          <el-button v-else class="tag-button" size="small" @click="showKeywordInput">
            + 添加关键词
          </el-button>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitForm">确认</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 题目详情对话框 -->
    <el-dialog
      v-model="detailDialogVisible"
      title="题目详情"
      width="800px"
    >
      <el-descriptions
        v-if="currentQuestion"
        :column="1"
        border
      >
        <el-descriptions-item label="标题">{{ currentQuestion.title }}</el-descriptions-item>
        <el-descriptions-item label="内容">
          <div class="question-content">{{ currentQuestion.content }}</div>
        </el-descriptions-item>
        <el-descriptions-item label="题目类型">
          <el-tag :type="getTypeTagType(currentQuestion.type)">
            {{ getTypeText(currentQuestion.type) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="难度等级">
          <el-rate
            v-model="currentQuestion.difficulty"
            disabled
            text-color="#ff9900"
          />
        </el-descriptions-item>
        <el-descriptions-item label="适用职位">{{ currentQuestion.position }}</el-descriptions-item>
        <el-descriptions-item label="标签">
          <div class="tags-container">
            <el-tag
              v-for="tag in currentQuestion.tags ? currentQuestion.tags.split(',') : []"
              :key="tag"
              effect="plain"
              class="tag-item"
            >
              {{ tag }}
            </el-tag>
          </div>
        </el-descriptions-item>
        <el-descriptions-item label="参考答案">
          <div class="reference-answer">{{ currentQuestion.referenceAnswer }}</div>
        </el-descriptions-item>
        <el-descriptions-item label="关键词">
          <div class="tags-container">
            <el-tag
              v-for="keyword in currentQuestion.keywords ? currentQuestion.keywords.split(',') : []"
              :key="keyword"
              type="success"
              effect="plain"
              class="tag-item"
            >
              {{ keyword }}
            </el-tag>
          </div>
        </el-descriptions-item>
        <el-descriptions-item label="使用次数">{{ currentQuestion.useCount }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="currentQuestion.status === 1 ? 'success' : 'danger'">
            {{ currentQuestion.status === 1 ? '启用' : '禁用' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ formatDateTime(currentQuestion.createTime) }}</el-descriptions-item>
        <el-descriptions-item label="更新时间">{{ formatDateTime(currentQuestion.updateTime) }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, nextTick } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import dayjs from 'dayjs'
import { 
  getQuestionPage, 
  createQuestion, 
  updateQuestion, 
  getQuestionDetail,
  deleteQuestion,
  updateQuestionStatus
} from '@/api/question'
import { 
  Search, 
  View, 
  EditPen, 
  Delete, 
  Check, 
  Close 
} from '@element-plus/icons-vue'
import TableActionButtons from '@/components/TableActionButtons.vue'

// 搜索条件
const searchKeyword = ref('')
const typeFilter = ref('')
const difficultyFilter = ref('')

// 分页参数
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

// 题目列表数据
const questionList = ref([])
const loading = ref(false)

// 当前查看的题目
const currentQuestion = ref(null)
const detailDialogVisible = ref(false)

// 创建/编辑题目对话框
const dialogVisible = ref(false)
const isEdit = ref(false)
const formRef = ref(null)
const form = reactive({
  id: null,
  title: '',
  content: '',
  type: '',
  difficulty: 3,
  position: '',
  tags: [],
  referenceAnswer: '',
  keywords: [],
  creatorId: 1 // 默认创建者ID，实际应该从用户信息中获取
})

// 标签输入
const tagInputVisible = ref(false)
const tagInputValue = ref('')
const tagInputRef = ref(null)

// 关键词输入
const keywordInputVisible = ref(false)
const keywordInputValue = ref('')
const keywordInputRef = ref(null)

// 表单验证规则
const rules = {
  title: [{ required: true, message: '请输入题目标题', trigger: 'blur' }],
  content: [{ required: true, message: '请输入题目内容', trigger: 'blur' }],
  type: [{ required: true, message: '请选择题目类型', trigger: 'change' }],
  difficulty: [{ required: true, message: '请选择难度等级', trigger: 'change' }],
  position: [{ required: true, message: '请输入适用职位', trigger: 'blur' }],
  referenceAnswer: [{ required: true, message: '请输入参考答案', trigger: 'blur' }]
}

// 格式化日期时间
const formatDateTime = (datetime) => {
  if (!datetime) return '-'
  return dayjs(datetime).format('YYYY-MM-DD HH:mm')
}

// 截断文本
const truncateText = (text, length) => {
  if (!text) return ''
  return text.length > length ? text.substring(0, length) + '...' : text
}

// 获取题目类型标签类型
const getTypeTagType = (type) => {
  switch (type) {
    case 'TECHNICAL': return 'primary'
    case 'BEHAVIORAL': return 'success'
    case 'SITUATIONAL': return 'warning'
    default: return 'info'
  }
}

// 获取题目类型文本
const getTypeText = (type) => {
  switch (type) {
    case 'TECHNICAL': return '技术题'
    case 'BEHAVIORAL': return '行为题'
    case 'SITUATIONAL': return '情景题'
    default: return '未知'
  }
}

// 加载题目列表
const loadQuestionList = async () => {
  try {
    loading.value = true
    const params = {
      current: currentPage.value,
      size: pageSize.value
    }
    
    if (searchKeyword.value) {
      params.keyword = searchKeyword.value
    }
    
    if (typeFilter.value) {
      params.type = typeFilter.value
    }
    
    if (difficultyFilter.value) {
      params.difficulty = difficultyFilter.value
    }
    
    const response = await getQuestionPage(params)
    
    if (response.success) {
      questionList.value = response.data.records
      total.value = response.data.total
    } else {
      ElMessage.error(response.message || '获取题目列表失败')
    }
  } catch (error) {
    console.error('获取题目列表失败:', error)
    ElMessage.error('获取题目列表失败')
  } finally {
    loading.value = false
  }
}

// 搜索处理
const handleSearch = () => {
  currentPage.value = 1
  loadQuestionList()
}

// 分页大小变化
const handleSizeChange = (val) => {
  pageSize.value = val
  loadQuestionList()
}

// 页码变化
const handleCurrentChange = (val) => {
  currentPage.value = val
  loadQuestionList()
}

// 显示创建题目对话框
const showCreateDialog = () => {
  isEdit.value = false
  dialogVisible.value = true
}

// 重置表单
const resetForm = () => {
  formRef.value?.resetFields()
  Object.assign(form, {
    id: null,
    title: '',
    content: '',
    type: '',
    difficulty: 3,
    position: '',
    tags: [],
    referenceAnswer: '',
    keywords: [],
    creatorId: 1
  })
}

// 显示标签输入框
const showTagInput = () => {
  tagInputVisible.value = true
  nextTick(() => {
    tagInputRef.value?.focus()
  })
}

// 处理标签确认
const handleTagConfirm = () => {
  if (tagInputValue.value) {
    if (!form.tags.includes(tagInputValue.value)) {
      form.tags.push(tagInputValue.value)
    }
  }
  tagInputVisible.value = false
  tagInputValue.value = ''
}

// 处理标签关闭
const handleTagClose = (tag) => {
  form.tags = form.tags.filter(item => item !== tag)
}

// 显示关键词输入框
const showKeywordInput = () => {
  keywordInputVisible.value = true
  nextTick(() => {
    keywordInputRef.value?.focus()
  })
}

// 处理关键词确认
const handleKeywordConfirm = () => {
  if (keywordInputValue.value) {
    if (!form.keywords.includes(keywordInputValue.value)) {
      form.keywords.push(keywordInputValue.value)
    }
  }
  keywordInputVisible.value = false
  keywordInputValue.value = ''
}

// 处理关键词关闭
const handleKeywordClose = (keyword) => {
  form.keywords = form.keywords.filter(item => item !== keyword)
}

// 提交表单
const submitForm = async () => {
  try {
    await formRef.value.validate()
    
    // 处理标签和关键词
    const formData = { ...form }
    formData.tags = form.tags.join(',')
    formData.keywords = form.keywords.join(',')
    
    if (isEdit.value) {
      // 编辑题目
      const response = await updateQuestion(formData)
      
      if (response.success) {
        ElMessage.success('更新题目成功')
        dialogVisible.value = false
        loadQuestionList()
      } else {
        ElMessage.error(response.message || '更新题目失败')
      }
    } else {
      // 创建题目
      const response = await createQuestion(formData)
      
      if (response.success) {
        ElMessage.success('创建题目成功')
        dialogVisible.value = false
        loadQuestionList()
      } else {
        ElMessage.error(response.message || '创建题目失败')
      }
    }
  } catch (error) {
    console.error('提交表单失败:', error)
    ElMessage.error('表单验证失败或提交失败')
  }
}

// 查看题目详情
const handleView = async (row) => {
  try {
    const response = await getQuestionDetail(row.id)
    
    if (response.success) {
      currentQuestion.value = response.data
      detailDialogVisible.value = true
    } else {
      ElMessage.error(response.message || '获取题目详情失败')
    }
  } catch (error) {
    console.error('获取题目详情失败:', error)
    ElMessage.error('获取题目详情失败')
  }
}

// 编辑题目
const handleEdit = async (row) => {
  try {
    const response = await getQuestionDetail(row.id)
    
    if (response.success) {
      const questionData = response.data
      isEdit.value = true
      
      // 填充表单数据
      form.id = questionData.id
      form.title = questionData.title
      form.content = questionData.content
      form.type = questionData.type
      form.difficulty = questionData.difficulty
      form.position = questionData.position
      form.tags = questionData.tags ? questionData.tags.split(',') : []
      form.referenceAnswer = questionData.referenceAnswer
      form.keywords = questionData.keywords ? questionData.keywords.split(',') : []
      
      dialogVisible.value = true
    } else {
      ElMessage.error(response.message || '获取题目详情失败')
    }
  } catch (error) {
    console.error('获取题目详情失败:', error)
    ElMessage.error('获取题目详情失败')
  }
}

// 修改题目状态
const handleStatusChange = (row, status) => {
  const statusText = status === 1 ? '启用' : '禁用'
  
  ElMessageBox.confirm(
    `确定要${statusText}题目 "${row.title}" 吗？`,
    `${statusText}题目`,
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      const response = await updateQuestionStatus(row.id, status)
      
      if (response.success) {
        ElMessage.success(`${statusText}题目成功`)
        loadQuestionList()
      } else {
        ElMessage.error(response.message || `${statusText}题目失败`)
      }
    } catch (error) {
      console.error(`${statusText}题目失败:`, error)
      ElMessage.error(`${statusText}题目失败`)
    }
  }).catch(() => {})
}

// 删除题目
const handleDelete = (row) => {
  ElMessageBox.confirm(
    `确定要删除题目 "${row.title}" 吗？此操作不可恢复！`,
    '删除题目',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'danger'
    }
  ).then(async () => {
    try {
      const response = await deleteQuestion(row.id)
      
      if (response.success) {
        ElMessage.success('删除题目成功')
        loadQuestionList()
      } else {
        ElMessage.error(response.message || '删除题目失败')
      }
    } catch (error) {
      console.error('删除题目失败:', error)
      ElMessage.error('删除题目失败')
    }
  }).catch(() => {})
}

// 获取操作按钮配置
const getActionButtons = (row) => {
  const buttons = [
    {
      text: '查看',
      type: 'primary',
      icon: View,
      onClick: () => handleView(row)
    },
    {
      text: '编辑',
      type: 'info',
      icon: EditPen,
      onClick: () => handleEdit(row)
    }
  ]
  
  if (row.status === 1) {
    buttons.push({
      text: '禁用',
      type: 'danger',
      icon: Close,
      onClick: () => handleStatusChange(row, 0)
    })
  } else {
    buttons.push({
      text: '启用',
      type: 'success',
      icon: Check,
      onClick: () => handleStatusChange(row, 1)
    })
  }
  
  buttons.push({
    text: '删除',
    type: 'danger',
    icon: Delete,
    onClick: () => handleDelete(row)
  })
  
  return buttons
}

// 初始化加载数据
onMounted(() => {
  loadQuestionList()
})
</script>

<style scoped>
.question-management-container {
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

.type-select, .difficulty-select {
  width: 150px;
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

.tag-input {
  width: 100px;
  margin-right: 10px;
  vertical-align: bottom;
}

.tag-button {
  height: 32px;
}

.tags-container {
  display: flex;
  flex-wrap: wrap;
  gap: 5px;
}

.tag-item {
  margin: 2px;
}

.question-content {
  white-space: pre-wrap;
  line-height: 1.5;
}

.reference-answer {
  white-space: pre-wrap;
  line-height: 1.5;
}
</style> 