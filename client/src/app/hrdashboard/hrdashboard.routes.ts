import {Route} from '@angular/router';
import {NewhiresComponent} from './components/newhires/newhires.component';

// LAZY LOAD COMPONENTS

export const HRDASHBOARD_ROUTES: Route[] = [
  {path: 'dashboard', loadComponent: () => import('./components/dashboard/dashboard.component').then(m => m.DashboardComponent)},
  {path: 'dashboard/newhires', loadComponent: () => import('./components/newhires/newhires.component').then(m => m.NewhiresComponent)},
  {path: 'dashboard/onboardnewhires', loadComponent: () => import('./components/onboardnewhires/onboardnewhires.component').then(m => m.OnboardnewhiresComponent)}
];
