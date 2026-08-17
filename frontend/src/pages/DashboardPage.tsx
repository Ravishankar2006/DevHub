import { useAuth } from '@/contexts/AuthContext'
import { useProjects } from '@/hooks/useProjects'
import { useTasks } from '@/hooks/useTasks'
import { useGoals } from '@/hooks/useGoals'
import { useLearningResources } from '@/hooks/useLearning'
import { FolderKanban, CheckSquare, Target, BookOpen } from 'lucide-react'
import DailyBriefWidget from '@/components/dashboard/DailyBriefWidget'
import RecommendationsWidget from '@/components/dashboard/RecommendationsWidget'
import DueSoonTasksWidget from '@/components/dashboard/DueSoonTasksWidget'
import UpcomingMilestonesWidget from '@/components/dashboard/UpcomingMilestonesWidget'
import TodaysHabitsWidget from '@/components/dashboard/TodaysHabitsWidget'
import RecentNotesWidget from '@/components/dashboard/RecentNotesWidget'
import ContinueLearningWidget from '@/components/dashboard/ContinueLearningWidget'
import UpcomingCalendarEventsWidget from '@/components/dashboard/UpcomingCalendarEventsWidget'
import GitHubActivityWidget from '@/components/dashboard/GitHubActivityWidget'
import LeetCodeActivityWidget from '@/components/dashboard/LeetCodeActivityWidget'
import RecentActivityWidget from '@/components/dashboard/RecentActivityWidget'

export default function DashboardPage() {
  const { user } = useAuth()
  const { data: projects } = useProjects()
  const { data: tasks } = useTasks()
  const { data: goals } = useGoals('ACTIVE')
  const { data: learningResources } = useLearningResources()

  const activeProjects = projects?.filter(p => !p.archived).length
  const openTasks = tasks?.filter(t => t.status !== 'DONE').length
  const activeGoals = goals?.length
  const learningItems = learningResources?.filter(r => r.status !== 'COMPLETED' && r.status !== 'ABANDONED').length

  const quickStats = [
    { label: 'Active Projects', value: activeProjects ?? '—', icon: FolderKanban, color: 'text-brand-500' },
    { label: 'Open Tasks',      value: openTasks ?? '—',      icon: CheckSquare,  color: 'text-emerald-500' },
    { label: 'Active Goals',    value: activeGoals ?? '—', icon: Target,       color: 'text-amber-500' },
    { label: 'Learning Items',  value: learningItems ?? '—', icon: BookOpen,     color: 'text-purple-500' },
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
        <div className="lg:col-span-2">
          <DailyBriefWidget />
        </div>
        <RecommendationsWidget />
        <DueSoonTasksWidget />
        <UpcomingMilestonesWidget />
        <TodaysHabitsWidget />
        <RecentNotesWidget />
        <ContinueLearningWidget />
        <UpcomingCalendarEventsWidget />
        <GitHubActivityWidget />
        <LeetCodeActivityWidget />
        <RecentActivityWidget />
      </div>
    </div>
  )
}
