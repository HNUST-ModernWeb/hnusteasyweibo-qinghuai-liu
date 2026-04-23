import axios from 'axios'

const api = axios.create({
  baseURL: '/api',
  withCredentials: true
})

// 响应拦截器处理401
api.interceptors.response.use(
  response => response,
  error => {
    if (error.response?.status === 401) {
      // 触发全局登录事件
      window.dispatchEvent(new CustomEvent('require-login'))
    }
    return Promise.reject(error)
  }
)

export default api