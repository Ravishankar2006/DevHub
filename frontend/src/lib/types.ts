export type ProjectStatus = 'PLANNING' | 'IN_PROGRESS' | 'ON_HOLD' | 'COMPLETED'
export type TaskStatus = 'TODO' | 'IN_PROGRESS' | 'DONE'
export type TaskPriority = 'LOW' | 'MEDIUM' | 'HIGH'

export interface Project {
  id: string
  name: string
  description: string | null
  repoUrl: string | null
  liveUrl: string | null
  roadmap: string | null
  stackTags: string[]
  status: ProjectStatus
  archived: boolean
  taskCount: number
  completedTaskCount: number
  milestoneCount: number
  createdAt: string
  updatedAt: string
}

export interface ProjectInput {
  name: string
  description?: string
  repoUrl?: string
  liveUrl?: string
  roadmap?: string
  stackTags?: string[]
  status?: ProjectStatus
  archived?: boolean
}

export interface Milestone {
  id: string
  projectId: string
  projectName: string
  title: string
  description: string | null
  dueDate: string | null
  totalTasks: number
  completedTasks: number
  progressPercent: number
  createdAt: string
  updatedAt: string
}

export interface MilestoneInput {
  title: string
  description?: string
  dueDate?: string | null
}

export interface Task {
  id: string
  projectId: string | null
  projectName: string | null
  milestoneId: string | null
  milestoneTitle: string | null
  title: string
  description: string | null
  status: TaskStatus
  priority: TaskPriority
  dueDate: string | null
  position: number
  createdAt: string
  updatedAt: string
}

export interface TaskInput {
  title: string
  description?: string
  projectId?: string | null
  milestoneId?: string | null
  status?: TaskStatus
  priority?: TaskPriority
  dueDate?: string | null
}

export const TASK_STATUSES: TaskStatus[] = ['TODO', 'IN_PROGRESS', 'DONE']

export const TASK_STATUS_LABELS: Record<TaskStatus, string> = {
  TODO: 'To Do',
  IN_PROGRESS: 'In Progress',
  DONE: 'Done',
}

export const PROJECT_STATUS_LABELS: Record<ProjectStatus, string> = {
  PLANNING: 'Planning',
  IN_PROGRESS: 'In Progress',
  ON_HOLD: 'On Hold',
  COMPLETED: 'Completed',
}

export type GoalType = 'DAILY' | 'WEEKLY' | 'MONTHLY' | 'CAREER'
export type GoalStatus = 'ACTIVE' | 'COMPLETED' | 'ABANDONED'
export type HabitFrequency = 'DAILY' | 'WEEKLY'

export interface Goal {
  id: string
  title: string
  description: string | null
  type: GoalType
  status: GoalStatus
  targetDate: string | null
  progressPercent: number
  projectId: string | null
  projectName: string | null
  createdAt: string
  updatedAt: string
}

export interface GoalInput {
  title: string
  description?: string
  type?: GoalType
  status?: GoalStatus
  targetDate?: string | null
  progressPercent?: number
  projectId?: string | null
}

export interface Habit {
  id: string
  goalId: string | null
  goalTitle: string | null
  title: string
  frequency: HabitFrequency
  currentStreak: number
  longestStreak: number
  checkedInToday: boolean
  createdAt: string
  updatedAt: string
}

export interface HabitInput {
  title: string
  goalId?: string | null
  frequency?: HabitFrequency
}

export const GOAL_TYPE_LABELS: Record<GoalType, string> = {
  DAILY: 'Daily',
  WEEKLY: 'Weekly',
  MONTHLY: 'Monthly',
  CAREER: 'Career',
}

export const GOAL_STATUS_LABELS: Record<GoalStatus, string> = {
  ACTIVE: 'Active',
  COMPLETED: 'Completed',
  ABANDONED: 'Abandoned',
}

export interface NoteFolder {
  id: string
  name: string
  noteCount: number
  createdAt: string
  updatedAt: string
}

export interface NoteFolderInput {
  name: string
}

export interface Note {
  id: string
  folderId: string | null
  folderName: string | null
  title: string
  content: string | null
  tags: string[]
  createdAt: string
  updatedAt: string
}

export interface NoteInput {
  title: string
  content?: string
  tags?: string[]
  folderId?: string | null
}
