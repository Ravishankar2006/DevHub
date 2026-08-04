import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import { Loader2 } from 'lucide-react'
import Modal from '@/components/ui/Modal'
import { useCreateNote, useUpdateNote } from '@/hooks/useNotes'
import { useNoteFolders } from '@/hooks/useNoteFolders'
import type { Note } from '@/lib/types'

const schema = z.object({
  title: z.string().min(1, 'Title is required'),
  content: z.string().optional(),
  tags: z.string().optional(),
  folderId: z.string().optional(),
})

type FormData = z.infer<typeof schema>

interface NoteFormModalProps {
  note?: Note
  defaultFolderId?: string | null
  onClose: () => void
}

export default function NoteFormModal({ note, defaultFolderId, onClose }: NoteFormModalProps) {
  const createNote = useCreateNote()
  const updateNote = useUpdateNote(note?.id ?? '')
  const { data: folders } = useNoteFolders()
  const isEditing = !!note

  const { register, handleSubmit, formState: { errors, isSubmitting } } = useForm<FormData>({
    resolver: zodResolver(schema),
    defaultValues: {
      title: note?.title ?? '',
      content: note?.content ?? '',
      tags: note?.tags?.join(', ') ?? '',
      folderId: note?.folderId ?? defaultFolderId ?? '',
    },
  })

  const onSubmit = async (data: FormData) => {
    const input = {
      title: data.title,
      content: data.content || undefined,
      tags: data.tags ? data.tags.split(',').map(t => t.trim()).filter(Boolean) : [],
      folderId: data.folderId || null,
    }

    if (isEditing) {
      await updateNote.mutateAsync(input)
    } else {
      await createNote.mutateAsync(input)
    }
    onClose()
  }

  return (
    <Modal title={isEditing ? 'Edit Note' : 'New Note'} onClose={onClose} wide>
      <form onSubmit={handleSubmit(onSubmit)} className="space-y-4" noValidate>
        <div className="form-group">
          <label htmlFor="note-title" className="label">Title</label>
          <input id="note-title" className="input" placeholder="Note title" {...register('title')} />
          {errors.title && <p className="error-text">{errors.title.message}</p>}
        </div>

        <div className="grid grid-cols-2 gap-4">
          <div className="form-group">
            <label htmlFor="note-folder" className="label">Folder</label>
            <select id="note-folder" className="input" {...register('folderId')}>
              <option value="">Unfiled</option>
              {folders?.map(f => (
                <option key={f.id} value={f.id}>{f.name}</option>
              ))}
            </select>
          </div>
          <div className="form-group">
            <label htmlFor="note-tags" className="label">Tags (comma-separated)</label>
            <input id="note-tags" className="input" placeholder="backend, ideas" {...register('tags')} />
          </div>
        </div>

        <div className="form-group">
          <label htmlFor="note-content" className="label">Content (markdown)</label>
          <textarea id="note-content" className="input" rows={10} placeholder="Write your note..." {...register('content')} />
        </div>

        <div className="flex justify-end gap-3 pt-2">
          <button type="button" className="btn-secondary" onClick={onClose}>Cancel</button>
          <button type="submit" className="btn-primary" disabled={isSubmitting}>
            {isSubmitting ? (
              <><Loader2 size={15} className="animate-spin" /> Saving...</>
            ) : (
              isEditing ? 'Save changes' : 'Create note'
            )}
          </button>
        </div>
      </form>
    </Modal>
  )
}
