import { Route } from '@angular/router';
import {PlanComponent} from './plan.component';

// LAZY LOAD COMPONENTS

export const PLAN_ROUTES: Route[] = [
  {
    path: 'plan', loadComponent: () => import('./plan.component').then(m => m.PlanComponent)
  }
];
