import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, FormArray, Validators } from '@angular/forms';
import { Router, ActivatedRoute } from '@angular/router';
import { Store } from '@ngrx/store';
import { Observable } from 'rxjs';
import { TemplateService } from '../../services/template.service';
import { Template, Task, TaskType, TaskPriority, TaskTypeEnum, TaskPriorityEnum } from '../../models/template.model';
import { selectUser } from '../../../auth/store/auth.selectors';
import { UserType } from '../../../auth/model/auth.model';

@Component({
  selector: 'app-template-edit',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './template-edit.component.html',
  styleUrls: ['./template-edit.component.css']
})
export class TemplateEditComponent implements OnInit {
  templateForm: FormGroup;
  user$: Observable<UserType | null>;
  templateId: number;
  template: Template | null = null;
  isLoading = false;
  errorMessage = '';
  
  // Expose enums to template
  taskTypes = Object.values(TaskTypeEnum);
  taskPriorities = Object.values(TaskPriorityEnum);
  
  // File upload properties
  selectedFiles: { [taskIndex: number]: File } = {};

  constructor(
    private fb: FormBuilder,
    private templateService: TemplateService,
    private router: Router,
    private route: ActivatedRoute,
    private store: Store
  ) {
    this.user$ = this.store.select(selectUser);
    this.templateId = Number(this.route.snapshot.paramMap.get('id'));
    
    this.templateForm = this.fb.group({
      title: ['', [Validators.required, Validators.minLength(3)]],
      description: ['', [Validators.required, Validators.minLength(10)]],
      status: ['ACTIVE', Validators.required],
      tasks: this.fb.array([])
    });
  }

  ngOnInit(): void {
    this.loadTemplate();
  }

  get tasks(): FormArray {
    return this.templateForm.get('tasks') as FormArray;
  }

  loadTemplate(): void {
    this.isLoading = true;
    this.templateService.getTemplate(this.templateId).subscribe({
      next: (template) => {
        this.template = template;
        this.populateForm(template);
        this.isLoading = false;
      },
      error: (error) => {
        console.error('Error loading template:', error);
        this.errorMessage = 'Failed to load template';
        this.isLoading = false;
      }
    });
  }

  populateForm(template: Template): void {
    this.templateForm.patchValue({
      title: template.title,
      description: template.description,
      status: template.status
    });

    // Clear existing tasks
    while (this.tasks.length !== 0) {
      this.tasks.removeAt(0);
    }

    // Add template tasks
    template.tasks?.forEach(task => {
      this.addTask(task);
    });
  }

  createTaskFormGroup(task?: Task): FormGroup {
    return this.fb.group({
      id: [task?.id || null],
      title: [task?.title || '', [Validators.required, Validators.minLength(3)]],
      description: [task?.description || '', [Validators.required, Validators.minLength(5)]],
      taskType: [task?.taskType || 'RESOURCE', Validators.required],
      priority: [task?.priority || 'MEDIUM', Validators.required],
      estimatedHours: [task?.estimatedHours || 1, [Validators.required, Validators.min(0.5)]],
      orderIndex: [task?.orderIndex || this.tasks.length],
      requiresSignature: [task?.requiresSignature || false],
      resourceUrl: [task?.resourceUrl || ''],
      eventDate: [task?.eventDate || '']
    });
  }

  addTask(task?: Task): void {
    const taskGroup = this.createTaskFormGroup(task);
    this.tasks.push(taskGroup);
  }

  removeTask(index: number): void {
    this.tasks.removeAt(index);
    // Update order indices
    this.tasks.controls.forEach((control, i) => {
      control.get('orderIndex')?.setValue(i);
    });
  }

  moveTaskUp(index: number): void {
    if (index > 0) {
      const task = this.tasks.at(index);
      this.tasks.removeAt(index);
      this.tasks.insert(index - 1, task);
      this.updateTaskOrder();
    }
  }

  moveTaskDown(index: number): void {
    if (index < this.tasks.length - 1) {
      const task = this.tasks.at(index);
      this.tasks.removeAt(index);
      this.tasks.insert(index + 1, task);
      this.updateTaskOrder();
    }
  }

  updateTaskOrder(): void {
    this.tasks.controls.forEach((control, index) => {
      control.get('orderIndex')?.setValue(index);
    });
  }

  onSubmit(): void {
    if (this.templateForm.valid) {
      this.isLoading = true;
      const formValue = this.templateForm.value;
      
      const templateData: Template = {
        id: this.templateId,
        title: formValue.title,
        description: formValue.description,
        status: formValue.status,
        tasks: formValue.tasks,
        createdAt: this.template?.createdAt || new Date(),
        updatedAt: new Date()
      };

      this.templateService.updateTemplate(this.templateId, templateData).subscribe({
        next: (response) => {
          console.log('Template updated successfully:', response);
          this.router.navigate(['/templates']);
        },
        error: (error) => {
          console.error('Error updating template:', error);
          this.errorMessage = 'Failed to update template. Please try again.';
          this.isLoading = false;
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
        control.controls.forEach(arrayControl => {
          if (arrayControl instanceof FormGroup) {
            Object.keys(arrayControl.controls).forEach(arrayKey => {
              arrayControl.get(arrayKey)?.markAsTouched();
            });
          }
        });
      }
    });
  }

  cancel(): void {
    this.router.navigate(['/templates']);
  }

  navigateToTemplates(): void {
    this.router.navigate(['/templates']);
  }

  navigateToDashboard(): void {
    this.router.navigate(['/dashboard']);
  }

  logout(): void {
    // Implement logout logic
    this.router.navigate(['/auth/login']);
  }
  
  onFileSelected(event: any, taskIndex: number): void {
    const file = event.target.files[0];
    if (file) {
      this.selectedFiles[taskIndex] = file;
    }
  }
  
  uploadDocument(taskIndex: number): void {
    const file = this.selectedFiles[taskIndex];
    const taskGroup = this.tasks.at(taskIndex) as FormGroup;
    const taskId = taskGroup.get('id')?.value;
    
    if (file && taskId) {
      this.isLoading = true;
      this.templateService.uploadDocumentToTask(taskId, file).subscribe({
        next: (document) => {
          console.log('Document uploaded successfully:', document);
          delete this.selectedFiles[taskIndex];
          this.isLoading = false;
          // You could show a success message here
        },
        error: (error) => {
          console.error('Error uploading document:', error);
          this.errorMessage = 'Failed to upload document. Please try again.';
          this.isLoading = false;
        }
      });
    } else if (file && !taskId) {
      // If task doesn't have an ID yet (new task), show a message
      this.errorMessage = 'Please save the template first before uploading documents to new tasks.';
    }
  }
  
  getTaskTypeLabel(taskType: string): string {
    switch (taskType) {
      case 'EVENT': return 'Event (Date/Time)';
      case 'DOCUMENT': return 'Document (Signature Required)';
      case 'RESOURCE': return 'Resource (URL/PDF/Video)';
      default: return taskType;
    }
  }
  
  isDocumentTask(taskIndex: number): boolean {
    const taskGroup = this.tasks.at(taskIndex) as FormGroup;
    return taskGroup.get('taskType')?.value === 'DOCUMENT';
  }
  
  isEventTask(taskIndex: number): boolean {
    const taskGroup = this.tasks.at(taskIndex) as FormGroup;
    return taskGroup.get('taskType')?.value === 'EVENT';
  }
  
  isResourceTask(taskIndex: number): boolean {
    const taskGroup = this.tasks.at(taskIndex) as FormGroup;
    return taskGroup.get('taskType')?.value === 'RESOURCE';
  }
}