import { Plus } from 'lucide-react'
import TaskCard from './TaskCard'
import { TASK_STATUSES, TASK_STATUS_LABELS, type Task, type TaskStatus } from '@/lib/types'

interface KanbanBoardProps {
  tasks: Task[]
  onEdit: (task: Task) => void
  onDelete: (task: Task) => void
  onStatusChange: (task: Task, status: TaskStatus) => void
  onAddTask: (status: TaskStatus) => void
  showProject?: boolean
}

export default function KanbanBoard({ tasks, onEdit, onDelete, onStatusChange, onAddTask, showProject }: KanbanBoardProps) {
  return (
    <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
      {TASK_STATUSES.map(status => {
        const columnTasks = tasks.filter(t => t.status === status)
        return (
          <div key={status} className="flex flex-col gap-3 min-w-0">
            <div className="flex items-center justify-between px-1">
              <h3 className="text-sm font-semibold text-[var(--text-primary)]">
                {TASK_STATUS_LABELS[status]}
                <span className="ml-2 text-xs font-normal text-[var(--text-muted)]">{columnTasks.length}</span>
              </h3>
              <button
                onClick={() => onAddTask(status)}
                className="btn-ghost p-1 rounded-lg"
                aria-label={`Add task to ${TASK_STATUS_LABELS[status]}`}
              >
                <Plus size={15} />
              </button>
            </div>

            <div className="flex flex-col gap-2.5 min-h-16">
              {columnTasks.length === 0 && (
                <div className="card border-dashed p-4 text-center text-xs text-[var(--text-muted)]">
                  No tasks
                </div>
              )}
              {columnTasks.map(task => (
                <TaskCard
                  key={task.id}
                  task={task}
                  showProject={showProject}
                  onEdit={() => onEdit(task)}
                  onDelete={() => onDelete(task)}
                  onStatusChange={newStatus => onStatusChange(task, newStatus)}
                />
              ))}
            </div>
          </div>
        )
      })}
    </div>
  )
}
