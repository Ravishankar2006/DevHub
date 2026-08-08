import { useEffect, useState } from 'react'
import { useQueryClient } from '@tanstack/react-query'
import { Loader2, RefreshCw, Sparkles } from 'lucide-react'
import { useTodayBrief, useGenerateDailyBrief } from '@/hooks/useDailyBrief'
import { useAiJob } from '@/hooks/useAiJobs'
import { formatRelativeTime } from '@/lib/utils'

export default function DailyBriefWidget() {
  const { data: brief, isLoading } = useTodayBrief()
  const generateBrief = useGenerateDailyBrief()
  const queryClient = useQueryClient()

  const [activeJobId, setActiveJobId] = useState<string | null>(null)
  const { data: activeJob } = useAiJob(activeJobId ?? undefined)

  useEffect(() => {
    if (!activeJob) return
    if (activeJob.status === 'COMPLETED' || activeJob.status === 'FAILED') {
      if (activeJob.status === 'FAILED') {
        window.alert(`Daily brief generation failed: ${activeJob.errorMessage ?? 'Unknown error'}`)
      }
      queryClient.invalidateQueries({ queryKey: ['dailyBrief'] })
      setActiveJobId(null)
    }
  }, [activeJob, queryClient])

  const handleGenerate = async () => {
    const job = await generateBrief.mutateAsync()
    setActiveJobId(job.id)
  }

  const isGenerating = activeJobId !== null

  return (
    <div className="card p-5 flex flex-col gap-3">
      <div className="flex items-center justify-between">
        <h3 className="text-sm font-semibold text-[var(--text-primary)] flex items-center gap-1.5">
          <Sparkles size={15} className="text-brand-500" /> Today's brief
        </h3>
        {brief && !isGenerating && (
          <button
            onClick={handleGenerate}
            className="text-xs text-brand-500 hover:underline flex items-center gap-1"
          >
            <RefreshCw size={11} /> Regenerate
          </button>
        )}
      </div>

      {isLoading && <p className="text-xs text-[var(--text-muted)]">Loading...</p>}

      {isGenerating && (
        <div className="flex items-center gap-2 text-sm text-[var(--text-secondary)]">
          <Loader2 size={14} className="animate-spin" /> Generating your brief...
        </div>
      )}

      {!isLoading && !isGenerating && !brief && (
        <div className="flex flex-col gap-3">
          <p className="text-sm text-[var(--text-secondary)]">
            Get an AI summary of what's on your plate today, pulled from your tasks, goals, deadlines,
            learning items, and calendar.
          </p>
          <button onClick={handleGenerate} className="btn-secondary text-sm self-start">
            <Sparkles size={14} /> Generate my brief
          </button>
        </div>
      )}

      {!isGenerating && brief && (
        <>
          <p className="text-sm text-[var(--text-primary)] whitespace-pre-wrap">{brief.content}</p>
          <p className="text-xs text-[var(--text-muted)]">Generated {formatRelativeTime(brief.generatedAt)}</p>
        </>
      )}
    </div>
  )
}
