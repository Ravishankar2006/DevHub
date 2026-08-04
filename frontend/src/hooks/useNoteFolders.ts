import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import api from '@/lib/api'
import type { NoteFolder, NoteFolderInput } from '@/lib/types'

export function useNoteFolders() {
  return useQuery({
    queryKey: ['noteFolders'],
    queryFn: async () => {
      const res = await api.get<NoteFolder[]>('/notes/folders')
      return res.data
    },
  })
}

export function useCreateNoteFolder() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: async (input: NoteFolderInput) => {
      const res = await api.post<NoteFolder>('/notes/folders', input)
      return res.data
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['noteFolders'] })
    },
  })
}

export function useUpdateNoteFolder(id: string) {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: async (input: NoteFolderInput) => {
      const res = await api.put<NoteFolder>(`/notes/folders/${id}`, input)
      return res.data
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['noteFolders'] })
    },
  })
}

export function useDeleteNoteFolder() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: async (id: string) => {
      await api.delete(`/notes/folders/${id}`)
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['noteFolders'] })
      queryClient.invalidateQueries({ queryKey: ['notes'] })
    },
  })
}
