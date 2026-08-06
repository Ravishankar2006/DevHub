import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import api from '@/lib/api'
import type { Resume, ResumeMetadataInput } from '@/lib/types'

interface ResumeFilters {
  label?: string
}

interface CreateResumeInput {
  file: File
  name: string
  label?: string
  notes?: string
}

export function useResumes(filters: ResumeFilters = {}) {
  return useQuery({
    queryKey: ['resumes', filters],
    queryFn: async () => {
      const res = await api.get<Resume[]>('/resumes', { params: filters })
      return res.data
    },
  })
}

export function useResume(id: string | undefined) {
  return useQuery({
    queryKey: ['resumes', id],
    queryFn: async () => {
      const res = await api.get<Resume>(`/resumes/${id}`)
      return res.data
    },
    enabled: !!id,
  })
}

export function useCreateResume() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: async (input: CreateResumeInput) => {
      const formData = new FormData()
      formData.append('file', input.file)
      formData.append('name', input.name)
      if (input.label) formData.append('label', input.label)
      if (input.notes) formData.append('notes', input.notes)

      const res = await api.post<Resume>('/resumes', formData, {
        headers: { 'Content-Type': undefined },
      })
      return res.data
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['resumes'] })
    },
  })
}

export function useUpdateResume(id: string) {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: async (input: ResumeMetadataInput) => {
      const res = await api.put<Resume>(`/resumes/${id}`, input)
      return res.data
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['resumes'] })
    },
  })
}

export function useDeleteResume() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: async (id: string) => {
      await api.delete(`/resumes/${id}`)
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['resumes'] })
    },
  })
}

export async function downloadResume(id: string, fileName: string) {
  const res = await api.get(`/resumes/${id}/download`, { responseType: 'blob' })
  const url = window.URL.createObjectURL(res.data)
  const a = document.createElement('a')
  a.href = url
  a.download = fileName
  document.body.appendChild(a)
  a.click()
  a.remove()
  window.URL.revokeObjectURL(url)
}
