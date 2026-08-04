import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import api from '@/lib/api'
import type { Note, NoteInput } from '@/lib/types'

interface NoteFilters {
  folderId?: string
  q?: string
}

export function useNotes(filters: NoteFilters = {}) {
  return useQuery({
    queryKey: ['notes', filters],
    queryFn: async () => {
      const res = await api.get<Note[]>('/notes', { params: filters })
      return res.data
    },
  })
}

export function useNote(id: string | undefined) {
  return useQuery({
    queryKey: ['notes', id],
    queryFn: async () => {
      const res = await api.get<Note>(`/notes/${id}`)
      return res.data
    },
    enabled: !!id,
  })
}

export function useCreateNote() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: async (input: NoteInput) => {
      const res = await api.post<Note>('/notes', input)
      return res.data
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['notes'] })
      queryClient.invalidateQueries({ queryKey: ['noteFolders'] })
    },
  })
}

export function useUpdateNote(id: string) {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: async (input: NoteInput) => {
      const res = await api.put<Note>(`/notes/${id}`, input)
      return res.data
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['notes'] })
      queryClient.invalidateQueries({ queryKey: ['noteFolders'] })
    },
  })
}

export function useDeleteNote() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: async (id: string) => {
      await api.delete(`/notes/${id}`)
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['notes'] })
      queryClient.invalidateQueries({ queryKey: ['noteFolders'] })
    },
  })
}
