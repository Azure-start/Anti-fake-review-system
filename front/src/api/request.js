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
    
    // 检查token是否过期
    if (userStore.token && userStore.isTokenExpired(userStore.token)) {
      console.log('检测到Token已过期，执行登出')
      ElMessage.warning('登录已过期，请重新登录')
      userStore.logout()
      
      // 创建特殊错误，标记为token过期
      const error = new Error('Token已过期')
      error.code = 'TOKEN_EXPIRED'
      error.isTokenExpired = true
      return Promise.reject(error)
    }
    
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
        const errorMessage = data.message || data.msg || '请求失败'
        if (!isDev) {
          ElMessage.error(errorMessage)
        } else {
          console.warn('后端返回错误:', errorMessage, data)
        }
        const error = new Error(errorMessage)
        error.code = data.code
        error.response = data
        return Promise.reject(error)
      }
    }
    
    return data
  },
  (error) => {
    // 如果是token过期导致的错误，只有请求拦截器的提示，不显示其他错误
    if (error.isTokenExpired || error.code === 'TOKEN_EXPIRED') {
      console.log('Token过期错误已在请求拦截器处理，不显示额外错误')
      return Promise.reject(error)
    }
    
    console.error('响应错误:', error)
    
    if (error.response) {
      const { status, data } = error.response
      console.error('🌐 HTTP响应错误:', status, data)
      
      // 创建详细的错误对象
      const detailedError = new Error()
      detailedError.status = status
      detailedError.response = data
      detailedError.type = 'HTTP_ERROR'
      
      // 处理不同的状态码
      switch (status) {
        case 401:
          detailedError.message = '未授权，请重新登录'
          ElMessage.error(detailedError.message)
          const userStore = useUserStore()
          userStore.logout()
          // 可以在这里跳转到登录页
          break
        case 403:
          detailedError.message = '拒绝访问'
          ElMessage.error(detailedError.message)
          break
        case 404:
          detailedError.message = '请求的资源不存在'
          ElMessage.error(detailedError.message)
          break
        case 500:
          detailedError.message = `服务器错误: ${data?.message || data?.msg || '内部服务器错误'}`
          ElMessage.error(detailedError.message)
          break
        default:
          detailedError.message = data?.message || data?.msg || `HTTP错误 ${status}`
          ElMessage.error(detailedError.message)
      }
      
      return Promise.reject(detailedError)
    } else if (error.request) {
      // 网络错误，后端服务未启动
      console.error('🌐 网络错误：后端服务未启动', error.message)
      const networkError = new Error('后端服务未启动，请检查网络连接')
      networkError.type = 'NETWORK_ERROR'
      networkError.originalError = error
      ElMessage.error('后端服务未启动，无法获取交易记录')
      return Promise.reject(networkError)
    } else {
      console.error('⚙️ 请求配置错误:', error.message)
      const configError = new Error('请求配置错误')
      configError.type = 'CONFIG_ERROR'
      configError.originalError = error
      ElMessage.error('请求配置错误')
      return Promise.reject(configError)
    }
    
    return Promise.reject(error)
  }
)

export default request

