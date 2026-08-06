import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import { Loader2, Trash2 } from 'lucide-react'
import Modal from '@/components/ui/Modal'
import { useCreateCalendarEvent, useDeleteCalendarEvent, useUpdateCalendarEvent } from '@/hooks/useCalendar'
import { CALENDAR_CATEGORY_LABELS, type CalendarEvent, type CalendarEventCategory } from '@/lib/types'

const schema = z.object({
  title: z.string().min(1, 'Title is required'),
  category: z.enum(['INTERVIEW', 'TASK', 'STUDY_BLOCK', 'DEADLINE', 'MEETING', 'OTHER']),
  allDay: z.boolean(),
  date: z.string().optional(),
  startDateTime: z.string().optional(),
  endDateTime: z.string().optional(),
  location: z.string().optional(),
  description: z.string().optional(),
})

type FormData = z.infer<typeof schema>

function toDateInputValue(date: Date) {
  const pad = (n: number) => String(n).padStart(2, '0')
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())}`
}

function toDateTimeInputValue(date: Date) {
  const pad = (n: number) => String(n).padStart(2, '0')
  return `${toDateInputValue(date)}T${pad(date.getHours())}:${pad(date.getMinutes())}`
}

interface CalendarEventFormModalProps {
  event?: CalendarEvent
  initialDate?: Date
  onClose: () => void
}

export default function CalendarEventFormModal({ event, initialDate, onClose }: CalendarEventFormModalProps) {
  const createEvent = useCreateCalendarEvent()
  const updateEvent = useUpdateCalendarEvent(event?.id ?? '')
  const deleteEvent = useDeleteCalendarEvent()
  const isEditing = !!event

  const baseDate = event ? new Date(event.startTime) : (initialDate ?? new Date())
  const defaultAllDay = event ? event.allDay : true

  const { register, handleSubmit, watch, formState: { errors, isSubmitting } } = useForm<FormData>({
    resolver: zodResolver(schema),
    defaultValues: {
      title: event?.title ?? '',
      category: event?.category ?? 'OTHER',
      allDay: defaultAllDay,
      date: toDateInputValue(baseDate),
      startDateTime: toDateTimeInputValue(baseDate),
      endDateTime: event?.endTime ? toDateTimeInputValue(new Date(event.endTime)) : '',
      location: event?.location ?? '',
      description: event?.description ?? '',
    },
  })

  const allDay = watch('allDay')

  const onSubmit = async (data: FormData) => {
    const startTime = data.allDay
      ? new Date(`${data.date}T00:00:00`).toISOString()
      : new Date(data.startDateTime ?? '').toISOString()

    const endTime = !data.allDay && data.endDateTime ? new Date(data.endDateTime).toISOString() : null

    const input = {
      title: data.title,
      category: data.category as CalendarEventCategory,
      allDay: data.allDay,
      startTime,
      endTime,
      location: data.location || undefined,
      description: data.description || undefined,
    }

    if (isEditing) {
      await updateEvent.mutateAsync(input)
    } else {
      await createEvent.mutateAsync(input)
    }
    onClose()
  }

  const handleDelete = async () => {
    if (!event) return
    if (window.confirm(`Delete "${event.title}"?`)) {
      await deleteEvent.mutateAsync(event.id)
      onClose()
    }
  }

  return (
    <Modal title={isEditing ? 'Edit Event' : 'New Event'} onClose={onClose} wide>
      <form onSubmit={handleSubmit(onSubmit)} className="space-y-4" noValidate>
        <div className="form-group">
          <label htmlFor="event-title" className="label">Title</label>
          <input id="event-title" className="input" placeholder="Interview with Acme" {...register('title')} />
          {errors.title && <p className="error-text">{errors.title.message}</p>}
        </div>

        <div className="grid grid-cols-2 gap-4">
          <div className="form-group">
            <label htmlFor="event-category" className="label">Category</label>
            <select id="event-category" className="input" {...register('category')}>
              {(Object.keys(CALENDAR_CATEGORY_LABELS) as CalendarEventCategory[]).map(category => (
                <option key={category} value={category}>{CALENDAR_CATEGORY_LABELS[category]}</option>
              ))}
            </select>
          </div>
          <div className="form-group">
            <label htmlFor="event-location" className="label">Location</label>
            <input id="event-location" className="input" placeholder="Zoom, office..." {...register('location')} />
          </div>
        </div>

        <div className="flex items-center gap-2">
          <input id="event-all-day" type="checkbox" className="h-4 w-4" {...register('allDay')} />
          <label htmlFor="event-all-day" className="text-sm text-[var(--text-secondary)]">All-day event</label>
        </div>

        {allDay ? (
          <div className="form-group">
            <label htmlFor="event-date" className="label">Date</label>
            <input id="event-date" type="date" className="input" {...register('date')} />
          </div>
        ) : (
          <div className="grid grid-cols-2 gap-4">
            <div className="form-group">
              <label htmlFor="event-start" className="label">Starts</label>
              <input id="event-start" type="datetime-local" className="input" {...register('startDateTime')} />
            </div>
            <div className="form-group">
              <label htmlFor="event-end" className="label">Ends (optional)</label>
              <input id="event-end" type="datetime-local" className="input" {...register('endDateTime')} />
            </div>
          </div>
        )}

        <div className="form-group">
          <label htmlFor="event-description" className="label">Description</label>
          <textarea id="event-description" className="input" rows={3} {...register('description')} />
        </div>

        <div className="flex items-center justify-between gap-3 pt-2">
          {isEditing ? (
            <button type="button" className="btn-ghost text-red-500 text-sm" onClick={handleDelete}>
              <Trash2 size={14} /> Delete
            </button>
          ) : <span />}
          <div className="flex gap-3">
            <button type="button" className="btn-secondary" onClick={onClose}>Cancel</button>
            <button type="submit" className="btn-primary" disabled={isSubmitting}>
              {isSubmitting ? (
                <><Loader2 size={15} className="animate-spin" /> Saving...</>
              ) : (
                isEditing ? 'Save changes' : 'Add event'
              )}
            </button>
          </div>
        </div>
      </form>
    </Modal>
  )
}
