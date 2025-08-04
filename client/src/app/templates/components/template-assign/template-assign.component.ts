import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router, ActivatedRoute } from '@angular/router';
import { Store } from '@ngrx/store';
import { Observable } from 'rxjs';
import { TemplateService } from '../../services/template.service';
import { Template, HrUser, Department } from '../../models/template.model';
import { selectUser } from '../../../auth/store/auth.selectors';
import { UserType } from '../../../auth/model/auth.model';

@Component({
  selector: 'app-template-assign',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './template-assign.component.html',
  styleUrls: ['./template-assign.component.css']
})
export class TemplateAssignComponent implements OnInit {
  assignForm: FormGroup;
  user$: Observable<UserType | null>;
  templateId: number;
  template: Template | null = null;
  employees: HrUser[] = [];
  departments: Department[] = [];
  isLoading = false;
  errorMessage = '';
  successMessage = '';

  constructor(
    private fb: FormBuilder,
    private templateService: TemplateService,
    private router: Router,
    private route: ActivatedRoute,
    private store: Store
  ) {
    this.user$ = this.store.select(selectUser);
    this.templateId = Number(this.route.snapshot.paramMap.get('id'));
    
    this.assignForm = this.fb.group({
      employeeId: ['', Validators.required],
      departmentId: [''],
      dueDate: ['', Validators.required],
      priority: ['MEDIUM', Validators.required],
      notes: ['']
    });
  }

  ngOnInit(): void {
    this.loadTemplate();
    this.loadEmployees();
    this.loadDepartments();
  }

  loadTemplate(): void {
    this.templateService.getTemplate(this.templateId).subscribe({
      next: (template) => {
        this.template = template;
      },
      error: (error) => {
        console.error('Error loading template:', error);
        this.errorMessage = 'Failed to load template';
      }
    });
  }

  loadEmployees(): void {
    this.templateService.getEmployees().subscribe({
      next: (employees: any[]) => {
        this.employees = employees;
      },
      error: (error: any) => {
          console.error('Error loading employees:', error);
          this.errorMessage = 'Failed to load employees';
        }
    });
  }

  loadDepartments(): void {
    this.templateService.getDepartments().subscribe({
      next: (departments) => {
        this.departments = departments;
      },
      error: (error) => {
        console.error('Error loading departments:', error);
        this.errorMessage = 'Failed to load departments';
      }
    });
  }

  onDepartmentChange(): void {
    const departmentId = this.assignForm.get('departmentId')?.value;
    if (departmentId) {
      // Filter employees by department
      this.templateService.getEmployeesByDepartment(departmentId).subscribe({
        next: (employees: any[]) => {
          this.employees = employees;
        },
        error: (error: any) => {
          console.error('Error loading employees:', error);
        }
      });
    } else {
      // Load all employees
      this.loadEmployees();
    }
    
    // Reset employee selection
    this.assignForm.get('employeeId')?.setValue('');
  }

  onSubmit(): void {
    if (this.assignForm.valid && this.template) {
      this.isLoading = true;
      this.errorMessage = '';
      this.successMessage = '';
      
      const formValue = this.assignForm.value;
      const assignmentData = {
        templateId: this.templateId,
        employeeId: formValue.employeeId,
        dueDate: formValue.dueDate,
        priority: formValue.priority,
        notes: formValue.notes
      };

      this.templateService.assignTemplate(assignmentData).subscribe({
        next: (response) => {
          console.log('Template assigned successfully:', response);
          this.successMessage = 'Template assigned successfully!';
          this.isLoading = false;
          
          // Reset form
          this.assignForm.reset({
            priority: 'MEDIUM'
          });
          
          // Navigate back after a short delay
          setTimeout(() => {
            this.router.navigate(['/templates']);
          }, 2000);
        },
        error: (error) => {
          console.error('Error assigning template:', error);
          this.errorMessage = 'Failed to assign template. Please try again.';
          this.isLoading = false;
        }
      });
    } else {
      this.markFormGroupTouched();
    }
  }

  markFormGroupTouched(): void {
    Object.keys(this.assignForm.controls).forEach(key => {
      const control = this.assignForm.get(key);
      control?.markAsTouched();
    });
  }

  getSelectedEmployee(): HrUser | null {
    const employeeId = this.assignForm.get('employeeId')?.value;
    if (!employeeId) return null;
    return this.employees.find(emp => emp.id.toString() === employeeId.toString()) || null;
  }

  getSelectedDepartment(): Department | null {
    const departmentId = this.assignForm.get('departmentId')?.value;
    return this.departments.find(dept => dept.id === Number(departmentId)) || null;
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

  getTotalHours(): number {
    if (!this.template?.tasks) return 0;
    return this.template.tasks.reduce((total, task) => total + (task.estimatedHours || 0), 0);
  }

  getTomorrowDate(): string {
    const tomorrow = new Date();
    tomorrow.setDate(tomorrow.getDate() + 1);
    return tomorrow.toISOString().split('T')[0];
  }

  logout(): void {
    // Implement logout logic
    this.router.navigate(['/auth/login']);
  }
}