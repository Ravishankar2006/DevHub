import { Link } from 'react-router-dom'
import { ExternalLink, GitBranch, MoreVertical, Pencil, Trash2 } from 'lucide-react'
import { useState } from 'react'
import { PROJECT_STATUS_LABELS, type Project, type ProjectStatus } from '@/lib/types'

const statusBadgeClass: Record<ProjectStatus, string> = {
  PLANNING: 'badge-gray',
  IN_PROGRESS: 'badge-blue',
  ON_HOLD: 'badge-amber',
  COMPLETED: 'badge-green',
}

interface ProjectCardProps {
  project: Project
  onEdit: () => void
  onDelete: () => void
}

export default function ProjectCard({ project, onEdit, onDelete }: ProjectCardProps) {
  const [menuOpen, setMenuOpen] = useState(false)
  const progress = project.taskCount === 0 ? 0 : Math.round((project.completedTaskCount / project.taskCount) * 100)

  return (
    <div className="card p-5 flex flex-col gap-3 relative">
      <div className="flex items-start justify-between gap-2">
        <Link to={`/projects/${project.id}`} className="min-w-0 flex-1">
          <h3 className="font-semibold text-[var(--text-primary)] truncate hover:text-brand-500 transition-colors">
            {project.name}
          </h3>
        </Link>
        <div className="relative flex-shrink-0">
          <button
            onClick={() => setMenuOpen(o => !o)}
            className="btn-ghost p-1.5 rounded-lg"
            aria-label="Project actions"
          >
            <MoreVertical size={16} />
          </button>
          {menuOpen && (
            <>
              <div className="fixed inset-0 z-10" onClick={() => setMenuOpen(false)} />
              <div className="absolute right-0 top-9 z-20 card p-1 w-36 shadow-lg">
                <button
                  onClick={() => { setMenuOpen(false); onEdit() }}
                  className="w-full flex items-center gap-2 px-2.5 py-1.5 rounded-lg text-sm text-[var(--text-primary)] hover:bg-[var(--bg-tertiary)] transition-colors"
                >
                  <Pencil size={14} /> Edit
                </button>
                <button
                  onClick={() => { setMenuOpen(false); onDelete() }}
                  className="w-full flex items-center gap-2 px-2.5 py-1.5 rounded-lg text-sm text-red-500 hover:bg-red-50 dark:hover:bg-red-900/20 transition-colors"
                >
                  <Trash2 size={14} /> Delete
                </button>
              </div>
            </>
          )}
        </div>
      </div>

      {project.description && (
        <p className="text-sm text-[var(--text-secondary)] line-clamp-2">{project.description}</p>
      )}

      {project.stackTags.length > 0 && (
        <div className="flex flex-wrap gap-1.5">
          {project.stackTags.map(tag => (
            <span key={tag} className="badge-gray">{tag}</span>
          ))}
        </div>
      )}

      <div className="flex items-center gap-2">
        <span className={statusBadgeClass[project.status]}>{PROJECT_STATUS_LABELS[project.status]}</span>
        {project.archived && <span className="badge-gray">Archived</span>}
      </div>

      <div className="flex items-center justify-between text-xs text-[var(--text-muted)] pt-1">
        <span>{project.completedTaskCount}/{project.taskCount} tasks · {project.milestoneCount} milestones</span>
        <div className="flex items-center gap-2">
          {project.repoUrl && (
            <a href={project.repoUrl} target="_blank" rel="noreferrer" onClick={e => e.stopPropagation()} className="hover:text-[var(--text-secondary)]">
              <GitBranch size={14} />
            </a>
          )}
          {project.liveUrl && (
            <a href={project.liveUrl} target="_blank" rel="noreferrer" onClick={e => e.stopPropagation()} className="hover:text-[var(--text-secondary)]">
              <ExternalLink size={14} />
            </a>
          )}
        </div>
      </div>

      {project.taskCount > 0 && (
        <div className="w-full h-1.5 rounded-full bg-[var(--bg-tertiary)] overflow-hidden">
          <div className="h-full bg-brand-500 transition-all" style={{ width: `${progress}%` }} />
        </div>
      )}
    </div>
  )
}
