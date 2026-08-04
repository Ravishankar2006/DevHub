import { useAuth } from '@/contexts/AuthContext'
import { useProjects } from '@/hooks/useProjects'
import { useTasks } from '@/hooks/useTasks'
import { useGoals } from '@/hooks/useGoals'
import { FolderKanban, CheckSquare, Target, BookOpen, Zap } from 'lucide-react'

export default function DashboardPage() {
  const { user } = useAuth()
  const { data: projects } = useProjects()
  const { data: tasks } = useTasks()
  const { data: goals } = useGoals('ACTIVE')

  const activeProjects = projects?.filter(p => !p.archived).length
  const openTasks = tasks?.filter(t => t.status !== 'DONE').length
  const activeGoals = goals?.length

  const quickStats = [
    { label: 'Active Projects', value: activeProjects ?? '—', icon: FolderKanban, color: 'text-brand-500' },
    { label: 'Open Tasks',      value: openTasks ?? '—',      icon: CheckSquare,  color: 'text-emerald-500' },
    { label: 'Active Goals',    value: activeGoals ?? '—', icon: Target,       color: 'text-amber-500' },
    { label: 'Learning Items',  value: '—', icon: BookOpen,     color: 'text-purple-500' },
  ]

  const hour = new Date().getHours()
  const greeting = hour < 12 ? 'Good morning' : hour < 17 ? 'Good afternoon' : 'Good evening'

  return (
    <div className="max-w-6xl mx-auto animate-fade-in">
      {/* Header */}
      <div className="page-header">
        <h2 className="page-title">{greeting}, {user?.name?.split(' ')[0]} 👋</h2>
        <p className="page-subtitle">Here's what's happening in your workspace today.</p>
      </div>

      {/* Quick stats */}
      <div className="grid grid-cols-2 lg:grid-cols-4 gap-4 mb-8">
        {quickStats.map(stat => (
          <div key={stat.label} className="card p-5">
            <div className="flex items-start justify-between mb-3">
              <stat.icon size={18} className={stat.color} />
            </div>
            <p className="text-2xl font-semibold text-[var(--text-primary)]">{stat.value}</p>
            <p className="text-xs text-[var(--text-secondary)] mt-1">{stat.label}</p>
          </div>
        ))}
      </div>

      {/* Welcome card — shown until real data is available */}
      <div className="card p-8 flex flex-col items-center text-center max-w-lg mx-auto">
        <div className="w-12 h-12 rounded-xl bg-brand-500/10 flex items-center justify-center mb-4">
          <Zap size={22} className="text-brand-500" />
        </div>
        <h3 className="text-lg font-semibold text-[var(--text-primary)] mb-2">
          Your workspace is ready
        </h3>
        <p className="text-sm text-[var(--text-secondary)] leading-relaxed mb-6">
          Start by creating a project, setting a goal, or adding your first note.
          Your dashboard will populate as you build your data.
        </p>
        <div className="flex gap-3">
          <a href="/projects" className="btn-primary text-sm">New Project</a>
          <a href="/goals" className="btn-secondary text-sm">Set a Goal</a>
        </div>
      </div>
    </div>
  )
}
