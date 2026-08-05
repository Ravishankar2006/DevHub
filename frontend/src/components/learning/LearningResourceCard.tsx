import { Calendar, ExternalLink, Pencil, Trash2 } from 'lucide-react'
import { LEARNING_STATUS_LABELS, LEARNING_TYPE_LABELS, type LearningResource, type LearningStatus } from '@/lib/types'

const statusBadgeClass: Record<LearningStatus, string> = {
  NOT_STARTED: 'badge-gray',
  IN_PROGRESS: 'badge-blue',
  COMPLETED: 'badge-green',
  ABANDONED: 'badge-gray',
}

interface LearningResourceCardProps {
  resource: LearningResource
  onEdit: () => void
  onDelete: () => void
}

export default function LearningResourceCard({ resource, onEdit, onDelete }: LearningResourceCardProps) {
  return (
    <div className="card p-5 flex flex-col gap-3">
      <div className="flex items-start justify-between gap-2">
        <h3 className="font-semibold text-[var(--text-primary)] truncate">{resource.title}</h3>
        <div className="flex items-center gap-1 flex-shrink-0">
          <button onClick={onEdit} className="btn-ghost p-1.5 rounded-lg" aria-label="Edit learning resource">
            <Pencil size={13} />
          </button>
          <button onClick={onDelete} className="btn-ghost p-1.5 rounded-lg text-red-500" aria-label="Delete learning resource">
            <Trash2 size={13} />
          </button>
        </div>
      </div>

      <div className="flex items-center gap-2 flex-wrap">
        <span className={statusBadgeClass[resource.status]}>{LEARNING_STATUS_LABELS[resource.status]}</span>
        <span className="badge-gray">{LEARNING_TYPE_LABELS[resource.resourceType]}</span>
        {resource.provider && <span className="badge-gray">{resource.provider}</span>}
      </div>

      {resource.tags.length > 0 && (
        <div className="flex items-center gap-1.5 flex-wrap">
          {resource.tags.map(tag => (
            <span key={tag} className="badge-blue">{tag}</span>
          ))}
        </div>
      )}

      <div>
        <div className="flex items-center justify-between text-xs text-[var(--text-muted)] mb-1.5">
          <span>Progress</span>
          <span>{resource.progressPercent}%</span>
        </div>
        <div className="w-full h-1.5 rounded-full bg-[var(--bg-tertiary)] overflow-hidden">
          <div className="h-full bg-brand-500 transition-all" style={{ width: `${resource.progressPercent}%` }} />
        </div>
      </div>

      <div className="flex items-center justify-between text-xs text-[var(--text-muted)]">
        {resource.estimatedCompletionDate ? (
          <span className="flex items-center gap-1.5">
            <Calendar size={12} /> Est. completion: {resource.estimatedCompletionDate}
          </span>
        ) : <span />}
        {resource.url && (
          <a
            href={resource.url}
            target="_blank"
            rel="noopener noreferrer"
            className="flex items-center gap-1 text-brand-500 hover:underline"
          >
            <ExternalLink size={12} /> Open
          </a>
        )}
      </div>
    </div>
  )
}
