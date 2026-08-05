import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import api from '@/lib/api'
import type { LearningResource, LearningResourceInput, LearningResourceType, LearningStatus } from '@/lib/types'

interface LearningFilters {
  status?: LearningStatus
  resourceType?: LearningResourceType
}

export function useLearningResources(filters: LearningFilters = {}) {
  return useQuery({
    queryKey: ['learning', filters],
    queryFn: async () => {
      const res = await api.get<LearningResource[]>('/learning', { params: filters })
      return res.data
    },
  })
}

export function useLearningResource(id: string | undefined) {
  return useQuery({
    queryKey: ['learning', id],
    queryFn: async () => {
      const res = await api.get<LearningResource>(`/learning/${id}`)
      return res.data
    },
    enabled: !!id,
  })
}

export function useCreateLearningResource() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: async (input: LearningResourceInput) => {
      const res = await api.post<LearningResource>('/learning', input)
      return res.data
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['learning'] })
    },
  })
}

export function useUpdateLearningResource(id: string) {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: async (input: LearningResourceInput) => {
      const res = await api.put<LearningResource>(`/learning/${id}`, input)
      return res.data
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['learning'] })
    },
  })
}

export function useDeleteLearningResource() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: async (id: string) => {
      await api.delete(`/learning/${id}`)
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['learning'] })
    },
  })
}
