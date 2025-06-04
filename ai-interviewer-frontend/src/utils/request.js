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
    if (res.code !== 200) {
      ElMessage.error(res.message || '请求失败')
      
      // 401 未授权，跳转登录
      if (res.code === 401) {
        router.push('/login')
      }
      
      return Promise.reject(new Error(res.message || 'Error'))
    }
    return res
  },
  error => {
    console.error('请求错误：', error)
    ElMessage.error(error.message || '网络错误')
    return Promise.reject(error)
  }
)

export default request