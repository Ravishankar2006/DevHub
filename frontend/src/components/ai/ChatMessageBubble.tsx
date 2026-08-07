import { Bot, User as UserIcon } from 'lucide-react'
import { cn } from '@/lib/utils'
import type { AIMessage } from '@/lib/types'

export default function ChatMessageBubble({ message }: { message: AIMessage }) {
  const isUser = message.role === 'USER'

  return (
    <div className={cn('flex gap-3', isUser && 'flex-row-reverse')}>
      <div
        className={cn(
          'w-7 h-7 rounded-full flex items-center justify-center flex-shrink-0 mt-0.5',
          isUser ? 'bg-brand-600 text-white' : 'bg-[var(--bg-tertiary)] text-[var(--text-secondary)]'
        )}
      >
        {isUser ? <UserIcon size={14} /> : <Bot size={14} />}
      </div>
      <div
        className={cn(
          'max-w-[75%] rounded-xl px-4 py-2.5 text-sm whitespace-pre-wrap break-words',
          isUser
            ? 'bg-brand-600 text-white'
            : 'bg-[var(--bg-tertiary)] text-[var(--text-primary)]'
        )}
      >
        {message.content}
      </div>
    </div>
  )
}
