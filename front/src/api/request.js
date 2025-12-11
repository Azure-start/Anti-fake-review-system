import axios from 'axios'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/userStore'

// 创建axios实例
const request = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '/api',
  timeout: 10000
})

// 请求拦截器
request.interceptors.request.use(
  (config) => {
    const userStore = useUserStore()
    
    // 添加token到请求头（开发模式下可选）
    if (userStore.token) {
      config.headers.Authorization = `Bearer ${userStore.token}`
    } else if (import.meta.env.MODE === 'development') {
      // 开发模式下，如果没有token也不强制要求
      console.log('开发模式：请求未携带token')
    }
    
    return config
  },
  (error) => {
    console.error('请求错误:', error)
    return Promise.reject(error)
  }
)

// 响应拦截器
request.interceptors.response.use(
  (response) => {
    const { data } = response
    
    // 如果后端返回的数据结构是 { code, data, message }
    if (data.code !== undefined) {
      if (data.code === 0 || data.code === 200) {
        return data.data || data
      } else {
        // 开发模式下不显示错误，避免后端未启动时看到大量错误提示
        const isDev = import.meta.env.MODE === 'development'
        if (!isDev) {
          ElMessage.error(data.message || '请求失败')
        } else {
          console.warn('后端返回错误:', data.message || '请求失败', data)
        }
        return Promise.reject(new Error(data.message || '请求失败'))
      }
    }
    
    return data
  },
  (error) => {
    console.error('响应错误:', error)
    
    if (error.response) {
      const { status, data } = error.response
      
      // 处理不同的状态码
      switch (status) {
        case 401:
          ElMessage.error('未授权，请重新登录')
          const userStore = useUserStore()
          userStore.logout()
          // 可以在这里跳转到登录页
          break
        case 403:
          ElMessage.error('拒绝访问')
          break
        case 404:
          ElMessage.error('请求的资源不存在')
          break
        case 500:
          ElMessage.error('服务器错误')
          break
        default:
          ElMessage.error(data?.message || '请求失败')
      }
    } else if (error.request) {
      // 网络错误，后端服务未启动
      console.error('网络错误：后端服务未启动', error.message)
      ElMessage.error('后端服务未启动，无法获取交易记录')
    } else {
      console.error('请求配置错误:', error.message)
      ElMessage.error('请求配置错误')
    }
    
    return Promise.reject(error)
  }
)

export default request

