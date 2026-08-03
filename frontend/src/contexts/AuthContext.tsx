import { createContext, useContext, useState, useEffect, type ReactNode } from 'react'
import api from '@/lib/api'

interface User {
  id: string
  email: string
  name: string
  role: string
  avatarUrl?: string
}

interface AuthContextType {
  user: User | null
  token: string | null
  isLoading: boolean
  isAuthenticated: boolean
  login: (email: string, password: string) => Promise<void>
  register: (name: string, email: string, password: string) => Promise<void>
  logout: () => void
}

const AuthContext = createContext<AuthContextType | undefined>(undefined)

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<User | null>(null)
  const [token, setToken] = useState<string | null>(() => localStorage.getItem('devhub-token'))
  const [isLoading, setIsLoading] = useState(true)

  // On mount, verify token and load user
  useEffect(() => {
    if (!token) {
      setIsLoading(false)
      return
    }
    api.get('/users/me')
      .then(res => setUser(res.data))
      .catch(() => {
        setToken(null)
        localStorage.removeItem('devhub-token')
        localStorage.removeItem('devhub-refresh-token')
      })
      .finally(() => setIsLoading(false))
  }, []) // eslint-disable-line react-hooks/exhaustive-deps

  const login = async (email: string, password: string) => {
    const res = await api.post('/auth/login', { email, password })
    const { accessToken, refreshToken, user: userData } = res.data
    setToken(accessToken)
    setUser(userData)
    localStorage.setItem('devhub-token', accessToken)
    localStorage.setItem('devhub-refresh-token', refreshToken)
  }

  const register = async (name: string, email: string, password: string) => {
    const res = await api.post('/auth/register', { name, email, password })
    const { accessToken, refreshToken, user: userData } = res.data
    setToken(accessToken)
    setUser(userData)
    localStorage.setItem('devhub-token', accessToken)
    localStorage.setItem('devhub-refresh-token', refreshToken)
  }

  const logout = () => {
    setUser(null)
    setToken(null)
    localStorage.removeItem('devhub-token')
    localStorage.removeItem('devhub-refresh-token')
  }

  return (
    <AuthContext.Provider value={{
      user, token, isLoading,
      isAuthenticated: !!user,
      login, register, logout,
    }}>
      {children}
    </AuthContext.Provider>
  )
}

export function useAuth(): AuthContextType {
  const ctx = useContext(AuthContext)
  if (!ctx) throw new Error('useAuth must be used inside AuthProvider')
  return ctx
}
