import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import api from '@/lib/api'
import type { Goal, GoalInput, GoalStatus } from '@/lib/types'

export function useGoals(status?: GoalStatus) {
  return useQuery({
    queryKey: ['goals', { status }],
    queryFn: async () => {
      const res = await api.get<Goal[]>('/goals', { params: status ? { status } : {} })
      return res.data
    },
  })
}

export function useGoal(id: string | undefined) {
  return useQuery({
    queryKey: ['goals', id],
    queryFn: async () => {
      const res = await api.get<Goal>(`/goals/${id}`)
      return res.data
    },
    enabled: !!id,
  })
}

export function useCreateGoal() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: async (input: GoalInput) => {
      const res = await api.post<Goal>('/goals', input)
      return res.data
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['goals'] })
    },
  })
}

export function useUpdateGoal(id: string) {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: async (input: GoalInput) => {
      const res = await api.put<Goal>(`/goals/${id}`, input)
      return res.data
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['goals'] })
    },
  })
}

export function useDeleteGoal() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: async (id: string) => {
      await api.delete(`/goals/${id}`)
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['goals'] })
      queryClient.invalidateQueries({ queryKey: ['habits'] })
    },
  })
}
