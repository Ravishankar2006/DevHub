import { useState } from 'react'
import { FolderKanban, Plus } from 'lucide-react'
import ProjectCard from '@/components/projects/ProjectCard'
import ProjectFormModal from '@/components/projects/ProjectFormModal'
import { useDeleteProject, useProjects } from '@/hooks/useProjects'
import type { Project } from '@/lib/types'

export default function ProjectsPage() {
  const [includeArchived, setIncludeArchived] = useState(false)
  const { data: projects, isLoading } = useProjects(includeArchived)
  const deleteProject = useDeleteProject()

  const [showForm, setShowForm] = useState(false)
  const [editingProject, setEditingProject] = useState<Project | undefined>(undefined)

  const openCreate = () => {
    setEditingProject(undefined)
    setShowForm(true)
  }

  const openEdit = (project: Project) => {
    setEditingProject(project)
    setShowForm(true)
  }

  const closeForm = () => {
    setShowForm(false)
    setEditingProject(undefined)
  }

  const handleDelete = (project: Project) => {
    if (window.confirm(`Delete "${project.name}"? This will also delete its tasks and milestones. This cannot be undone.`)) {
      deleteProject.mutate(project.id)
    }
  }

  return (
    <div className="max-w-6xl mx-auto animate-fade-in">
      <div className="page-header flex items-center justify-between">
        <div>
          <h2 className="page-title">Projects</h2>
          <p className="page-subtitle">Manage your software projects, one workspace at a time.</p>
        </div>
        <button id="new-project-btn" onClick={openCreate} className="btn-primary text-sm">
          <Plus size={16} /> New Project
        </button>
      </div>

      <label className="flex items-center gap-2 text-sm text-[var(--text-secondary)] mb-5 cursor-pointer w-fit">
        <input
          type="checkbox"
          checked={includeArchived}
          onChange={e => setIncludeArchived(e.target.checked)}
          className="rounded"
        />
        Show archived
      </label>

      {isLoading && <p className="text-sm text-[var(--text-muted)]">Loading projects...</p>}

      {!isLoading && projects?.length === 0 && (
        <div className="card p-10 flex flex-col items-center text-center max-w-md mx-auto">
          <div className="w-12 h-12 rounded-xl bg-brand-500/10 flex items-center justify-center mb-4">
            <FolderKanban size={22} className="text-brand-500" />
          </div>
          <h3 className="text-lg font-semibold text-[var(--text-primary)] mb-2">No projects yet</h3>
          <p className="text-sm text-[var(--text-secondary)] mb-6">
            Create your first project to start tracking tasks and milestones.
          </p>
          <button onClick={openCreate} className="btn-primary text-sm">
            <Plus size={16} /> New Project
          </button>
        </div>
      )}

      {!isLoading && projects && projects.length > 0 && (
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4">
          {projects.map(project => (
            <ProjectCard
              key={project.id}
              project={project}
              onEdit={() => openEdit(project)}
              onDelete={() => handleDelete(project)}
            />
          ))}
        </div>
      )}

      {showForm && <ProjectFormModal project={editingProject} onClose={closeForm} />}
    </div>
  )
}
