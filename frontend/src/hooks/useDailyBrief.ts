import axios from 'axios'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import api from '@/lib/api'
import type { AiJob, DailyBrief } from '@/lib/types'

export function useTodayBrief() {
  return useQuery({
    queryKey: ['dailyBrief', 'today'],
    queryFn: async () => {
      try {
        const res = await api.get<DailyBrief>('/brief/today')
        return res.data
      } catch (err) {
        if (axios.isAxiosError(err) && err.response?.status === 404) {
          return null
        }
        throw err
      }
    },
  })
}

export function useGenerateDailyBrief() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: async () => {
      const res = await api.post<AiJob>('/brief/generate')
      return res.data
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['dailyBrief'] })
    },
  })
}
