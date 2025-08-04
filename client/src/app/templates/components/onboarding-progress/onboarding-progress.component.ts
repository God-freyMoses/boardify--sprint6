import { Component, OnInit, inject, ViewChild, ElementRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { TemplateService } from '../../services/template.service';
import { Template, Todo, Document as TemplateDocument, Notification, Progress, Task } from '../../models/template.model';
import { Store } from '@ngrx/store';
import { selectUser } from '../../../auth/store/auth.selectors';
import { UserType } from '../../../auth/model/auth.model';
import { Observable } from 'rxjs';

@Component({
  selector: 'app-onboarding-progress',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './onboarding-progress.component.html',
  styleUrls: ['./onboarding-progress.component.css']
})
export class OnboardingProgressComponent implements OnInit {
  private templateService = inject(TemplateService);
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private store = inject(Store);

  templateId!: number;
  template: Template | null = null;
  todos: Todo[] = [];
  tasks: Task[] = [];
  documents: TemplateDocument[] = [];
  notifications: Notification[] = [];
  progress: Progress | null = null;
  
  loading = false;
  error = '';
  uploadingFile = false;
  selectedFile: File | null = null;
  
  user$: Observable<UserType | null> = this.store.select(selectUser);
  currentUser: UserType | null = null;

  @ViewChild('fileInput') fileInput!: ElementRef<HTMLInputElement>;

  ngOnInit() {
    this.user$.subscribe(user => {
      this.currentUser = user;
    });

    this.route.params.subscribe(params => {
      this.templateId = +params['templateId'];
      this.loadOnboardingData();
    });
  }

  loadOnboardingData() {
    if (!this.currentUser) return;
    
    this.loading = true;
    this.error = '';

    // Load template details
    this.templateService.getTemplateById(this.templateId).subscribe({
      next: (template) => {
        this.template = template;
      },
      error: (error) => {
        this.error = 'Failed to load template details';
        console.error('Error loading template:', error);
      }
    });

    // Load user's todos for this template
    this.templateService.getTodosByUser(this.currentUser.id).subscribe({
      next: (todos) => {
        this.todos = todos.filter(todo => todo.templateId === this.templateId);
      },
      error: (error) => {
        console.error('Error loading todos:', error);
      }
    });

    // Load user's tasks for this template
    this.templateService.getTasksByUser(this.currentUser.id).subscribe({
      next: (tasks) => {
        this.tasks = tasks.filter(task => task.templateId === this.templateId);
      },
      error: (error) => {
        console.error('Error loading tasks:', error);
      }
    });

    // Load documents for this template
    this.templateService.getDocumentsByTemplate(this.templateId).subscribe({
      next: (documents) => {
        this.documents = documents;
      },
      error: (error) => {
        console.error('Error loading documents:', error);
      }
    });

    // Load user's notifications
    this.templateService.getNotificationsByUser(this.currentUser.id).subscribe({
      next: (notifications) => {
        this.notifications = notifications.filter(n => n.templateId === this.templateId);
      },
      error: (error) => {
        console.error('Error loading notifications:', error);
      }
    });

    // Load progress
    this.templateService.getProgressByUserAndTemplate(this.currentUser.id, this.templateId).subscribe({
      next: (progress) => {
        this.progress = progress;
        this.loading = false;
      },
      error: (error) => {
        console.error('Error loading progress:', error);
        this.loading = false;
      }
    });
  }

  markTodoComplete(todoId: number) {
    this.templateService.markTodoComplete(todoId).subscribe({
      next: (updatedTodo) => {
        const index = this.todos.findIndex(t => t.id === todoId);
        if (index !== -1) {
          this.todos[index] = updatedTodo;
        }
        this.updateProgress();
      },
      error: (error) => {
        this.error = 'Failed to update todo';
        console.error('Error updating todo:', error);
      }
    });
  }

  markTaskComplete(taskId: number) {
    this.templateService.markTaskComplete(taskId).subscribe({
      next: (updatedTask) => {
        const index = this.tasks.findIndex(t => t.id === taskId);
        if (index !== -1) {
          this.tasks[index] = updatedTask;
        }
        this.updateProgress();
      },
      error: (error) => {
        this.error = 'Failed to update task';
        console.error('Error updating task:', error);
      }
    });
  }

  updateProgress() {
    if (!this.currentUser) return;
    
    this.templateService.updateProgress(this.currentUser.id, this.templateId).subscribe({
      next: (progress) => {
        this.progress = progress;
      },
      error: (error) => {
        console.error('Error updating progress:', error);
      }
    });
  }

  onFileSelected(event: any) {
    const file = event.target.files[0];
    if (file) {
      this.selectedFile = file;
    }
  }

  triggerFileInput(): void {
    this.fileInput?.nativeElement?.click();
  }

  triggerFileUpload(): void {
    this.fileInput?.nativeElement?.click();
  }

  uploadDocument() {
    if (!this.selectedFile) return;
    
    this.uploadingFile = true;
    this.templateService.uploadDocument(this.templateId, this.selectedFile).subscribe({
      next: (document) => {
        this.documents.push(document);
        this.selectedFile = null;
        this.uploadingFile = false;
        // Reset file input
        if (this.fileInput?.nativeElement) {
          this.fileInput.nativeElement.value = '';
        }
      },
      error: (error) => {
        this.error = 'Failed to upload document';
        this.uploadingFile = false;
        console.error('Error uploading document:', error);
      }
    });
  }

  downloadDocument(documentId: number, fileName: string) {
    this.templateService.downloadDocument(documentId).subscribe({
      next: (blob) => {
        const url = window.URL.createObjectURL(blob);
        const link = document.createElement('a');
        link.href = url;
        link.download = fileName;
        link.click();
        window.URL.revokeObjectURL(url);
      },
      error: (error) => {
        this.error = 'Failed to download document';
        console.error('Error downloading document:', error);
      }
    });
  }

  markNotificationAsRead(notificationId: number) {
    this.templateService.markNotificationAsRead(notificationId).subscribe({
      next: (updatedNotification) => {
        const index = this.notifications.findIndex(n => n.id === notificationId);
        if (index !== -1) {
          this.notifications[index] = updatedNotification;
        }
      },
      error: (error) => {
        console.error('Error marking notification as read:', error);
      }
    });
  }

  getProgressPercentage(): number {
    if (!this.progress) return 0;
    return Math.round(this.progress.progressPercentage);
  }

  getCompletedTasksCount(): number {
    return this.todos.filter(todo => todo.isCompleted).length + 
           this.tasks.filter(task => task.isCompleted).length;
  }

  getTotalTasksCount(): number {
    return this.todos.length + this.tasks.length;
  }

  formatDate(dateString: string): string {
    if (!dateString) return 'N/A';
    return new Date(dateString).toLocaleDateString();
  }

  formatFileSize(bytes: number): string {
    if (bytes === 0) return '0 Bytes';
    const k = 1024;
    const sizes = ['Bytes', 'KB', 'MB', 'GB'];
    const i = Math.floor(Math.log(bytes) / Math.log(k));
    return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i];
  }

  logout() {
    this.router.navigate(['/login']);
  }

  navigateToUserDashboard() {
    this.router.navigate(['/user-dash']);
  }
}