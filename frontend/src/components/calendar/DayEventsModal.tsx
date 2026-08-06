import Modal from '@/components/ui/Modal'
import { categoryBadgeClass } from '@/components/calendar/CalendarDayCell'
import { CALENDAR_CATEGORY_LABELS, type CalendarEvent } from '@/lib/types'
import { formatDate } from '@/lib/utils'

interface DayEventsModalProps {
  date: Date
  events: CalendarEvent[]
  onEventClick: (event: CalendarEvent) => void
  onClose: () => void
}

export default function DayEventsModal({ date, events, onEventClick, onClose }: DayEventsModalProps) {
  return (
    <Modal title={formatDate(date)} onClose={onClose}>
      <div className="space-y-2">
        {events.map(event => (
          <button
            key={event.id}
            onClick={() => onEventClick(event)}
            className="w-full text-left card p-3 flex items-center justify-between gap-2 hover:bg-[var(--bg-tertiary)] transition-colors"
          >
            <span className="text-sm text-[var(--text-primary)] truncate">{event.title}</span>
            <span className={categoryBadgeClass[event.category]}>{CALENDAR_CATEGORY_LABELS[event.category]}</span>
          </button>
        ))}
      </div>
    </Modal>
  )
}
