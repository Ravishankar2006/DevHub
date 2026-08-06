import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import api from '@/lib/api'
import type { JobApplication, JobApplicationInput, JobApplicationStatus, JobStatusChangeInput } from '@/lib/types'

interface JobApplicationFilters {
  status?: JobApplicationStatus
  companyId?: string
}

export function useJobApplications(filters: JobApplicationFilters = {}) {
  return useQuery({
    queryKey: ['jobApplications', filters],
    queryFn: async () => {
      const res = await api.get<JobApplication[]>('/careers/applications', { params: filters })
      return res.data
    },
  })
}

export function useJobApplication(id: string | undefined) {
  return useQuery({
    queryKey: ['jobApplications', id],
    queryFn: async () => {
      const res = await api.get<JobApplication>(`/careers/applications/${id}`)
      return res.data
    },
    enabled: !!id,
  })
}

export function useCreateJobApplication() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: async (input: JobApplicationInput) => {
      const res = await api.post<JobApplication>('/careers/applications', input)
      return res.data
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['jobApplications'] })
    },
  })
}

export function useUpdateJobApplication(id: string) {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: async (input: JobApplicationInput) => {
      const res = await api.put<JobApplication>(`/careers/applications/${id}`, input)
      return res.data
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['jobApplications'] })
    },
  })
}

export function useDeleteJobApplication() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: async (id: string) => {
      await api.delete(`/careers/applications/${id}`)
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['jobApplications'] })
    },
  })
}

export function useChangeJobApplicationStatus(id: string) {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: async (input: JobStatusChangeInput) => {
      const res = await api.patch<JobApplication>(`/careers/applications/${id}/status`, input)
      return res.data
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['jobApplications'] })
    },
  })
}
