import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import api from '@/lib/api'
import type { Milestone, MilestoneInput } from '@/lib/types'

export function useMilestones(projectId: string | undefined) {
  return useQuery({
    queryKey: ['milestones', projectId],
    queryFn: async () => {
      const res = await api.get<Milestone[]>(`/projects/${projectId}/milestones`)
      return res.data
    },
    enabled: !!projectId,
  })
}

export function useCreateMilestone(projectId: string) {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: async (input: MilestoneInput) => {
      const res = await api.post<Milestone>(`/projects/${projectId}/milestones`, input)
      return res.data
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['milestones', projectId] })
      queryClient.invalidateQueries({ queryKey: ['projects', projectId] })
    },
  })
}

export function useUpdateMilestone(projectId: string, milestoneId: string) {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: async (input: MilestoneInput) => {
      const res = await api.put<Milestone>(`/projects/${projectId}/milestones/${milestoneId}`, input)
      return res.data
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['milestones', projectId] })
    },
  })
}

export function useDeleteMilestone(projectId: string) {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: async (milestoneId: string) => {
      await api.delete(`/projects/${projectId}/milestones/${milestoneId}`)
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['milestones', projectId] })
      queryClient.invalidateQueries({ queryKey: ['tasks'] })
    },
  })
}
