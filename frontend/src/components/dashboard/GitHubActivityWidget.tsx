import { Link } from 'react-router-dom'
import { Flame, GitFork, Star } from 'lucide-react'
import { useGitHubAccount } from '@/hooks/useGitHub'

export default function GitHubActivityWidget() {
  const { data: account, isLoading } = useGitHubAccount()

  const topLanguages = account
    ? Object.entries(account.languageBreakdown)
        .sort((a, b) => b[1] - a[1])
        .slice(0, 3)
        .map(([language]) => language)
    : []

  const recentRepos = account ? account.repos.filter(r => !r.isFork).slice(0, 3) : []

  const activityDays = account ? Object.entries(account.dailyActivity).slice(-14) : []
  const maxCommits = Math.max(1, ...activityDays.map(([, count]) => count))

  return (
    <div className="card p-5 flex flex-col gap-3">
      <div className="flex items-center justify-between">
        <h3 className="text-sm font-semibold text-[var(--text-primary)] flex items-center gap-1.5">
          <GitFork size={15} className="text-brand-500" /> GitHub activity
        </h3>
        <Link to="/settings" className="text-xs text-brand-500 hover:underline">Settings →</Link>
      </div>

      {isLoading && <p className="text-xs text-[var(--text-muted)]">Loading...</p>}

      {!isLoading && account && !account.connected && (
        <div className="flex flex-col gap-2">
          <p className="text-sm text-[var(--text-secondary)]">Connect GitHub to see your coding activity here.</p>
          <Link to="/settings" className="text-xs text-brand-500 hover:underline">Connect GitHub →</Link>
        </div>
      )}

      {!isLoading && account && account.connected && account.repos.length === 0 && (
        <p className="text-xs text-[var(--text-muted)]">No repositories synced yet.</p>
      )}

      {!isLoading && account && account.connected && account.repos.length > 0 && (
        <div className="flex flex-col gap-3">
          <div className="flex items-center gap-3 text-xs">
            <span className="inline-flex items-center gap-1 text-amber-500 font-medium">
              <Flame size={12} /> {account.currentStreak} day{account.currentStreak === 1 ? '' : 's'}
            </span>
            <span className="text-[var(--text-secondary)]">{account.commitsLast30Days} commits · last 30d</span>
          </div>

          {activityDays.length > 0 && (
            <div className="flex items-end gap-0.5 h-8">
              {activityDays.map(([date, count]) => (
                <div
                  key={date}
                  title={`${date}: ${count} commit${count === 1 ? '' : 's'}`}
                  className={`flex-1 rounded-sm ${count > 0 ? 'bg-brand-500' : 'bg-[var(--bg-tertiary)]'}`}
                  style={{ height: `${count > 0 ? Math.max(20, (count / maxCommits) * 100) : 100}%` }}
                />
              ))}
            </div>
          )}

          {topLanguages.length > 0 && (
            <div className="flex items-center gap-1.5">
              {topLanguages.map(lang => <span key={lang} className="badge-gray">{lang}</span>)}
            </div>
          )}

          <div className="flex flex-col gap-2">
            {recentRepos.map(repo => (
              <a
                key={repo.id}
                href={repo.htmlUrl}
                target="_blank"
                rel="noopener noreferrer"
                className="flex items-center justify-between gap-2 group"
              >
                <p className="text-sm text-[var(--text-primary)] group-hover:underline truncate">{repo.name}</p>
                <span className="flex items-center gap-1 text-xs text-[var(--text-muted)] flex-shrink-0">
                  <Star size={11} /> {repo.stars}
                </span>
              </a>
            ))}
          </div>
        </div>
      )}
    </div>
  )
}
