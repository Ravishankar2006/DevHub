import { Folder, Plus, Trash2 } from 'lucide-react'
import type { NoteFolder } from '@/lib/types'

interface NoteFolderSidebarProps {
  folders: NoteFolder[]
  totalNoteCount: number
  selectedFolderId: string | null
  onSelect: (folderId: string | null) => void
  onCreateFolder: () => void
  onDeleteFolder: (folder: NoteFolder) => void
}

export default function NoteFolderSidebar({
  folders,
  totalNoteCount,
  selectedFolderId,
  onSelect,
  onCreateFolder,
  onDeleteFolder,
}: NoteFolderSidebarProps) {
  return (
    <div className="card p-3 w-56 flex-shrink-0 h-fit">
      <div className="flex items-center justify-between px-2 mb-2">
        <span className="text-xs font-medium text-[var(--text-muted)] uppercase tracking-wide">Folders</span>
        <button onClick={onCreateFolder} className="btn-ghost p-1 rounded-lg" aria-label="New folder">
          <Plus size={14} />
        </button>
      </div>

      <button
        onClick={() => onSelect(null)}
        className={`w-full flex items-center justify-between px-2 py-1.5 rounded-lg text-sm transition-colors ${
          selectedFolderId === null ? 'bg-brand-500/10 text-brand-500' : 'text-[var(--text-secondary)] hover:bg-[var(--bg-tertiary)]'
        }`}
      >
        <span>All notes</span>
        <span className="text-xs text-[var(--text-muted)]">{totalNoteCount}</span>
      </button>

      {folders.map(folder => (
        <div
          key={folder.id}
          className={`group flex items-center justify-between px-2 py-1.5 rounded-lg text-sm transition-colors cursor-pointer ${
            selectedFolderId === folder.id ? 'bg-brand-500/10 text-brand-500' : 'text-[var(--text-secondary)] hover:bg-[var(--bg-tertiary)]'
          }`}
          onClick={() => onSelect(folder.id)}
        >
          <span className="flex items-center gap-1.5 truncate">
            <Folder size={13} /> {folder.name}
          </span>
          <div className="flex items-center gap-1.5 flex-shrink-0">
            <span className="text-xs text-[var(--text-muted)]">{folder.noteCount}</span>
            <button
              onClick={e => { e.stopPropagation(); onDeleteFolder(folder) }}
              className="opacity-0 group-hover:opacity-100 hover:text-red-500 transition-opacity"
              aria-label="Delete folder"
            >
              <Trash2 size={12} />
            </button>
          </div>
        </div>
      ))}
    </div>
  )
}
