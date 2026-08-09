import { http, HttpResponse } from 'msw'

const BASE = 'http://localhost:8080/api'

export const fakeUser = {
  id: 'user-1',
  email: 'test@devhub.test',
  name: 'Test User',
  role: 'USER',
  avatarUrl: null,
  theme: 'light',
  githubConnected: false,
  dailyBriefEnabled: true,
}

const authResponse = {
  accessToken: 'fake-access-token',
  refreshToken: 'fake-refresh-token',
  user: fakeUser,
}

export const handlers = [
  http.post(`${BASE}/auth/login`, () => HttpResponse.json(authResponse)),
  http.post(`${BASE}/auth/register`, () => HttpResponse.json(authResponse, { status: 201 })),

  http.get(`${BASE}/users/me`, () => HttpResponse.json(fakeUser)),
  http.put(`${BASE}/users/me/theme`, () => HttpResponse.json(fakeUser)),

  http.get(`${BASE}/projects`, () => HttpResponse.json([])),
  http.post(`${BASE}/projects`, () => HttpResponse.json({ id: 'p1', name: 'New Project' }, { status: 201 })),
  http.get(`${BASE}/projects/:id`, () => HttpResponse.json({ id: 'p1', name: 'Project', status: 'IN_PROGRESS', archived: false, stackTags: [], taskCount: 0, completedTaskCount: 0, milestoneCount: 0 })),
  http.put(`${BASE}/projects/:id`, () => HttpResponse.json({ id: 'p1', name: 'Project' })),
  http.delete(`${BASE}/projects/:id`, () => new HttpResponse(null, { status: 204 })),
  http.get(`${BASE}/projects/:projectId/milestones`, () => HttpResponse.json([])),
  http.get(`${BASE}/milestones`, () => HttpResponse.json([])),

  http.get(`${BASE}/tasks`, () => HttpResponse.json([])),
  http.post(`${BASE}/tasks`, () => HttpResponse.json({ id: 't1', title: 'New Task' }, { status: 201 })),
  http.get(`${BASE}/tasks/:id`, () => HttpResponse.json({ id: 't1', title: 'Task' })),
  http.put(`${BASE}/tasks/:id`, () => HttpResponse.json({ id: 't1', title: 'Task' })),
  http.delete(`${BASE}/tasks/:id`, () => new HttpResponse(null, { status: 204 })),

  http.get(`${BASE}/goals`, () => HttpResponse.json([])),
  http.get(`${BASE}/habits`, () => HttpResponse.json([])),
  http.post(`${BASE}/habits/:id/checkins/today`, () => HttpResponse.json({ id: 'h1', checkedInToday: true, currentStreak: 1 })),

  http.get(`${BASE}/notes`, () => HttpResponse.json([])),
  http.get(`${BASE}/notes/folders`, () => HttpResponse.json([])),

  http.get(`${BASE}/learning`, () => HttpResponse.json([])),

  http.get(`${BASE}/resumes`, () => HttpResponse.json([])),
  http.post(`${BASE}/resumes/:id/review`, () => HttpResponse.json({ id: 'job-1', jobType: 'RESUME_REVIEW', status: 'PENDING' }, { status: 202 })),

  http.get(`${BASE}/careers/applications`, () => HttpResponse.json([])),

  http.get(`${BASE}/calendar/events`, () => HttpResponse.json([])),

  http.get(`${BASE}/ai/conversations`, () => HttpResponse.json([])),
  http.get(`${BASE}/ai/conversations/:id`, () => HttpResponse.json({ id: 'c1', title: null, messages: [] })),
  http.post(`${BASE}/ai/conversations`, () => HttpResponse.json({ id: 'c1', title: null, messages: [] }, { status: 201 })),

  http.get(`${BASE}/jobs/:id`, () => HttpResponse.json({ id: 'job-1', jobType: 'RESUME_REVIEW', status: 'COMPLETED' })),

  http.get(`${BASE}/documents`, () => HttpResponse.json([])),
  http.get(`${BASE}/search`, () => HttpResponse.json([])),

  http.post(`${BASE}/brief/generate`, () => HttpResponse.json({ id: 'job-1', jobType: 'DAILY_BRIEF', status: 'PENDING' }, { status: 202 })),
  http.get(`${BASE}/brief/today`, () => new HttpResponse(null, { status: 404 })),

  http.get(`${BASE}/github/account`, () => HttpResponse.json({ connected: false, username: null, avatarUrl: null, connectedAt: null, lastSyncedAt: null, repos: [], languageBreakdown: {} })),
  http.get(`${BASE}/github/authorize`, () => HttpResponse.json({ authorizeUrl: 'https://github.com/login/oauth/authorize' })),
  http.post(`${BASE}/github/sync`, () => HttpResponse.json({ id: 'job-1', jobType: 'GITHUB_SYNC', status: 'PENDING' }, { status: 202 })),
]
