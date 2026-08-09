import { lazy, Suspense } from 'react'
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { Loader2 } from 'lucide-react'
import { ThemeProvider } from '@/contexts/ThemeContext'
import { AuthProvider } from '@/contexts/AuthContext'
import ProtectedRoute from '@/components/ProtectedRoute'
import AppLayout from '@/components/layout/AppLayout'

const LoginPage = lazy(() => import('@/pages/auth/LoginPage'))
const RegisterPage = lazy(() => import('@/pages/auth/RegisterPage'))
const DashboardPage = lazy(() => import('@/pages/DashboardPage'))
const ProjectsPage = lazy(() => import('@/pages/ProjectsPage'))
const ProjectDetailPage = lazy(() => import('@/pages/ProjectDetailPage'))
const TasksPage = lazy(() => import('@/pages/TasksPage'))
const GoalsPage = lazy(() => import('@/pages/GoalsPage'))
const NotesPage = lazy(() => import('@/pages/NotesPage'))
const LearningPage = lazy(() => import('@/pages/LearningPage'))
const ResumesPage = lazy(() => import('@/pages/ResumesPage'))
const CareersPage = lazy(() => import('@/pages/CareersPage'))
const CalendarPage = lazy(() => import('@/pages/CalendarPage'))
const AIChatPage = lazy(() => import('@/pages/AIChatPage'))
const SearchPage = lazy(() => import('@/pages/SearchPage'))
const SettingsPage = lazy(() => import('@/pages/SettingsPage'))
const NotFoundPage = lazy(() => import('@/pages/NotFoundPage'))

const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      retry: 1,
      staleTime: 1000 * 60, // 1 minute
    },
  },
})

function PageLoading() {
  return (
    <div className="flex items-center justify-center h-64">
      <Loader2 size={24} className="animate-spin text-brand-500" />
    </div>
  )
}

export default function App() {
  return (
    <QueryClientProvider client={queryClient}>
      <ThemeProvider>
        <AuthProvider>
          <BrowserRouter>
            <Suspense fallback={<PageLoading />}>
              <Routes>
                {/* Public routes */}
                <Route path="/login"    element={<LoginPage />} />
                <Route path="/register" element={<RegisterPage />} />

                {/* Protected routes */}
                <Route element={<ProtectedRoute />}>
                  <Route element={<AppLayout />}>
                    <Route index element={<Navigate to="/dashboard" replace />} />
                    <Route path="/dashboard" element={<DashboardPage />} />
                    <Route path="/projects"  element={<ProjectsPage />} />
                    <Route path="/projects/:id" element={<ProjectDetailPage />} />
                    <Route path="/tasks"     element={<TasksPage />} />
                    <Route path="/goals"     element={<GoalsPage />} />
                    <Route path="/notes"     element={<NotesPage />} />
                    <Route path="/learning"  element={<LearningPage />} />
                    <Route path="/resumes"   element={<ResumesPage />} />
                    <Route path="/careers"   element={<CareersPage />} />
                    <Route path="/calendar"  element={<CalendarPage />} />
                    <Route path="/search"    element={<SearchPage />} />
                    <Route path="/ai/chat"   element={<AIChatPage />} />
                    <Route path="/settings"  element={<SettingsPage />} />
                  </Route>
                </Route>

                <Route path="*" element={<NotFoundPage />} />
              </Routes>
            </Suspense>
          </BrowserRouter>
        </AuthProvider>
      </ThemeProvider>
    </QueryClientProvider>
  )
}
