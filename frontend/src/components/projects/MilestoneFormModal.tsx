import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import { Loader2 } from 'lucide-react'
import Modal from '@/components/ui/Modal'
import { useCreateMilestone, useUpdateMilestone } from '@/hooks/useMilestones'
import type { Milestone } from '@/lib/types'

const schema = z.object({
  title: z.string().min(1, 'Title is required'),
  description: z.string().optional(),
  dueDate: z.string().optional(),
})

type FormData = z.infer<typeof schema>

interface MilestoneFormModalProps {
  projectId: string
  milestone?: Milestone
  onClose: () => void
}

export default function MilestoneFormModal({ projectId, milestone, onClose }: MilestoneFormModalProps) {
  const createMilestone = useCreateMilestone(projectId)
  const updateMilestone = useUpdateMilestone(projectId, milestone?.id ?? '')
  const isEditing = !!milestone

  const { register, handleSubmit, formState: { errors, isSubmitting } } = useForm<FormData>({
    resolver: zodResolver(schema),
    defaultValues: {
      title: milestone?.title ?? '',
      description: milestone?.description ?? '',
      dueDate: milestone?.dueDate ?? '',
    },
  })

  const onSubmit = async (data: FormData) => {
    const input = {
      title: data.title,
      description: data.description || undefined,
      dueDate: data.dueDate || null,
    }

    if (isEditing) {
      await updateMilestone.mutateAsync(input)
    } else {
      await createMilestone.mutateAsync(input)
    }
    onClose()
  }

  return (
    <Modal title={isEditing ? 'Edit Milestone' : 'New Milestone'} onClose={onClose}>
      <form onSubmit={handleSubmit(onSubmit)} className="space-y-4" noValidate>
        <div className="form-group">
          <label htmlFor="milestone-title" className="label">Title</label>
          <input id="milestone-title" className="input" placeholder="Beta launch" {...register('title')} />
          {errors.title && <p className="error-text">{errors.title.message}</p>}
        </div>

        <div className="form-group">
          <label htmlFor="milestone-description" className="label">Description</label>
          <textarea id="milestone-description" className="input" rows={2} {...register('description')} />
        </div>

        <div className="form-group">
          <label htmlFor="milestone-due-date" className="label">Due date</label>
          <input id="milestone-due-date" type="date" className="input" {...register('dueDate')} />
        </div>

        <div className="flex justify-end gap-3 pt-2">
          <button type="button" className="btn-secondary" onClick={onClose}>Cancel</button>
          <button type="submit" className="btn-primary" disabled={isSubmitting}>
            {isSubmitting ? (
              <><Loader2 size={15} className="animate-spin" /> Saving...</>
            ) : (
              isEditing ? 'Save changes' : 'Create milestone'
            )}
          </button>
        </div>
      </form>
    </Modal>
  )
}
