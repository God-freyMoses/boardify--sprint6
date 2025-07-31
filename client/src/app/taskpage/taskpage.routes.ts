import {Route} from '@angular/router';
import {TaskPageComponent} from './taskpage.component';

// LAZY LOAD COMPONENTS

export const TASKPAGE_ROUTES: Route[] = [
  {path: 'taskpage', loadComponent: () => import('./taskpage.component').then(m => m.TaskPageComponent)}
];
