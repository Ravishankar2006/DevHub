import { useEffect, useRef, useState } from 'react'
import { Send, Loader2 } from 'lucide-react'
import ConversationList from '@/components/ai/ConversationList'
import ChatMessageBubble from '@/components/ai/ChatMessageBubble'
import ProposalCard from '@/components/ai/ProposalCard'
import {
  useAIConversations,
  useAIConversation,
  useCreateConversation,
  useSendMessage,
  useDeleteConversation,
} from '@/hooks/useAI'
import type { AIConversationSummary } from '@/lib/types'

export default function AIChatPage() {
  const [selectedId, setSelectedId] = useState<string | null>(null)
  const [input, setInput] = useState('')
  const [error, setError] = useState<string | null>(null)
  const messagesEndRef = useRef<HTMLDivElement>(null)

  const { data: conversations = [] } = useAIConversations()
  const { data: conversation, isLoading: isLoadingConversation } = useAIConversation(selectedId ?? undefined)
  const createConversation = useCreateConversation()
  const sendMessage = useSendMessage(selectedId ?? '')
  const deleteConversation = useDeleteConversation()

  useEffect(() => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' })
  }, [conversation?.messages.length])

  const handleNew = async () => {
    setError(null)
    const created = await createConversation.mutateAsync()
    setSelectedId(created.id)
  }

  const handleDelete = async (target: AIConversationSummary) => {
    if (!window.confirm(`Delete "${target.title ?? 'New conversation'}"?`)) return
    await deleteConversation.mutateAsync(target.id)
    if (selectedId === target.id) setSelectedId(null)
  }

  const handleSend = async () => {
    const content = input.trim()
    if (!content || !selectedId) return
    setInput('')
    setError(null)
    try {
      await sendMessage.mutateAsync(content)
    } catch {
      setError('The AI assistant is temporarily unavailable. Please try again.')
    }
  }

  const handleKeyDown = (e: React.KeyboardEvent<HTMLTextAreaElement>) => {
    if (e.key === 'Enter' && !e.shiftKey) {
      e.preventDefault()
      handleSend()
    }
  }

  return (
    <div className="max-w-6xl mx-auto animate-fade-in h-[calc(100vh-8rem)] flex flex-col">
      <div className="page-header">
        <h2 className="page-title">AI Assistant</h2>
        <p className="page-subtitle">Ask questions, brainstorm, or get help planning your next steps.</p>
      </div>

      <div className="flex-1 grid grid-cols-[280px_1fr] gap-4 min-h-0">
        <ConversationList
          conversations={conversations}
          selectedId={selectedId}
          onSelect={setSelectedId}
          onNew={handleNew}
          onDelete={handleDelete}
        />

        <div className="card flex flex-col min-h-0">
          {!selectedId && (
            <div className="flex-1 flex items-center justify-center text-center px-6">
              <div>
                <p className="text-[var(--text-secondary)]">Select a conversation or start a new chat.</p>
              </div>
            </div>
          )}

          {selectedId && (
            <>
              <div className="flex-1 overflow-y-auto p-4 space-y-4">
                {isLoadingConversation && (
                  <div className="flex justify-center py-6">
                    <Loader2 size={18} className="animate-spin text-[var(--text-muted)]" />
                  </div>
                )}
                {conversation?.messages.map(message => (
                  <ChatMessageBubble key={message.id} message={message} />
                ))}
                {conversation?.pendingProposals.map(proposal => (
                  <ProposalCard key={proposal.id} proposal={proposal} conversationId={selectedId!} />
                ))}
                {sendMessage.isPending && (
                  <div className="flex gap-3">
                    <div className="w-7 h-7 rounded-full bg-[var(--bg-tertiary)] flex items-center justify-center flex-shrink-0">
                      <Loader2 size={14} className="animate-spin text-[var(--text-secondary)]" />
                    </div>
                    <div className="bg-[var(--bg-tertiary)] rounded-xl px-4 py-2.5 text-sm text-[var(--text-muted)]">
                      Thinking...
                    </div>
                  </div>
                )}
                <div ref={messagesEndRef} />
              </div>

              {error && <p className="error-text px-4">{error}</p>}

              <div className="p-3 border-t border-[var(--border)] flex gap-2 items-end">
                <textarea
                  value={input}
                  onChange={e => setInput(e.target.value)}
                  onKeyDown={handleKeyDown}
                  placeholder="Type a message..."
                  rows={1}
                  className="input resize-none"
                  disabled={sendMessage.isPending}
                />
                <button
                  onClick={handleSend}
                  disabled={!input.trim() || sendMessage.isPending}
                  className="btn-primary flex-shrink-0"
                >
                  <Send size={16} />
                </button>
              </div>
            </>
          )}
        </div>
      </div>
    </div>
  )
}
