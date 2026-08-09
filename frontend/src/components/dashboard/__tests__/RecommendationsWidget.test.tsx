import { describe, expect, it } from 'vitest'
import { buildRecommendations } from '../RecommendationsWidget'
import type { Goal, Habit, JobApplication, Resume, Task } from '@/lib/types'

function task(overrides: Partial<Task> = {}): Task {
  return {
    id: 't1', projectId: null, projectName: null, milestoneId: null, milestoneTitle: null,
    title: 'Task', description: null, status: 'TODO', priority: 'MEDIUM', dueDate: null,
    position: 0, createdAt: '2026-01-01T00:00:00Z', updatedAt: '2026-01-01T00:00:00Z',
    ...overrides,
  }
}

function goal(overrides: Partial<Goal> = {}): Goal {
  return {
    id: 'g1', title: 'Goal', description: null, type: 'DAILY', status: 'ACTIVE', targetDate: null,
    progressPercent: 0, projectId: null, projectName: null,
    createdAt: '2026-01-01T00:00:00Z', updatedAt: '2026-01-01T00:00:00Z',
    ...overrides,
  }
}

function habit(overrides: Partial<Habit> = {}): Habit {
  return {
    id: 'h1', goalId: null, goalTitle: null, title: 'Habit', frequency: 'DAILY',
    currentStreak: 0, longestStreak: 0, checkedInToday: false,
    createdAt: '2026-01-01T00:00:00Z', updatedAt: '2026-01-01T00:00:00Z',
    ...overrides,
  }
}

function resume(overrides: Partial<Resume> = {}): Resume {
  return {
    id: 'r1', name: 'Resume', label: null, fileName: 'resume.pdf', fileSizeBytes: 1000,
    notes: null, downloadUrl: '/resumes/r1/download', reviewScore: null, reviewSummary: null,
    reviewIssues: [], reviewSuggestions: [], reviewedAt: null,
    createdAt: '2026-01-01T00:00:00Z', updatedAt: '2026-01-01T00:00:00Z',
    ...overrides,
  }
}

function application(overrides: Partial<JobApplication> = {}): JobApplication {
  return {
    id: 'a1', roleTitle: 'Engineer', status: 'APPLIED', appliedDate: null, deadline: null,
    location: null, jobPostingUrl: null, notes: null,
    company: { id: 'c1', name: 'Acme', website: null, notes: null },
    resumeId: null, resumeName: null, statusHistory: [],
    createdAt: '2026-01-01T00:00:00Z', updatedAt: '2026-01-01T00:00:00Z',
    ...overrides,
  }
}

describe('buildRecommendations', () => {
  it('returns empty when everything is caught up and nothing to encourage', () => {
    const recs = buildRecommendations([], [goal()], [resume({ reviewScore: 90 })], [], [])
    expect(recs).toEqual([])
  })

  it('flags overdue tasks with the correct count and pluralization', () => {
    const yesterday = new Date(Date.now() - 86400000).toISOString()
    const recs = buildRecommendations(
      [task({ dueDate: yesterday, status: 'TODO' }), task({ dueDate: yesterday, status: 'TODO' })],
      [goal()], [resume({ reviewScore: 1 })], [], []
    )
    expect(recs[0].text).toBe('You have 2 overdue tasks')
    expect(recs[0].tone).toBe('amber')
    expect(recs[0].linkTo).toBe('/tasks')
  })

  it('does not flag a done task even if its due date is in the past', () => {
    const yesterday = new Date(Date.now() - 86400000).toISOString()
    const recs = buildRecommendations([task({ dueDate: yesterday, status: 'DONE' })], [goal()], [resume({ reviewScore: 1 })], [], [])
    expect(recs.find(r => r.text.includes('overdue'))).toBeUndefined()
  })

  it('flags a job application deadline within 3 days, picking the soonest', () => {
    const now = new Date()
    const midnightToday = new Date(now.getFullYear(), now.getMonth(), now.getDate())
    const in2Days = new Date(midnightToday.getTime() + 2 * 86400000).toISOString()
    const in1Day = new Date(midnightToday.getTime() + 1 * 86400000).toISOString()
    const recs = buildRecommendations(
      [], [goal()], [resume({ reviewScore: 1 })], [],
      [application({ deadline: in2Days, roleTitle: 'Later Role' }), application({ deadline: in1Day, roleTitle: 'Sooner Role' })]
    )
    expect(recs[0].text).toContain('Sooner Role')
    expect(recs[0].text).toContain('deadline in 1 day')
  })

  it('ignores a deadline more than 3 days away', () => {
    const in10Days = new Date(Date.now() + 10 * 86400000).toISOString()
    const recs = buildRecommendations([], [goal()], [resume({ reviewScore: 1 })], [], [application({ deadline: in10Days })])
    expect(recs.find(r => r.linkTo === '/careers')).toBeUndefined()
  })

  it('flags a habit streak at risk when not checked in today', () => {
    const recs = buildRecommendations([], [goal()], [resume({ reviewScore: 1 })], [habit({ currentStreak: 5, checkedInToday: false, title: 'Exercise' })], [])
    expect(recs[0].text).toBe('Don\'t break your streak on "Exercise"')
    expect(recs[0].tone).toBe('blue')
  })

  it('does not flag a habit with zero streak or already checked in today', () => {
    const recs = buildRecommendations(
      [], [goal()], [resume({ reviewScore: 1 })],
      [habit({ currentStreak: 0, checkedInToday: false }), habit({ currentStreak: 5, checkedInToday: true })], []
    )
    expect(recs.find(r => r.text.includes('streak'))).toBeUndefined()
  })

  it('suggests resume review when a resume exists but none has been reviewed', () => {
    const recs = buildRecommendations([], [goal()], [resume({ reviewScore: null })], [], [])
    expect(recs.some(r => r.text === 'Get AI feedback on your resume')).toBe(true)
  })

  it('suggests setting a goal when there are zero active goals', () => {
    const recs = buildRecommendations([], [], [resume({ reviewScore: 1 })], [], [])
    expect(recs.some(r => r.text === 'Set a goal to stay focused')).toBe(true)
  })

  it('suggests uploading a resume when there are zero resumes', () => {
    const recs = buildRecommendations([], [goal()], [], [], [])
    expect(recs.some(r => r.text === 'Upload a resume to get started')).toBe(true)
  })

  it('caps the result at 3 recommendations even when more rules match', () => {
    const yesterday = new Date(Date.now() - 86400000).toISOString()
    const recs = buildRecommendations(
      [task({ dueDate: yesterday, status: 'TODO' })],
      [],
      [],
      [habit({ currentStreak: 3, checkedInToday: false })],
      [application({ deadline: new Date(Date.now() + 86400000).toISOString() })]
    )
    expect(recs.length).toBeLessThanOrEqual(3)
  })
})
