import { useState } from 'react'
import { BookOpen, Plus } from 'lucide-react'
import LearningResourceCard from '@/components/learning/LearningResourceCard'
import LearningResourceFormModal from '@/components/learning/LearningResourceFormModal'
import { useDeleteLearningResource, useLearningResources } from '@/hooks/useLearning'
import {
  LEARNING_STATUS_LABELS,
  LEARNING_TYPE_LABELS,
  type LearningResource,
  type LearningResourceType,
  type LearningStatus,
} from '@/lib/types'

export default function LearningPage() {
  const [statusFilter, setStatusFilter] = useState<LearningStatus | ''>('')
  const [typeFilter, setTypeFilter] = useState<LearningResourceType | ''>('')

  const { data: resources, isLoading } = useLearningResources({
    status: statusFilter || undefined,
    resourceType: typeFilter || undefined,
  })

  const deleteResource = useDeleteLearningResource()

  const [showForm, setShowForm] = useState(false)
  const [editingResource, setEditingResource] = useState<LearningResource | undefined>(undefined)

  const openCreate = () => { setEditingResource(undefined); setShowForm(true) }
  const openEdit = (resource: LearningResource) => { setEditingResource(resource); setShowForm(true) }
  const closeForm = () => { setShowForm(false); setEditingResource(undefined) }

  const handleDelete = (resource: LearningResource) => {
    if (window.confirm(`Delete "${resource.title}"?`)) {
      deleteResource.mutate(resource.id)
    }
  }

  return (
    <div className="max-w-6xl mx-auto animate-fade-in">
      <div className="page-header flex items-center justify-between gap-4 flex-wrap">
        <div>
          <h2 className="page-title">Learning Tracker</h2>
          <p className="page-subtitle">Track courses, books, and resources — and the progress you're making on them.</p>
        </div>
        <button onClick={openCreate} className="btn-primary text-sm">
          <Plus size={16} /> Add resource
        </button>
      </div>

      <div className="flex items-center gap-3 mb-5 flex-wrap">
        <select value={statusFilter} onChange={e => setStatusFilter(e.target.value as LearningStatus | '')} className="input w-auto text-sm">
          <option value="">All statuses</option>
          {(Object.keys(LEARNING_STATUS_LABELS) as LearningStatus[]).map(status => (
            <option key={status} value={status}>{LEARNING_STATUS_LABELS[status]}</option>
          ))}
        </select>
        <select value={typeFilter} onChange={e => setTypeFilter(e.target.value as LearningResourceType | '')} className="input w-auto text-sm">
          <option value="">All types</option>
          {(Object.keys(LEARNING_TYPE_LABELS) as LearningResourceType[]).map(type => (
            <option key={type} value={type}>{LEARNING_TYPE_LABELS[type]}</option>
          ))}
        </select>
      </div>

      {isLoading && <p className="text-sm text-[var(--text-muted)]">Loading learning resources...</p>}

      {!isLoading && resources?.length === 0 && (
        <div className="card p-10 flex flex-col items-center text-center">
          <div className="w-12 h-12 rounded-xl bg-brand-500/10 flex items-center justify-center mb-4">
            <BookOpen size={22} className="text-brand-500" />
          </div>
          <h3 className="text-lg font-semibold text-[var(--text-primary)] mb-2">No learning resources found</h3>
          <p className="text-sm text-[var(--text-secondary)] mb-6">
            {statusFilter || typeFilter ? 'Try a different filter.' : 'Add a course, book, or tutorial to start tracking your progress.'}
          </p>
          <button onClick={openCreate} className="btn-primary text-sm">
            <Plus size={16} /> Add resource
          </button>
        </div>
      )}

      {!isLoading && resources && resources.length > 0 && (
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4">
          {resources.map(resource => (
            <LearningResourceCard
              key={resource.id}
              resource={resource}
              onEdit={() => openEdit(resource)}
              onDelete={() => handleDelete(resource)}
            />
          ))}
        </div>
      )}

      {showForm && <LearningResourceFormModal resource={editingResource} onClose={closeForm} />}
    </div>
  )
}
