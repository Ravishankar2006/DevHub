import { Building2, Calendar, FileText, History, MapPin, Pencil, Trash2 } from 'lucide-react'
import { JOB_STATUS_LABELS, type JobApplication, type JobApplicationStatus } from '@/lib/types'
import { formatDate } from '@/lib/utils'

const statusBadgeClass: Record<JobApplicationStatus, string> = {
  WISHLIST: 'badge-gray',
  APPLIED: 'badge-blue',
  INTERVIEWING: 'badge-amber',
  OFFER: 'badge-green',
  ACCEPTED: 'badge-green',
  REJECTED: 'badge bg-red-100 text-red-700 dark:bg-red-900/30 dark:text-red-400',
  WITHDRAWN: 'badge-gray',
}

interface JobApplicationCardProps {
  application: JobApplication
  onEdit: () => void
  onDelete: () => void
  onLogStatus: () => void
}

export default function JobApplicationCard({ application, onEdit, onDelete, onLogStatus }: JobApplicationCardProps) {
  return (
    <div className="card p-5 flex flex-col gap-3">
      <div className="flex items-start justify-between gap-2">
        <div className="min-w-0">
          <h3 className="font-semibold text-[var(--text-primary)] truncate">{application.roleTitle}</h3>
          <p className="flex items-center gap-1.5 text-sm text-[var(--text-secondary)] truncate">
            <Building2 size={13} /> {application.company.name}
          </p>
        </div>
        <div className="flex items-center gap-1 flex-shrink-0">
          <button onClick={onEdit} className="btn-ghost p-1.5 rounded-lg" aria-label="Edit application">
            <Pencil size={13} />
          </button>
          <button onClick={onDelete} className="btn-ghost p-1.5 rounded-lg text-red-500" aria-label="Delete application">
            <Trash2 size={13} />
          </button>
        </div>
      </div>

      <div className="flex items-center gap-2 flex-wrap">
        <span className={statusBadgeClass[application.status]}>{JOB_STATUS_LABELS[application.status]}</span>
        {application.location && (
          <span className="badge-gray flex items-center gap-1"><MapPin size={11} /> {application.location}</span>
        )}
        {application.resumeName && (
          <span className="badge-gray flex items-center gap-1"><FileText size={11} /> {application.resumeName}</span>
        )}
      </div>

      {application.notes && (
        <p className="text-sm text-[var(--text-secondary)] line-clamp-2">{application.notes}</p>
      )}

      <div className="flex items-center justify-between text-xs text-[var(--text-muted)] pt-1">
        {application.deadline ? (
          <span className="flex items-center gap-1.5">
            <Calendar size={12} /> Deadline: {formatDate(application.deadline)}
          </span>
        ) : <span />}
        <button
          onClick={onLogStatus}
          className="flex items-center gap-1 text-brand-500 hover:underline"
        >
          <History size={12} /> Status history
        </button>
      </div>
    </div>
  )
}
