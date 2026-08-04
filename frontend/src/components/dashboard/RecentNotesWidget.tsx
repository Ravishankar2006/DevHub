import { Link } from 'react-router-dom'
import { StickyNote } from 'lucide-react'
import { useNotes } from '@/hooks/useNotes'
import { formatRelativeTime } from '@/lib/utils'

export default function RecentNotesWidget() {
  const { data: notes, isLoading } = useNotes()

  const recent = [...(notes ?? [])]
    .sort((a, b) => new Date(b.updatedAt).getTime() - new Date(a.updatedAt).getTime())
    .slice(0, 5)

  return (
    <div className="card p-5 flex flex-col gap-3">
      <div className="flex items-center justify-between">
        <h3 className="text-sm font-semibold text-[var(--text-primary)] flex items-center gap-1.5">
          <StickyNote size={15} className="text-purple-500" /> Recent notes
        </h3>
        <Link to="/notes" className="text-xs text-brand-500 hover:underline">View all →</Link>
      </div>

      {isLoading && <p className="text-xs text-[var(--text-muted)]">Loading...</p>}

      {!isLoading && recent.length === 0 && (
        <p className="text-xs text-[var(--text-muted)]">No notes yet.</p>
      )}

      {recent.length > 0 && (
        <div className="flex flex-col gap-2">
          {recent.map(note => (
            <div key={note.id} className="flex items-center justify-between gap-2">
              <div className="min-w-0 flex-1">
                <p className="text-sm text-[var(--text-primary)] truncate">{note.title}</p>
                {(note.folderName || note.tags.length > 0) && (
                  <div className="flex items-center gap-1.5 flex-wrap mt-0.5">
                    {note.folderName && <span className="badge-gray">{note.folderName}</span>}
                    {note.tags.slice(0, 3).map(tag => (
                      <span key={tag} className="badge-blue">{tag}</span>
                    ))}
                  </div>
                )}
              </div>
              <span className="text-xs text-[var(--text-muted)] flex-shrink-0">{formatRelativeTime(note.updatedAt)}</span>
            </div>
          ))}
        </div>
      )}
    </div>
  )
}
