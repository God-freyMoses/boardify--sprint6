import { Route } from '@angular/router';
import {OpendocumentsComponent} from './opendocuments.component';

// LAZY LOAD COMPONENTS

export const OPENDOCUMENTS_ROUTES: Route[] = [
  {
    path: 'opendocuments', loadComponent: () => import('./opendocuments.component').then(m => m.OpendocumentsComponent)
  }
];
