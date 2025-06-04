import { defineStore } from 'pinia'
import { ref } from 'vue'
import { login, register, getUserInfo } from '@/api/auth'
import { ElMessage } from 'element-plus'
import router from '@/router'
import { getToken, setToken, getUserInfo as getStoredUserInfo, setUserInfo, clearAuth, getUserRole } from '@/utils/auth'

export const useUserStore = defineStore('user', () => {
  // 状态
  const userInfo = ref(getStoredUserInfo())
  const token = ref(getToken() || '')
  const role = ref(getUserRole() || '')
  const isLogin = ref(!!token.value)

  // 登录
  const loginAction = async (loginForm) => {
    try {
      const res = await login(loginForm)
      token.value = res.data.token
      
      // 保存token
      setToken(token.value)
      
      // 获取完整的用户信息
      try {
        await getUserInfoAction()
      } catch (error) {
        console.error('获取用户信息失败:', error)
        ElMessage.warning('未获取到用户信息，请重新登录')
        logout()
        return Promise.reject(error)
      }
      
      isLogin.value = true
      ElMessage.success('登录成功')
      
      // 根据角色跳转到不同页面
      redirectByRole()
      
      return res
    } catch (error) {
      console.error('登录失败:', error)
      ElMessage.error(error.message || '登录失败')
      throw error
    }
  }

  // 注册
  const registerAction = async (registerForm) => {
    try {
      const res = await register(registerForm)
      ElMessage.success('注册成功，请登录')
      return res
    } catch (error) {
      console.error('注册失败:', error)
      ElMessage.error(error.message || '注册失败')
      throw error
    }
  }

  // 获取用户信息
  const getUserInfoAction = async () => {
    try {
      const res = await getUserInfo()
      userInfo.value = res.data
      role.value = res.data.role
      
      // 保存到localStorage
      setUserInfo(userInfo.value)
      
      return res
    } catch (error) {
      console.error('获取用户信息失败-getUserInfoAction:', error)
      throw error
    }
  }

  // 根据角色跳转
  const redirectByRole = () => {
    switch (role.value) {
      case 'INTERVIEWER':
        router.push('/interviewer/dashboard')
        break
      case 'HR':
        router.push('/hr/dashboard')
        break
      case 'CANDIDATE':
        router.push('/candidate/dashboard')
        break
      default:
        router.push('/login')
    }
  }

  // 登出
  const logout = () => {
    token.value = ''
    userInfo.value = null
    role.value = ''
    isLogin.value = false
    clearAuth()
    router.push('/login')
  }

  return {
    userInfo,
    token,
    role,
    isLogin,
    loginAction,
    registerAction,
    getUserInfoAction,
    redirectByRole,
    logout
  }
}) 