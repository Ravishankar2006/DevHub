import { Link } from 'react-router-dom'
import { CalendarDays } from 'lucide-react'
import { categoryBadgeClass } from '@/components/calendar/CalendarDayCell'
import { useUpcomingCalendarEvents } from '@/hooks/useCalendar'
import { CALENDAR_CATEGORY_LABELS } from '@/lib/types'
import { formatRelativeTime } from '@/lib/utils'

export default function UpcomingCalendarEventsWidget() {
  const { data: events, isLoading } = useUpcomingCalendarEvents(5)

  return (
    <div className="card p-5 flex flex-col gap-3">
      <div className="flex items-center justify-between">
        <h3 className="text-sm font-semibold text-[var(--text-primary)] flex items-center gap-1.5">
          <CalendarDays size={15} className="text-brand-500" /> Upcoming events
        </h3>
        <Link to="/calendar" className="text-xs text-brand-500 hover:underline">View all →</Link>
      </div>

      {isLoading && <p className="text-xs text-[var(--text-muted)]">Loading...</p>}

      {!isLoading && events?.length === 0 && (
        <p className="text-xs text-[var(--text-muted)]">No upcoming events.</p>
      )}

      {events && events.length > 0 && (
        <div className="flex flex-col gap-3">
          {events.map(event => (
            <div key={event.id} className="flex items-center justify-between gap-2">
              <div className="min-w-0 flex items-center gap-2">
                <span className={categoryBadgeClass[event.category]}>{CALENDAR_CATEGORY_LABELS[event.category]}</span>
                <p className="text-sm text-[var(--text-primary)] truncate">{event.title}</p>
              </div>
              <span className="text-xs text-[var(--text-muted)] flex-shrink-0">{formatRelativeTime(event.startTime)}</span>
            </div>
          ))}
        </div>
      )}
    </div>
  )
}
