import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import api from '@/lib/api'
import type { CalendarEvent, CalendarEventInput } from '@/lib/types'

interface CalendarEventFilters {
  from?: string
  to?: string
}

export function useCalendarEvents(filters: CalendarEventFilters = {}) {
  return useQuery({
    queryKey: ['calendarEvents', filters],
    queryFn: async () => {
      const res = await api.get<CalendarEvent[]>('/calendar/events', { params: filters })
      return res.data
    },
    enabled: !!filters.from && !!filters.to,
  })
}

export function useCalendarEvent(id: string | undefined) {
  return useQuery({
    queryKey: ['calendarEvents', id],
    queryFn: async () => {
      const res = await api.get<CalendarEvent>(`/calendar/events/${id}`)
      return res.data
    },
    enabled: !!id,
  })
}

export function useCreateCalendarEvent() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: async (input: CalendarEventInput) => {
      const res = await api.post<CalendarEvent>('/calendar/events', input)
      return res.data
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['calendarEvents'] })
    },
  })
}

export function useUpdateCalendarEvent(id: string) {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: async (input: CalendarEventInput) => {
      const res = await api.put<CalendarEvent>(`/calendar/events/${id}`, input)
      return res.data
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['calendarEvents'] })
    },
  })
}

export function useDeleteCalendarEvent() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: async (id: string) => {
      await api.delete(`/calendar/events/${id}`)
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['calendarEvents'] })
    },
  })
}

export function useUpcomingCalendarEvents(limit: number) {
  const now = new Date()
  const horizon = new Date(now.getTime() + 30 * 24 * 60 * 60 * 1000)

  const { data, isLoading } = useCalendarEvents({ from: now.toISOString(), to: horizon.toISOString() })

  return { data: data?.slice(0, limit), isLoading }
}
