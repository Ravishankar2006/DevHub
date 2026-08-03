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
