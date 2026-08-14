import { Link } from 'react-router-dom'
import { Code2, Flame, Trophy } from 'lucide-react'
import { useLeetCodeAccount } from '@/hooks/useLeetCode'

export default function LeetCodeActivityWidget() {
  const { data: account, isLoading } = useLeetCodeAccount()

  const activityDays = account ? Object.entries(account.dailyActivity).slice(-14) : []
  const maxSubmissions = Math.max(1, ...activityDays.map(([, count]) => count))

  return (
    <div className="card p-5 flex flex-col gap-3">
      <div className="flex items-center justify-between">
        <h3 className="text-sm font-semibold text-[var(--text-primary)] flex items-center gap-1.5">
          <Code2 size={15} className="text-brand-500" /> LeetCode activity
        </h3>
        <Link to="/settings" className="text-xs text-brand-500 hover:underline">Settings →</Link>
      </div>

      {isLoading && <p className="text-xs text-[var(--text-muted)]">Loading...</p>}

      {!isLoading && account && !account.connected && (
        <div className="flex flex-col gap-2">
          <p className="text-sm text-[var(--text-secondary)]">Connect LeetCode to see your problem-solving activity here.</p>
          <Link to="/settings" className="text-xs text-brand-500 hover:underline">Connect LeetCode →</Link>
        </div>
      )}

      {!isLoading && account && account.connected && (
        <div className="flex flex-col gap-3">
          <div className="flex items-center gap-3 text-xs">
            <span className="inline-flex items-center gap-1 text-amber-500 font-medium">
              <Flame size={12} /> {account.currentStreak} day{account.currentStreak === 1 ? '' : 's'}
            </span>
            <span className="text-[var(--text-secondary)]">{account.totalSolved} solved</span>
            {account.ranking != null && (
              <span className="inline-flex items-center gap-1 text-[var(--text-secondary)]">
                <Trophy size={12} /> Rank {account.ranking.toLocaleString()}
              </span>
            )}
          </div>

          {activityDays.length > 0 && (
            <div className="flex items-end gap-0.5 h-8">
              {activityDays.map(([date, count]) => (
                <div
                  key={date}
                  title={`${date}: ${count} submission${count === 1 ? '' : 's'}`}
                  className={`flex-1 rounded-sm ${count > 0 ? 'bg-brand-500' : 'bg-[var(--bg-tertiary)]'}`}
                  style={{ height: `${count > 0 ? Math.max(20, (count / maxSubmissions) * 100) : 100}%` }}
                />
              ))}
            </div>
          )}

          <div className="grid grid-cols-3 gap-2">
            <div className="flex flex-col gap-0.5 p-2 rounded-lg bg-[var(--bg-tertiary)]">
              <span className="text-xs text-emerald-500 font-medium">Easy</span>
              <span className="text-sm font-semibold text-[var(--text-primary)]">{account.easySolved}</span>
            </div>
            <div className="flex flex-col gap-0.5 p-2 rounded-lg bg-[var(--bg-tertiary)]">
              <span className="text-xs text-amber-500 font-medium">Medium</span>
              <span className="text-sm font-semibold text-[var(--text-primary)]">{account.mediumSolved}</span>
            </div>
            <div className="flex flex-col gap-0.5 p-2 rounded-lg bg-[var(--bg-tertiary)]">
              <span className="text-xs text-red-500 font-medium">Hard</span>
              <span className="text-sm font-semibold text-[var(--text-primary)]">{account.hardSolved}</span>
            </div>
          </div>
        </div>
      )}
    </div>
  )
}
