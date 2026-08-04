import { useState } from 'react'
import { Plus, Search, StickyNote } from 'lucide-react'
import NoteFolderSidebar from '@/components/notes/NoteFolderSidebar'
import NoteListItem from '@/components/notes/NoteListItem'
import NoteFormModal from '@/components/notes/NoteFormModal'
import { useDeleteNote, useNotes } from '@/hooks/useNotes'
import { useCreateNoteFolder, useDeleteNoteFolder, useNoteFolders } from '@/hooks/useNoteFolders'
import type { Note, NoteFolder } from '@/lib/types'

export default function NotesPage() {
  const [selectedFolderId, setSelectedFolderId] = useState<string | null>(null)
  const [search, setSearch] = useState('')

  const { data: folders } = useNoteFolders()
  const { data: notes, isLoading } = useNotes({
    folderId: selectedFolderId ?? undefined,
    q: search || undefined,
  })
  const { data: allNotes } = useNotes()

  const createFolder = useCreateNoteFolder()
  const deleteFolder = useDeleteNoteFolder()
  const deleteNote = useDeleteNote()

  const [showNoteForm, setShowNoteForm] = useState(false)
  const [editingNote, setEditingNote] = useState<Note | undefined>(undefined)

  const openCreateNote = () => { setEditingNote(undefined); setShowNoteForm(true) }
  const openEditNote = (note: Note) => { setEditingNote(note); setShowNoteForm(true) }
  const closeNoteForm = () => { setShowNoteForm(false); setEditingNote(undefined) }

  const handleCreateFolder = () => {
    const name = window.prompt('Folder name')
    if (name && name.trim()) {
      createFolder.mutate({ name: name.trim() })
    }
  }

  const handleDeleteFolder = (folder: NoteFolder) => {
    if (window.confirm(`Delete folder "${folder.name}"? Its notes will be unfiled, not deleted.`)) {
      if (selectedFolderId === folder.id) setSelectedFolderId(null)
      deleteFolder.mutate(folder.id)
    }
  }

  const handleDeleteNote = (note: Note) => {
    if (window.confirm(`Delete note "${note.title}"?`)) {
      deleteNote.mutate(note.id)
    }
  }

  return (
    <div className="max-w-6xl mx-auto animate-fade-in">
      <div className="page-header flex items-center justify-between gap-4 flex-wrap">
        <div>
          <h2 className="page-title">Notes</h2>
          <p className="page-subtitle">Markdown notes, organized by folder and tag.</p>
        </div>
        <button onClick={openCreateNote} className="btn-primary text-sm">
          <Plus size={16} /> New Note
        </button>
      </div>

      <div className="flex gap-5 items-start">
        <NoteFolderSidebar
          folders={folders ?? []}
          totalNoteCount={allNotes?.length ?? 0}
          selectedFolderId={selectedFolderId}
          onSelect={setSelectedFolderId}
          onCreateFolder={handleCreateFolder}
          onDeleteFolder={handleDeleteFolder}
        />

        <div className="flex-1 min-w-0">
          <div className="relative mb-4">
            <Search size={15} className="absolute left-3 top-1/2 -translate-y-1/2 text-[var(--text-muted)]" />
            <input
              value={search}
              onChange={e => setSearch(e.target.value)}
              placeholder="Search notes by title, tag, or content..."
              className="input pl-9"
            />
          </div>

          {isLoading && <p className="text-sm text-[var(--text-muted)]">Loading notes...</p>}

          {!isLoading && notes?.length === 0 && (
            <div className="card p-10 flex flex-col items-center text-center">
              <div className="w-12 h-12 rounded-xl bg-brand-500/10 flex items-center justify-center mb-4">
                <StickyNote size={22} className="text-brand-500" />
              </div>
              <h3 className="text-lg font-semibold text-[var(--text-primary)] mb-2">No notes found</h3>
              <p className="text-sm text-[var(--text-secondary)] mb-6">
                {search ? 'Try a different search term.' : 'Create your first note to get started.'}
              </p>
              <button onClick={openCreateNote} className="btn-primary text-sm">
                <Plus size={16} /> New Note
              </button>
            </div>
          )}

          {!isLoading && notes && notes.length > 0 && (
            <div className="space-y-3">
              {notes.map(note => (
                <NoteListItem
                  key={note.id}
                  note={note}
                  onClick={() => openEditNote(note)}
                  onDelete={() => handleDeleteNote(note)}
                />
              ))}
            </div>
          )}
        </div>
      </div>

      {showNoteForm && (
        <NoteFormModal note={editingNote} defaultFolderId={selectedFolderId} onClose={closeNoteForm} />
      )}
    </div>
  )
}
