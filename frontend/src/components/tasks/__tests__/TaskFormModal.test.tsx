import { describe, expect, it, vi } from 'vitest'
import { screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { renderAuthenticated } from '@/test/test-utils'
import TaskFormModal from '@/components/tasks/TaskFormModal'

describe('TaskFormModal', () => {
  it('blocks submission when the title is empty', async () => {
    const onClose = vi.fn()
    renderAuthenticated(<TaskFormModal onClose={onClose} />)
    const user = userEvent.setup()

    await user.click(screen.getByRole('button', { name: /create task/i }))

    expect(await screen.findByText(/title is required/i)).toBeInTheDocument()
    expect(onClose).not.toHaveBeenCalled()
  })

  it('submits and closes when a title is provided', async () => {
    const onClose = vi.fn()
    renderAuthenticated(<TaskFormModal onClose={onClose} />)
    const user = userEvent.setup()

    await user.type(screen.getByLabelText(/title/i), 'Write more tests')
    await user.click(screen.getByRole('button', { name: /create task/i }))

    await waitFor(() => expect(onClose).toHaveBeenCalled())
  })

  it('calls onClose when Cancel is clicked without submitting', async () => {
    const onClose = vi.fn()
    renderAuthenticated(<TaskFormModal onClose={onClose} />)
    const user = userEvent.setup()

    await user.click(screen.getByRole('button', { name: /cancel/i }))

    expect(onClose).toHaveBeenCalledTimes(1)
  })
})
