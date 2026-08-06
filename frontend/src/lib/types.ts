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

export type LearningResourceType = 'COURSE' | 'BOOK' | 'TUTORIAL' | 'CERTIFICATE' | 'VIDEO' | 'ARTICLE'
export type LearningStatus = 'NOT_STARTED' | 'IN_PROGRESS' | 'COMPLETED' | 'ABANDONED'

export interface LearningResource {
  id: string
  title: string
  resourceType: LearningResourceType
  provider: string | null
  url: string | null
  status: LearningStatus
  progressPercent: number
  estimatedCompletionDate: string | null
  tags: string[]
  notes: string | null
  createdAt: string
  updatedAt: string
}

export interface LearningResourceInput {
  title: string
  resourceType?: LearningResourceType
  provider?: string | null
  url?: string | null
  status?: LearningStatus
  progressPercent?: number
  estimatedCompletionDate?: string | null
  tags?: string[]
  notes?: string | null
}

export const LEARNING_TYPE_LABELS: Record<LearningResourceType, string> = {
  COURSE: 'Course',
  BOOK: 'Book',
  TUTORIAL: 'Tutorial',
  CERTIFICATE: 'Certificate',
  VIDEO: 'Video',
  ARTICLE: 'Article',
}

export const LEARNING_STATUS_LABELS: Record<LearningStatus, string> = {
  NOT_STARTED: 'Not Started',
  IN_PROGRESS: 'In Progress',
  COMPLETED: 'Completed',
  ABANDONED: 'Abandoned',
}

export interface Resume {
  id: string
  name: string
  label: string | null
  fileName: string
  fileSizeBytes: number
  notes: string | null
  downloadUrl: string
  createdAt: string
  updatedAt: string
}

export interface ResumeMetadataInput {
  name: string
  label?: string | null
  notes?: string | null
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

export type JobApplicationStatus =
  | 'WISHLIST'
  | 'APPLIED'
  | 'INTERVIEWING'
  | 'OFFER'
  | 'ACCEPTED'
  | 'REJECTED'
  | 'WITHDRAWN'

export interface Company {
  id: string
  name: string
  website: string | null
  notes: string | null
}

export interface JobStatusHistoryEntry {
  id: string
  status: JobApplicationStatus
  note: string | null
  changedAt: string
}

export interface JobApplication {
  id: string
  roleTitle: string
  status: JobApplicationStatus
  appliedDate: string | null
  deadline: string | null
  location: string | null
  jobPostingUrl: string | null
  notes: string | null
  company: Company
  resumeId: string | null
  resumeName: string | null
  statusHistory: JobStatusHistoryEntry[]
  createdAt: string
  updatedAt: string
}

export interface JobApplicationInput {
  companyName: string
  companyWebsite?: string | null
  roleTitle: string
  status?: JobApplicationStatus
  appliedDate?: string | null
  deadline?: string | null
  location?: string | null
  jobPostingUrl?: string | null
  notes?: string | null
  resumeId?: string | null
}

export interface JobStatusChangeInput {
  status: JobApplicationStatus
  note?: string | null
}

export const JOB_STATUS_LABELS: Record<JobApplicationStatus, string> = {
  WISHLIST: 'Wishlist',
  APPLIED: 'Applied',
  INTERVIEWING: 'Interviewing',
  OFFER: 'Offer',
  ACCEPTED: 'Accepted',
  REJECTED: 'Rejected',
  WITHDRAWN: 'Withdrawn',
}
