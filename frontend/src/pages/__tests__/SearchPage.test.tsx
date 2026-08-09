import { describe, expect, it } from 'vitest'
import { screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { http, HttpResponse } from 'msw'
import { renderAuthenticated } from '@/test/test-utils'
import { server } from '@/test/setup'
import SearchPage from '@/pages/SearchPage'

const BASE = 'http://localhost:8080/api'

describe('SearchPage', () => {
  it('shows the empty state when no documents are uploaded', async () => {
    renderAuthenticated(<SearchPage />)
    expect(await screen.findByText(/no documents uploaded yet/i)).toBeInTheDocument()
  })

  it('runs a search and renders matching results with score and snippet', async () => {
    server.use(
      http.get(`${BASE}/search`, () =>
        HttpResponse.json([
          { documentId: 'd1', title: 'Rust ownership notes', sourceType: 'NOTE', snippet: 'Ownership rules prevent data races.', score: 0.77 },
        ])
      )
    )

    renderAuthenticated(<SearchPage />)
    const user = userEvent.setup()

    await user.type(screen.getByPlaceholderText(/search your notes and documents/i), 'memory safety')
    await user.click(screen.getByRole('button', { name: /^search$/i }))

    expect(await screen.findByText('Rust ownership notes')).toBeInTheDocument()
    expect(screen.getByText(/ownership rules prevent data races/i)).toBeInTheDocument()
    expect(screen.getByText('77% match')).toBeInTheDocument()
  })

  it('shows a no-results message when the search returns nothing', async () => {
    server.use(http.get(`${BASE}/search`, () => HttpResponse.json([])))

    renderAuthenticated(<SearchPage />)
    const user = userEvent.setup()

    await user.type(screen.getByPlaceholderText(/search your notes and documents/i), 'nothing matches this')
    await user.click(screen.getByRole('button', { name: /^search$/i }))

    await waitFor(() => expect(screen.getByText(/no matching results/i)).toBeInTheDocument())
  })
})
