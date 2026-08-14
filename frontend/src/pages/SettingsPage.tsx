import { useEffect, useState, type ReactNode } from 'react'
import { useSearchParams } from 'react-router-dom'
import { CheckCircle2, Code2, Flame, GitFork, Loader2, RefreshCw, Star, Trophy, XCircle } from 'lucide-react'
import { useQueryClient } from '@tanstack/react-query'
import {
  useGitHubAccount,
  useConnectGitHub,
  useDisconnectGitHub,
  useTriggerGitHubSync,
} from '@/hooks/useGitHub'
import {
  useLeetCodeAccount,
  useConnectLeetCode,
  useDisconnectLeetCode,
  useTriggerLeetCodeSync,
} from '@/hooks/useLeetCode'
import { useAiJob } from '@/hooks/useAiJobs'
import { formatRelativeTime } from '@/lib/utils'

function StatBlock({ icon, label, value }: { icon: ReactNode; label: string; value: string | number }) {
  return (
    <div className="flex flex-col gap-0.5 p-2.5 rounded-lg bg-[var(--bg-tertiary)]">
      <span className="flex items-center gap-1 text-xs text-[var(--text-muted)]">{icon} {label}</span>
      <span className="text-sm font-semibold text-[var(--text-primary)]">{value}</span>
    </div>
  )
}

export default function SettingsPage() {
  const [searchParams, setSearchParams] = useSearchParams()
  const callbackStatus = searchParams.get('github')

  const { data: account, isLoading } = useGitHubAccount()
  const connectGitHub = useConnectGitHub()
  const disconnectGitHub = useDisconnectGitHub()
  const triggerSync = useTriggerGitHubSync()
  const queryClient = useQueryClient()

  const { data: leetCodeAccount, isLoading: isLeetCodeLoading } = useLeetCodeAccount()
  const connectLeetCode = useConnectLeetCode()
  const disconnectLeetCode = useDisconnectLeetCode()
  const triggerLeetCodeSync = useTriggerLeetCodeSync()
  const [leetCodeUsername, setLeetCodeUsername] = useState('')

  const [activeJobId, setActiveJobId] = useState<string | null>(null)
  const { data: activeJob } = useAiJob(activeJobId ?? undefined)

  const [activeLeetCodeJobId, setActiveLeetCodeJobId] = useState<string | null>(null)
  const { data: activeLeetCodeJob } = useAiJob(activeLeetCodeJobId ?? undefined)

  useEffect(() => {
    if (!activeJob) return
    if (activeJob.status === 'COMPLETED' || activeJob.status === 'FAILED') {
      queryClient.invalidateQueries({ queryKey: ['github'] })
      setActiveJobId(null)
    }
  }, [activeJob, queryClient])

  useEffect(() => {
    if (!activeLeetCodeJob) return
    if (activeLeetCodeJob.status === 'COMPLETED' || activeLeetCodeJob.status === 'FAILED') {
      queryClient.invalidateQueries({ queryKey: ['leetcode'] })
      setActiveLeetCodeJobId(null)
    }
  }, [activeLeetCodeJob, queryClient])

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

  const handleLeetCodeConnect = async () => {
    if (!leetCodeUsername.trim()) return
    await connectLeetCode.mutateAsync(leetCodeUsername.trim())
    setLeetCodeUsername('')
  }

  const handleLeetCodeSync = async () => {
    const job = await triggerLeetCodeSync.mutateAsync()
    setActiveLeetCodeJobId(job.id)
  }

  const handleLeetCodeDisconnect = () => {
    if (window.confirm('Disconnect your LeetCode account? Synced stats will be removed.')) {
      disconnectLeetCode.mutate()
    }
  }

  const isLeetCodeSyncing = activeLeetCodeJobId !== null

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

            {account.repos.length > 0 && (
              <div className="grid grid-cols-2 sm:grid-cols-4 gap-2">
                <StatBlock icon={<Flame size={13} className="text-amber-500" />} label="Current streak" value={`${account.currentStreak}d`} />
                <StatBlock icon={<Flame size={13} className="text-[var(--text-muted)]" />} label="Longest streak" value={`${account.longestStreak}d`} />
                <StatBlock icon={<Star size={13} className="text-amber-500" />} label="Total stars" value={account.totalStars} />
                <StatBlock icon={<GitFork size={13} className="text-brand-500" />} label="Original repos" value={account.originalRepoCount} />
              </div>
            )}

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

      <div className="card p-5 flex flex-col gap-3 mt-5">
        <h3 className="text-sm font-semibold text-[var(--text-primary)] flex items-center gap-1.5">
          <Code2 size={15} className="text-brand-500" /> LeetCode
        </h3>

        {isLeetCodeLoading && <p className="text-xs text-[var(--text-muted)]">Loading...</p>}

        {!isLeetCodeLoading && leetCodeAccount && !leetCodeAccount.connected && (
          <div className="flex flex-col gap-3">
            <p className="text-sm text-[var(--text-secondary)]">
              Connect your LeetCode account to track your problem-solving stats and streak.
            </p>
            <div className="flex items-center gap-2">
              <input
                type="text"
                value={leetCodeUsername}
                onChange={e => setLeetCodeUsername(e.target.value)}
                placeholder="LeetCode username"
                className="input text-sm flex-1"
              />
              <button
                onClick={handleLeetCodeConnect}
                disabled={connectLeetCode.isPending || !leetCodeUsername.trim()}
                className="btn-primary text-sm"
              >
                <Code2 size={14} /> Connect
              </button>
            </div>
            {connectLeetCode.isError && (
              <p className="text-xs text-red-600 dark:text-red-400">
                Could not find that LeetCode username. Please check it and try again.
              </p>
            )}
          </div>
        )}

        {!isLeetCodeLoading && leetCodeAccount && leetCodeAccount.connected && (
          <div className="flex flex-col gap-3">
            <div>
              <p className="text-sm font-medium text-[var(--text-primary)]">{leetCodeAccount.username}</p>
              <p className="text-xs text-[var(--text-muted)]">
                {leetCodeAccount.totalSolved} solved
                {leetCodeAccount.lastSyncedAt && ` · last synced ${formatRelativeTime(leetCodeAccount.lastSyncedAt)}`}
              </p>
            </div>

            <div className="grid grid-cols-2 sm:grid-cols-4 gap-2">
              <StatBlock icon={<Flame size={13} className="text-amber-500" />} label="Current streak" value={`${leetCodeAccount.currentStreak}d`} />
              <StatBlock icon={<Flame size={13} className="text-[var(--text-muted)]" />} label="Longest streak" value={`${leetCodeAccount.longestStreak}d`} />
              <StatBlock icon={<Star size={13} className="text-amber-500" />} label="Total solved" value={leetCodeAccount.totalSolved} />
              {leetCodeAccount.ranking != null && (
                <StatBlock icon={<Trophy size={13} className="text-brand-500" />} label="Ranking" value={leetCodeAccount.ranking.toLocaleString()} />
              )}
            </div>

            <div className="flex items-center gap-3">
              <button
                onClick={handleLeetCodeSync}
                disabled={isLeetCodeSyncing}
                className="btn-secondary text-sm"
              >
                {isLeetCodeSyncing ? <Loader2 size={14} className="animate-spin" /> : <RefreshCw size={14} />}
                {isLeetCodeSyncing ? 'Syncing...' : 'Sync now'}
              </button>
              <button
                onClick={handleLeetCodeDisconnect}
                disabled={disconnectLeetCode.isPending}
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
