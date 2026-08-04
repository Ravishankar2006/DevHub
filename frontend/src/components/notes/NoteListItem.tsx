import { Trash2 } from 'lucide-react'
import type { Note } from '@/lib/types'

interface NoteListItemProps {
  note: Note
  onClick: () => void
  onDelete: () => void
}

export default function NoteListItem({ note, onClick, onDelete }: NoteListItemProps) {
  return (
    <div className="card p-4 flex items-start justify-between gap-3 cursor-pointer hover:border-brand-500/40" onClick={onClick}>
      <div className="min-w-0 flex-1">
        <p className="text-sm font-medium text-[var(--text-primary)] truncate">{note.title}</p>
        {note.content && (
          <p className="text-xs text-[var(--text-secondary)] line-clamp-2 mt-1">{note.content}</p>
        )}
        <div className="flex items-center gap-1.5 flex-wrap mt-2">
          {note.folderName && <span className="badge-gray">{note.folderName}</span>}
          {note.tags.map(tag => (
            <span key={tag} className="badge-blue">{tag}</span>
          ))}
        </div>
      </div>
      <button
        onClick={e => { e.stopPropagation(); onDelete() }}
        className="btn-ghost p-1.5 rounded-lg flex-shrink-0 text-red-500"
        aria-label="Delete note"
      >
        <Trash2 size={13} />
      </button>
    </div>
  )
}
