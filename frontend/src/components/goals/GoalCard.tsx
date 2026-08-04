import { Calendar, FolderKanban, Pencil, Trash2 } from 'lucide-react'
import { GOAL_STATUS_LABELS, GOAL_TYPE_LABELS, type Goal, type GoalStatus } from '@/lib/types'

const statusBadgeClass: Record<GoalStatus, string> = {
  ACTIVE: 'badge-blue',
  COMPLETED: 'badge-green',
  ABANDONED: 'badge-gray',
}

interface GoalCardProps {
  goal: Goal
  onEdit: () => void
  onDelete: () => void
}

export default function GoalCard({ goal, onEdit, onDelete }: GoalCardProps) {
  return (
    <div className="card p-5 flex flex-col gap-3">
      <div className="flex items-start justify-between gap-2">
        <h3 className="font-semibold text-[var(--text-primary)] truncate">{goal.title}</h3>
        <div className="flex items-center gap-1 flex-shrink-0">
          <button onClick={onEdit} className="btn-ghost p-1.5 rounded-lg" aria-label="Edit goal">
            <Pencil size={13} />
          </button>
          <button onClick={onDelete} className="btn-ghost p-1.5 rounded-lg text-red-500" aria-label="Delete goal">
            <Trash2 size={13} />
          </button>
        </div>
      </div>

      {goal.description && (
        <p className="text-sm text-[var(--text-secondary)] line-clamp-2">{goal.description}</p>
      )}

      <div className="flex items-center gap-2 flex-wrap">
        <span className={statusBadgeClass[goal.status]}>{GOAL_STATUS_LABELS[goal.status]}</span>
        <span className="badge-gray">{GOAL_TYPE_LABELS[goal.type]}</span>
        {goal.projectName && (
          <span className="badge-gray inline-flex items-center gap-1">
            <FolderKanban size={11} /> {goal.projectName}
          </span>
        )}
      </div>

      <div>
        <div className="flex items-center justify-between text-xs text-[var(--text-muted)] mb-1.5">
          <span>Progress</span>
          <span>{goal.progressPercent}%</span>
        </div>
        <div className="w-full h-1.5 rounded-full bg-[var(--bg-tertiary)] overflow-hidden">
          <div className="h-full bg-brand-500 transition-all" style={{ width: `${goal.progressPercent}%` }} />
        </div>
      </div>

      {goal.targetDate && (
        <div className="flex items-center gap-1.5 text-xs text-[var(--text-muted)]">
          <Calendar size={12} /> Target: {goal.targetDate}
        </div>
      )}
    </div>
  )
}
