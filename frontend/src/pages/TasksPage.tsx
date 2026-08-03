import { useState } from 'react'
import { Plus } from 'lucide-react'
import KanbanBoard from '@/components/tasks/KanbanBoard'
import TaskFormModal from '@/components/tasks/TaskFormModal'
import { useDeleteTask, useTasks, useUpdateTask } from '@/hooks/useTasks'
import { useProjects } from '@/hooks/useProjects'
import type { Task, TaskStatus } from '@/lib/types'

export default function TasksPage() {
  const [projectFilter, setProjectFilter] = useState<string>('')
  const { data: projects } = useProjects()
  const { data: tasks, isLoading } = useTasks(projectFilter ? { projectId: projectFilter } : {})

  const deleteTask = useDeleteTask()
  const updateTask = useUpdateTask()

  const [showTaskForm, setShowTaskForm] = useState(false)
  const [editingTask, setEditingTask] = useState<Task | undefined>(undefined)
  const [newTaskStatus, setNewTaskStatus] = useState<TaskStatus>('TODO')

  const updateTaskStatus = (task: Task, status: TaskStatus) => {
    updateTask.mutate({
      id: task.id,
      title: task.title,
      description: task.description ?? undefined,
      projectId: task.projectId,
      milestoneId: task.milestoneId,
      status,
      priority: task.priority,
      dueDate: task.dueDate,
    })
  }

  return (
    <div className="max-w-6xl mx-auto animate-fade-in">
      <div className="page-header flex items-center justify-between gap-4 flex-wrap">
        <div>
          <h2 className="page-title">Tasks</h2>
          <p className="page-subtitle">All your tasks, across every project, in one board.</p>
        </div>
        <div className="flex items-center gap-3">
          <select
            value={projectFilter}
            onChange={e => setProjectFilter(e.target.value)}
            className="input text-sm w-48"
          >
            <option value="">All projects</option>
            {projects?.map(p => (
              <option key={p.id} value={p.id}>{p.name}</option>
            ))}
          </select>
          <button
            id="new-task-btn"
            onClick={() => { setEditingTask(undefined); setNewTaskStatus('TODO'); setShowTaskForm(true) }}
            className="btn-primary text-sm"
          >
            <Plus size={16} /> New Task
          </button>
        </div>
      </div>

      {isLoading && <p className="text-sm text-[var(--text-muted)]">Loading tasks...</p>}

      {!isLoading && (
        <KanbanBoard
          tasks={tasks ?? []}
          showProject
          onEdit={task => { setEditingTask(task); setShowTaskForm(true) }}
          onDelete={task => {
            if (window.confirm(`Delete task "${task.title}"?`)) deleteTask.mutate(task.id)
          }}
          onStatusChange={updateTaskStatus}
          onAddTask={status => { setEditingTask(undefined); setNewTaskStatus(status); setShowTaskForm(true) }}
        />
      )}

      {showTaskForm && (
        <TaskFormModal
          task={editingTask}
          defaultProjectId={projectFilter || undefined}
          defaultStatus={newTaskStatus}
          onClose={() => { setShowTaskForm(false); setEditingTask(undefined) }}
        />
      )}
    </div>
  )
}
