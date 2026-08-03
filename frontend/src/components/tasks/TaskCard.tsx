import { Trash2 } from 'lucide-react'
import { formatDate } from '@/lib/utils'
import { TASK_STATUSES, TASK_STATUS_LABELS, type Task, type TaskPriority, type TaskStatus } from '@/lib/types'

const priorityBadgeClass: Record<TaskPriority, string> = {
  LOW: 'badge-gray',
  MEDIUM: 'badge-blue',
  HIGH: 'badge-amber',
}

interface TaskCardProps {
  task: Task
  onEdit: () => void
  onDelete: () => void
  onStatusChange: (status: TaskStatus) => void
  showProject?: boolean
}

export default function TaskCard({ task, onEdit, onDelete, onStatusChange, showProject }: TaskCardProps) {
  return (
    <div className="card p-3.5 flex flex-col gap-2">
      <div className="flex items-start justify-between gap-2">
        <button onClick={onEdit} className="text-left flex-1 min-w-0">
          <p className="text-sm font-medium text-[var(--text-primary)] hover:text-brand-500 transition-colors">
            {task.title}
          </p>
        </button>
        <button
          onClick={onDelete}
          className="text-[var(--text-muted)] hover:text-red-500 transition-colors flex-shrink-0"
          aria-label="Delete task"
        >
          <Trash2 size={13} />
        </button>
      </div>

      {task.description && (
        <p className="text-xs text-[var(--text-secondary)] line-clamp-2">{task.description}</p>
      )}

      <div className="flex flex-wrap items-center gap-1.5">
        <span className={priorityBadgeClass[task.priority]}>{task.priority}</span>
        {showProject && task.projectName && <span className="badge-gray">{task.projectName}</span>}
        {task.milestoneTitle && <span className="badge-gray">{task.milestoneTitle}</span>}
        {task.dueDate && <span className="badge-gray">{formatDate(task.dueDate)}</span>}
      </div>

      <select
        value={task.status}
        onChange={e => onStatusChange(e.target.value as TaskStatus)}
        onClick={e => e.stopPropagation()}
        className="input text-xs py-1.5 mt-1"
      >
        {TASK_STATUSES.map(status => (
          <option key={status} value={status}>{TASK_STATUS_LABELS[status]}</option>
        ))}
      </select>
    </div>
  )
}
