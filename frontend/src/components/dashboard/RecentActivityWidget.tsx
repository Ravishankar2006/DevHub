import { History, CheckSquare, Flame, StickyNote, CalendarDays, Target, FolderKanban, Bot } from 'lucide-react'
import { useActivityLog } from '@/hooks/useActivity'
import { formatRelativeTime, cn } from '@/lib/utils'
import type { ActivityLogEntry } from '@/lib/types'

function iconForActionType(actionType: string) {
  if (actionType.includes('TASK')) return { Icon: CheckSquare, color: 'text-emerald-500' }
  if (actionType.includes('HABIT')) return { Icon: Flame, color: 'text-orange-500' }
  if (actionType.includes('NOTE')) return { Icon: StickyNote, color: 'text-purple-500' }
  if (actionType.includes('CALENDAR')) return { Icon: CalendarDays, color: 'text-blue-500' }
  if (actionType.includes('GOAL')) return { Icon: Target, color: 'text-amber-500' }
  if (actionType.includes('PROJECT')) return { Icon: FolderKanban, color: 'text-brand-500' }
  return { Icon: History, color: 'text-[var(--text-muted)]' }
}

function ActivityRow({ entry }: { entry: ActivityLogEntry }) {
  const { Icon, color } = iconForActionType(entry.actionType)

  return (
    <div className="flex items-start gap-2.5">
      <Icon size={14} className={cn(color, 'mt-0.5 flex-shrink-0')} />
      <div className="min-w-0 flex-1">
        <p className="text-sm text-[var(--text-primary)] truncate">{entry.summary}</p>
        <div className="flex items-center gap-1.5 mt-0.5">
          {entry.source === 'AI_AGENT' && (
            <span className="badge-blue inline-flex items-center gap-1">
              <Bot size={10} /> AI
            </span>
          )}
          <span className="text-xs text-[var(--text-muted)]">{formatRelativeTime(entry.createdAt)}</span>
        </div>
      </div>
    </div>
  )
}

export default function RecentActivityWidget() {
  const { data: entries, isLoading } = useActivityLog()
  const recent = (entries ?? []).slice(0, 6)

  return (
    <div className="card p-5 flex flex-col gap-3">
      <h3 className="text-sm font-semibold text-[var(--text-primary)] flex items-center gap-1.5">
        <History size={15} className="text-[var(--text-secondary)]" /> Recent activity
      </h3>

      {isLoading && <p className="text-xs text-[var(--text-muted)]">Loading...</p>}

      {!isLoading && recent.length === 0 && (
        <p className="text-xs text-[var(--text-muted)]">No activity yet.</p>
      )}

      {recent.length > 0 && (
        <div className="flex flex-col gap-2.5">
          {recent.map(entry => (
            <ActivityRow key={entry.id} entry={entry} />
          ))}
        </div>
      )}
    </div>
  )
}
