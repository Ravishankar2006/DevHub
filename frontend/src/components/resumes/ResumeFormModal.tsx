import { useState } from 'react'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import { Loader2, Upload } from 'lucide-react'
import Modal from '@/components/ui/Modal'
import { useCreateResume, useUpdateResume } from '@/hooks/useResumes'
import type { Resume } from '@/lib/types'

const schema = z.object({
  name: z.string().min(1, 'Name is required'),
  label: z.string().optional(),
  notes: z.string().optional(),
})

type FormData = z.infer<typeof schema>

interface ResumeFormModalProps {
  resume?: Resume
  onClose: () => void
}

export default function ResumeFormModal({ resume, onClose }: ResumeFormModalProps) {
  const createResume = useCreateResume()
  const updateResume = useUpdateResume(resume?.id ?? '')
  const isEditing = !!resume

  const [file, setFile] = useState<File | null>(null)
  const [fileError, setFileError] = useState<string | undefined>(undefined)

  const { register, handleSubmit, formState: { errors, isSubmitting } } = useForm<FormData>({
    resolver: zodResolver(schema),
    defaultValues: {
      name: resume?.name ?? '',
      label: resume?.label ?? '',
      notes: resume?.notes ?? '',
    },
  })

  const handleFileChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const selected = e.target.files?.[0] ?? null
    if (selected && selected.type !== 'application/pdf' && !selected.name.toLowerCase().endsWith('.pdf')) {
      setFile(null)
      setFileError('Only PDF files are allowed')
      return
    }
    setFile(selected)
    setFileError(undefined)
  }

  const onSubmit = async (data: FormData) => {
    if (isEditing) {
      await updateResume.mutateAsync({
        name: data.name,
        label: data.label || undefined,
        notes: data.notes || undefined,
      })
    } else {
      if (!file) {
        setFileError('Select a PDF file to upload')
        return
      }
      await createResume.mutateAsync({
        file,
        name: data.name,
        label: data.label || undefined,
        notes: data.notes || undefined,
      })
    }
    onClose()
  }

  return (
    <Modal title={isEditing ? 'Edit Resume' : 'Upload Resume'} onClose={onClose}>
      <form onSubmit={handleSubmit(onSubmit)} className="space-y-4" noValidate>
        {!isEditing && (
          <div className="form-group">
            <label htmlFor="resume-file" className="label">PDF file</label>
            <label
              htmlFor="resume-file"
              className="input flex items-center gap-2 cursor-pointer text-[var(--text-secondary)]"
            >
              <Upload size={15} />
              {file ? file.name : 'Choose a PDF...'}
            </label>
            <input
              id="resume-file"
              type="file"
              accept="application/pdf,.pdf"
              className="hidden"
              onChange={handleFileChange}
            />
            {fileError && <p className="error-text">{fileError}</p>}
          </div>
        )}

        <div className="form-group">
          <label htmlFor="resume-name" className="label">Name</label>
          <input id="resume-name" className="input" placeholder="Backend Resume v2" {...register('name')} />
          {errors.name && <p className="error-text">{errors.name.message}</p>}
        </div>

        <div className="form-group">
          <label htmlFor="resume-label" className="label">Label</label>
          <input id="resume-label" className="input" placeholder="backend, frontend, general..." {...register('label')} />
        </div>

        <div className="form-group">
          <label htmlFor="resume-notes" className="label">Notes</label>
          <textarea id="resume-notes" className="input" rows={3} {...register('notes')} />
        </div>

        <div className="flex justify-end gap-3 pt-2">
          <button type="button" className="btn-secondary" onClick={onClose}>Cancel</button>
          <button type="submit" className="btn-primary" disabled={isSubmitting}>
            {isSubmitting ? (
              <><Loader2 size={15} className="animate-spin" /> Saving...</>
            ) : (
              isEditing ? 'Save changes' : 'Upload resume'
            )}
          </button>
        </div>
      </form>
    </Modal>
  )
}
