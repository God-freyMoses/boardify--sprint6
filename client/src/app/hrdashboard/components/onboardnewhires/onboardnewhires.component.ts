import { Component , inject, OnInit} from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { RouterLink, Router } from '@angular/router';
import { Store } from '@ngrx/store';
import * as NewHireActions from '../../store/onboardnewhires.actions';
import {selectNewHiresLoading, selectNewHiresError } from '../../store/onboardnewhires.selectors';
import { DepartmentService, Department } from '../../service/department.service';
import * as AuthActions from '../../../auth/store/auth.actions';

@Component({
  selector: 'onboardnewhires',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  templateUrl: './onboardnewhires.component.html',
  styleUrls: ['./onboardnewhires.component.css']
})
export class OnboardnewhiresComponent implements OnInit {

  private store = inject(Store);
  private router = inject(Router);
  private departmentService = inject(DepartmentService);

  step = 1;
  form: FormGroup;
  departments: Department[] = [];
  loadingDepartments = false;

  loading$ = this.store.select(selectNewHiresLoading);
  error$   = this.store.select(selectNewHiresError);

  constructor(private fb: FormBuilder) {
    this.form = this.fb.group({
// Step 1: Basic Details
      firstName: ['', Validators.required],
      lastName: ['', Validators.required],
      email: ['', [Validators.required, Validators.email]],
      departmentId: ['', Validators.required], // Department name
// Step 2: Role & Others
      role: ['', Validators.required],
      gender: ['', Validators.required],
      title: ['', Validators.required],
      pictureUrl: [''],
      password: ['']
// Step 3: Review & Submit (no additional fields)

    });
  }

  ngOnInit() {
    this.loadDepartments();
  }

  loadDepartments() {
    this.loadingDepartments = true;
    this.departmentService.getDepartments().subscribe({
      next: (result) => {
        if (result.success) {
          this.departments = result.data;
        }
        this.loadingDepartments = false;
      },
      error: (error) => {
        console.error('Error loading departments:', error);
        this.loadingDepartments = false;
      }
    });
  }

  next() {
    if (this.step < 3) {
      this.step++;
    }
  }

  back() {
    if (this.step > 1) {
      this.step--;
    }
  }

  generatePassword() {
    const characters = 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789';
    let password = '';
    for (let i = 0; i < 8; i++) {
      password += characters.charAt(Math.floor(Math.random() * characters.length));
    }
    this.form.get('password')?.setValue(password);
  }

  onFileSelected(event: Event) {
    const input = event.target as HTMLInputElement;
    if (input.files && input.files.length > 0) {
      const file = input.files[0];
      // You can store or upload the file here
      console.log('Selected file:', file.name);
    }
  }



  submit() {
    if (this.form.valid) {
      this.store.dispatch(
        NewHireActions.ADD_NEW_HIRE({ hire: this.form.value })
      );
    } else {
      this.form.markAllAsTouched();
    }
  }

  navigateToDashboard() {
    this.router.navigate(['/dashboard']);
  }

  navigateToNewHires() {
    this.router.navigate(['/dashboard/newhires']);
  }

  logout() {
    this.store.dispatch(AuthActions.LOGOUT());
    this.router.navigate(['/login']);
  }
}
