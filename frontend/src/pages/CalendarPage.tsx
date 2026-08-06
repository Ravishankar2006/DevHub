import CalendarGrid from '@/components/calendar/CalendarGrid'

export default function CalendarPage() {
  return (
    <div className="max-w-6xl mx-auto animate-fade-in">
      <div className="page-header">
        <h2 className="page-title">Calendar</h2>
        <p className="page-subtitle">Interviews, deadlines, study blocks, and meetings in one place.</p>
      </div>

      <CalendarGrid />
    </div>
  )
}
