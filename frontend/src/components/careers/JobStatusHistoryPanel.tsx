import { useState } from 'react'
import { Loader2 } from 'lucide-react'
import Modal from '@/components/ui/Modal'
import { useChangeJobApplicationStatus } from '@/hooks/useCareers'
import { JOB_STATUS_LABELS, type JobApplication, type JobApplicationStatus } from '@/lib/types'

interface JobStatusHistoryPanelProps {
  application: JobApplication
  onClose: () => void
}

export default function JobStatusHistoryPanel({ application, onClose }: JobStatusHistoryPanelProps) {
  const changeStatus = useChangeJobApplicationStatus(application.id)
  const [status, setStatus] = useState<JobApplicationStatus>(application.status)
  const [note, setNote] = useState('')

  const handleLog = async () => {
    await changeStatus.mutateAsync({ status, note: note.trim() || undefined })
    setNote('')
  }

  return (
    <Modal title={`Status history — ${application.roleTitle}`} onClose={onClose}>
      <div className="space-y-4">
        <div className="flex gap-3 items-end">
          <div className="form-group flex-1">
            <label htmlFor="status-history-status" className="label">Status</label>
            <select
              id="status-history-status"
              className="input"
              value={status}
              onChange={e => setStatus(e.target.value as JobApplicationStatus)}
            >
              {(Object.keys(JOB_STATUS_LABELS) as JobApplicationStatus[]).map(s => (
                <option key={s} value={s}>{JOB_STATUS_LABELS[s]}</option>
              ))}
            </select>
          </div>
          <button
            onClick={handleLog}
            disabled={changeStatus.isPending}
            className="btn-primary text-sm"
          >
            {changeStatus.isPending ? <Loader2 size={15} className="animate-spin" /> : 'Log'}
          </button>
        </div>

        <div className="form-group">
          <label htmlFor="status-history-note" className="label">Note (optional)</label>
          <textarea
            id="status-history-note"
            className="input"
            rows={2}
            placeholder="Add a note about this update..."
            value={note}
            onChange={e => setNote(e.target.value)}
          />
        </div>

        <div className="border-t border-[var(--border)] pt-3 space-y-3 max-h-64 overflow-y-auto">
          {application.statusHistory.length === 0 && (
            <p className="text-sm text-[var(--text-muted)]">No history yet.</p>
          )}
          {application.statusHistory.map(entry => (
            <div key={entry.id} className="flex items-start gap-3 text-sm">
              <span className="badge-gray flex-shrink-0">{JOB_STATUS_LABELS[entry.status]}</span>
              <div className="min-w-0">
                {entry.note && <p className="text-[var(--text-secondary)]">{entry.note}</p>}
                <p className="text-xs text-[var(--text-muted)]">{new Date(entry.changedAt).toLocaleString()}</p>
              </div>
            </div>
          ))}
        </div>

        <div className="flex justify-end pt-2">
          <button type="button" className="btn-secondary" onClick={onClose}>Close</button>
        </div>
      </div>
    </Modal>
  )
}
