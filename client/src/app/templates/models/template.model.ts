export interface Template {
  id?: number;
  title: string;
  description: string;
  status: TemplateStatus;
  hrId?: string;
  hrName?: string;
  taskIds?: number[];
  tasks?: Task[];
  departmentIds?: number[];
  createdAt?: Date;
  updatedAt?: Date;
  createdDate?: string;
  updatedDate?: string;
}

export type TemplateStatus = 'ACTIVE' | 'INACTIVE' | 'DRAFT';

export enum TemplateStatusEnum {
  ACTIVE = 'ACTIVE',
  INACTIVE = 'INACTIVE',
  DRAFT = 'DRAFT'
}

export enum TaskStatus {
  PENDING = 'PENDING',
  IN_PROGRESS = 'IN_PROGRESS',
  COMPLETED = 'COMPLETED'
}

export interface Todo {
  id?: number;
  title: string;
  description: string;
  isCompleted: boolean;
  templateId: number;
  userId: string;
  createdDate?: string;
  updatedDate?: string;
}

export interface Document {
  id?: number;
  fileName: string;
  filePath: string;
  fileSize: number;
  uploadedBy: string;
  templateId: number;
  createdDate?: string;
}

export interface Notification {
  id?: number;
  title: string;
  message: string;
  isRead: boolean;
  userId: string;
  templateId?: number;
  createdDate?: string;
}

export interface Progress {
  id?: number;
  userId: string;
  templateId: number;
  completedTasks: number;
  totalTasks: number;
  progressPercentage: number;
  status: TemplateStatus;
  startDate?: string;
  completionDate?: string;
}

export interface Task {
  id?: number;
  title: string;
  description: string;
  taskType: TaskType;
  priority: TaskPriority;
  estimatedHours: number;
  orderIndex: number;
  isCompleted: boolean;
  requiresSignature: boolean;
  resourceUrl?: string;
  eventDate?: string;
  templateId?: number;
  assignedUserId?: string;
  dueDate?: string;
  createdDate?: string;
  updatedDate?: string;
}

export type TaskType = 'EVENT' | 'DOCUMENT' | 'RESOURCE';
export type TaskPriority = 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL';

export enum TaskTypeEnum {
  EVENT = 'EVENT',
  DOCUMENT = 'DOCUMENT',
  RESOURCE = 'RESOURCE'
}

export enum TaskPriorityEnum {
  LOW = 'LOW',
  MEDIUM = 'MEDIUM',
  HIGH = 'HIGH',
  CRITICAL = 'CRITICAL'
}

export interface Department {
  id: number;
  name: string;
  description?: string;
}

export interface HrUser {
  id: string;
  firstName: string;
  lastName: string;
  email: string;
  role: string;
  department?: Department;
}