import { useAuth } from '@/contexts/AuthContext'
import { useProjects } from '@/hooks/useProjects'
import { useTasks } from '@/hooks/useTasks'
import { useGoals } from '@/hooks/useGoals'
import { FolderKanban, CheckSquare, Target, BookOpen } from 'lucide-react'
import DueSoonTasksWidget from '@/components/dashboard/DueSoonTasksWidget'
import UpcomingMilestonesWidget from '@/components/dashboard/UpcomingMilestonesWidget'
import TodaysHabitsWidget from '@/components/dashboard/TodaysHabitsWidget'
import RecentNotesWidget from '@/components/dashboard/RecentNotesWidget'

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

      {/* Overview widgets */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-5">
        <DueSoonTasksWidget />
        <UpcomingMilestonesWidget />
        <TodaysHabitsWidget />
        <RecentNotesWidget />
      </div>
    </div>
  )
}
