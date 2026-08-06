import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import { Loader2 } from 'lucide-react'
import Modal from '@/components/ui/Modal'
import { useCreateJobApplication, useUpdateJobApplication } from '@/hooks/useCareers'
import { useResumes } from '@/hooks/useResumes'
import { JOB_STATUS_LABELS, type JobApplication, type JobApplicationStatus } from '@/lib/types'

const schema = z.object({
  companyName: z.string().min(1, 'Company is required'),
  companyWebsite: z.string().optional(),
  roleTitle: z.string().min(1, 'Role title is required'),
  status: z.enum(['WISHLIST', 'APPLIED', 'INTERVIEWING', 'OFFER', 'ACCEPTED', 'REJECTED', 'WITHDRAWN']),
  appliedDate: z.string().optional(),
  deadline: z.string().optional(),
  location: z.string().optional(),
  jobPostingUrl: z.string().optional(),
  resumeId: z.string().optional(),
  notes: z.string().optional(),
})

type FormData = z.infer<typeof schema>

interface JobApplicationFormModalProps {
  application?: JobApplication
  onClose: () => void
}

export default function JobApplicationFormModal({ application, onClose }: JobApplicationFormModalProps) {
  const createApplication = useCreateJobApplication()
  const updateApplication = useUpdateJobApplication(application?.id ?? '')
  const isEditing = !!application
  const { data: resumes } = useResumes()

  const { register, handleSubmit, formState: { errors, isSubmitting } } = useForm<FormData>({
    resolver: zodResolver(schema),
    defaultValues: {
      companyName: application?.company.name ?? '',
      companyWebsite: application?.company.website ?? '',
      roleTitle: application?.roleTitle ?? '',
      status: application?.status ?? 'WISHLIST',
      appliedDate: application?.appliedDate ?? '',
      deadline: application?.deadline ?? '',
      location: application?.location ?? '',
      jobPostingUrl: application?.jobPostingUrl ?? '',
      resumeId: application?.resumeId ?? '',
      notes: application?.notes ?? '',
    },
  })

  const onSubmit = async (data: FormData) => {
    const input = {
      companyName: data.companyName,
      companyWebsite: data.companyWebsite || undefined,
      roleTitle: data.roleTitle,
      status: data.status as JobApplicationStatus,
      appliedDate: data.appliedDate || null,
      deadline: data.deadline || null,
      location: data.location || undefined,
      jobPostingUrl: data.jobPostingUrl || undefined,
      resumeId: data.resumeId || null,
      notes: data.notes || undefined,
    }

    if (isEditing) {
      await updateApplication.mutateAsync(input)
    } else {
      await createApplication.mutateAsync(input)
    }
    onClose()
  }

  return (
    <Modal title={isEditing ? 'Edit Application' : 'New Application'} onClose={onClose} wide>
      <form onSubmit={handleSubmit(onSubmit)} className="space-y-4" noValidate>
        <div className="grid grid-cols-2 gap-4">
          <div className="form-group">
            <label htmlFor="job-company" className="label">Company</label>
            <input id="job-company" className="input" placeholder="Acme Corp" {...register('companyName')} />
            {errors.companyName && <p className="error-text">{errors.companyName.message}</p>}
          </div>
          <div className="form-group">
            <label htmlFor="job-company-website" className="label">Company website</label>
            <input id="job-company-website" className="input" placeholder="https://..." {...register('companyWebsite')} />
          </div>
        </div>

        <div className="form-group">
          <label htmlFor="job-role" className="label">Role title</label>
          <input id="job-role" className="input" placeholder="Backend Engineer" {...register('roleTitle')} />
          {errors.roleTitle && <p className="error-text">{errors.roleTitle.message}</p>}
        </div>

        <div className="grid grid-cols-2 gap-4">
          <div className="form-group">
            <label htmlFor="job-status" className="label">Status</label>
            <select id="job-status" className="input" {...register('status')}>
              {(Object.keys(JOB_STATUS_LABELS) as JobApplicationStatus[]).map(status => (
                <option key={status} value={status}>{JOB_STATUS_LABELS[status]}</option>
              ))}
            </select>
          </div>
          <div className="form-group">
            <label htmlFor="job-location" className="label">Location</label>
            <input id="job-location" className="input" placeholder="Remote, NYC..." {...register('location')} />
          </div>
        </div>

        <div className="grid grid-cols-2 gap-4">
          <div className="form-group">
            <label htmlFor="job-applied-date" className="label">Applied date</label>
            <input id="job-applied-date" type="date" className="input" {...register('appliedDate')} />
          </div>
          <div className="form-group">
            <label htmlFor="job-deadline" className="label">Deadline</label>
            <input id="job-deadline" type="date" className="input" {...register('deadline')} />
          </div>
        </div>

        <div className="grid grid-cols-2 gap-4">
          <div className="form-group">
            <label htmlFor="job-posting-url" className="label">Job posting link</label>
            <input id="job-posting-url" className="input" placeholder="https://..." {...register('jobPostingUrl')} />
          </div>
          <div className="form-group">
            <label htmlFor="job-resume" className="label">Linked resume</label>
            <select id="job-resume" className="input" {...register('resumeId')}>
              <option value="">None</option>
              {resumes?.map(resume => (
                <option key={resume.id} value={resume.id}>{resume.name}</option>
              ))}
            </select>
          </div>
        </div>

        <div className="form-group">
          <label htmlFor="job-notes" className="label">Notes</label>
          <textarea id="job-notes" className="input" rows={3} {...register('notes')} />
        </div>

        <div className="flex justify-end gap-3 pt-2">
          <button type="button" className="btn-secondary" onClick={onClose}>Cancel</button>
          <button type="submit" className="btn-primary" disabled={isSubmitting}>
            {isSubmitting ? (
              <><Loader2 size={15} className="animate-spin" /> Saving...</>
            ) : (
              isEditing ? 'Save changes' : 'Add application'
            )}
          </button>
        </div>
      </form>
    </Modal>
  )
}
