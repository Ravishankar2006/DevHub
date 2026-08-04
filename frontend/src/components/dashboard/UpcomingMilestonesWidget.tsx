import { Link } from 'react-router-dom'
import { Milestone as MilestoneIcon } from 'lucide-react'
import { useAllMilestones } from '@/hooks/useMilestones'
import { formatRelativeTime } from '@/lib/utils'

export default function UpcomingMilestonesWidget() {
  const { data: milestones, isLoading } = useAllMilestones()

  const upcoming = (milestones ?? [])
    .filter(m => m.dueDate && m.progressPercent < 100)
    .sort((a, b) => new Date(a.dueDate!).getTime() - new Date(b.dueDate!).getTime())
    .slice(0, 5)

  return (
    <div className="card p-5 flex flex-col gap-3">
      <div className="flex items-center justify-between">
        <h3 className="text-sm font-semibold text-[var(--text-primary)] flex items-center gap-1.5">
          <MilestoneIcon size={15} className="text-brand-500" /> Upcoming milestones
        </h3>
        <Link to="/projects" className="text-xs text-brand-500 hover:underline">View all →</Link>
      </div>

      {isLoading && <p className="text-xs text-[var(--text-muted)]">Loading...</p>}

      {!isLoading && upcoming.length === 0 && (
        <p className="text-xs text-[var(--text-muted)]">No upcoming milestones.</p>
      )}

      {upcoming.length > 0 && (
        <div className="flex flex-col gap-3">
          {upcoming.map(milestone => (
            <div key={milestone.id} className="flex flex-col gap-1">
              <div className="flex items-center justify-between gap-2">
                <p className="text-sm text-[var(--text-primary)] truncate flex-1">{milestone.title}</p>
                <span className="text-xs text-[var(--text-muted)] flex-shrink-0">{formatRelativeTime(milestone.dueDate!)}</span>
              </div>
              <div className="flex items-center gap-2">
                <span className="badge-gray flex-shrink-0">{milestone.projectName}</span>
                <div className="flex-1 h-1.5 rounded-full bg-[var(--bg-tertiary)] overflow-hidden">
                  <div className="h-full bg-brand-500 transition-all" style={{ width: `${milestone.progressPercent}%` }} />
                </div>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  )
}
