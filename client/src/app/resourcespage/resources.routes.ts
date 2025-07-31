import { Route } from '@angular/router';
import {ResourcesComponent} from './resources.component';

// LAZY LOAD COMPONENTS

export const RESOURCES_ROUTES: Route[] = [
  {
    path: 'resources', loadComponent: () => import('./resources.component').then(m => m.ResourcesComponent)
  }
];
