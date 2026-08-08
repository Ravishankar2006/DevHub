import { Download, FileText, Loader2, Pencil, RefreshCw, Sparkles, Trash2 } from 'lucide-react'
import { downloadResume } from '@/hooks/useResumes'
import { formatDate, formatFileSize } from '@/lib/utils'
import type { Resume } from '@/lib/types'

interface ResumeCardProps {
  resume: Resume
  onEdit: () => void
  onDelete: () => void
  onReview: () => void
  onViewReview: () => void
  isReviewing: boolean
}

function scoreBadgeClass(score: number) {
  if (score >= 80) return 'badge-green'
  if (score >= 50) return 'badge-amber'
  return 'badge bg-red-100 text-red-700 dark:bg-red-900/30 dark:text-red-400'
}

export default function ResumeCard({ resume, onEdit, onDelete, onReview, onViewReview, isReviewing }: ResumeCardProps) {
  return (
    <div className="card p-5 flex flex-col gap-3">
      <div className="flex items-start justify-between gap-2">
        <h3 className="font-semibold text-[var(--text-primary)] truncate">{resume.name}</h3>
        <div className="flex items-center gap-1 flex-shrink-0">
          <button onClick={onEdit} className="btn-ghost p-1.5 rounded-lg" aria-label="Edit resume">
            <Pencil size={13} />
          </button>
          <button onClick={onDelete} className="btn-ghost p-1.5 rounded-lg text-red-500" aria-label="Delete resume">
            <Trash2 size={13} />
          </button>
        </div>
      </div>

      <div className="flex items-center gap-2 flex-wrap">
        {resume.label && <span className="badge-gray">{resume.label}</span>}
      </div>

      <div className="flex items-center gap-1.5 text-xs text-[var(--text-muted)]">
        <FileText size={12} /> {resume.fileName} · {formatFileSize(resume.fileSizeBytes)}
      </div>

      {resume.notes && (
        <p className="text-sm text-[var(--text-secondary)] line-clamp-2">{resume.notes}</p>
      )}

      <div className="flex items-center gap-2 flex-wrap pt-1">
        {isReviewing && (
          <span className="badge-gray flex items-center gap-1">
            <Loader2 size={11} className="animate-spin" /> Reviewing...
          </span>
        )}
        {!isReviewing && resume.reviewScore != null && (
          <>
            <button onClick={onViewReview} className={scoreBadgeClass(resume.reviewScore)}>
              Score: {resume.reviewScore}
            </button>
            <button onClick={onReview} className="btn-ghost p-1.5 rounded-lg" aria-label="Re-review resume" title="Re-review">
              <RefreshCw size={12} />
            </button>
          </>
        )}
        {!isReviewing && resume.reviewScore == null && (
          <button onClick={onReview} className="btn-secondary text-xs py-1 px-2.5">
            <Sparkles size={12} /> Review
          </button>
        )}
      </div>

      <div className="flex items-center justify-between text-xs text-[var(--text-muted)] pt-1">
        <span>Uploaded {formatDate(resume.createdAt)}</span>
        <button
          onClick={() => downloadResume(resume.id, resume.fileName)}
          className="flex items-center gap-1 text-brand-500 hover:underline"
        >
          <Download size={12} /> Download
        </button>
      </div>
    </div>
  )
}
