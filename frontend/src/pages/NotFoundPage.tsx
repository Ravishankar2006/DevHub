import { Link } from 'react-router-dom'
import { Home } from 'lucide-react'

export default function NotFoundPage() {
  return (
    <div className="min-h-screen flex items-center justify-center bg-[var(--bg-primary)]">
      <div className="text-center">
        <p className="text-8xl font-bold text-[var(--text-muted)] mb-4">404</p>
        <h1 className="text-2xl font-semibold text-[var(--text-primary)] mb-2">Page not found</h1>
        <p className="text-[var(--text-secondary)] text-sm mb-6">
          The page you're looking for doesn't exist or has been moved.
        </p>
        <Link to="/dashboard" className="btn-primary">
          <Home size={15} />
          Back to Dashboard
        </Link>
      </div>
    </div>
  )
}
