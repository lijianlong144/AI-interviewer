<template>
  <div class="login-container">
    <div class="login-background"></div>
    <div class="login-content">
      <el-card class="login-card" shadow="always">
        <div class="login-header">
          <el-icon class="logo-icon"><Monitor /></el-icon>
          <h1 class="login-title">AI面试系统</h1>
        </div>
        
        <div class="login-description">企业智能招聘面试管理平台</div>
        
        <el-tabs v-model="activeTab">
          <el-tab-pane label="登录" name="login">
            <el-form :model="loginForm" :rules="loginRules" ref="loginFormRef">
              <el-form-item prop="username">
                <el-input 
                  v-model="loginForm.username" 
                  placeholder="请输入用户名"
                  :prefix-icon="User"
                  size="large"
                />
              </el-form-item>
              <el-form-item prop="password">
                <el-input 
                  v-model="loginForm.password" 
                  type="password" 
                  placeholder="请输入密码"
                  :prefix-icon="Lock"
                  size="large"
                  @keyup.enter="handleLogin"
                />
              </el-form-item>
              <el-form-item>
                <el-button type="primary" @click="handleLogin" :loading="loading" size="large" class="submit-btn">
                  登录
                </el-button>
              </el-form-item>
            </el-form>
          </el-tab-pane>
          
          <el-tab-pane label="注册" name="register">
            <el-form :model="registerForm" :rules="registerRules" ref="registerFormRef">
              <el-form-item prop="username">
                <el-input 
                  v-model="registerForm.username" 
                  placeholder="请输入用户名"
                  :prefix-icon="User"
                  size="large"
                />
              </el-form-item>
              <el-form-item prop="password">
                <el-input 
                  v-model="registerForm.password" 
                  type="password" 
                  placeholder="请输入密码"
                  :prefix-icon="Lock"
                  size="large"
                />
              </el-form-item>
              <el-form-item prop="confirmPassword">
                <el-input 
                  v-model="registerForm.confirmPassword" 
                  type="password" 
                  placeholder="请确认密码"
                  :prefix-icon="Lock"
                  size="large"
                />
              </el-form-item>
              <el-form-item prop="realName">
                <el-input 
                  v-model="registerForm.realName" 
                  placeholder="请输入真实姓名"
                  :prefix-icon="User"
                  size="large"
                />
              </el-form-item>
              <el-form-item>
                <el-button type="primary" @click="handleRegister" :loading="loading" size="large" class="submit-btn">
                  注册
                </el-button>
              </el-form-item>
            </el-form>
          </el-tab-pane>
        </el-tabs>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useUserStore } from '@/stores/userStore'
import { User, Lock, Monitor } from '@element-plus/icons-vue'

const userStore = useUserStore()
const activeTab = ref('login')
const loginFormRef = ref()
const registerFormRef = ref()
const loading = ref(false)

// 登录表单
const loginForm = reactive({
  username: '',
  password: ''
})

// 注册表单
const registerForm = reactive({
  username: '',
  password: '',
  confirmPassword: '',
  realName: ''
})

// 登录验证规则
const loginRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

