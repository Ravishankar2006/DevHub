import { Link } from 'react-router-dom'
import { BookOpen } from 'lucide-react'
import { useLearningResources } from '@/hooks/useLearning'
import { LEARNING_TYPE_LABELS } from '@/lib/types'

export default function ContinueLearningWidget() {
  const { data: resources, isLoading } = useLearningResources({ status: 'IN_PROGRESS' })

  const inProgress = [...(resources ?? [])]
    .sort((a, b) => new Date(b.updatedAt).getTime() - new Date(a.updatedAt).getTime())
    .slice(0, 5)

  return (
    <div className="card p-5 flex flex-col gap-3">
      <div className="flex items-center justify-between">
        <h3 className="text-sm font-semibold text-[var(--text-primary)] flex items-center gap-1.5">
          <BookOpen size={15} className="text-purple-500" /> Continue learning
        </h3>
        <Link to="/learning" className="text-xs text-brand-500 hover:underline">View all →</Link>
      </div>

      {isLoading && <p className="text-xs text-[var(--text-muted)]">Loading...</p>}

      {!isLoading && inProgress.length === 0 && (
        <p className="text-xs text-[var(--text-muted)]">No resources in progress.</p>
      )}

      {inProgress.length > 0 && (
        <div className="flex flex-col gap-3">
          {inProgress.map(resource => (
            <div key={resource.id} className="flex flex-col gap-1">
              <div className="flex items-center justify-between gap-2">
                <p className="text-sm text-[var(--text-primary)] truncate flex-1">{resource.title}</p>
                <span className="badge-gray flex-shrink-0">{LEARNING_TYPE_LABELS[resource.resourceType]}</span>
              </div>
              <div className="flex-1 h-1.5 rounded-full bg-[var(--bg-tertiary)] overflow-hidden">
                <div className="h-full bg-brand-500 transition-all" style={{ width: `${resource.progressPercent}%` }} />
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  )
}
