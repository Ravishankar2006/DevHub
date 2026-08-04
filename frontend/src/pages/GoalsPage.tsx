import { useState } from 'react'
import { Flame, Plus, Target } from 'lucide-react'
import GoalCard from '@/components/goals/GoalCard'
import GoalFormModal from '@/components/goals/GoalFormModal'
import HabitCard from '@/components/goals/HabitCard'
import HabitFormModal from '@/components/goals/HabitFormModal'
import { useDeleteGoal, useGoals } from '@/hooks/useGoals'
import { useDeleteHabit, useHabits, useToggleHabitCheckin } from '@/hooks/useHabits'
import type { Goal, Habit } from '@/lib/types'

export default function GoalsPage() {
  const { data: goals, isLoading: goalsLoading } = useGoals()
  const { data: habits, isLoading: habitsLoading } = useHabits()

  const deleteGoal = useDeleteGoal()
  const deleteHabit = useDeleteHabit()
  const toggleCheckin = useToggleHabitCheckin()

  const [showGoalForm, setShowGoalForm] = useState(false)
  const [editingGoal, setEditingGoal] = useState<Goal | undefined>(undefined)
  const [showHabitForm, setShowHabitForm] = useState(false)
  const [editingHabit, setEditingHabit] = useState<Habit | undefined>(undefined)

  const openCreateGoal = () => { setEditingGoal(undefined); setShowGoalForm(true) }
  const openEditGoal = (goal: Goal) => { setEditingGoal(goal); setShowGoalForm(true) }
  const closeGoalForm = () => { setShowGoalForm(false); setEditingGoal(undefined) }

  const openCreateHabit = () => { setEditingHabit(undefined); setShowHabitForm(true) }
  const openEditHabit = (habit: Habit) => { setEditingHabit(habit); setShowHabitForm(true) }
  const closeHabitForm = () => { setShowHabitForm(false); setEditingHabit(undefined) }

  const handleDeleteGoal = (goal: Goal) => {
    if (window.confirm(`Delete goal "${goal.title}"? Linked habits will be unlinked, not deleted.`)) {
      deleteGoal.mutate(goal.id)
    }
  }

  const handleDeleteHabit = (habit: Habit) => {
    if (window.confirm(`Delete habit "${habit.title}"?`)) {
      deleteHabit.mutate(habit.id)
    }
  }

  return (
    <div className="max-w-6xl mx-auto animate-fade-in">
      <div className="page-header">
        <h2 className="page-title">Goals & Habits</h2>
        <p className="page-subtitle">Track your goals and build streaks with daily or weekly habits.</p>
      </div>

      <div className="flex items-center justify-between mb-4">
        <h3 className="section-title mb-0">Goals</h3>
        <button onClick={openCreateGoal} className="btn-ghost text-xs">
          <Plus size={14} /> Add goal
        </button>
      </div>

      {goalsLoading && <p className="text-sm text-[var(--text-muted)]">Loading goals...</p>}

      {!goalsLoading && goals?.length === 0 && (
        <div className="card p-8 flex flex-col items-center text-center mb-8">
          <div className="w-12 h-12 rounded-xl bg-brand-500/10 flex items-center justify-center mb-3">
            <Target size={20} className="text-brand-500" />
          </div>
          <p className="text-sm text-[var(--text-secondary)]">No goals yet. Set your first one.</p>
        </div>
      )}

      {!goalsLoading && goals && goals.length > 0 && (
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4 mb-8">
          {goals.map(goal => (
            <GoalCard
              key={goal.id}
              goal={goal}
              onEdit={() => openEditGoal(goal)}
              onDelete={() => handleDeleteGoal(goal)}
            />
          ))}
        </div>
      )}

      <div className="flex items-center justify-between mb-4">
        <h3 className="section-title mb-0">Habits</h3>
        <button onClick={openCreateHabit} className="btn-ghost text-xs">
          <Plus size={14} /> Add habit
        </button>
      </div>

      {habitsLoading && <p className="text-sm text-[var(--text-muted)]">Loading habits...</p>}

      {!habitsLoading && habits?.length === 0 && (
        <div className="card p-8 flex flex-col items-center text-center">
          <div className="w-12 h-12 rounded-xl bg-brand-500/10 flex items-center justify-center mb-3">
            <Flame size={20} className="text-brand-500" />
          </div>
          <p className="text-sm text-[var(--text-secondary)]">No habits yet. Start a streak.</p>
        </div>
      )}

      {!habitsLoading && habits && habits.length > 0 && (
        <div className="space-y-2">
          {habits.map(habit => (
            <HabitCard
              key={habit.id}
              habit={habit}
              isToggling={toggleCheckin.isPending}
              onToggleCheckin={() => toggleCheckin.mutate(habit.id)}
              onEdit={() => openEditHabit(habit)}
              onDelete={() => handleDeleteHabit(habit)}
            />
          ))}
        </div>
      )}

      {showGoalForm && <GoalFormModal goal={editingGoal} onClose={closeGoalForm} />}
      {showHabitForm && <HabitFormModal habit={editingHabit} onClose={closeHabitForm} />}
    </div>
  )
}
