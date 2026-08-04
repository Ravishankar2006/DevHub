import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import api from '@/lib/api'
import type { Habit, HabitInput } from '@/lib/types'

export function useHabits(goalId?: string) {
  return useQuery({
    queryKey: ['habits', { goalId }],
    queryFn: async () => {
      const res = await api.get<Habit[]>('/habits', { params: goalId ? { goalId } : {} })
      return res.data
    },
  })
}

export function useCreateHabit() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: async (input: HabitInput) => {
      const res = await api.post<Habit>('/habits', input)
      return res.data
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['habits'] })
    },
  })
}

export function useUpdateHabit(id: string) {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: async (input: HabitInput) => {
      const res = await api.put<Habit>(`/habits/${id}`, input)
      return res.data
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['habits'] })
    },
  })
}

export function useDeleteHabit() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: async (id: string) => {
      await api.delete(`/habits/${id}`)
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['habits'] })
    },
  })
}

export function useToggleHabitCheckin() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: async (habitId: string) => {
      const res = await api.post<Habit>(`/habits/${habitId}/checkins/today`)
      return res.data
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['habits'] })
    },
  })
}
