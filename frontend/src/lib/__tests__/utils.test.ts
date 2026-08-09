import { describe, expect, it } from 'vitest'
import { formatDate, formatFileSize, formatRelativeTime, getInitials } from '../utils'

describe('formatDate', () => {
  it('formats an ISO date string as a short month/day/year', () => {
    expect(formatDate('2026-03-05T00:00:00Z')).toMatch(/Mar 5, 2026|Mar 4, 2026/)
  })
})

describe('formatRelativeTime', () => {
  it('returns "just now" for timestamps within the last minute', () => {
    expect(formatRelativeTime(new Date(Date.now() - 10_000).toISOString())).toBe('just now')
  })

  it('formats minutes for timestamps within the last hour', () => {
    const result = formatRelativeTime(new Date(Date.now() - 5 * 60_000).toISOString())
    expect(result).toContain('minute')
  })

  it('formats hours for timestamps within the last day', () => {
    const result = formatRelativeTime(new Date(Date.now() - 3 * 3600_000).toISOString())
    expect(result).toContain('hour')
  })

  it('formats days for timestamps beyond a day', () => {
    const result = formatRelativeTime(new Date(Date.now() - 3 * 86400_000).toISOString())
    expect(result).toContain('day')
  })

  it('formats future timestamps as "in N ..." rather than "ago"', () => {
    const result = formatRelativeTime(new Date(Date.now() + 2 * 3600_000).toISOString())
    expect(result).toMatch(/^in /)
  })
})

describe('formatFileSize', () => {
  it('formats bytes under 1KB as bytes', () => {
    expect(formatFileSize(500)).toBe('500 B')
  })

  it('formats kilobytes with one decimal place', () => {
    expect(formatFileSize(2048)).toBe('2.0 KB')
  })

  it('formats megabytes with one decimal place', () => {
    expect(formatFileSize(5 * 1024 * 1024)).toBe('5.0 MB')
  })
})

describe('getInitials', () => {
  it('returns the first letter of the first two words, uppercased', () => {
    expect(getInitials('ravi shankar')).toBe('RS')
  })

  it('handles a single-word name', () => {
    expect(getInitials('ravi')).toBe('R')
  })

  it('ignores words beyond the first two', () => {
    expect(getInitials('ravi kumar shankar')).toBe('RK')
  })
})
