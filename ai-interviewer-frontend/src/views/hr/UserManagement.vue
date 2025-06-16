<template>
  <div class="user-management-container">
    <div class="page-title">
      <h2>用户管理</h2>
    </div>
    
    <div class="search-filter-container">
      <div class="search-box">
        <el-input
          v-model="searchKeyword"
          placeholder="搜索用户名/真实姓名"
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
        <el-select v-model="roleFilter" placeholder="角色" clearable @change="handleSearch" class="role-select">
          <el-option label="全部" value="" />
          <el-option label="面试官" value="1" />
          <el-option label="HR" value="2" />
          <el-option label="候选人" value="3" />
        </el-select>
      </div>
      
      <div class="filter-box">
        <el-select v-model="statusFilter" placeholder="状态" clearable @change="handleSearch" class="status-select">
          <el-option label="全部" value="" />
          <el-option label="启用" value="1" />
          <el-option label="禁用" value="0" />
        </el-select>
      </div>
    </div>

    <el-card class="table-card">
      <el-table
        v-loading="loading"
        :data="userList"
        border
        style="width: 100%"
        :fit="true"
        :header-cell-style="{background:'#f5f7fa'}"
        :cell-style="{padding: '8px 0'}"
      >
        <el-table-column prop="id" label="ID" width="60" align="center" />
        <el-table-column prop="username" label="用户名" width="120" />
        <el-table-column prop="realName" label="真实姓名" width="100" />
        <el-table-column prop="role" label="角色" width="90" align="center">
          <template #default="scope">
            <el-tag
              :type="getRoleTagType(scope.row.role)"
              effect="plain"
              size="small"
            >
              {{ getRoleText(scope.row.role) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="phone" label="手机号" width="110" align="center" />
        <el-table-column prop="email" label="邮箱" width="150" show-overflow-tooltip />
        <el-table-column prop="createTime" label="注册时间" width="140" align="center">
          <template #default="scope">
            {{ formatDateTime(scope.row.createTime) }}
          </template>
        </el-table-column>
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
        <el-table-column label="操作" fixed="right" min-width="280" align="center">
          <template #default="scope">
            <div class="action-buttons">
              <el-button
                size="small"
                type="warning"
                @click="handleEdit(scope.row)"
              >
                <el-icon><Edit /></el-icon>
                编辑
              </el-button>
              <el-button
                v-if="scope.row.status === 1"
                size="small"
                type="danger"
                @click="handleStatusChange(scope.row, 0)"
              >
                <el-icon><CircleClose /></el-icon>
                禁用
              </el-button>
              <el-button
                v-else
                size="small"
                type="success"
                @click="handleStatusChange(scope.row, 1)"
              >
                <el-icon><Check /></el-icon>
                启用
              </el-button>
              <el-button
                size="small"
                type="info"
                @click="handleResetPassword(scope.row)"
              >
                <el-icon><Key /></el-icon>
                重置密码
              </el-button>
            </div>
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

    <!-- 编辑用户对话框 -->
    <el-dialog
      v-model="editDialogVisible"
      title="编辑用户信息"
      width="500px"
    >
      <el-form
        ref="editFormRef"
        :model="editForm"
        :rules="editRules"
        label-width="100px"
      >
        <el-form-item label="用户名" prop="username">
          <el-input v-model="editForm.username" disabled />
        </el-form-item>
        <el-form-item label="真实姓名" prop="realName">
          <el-input v-model="editForm.realName" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="editForm.phone" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="editForm.email" />
        </el-form-item>
        <el-form-item label="角色" prop="role">
          <el-select v-model="editForm.role" placeholder="请选择角色">
            <el-option label="面试官" value="1" />
            <el-option label="HR" value="2" />
            <el-option label="候选人" value="3" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="editDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitEdit">确认</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 重置密码对话框 -->
    <el-dialog
      v-model="resetPasswordDialogVisible"
      title="重置密码"
      width="400px"
    >
      <el-form
        ref="resetPasswordFormRef"
        :model="resetPasswordForm"
        :rules="resetPasswordRules"
        label-width="100px"
      >
        <el-form-item label="新密码" prop="newPassword">
          <el-input
            v-model="resetPasswordForm.newPassword"
            type="password"
            show-password
          />
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input
            v-model="resetPasswordForm.confirmPassword"
            type="password"
            show-password
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="resetPasswordDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitResetPassword">确认</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/utils/request'
import dayjs from 'dayjs'
import { getUserPage, updateUserInfo, updateUserStatus, changePassword } from '@/api/user'
import { Search, Edit, Check, CircleClose, Key } from '@element-plus/icons-vue'

// 搜索条件
const searchKeyword = ref('')
const roleFilter = ref('')
const statusFilter = ref('')

// 分页参数
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

// 用户列表数据
const userList = ref([])
const loading = ref(false)

// 编辑用户相关
const editDialogVisible = ref(false)
const editFormRef = ref(null)
const editForm = reactive({
  id: '',
  username: '',
  realName: '',
  phone: '',
  email: '',
  role: ''
})
const editRules = {
  realName: [{ required: true, message: '请输入真实姓名', trigger: 'blur' }],
  role: [{ required: true, message: '请选择角色', trigger: 'change' }],
  phone: [
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号码', trigger: 'blur' }
  ],
  email: [
    { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
  ]
}

// 重置密码相关
const resetPasswordDialogVisible = ref(false)
const resetPasswordFormRef = ref(null)
const resetPasswordForm = reactive({
  userId: '',
  newPassword: '',
  confirmPassword: ''
})
const resetPasswordRules = {
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能小于6位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value !== resetPasswordForm.newPassword) {
          callback(new Error('两次输入的密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
}

// 格式化日期时间
const formatDateTime = (datetime) => {
  if (!datetime) return '-'
  return dayjs(datetime).format('YYYY-MM-DD HH:mm')
}

// 获取角色标签类型
const getRoleTagType = (role) => {
  // 处理数字类型的角色ID
  if (typeof role === 'number' || !isNaN(Number(role))) {
    const roleId = Number(role);
    switch (roleId) {
      case 1: return 'primary' // 面试官
      case 2: return 'success' // HR
      case 3: return 'info'    // 候选人
      default: return ''
    }
  }
  
  // 处理字符串类型的角色
  switch (role) {
    case 'INTERVIEWER': return 'primary'
    case 'HR': return 'success'
    case 'CANDIDATE': return 'info'
    default: return ''
  }
}

// 获取角色文本
const getRoleText = (role) => {
  // 处理数字类型的角色ID
  if (typeof role === 'number' || !isNaN(Number(role))) {
    const roleId = Number(role);
    switch (roleId) {
      case 1: return '面试官'
      case 2: return 'HR'
      case 3: return '候选人'
      default: return '未知'
    }
  }
  
  // 处理字符串类型的角色
  switch (role) {
    case 'INTERVIEWER': return '面试官'
    case 'HR': return 'HR'
    case 'CANDIDATE': return '候选人'
    default: return '未知'
  }
}

// 加载用户列表
const loadUserList = async () => {
  try {
    loading.value = true
    const params = {
      current: currentPage.value,
      size: pageSize.value
    }
    
    if (searchKeyword.value) {
      // 根据后端API参数，searchKeyword可能是用户名或真实姓名
      // 这里我们同时设置两个参数，让后端自行处理
      params.username = searchKeyword.value
      params.realName = searchKeyword.value
    }
    
    if (roleFilter.value) {
      params.roleId = roleFilter.value
    }
    
    if (statusFilter.value !== '') {
      params.status = statusFilter.value
    }
    
    const response = await getUserPage(params)
    
    if (response.success) {
      userList.value = response.data.records
      total.value = response.data.total
    } else {
      ElMessage.error(response.message || '获取用户列表失败')
    }
  } catch (error) {
    console.error('获取用户列表失败:', error)
    ElMessage.error('获取用户列表失败')
  } finally {
    loading.value = false
  }
}

// 搜索处理
const handleSearch = () => {
  currentPage.value = 1
  loadUserList()
}

// 分页大小变化
const handleSizeChange = (val) => {
  pageSize.value = val
  loadUserList()
}

// 页码变化
const handleCurrentChange = (val) => {
  currentPage.value = val
  loadUserList()
}

// 编辑用户
const handleEdit = (row) => {
  editForm.id = row.id
  editForm.username = row.username
  editForm.realName = row.realName
  editForm.phone = row.phone || ''
  editForm.email = row.email || ''
  editForm.role = row.role
  
  editDialogVisible.value = true
}

// 提交编辑
const submitEdit = async () => {
  try {
    await editFormRef.value.validate()
    
    const response = await updateUserInfo(editForm)
    
    if (response.success) {
      ElMessage.success('更新用户信息成功')
      editDialogVisible.value = false
      loadUserList()
    } else {
      ElMessage.error(response.message || '更新用户信息失败')
    }
  } catch (error) {
    console.error('更新用户信息失败:', error)
    ElMessage.error('表单验证失败或更新用户信息失败')
  }
}

// 修改用户状态
const handleStatusChange = (row, status) => {
  const statusText = status === 1 ? '启用' : '禁用'
  
  ElMessageBox.confirm(
    `确定要${statusText}用户 ${row.username} 吗？`,
    `${statusText}用户`,
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      const response = await updateUserStatus(row.id, status)
      
      if (response.success) {
        ElMessage.success(`${statusText}用户成功`)
        loadUserList()
      } else {
        ElMessage.error(response.message || `${statusText}用户失败`)
      }
    } catch (error) {
      console.error(`${statusText}用户失败:`, error)
      ElMessage.error(`${statusText}用户失败`)
    }
  }).catch(() => {})
}

// 重置密码
const handleResetPassword = (row) => {
  resetPasswordForm.userId = row.id
  resetPasswordForm.newPassword = ''
  resetPasswordForm.confirmPassword = ''
  resetPasswordDialogVisible.value = true
}

// 提交重置密码
const submitResetPassword = async () => {
  try {
    await resetPasswordFormRef.value.validate()
    
    const response = await changePassword(resetPasswordForm.userId, null, resetPasswordForm.newPassword)
    
    if (response.success) {
      ElMessage.success('重置密码成功')
      resetPasswordDialogVisible.value = false
    } else {
      ElMessage.error(response.message || '重置密码失败')
    }
  } catch (error) {
    console.error('重置密码失败:', error)
    ElMessage.error('表单验证失败或重置密码失败')
  }
}

// 初始化加载数据
onMounted(() => {
  loadUserList()
})
</script>

<style scoped>
.user-management-container {
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

.role-select, .status-select {
  width: 150px;
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