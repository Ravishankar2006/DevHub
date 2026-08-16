import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import api from '@/lib/api'
import type { AIConversationDetail, AIConversationSummary, AIProposedAction } from '@/lib/types'

export function useAIConversations() {
  return useQuery({
    queryKey: ['aiConversations'],
    queryFn: async () => {
      const res = await api.get<AIConversationSummary[]>('/ai/conversations')
      return res.data
    },
  })
}

export function useAIConversation(id: string | undefined) {
  return useQuery({
    queryKey: ['aiConversations', id],
    queryFn: async () => {
      const res = await api.get<AIConversationDetail>(`/ai/conversations/${id}`)
      return res.data
    },
    enabled: !!id,
  })
}

export function useCreateConversation() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: async () => {
      const res = await api.post<AIConversationDetail>('/ai/conversations')
      return res.data
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['aiConversations'] })
    },
  })
}

export function useSendMessage(conversationId: string) {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: async (content: string) => {
      const res = await api.post<AIConversationDetail>(`/ai/conversations/${conversationId}/messages`, { content })
      return res.data
    },
    onSuccess: (data) => {
      queryClient.setQueryData(['aiConversations', conversationId], data)
      queryClient.invalidateQueries({ queryKey: ['aiConversations'] })
    },
  })
}

export function useConfirmProposal(conversationId: string) {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: async (proposalId: string) => {
      const res = await api.post<AIProposedAction>(`/ai/proposals/${proposalId}/confirm`)
      return res.data
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['aiConversations', conversationId] })
    },
  })
}

export function useRejectProposal(conversationId: string) {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: async (proposalId: string) => {
      const res = await api.post<AIProposedAction>(`/ai/proposals/${proposalId}/reject`)
      return res.data
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['aiConversations', conversationId] })
    },
  })
}

export function useDeleteConversation() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: async (id: string) => {
      await api.delete(`/ai/conversations/${id}`)
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['aiConversations'] })
    },
  })
}
