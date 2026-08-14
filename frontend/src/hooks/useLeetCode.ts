import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import api from '@/lib/api'
import type { AiJob, LeetCodeAccount } from '@/lib/types'

export function useLeetCodeAccount() {
  return useQuery({
    queryKey: ['leetcode', 'account'],
    queryFn: async () => {
      const res = await api.get<LeetCodeAccount>('/leetcode/account')
      return res.data
    },
  })
}

export function useConnectLeetCode() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: async (username: string) => {
      const res = await api.post<LeetCodeAccount>('/leetcode/connect', { username })
      return res.data
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['leetcode'] })
    },
  })
}

export function useDisconnectLeetCode() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: async () => {
      await api.delete('/leetcode/account')
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['leetcode'] })
    },
  })
}

export function useTriggerLeetCodeSync() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: async () => {
      const res = await api.post<AiJob>('/leetcode/sync')
      return res.data
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['leetcode'] })
    },
  })
}
