import { Link } from 'react-router-dom'
import { Check, Flame, Target } from 'lucide-react'
import { useHabits, useToggleHabitCheckin } from '@/hooks/useHabits'

export default function TodaysHabitsWidget() {
  const { data: habits, isLoading } = useHabits()
  const toggleCheckin = useToggleHabitCheckin()

  return (
    <div className="card p-5 flex flex-col gap-3">
      <div className="flex items-center justify-between">
        <h3 className="text-sm font-semibold text-[var(--text-primary)] flex items-center gap-1.5">
          <Flame size={15} className="text-amber-500" /> Today's habits
        </h3>
        <Link to="/goals" className="text-xs text-brand-500 hover:underline">View all →</Link>
      </div>

      {isLoading && <p className="text-xs text-[var(--text-muted)]">Loading...</p>}

      {!isLoading && habits?.length === 0 && (
        <p className="text-xs text-[var(--text-muted)]">No habits yet — create one from Goals.</p>
      )}

      {habits && habits.length > 0 && (
        <div className="flex flex-col gap-2">
          {habits.map(habit => (
            <div key={habit.id} className="flex items-center gap-2.5">
              <button
                onClick={() => toggleCheckin.mutate(habit.id)}
                disabled={toggleCheckin.isPending}
                className={`flex-shrink-0 w-6 h-6 rounded-full flex items-center justify-center transition-colors ${
                  habit.checkedInToday
                    ? 'bg-brand-500 text-white'
                    : 'bg-[var(--bg-tertiary)] text-[var(--text-muted)] hover:text-[var(--text-primary)]'
                }`}
                aria-label={habit.checkedInToday ? 'Checked in today' : 'Check in today'}
              >
                <Check size={12} />
              </button>
              <p className="text-sm text-[var(--text-primary)] truncate flex-1">{habit.title}</p>
              <span className="inline-flex items-center gap-1 text-xs text-amber-500 flex-shrink-0">
                <Flame size={11} /> {habit.currentStreak}
              </span>
              {habit.goalTitle && (
                <span className="hidden sm:inline-flex items-center gap-1 text-xs text-[var(--text-muted)] flex-shrink-0">
                  <Target size={11} /> {habit.goalTitle}
                </span>
              )}
            </div>
          ))}
        </div>
      )}
    </div>
  )
}
