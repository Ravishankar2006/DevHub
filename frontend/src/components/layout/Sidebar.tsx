import { NavLink, useNavigate } from 'react-router-dom'
import {
  LayoutDashboard, FolderKanban, CheckSquare, Target,
  BookOpen, Briefcase, FileText, Calendar, Search,
  Settings, LogOut, Bot, ChevronRight
} from 'lucide-react'
import { useAuth } from '@/contexts/AuthContext'
import { getInitials, cn } from '@/lib/utils'

const navItems = [
  { to: '/dashboard',  label: 'Dashboard',  icon: LayoutDashboard },
  { to: '/projects',   label: 'Projects',   icon: FolderKanban },
  { to: '/tasks',      label: 'Tasks',      icon: CheckSquare },
  { to: '/goals',      label: 'Goals',      icon: Target },
  { to: '/notes',      label: 'Notes',      icon: BookOpen },
  { to: '/learning',   label: 'Learning',   icon: BookOpen },
  { to: '/resumes',    label: 'Resumes',    icon: FileText },
  { to: '/careers',    label: 'Jobs',       icon: Briefcase },
  { to: '/calendar',   label: 'Calendar',   icon: Calendar },
  { to: '/search',     label: 'Search',     icon: Search },
]

const aiItems = [
  { to: '/ai/chat',    label: 'AI Assistant', icon: Bot },
]

interface SidebarProps {
  onClose?: () => void
}

export default function Sidebar({ onClose }: SidebarProps) {
  const { user, logout } = useAuth()
  const navigate = useNavigate()

  const handleLogout = () => {
    logout()
    navigate('/login')
  }

  const linkClass = ({ isActive }: { isActive: boolean }) =>
    cn(
      'flex items-center gap-3 px-3 py-2 rounded-lg text-sm font-medium transition-all duration-150 group',
      isActive
        ? 'bg-[var(--sidebar-active)] text-white'
        : 'text-[var(--sidebar-text)] hover:bg-white/5 hover:text-white'
    )

  return (
    <aside className="flex flex-col h-full bg-[var(--sidebar-bg)] border-r border-white/5 w-64">
      {/* Logo */}
      <div className="flex items-center gap-2.5 px-4 py-5 border-b border-white/5">
        <div className="w-8 h-8 rounded-lg bg-brand-500 flex items-center justify-center flex-shrink-0">
          <span className="text-white font-bold text-sm">D</span>
        </div>
        <div>
          <p className="text-white font-semibold text-sm">DevHub</p>
          <p className="text-[var(--sidebar-text)] text-xs">Developer OS</p>
        </div>
      </div>

      {/* Nav */}
      <nav className="flex-1 overflow-y-auto px-3 py-4 space-y-0.5">
        <p className="text-xs font-semibold text-[var(--text-muted)] uppercase tracking-wider px-3 mb-2">Workspace</p>
        {navItems.map(item => (
          <NavLink key={item.to} to={item.to} className={linkClass} onClick={onClose}>
            <item.icon size={16} className="flex-shrink-0" />
            <span className="flex-1">{item.label}</span>
            <ChevronRight size={12} className="opacity-0 group-hover:opacity-50 transition-opacity" />
          </NavLink>
        ))}

        <div className="pt-4 pb-1">
          <p className="text-xs font-semibold text-[var(--text-muted)] uppercase tracking-wider px-3 mb-2">Intelligence</p>
        </div>
        {aiItems.map(item => (
          <NavLink key={item.to} to={item.to} className={linkClass} onClick={onClose}>
            <item.icon size={16} className="flex-shrink-0" />
            <span className="flex-1">{item.label}</span>
          </NavLink>
        ))}
      </nav>

      {/* Footer */}
      <div className="p-3 border-t border-white/5 space-y-1">
        <NavLink to="/settings" className={linkClass}>
          <Settings size={16} />
          <span>Settings</span>
        </NavLink>
        {user && (
          <div className="flex items-center gap-3 px-3 py-2 mt-1">
            <div className="w-7 h-7 rounded-full bg-brand-500 flex items-center justify-center flex-shrink-0">
              <span className="text-white text-xs font-semibold">{getInitials(user.name)}</span>
            </div>
            <div className="flex-1 min-w-0">
              <p className="text-white text-xs font-medium truncate">{user.name}</p>
              <p className="text-[var(--sidebar-text)] text-xs truncate">{user.email}</p>
            </div>
            <button
              onClick={handleLogout}
              className="text-[var(--sidebar-text)] hover:text-red-400 transition-colors"
              title="Log out"
            >
              <LogOut size={14} />
            </button>
          </div>
        )}
      </div>
    </aside>
  )
}
