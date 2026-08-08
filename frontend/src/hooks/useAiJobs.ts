import { useQuery } from '@tanstack/react-query'
import api from '@/lib/api'
import type { AiJob } from '@/lib/types'

export function useAiJob(jobId: string | undefined) {
  return useQuery({
    queryKey: ['aiJobs', jobId],
    queryFn: async () => {
      const res = await api.get<AiJob>(`/jobs/${jobId}`)
      return res.data
    },
    enabled: !!jobId,
    refetchInterval: query => {
      const status = query.state.data?.status
      return status === 'PENDING' || status === 'PROCESSING' ? 2000 : false
    },
  })
}