// 注册验证规则
const registerRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value !== registerForm.password) {
          callback(new Error('两次输入密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ],
  realName: [{ required: true, message: '请输入真实姓名', trigger: 'blur' }]
}

// 登录处理
const handleLogin = async () => {
  try {
    const valid = await loginFormRef.value.validate()
    if (!valid) return
    
    loading.value = true
    await userStore.loginAction(loginForm)
  } catch (error) {
    console.error('登录失败:', error)
  } finally {
    loading.value = false
  }
}

// 注册处理
const handleRegister = async () => {
  try {
    const valid = await registerFormRef.value.validate()
    if (!valid) return
    
    loading.value = true
    
    // 注册成功后切换到登录tab
    await userStore.registerAction(registerForm)
    activeTab.value = 'login'
    loginForm.username = registerForm.username
    loginForm.password = ''
    
    // 清空注册表单
    registerForm.username = ''
    registerForm.password = ''
    registerForm.confirmPassword = ''
    registerForm.realName = ''
  } catch (error) {
    console.error('注册失败:', error)
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  overflow: hidden;
  background: linear-gradient(135deg, #0f2350 0%, #1a3a7a 50%, #2563eb 100%);
}

.login-background {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  opacity: 0.6;
  background-image: url("data:image/svg+xml,%3Csvg width='100' height='100' viewBox='0 0 100 100' xmlns='http://www.w3.org/2000/svg'%3E%3Cpath d='M11 18c3.866 0 7-3.134 7-7s-3.134-7-7-7-7 3.134-7 7 3.134 7 7 7zm48 25c3.866 0 7-3.134 7-7s-3.134-7-7-7-7 3.134-7 7 3.134 7 7 7zm-43-7c1.657 0 3-1.343 3-3s-1.343-3-3-3-3 1.343-3 3 1.343 3 3 3zm63 31c1.657 0 3-1.343 3-3s-1.343-3-3-3-3 1.343-3 3 1.343 3 3 3zM34 90c1.657 0 3-1.343 3-3s-1.343-3-3-3-3 1.343-3 3 1.343 3 3 3zm56-76c1.657 0 3-1.343 3-3s-1.343-3-3-3-3 1.343-3 3 1.343 3 3 3z' fill='%23ffffff' fill-opacity='0.08' fill-rule='evenodd'/%3E%3C/svg%3E"),
    radial-gradient(circle at 20% 30%, rgba(255, 255, 255, 0.15) 0%, transparent 40%), 
    radial-gradient(circle at 80% 70%, rgba(255, 255, 255, 0.1) 0%, transparent 30%),
    linear-gradient(to bottom right, rgba(37, 99, 235, 0.1) 0%, rgba(15, 35, 80, 0.2) 100%);
  background-position: center;
  background-size: 80px 80px, cover, cover, cover;
  animation: bgAnimation 40s linear infinite;
  backdrop-filter: blur(2px);
}

@keyframes bgAnimation {
  0% {
    background-position: 0 0, center, center, center;
  }
  100% {
    background-position: 500px 500px, center, center, center;
  }
}

.login-content {
  position: relative;
  z-index: 2;
  width: 100%;
  max-width: 450px;
  margin: 0 20px;
}

.login-card {
  border-radius: 16px;
  overflow: hidden;
  backdrop-filter: blur(10px);
  background-color: rgba(255, 255, 255, 0.95);
  box-shadow: 0 15px 35px rgba(0, 0, 0, 0.3);
}

.login-header {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-bottom: 16px;
  padding-top: 24px;
}

.logo-icon {
  font-size: 54px;
  margin-bottom: 16px;
  background: linear-gradient(135deg, #0f2350 0%, #2563eb 100%);
  border-radius: 12px;
  padding: 12px;
  color: white;
  box-shadow: 0 8px 16px rgba(15, 35, 80, 0.3);
}

.login-title {
  font-size: 28px;
  font-weight: 700;
  color: #0f2350;
  margin-bottom: 8px;
  text-align: center;
}

.login-description {
  text-align: center;
  margin-bottom: 32px;
  color: var(--text-secondary);
  font-size: 16px;
}

:deep(.el-tabs__active-bar) {
  background-color: #2563eb;
}

:deep(.el-tabs__item.is-active) {
  color: #2563eb;
}

:deep(.el-tabs__item:hover) {
  color: #1d4ed8;
}

:deep(.el-tabs__nav-wrap::after) {
  background-color: var(--border-color);
  height: 1px;
}

.el-form {
  margin-top: 20px;
  padding: 0 16px;
}

:deep(.el-input__wrapper) {
  border: none;
  box-shadow: 0 0 0 1px rgba(0, 0, 0, 0.12) inset;
  transition: all 0.3s ease;
  border-radius: 8px;
}

:deep(.el-input__wrapper:hover) {
  box-shadow: 0 0 0 1px #2563eb inset;
}

:deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 1px #1d4ed8 inset;
}

.submit-btn {
  width: 100%;
  height: 44px;
  border-radius: 8px;
  margin-top: 16px;
  font-weight: 500;
  background: linear-gradient(135deg, #0f2350 0%, #2563eb 100%);
  border: none;
  box-shadow: 0 4px 12px rgba(15, 35, 80, 0.4);
  transition: all 0.3s ease;
}

.submit-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 15px rgba(15, 35, 80, 0.5);
}

@media (max-width: 576px) {
  .login-card {
    border-radius: 12px;
  }
  
  .login-content {
    margin: 0 16px;
  }
  
  .login-header {
    padding-top: 16px;
  }
  
  .logo-icon {
    font-size: 40px;
    padding: 8px;
  }
  
  .login-title {
    font-size: 24px;
  }
  
  .login-description {
    font-size: 14px;
    margin-bottom: 24px;
  }
}
</style>