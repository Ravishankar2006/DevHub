import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import { Eye, EyeOff, Loader2 } from 'lucide-react'
import { useAuth } from '@/contexts/AuthContext'
import { useTheme } from '@/contexts/ThemeContext'
import { Sun, Moon } from 'lucide-react'

const schema = z.object({
  email:    z.string().email('Enter a valid email'),
  password: z.string().min(8, 'Password must be at least 8 characters'),
})

type FormData = z.infer<typeof schema>

export default function LoginPage() {
  const { login } = useAuth()
  const { theme, toggleTheme } = useTheme()
  const navigate = useNavigate()
  const [showPassword, setShowPassword] = useState(false)
  const [serverError, setServerError] = useState('')

  const { register, handleSubmit, formState: { errors, isSubmitting } } = useForm<FormData>({
    resolver: zodResolver(schema),
  })

  const onSubmit = async (data: FormData) => {
    setServerError('')
    try {
      await login(data.email, data.password)
      navigate('/dashboard')
    } catch (err: unknown) {
      const msg = (err as { response?: { data?: { message?: string } } })?.response?.data?.message
      setServerError(msg ?? 'Invalid credentials. Please try again.')
    }
  }

  return (
    <div className="min-h-screen flex bg-[var(--bg-primary)]">
      {/* Left decorative panel */}
      <div className="hidden lg:flex lg:w-1/2 bg-[var(--sidebar-bg)] flex-col items-start justify-between p-12">
        <div className="flex items-center gap-2.5">
          <div className="w-8 h-8 rounded-lg bg-brand-500 flex items-center justify-center">
            <span className="text-white font-bold text-sm">D</span>
          </div>
          <span className="text-white font-semibold">DevHub</span>
        </div>
        <div className="max-w-sm">
          <p className="text-3xl font-semibold text-white leading-snug mb-4">
            Your AI Developer<br/>Operating System
          </p>
          <p className="text-[var(--sidebar-text)] text-base leading-relaxed">
            Manage projects, track learning, plan your career, and get AI-powered guidance — all in one place.
          </p>
          <div className="mt-8 grid grid-cols-2 gap-3">
            {['Project Manager', 'Goal Tracking', 'AI Assistant', 'Job Tracker'].map(f => (
              <div key={f} className="flex items-center gap-2 text-sm text-[var(--sidebar-text)]">
                <div className="w-1.5 h-1.5 rounded-full bg-brand-400 flex-shrink-0" />
                {f}
              </div>
            ))}
          </div>
        </div>
        <p className="text-[var(--text-muted)] text-xs">Built for solo developers. Designed to scale.</p>
      </div>

      {/* Right auth panel */}
      <div className="flex-1 flex flex-col">
        <div className="flex justify-end p-4">
          <button
            onClick={toggleTheme}
            className="btn-ghost p-2 rounded-lg text-[var(--text-secondary)]"
            aria-label="Toggle theme"
          >
            {theme === 'dark' ? <Sun size={17} /> : <Moon size={17} />}
          </button>
        </div>

        <div className="flex-1 flex items-center justify-center px-6 py-12">
          <div className="w-full max-w-sm">
            <div className="mb-8">
              <h1 className="text-2xl font-semibold text-[var(--text-primary)]">Welcome back</h1>
              <p className="text-[var(--text-secondary)] text-sm mt-1.5">Sign in to your DevHub account</p>
            </div>

            <form onSubmit={handleSubmit(onSubmit)} className="space-y-4" noValidate>
              {serverError && (
                <div className="p-3 rounded-lg bg-red-50 dark:bg-red-900/20 border border-red-200 dark:border-red-800 text-red-600 dark:text-red-400 text-sm animate-fade-in">
                  {serverError}
                </div>
              )}

              <div className="form-group">
                <label htmlFor="login-email" className="label">Email</label>
                <input
                  id="login-email"
                  type="email"
                  placeholder="you@example.com"
                  className="input"
                  autoComplete="email"
                  {...register('email')}
                />
                {errors.email && <p className="error-text">{errors.email.message}</p>}
              </div>

              <div className="form-group">
                <label htmlFor="login-password" className="label">Password</label>
                <div className="relative">
                  <input
                    id="login-password"
                    type={showPassword ? 'text' : 'password'}
                    placeholder="••••••••"
                    className="input pr-10"
                    autoComplete="current-password"
                    {...register('password')}
                  />
                  <button
                    type="button"
                    onClick={() => setShowPassword(p => !p)}
                    className="absolute right-3 top-1/2 -translate-y-1/2 text-[var(--text-muted)] hover:text-[var(--text-secondary)] transition-colors"
                    aria-label="Toggle password visibility"
                  >
                    {showPassword ? <EyeOff size={15} /> : <Eye size={15} />}
                  </button>
                </div>
                {errors.password && <p className="error-text">{errors.password.message}</p>}
              </div>

              <button
                id="login-submit-btn"
                type="submit"
                className="btn-primary w-full mt-2"
                disabled={isSubmitting}
              >
                {isSubmitting ? (
                  <><Loader2 size={15} className="animate-spin" /> Signing in...</>
                ) : (
                  'Sign in'
                )}
              </button>
            </form>

            <p className="text-center text-sm text-[var(--text-secondary)] mt-6">
              Don't have an account?{' '}
              <Link to="/register" className="text-brand-500 hover:text-brand-600 font-medium transition-colors">
                Create one
              </Link>
            </p>
          </div>
        </div>
      </div>
    </div>
  )
}
