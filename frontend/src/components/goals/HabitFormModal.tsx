import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import { Loader2 } from 'lucide-react'
import Modal from '@/components/ui/Modal'
import { useCreateHabit, useUpdateHabit } from '@/hooks/useHabits'
import { useGoals } from '@/hooks/useGoals'
import type { Habit, HabitFrequency } from '@/lib/types'

const schema = z.object({
  title: z.string().min(1, 'Title is required'),
  frequency: z.enum(['DAILY', 'WEEKLY']),
  goalId: z.string().optional(),
})

type FormData = z.infer<typeof schema>

interface HabitFormModalProps {
  habit?: Habit
  onClose: () => void
}

export default function HabitFormModal({ habit, onClose }: HabitFormModalProps) {
  const createHabit = useCreateHabit()
  const updateHabit = useUpdateHabit(habit?.id ?? '')
  const { data: goals } = useGoals()
  const isEditing = !!habit

  const { register, handleSubmit, formState: { errors, isSubmitting } } = useForm<FormData>({
    resolver: zodResolver(schema),
    defaultValues: {
      title: habit?.title ?? '',
      frequency: habit?.frequency ?? 'DAILY',
      goalId: habit?.goalId ?? '',
    },
  })

  const onSubmit = async (data: FormData) => {
    const input = {
      title: data.title,
      frequency: data.frequency as HabitFrequency,
      goalId: data.goalId || null,
    }

    if (isEditing) {
      await updateHabit.mutateAsync(input)
    } else {
      await createHabit.mutateAsync(input)
    }
    onClose()
  }

  return (
    <Modal title={isEditing ? 'Edit Habit' : 'New Habit'} onClose={onClose}>
      <form onSubmit={handleSubmit(onSubmit)} className="space-y-4" noValidate>
        <div className="form-group">
          <label htmlFor="habit-title" className="label">Title</label>
          <input id="habit-title" className="input" placeholder="Code for 30 minutes" {...register('title')} />
          {errors.title && <p className="error-text">{errors.title.message}</p>}
        </div>

        <div className="form-group">
          <label htmlFor="habit-frequency" className="label">Frequency</label>
          <select id="habit-frequency" className="input" {...register('frequency')}>
            <option value="DAILY">Daily</option>
            <option value="WEEKLY">Weekly</option>
          </select>
        </div>

        <div className="form-group">
          <label htmlFor="habit-goal" className="label">Related goal (optional)</label>
          <select id="habit-goal" className="input" {...register('goalId')}>
            <option value="">None</option>
            {goals?.map(g => (
              <option key={g.id} value={g.id}>{g.title}</option>
            ))}
          </select>
        </div>

        <div className="flex justify-end gap-3 pt-2">
          <button type="button" className="btn-secondary" onClick={onClose}>Cancel</button>
          <button type="submit" className="btn-primary" disabled={isSubmitting}>
            {isSubmitting ? (
              <><Loader2 size={15} className="animate-spin" /> Saving...</>
            ) : (
              isEditing ? 'Save changes' : 'Create habit'
            )}
          </button>
        </div>
      </form>
    </Modal>
  )
}
