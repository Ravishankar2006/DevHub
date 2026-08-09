import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import api from '@/lib/api'
import type { AiJob, GitHubAccount } from '@/lib/types'

export function useGitHubAccount() {
  return useQuery({
    queryKey: ['github', 'account'],
    queryFn: async () => {
      const res = await api.get<GitHubAccount>('/github/account')
      return res.data
    },
  })
}

export function useConnectGitHub() {
  return useMutation({
    mutationFn: async () => {
      const res = await api.get<{ authorizeUrl: string }>('/github/authorize')
      return res.data
    },
    onSuccess: data => {
      window.location.href = data.authorizeUrl
    },
  })
}

export function useDisconnectGitHub() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: async () => {
      await api.delete('/github/account')
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['github'] })
    },
  })
}

export function useTriggerGitHubSync() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: async () => {
      const res = await api.post<AiJob>('/github/sync')
      return res.data
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['github'] })
    },
  })
}
