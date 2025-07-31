import {Route} from '@angular/router';

// LAZY LOAD COMPONENTS

export const USERDASHBOARD_ROUTES: Route[] = [
  {
    path: 'userdashboard',
    loadComponent: () => import('./components/userdashboard.component').then(m => m.UserDashboardComponent)
  },
  {
    path: 'userdashboard/tasks',
    loadComponent: () => import('../taskpage/taskpage.component').then(m => m.TaskPageComponent)
  },
  {
    path: 'userdashboard/documents',
    loadComponent: () => import('../userdocuments/userdocuments.component').then(m => m.UserdocumentsComponent)
  },
  {
    path: 'userdashboard/resources',
    loadComponent: () => import('../resourcespage/resources.component').then(m => m.ResourcesComponent)
  },
  {
    path: 'userdashboard/events',
    loadComponent: () => import('../eventpage/eventpage.component').then(m => m.EventPageComponent)
  }
];
