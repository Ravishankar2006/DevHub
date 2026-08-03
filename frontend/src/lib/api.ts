import axios from 'axios'

const api = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL ?? 'http://localhost:8080/api',
  headers: { 'Content-Type': 'application/json' },
})

// Attach JWT token to every request
api.interceptors.request.use(config => {
  const token = localStorage.getItem('devhub-token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

// Handle 401 — clear tokens and redirect to login
api.interceptors.response.use(
  res => res,
  err => {
    if (err.response?.status === 401) {
      localStorage.removeItem('devhub-token')
      localStorage.removeItem('devhub-refresh-token')
      window.location.href = '/login'
    }
    return Promise.reject(err)
  }
)

export default api
