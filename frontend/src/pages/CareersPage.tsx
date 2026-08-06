import { useState } from 'react'
import { Briefcase, Plus } from 'lucide-react'
import JobApplicationCard from '@/components/careers/JobApplicationCard'
import JobApplicationFormModal from '@/components/careers/JobApplicationFormModal'
import JobStatusHistoryPanel from '@/components/careers/JobStatusHistoryPanel'
import { useDeleteJobApplication, useJobApplications } from '@/hooks/useCareers'
import { JOB_STATUS_LABELS, type JobApplication, type JobApplicationStatus } from '@/lib/types'

export default function CareersPage() {
  const [statusFilter, setStatusFilter] = useState<JobApplicationStatus | ''>('')

  const { data: applications, isLoading } = useJobApplications({
    status: statusFilter || undefined,
  })

  const deleteApplication = useDeleteJobApplication()

  const [showForm, setShowForm] = useState(false)
  const [editingApplication, setEditingApplication] = useState<JobApplication | undefined>(undefined)
  const [historyApplicationId, setHistoryApplicationId] = useState<string | undefined>(undefined)

  const openCreate = () => { setEditingApplication(undefined); setShowForm(true) }
  const openEdit = (application: JobApplication) => { setEditingApplication(application); setShowForm(true) }
  const closeForm = () => { setShowForm(false); setEditingApplication(undefined) }

  const handleDelete = (application: JobApplication) => {
    if (window.confirm(`Delete the application for "${application.roleTitle}" at ${application.company.name}?`)) {
      deleteApplication.mutate(application.id)
    }
  }

  const historyApplication = applications?.find(a => a.id === historyApplicationId)

  return (
    <div className="max-w-6xl mx-auto animate-fade-in">
      <div className="page-header flex items-center justify-between gap-4 flex-wrap">
        <div>
          <h2 className="page-title">Job Tracker</h2>
          <p className="page-subtitle">Track applications, companies, and status history in one place.</p>
        </div>
        <button onClick={openCreate} className="btn-primary text-sm">
          <Plus size={16} /> Add application
        </button>
      </div>

      <div className="flex items-center gap-3 mb-5 flex-wrap">
        <select value={statusFilter} onChange={e => setStatusFilter(e.target.value as JobApplicationStatus | '')} className="input w-auto text-sm">
          <option value="">All statuses</option>
          {(Object.keys(JOB_STATUS_LABELS) as JobApplicationStatus[]).map(status => (
            <option key={status} value={status}>{JOB_STATUS_LABELS[status]}</option>
          ))}
        </select>
      </div>

      {isLoading && <p className="text-sm text-[var(--text-muted)]">Loading applications...</p>}

      {!isLoading && applications?.length === 0 && (
        <div className="card p-10 flex flex-col items-center text-center">
          <div className="w-12 h-12 rounded-xl bg-brand-500/10 flex items-center justify-center mb-4">
            <Briefcase size={22} className="text-brand-500" />
          </div>
          <h3 className="text-lg font-semibold text-[var(--text-primary)] mb-2">No applications found</h3>
          <p className="text-sm text-[var(--text-secondary)] mb-6">
            {statusFilter ? 'Try a different filter.' : 'Add your first application to start tracking.'}
          </p>
          <button onClick={openCreate} className="btn-primary text-sm">
            <Plus size={16} /> Add application
          </button>
        </div>
      )}

      {!isLoading && applications && applications.length > 0 && (
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4">
          {applications.map(application => (
            <JobApplicationCard
              key={application.id}
              application={application}
              onEdit={() => openEdit(application)}
              onDelete={() => handleDelete(application)}
              onLogStatus={() => setHistoryApplicationId(application.id)}
            />
          ))}
        </div>
      )}

      {showForm && <JobApplicationFormModal application={editingApplication} onClose={closeForm} />}
      {historyApplication && (
        <JobStatusHistoryPanel application={historyApplication} onClose={() => setHistoryApplicationId(undefined)} />
      )}
    </div>
  )
}
