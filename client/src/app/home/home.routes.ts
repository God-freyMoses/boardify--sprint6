import { Route } from '@angular/router';

// LAZY LOAD COMPONENTS

export const HOME_ROUTES: Route[] = [
  {
    path: 'home', loadComponent: () => import('./home.component').then(m => m.HomeComponent)
  }
];
