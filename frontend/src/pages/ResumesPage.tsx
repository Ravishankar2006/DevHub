import { useState } from 'react'
import { FileText, Plus, Search } from 'lucide-react'
import ResumeCard from '@/components/resumes/ResumeCard'
import ResumeFormModal from '@/components/resumes/ResumeFormModal'
import { useDeleteResume, useResumes } from '@/hooks/useResumes'
import type { Resume } from '@/lib/types'

export default function ResumesPage() {
  const [labelFilter, setLabelFilter] = useState('')

  const { data: resumes, isLoading } = useResumes({ label: labelFilter || undefined })
  const deleteResume = useDeleteResume()

  const [showForm, setShowForm] = useState(false)
  const [editingResume, setEditingResume] = useState<Resume | undefined>(undefined)

  const openCreate = () => { setEditingResume(undefined); setShowForm(true) }
  const openEdit = (resume: Resume) => { setEditingResume(resume); setShowForm(true) }
  const closeForm = () => { setShowForm(false); setEditingResume(undefined) }

  const handleDelete = (resume: Resume) => {
    if (window.confirm(`Delete "${resume.name}"?`)) {
      deleteResume.mutate(resume.id)
    }
  }

  return (
    <div className="max-w-6xl mx-auto animate-fade-in">
      <div className="page-header flex items-center justify-between gap-4 flex-wrap">
        <div>
          <h2 className="page-title">Resume Manager</h2>
          <p className="page-subtitle">Upload and organize your resume versions, labeled by role.</p>
        </div>
        <button onClick={openCreate} className="btn-primary text-sm">
          <Plus size={16} /> Upload resume
        </button>
      </div>

      <div className="relative mb-5 max-w-xs">
        <Search size={15} className="absolute left-3 top-1/2 -translate-y-1/2 text-[var(--text-muted)]" />
        <input
          value={labelFilter}
          onChange={e => setLabelFilter(e.target.value)}
          placeholder="Filter by label..."
          className="input pl-9"
        />
      </div>

      {isLoading && <p className="text-sm text-[var(--text-muted)]">Loading resumes...</p>}

      {!isLoading && resumes?.length === 0 && (
        <div className="card p-10 flex flex-col items-center text-center">
          <div className="w-12 h-12 rounded-xl bg-brand-500/10 flex items-center justify-center mb-4">
            <FileText size={22} className="text-brand-500" />
          </div>
          <h3 className="text-lg font-semibold text-[var(--text-primary)] mb-2">No resumes found</h3>
          <p className="text-sm text-[var(--text-secondary)] mb-6">
            {labelFilter ? 'Try a different label.' : 'Upload your first resume to get started.'}
          </p>
          <button onClick={openCreate} className="btn-primary text-sm">
            <Plus size={16} /> Upload resume
          </button>
        </div>
      )}

      {!isLoading && resumes && resumes.length > 0 && (
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4">
          {resumes.map(resume => (
            <ResumeCard
              key={resume.id}
              resume={resume}
              onEdit={() => openEdit(resume)}
              onDelete={() => handleDelete(resume)}
            />
          ))}
        </div>
      )}

      {showForm && <ResumeFormModal resume={editingResume} onClose={closeForm} />}
    </div>
  )
}
