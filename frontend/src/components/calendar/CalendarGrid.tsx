import { useMemo, useState } from 'react'
import { ChevronLeft, ChevronRight } from 'lucide-react'
import CalendarDayCell from '@/components/calendar/CalendarDayCell'
import CalendarEventFormModal from '@/components/calendar/CalendarEventFormModal'
import DayEventsModal from '@/components/calendar/DayEventsModal'
import { useCalendarEvents } from '@/hooks/useCalendar'
import { getMonthGridDates } from '@/lib/calendarGrid'
import type { CalendarEvent } from '@/lib/types'

const WEEKDAY_LABELS = ['Sun', 'Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat']

export default function CalendarGrid() {
  const [currentMonth, setCurrentMonth] = useState(() => {
    const now = new Date()
    return new Date(now.getFullYear(), now.getMonth(), 1)
  })

  const [formModal, setFormModal] = useState<{ event?: CalendarEvent; initialDate?: Date } | null>(null)
  const [dayEventsModalDate, setDayEventsModalDate] = useState<Date | null>(null)

  const gridDates = useMemo(
    () => getMonthGridDates(currentMonth.getFullYear(), currentMonth.getMonth()),
    [currentMonth],
  )

  const from = gridDates[0]
  const to = useMemo(() => {
    const last = new Date(gridDates[gridDates.length - 1])
    last.setHours(23, 59, 59, 999)
    return last
  }, [gridDates])

  const { data: events } = useCalendarEvents({ from: from.toISOString(), to: to.toISOString() })

  const eventsByDay = useMemo(() => {
    const map = new Map<string, CalendarEvent[]>()
    for (const date of gridDates) {
      map.set(date.toDateString(), [])
    }
    for (const event of events ?? []) {
      const day = new Date(event.startTime).toDateString()
      map.get(day)?.push(event)
    }
    return map
  }, [events, gridDates])

  const goToPrevMonth = () => setCurrentMonth(m => new Date(m.getFullYear(), m.getMonth() - 1, 1))
  const goToNextMonth = () => setCurrentMonth(m => new Date(m.getFullYear(), m.getMonth() + 1, 1))
  const goToToday = () => {
    const now = new Date()
    setCurrentMonth(new Date(now.getFullYear(), now.getMonth(), 1))
  }

  const monthLabel = currentMonth.toLocaleDateString('en-US', { month: 'long', year: 'numeric' })

  const dayEventsForModal = dayEventsModalDate
    ? (eventsByDay.get(dayEventsModalDate.toDateString()) ?? [])
    : []

  return (
    <div>
      <div className="flex items-center justify-between mb-4">
        <div className="flex items-center gap-2">
          <button onClick={goToPrevMonth} className="btn-ghost p-1.5 rounded-lg" aria-label="Previous month">
            <ChevronLeft size={16} />
          </button>
          <h3 className="text-lg font-semibold text-[var(--text-primary)] w-44 text-center">{monthLabel}</h3>
          <button onClick={goToNextMonth} className="btn-ghost p-1.5 rounded-lg" aria-label="Next month">
            <ChevronRight size={16} />
          </button>
          <button onClick={goToToday} className="btn-secondary text-xs ml-2">Today</button>
        </div>
        <button onClick={() => setFormModal({ initialDate: new Date() })} className="btn-primary text-sm">
          + New event
        </button>
      </div>

      <div className="grid grid-cols-7 border-t border-l border-[var(--border)]">
        {WEEKDAY_LABELS.map(label => (
          <div key={label} className="p-2 text-xs font-medium text-[var(--text-muted)] border-r border-b border-[var(--border)] text-center">
            {label}
          </div>
        ))}
        {gridDates.map(date => (
          <CalendarDayCell
            key={date.toDateString()}
            date={date}
            referenceMonth={currentMonth}
            events={eventsByDay.get(date.toDateString()) ?? []}
            onDayClick={d => setFormModal({ initialDate: d })}
            onEventClick={event => setFormModal({ event })}
            onMoreClick={d => setDayEventsModalDate(d)}
          />
        ))}
      </div>

      {formModal && (
        <CalendarEventFormModal
          event={formModal.event}
          initialDate={formModal.initialDate}
          onClose={() => setFormModal(null)}
        />
      )}

      {dayEventsModalDate && (
        <DayEventsModal
          date={dayEventsModalDate}
          events={dayEventsForModal}
          onEventClick={event => { setDayEventsModalDate(null); setFormModal({ event }) }}
          onClose={() => setDayEventsModalDate(null)}
        />
      )}
    </div>
  )
}
