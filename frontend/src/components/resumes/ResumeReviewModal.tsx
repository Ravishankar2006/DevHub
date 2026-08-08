import Modal from '@/components/ui/Modal'
import { formatDate } from '@/lib/utils'
import type { Resume } from '@/lib/types'

function scoreBadgeClass(score: number) {
  if (score >= 80) return 'badge-green'
  if (score >= 50) return 'badge-amber'
  return 'badge bg-red-100 text-red-700 dark:bg-red-900/30 dark:text-red-400'
}

export default function ResumeReviewModal({ resume, onClose }: { resume: Resume; onClose: () => void }) {
  return (
    <Modal title={`AI Review — ${resume.name}`} onClose={onClose} wide>
      <div className="flex flex-col gap-5">
        <div className="flex items-center gap-3">
          {resume.reviewScore != null && (
            <span className={scoreBadgeClass(resume.reviewScore)}>Score: {resume.reviewScore}/100</span>
          )}
          {resume.reviewedAt && (
            <span className="text-xs text-[var(--text-muted)]">Reviewed {formatDate(resume.reviewedAt)}</span>
          )}
        </div>

        {resume.reviewSummary && (
          <p className="text-sm text-[var(--text-secondary)]">{resume.reviewSummary}</p>
        )}

        {resume.reviewIssues.length > 0 && (
          <div>
            <h4 className="section-title mb-2">Issues</h4>
            <ul className="list-disc list-inside space-y-1.5 text-sm text-[var(--text-secondary)]">
              {resume.reviewIssues.map((issue, i) => (
                <li key={i}>{issue}</li>
              ))}
            </ul>
          </div>
        )}

        {resume.reviewSuggestions.length > 0 && (
          <div>
            <h4 className="section-title mb-2">Suggestions</h4>
            <ul className="list-disc list-inside space-y-1.5 text-sm text-[var(--text-secondary)]">
              {resume.reviewSuggestions.map((suggestion, i) => (
                <li key={i}>{suggestion}</li>
              ))}
            </ul>
          </div>
        )}
      </div>
    </Modal>
  )
}
