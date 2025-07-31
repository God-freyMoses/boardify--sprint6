import { Route } from '@angular/router';
import {PaymentpageComponent} from './paymentpage.component';

// LAZY LOAD COMPONENTS

export const PAYMENTPAGE_ROUTES: Route[] = [
  {
    path: 'paymentpage', loadComponent: () => import('./paymentpage.component').then(m => m.PaymentpageComponent)
  }
];
