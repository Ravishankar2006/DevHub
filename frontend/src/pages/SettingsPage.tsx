import { useEffect, useState } from 'react'
import { useSearchParams } from 'react-router-dom'
import { CheckCircle2, GitFork, Loader2, RefreshCw, XCircle } from 'lucide-react'
import { useQueryClient } from '@tanstack/react-query'
import {
  useGitHubAccount,
  useConnectGitHub,
  useDisconnectGitHub,
  useTriggerGitHubSync,
} from '@/hooks/useGitHub'
import { useAiJob } from '@/hooks/useAiJobs'
import { formatRelativeTime } from '@/lib/utils'

export default function SettingsPage() {
  const [searchParams, setSearchParams] = useSearchParams()
  const callbackStatus = searchParams.get('github')

  const { data: account, isLoading } = useGitHubAccount()
  const connectGitHub = useConnectGitHub()
  const disconnectGitHub = useDisconnectGitHub()
  const triggerSync = useTriggerGitHubSync()
  const queryClient = useQueryClient()

  const [activeJobId, setActiveJobId] = useState<string | null>(null)
  const { data: activeJob } = useAiJob(activeJobId ?? undefined)

  useEffect(() => {
    if (!activeJob) return
    if (activeJob.status === 'COMPLETED' || activeJob.status === 'FAILED') {
      queryClient.invalidateQueries({ queryKey: ['github'] })
      setActiveJobId(null)
    }
  }, [activeJob, queryClient])

  const dismissBanner = () => {
    searchParams.delete('github')
    setSearchParams(searchParams, { replace: true })
  }

  const handleSync = async () => {
    const job = await triggerSync.mutateAsync()
    setActiveJobId(job.id)
  }

  const handleDisconnect = () => {
    if (window.confirm('Disconnect your GitHub account? Synced repository data will be removed.')) {
      disconnectGitHub.mutate()
    }
  }

  const isSyncing = activeJobId !== null

  return (
    <div className="max-w-2xl mx-auto animate-fade-in">
      <div className="page-header">
        <h2 className="page-title">Settings</h2>
        <p className="page-subtitle">Manage connected accounts and integrations.</p>
      </div>

      {callbackStatus === 'connected' && (
        <div className="flex items-center justify-between gap-2 mb-4 p-3 rounded-lg bg-emerald-100 text-emerald-700 dark:bg-emerald-900/30 dark:text-emerald-400 text-sm">
          <span className="flex items-center gap-1.5"><CheckCircle2 size={14} /> GitHub connected successfully.</span>
          <button onClick={dismissBanner} className="text-xs hover:underline">Dismiss</button>
        </div>
      )}
      {callbackStatus === 'error' && (
        <div className="flex items-center justify-between gap-2 mb-4 p-3 rounded-lg bg-red-100 text-red-700 dark:bg-red-900/30 dark:text-red-400 text-sm">
          <span className="flex items-center gap-1.5"><XCircle size={14} /> Could not connect your GitHub account. Please try again.</span>
          <button onClick={dismissBanner} className="text-xs hover:underline">Dismiss</button>
        </div>
      )}

      <div className="card p-5 flex flex-col gap-3">
        <h3 className="text-sm font-semibold text-[var(--text-primary)] flex items-center gap-1.5">
          <GitFork size={15} className="text-brand-500" /> GitHub
        </h3>

        {isLoading && <p className="text-xs text-[var(--text-muted)]">Loading...</p>}

        {!isLoading && account && !account.connected && (
          <div className="flex flex-col gap-3">
            <p className="text-sm text-[var(--text-secondary)]">
              Connect your GitHub account to import your repositories and show them on your dashboard.
            </p>
            <button
              onClick={() => connectGitHub.mutate()}
              disabled={connectGitHub.isPending}
              className="btn-primary text-sm self-start"
            >
              <GitFork size={14} /> Connect GitHub
            </button>
          </div>
        )}

        {!isLoading && account && account.connected && (
          <div className="flex flex-col gap-3">
            <div className="flex items-center gap-3">
              {account.avatarUrl && (
                <img src={account.avatarUrl} alt={account.username ?? 'GitHub avatar'} className="w-10 h-10 rounded-full" />
              )}
              <div>
                <p className="text-sm font-medium text-[var(--text-primary)]">{account.username}</p>
                <p className="text-xs text-[var(--text-muted)]">
                  {account.repos.length} repositor{account.repos.length === 1 ? 'y' : 'ies'} synced
                  {account.lastSyncedAt && ` · last synced ${formatRelativeTime(account.lastSyncedAt)}`}
                </p>
              </div>
            </div>

            <div className="flex items-center gap-3">
              <button
                onClick={handleSync}
                disabled={isSyncing}
                className="btn-secondary text-sm"
              >
                {isSyncing ? <Loader2 size={14} className="animate-spin" /> : <RefreshCw size={14} />}
                {isSyncing ? 'Syncing...' : 'Sync now'}
              </button>
              <button
                onClick={handleDisconnect}
                disabled={disconnectGitHub.isPending}
                className="text-xs text-red-600 dark:text-red-400 hover:underline"
              >
                Disconnect
              </button>
            </div>
          </div>
        )}
      </div>
    </div>
  )
}
