<template>
  <div class="profile-container">
    <el-card class="profile-card">
      <template #header>
        <div class="card-header">
          <span>个人信息</span>
          <el-button v-if="!isEditing" type="primary" link @click="startEdit">编辑</el-button>
          <div v-else>
            <el-button type="success" link @click="saveProfile" :loading="saveLoading">保存</el-button>
            <el-button type="info" link @click="cancelEdit">取消</el-button>
          </div>
        </div>
      </template>
      
      <el-form 
        :model="userForm" 
        :rules="formRules"
        ref="userFormRef"
        label-width="100px"
        :disabled="!isEditing"
      >
        <el-form-item label="用户名" prop="username">
          <el-input v-model="userForm.username" disabled />
        </el-form-item>
        
        <el-form-item label="真实姓名" prop="realName">
          <el-input v-model="userForm.realName" />
        </el-form-item>
        
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="userForm.email" />
        </el-form-item>
        
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="userForm.phone" />
        </el-form-item>
        
        <el-form-item label="头像">
          <el-upload
            class="avatar-uploader"
            action="#"
            :show-file-list="false"
            :before-upload="beforeAvatarUpload"
            :http-request="handleAvatarUpload"
            :disabled="!isEditing"
          >
            <img v-if="userForm.avatar" :src="userForm.avatar" class="avatar" />
            <el-icon v-else class="avatar-icon"><Plus /></el-icon>
            <div class="upload-tip">点击上传头像</div>
          </el-upload>
        </el-form-item>
      </el-form>
    </el-card>
    
    <el-card class="password-card">
      <template #header>
        <div class="card-header">
          <span>修改密码</span>
        </div>
      </template>
      
      <el-form 
        :model="passwordForm" 
        :rules="passwordRules"
        ref="passwordFormRef"
        label-width="100px"
      >
        <el-form-item label="原密码" prop="oldPassword">
          <el-input 
            v-model="passwordForm.oldPassword" 
            type="password"
            placeholder="请输入原密码"
          />
        </el-form-item>
        
        <el-form-item label="新密码" prop="newPassword">
          <el-input 
            v-model="passwordForm.newPassword" 
            type="password"
            placeholder="请输入新密码" 
          />
        </el-form-item>
        
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input 
            v-model="passwordForm.confirmPassword" 
            type="password"
            placeholder="请再次输入新密码"
          />
        </el-form-item>
        
        <el-form-item>
          <el-button type="primary" @click="changePassword" :loading="pwdLoading">修改密码</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useUserStore } from '@/stores/userStore'
import { getUserInfo, updateUserInfo, changePassword as apiChangePassword } from '@/api/user'
import { ElMessage } from 'element-plus'

const userStore = useUserStore()

// 表单引用
const userFormRef = ref()
const passwordFormRef = ref()

// 状态控制
const isEditing = ref(false)
const saveLoading = ref(false)
const pwdLoading = ref(false)

// 用户表单数据
const userForm = reactive({
  id: '',
  username: '',
  realName: '',
  email: '',
  phone: '',
  avatar: ''
})

// 密码表单
const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

// 用户表单验证规则
const formRules = {
  realName: [
    { required: true, message: '请输入真实姓名', trigger: 'blur' }
  ],
  email: [
    { required: false, message: '请输入邮箱地址', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
  ],
  phone: [
    { required: false, message: '请输入手机号码', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号码', trigger: 'blur' }
  ]
}

// 密码表单验证规则
const passwordRules = {
  oldPassword: [
    { required: true, message: '请输入原密码', trigger: 'blur' }
  ],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能小于6位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入新密码', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value !== passwordForm.newPassword) {
          callback(new Error('两次输入密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
}

// 开始编辑
const startEdit = () => {
  isEditing.value = true
}

// 取消编辑
const cancelEdit = () => {
  isEditing.value = false
  loadUserInfo() // 重新加载用户信息，恢复原始数据
}

// 保存个人信息
const saveProfile = async () => {
  try {
    await userFormRef.value.validate()
    
    saveLoading.value = true
    await updateUserInfo({
      id: userForm.id,
      realName: userForm.realName,
      email: userForm.email,
      phone: userForm.phone,
      avatar: userForm.avatar
    })
    
    ElMessage.success('个人信息更新成功')
    isEditing.value = false
    
    // 更新存储的用户信息
    await userStore.getUserInfoAction()
  } catch (error) {
    console.error('保存个人信息失败:', error)
    ElMessage.error('保存个人信息失败')
  } finally {
    saveLoading.value = false
  }
}

// 修改密码
const changePassword = async () => {
  try {
    await passwordFormRef.value.validate()
    
    pwdLoading.value = true
    await apiChangePassword(
      userForm.id, 
      passwordForm.oldPassword, 
      passwordForm.newPassword
    )
    
    ElMessage.success('密码修改成功')
    
    // 清空密码表单
    passwordForm.oldPassword = ''
    passwordForm.newPassword = ''
    passwordForm.confirmPassword = ''
    
    // 重置表单验证状态
    passwordFormRef.value.resetFields()
  } catch (error) {
    console.error('修改密码失败:', error)
    ElMessage.error(error.message || '修改密码失败')
  } finally {
    pwdLoading.value = false
  }
}

// 头像上传前的验证
const beforeAvatarUpload = (file) => {
  const isJPG = file.type === 'image/jpeg' || file.type === 'image/png'
  const isLt2M = file.size / 1024 / 1024 < 2

  if (!isJPG) {
    ElMessage.error('头像只能是 JPG 或 PNG 格式!')
  }
  if (!isLt2M) {
    ElMessage.error('头像大小不能超过 2MB!')
  }
  return isJPG && isLt2M
}

// 处理头像上传
const handleAvatarUpload = (options) => {
  // 这里可以调用API上传头像，获取URL
  // 实际项目中需要替换为真实的上传逻辑
  const file = options.file
  const reader = new FileReader()
  reader.readAsDataURL(file)
  reader.onload = () => {
    userForm.avatar = reader.result
    ElMessage.success('头像上传成功')
  }
}

// 加载用户信息
const loadUserInfo = async () => {
  try {
    const userId = userStore.userInfo?.id
    
    if (!userId) {
      ElMessage.warning('未获取到用户信息，请重新登录')
      return
    }
    
    const res = await getUserInfo(userId)
    const userData = res.data
    
    // 更新表单数据
    Object.assign(userForm, {
      id: userData.id,
      username: userData.username,
      realName: userData.realName || '',
      email: userData.email || '',
      phone: userData.phone || '',
      avatar: userData.avatar || ''
    })
  } catch (error) {
    console.error('获取用户信息失败:', error)
    ElMessage.error('获取用户信息失败')
  }
}

onMounted(async () => {
  // 如果没有用户信息，先获取用户信息
  if (!userStore.userInfo) {
    try {
      await userStore.getUserInfoAction()
    } catch (error) {
      console.error('获取用户信息失败:', error)
      return
    }
  }
  
  await loadUserInfo()
})
</script>

<style scoped>
.profile-container {
  padding: 20px 0;
}

.profile-card, .password-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.avatar-uploader {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}

.avatar {
  width: 100px;
  height: 100px;
  border-radius: 50%;
  object-fit: cover;
}

.avatar-icon {
  font-size: 28px;
  color: #8c939d;
  width: 100px;
  height: 100px;
  border: 1px dashed #d9d9d9;
  border-radius: 50%;
  display: flex;
  justify-content: center;
  align-items: center;
}

.upload-tip {
  margin-top: 8px;
  color: #606266;
  font-size: 14px;
}
</style> 