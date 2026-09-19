import axios from 'axios'

// 创建 axios 实例
const request = axios.create({
  baseURL: process.env.VUE_APP_BASE_URL || 'http://localhost:8080',
  timeout: 30000 // 管理端列表偶发慢，给足超时；分页优化后应远低于此
})

// 请求拦截器
request.interceptors.request.use(
  config => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.authentication = token
    }
    return config
  },
  error => {
    return Promise.reject(error)
  }
)

// 响应拦截器
request.interceptors.response.use(
  response => {
    const res = response.data
    if (res.code !== 1) {
      return Promise.reject(new Error(res.msg || '请求失败'))
    }
    return res
  },
  error => {
    // 处理 401 未授权错误
    if (error.response && error.response.status === 401) {
      console.error('Token 过期或无效，请重新登录')
      // 清除本地存储的 token
      localStorage.removeItem('token')
      // 跳转到登录页
      window.location.href = '/#/login'
    }
    return Promise.reject(error)
  }
)

export default request
