import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import { Loader2 } from 'lucide-react'
import Modal from '@/components/ui/Modal'
import { useCreateGoal, useUpdateGoal } from '@/hooks/useGoals'
import { useProjects } from '@/hooks/useProjects'
import { GOAL_STATUS_LABELS, GOAL_TYPE_LABELS, type Goal, type GoalStatus, type GoalType } from '@/lib/types'

const schema = z.object({
  title: z.string().min(1, 'Title is required'),
  description: z.string().optional(),
  type: z.enum(['DAILY', 'WEEKLY', 'MONTHLY', 'CAREER']),
  status: z.enum(['ACTIVE', 'COMPLETED', 'ABANDONED']),
  targetDate: z.string().optional(),
  progressPercent: z.number().min(0).max(100),
  projectId: z.string().optional(),
})

type FormData = z.infer<typeof schema>

interface GoalFormModalProps {
  goal?: Goal
  onClose: () => void
}

export default function GoalFormModal({ goal, onClose }: GoalFormModalProps) {
  const createGoal = useCreateGoal()
  const updateGoal = useUpdateGoal(goal?.id ?? '')
  const { data: projects } = useProjects()
  const isEditing = !!goal

  const { register, handleSubmit, formState: { errors, isSubmitting } } = useForm<FormData>({
    resolver: zodResolver(schema),
    defaultValues: {
      title: goal?.title ?? '',
      description: goal?.description ?? '',
      type: goal?.type ?? 'MONTHLY',
      status: goal?.status ?? 'ACTIVE',
      targetDate: goal?.targetDate ?? '',
      progressPercent: goal?.progressPercent ?? 0,
      projectId: goal?.projectId ?? '',
    },
  })

  const onSubmit = async (data: FormData) => {
    const input = {
      title: data.title,
      description: data.description || undefined,
      type: data.type as GoalType,
      status: data.status as GoalStatus,
      targetDate: data.targetDate || null,
      progressPercent: data.progressPercent,
      projectId: data.projectId || null,
    }

    if (isEditing) {
      await updateGoal.mutateAsync(input)
    } else {
      await createGoal.mutateAsync(input)
    }
    onClose()
  }

  return (
    <Modal title={isEditing ? 'Edit Goal' : 'New Goal'} onClose={onClose}>
      <form onSubmit={handleSubmit(onSubmit)} className="space-y-4" noValidate>
        <div className="form-group">
          <label htmlFor="goal-title" className="label">Title</label>
          <input id="goal-title" className="input" placeholder="Ship the MVP" {...register('title')} />
          {errors.title && <p className="error-text">{errors.title.message}</p>}
        </div>

        <div className="form-group">
          <label htmlFor="goal-description" className="label">Description</label>
          <textarea id="goal-description" className="input" rows={2} {...register('description')} />
        </div>

        <div className="grid grid-cols-2 gap-4">
          <div className="form-group">
            <label htmlFor="goal-type" className="label">Type</label>
            <select id="goal-type" className="input" {...register('type')}>
              {(Object.keys(GOAL_TYPE_LABELS) as GoalType[]).map(type => (
                <option key={type} value={type}>{GOAL_TYPE_LABELS[type]}</option>
              ))}
            </select>
          </div>
          <div className="form-group">
            <label htmlFor="goal-status" className="label">Status</label>
            <select id="goal-status" className="input" {...register('status')}>
              {(Object.keys(GOAL_STATUS_LABELS) as GoalStatus[]).map(status => (
                <option key={status} value={status}>{GOAL_STATUS_LABELS[status]}</option>
              ))}
            </select>
          </div>
        </div>

        <div className="grid grid-cols-2 gap-4">
          <div className="form-group">
            <label htmlFor="goal-target-date" className="label">Target date</label>
            <input id="goal-target-date" type="date" className="input" {...register('targetDate')} />
          </div>
          <div className="form-group">
            <label htmlFor="goal-progress" className="label">Progress (%)</label>
            <input id="goal-progress" type="number" min={0} max={100} className="input" {...register('progressPercent', { valueAsNumber: true })} />
          </div>
        </div>

        <div className="form-group">
          <label htmlFor="goal-project" className="label">Related project (optional)</label>
          <select id="goal-project" className="input" {...register('projectId')}>
            <option value="">None</option>
            {projects?.map(p => (
              <option key={p.id} value={p.id}>{p.name}</option>
            ))}
          </select>
        </div>

        <div className="flex justify-end gap-3 pt-2">
          <button type="button" className="btn-secondary" onClick={onClose}>Cancel</button>
          <button type="submit" className="btn-primary" disabled={isSubmitting}>
            {isSubmitting ? (
              <><Loader2 size={15} className="animate-spin" /> Saving...</>
            ) : (
              isEditing ? 'Save changes' : 'Create goal'
            )}
          </button>
        </div>
      </form>
    </Modal>
  )
}
