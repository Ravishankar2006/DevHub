import { useRef, useState } from 'react'
import { AlertCircle, FileText, Loader2, Search, StickyNote, Trash2, Upload } from 'lucide-react'
import { useDocuments, useUploadDocument, useDeleteDocument } from '@/hooks/useDocuments'
import { useSearch } from '@/hooks/useSearch'
import type { DocumentStatus } from '@/lib/types'

const statusBadge: Record<DocumentStatus, string> = {
  PENDING: 'badge-blue',
  INDEXED: 'badge-green',
  FAILED: 'badge bg-red-100 text-red-700 dark:bg-red-900/30 dark:text-red-400',
}

const statusLabel: Record<DocumentStatus, string> = {
  PENDING: 'Indexing...',
  INDEXED: 'Indexed',
  FAILED: 'Failed',
}

export default function SearchPage() {
  const { data: documents, isLoading: documentsLoading } = useDocuments()
  const uploadDocument = useUploadDocument()
  const deleteDocument = useDeleteDocument()
  const fileInputRef = useRef<HTMLInputElement>(null)

  const [query, setQuery] = useState('')
  const search = useSearch()

  const handleFileChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0]
    if (file) {
      uploadDocument.mutate({ file })
    }
    if (fileInputRef.current) fileInputRef.current.value = ''
  }

  const handleDelete = (id: string, title: string) => {
    if (window.confirm(`Delete "${title}"? This removes it from search.`)) {
      deleteDocument.mutate(id)
    }
  }

  const handleSearch = (e: React.FormEvent) => {
    e.preventDefault()
    if (query.trim()) {
      search.mutate(query.trim())
    }
  }

  return (
    <div className="max-w-3xl mx-auto animate-fade-in">
      <div className="page-header">
        <h2 className="page-title">Search</h2>
        <p className="page-subtitle">Semantic search across your notes and uploaded documents.</p>
      </div>

      <div className="card p-5 flex flex-col gap-3 mb-5">
        <h3 className="text-sm font-semibold text-[var(--text-primary)] flex items-center gap-1.5">
          <FileText size={15} className="text-brand-500" /> Your documents
        </h3>
        <p className="text-xs text-[var(--text-secondary)]">
          Notes are indexed automatically. Upload .txt, .md, or PDF files to make them searchable too.
        </p>

        <label
          htmlFor="document-file"
          className="input flex items-center gap-2 cursor-pointer text-[var(--text-secondary)] w-fit"
        >
          {uploadDocument.isPending ? <Loader2 size={15} className="animate-spin" /> : <Upload size={15} />}
          {uploadDocument.isPending ? 'Uploading...' : 'Upload a document'}
        </label>
        <input
          id="document-file"
          ref={fileInputRef}
          type="file"
          accept=".txt,.md,application/pdf,.pdf"
          className="hidden"
          disabled={uploadDocument.isPending}
          onChange={handleFileChange}
        />

        {documentsLoading && <p className="text-xs text-[var(--text-muted)]">Loading...</p>}
        {!documentsLoading && documents && documents.length === 0 && (
          <p className="text-xs text-[var(--text-muted)]">No documents uploaded yet.</p>
        )}
        {documents && documents.length > 0 && (
          <div className="flex flex-col gap-2 mt-1">
            {documents.map(doc => (
              <div key={doc.id} className="flex items-center justify-between gap-2">
                <div className="flex items-center gap-2 min-w-0">
                  <span className={statusBadge[doc.status]}>{statusLabel[doc.status]}</span>
                  <p className="text-sm text-[var(--text-primary)] truncate">{doc.title}</p>
                </div>
                <div className="flex items-center gap-2 flex-shrink-0">
                  {doc.status === 'FAILED' && doc.errorMessage && (
                    <span title={doc.errorMessage}><AlertCircle size={13} className="text-red-500" /></span>
                  )}
                  <button
                    onClick={() => handleDelete(doc.id, doc.title)}
                    className="text-[var(--text-muted)] hover:text-red-500"
                  >
                    <Trash2 size={13} />
                  </button>
                </div>
              </div>
            ))}
          </div>
        )}
      </div>

      <div className="card p-5 flex flex-col gap-3">
        <h3 className="text-sm font-semibold text-[var(--text-primary)] flex items-center gap-1.5">
          <Search size={15} className="text-brand-500" /> Search
        </h3>
        <form onSubmit={handleSearch} className="flex gap-2">
          <input
            className="input flex-1"
            placeholder="Search your notes and documents..."
            value={query}
            onChange={e => setQuery(e.target.value)}
          />
          <button type="submit" className="btn-primary text-sm" disabled={search.isPending || !query.trim()}>
            {search.isPending ? <Loader2 size={14} className="animate-spin" /> : <Search size={14} />}
            Search
          </button>
        </form>

        {search.isPending && <p className="text-xs text-[var(--text-muted)]">Searching...</p>}

        {search.isSuccess && search.data.length === 0 && (
          <p className="text-xs text-[var(--text-muted)]">No matching results.</p>
        )}

        {search.isSuccess && search.data.length > 0 && (
          <div className="flex flex-col gap-3 mt-1">
            {search.data.map((result, i) => (
              <div key={i} className="border-t border-[var(--border)] pt-3 first:border-t-0 first:pt-0">
                <div className="flex items-center justify-between gap-2 mb-1">
                  <div className="flex items-center gap-1.5 min-w-0">
                    {result.sourceType === 'NOTE'
                      ? <StickyNote size={13} className="text-purple-500 flex-shrink-0" />
                      : <FileText size={13} className="text-brand-500 flex-shrink-0" />}
                    <p className="text-sm font-medium text-[var(--text-primary)] truncate">{result.title}</p>
                  </div>
                  <span className="text-xs text-[var(--text-muted)] flex-shrink-0">
                    {Math.round(result.score * 100)}% match
                  </span>
                </div>
                <p className="text-xs text-[var(--text-secondary)]">{result.snippet}</p>
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  )
}
