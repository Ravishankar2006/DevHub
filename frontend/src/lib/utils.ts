import { type ClassValue, clsx } from 'clsx'
import { twMerge } from 'tailwind-merge'

export function cn(...inputs: ClassValue[]) {
  return twMerge(clsx(inputs))
}

export function formatDate(date: string | Date) {
  return new Intl.DateTimeFormat('en-US', {
    month: 'short',
    day: 'numeric',
    year: 'numeric',
  }).format(new Date(date))
}

export function formatRelativeTime(date: string | Date) {
  const rtf = new Intl.RelativeTimeFormat('en', { numeric: 'auto' })
  const diff = (new Date(date).getTime() - Date.now()) / 1000
  if (Math.abs(diff) < 60) return 'just now'
  if (Math.abs(diff) < 3600) return rtf.format(Math.round(diff / 60), 'minute')
  if (Math.abs(diff) < 86400) return rtf.format(Math.round(diff / 3600), 'hour')
  return rtf.format(Math.round(diff / 86400), 'day')
}

export function getInitials(name: string) {
  return name
    .split(' ')
    .slice(0, 2)
    .map(n => n[0])
    .join('')
    .toUpperCase()
}
