<template>
  <div class="position-list-container">
    <el-row :gutter="20">
      <el-col :span="24">
        <div class="page-header">
          <h1>职位列表</h1>
          <p class="subtitle">发现适合您的职位机会</p>
        </div>
      </el-col>
    </el-row>

    <!-- 搜索和筛选 -->
    <el-card class="search-card">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="职位名称">
          <el-input 
            v-model="searchForm.title" 
            placeholder="请输入职位名称"
            clearable
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item label="工作地点">
          <el-input 
            v-model="searchForm.workLocation" 
            placeholder="请输入工作地点"
            clearable
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
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 职位列表 -->
    <div class="position-list" v-loading="loading">
      <el-row :gutter="20">
        <el-col :span="8" v-for="position in positionList" :key="position.id">
          <el-card class="position-card" shadow="hover">
            <div class="position-header">
              <h3 class="position-title">{{ position.title }}</h3>
              <el-tag :type="getJobTypeTag(position.jobType)">
                {{ getJobTypeText(position.jobType) }}
              </el-tag>
            </div>
            
            <div class="position-info">
              <div class="info-item">
                <el-icon><Location /></el-icon>
                <span>{{ position.workLocation }}</span>
              </div>
              <div class="info-item">
                <el-icon><OfficeBuilding /></el-icon>
                <span>{{ position.department }}</span>
              </div>
              <div class="info-item">
                <el-icon><Money /></el-icon>
                <span>{{ formatSalary(position.salaryMin, position.salaryMax) }}</span>
              </div>
              <div class="info-item">
                <el-icon><User /></el-icon>
                <span>{{ position.experienceRequired || '经验不限' }}</span>
              </div>
            </div>

            <div class="position-desc">
              {{ truncateText(position.description, 100) }}
            </div>

            <div class="position-footer">
              <span class="publish-time">
                发布于 {{ formatDate(position.publishTime) }}
              </span>
              <el-button type="primary" size="small" @click="viewDetail(position)">
                查看详情
              </el-button>
            </div>
          </el-card>
        </el-col>
      </el-row>

      <!-- 空状态 -->
      <el-empty v-if="!loading && positionList.length === 0" description="暂无职位信息" />
    </div>

    <!-- 分页 -->
    <div class="pagination-container" v-if="total > 0">
      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :page-sizes="[9, 18, 36]"
        :total="total"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getPositionPage } from '@/api/position'
import dayjs from 'dayjs'

const router = useRouter()

// 搜索表单
const searchForm = reactive({
  title: '',
  workLocation: '',
  department: ''
})

// 分页参数
const currentPage = ref(1)
const pageSize = ref(9)
const total = ref(0)

// 职位列表
const positionList = ref([])
const loading = ref(false)

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
  return dayjs(date).format('YYYY-MM-DD')
}

// 截断文本
const truncateText = (text, length) => {
  if (!text) return ''
  return text.length > length ? text.substring(0, length) + '...' : text
}

// 查看详情
const viewDetail = (position) => {
  router.push(`/candidate/position/${position.id}`)
}

// 搜索
const handleSearch = () => {
  currentPage.value = 1
  loadPositions()
}

// 重置搜索
const resetSearch = () => {
  searchForm.title = ''
  searchForm.workLocation = ''
  searchForm.department = ''
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
      status: 1 // 只查询招聘中的职位
    }
    
    // 添加搜索条件
    if (searchForm.title) {
      params.title = searchForm.title
    }
    if (searchForm.workLocation) {
      params.workLocation = searchForm.workLocation
    }
    if (searchForm.department) {
      params.department = searchForm.department
    }
    
    const res = await getPositionPage(params)
    
    if (res.data) {
      positionList.value = res.data.records || []
      total.value = res.data.total || 0
    }
  } catch (error) {
    console.error('获取职位列表失败:', error)
    ElMessage.error('获取职位列表失败')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadPositions()
})
</script>

<style scoped>
.position-list-container {
  padding: 20px 0;
}

.page-header {
  text-align: center;
  margin-bottom: 30px;
}

.page-header h1 {
  font-size: 32px;
  margin-bottom: 10px;
}

.subtitle {
  color: #909399;
  font-size: 16px;
}

.search-card {
  margin-bottom: 30px;
}

.search-form {
  padding: 10px 0;
}

.position-list {
  min-height: 400px;
}

.position-card {
  margin-bottom: 20px;
  transition: all 0.3s;
}

.position-card:hover {
  transform: translateY(-5px);
}

.position-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
}

.position-title {
  font-size: 18px;
  margin: 0;
  color: #303133;
}

.position-info {
  margin-bottom: 15px;
}

.info-item {
  display: flex;
  align-items: center;
  margin-bottom: 8px;
  color: #606266;
  font-size: 14px;
}

.info-item .el-icon {
  margin-right: 5px;
  color: #909399;
}

.position-desc {
  color: #606266;
  line-height: 1.6;
  margin-bottom: 15px;
  min-height: 60px;
}

.position-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 15px;
  border-top: 1px solid #EBEEF5;
}

.publish-time {
  color: #909399;
  font-size: 12px;
}

.pagination-container {
  margin-top: 30px;
  display: flex;
  justify-content: center;
}
</style>