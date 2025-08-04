import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { FormsModule, ReactiveFormsModule, FormBuilder, FormGroup, Validators, FormArray } from '@angular/forms';
import { TemplateService } from '../../services/template.service';
import { Template, TemplateStatus, TemplateStatusEnum } from '../../models/template.model';
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
  private router = inject(Router);
  private fb = inject(FormBuilder);
  private store = inject(Store);

  templateForm: FormGroup;
  loading = false;
  error = '';
  success = '';
  
  user$: Observable<UserType | null> = this.store.select(selectUser);
  currentUser: UserType | null = null;

  templateStatuses = Object.values(TemplateStatusEnum);

  constructor() {
    this.templateForm = this.fb.group({
      title: ['', [Validators.required, Validators.minLength(3)]],
      description: ['', [Validators.required, Validators.minLength(10)]],
      status: ['ACTIVE', Validators.required],
      tasks: this.fb.array([this.createTaskFormGroup()])
    });
  }

  ngOnInit() {
    this.user$.subscribe(user => {
      this.currentUser = user;
    });
  }

  get tasks(): FormArray {
    return this.templateForm.get('tasks') as FormArray;
  }

  createTaskFormGroup(): FormGroup {
    return this.fb.group({
      title: ['', [Validators.required, Validators.minLength(3)]],
      description: ['', [Validators.required, Validators.minLength(5)]],
      dueDate: [''],
      isCompleted: [false]
    });
  }

  addTask() {
    this.tasks.push(this.createTaskFormGroup());
  }

  removeTask(index: number) {
    if (this.tasks.length > 1) {
      this.tasks.removeAt(index);
    }
  }

  onSubmit() {
    if (this.templateForm.valid && this.currentUser) {
      this.loading = true;
      this.error = '';
      this.success = '';

      const formValue = this.templateForm.value;
      const template: Template = {
        title: formValue.title,
        description: formValue.description,
        status: formValue.status,
        hrId: this.currentUser.id,
        taskIds: [], // Will be populated after tasks are created
        departmentIds: [] // Can be assigned later
      };

      this.templateService.createTemplate(template).subscribe({
        next: (createdTemplate) => {
          this.success = 'Template created successfully!';
          this.loading = false;
          
          // Navigate back to templates list after a short delay
          setTimeout(() => {
            this.router.navigate(['/templates']);
          }, 2000);
        },
        error: (error) => {
          this.error = 'Failed to create template. Please try again.';
          this.loading = false;
          console.error('Error creating template:', error);
        }
      });
    } else {
      this.markFormGroupTouched();
    }
  }

  markFormGroupTouched() {
    Object.keys(this.templateForm.controls).forEach(key => {
      const control = this.templateForm.get(key);
      control?.markAsTouched();
      
      if (control instanceof FormArray) {
        control.controls.forEach(group => {
          if (group instanceof FormGroup) {
            Object.keys(group.controls).forEach(nestedKey => {
              group.get(nestedKey)?.markAsTouched();
            });
          }
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

  cancel() {
    this.router.navigate(['/templates']);
  }

  logout() {
    this.router.navigate(['/login']);
  }

  navigateToDashboard() {
    this.router.navigate(['/dashboard']);
  }

  navigateToTemplates() {
    this.router.navigate(['/templates']);
  }
}