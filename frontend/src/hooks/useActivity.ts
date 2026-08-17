import { useQuery } from '@tanstack/react-query'
import api from '@/lib/api'
import type { ActivityLogEntry } from '@/lib/types'

export function useActivityLog() {
  return useQuery({
    queryKey: ['activity'],
    queryFn: async () => {
      const res = await api.get<ActivityLogEntry[]>('/activity')
      return res.data
    },
  })
}
