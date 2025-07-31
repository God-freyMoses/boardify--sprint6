import { Component, OnInit, inject } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { AuthService } from '../../service/auth.service';
import { RegisterRequestType } from '../../model/auth.model';

@Component({
  selector: 'app-register',
  imports: [CommonModule, ReactiveFormsModule, RouterModule],
  templateUrl: './register.component.html',
  styleUrls: ['./register.component.css']
})
export class RegisterComponent implements OnInit {
  private fb = inject(FormBuilder);
  private authService = inject(AuthService);
  private router = inject(Router);

  form!: FormGroup;
  showPassword = false;
  isLoading = false;
  errorMessage = '';

  constructor() {}

  ngOnInit(): void {
    this.form = this.fb.group({
      firstName: ['', [Validators.required]],
      lastName: ['', [Validators.required]],
      email: ['', [Validators.required, Validators.email]],
      companyName: ['', [Validators.required]],
      password: ['', [Validators.required, Validators.minLength(6)]],
      confirmPassword: ['', [Validators.required]]
    }, { validators: this.matchPasswords });
  }

  matchPasswords(formGroup: FormGroup) {
    const password = formGroup.get('password')?.value;
    const confirmPassword = formGroup.get('confirmPassword')?.value;
    return password === confirmPassword ? null : { mismatch: true };
  }

  togglePassword(): void {
    this.showPassword = !this.showPassword;
  }

  onSubmit(): void {
    if (this.form.invalid) {
      this.markFormGroupTouched();
      return;
    }

    this.isLoading = true;
    this.errorMessage = '';

    const { firstName, lastName, email, password, companyName } = this.form.value;
    const credentials: RegisterRequestType = {
      firstName,
      lastName,
      email,
      password,
      companyName,
      role: 'HR_MANAGER'
    };

    this.authService.register(credentials).subscribe({
      next: (response) => {
        console.log('Registration successful:', response);
        this.isLoading = false;
        // Navigate to login page on success
        this.router.navigate(['/login']);
      },
      error: (error) => {
        console.error('Registration failed:', error);
        this.isLoading = false;
        this.errorMessage = error.error?.message || 'Registration failed. Please try again.';
      }
    });
  }

  private markFormGroupTouched(): void {
    Object.keys(this.form.controls).forEach(key => {
      const control = this.form.get(key);
      control?.markAsTouched();
    });
  }

  // Helper methods for template
  isFieldInvalid(fieldName: string): boolean {
    const field = this.form.get(fieldName);
    return !!(field && field.invalid && field.touched);
  }

  getFieldError(fieldName: string): string {
    const field = this.form.get(fieldName);
    if (field?.errors) {
      if (field.errors['required']) return `${fieldName} is required`;
      if (field.errors['email']) return 'Enter a valid email';
      if (field.errors['minlength']) return 'Must be at least 6 characters';
    }
    return '';
  }

  get passwordMismatch(): boolean {
    return this.form.errors?.['mismatch'] &&
           this.form.get('confirmPassword')?.touched || false;
  }

}
