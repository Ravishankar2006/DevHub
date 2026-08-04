import { Link } from 'react-router-dom'
import { CheckSquare } from 'lucide-react'
import { useTasks } from '@/hooks/useTasks'
import { formatRelativeTime } from '@/lib/utils'
import type { TaskPriority } from '@/lib/types'

const priorityBadgeClass: Record<TaskPriority, string> = {
  LOW: 'badge-gray',
  MEDIUM: 'badge-blue',
  HIGH: 'badge-amber',
}

export default function DueSoonTasksWidget() {
  const { data: tasks, isLoading } = useTasks()

  const dueSoon = (tasks ?? [])
    .filter(t => t.dueDate && t.status !== 'DONE')
    .sort((a, b) => new Date(a.dueDate!).getTime() - new Date(b.dueDate!).getTime())
    .slice(0, 5)

  return (
    <div className="card p-5 flex flex-col gap-3">
      <div className="flex items-center justify-between">
        <h3 className="text-sm font-semibold text-[var(--text-primary)] flex items-center gap-1.5">
          <CheckSquare size={15} className="text-emerald-500" /> Due soon
        </h3>
        <Link to="/tasks" className="text-xs text-brand-500 hover:underline">View all →</Link>
      </div>

      {isLoading && <p className="text-xs text-[var(--text-muted)]">Loading...</p>}

      {!isLoading && dueSoon.length === 0 && (
        <p className="text-xs text-[var(--text-muted)]">No tasks due soon.</p>
      )}

      {dueSoon.length > 0 && (
        <div className="flex flex-col gap-2">
          {dueSoon.map(task => (
            <div key={task.id} className="flex items-center justify-between gap-2">
              <p className="text-sm text-[var(--text-primary)] truncate flex-1">{task.title}</p>
              <div className="flex items-center gap-1.5 flex-shrink-0">
                <span className={priorityBadgeClass[task.priority]}>{task.priority}</span>
                <span className="text-xs text-[var(--text-muted)]">{formatRelativeTime(task.dueDate!)}</span>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  )
}
