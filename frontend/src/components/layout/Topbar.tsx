import { Sun, Moon, Menu, Bell } from 'lucide-react'
import { useTheme } from '@/contexts/ThemeContext'
import { useAuth } from '@/contexts/AuthContext'
import { getInitials } from '@/lib/utils'

interface TopbarProps {
  onMenuClick: () => void
  title?: string
}

export default function Topbar({ onMenuClick, title }: TopbarProps) {
  const { theme, toggleTheme } = useTheme()
  const { user } = useAuth()

  return (
    <header className="h-14 flex items-center justify-between px-4 md:px-6 border-b border-[var(--border)] bg-[var(--bg-primary)] z-10 flex-shrink-0">
      <div className="flex items-center gap-3">
        {/* Mobile menu toggle */}
        <button
          id="topbar-menu-btn"
          onClick={onMenuClick}
          className="md:hidden btn-ghost p-1.5 rounded-lg"
          aria-label="Open menu"
        >
          <Menu size={18} />
        </button>
        {title && (
          <h1 className="text-base font-semibold text-[var(--text-primary)]">{title}</h1>
        )}
      </div>

      <div className="flex items-center gap-1">
        {/* Notifications */}
        <button
          id="topbar-notifications-btn"
          className="relative btn-ghost p-2 rounded-lg"
          aria-label="Notifications"
        >
          <Bell size={17} />
          <span className="absolute top-1.5 right-1.5 w-1.5 h-1.5 bg-brand-500 rounded-full" />
        </button>

        {/* Theme toggle */}
        <button
          id="topbar-theme-toggle"
          onClick={toggleTheme}
          className="btn-ghost p-2 rounded-lg"
          aria-label="Toggle theme"
        >
          {theme === 'dark' ? <Sun size={17} /> : <Moon size={17} />}
        </button>

        {/* Avatar */}
        {user && (
          <div className="w-7 h-7 rounded-full bg-brand-500 flex items-center justify-center ml-1 flex-shrink-0">
            <span className="text-white text-xs font-semibold">{getInitials(user.name)}</span>
          </div>
        )}
      </div>
    </header>
  )
}
