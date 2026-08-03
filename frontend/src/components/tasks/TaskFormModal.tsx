import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import { Loader2 } from 'lucide-react'
import Modal from '@/components/ui/Modal'
import { useCreateTask, useUpdateTask } from '@/hooks/useTasks'
import { useProjects } from '@/hooks/useProjects'
import { useMilestones } from '@/hooks/useMilestones'
import { TASK_STATUSES, TASK_STATUS_LABELS, type Task, type TaskPriority, type TaskStatus } from '@/lib/types'

const schema = z.object({
  title: z.string().min(1, 'Title is required'),
  description: z.string().optional(),
  projectId: z.string().optional(),
  milestoneId: z.string().optional(),
  status: z.enum(['TODO', 'IN_PROGRESS', 'DONE']),
  priority: z.enum(['LOW', 'MEDIUM', 'HIGH']),
  dueDate: z.string().optional(),
})

type FormData = z.infer<typeof schema>

interface TaskFormModalProps {
  task?: Task
  defaultProjectId?: string
  defaultStatus?: TaskStatus
  onClose: () => void
}

export default function TaskFormModal({ task, defaultProjectId, defaultStatus, onClose }: TaskFormModalProps) {
  const isEditing = !!task
  const createTask = useCreateTask()
  const updateTask = useUpdateTask()
  const { data: projects } = useProjects()

  const { register, handleSubmit, watch, formState: { errors, isSubmitting } } = useForm<FormData>({
    resolver: zodResolver(schema),
    defaultValues: {
      title: task?.title ?? '',
      description: task?.description ?? '',
      projectId: task?.projectId ?? defaultProjectId ?? '',
      milestoneId: task?.milestoneId ?? '',
      status: task?.status ?? defaultStatus ?? 'TODO',
      priority: task?.priority ?? 'MEDIUM',
      dueDate: task?.dueDate ?? '',
    },
  })

  const selectedProjectId = watch('projectId')
  const { data: milestones } = useMilestones(selectedProjectId || undefined)

  const onSubmit = async (data: FormData) => {
    const input = {
      title: data.title,
      description: data.description || undefined,
      projectId: data.projectId || null,
      milestoneId: data.milestoneId || null,
      status: data.status,
      priority: data.priority,
      dueDate: data.dueDate || null,
    }

    if (isEditing && task) {
      await updateTask.mutateAsync({ id: task.id, ...input })
    } else {
      await createTask.mutateAsync(input)
    }
    onClose()
  }

  return (
    <Modal title={isEditing ? 'Edit Task' : 'New Task'} onClose={onClose}>
      <form onSubmit={handleSubmit(onSubmit)} className="space-y-4" noValidate>
        <div className="form-group">
          <label htmlFor="task-title" className="label">Title</label>
          <input id="task-title" className="input" placeholder="Implement login page" {...register('title')} />
          {errors.title && <p className="error-text">{errors.title.message}</p>}
        </div>

        <div className="form-group">
          <label htmlFor="task-description" className="label">Description</label>
          <textarea id="task-description" className="input" rows={2} {...register('description')} />
        </div>

        <div className="form-group">
          <label htmlFor="task-project" className="label">Project</label>
          <select id="task-project" className="input" {...register('projectId')}>
            <option value="">No project (standalone)</option>
            {projects?.map(p => (
              <option key={p.id} value={p.id}>{p.name}</option>
            ))}
          </select>
        </div>

        {selectedProjectId && (
          <div className="form-group">
            <label htmlFor="task-milestone" className="label">Milestone</label>
            <select id="task-milestone" className="input" {...register('milestoneId')}>
              <option value="">No milestone</option>
              {milestones?.map(m => (
                <option key={m.id} value={m.id}>{m.title}</option>
              ))}
            </select>
          </div>
        )}

        <div className="grid grid-cols-2 gap-4">
          <div className="form-group">
            <label htmlFor="task-status" className="label">Status</label>
            <select id="task-status" className="input" {...register('status')}>
              {TASK_STATUSES.map(status => (
                <option key={status} value={status}>{TASK_STATUS_LABELS[status]}</option>
              ))}
            </select>
          </div>
          <div className="form-group">
            <label htmlFor="task-priority" className="label">Priority</label>
            <select id="task-priority" className="input" {...register('priority')}>
              {(['LOW', 'MEDIUM', 'HIGH'] as TaskPriority[]).map(priority => (
                <option key={priority} value={priority}>{priority}</option>
              ))}
            </select>
          </div>
        </div>

        <div className="form-group">
          <label htmlFor="task-due-date" className="label">Due date</label>
          <input id="task-due-date" type="date" className="input" {...register('dueDate')} />
        </div>

        <div className="flex justify-end gap-3 pt-2">
          <button type="button" className="btn-secondary" onClick={onClose}>Cancel</button>
          <button type="submit" className="btn-primary" disabled={isSubmitting}>
            {isSubmitting ? (
              <><Loader2 size={15} className="animate-spin" /> Saving...</>
            ) : (
              isEditing ? 'Save changes' : 'Create task'
            )}
          </button>
        </div>
      </form>
    </Modal>
  )
}
