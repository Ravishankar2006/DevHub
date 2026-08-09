import { describe, expect, it } from 'vitest'
import { screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { renderAuthenticated } from '@/test/test-utils'
import LoginPage from '@/pages/auth/LoginPage'

describe('LoginPage', () => {
  it('shows validation errors for an invalid email and short password', async () => {
    renderAuthenticated(<LoginPage />, { route: '/login' })
    const user = userEvent.setup()

    await user.type(screen.getByLabelText(/email/i), 'not-an-email')
    await user.type(screen.getByLabelText(/^password$/i), 'short')
    await user.click(screen.getByRole('button', { name: /sign in/i }))

    expect(await screen.findByText(/enter a valid email/i)).toBeInTheDocument()
    expect(await screen.findByText(/at least 8 characters/i)).toBeInTheDocument()
  })

  it('submits successfully with valid credentials', async () => {
    renderAuthenticated(<LoginPage />, { route: '/login' })
    const user = userEvent.setup()

    await user.type(screen.getByLabelText(/email/i), 'test@devhub.test')
    await user.type(screen.getByLabelText(/^password$/i), 'TestPass123!')
    await user.click(screen.getByRole('button', { name: /sign in/i }))

    await waitFor(() => expect(localStorage.getItem('devhub-token')).toBe('fake-access-token'))
  })

  it('toggles password visibility', async () => {
    renderAuthenticated(<LoginPage />, { route: '/login' })
    const user = userEvent.setup()

    const passwordInput = screen.getByLabelText(/^password$/i) as HTMLInputElement
    expect(passwordInput.type).toBe('password')

    await user.click(screen.getByRole('button', { name: /toggle password visibility/i }))
    expect(passwordInput.type).toBe('text')
  })
})
