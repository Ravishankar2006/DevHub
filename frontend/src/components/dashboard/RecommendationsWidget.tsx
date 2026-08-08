import { Link } from 'react-router-dom'
import { AlertCircle, CheckCircle2, Flame, Lightbulb, Target, FileText } from 'lucide-react'
import { useTasks } from '@/hooks/useTasks'
import { useGoals } from '@/hooks/useGoals'
import { useResumes } from '@/hooks/useResumes'
import { useHabits } from '@/hooks/useHabits'
import { useJobApplications } from '@/hooks/useCareers'
import type { Goal, Habit, JobApplication, Resume, Task } from '@/lib/types'

type Tone = 'amber' | 'blue' | 'gray'

interface Recommendation {
  icon: typeof AlertCircle
  tone: Tone
  text: string
  linkTo: string
}

const toneBadgeClass: Record<Tone, string> = {
  amber: 'badge-amber',
  blue: 'badge-blue',
  gray: 'badge-gray',
}

function buildRecommendations(
  tasks: Task[],
  activeGoals: Goal[],
  resumes: Resume[],
  habits: Habit[],
  applications: JobApplication[]
): Recommendation[] {
  const recs: Recommendation[] = []
  const now = new Date()
  const today = new Date(now.getFullYear(), now.getMonth(), now.getDate())

  // Tier 1: urgent
  const overdueTasks = tasks.filter(t => t.dueDate && t.status !== 'DONE' && new Date(t.dueDate) < today)
  if (overdueTasks.length > 0) {
    recs.push({
      icon: AlertCircle,
      tone: 'amber',
      text: `You have ${overdueTasks.length} overdue task${overdueTasks.length > 1 ? 's' : ''}`,
      linkTo: '/tasks',
    })
  }

  const soonDeadline = applications
    .filter(a => a.deadline)
    .map(a => ({ app: a, days: Math.ceil((new Date(a.deadline!).getTime() - today.getTime()) / 86400000) }))
    .filter(({ days }) => days >= 0 && days <= 3)
    .sort((a, b) => a.days - b.days)[0]
  if (soonDeadline) {
    const { app, days } = soonDeadline
    recs.push({
      icon: AlertCircle,
      tone: 'amber',
      text: `${app.roleTitle} at ${app.company.name} — deadline in ${days} day${days === 1 ? '' : 's'}`,
      linkTo: '/careers',
    })
  }

  // Tier 2: attention
  const atRiskHabit = habits.find(h => h.currentStreak > 0 && !h.checkedInToday)
  if (atRiskHabit) {
    recs.push({
      icon: Flame,
      tone: 'blue',
      text: `Don't break your streak on "${atRiskHabit.title}"`,
      linkTo: '/goals',
    })
  }

  if (resumes.length > 0 && !resumes.some(r => r.reviewScore != null)) {
    recs.push({
      icon: FileText,
      tone: 'blue',
      text: 'Get AI feedback on your resume',
      linkTo: '/resumes',
    })
  }

  // Tier 3: encouragement
  if (activeGoals.length === 0) {
    recs.push({
      icon: Target,
      tone: 'gray',
      text: 'Set a goal to stay focused',
      linkTo: '/goals',
    })
  }

  if (resumes.length === 0) {
    recs.push({
      icon: FileText,
      tone: 'gray',
      text: 'Upload a resume to get started',
      linkTo: '/resumes',
    })
  }

  return recs.slice(0, 3)
}

export default function RecommendationsWidget() {
  const { data: tasks, isLoading: tasksLoading } = useTasks()
  const { data: activeGoals, isLoading: goalsLoading } = useGoals('ACTIVE')
  const { data: resumes, isLoading: resumesLoading } = useResumes()
  const { data: habits, isLoading: habitsLoading } = useHabits()
  const { data: applications, isLoading: applicationsLoading } = useJobApplications()

  const isLoading = tasksLoading || goalsLoading || resumesLoading || habitsLoading || applicationsLoading

  const recommendations = isLoading
    ? []
    : buildRecommendations(tasks ?? [], activeGoals ?? [], resumes ?? [], habits ?? [], applications ?? [])

  return (
    <div className="card p-5 flex flex-col gap-3">
      <h3 className="text-sm font-semibold text-[var(--text-primary)] flex items-center gap-1.5">
        <Lightbulb size={15} className="text-brand-500" /> Recommendations
      </h3>

      {isLoading && <p className="text-xs text-[var(--text-muted)]">Loading...</p>}

      {!isLoading && recommendations.length === 0 && (
        <div className="flex items-center gap-1.5 text-sm text-[var(--text-secondary)]">
          <CheckCircle2 size={14} className="text-emerald-500" /> You're all caught up!
        </div>
      )}

      {recommendations.length > 0 && (
        <div className="flex flex-col gap-2">
          {recommendations.map((rec, i) => (
            <Link
              key={i}
              to={rec.linkTo}
              className="flex items-center gap-2 group"
            >
              <span className={toneBadgeClass[rec.tone]}>
                <rec.icon size={11} />
              </span>
              <p className="text-sm text-[var(--text-primary)] group-hover:underline truncate">{rec.text}</p>
            </Link>
          ))}
        </div>
      )}
    </div>
  )
}
