import { isSameMonth, isToday } from '@/lib/calendarGrid'
import type { CalendarEvent, CalendarEventCategory } from '@/lib/types'

export const categoryBadgeClass: Record<CalendarEventCategory, string> = {
  INTERVIEW: 'badge-blue',
  TASK: 'badge-gray',
  STUDY_BLOCK: 'badge bg-purple-100 text-purple-700 dark:bg-purple-900/30 dark:text-purple-400',
  DEADLINE: 'badge bg-red-100 text-red-700 dark:bg-red-900/30 dark:text-red-400',
  MEETING: 'badge-amber',
  OTHER: 'badge-gray',
}

const MAX_VISIBLE_EVENTS = 3

interface CalendarDayCellProps {
  date: Date
  referenceMonth: Date
  events: CalendarEvent[]
  onDayClick: (date: Date) => void
  onEventClick: (event: CalendarEvent) => void
  onMoreClick: (date: Date) => void
}

export default function CalendarDayCell({ date, referenceMonth, events, onDayClick, onEventClick, onMoreClick }: CalendarDayCellProps) {
  const inMonth = isSameMonth(date, referenceMonth)
  const today = isToday(date)
  const visibleEvents = events.slice(0, MAX_VISIBLE_EVENTS)
  const overflowCount = events.length - visibleEvents.length

  return (
    <div
      onClick={() => onDayClick(date)}
      className={`min-h-[100px] p-2 border border-[var(--border)] flex flex-col gap-1 cursor-pointer hover:bg-[var(--bg-tertiary)] transition-colors ${inMonth ? '' : 'opacity-40'}`}
    >
      <span className={`text-xs font-medium ${today ? 'inline-flex items-center justify-center w-5 h-5 rounded-full bg-brand-500 text-white' : 'text-[var(--text-secondary)]'}`}>
        {date.getDate()}
      </span>

      <div className="flex flex-col gap-1">
        {visibleEvents.map(event => (
          <button
            key={event.id}
            onClick={e => { e.stopPropagation(); onEventClick(event) }}
            className={`${categoryBadgeClass[event.category]} block w-full text-left truncate`}
          >
            {event.title}
          </button>
        ))}
        {overflowCount > 0 && (
          <button
            onClick={e => { e.stopPropagation(); onMoreClick(date) }}
            className="text-xs text-brand-500 hover:underline text-left"
          >
            +{overflowCount} more
          </button>
        )}
      </div>
    </div>
  )
}
