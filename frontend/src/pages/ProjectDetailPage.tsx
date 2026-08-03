import { useState } from 'react'
import { Link, useNavigate, useParams } from 'react-router-dom'
import { ArrowLeft, ExternalLink, GitBranch, Pencil, Plus, Target, Trash2 } from 'lucide-react'
import ProjectFormModal from '@/components/projects/ProjectFormModal'
import MilestoneFormModal from '@/components/projects/MilestoneFormModal'
import KanbanBoard from '@/components/tasks/KanbanBoard'
import TaskFormModal from '@/components/tasks/TaskFormModal'
import { useDeleteProject, useProject } from '@/hooks/useProjects'
import { useDeleteMilestone, useMilestones } from '@/hooks/useMilestones'
import { useDeleteTask, useTasks, useUpdateTask } from '@/hooks/useTasks'
import { PROJECT_STATUS_LABELS, type Milestone, type Task, type TaskStatus } from '@/lib/types'

export default function ProjectDetailPage() {
  const { id } = useParams<{ id: string }>()
  const navigate = useNavigate()

  const { data: project, isLoading } = useProject(id)
  const { data: milestones } = useMilestones(id)
  const { data: tasks } = useTasks({ projectId: id })

  const deleteProject = useDeleteProject()
  const deleteMilestone = useDeleteMilestone(id ?? '')
  const deleteTask = useDeleteTask()
  const updateTask = useUpdateTask()

  const [showProjectForm, setShowProjectForm] = useState(false)
  const [showMilestoneForm, setShowMilestoneForm] = useState(false)
  const [editingMilestone, setEditingMilestone] = useState<Milestone | undefined>(undefined)
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

  if (isLoading) {
    return <p className="text-sm text-[var(--text-muted)]">Loading project...</p>
  }

  if (!project) {
    return (
      <div className="text-center py-12">
        <p className="text-sm text-[var(--text-secondary)]">Project not found.</p>
        <Link to="/projects" className="text-brand-500 hover:text-brand-600 text-sm font-medium mt-2 inline-block">
          Back to projects
        </Link>
      </div>
    )
  }

  const handleDeleteProject = () => {
    if (window.confirm(`Delete "${project.name}"? This will also delete its tasks and milestones. This cannot be undone.`)) {
      deleteProject.mutate(project.id, { onSuccess: () => navigate('/projects') })
    }
  }

  const handleDeleteMilestone = (milestone: Milestone) => {
    if (window.confirm(`Delete milestone "${milestone.title}"? Linked tasks will be unlinked, not deleted.`)) {
      deleteMilestone.mutate(milestone.id)
    }
  }

  return (
    <div className="max-w-6xl mx-auto animate-fade-in">
      <Link to="/projects" className="inline-flex items-center gap-1.5 text-sm text-[var(--text-secondary)] hover:text-[var(--text-primary)] transition-colors mb-4">
        <ArrowLeft size={14} /> Back to projects
      </Link>

      <div className="page-header flex items-start justify-between gap-4">
        <div className="min-w-0">
          <div className="flex items-center gap-3 flex-wrap">
            <h2 className="page-title">{project.name}</h2>
            <span className="badge-blue">{PROJECT_STATUS_LABELS[project.status]}</span>
            {project.archived && <span className="badge-gray">Archived</span>}
          </div>
          {project.description && <p className="page-subtitle">{project.description}</p>}
          <div className="flex items-center gap-3 mt-2">
            {project.repoUrl && (
              <a href={project.repoUrl} target="_blank" rel="noreferrer" className="inline-flex items-center gap-1 text-xs text-[var(--text-secondary)] hover:text-brand-500 transition-colors">
                <GitBranch size={13} /> Repository
              </a>
            )}
            {project.liveUrl && (
              <a href={project.liveUrl} target="_blank" rel="noreferrer" className="inline-flex items-center gap-1 text-xs text-[var(--text-secondary)] hover:text-brand-500 transition-colors">
                <ExternalLink size={13} /> Live site
              </a>
            )}
          </div>
        </div>
        <div className="flex gap-2 flex-shrink-0">
          <button onClick={() => setShowProjectForm(true)} className="btn-secondary text-sm">
            <Pencil size={14} /> Edit
          </button>
          <button onClick={handleDeleteProject} className="btn-secondary text-sm text-red-500">
            <Trash2 size={14} /> Delete
          </button>
        </div>
      </div>

      {project.stackTags.length > 0 && (
        <div className="flex flex-wrap gap-1.5 mb-6">
          {project.stackTags.map(tag => (
            <span key={tag} className="badge-gray">{tag}</span>
          ))}
        </div>
      )}

      {project.roadmap && (
        <div className="card p-5 mb-6">
          <h3 className="section-title">Roadmap & Notes</h3>
          <p className="text-sm text-[var(--text-secondary)] whitespace-pre-wrap">{project.roadmap}</p>
        </div>
      )}

      <div className="card p-5 mb-6">
        <div className="flex items-center justify-between mb-4">
          <h3 className="section-title mb-0">Milestones</h3>
          <button
            onClick={() => { setEditingMilestone(undefined); setShowMilestoneForm(true) }}
            className="btn-ghost text-xs"
          >
            <Plus size={14} /> Add milestone
          </button>
        </div>

        {(!milestones || milestones.length === 0) && (
          <p className="text-sm text-[var(--text-muted)]">No milestones yet.</p>
        )}

        <div className="space-y-3">
          {milestones?.map(milestone => (
            <div key={milestone.id} className="flex items-center gap-3">
              <Target size={15} className="text-[var(--text-muted)] flex-shrink-0" />
              <div className="flex-1 min-w-0">
                <div className="flex items-center justify-between gap-2">
                  <p className="text-sm font-medium text-[var(--text-primary)] truncate">{milestone.title}</p>
                  <span className="text-xs text-[var(--text-muted)] flex-shrink-0">
                    {milestone.completedTasks}/{milestone.totalTasks} · {milestone.progressPercent}%
                  </span>
                </div>
                <div className="w-full h-1.5 rounded-full bg-[var(--bg-tertiary)] overflow-hidden mt-1.5">
                  <div className="h-full bg-brand-500 transition-all" style={{ width: `${milestone.progressPercent}%` }} />
                </div>
              </div>
              <button
                onClick={() => { setEditingMilestone(milestone); setShowMilestoneForm(true) }}
                className="btn-ghost p-1.5 rounded-lg flex-shrink-0"
                aria-label="Edit milestone"
              >
                <Pencil size={13} />
              </button>
              <button
                onClick={() => handleDeleteMilestone(milestone)}
                className="btn-ghost p-1.5 rounded-lg flex-shrink-0 text-red-500"
                aria-label="Delete milestone"
              >
                <Trash2 size={13} />
              </button>
            </div>
          ))}
        </div>
      </div>

      <div>
        <h3 className="section-title">Tasks</h3>
        <KanbanBoard
          tasks={tasks ?? []}
          onEdit={task => { setEditingTask(task); setShowTaskForm(true) }}
          onDelete={task => {
            if (window.confirm(`Delete task "${task.title}"?`)) deleteTask.mutate(task.id)
          }}
          onStatusChange={updateTaskStatus}
          onAddTask={status => { setEditingTask(undefined); setNewTaskStatus(status); setShowTaskForm(true) }}
        />
      </div>

      {showProjectForm && <ProjectFormModal project={project} onClose={() => setShowProjectForm(false)} />}
      {showMilestoneForm && id && (
        <MilestoneFormModal
          projectId={id}
          milestone={editingMilestone}
          onClose={() => { setShowMilestoneForm(false); setEditingMilestone(undefined) }}
        />
      )}
      {showTaskForm && (
        <TaskFormModal
          task={editingTask}
          defaultProjectId={id}
          defaultStatus={newTaskStatus}
          onClose={() => { setShowTaskForm(false); setEditingTask(undefined) }}
        />
      )}
    </div>
  )
}
