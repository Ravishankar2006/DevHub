import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { ThemeProvider } from '@/contexts/ThemeContext'
import { AuthProvider } from '@/contexts/AuthContext'
import ProtectedRoute from '@/components/ProtectedRoute'
import AppLayout from '@/components/layout/AppLayout'
import LoginPage from '@/pages/auth/LoginPage'
import RegisterPage from '@/pages/auth/RegisterPage'
import DashboardPage from '@/pages/DashboardPage'
import ProjectsPage from '@/pages/ProjectsPage'
import ProjectDetailPage from '@/pages/ProjectDetailPage'
import TasksPage from '@/pages/TasksPage'
import GoalsPage from '@/pages/GoalsPage'
import NotesPage from '@/pages/NotesPage'
import LearningPage from '@/pages/LearningPage'
import ResumesPage from '@/pages/ResumesPage'
import CareersPage from '@/pages/CareersPage'
import CalendarPage from '@/pages/CalendarPage'
import AIChatPage from '@/pages/AIChatPage'
import SettingsPage from '@/pages/SettingsPage'
import NotFoundPage from '@/pages/NotFoundPage'

const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      retry: 1,
      staleTime: 1000 * 60, // 1 minute
    },
  },
})

// Stub page for modules not yet built
function ComingSoon({ name }: { name: string }) {
  return (
    <div className="flex items-center justify-center h-64">
      <div className="text-center">
        <p className="text-4xl mb-3">🚧</p>
        <p className="text-lg font-semibold text-[var(--text-primary)]">{name}</p>
        <p className="text-sm text-[var(--text-secondary)] mt-1">Coming in Phase 1</p>
      </div>
    </div>
  )
}

export default function App() {
  return (
    <QueryClientProvider client={queryClient}>
      <ThemeProvider>
        <AuthProvider>
          <BrowserRouter>
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
                  <Route path="/search"    element={<ComingSoon name="Semantic Search" />} />
                  <Route path="/ai/chat"   element={<AIChatPage />} />
                  <Route path="/settings"  element={<SettingsPage />} />
                </Route>
              </Route>

              <Route path="*" element={<NotFoundPage />} />
            </Routes>
          </BrowserRouter>
        </AuthProvider>
      </ThemeProvider>
    </QueryClientProvider>
  )
}
