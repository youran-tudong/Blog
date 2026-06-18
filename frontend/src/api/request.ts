import axios from 'axios'

export interface ApiResult<T> {
  code: number
  message: string
  data: T
}

export const request = axios.create({
  baseURL: '/api',
  timeout: 15000,
})

request.interceptors.request.use((config) => {
  const token = localStorage.getItem('technote-token')
  if (token) {
    config.headers.Authorization = token
  }
  return config
})

request.interceptors.response.use(
  (response) => {
    if (response.config.responseType === 'blob' || response.config.responseType === 'arraybuffer') {
      return response
    }
    const result = response.data as ApiResult<unknown>
    if (result.code !== 200) {
      return Promise.reject(new Error(result.message || '请求失败'))
    }
    return response
  },
  (error) => Promise.reject(error),
)
