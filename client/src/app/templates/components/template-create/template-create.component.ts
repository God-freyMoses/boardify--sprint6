import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { FormsModule, ReactiveFormsModule, FormBuilder, FormGroup, Validators, FormArray } from '@angular/forms';
import { TemplateService } from '../../services/template.service';
import { Template, TemplateStatus, TemplateStatusEnum, TaskType, TaskPriority, TaskTypeEnum, TaskPriorityEnum } from '../../models/template.model';
import { Store } from '@ngrx/store';
import { selectUser } from '../../../auth/store/auth.selectors';
import { UserType } from '../../../auth/model/auth.model';
import { Observable } from 'rxjs';

@Component({
  selector: 'app-template-create',
  standalone: true,
  imports: [CommonModule, FormsModule, ReactiveFormsModule],
  templateUrl: './template-create.component.html',
  styleUrls: ['./template-create.component.css']
})
export class TemplateCreateComponent implements OnInit {
  private templateService = inject(TemplateService);
  public router = inject(Router);
  private fb = inject(FormBuilder);
  private store = inject(Store);

  templateForm: FormGroup;
  loading = false;
  error = '';
  success = '';
  
  user$: Observable<UserType | null> = this.store.select(selectUser);
  currentUser: UserType | null = null;

  templateStatuses = Object.values(TemplateStatusEnum);
  
  // Expose enums to template
  taskTypes = Object.values(TaskTypeEnum);
  taskPriorities = Object.values(TaskPriorityEnum);
  
  // File upload properties
  selectedFiles: { [taskIndex: number]: File } = {};

  // Method to trigger file input click
  triggerFileInput(taskIndex: number): void {
    const fileInput = document.getElementById('file-' + taskIndex) as HTMLInputElement;
    if (fileInput) {
      fileInput.click();
    }
  }

  constructor() {
    this.templateForm = this.fb.group({
      title: ['', [Validators.required, Validators.minLength(3)]],
      description: ['', [Validators.required, Validators.minLength(10)]],
      status: [TemplateStatusEnum.ACTIVE, Validators.required],
      tasks: this.fb.array([])
    });
  }

  ngOnInit(): void {
    this.user$.subscribe(user => {
      this.currentUser = user;
    });
    
    // Add initial task
    this.addTask();
  }

  get tasks(): FormArray {
    return this.templateForm.get('tasks') as FormArray;
  }

  createTaskFormGroup(): FormGroup {
    return this.fb.group({
      title: ['', [Validators.required, Validators.minLength(3)]],
      description: ['', [Validators.required, Validators.minLength(10)]],
      dueDate: [''],
      taskType: [TaskTypeEnum.RESOURCE, Validators.required],
      priority: [TaskPriorityEnum.MEDIUM, Validators.required],
      eventDate: [''],
      requiresSignature: [false],
      resourceUrl: ['']
    });
  }

  addTask(): void {
    this.tasks.push(this.createTaskFormGroup());
  }

  removeTask(index: number): void {
    if (this.tasks.length > 1) {
      this.tasks.removeAt(index);
      // Clean up selected file for this task
      delete this.selectedFiles[index];
    }
  }

  // File upload methods
  onFileSelected(event: any, taskIndex: number): void {
    const file = event.target.files[0];
    if (file) {
      this.selectedFiles[taskIndex] = file;
    }
  }

  uploadDocument(taskIndex: number): void {
    const file = this.selectedFiles[taskIndex];
    if (file) {
      // For now, just store the file name as resourceUrl
      const taskGroup = this.tasks.at(taskIndex) as FormGroup;
      taskGroup.patchValue({ resourceUrl: file.name });
      
      // In a real application, you would upload the file to a server
      console.log('File uploaded:', file.name);
    }
  }

  // Task type checking methods
  isDocumentTask(taskIndex: number): boolean {
    const taskGroup = this.tasks.at(taskIndex) as FormGroup;
    return taskGroup.get('taskType')?.value === TaskTypeEnum.DOCUMENT;
  }

  isEventTask(taskIndex: number): boolean {
    const taskGroup = this.tasks.at(taskIndex) as FormGroup;
    return taskGroup.get('taskType')?.value === TaskTypeEnum.EVENT;
  }

  isResourceTask(taskIndex: number): boolean {
    const taskGroup = this.tasks.at(taskIndex) as FormGroup;
    return taskGroup.get('taskType')?.value === TaskTypeEnum.RESOURCE;
  }

  onSubmit(): void {
    if (this.templateForm.valid && this.currentUser) {
      this.loading = true;
      this.error = '';
      
      const templateData: Template = {
        ...this.templateForm.value,
        hrId: this.currentUser.id,
        createdAt: new Date(),
        updatedAt: new Date()
      };

      this.templateService.createTemplate(templateData).subscribe({
        next: (response) => {
          this.loading = false;
          this.success = 'Template created successfully!';
          setTimeout(() => {
            this.router.navigate(['/templates']);
          }, 2000);
        },
        error: (error) => {
          this.loading = false;
          this.error = 'Failed to create template. Please try again.';
          console.error('Error creating template:', error);
        }
      });
    } else {
      this.markFormGroupTouched();
    }
  }

  markFormGroupTouched(): void {
    Object.keys(this.templateForm.controls).forEach(key => {
      const control = this.templateForm.get(key);
      control?.markAsTouched();
      
      if (control instanceof FormArray) {
        control.controls.forEach(taskControl => {
          Object.keys(taskControl.value).forEach(taskKey => {
            taskControl.get(taskKey)?.markAsTouched();
          });
        });
      }
    });
  }

  isFieldInvalid(fieldName: string): boolean {
    const field = this.templateForm.get(fieldName);
    return !!(field && field.invalid && field.touched);
  }

  isTaskFieldInvalid(taskIndex: number, fieldName: string): boolean {
    const taskGroup = this.tasks.at(taskIndex) as FormGroup;
    const field = taskGroup.get(fieldName);
    return !!(field && field.invalid && field.touched);
  }

  getFieldError(fieldName: string): string {
    const field = this.templateForm.get(fieldName);
    if (field?.errors) {
      if (field.errors['required']) return `${fieldName} is required`;
      if (field.errors['minlength']) return `${fieldName} must be at least ${field.errors['minlength'].requiredLength} characters`;
    }
    return '';
  }

  getTaskFieldError(taskIndex: number, fieldName: string): string {
    const taskGroup = this.tasks.at(taskIndex) as FormGroup;
    const field = taskGroup.get(fieldName);
    if (field?.errors) {
      if (field.errors['required']) return `${fieldName} is required`;
      if (field.errors['minlength']) return `${fieldName} must be at least ${field.errors['minlength'].requiredLength} characters`;
    }
    return '';
  }

  cancel(): void {
    this.router.navigate(['/templates']);
  }

  logout(): void {
    this.router.navigate(['/login']);
  }

  navigateToDashboard(): void {
    this.router.navigate(['/dashboard']);
  }

  navigateToTemplates(): void {
    this.router.navigate(['/templates']);
  }
}