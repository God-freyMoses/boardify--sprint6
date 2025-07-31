import { Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { Store } from '@ngrx/store';
import { Observable } from 'rxjs';
import * as NewHireActions from '../../store/onboardnewhires.actions';
import { selectAllNewHires, selectNewHiresLoading } from '../../store/onboardnewhires.selectors';
import * as AuthActions from '../../../auth/store/auth.actions';

interface Employee {
  id?: string;
  firstName: string;
  lastName: string;
  email: string;
  title: string;
  gender: string;
  pictureUrl: string;
  departmentName: string;
  hrManagerName: string;
  createdAt: string;
}

@Component({
  selector: 'newhires',
  standalone: true,
  imports: [ CommonModule, FormsModule ],
  templateUrl: './newhires.component.html',
  styleUrls: ['./newhires.component.css']
})
export class NewhiresComponent implements OnInit {
  private router = inject(Router);
  private store = inject(Store);
  filterText = '';

  employees$: Observable<any[]> = this.store.select(selectAllNewHires);
  loading$: Observable<boolean> = this.store.select(selectNewHiresLoading);
  employees: Employee[] = [];

  ngOnInit() {
    this.store.dispatch(NewHireActions.LOAD_NEW_HIRES());
    this.employees$.subscribe(hires => {
      this.employees = hires;
    });
  }

  get filteredEmployees(): Employee[] {
    const ft = this.filterText.toLowerCase().trim();
    if (!ft) return this.employees;
    return this.employees.filter(e =>
      (e.firstName + ' ' + e.lastName).toLowerCase().includes(ft) ||
      e.title.toLowerCase().includes(ft) ||
      e.departmentName?.toLowerCase().includes(ft)
    );
  }

  navigateToOnboardNewHires() {
    this.router.navigate(['/dashboard/onboardnewhires']);
  }

  navigateToDashboard() {
    this.router.navigate(['/dashboard']);
  }

  logout() {
    this.store.dispatch(AuthActions.LOGOUT());
    this.router.navigate(['/login']);
  }
}

