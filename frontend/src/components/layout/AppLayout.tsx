import { useState } from 'react'
import { Outlet, useLocation } from 'react-router-dom'
import Sidebar from './Sidebar'
import Topbar from './Topbar'

const pageTitles: Record<string, string> = {
  '/dashboard':  'Dashboard',
  '/projects':   'Projects',
  '/tasks':      'Tasks',
  '/goals':      'Goals',
  '/notes':      'Notes',
  '/learning':   'Learning',
  '/resumes':    'Resumes',
  '/careers':    'Jobs',
  '/calendar':   'Calendar',
  '/search':     'Semantic Search',
  '/ai/chat':    'AI Assistant',
  '/settings':   'Settings',
}

export default function AppLayout() {
  const [sidebarOpen, setSidebarOpen] = useState(false)
  const { pathname } = useLocation()

  const title = Object.entries(pageTitles).find(([path]) =>
    pathname === path || pathname.startsWith(path + '/')
  )?.[1] ?? 'DevHub'

  return (
    <div className="flex h-screen overflow-hidden bg-[var(--bg-primary)]">
      {/* Desktop sidebar */}
      <div className="hidden md:flex flex-shrink-0">
        <Sidebar />
      </div>

      {/* Mobile sidebar overlay */}
      {sidebarOpen && (
        <div className="fixed inset-0 z-40 flex md:hidden">
          <div
            className="absolute inset-0 bg-black/50"
            onClick={() => setSidebarOpen(false)}
          />
          <div className="relative z-50 flex-shrink-0 animate-slide-up">
            <Sidebar onClose={() => setSidebarOpen(false)} />
          </div>
        </div>
      )}

      {/* Main content */}
      <div className="flex flex-col flex-1 min-w-0 overflow-hidden">
        <Topbar onMenuClick={() => setSidebarOpen(true)} title={title} />
        <main className="flex-1 overflow-y-auto p-4 md:p-6 animate-fade-in">
          <Outlet />
        </main>
      </div>
    </div>
  )
}
