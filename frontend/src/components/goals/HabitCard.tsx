import { Check, Flame, Pencil, Target, Trash2 } from 'lucide-react'
import type { Habit } from '@/lib/types'

interface HabitCardProps {
  habit: Habit
  onToggleCheckin: () => void
  onEdit: () => void
  onDelete: () => void
  isToggling?: boolean
}

export default function HabitCard({ habit, onToggleCheckin, onEdit, onDelete, isToggling }: HabitCardProps) {
  return (
    <div className="card p-4 flex items-center gap-3">
      <button
        onClick={onToggleCheckin}
        disabled={isToggling}
        className={`flex-shrink-0 w-9 h-9 rounded-full flex items-center justify-center transition-colors ${
          habit.checkedInToday
            ? 'bg-brand-500 text-white'
            : 'bg-[var(--bg-tertiary)] text-[var(--text-muted)] hover:text-[var(--text-primary)]'
        }`}
        aria-label={habit.checkedInToday ? 'Checked in today' : 'Check in today'}
      >
        <Check size={16} />
      </button>

      <div className="flex-1 min-w-0">
        <p className="text-sm font-medium text-[var(--text-primary)] truncate">{habit.title}</p>
        <div className="flex items-center gap-3 mt-0.5">
          <span className="inline-flex items-center gap-1 text-xs text-amber-500">
            <Flame size={12} /> {habit.currentStreak} day{habit.currentStreak === 1 ? '' : 's'}
          </span>
          <span className="text-xs text-[var(--text-muted)]">Best: {habit.longestStreak}</span>
          {habit.goalTitle && (
            <span className="inline-flex items-center gap-1 text-xs text-[var(--text-muted)]">
              <Target size={11} /> {habit.goalTitle}
            </span>
          )}
        </div>
      </div>

      <div className="flex items-center gap-1 flex-shrink-0">
        <button onClick={onEdit} className="btn-ghost p-1.5 rounded-lg" aria-label="Edit habit">
          <Pencil size={13} />
        </button>
        <button onClick={onDelete} className="btn-ghost p-1.5 rounded-lg text-red-500" aria-label="Delete habit">
          <Trash2 size={13} />
        </button>
      </div>
    </div>
  )
}
