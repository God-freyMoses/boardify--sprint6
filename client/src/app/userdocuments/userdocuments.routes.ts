import { Route } from '@angular/router';
import {UserdocumentsComponent} from './userdocuments.component';

// LAZY LOAD COMPONENTS

export const USERDOCUMENTS_ROUTES: Route[] = [
  {
    path: 'userdocuments', loadComponent: () => import('./userdocuments.component').then(m => m.UserdocumentsComponent)
  }
];
