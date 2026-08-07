import { Plus, MessageSquare, Trash2 } from 'lucide-react'
import { cn, formatRelativeTime } from '@/lib/utils'
import type { AIConversationSummary } from '@/lib/types'

interface ConversationListProps {
  conversations: AIConversationSummary[]
  selectedId: string | null
  onSelect: (id: string) => void
  onNew: () => void
  onDelete: (conversation: AIConversationSummary) => void
}

export default function ConversationList({ conversations, selectedId, onSelect, onNew, onDelete }: ConversationListProps) {
  return (
    <div className="card flex flex-col h-full">
      <div className="p-3 border-b border-[var(--border)]">
        <button onClick={onNew} className="btn-primary w-full text-sm">
          <Plus size={16} />
          New chat
        </button>
      </div>

      <div className="flex-1 overflow-y-auto p-2 space-y-1">
        {conversations.length === 0 && (
          <p className="text-sm text-[var(--text-muted)] text-center py-6 px-2">
            No conversations yet. Start a new chat to talk with the AI assistant.
          </p>
        )}
        {conversations.map(conversation => (
          <div
            key={conversation.id}
            onClick={() => onSelect(conversation.id)}
            className={cn(
              'group flex items-start gap-2 px-3 py-2.5 rounded-lg cursor-pointer transition-colors',
              selectedId === conversation.id
                ? 'bg-brand-100 dark:bg-brand-900/30'
                : 'hover:bg-[var(--bg-tertiary)]'
            )}
          >
            <MessageSquare size={15} className="flex-shrink-0 mt-0.5 text-[var(--text-muted)]" />
            <div className="flex-1 min-w-0">
              <p className="text-sm font-medium text-[var(--text-primary)] truncate">
                {conversation.title ?? 'New conversation'}
              </p>
              <p className="text-xs text-[var(--text-muted)]">{formatRelativeTime(conversation.updatedAt)}</p>
            </div>
            <button
              onClick={e => {
                e.stopPropagation()
                onDelete(conversation)
              }}
              className="opacity-0 group-hover:opacity-100 text-[var(--text-muted)] hover:text-red-500 transition-opacity flex-shrink-0"
              title="Delete conversation"
            >
              <Trash2 size={14} />
            </button>
          </div>
        ))}
      </div>
    </div>
  )
}
