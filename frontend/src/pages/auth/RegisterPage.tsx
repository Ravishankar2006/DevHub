import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import { Eye, EyeOff, Loader2, Sun, Moon } from 'lucide-react'
import { useAuth } from '@/contexts/AuthContext'
import { useTheme } from '@/contexts/ThemeContext'

const schema = z.object({
  name:            z.string().min(2, 'Name must be at least 2 characters'),
  email:           z.string().email('Enter a valid email'),
  password:        z.string().min(8, 'Password must be at least 8 characters'),
  confirmPassword: z.string(),
}).refine(d => d.password === d.confirmPassword, {
  message: "Passwords don't match",
  path: ['confirmPassword'],
})

type FormData = z.infer<typeof schema>

export default function RegisterPage() {
  const { register: registerUser } = useAuth()
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
      await registerUser(data.name, data.email, data.password)
      navigate('/dashboard')
    } catch (err: unknown) {
      const msg = (err as { response?: { data?: { message?: string } } })?.response?.data?.message
      setServerError(msg ?? 'Registration failed. Please try again.')
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
            Start your developer<br/>journey today
          </p>
          <p className="text-[var(--sidebar-text)] text-base leading-relaxed">
            Connect your work, learning, and career goals in one AI-powered workspace built for developers.
          </p>
        </div>
        <p className="text-[var(--text-muted)] text-xs">Free to start. No credit card required.</p>
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
              <h1 className="text-2xl font-semibold text-[var(--text-primary)]">Create your account</h1>
              <p className="text-[var(--text-secondary)] text-sm mt-1.5">Set up your DevHub workspace</p>
            </div>

            <form onSubmit={handleSubmit(onSubmit)} className="space-y-4" noValidate>
              {serverError && (
                <div className="p-3 rounded-lg bg-red-50 dark:bg-red-900/20 border border-red-200 dark:border-red-800 text-red-600 dark:text-red-400 text-sm animate-fade-in">
                  {serverError}
                </div>
              )}

              <div className="form-group">
                <label htmlFor="register-name" className="label">Full name</label>
                <input
                  id="register-name"
                  type="text"
                  placeholder="Your name"
                  className="input"
                  autoComplete="name"
                  {...register('name')}
                />
                {errors.name && <p className="error-text">{errors.name.message}</p>}
              </div>

              <div className="form-group">
                <label htmlFor="register-email" className="label">Email</label>
                <input
                  id="register-email"
                  type="email"
                  placeholder="you@example.com"
                  className="input"
                  autoComplete="email"
                  {...register('email')}
                />
                {errors.email && <p className="error-text">{errors.email.message}</p>}
              </div>

              <div className="form-group">
                <label htmlFor="register-password" className="label">Password</label>
                <div className="relative">
                  <input
                    id="register-password"
                    type={showPassword ? 'text' : 'password'}
                    placeholder="Min. 8 characters"
                    className="input pr-10"
                    autoComplete="new-password"
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

              <div className="form-group">
                <label htmlFor="register-confirm-password" className="label">Confirm password</label>
                <input
                  id="register-confirm-password"
                  type={showPassword ? 'text' : 'password'}
                  placeholder="Repeat your password"
                  className="input"
                  autoComplete="new-password"
                  {...register('confirmPassword')}
                />
                {errors.confirmPassword && <p className="error-text">{errors.confirmPassword.message}</p>}
              </div>

              <button
                id="register-submit-btn"
                type="submit"
                className="btn-primary w-full mt-2"
                disabled={isSubmitting}
              >
                {isSubmitting ? (
                  <><Loader2 size={15} className="animate-spin" /> Creating account...</>
                ) : (
                  'Create account'
                )}
              </button>
            </form>

            <p className="text-center text-sm text-[var(--text-secondary)] mt-6">
              Already have an account?{' '}
              <Link to="/login" className="text-brand-500 hover:text-brand-600 font-medium transition-colors">
                Sign in
              </Link>
            </p>
          </div>
        </div>
      </div>
    </div>
  )
}
