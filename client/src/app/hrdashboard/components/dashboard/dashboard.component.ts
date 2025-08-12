import { Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { Store } from '@ngrx/store';
import { Observable } from 'rxjs';
import { selectUser } from '../../../auth/store/auth.selectors';
import { UserType } from '../../../auth/model/auth.model';
import * as AuthActions from '../../../auth/store/auth.actions';
// import { FullCalendarModule } from '@fullcalendar/angular';
// import dayGridPlugin from '@fullcalendar/daygrid';
// import { CalendarOptions } from '@fullcalendar/core';



@Component({
  selector: 'dashboard',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.css']
})
export class DashboardComponent implements OnInit {
  private router = inject(Router);
  private store = inject(Store);

  user$: Observable<UserType | null> = this.store.select(selectUser);
  currentUser: UserType | null = null;

  ngOnInit() {


    this.user$.subscribe(user => {
      this.currentUser = user;
      console.log('Dashboard component - current user:', this.currentUser);
    });
  }

  // calendarOptions: CalendarOptions = {
  //   plugins: [ dayGridPlugin ],
  //   initialView: 'dayGridMonth',
  //   height: '100%',
  //   headerToolbar: {
  //     left: 'prev,next today',
  //     center: 'title',
  //     right: ''
  //   },
  //   events: [
  //     { title: 'Formalities In‑Progress', date: '2025-01-31' },
  //     { title: '1:1 w/ Jasmine',     date: '2025-01-31' },
  //     { title: 'Template Review',     date: '2025-01-31' },
  //     { title: 'Recruitment Summary', date: '2025-02-01' }
  //   ]
  // };

  navigateToOnboardNewHires() {
    this.router.navigate(['/dashboard/onboardnewhires']);
  }

  navigateToNewHires() {
    this.router.navigate(['/dashboard/newhires']);
  }

  navigateToTemplates() {
    this.router.navigate(['/templates']);
  }

  logout() {
    this.store.dispatch(AuthActions.LOGOUT());
    //clear localStorage
    localStorage.removeItem('token');
    localStorage.removeItem('user');
    localStorage.removeItem('auth');
    console.log('User logged out. Redirecting to login page...');
    // Redirect to login page
    this.router.navigate(['/login']);
  }

}
