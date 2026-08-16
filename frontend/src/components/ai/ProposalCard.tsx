import { useEffect, useState } from 'react'
import { AlertTriangle, Bot, Check, Loader2, X } from 'lucide-react'
import { cn } from '@/lib/utils'
import { useConfirmProposal, useRejectProposal } from '@/hooks/useAI'
import type { AIProposedAction } from '@/lib/types'

export default function ProposalCard({
  proposal,
  conversationId,
}: {
  proposal: AIProposedAction
  conversationId: string
}) {
  const confirm = useConfirmProposal(conversationId)
  const reject = useRejectProposal(conversationId)
  const [canConfirm, setCanConfirm] = useState(!proposal.destructive)

  useEffect(() => {
    if (!proposal.destructive) return
    const timer = setTimeout(() => setCanConfirm(true), 2000)
    return () => clearTimeout(timer)
  }, [proposal.destructive])

  const busy = confirm.isPending || reject.isPending

  return (
    <div className="flex gap-3">
      <div
        className={cn(
          'w-7 h-7 rounded-full flex items-center justify-center flex-shrink-0 mt-0.5',
          proposal.destructive ? 'bg-red-500/15 text-red-500' : 'bg-[var(--bg-tertiary)] text-[var(--text-secondary)]'
        )}
      >
        {proposal.destructive ? <AlertTriangle size={14} /> : <Bot size={14} />}
      </div>
      <div
        className={cn(
          'max-w-[75%] rounded-xl px-4 py-3 text-sm space-y-3',
          proposal.destructive
            ? 'bg-red-500/5 border border-red-500/40'
            : 'bg-[var(--bg-tertiary)] border border-[var(--border)]'
        )}
      >
        <p className="text-[var(--text-primary)]">{proposal.summary}</p>
        {proposal.destructive && (
          <p className="text-red-500 text-xs font-medium">This cannot be undone.</p>
        )}
        <div className="flex gap-2">
          <button
            onClick={() => confirm.mutate(proposal.id)}
            disabled={busy || !canConfirm}
            className={cn(
              'btn',
              proposal.destructive
                ? 'border border-red-500 text-red-500 bg-transparent hover:bg-red-500/10'
                : 'btn-primary'
            )}
          >
            {confirm.isPending ? <Loader2 size={14} className="animate-spin" /> : <Check size={14} />}
            Confirm
          </button>
          <button onClick={() => reject.mutate(proposal.id)} disabled={busy} className="btn-ghost">
            {reject.isPending ? <Loader2 size={14} className="animate-spin" /> : <X size={14} />}
            Cancel
          </button>
        </div>
      </div>
    </div>
  )
}
