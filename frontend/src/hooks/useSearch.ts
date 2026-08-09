import { useMutation } from '@tanstack/react-query'
import api from '@/lib/api'
import type { SearchResult } from '@/lib/types'

export function useSearch() {
  return useMutation({
    mutationFn: async (query: string) => {
      const res = await api.get<SearchResult[]>('/search', { params: { q: query } })
      return res.data
    },
  })
}
