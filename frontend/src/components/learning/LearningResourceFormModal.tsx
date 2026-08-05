import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import { Loader2 } from 'lucide-react'
import Modal from '@/components/ui/Modal'
import { useCreateLearningResource, useUpdateLearningResource } from '@/hooks/useLearning'
import {
  LEARNING_STATUS_LABELS,
  LEARNING_TYPE_LABELS,
  type LearningResource,
  type LearningResourceType,
  type LearningStatus,
} from '@/lib/types'

const schema = z.object({
  title: z.string().min(1, 'Title is required'),
  resourceType: z.enum(['COURSE', 'BOOK', 'TUTORIAL', 'CERTIFICATE', 'VIDEO', 'ARTICLE']),
  provider: z.string().optional(),
  url: z.string().optional(),
  status: z.enum(['NOT_STARTED', 'IN_PROGRESS', 'COMPLETED', 'ABANDONED']),
  progressPercent: z.number().min(0).max(100),
  estimatedCompletionDate: z.string().optional(),
  tags: z.string().optional(),
  notes: z.string().optional(),
})

type FormData = z.infer<typeof schema>

interface LearningResourceFormModalProps {
  resource?: LearningResource
  onClose: () => void
}

export default function LearningResourceFormModal({ resource, onClose }: LearningResourceFormModalProps) {
  const createResource = useCreateLearningResource()
  const updateResource = useUpdateLearningResource(resource?.id ?? '')
  const isEditing = !!resource

  const { register, handleSubmit, formState: { errors, isSubmitting } } = useForm<FormData>({
    resolver: zodResolver(schema),
    defaultValues: {
      title: resource?.title ?? '',
      resourceType: resource?.resourceType ?? 'COURSE',
      provider: resource?.provider ?? '',
      url: resource?.url ?? '',
      status: resource?.status ?? 'NOT_STARTED',
      progressPercent: resource?.progressPercent ?? 0,
      estimatedCompletionDate: resource?.estimatedCompletionDate ?? '',
      tags: resource?.tags?.join(', ') ?? '',
      notes: resource?.notes ?? '',
    },
  })

  const onSubmit = async (data: FormData) => {
    const input = {
      title: data.title,
      resourceType: data.resourceType as LearningResourceType,
      provider: data.provider || undefined,
      url: data.url || undefined,
      status: data.status as LearningStatus,
      progressPercent: data.progressPercent,
      estimatedCompletionDate: data.estimatedCompletionDate || null,
      tags: data.tags ? data.tags.split(',').map(t => t.trim()).filter(Boolean) : [],
      notes: data.notes || undefined,
    }

    if (isEditing) {
      await updateResource.mutateAsync(input)
    } else {
      await createResource.mutateAsync(input)
    }
    onClose()
  }

  return (
    <Modal title={isEditing ? 'Edit Learning Resource' : 'New Learning Resource'} onClose={onClose}>
      <form onSubmit={handleSubmit(onSubmit)} className="space-y-4" noValidate>
        <div className="form-group">
          <label htmlFor="learning-title" className="label">Title</label>
          <input id="learning-title" className="input" placeholder="Advanced React Patterns" {...register('title')} />
          {errors.title && <p className="error-text">{errors.title.message}</p>}
        </div>

        <div className="grid grid-cols-2 gap-4">
          <div className="form-group">
            <label htmlFor="learning-type" className="label">Type</label>
            <select id="learning-type" className="input" {...register('resourceType')}>
              {(Object.keys(LEARNING_TYPE_LABELS) as LearningResourceType[]).map(type => (
                <option key={type} value={type}>{LEARNING_TYPE_LABELS[type]}</option>
              ))}
            </select>
          </div>
          <div className="form-group">
            <label htmlFor="learning-status" className="label">Status</label>
            <select id="learning-status" className="input" {...register('status')}>
              {(Object.keys(LEARNING_STATUS_LABELS) as LearningStatus[]).map(status => (
                <option key={status} value={status}>{LEARNING_STATUS_LABELS[status]}</option>
              ))}
            </select>
          </div>
        </div>

        <div className="grid grid-cols-2 gap-4">
          <div className="form-group">
            <label htmlFor="learning-provider" className="label">Provider</label>
            <input id="learning-provider" className="input" placeholder="Coursera, O'Reilly..." {...register('provider')} />
          </div>
          <div className="form-group">
            <label htmlFor="learning-url" className="label">Link</label>
            <input id="learning-url" className="input" placeholder="https://..." {...register('url')} />
          </div>
        </div>

        <div className="grid grid-cols-2 gap-4">
          <div className="form-group">
            <label htmlFor="learning-est-date" className="label">Est. completion date</label>
            <input id="learning-est-date" type="date" className="input" {...register('estimatedCompletionDate')} />
          </div>
          <div className="form-group">
            <label htmlFor="learning-progress" className="label">Progress (%)</label>
            <input id="learning-progress" type="number" min={0} max={100} className="input" {...register('progressPercent', { valueAsNumber: true })} />
          </div>
        </div>

        <div className="form-group">
          <label htmlFor="learning-tags" className="label">Skill tags (comma-separated)</label>
          <input id="learning-tags" className="input" placeholder="react, typescript" {...register('tags')} />
        </div>

        <div className="form-group">
          <label htmlFor="learning-notes" className="label">Notes</label>
          <textarea id="learning-notes" className="input" rows={3} {...register('notes')} />
        </div>

        <div className="flex justify-end gap-3 pt-2">
          <button type="button" className="btn-secondary" onClick={onClose}>Cancel</button>
          <button type="submit" className="btn-primary" disabled={isSubmitting}>
            {isSubmitting ? (
              <><Loader2 size={15} className="animate-spin" /> Saving...</>
            ) : (
              isEditing ? 'Save changes' : 'Add resource'
            )}
          </button>
        </div>
      </form>
    </Modal>
  )
}
