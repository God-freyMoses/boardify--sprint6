import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Template, Todo, Document, Notification, Progress, Task } from '../models/template.model';

@Injectable({
  providedIn: 'root'
})
export class TemplateService {
  private http = inject(HttpClient);
  private baseUrl = 'http://localhost:8080/api';

  // Template CRUD operations
  createTemplate(template: Template): Observable<Template> {
    return this.http.post<Template>(`${this.baseUrl}/templates`, template);
  }

  getTemplate(id: number): Observable<Template> {
    return this.http.get<Template>(`${this.baseUrl}/templates/${id}`);
  }

  getAllTemplates(): Observable<Template[]> {
    return this.http.get<Template[]>(`${this.baseUrl}/templates`);
  }

  getTemplateById(id: number): Observable<Template> {
    return this.http.get<Template>(`${this.baseUrl}/templates/${id}`);
  }

  updateTemplate(id: number, template: Template): Observable<Template> {
    return this.http.put<Template>(`${this.baseUrl}/templates/${id}`, template);
  }

  deleteTemplate(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/templates/${id}`);
  }

  assignTemplate(assignmentData: any): Observable<void> {
    return this.http.post<void>(`${this.baseUrl}/templates/assign`, assignmentData);
  }

  getEmployeesByDepartment(departmentId: number): Observable<any[]> {
    return this.http.get<any[]>(`${this.baseUrl}/employees/department/${departmentId}`);
  }

  getDepartments(): Observable<any[]> {
    return this.http.get<any[]>(`${this.baseUrl}/departments`);
  }

  getEmployees(): Observable<any[]> {
    return this.http.get<any[]>(`${this.baseUrl}/employees`);
  }

  // Todo operations
  createTodo(todo: Todo): Observable<Todo> {
    return this.http.post<Todo>(`${this.baseUrl}/todos`, todo);
  }

  getTodosByTemplate(templateId: number): Observable<Todo[]> {
    return this.http.get<Todo[]>(`${this.baseUrl}/todos/template/${templateId}`);
  }

  getTodosByUser(userId: string): Observable<Todo[]> {
    return this.http.get<Todo[]>(`${this.baseUrl}/todos/user/${userId}`);
  }

  updateTodo(id: number, todo: Todo): Observable<Todo> {
    return this.http.put<Todo>(`${this.baseUrl}/todos/${id}`, todo);
  }

  deleteTodo(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/todos/${id}`);
  }

  markTodoComplete(id: number): Observable<Todo> {
    return this.http.patch<Todo>(`${this.baseUrl}/todos/${id}/complete`, {});
  }

  // Document operations
  uploadDocument(templateId: number, file: File): Observable<Document> {
    const formData = new FormData();
    formData.append('file', file);
    formData.append('templateId', templateId.toString());
    return this.http.post<Document>(`http://localhost:8080/api/documents/upload`, formData);
  }
  
  uploadDocumentToTask(taskId: number, file: File): Observable<Document> {
    const formData = new FormData();
    formData.append('file', file);
    formData.append('taskId', taskId.toString());
    return this.http.post<Document>(`http://localhost:8080/api/documents/upload`, formData);
  }

  getDocumentsByTemplate(templateId: number): Observable<Document[]> {
    return this.http.get<Document[]>(`http://localhost:8080/api/documents/template/${templateId}`);
  }

  downloadDocument(id: number): Observable<Blob> {
    return this.http.get(`http://localhost:8080/api/documents/${id}/download`, { responseType: 'blob' });
  }

  deleteDocument(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/documents/${id}`);
  }

  // Notification operations
  createNotification(notification: Notification): Observable<Notification> {
    return this.http.post<Notification>(`${this.baseUrl}/notifications`, notification);
  }

  getNotificationsByUser(userId: string): Observable<Notification[]> {
    return this.http.get<Notification[]>(`${this.baseUrl}/notifications/user/${userId}`);
  }

  markNotificationAsRead(id: number): Observable<Notification> {
    return this.http.patch<Notification>(`${this.baseUrl}/notifications/${id}/read`, {});
  }

  deleteNotification(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/notifications/${id}`);
  }

  // Progress tracking
  getProgressByUserAndTemplate(userId: string, templateId: number): Observable<Progress> {
    return this.http.get<Progress>(`${this.baseUrl}/progress/user/${userId}/template/${templateId}`);
  }

  updateProgress(userId: string, templateId: number): Observable<Progress> {
    return this.http.post<Progress>(`${this.baseUrl}/progress/update`, { userId, templateId });
  }

  // Task operations
  getTasksByTemplate(templateId: number): Observable<Task[]> {
    return this.http.get<Task[]>(`${this.baseUrl}/tasks/template/${templateId}`);
  }

  getTasksByUser(userId: string): Observable<Task[]> {
    return this.http.get<Task[]>(`${this.baseUrl}/tasks/user/${userId}`);
  }

  updateTask(id: number, task: Task): Observable<Task> {
    return this.http.put<Task>(`${this.baseUrl}/tasks/${id}`, task);
  }

  markTaskComplete(id: number): Observable<Task> {
    return this.http.patch<Task>(`${this.baseUrl}/tasks/${id}/complete`, {});
  }
}