import {Route} from '@angular/router';
import {EventPageComponent} from './eventpage.component';

// LAZY LOAD COMPONENTS

export const EVENTPAGE_ROUTES: Route[] = [
  {path: 'eventpage', loadComponent: () => import('./eventpage.component').then(m => m.EventPageComponent)}
];
