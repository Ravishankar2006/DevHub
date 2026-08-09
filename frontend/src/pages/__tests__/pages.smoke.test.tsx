import { describe, expect, it } from 'vitest'
import { screen, waitFor } from '@testing-library/react'
import { Route, Routes } from 'react-router-dom'
import { renderAuthenticated } from '@/test/test-utils'
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
import SearchPage from '@/pages/SearchPage'
import SettingsPage from '@/pages/SettingsPage'
import NotFoundPage from '@/pages/NotFoundPage'

describe('page smoke tests', () => {
  it('DashboardPage renders without crashing', async () => {
    renderAuthenticated(<DashboardPage />)
    await waitFor(() => expect(screen.getByText(/Recommendations/i)).toBeInTheDocument())
  })

  it('ProjectsPage renders without crashing', async () => {
    renderAuthenticated(<ProjectsPage />)
    await waitFor(() => expect(document.body).toBeTruthy())
  })

  it('ProjectDetailPage renders without crashing given a route param', async () => {
    renderAuthenticated(
      <Routes>
        <Route path="/projects/:id" element={<ProjectDetailPage />} />
      </Routes>,
      { route: '/projects/p1' }
    )
    await waitFor(() => expect(document.body).toBeTruthy())
  })

  it('TasksPage renders without crashing', async () => {
    renderAuthenticated(<TasksPage />)
    await waitFor(() => expect(document.body).toBeTruthy())
  })

  it('GoalsPage renders without crashing', async () => {
    renderAuthenticated(<GoalsPage />)
    await waitFor(() => expect(document.body).toBeTruthy())
  })

  it('NotesPage renders without crashing', async () => {
    renderAuthenticated(<NotesPage />)
    await waitFor(() => expect(document.body).toBeTruthy())
  })

  it('LearningPage renders without crashing', async () => {
    renderAuthenticated(<LearningPage />)
    await waitFor(() => expect(document.body).toBeTruthy())
  })

  it('ResumesPage renders without crashing', async () => {
    renderAuthenticated(<ResumesPage />)
    await waitFor(() => expect(document.body).toBeTruthy())
  })

  it('CareersPage renders without crashing', async () => {
    renderAuthenticated(<CareersPage />)
    await waitFor(() => expect(document.body).toBeTruthy())
  })

  it('CalendarPage renders without crashing', async () => {
    renderAuthenticated(<CalendarPage />)
    await waitFor(() => expect(document.body).toBeTruthy())
  })

  it('AIChatPage renders without crashing', async () => {
    renderAuthenticated(<AIChatPage />)
    await waitFor(() => expect(document.body).toBeTruthy())
  })

  it('SearchPage renders without crashing', async () => {
    renderAuthenticated(<SearchPage />)
    await waitFor(() => expect(screen.getByText(/Your documents/i)).toBeInTheDocument())
  })

  it('SettingsPage renders without crashing', async () => {
    renderAuthenticated(<SettingsPage />)
    await waitFor(() => expect(screen.getByText(/GitHub/i)).toBeInTheDocument())
  })

  it('NotFoundPage renders without crashing', async () => {
    renderAuthenticated(<NotFoundPage />)
    await waitFor(() => expect(document.body).toBeTruthy())
  })
})
