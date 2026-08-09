import { Link } from 'react-router-dom'
import { GitFork, Star } from 'lucide-react'
import { useGitHubAccount } from '@/hooks/useGitHub'

export default function GitHubActivityWidget() {
  const { data: account, isLoading } = useGitHubAccount()

  const topLanguages = account
    ? Object.entries(account.languageBreakdown)
        .sort((a, b) => b[1] - a[1])
        .slice(0, 3)
        .map(([language]) => language)
    : []

  const recentRepos = account?.repos.slice(0, 3) ?? []

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
          <p className="text-sm text-[var(--text-secondary)]">Connect GitHub to see your repositories here.</p>
          <Link to="/settings" className="text-xs text-brand-500 hover:underline">Connect GitHub →</Link>
        </div>
      )}

      {!isLoading && account && account.connected && account.repos.length === 0 && (
        <p className="text-xs text-[var(--text-muted)]">No repositories synced yet.</p>
      )}

      {!isLoading && account && account.connected && account.repos.length > 0 && (
        <div className="flex flex-col gap-3">
          <div className="flex items-center gap-3 text-xs text-[var(--text-secondary)]">
            <span>{account.repos.length} repos</span>
            {topLanguages.length > 0 && (
              <span className="flex items-center gap-1.5">
                {topLanguages.map(lang => <span key={lang} className="badge-gray">{lang}</span>)}
              </span>
            )}
          </div>
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
