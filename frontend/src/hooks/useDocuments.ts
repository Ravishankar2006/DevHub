import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import api from '@/lib/api'
import type { DocumentItem } from '@/lib/types'

export function useDocuments() {
  return useQuery({
    queryKey: ['documents'],
    queryFn: async () => {
      const res = await api.get<DocumentItem[]>('/documents')
      return res.data
    },
    refetchInterval: query => {
      const documents = query.state.data
      return documents?.some(d => d.status === 'PENDING') ? 2000 : false
    },
  })
}

export function useUploadDocument() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: async ({ file, title }: { file: File; title?: string }) => {
      const formData = new FormData()
      formData.append('file', file)
      if (title) formData.append('title', title)
      const res = await api.post<DocumentItem>('/documents', formData, {
        headers: { 'Content-Type': 'multipart/form-data' },
      })
      return res.data
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['documents'] })
    },
  })
}

export function useDeleteDocument() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: async (id: string) => {
      await api.delete(`/documents/${id}`)
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['documents'] })
    },
  })
}
