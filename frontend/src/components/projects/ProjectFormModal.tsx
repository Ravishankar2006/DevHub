import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import { Loader2 } from 'lucide-react'
import Modal from '@/components/ui/Modal'
import { useCreateProject, useUpdateProject } from '@/hooks/useProjects'
import { PROJECT_STATUS_LABELS, type Project, type ProjectStatus } from '@/lib/types'

const schema = z.object({
  name: z.string().min(1, 'Name is required'),
  description: z.string().optional(),
  repoUrl: z.string().optional(),
  liveUrl: z.string().optional(),
  roadmap: z.string().optional(),
  stackTags: z.string().optional(),
  status: z.enum(['PLANNING', 'IN_PROGRESS', 'ON_HOLD', 'COMPLETED']),
})

type FormData = z.infer<typeof schema>

interface ProjectFormModalProps {
  project?: Project
  onClose: () => void
}

export default function ProjectFormModal({ project, onClose }: ProjectFormModalProps) {
  const createProject = useCreateProject()
  const updateProject = useUpdateProject(project?.id ?? '')
  const isEditing = !!project

  const { register, handleSubmit, formState: { errors, isSubmitting } } = useForm<FormData>({
    resolver: zodResolver(schema),
    defaultValues: {
      name: project?.name ?? '',
      description: project?.description ?? '',
      repoUrl: project?.repoUrl ?? '',
      liveUrl: project?.liveUrl ?? '',
      roadmap: project?.roadmap ?? '',
      stackTags: project?.stackTags?.join(', ') ?? '',
      status: project?.status ?? 'PLANNING',
    },
  })

  const onSubmit = async (data: FormData) => {
    const input = {
      name: data.name,
      description: data.description || undefined,
      repoUrl: data.repoUrl || undefined,
      liveUrl: data.liveUrl || undefined,
      roadmap: data.roadmap || undefined,
      stackTags: data.stackTags
        ? data.stackTags.split(',').map(t => t.trim()).filter(Boolean)
        : [],
      status: data.status,
      archived: project?.archived ?? false,
    }

    if (isEditing) {
      await updateProject.mutateAsync(input)
    } else {
      await createProject.mutateAsync(input)
    }
    onClose()
  }

  return (
    <Modal title={isEditing ? 'Edit Project' : 'New Project'} onClose={onClose} wide>
      <form onSubmit={handleSubmit(onSubmit)} className="space-y-4" noValidate>
        <div className="form-group">
          <label htmlFor="project-name" className="label">Name</label>
          <input id="project-name" className="input" placeholder="My awesome project" {...register('name')} />
          {errors.name && <p className="error-text">{errors.name.message}</p>}
        </div>

        <div className="form-group">
          <label htmlFor="project-description" className="label">Description</label>
          <textarea id="project-description" className="input" rows={2} placeholder="What is this project about?" {...register('description')} />
        </div>

        <div className="grid grid-cols-2 gap-4">
          <div className="form-group">
            <label htmlFor="project-repo-url" className="label">Repo URL</label>
            <input id="project-repo-url" className="input" placeholder="https://github.com/..." {...register('repoUrl')} />
          </div>
          <div className="form-group">
            <label htmlFor="project-live-url" className="label">Live URL</label>
            <input id="project-live-url" className="input" placeholder="https://..." {...register('liveUrl')} />
          </div>
        </div>

        <div className="form-group">
          <label htmlFor="project-stack-tags" className="label">Stack tags (comma-separated)</label>
          <input id="project-stack-tags" className="input" placeholder="React, TypeScript, Spring Boot" {...register('stackTags')} />
        </div>

        <div className="form-group">
          <label htmlFor="project-status" className="label">Status</label>
          <select id="project-status" className="input" {...register('status')}>
            {(Object.keys(PROJECT_STATUS_LABELS) as ProjectStatus[]).map(status => (
              <option key={status} value={status}>{PROJECT_STATUS_LABELS[status]}</option>
            ))}
          </select>
        </div>

        <div className="form-group">
          <label htmlFor="project-roadmap" className="label">Roadmap / notes</label>
          <textarea id="project-roadmap" className="input" rows={4} placeholder="Markdown notes, roadmap, documentation..." {...register('roadmap')} />
        </div>

        <div className="flex justify-end gap-3 pt-2">
          <button type="button" className="btn-secondary" onClick={onClose}>Cancel</button>
          <button type="submit" className="btn-primary" disabled={isSubmitting}>
            {isSubmitting ? (
              <><Loader2 size={15} className="animate-spin" /> Saving...</>
            ) : (
              isEditing ? 'Save changes' : 'Create project'
            )}
          </button>
        </div>
      </form>
    </Modal>
  )
}
