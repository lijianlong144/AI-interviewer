import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '@/router'
import { getToken } from '@/utils/auth'

// 创建axios实例
const request = axios.create({
  baseURL: 'http://localhost:8083/api',
  timeout: 10000
})

// 请求拦截器
request.interceptors.request.use(
  config => {
    const token = getToken()
    if (token) {
      config.headers['Authorization'] = `Bearer ${token}`
    }
    return config
  },
  error => {
    console.error(error)
    return Promise.reject(error)
  }
)

// 响应拦截器
request.interceptors.response.use(
  response => {
    const res = response.data
    
    // 确保返回的数据符合预期结构
    if (!res || typeof res !== 'object') {
      ElMessage.error('服务器返回数据格式错误')
      return Promise.reject(new Error('服务器返回数据格式错误'))
    }
    
    if (res.code !== 200) {
      ElMessage.error(res.message || '请求失败')
      
      // 401 未授权，跳转登录
      if (res.code === 401) {
        // 清除token
        localStorage.removeItem('token')
        localStorage.removeItem('userInfo')
        router.push('/login')
      }
      
      return Promise.reject(new Error(res.message || 'Error'))
    }
    
    // 确保data字段存在
    if (res.data === undefined) {
      console.warn('API返回的数据中没有data字段', res)
    }
    
    return res
  },
  error => {
    console.error('请求错误：', error)
    
    // 处理网络错误或超时
    if (error.message.includes('timeout')) {
      ElMessage.error('请求超时，请检查网络连接')
    } else if (error.message.includes('Network Error')) {
      ElMessage.error('网络错误，请检查网络连接')
    } else {
      ElMessage.error(error.message || '网络错误')
    }
    
    return Promise.reject(error)
  }
)

export default request