import { Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { Store } from '@ngrx/store';
import { Observable } from 'rxjs';
import { selectUser } from '../../auth/store/auth.selectors';
import { UserType } from '../../auth/model/auth.model';
import { AuthDebugService } from '../../auth/service/auth-debug.service';
import * as AuthActions from '../../auth/store/auth.actions';

@Component({
  selector: 'userdashboard',
  imports: [CommonModule],
  templateUrl: './userdashboard.component.html',
  styleUrls: ['./userdashboard.component.css']
})

export class UserDashboardComponent implements OnInit {
  private store = inject(Store);
  private router = inject(Router);
  private authDebugService = inject(AuthDebugService);
  
  user$: Observable<UserType | null> = this.store.select(selectUser);
  currentUser: UserType | null = null;

  ngOnInit() {
    // Debug auth state
    console.log('UserDashboard component initializing...');
    this.authDebugService.checkAuthState();
    
    this.user$.subscribe(user => {
      this.currentUser = user;
      console.log('UserDashboard component - current user:', this.currentUser);
    });
  }

  navigateToDashboard() {
    this.router.navigate(['/userdashboard']);
  }

  navigateToTasks() {
    this.router.navigate(['/userdashboard/tasks']);
  }

  navigateToDocuments() {
    this.router.navigate(['/userdashboard/documents']);
  }

  navigateToResources() {
    this.router.navigate(['/userdashboard/resources']);
  }

  navigateToEvents() {
    this.router.navigate(['/userdashboard/events']);
  }

  navigateToSettings() {
    // TODO: Implement settings navigation when settings component is available
    console.log('Settings navigation - to be implemented');
  }

  logout() {
    this.store.dispatch(AuthActions.LOGOUT());
    this.router.navigate(['/login']);
  }
}
