import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { HttpClient } from '@angular/common/http';

@Component({
  selector: 'app-login',
  imports: [CommonModule, ReactiveFormsModule, RouterModule],
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.css']
})
export class LoginComponent {
  private fb = inject(FormBuilder);
  private router = inject(Router);
  private http = inject(HttpClient);
  
  loginForm: FormGroup;
  isLoading = false;
  errorMessage = '';
  showPassword = false;

  constructor() {
    this.loginForm = this.fb.group({
      email: ['', [Validators.required, Validators.email]],
      password: ['', [Validators.required, Validators.minLength(6)]]
    });
  }

  togglePassword() {
    this.showPassword = !this.showPassword;
  }

  onSubmit() {
    if (this.loginForm.valid && !this.isLoading) {
      this.isLoading = true;
      this.errorMessage = '';

      const loginData = {
        email: this.loginForm.value.email,
        password: this.loginForm.value.password
      };

      this.http.post<any>('http://localhost:8080/api/users/login', loginData)
        .subscribe({
          next: (response) => {
            this.isLoading = false;
            if (response.success && response.data) {
              // Store token and user data
              localStorage.setItem('token', response.data.token);
              localStorage.setItem('user', JSON.stringify(response.data.user));
              
              // Navigate based on role
              if (response.data.user.role === 'HR_MANAGER') {
                this.router.navigate(['/dashboard']);
              } else if (response.data.user.role === 'NEW_HIRE') {
                this.router.navigate(['/userdashboard']);
              } else {
                this.router.navigate(['/dashboard']);
              }
            }
          },
          error: (error) => {
            this.isLoading = false;
            this.errorMessage = error.error?.message || 'Login failed. Please try again.';
            console.error('Login error:', error);
          }
        });
    }
  }

  get email() { return this.loginForm.get('email'); }
  get password() { return this.loginForm.get('password'); }
}
