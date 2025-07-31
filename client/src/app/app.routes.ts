import { Routes } from '@angular/router';
import {AUTH_ROUTES} from './auth/auth.routes';
import {HRDASHBOARD_ROUTES} from './hrdashboard/hrdashboard.routes';
import {HOME_ROUTES} from './home/home.routes';
import {USERDASHBOARD_ROUTES} from './userdashboard/userdashboard.routes';
import {TASKPAGE_ROUTES} from './taskpage/taskpage.routes';
import {USERDOCUMENTS_ROUTES} from './userdocuments/userdocuments.routes';
import {OPENDOCUMENTS_ROUTES} from './opendocuments/opendocuments.routes';

import {PLAN_ROUTES} from './plan/plan.routes';
import {PAYMENTPAGE_ROUTES} from './paymentpage/paymentpage.routes';
import {RESOURCES_ROUTES} from './resourcespage/resources.routes';

import {EVENTPAGE_ROUTES} from './eventpage/eventpage.routes';




export const routes: Routes = [
  ...AUTH_ROUTES,
  ...HRDASHBOARD_ROUTES,
  ...HOME_ROUTES,
  ...USERDASHBOARD_ROUTES,
  ...TASKPAGE_ROUTES,
  ...USERDOCUMENTS_ROUTES,
  ...OPENDOCUMENTS_ROUTES,

  ...PLAN_ROUTES,
  ...PAYMENTPAGE_ROUTES,
  ...RESOURCES_ROUTES,

  ...EVENTPAGE_ROUTES,

  {path: '', redirectTo: 'home', pathMatch: 'full'},
  {path: 'login', redirectTo: 'login', pathMatch: 'full'},
  {path: '404', loadComponent: () => import('./share/components/notfound.component').then(m => m.NotFoundComponent)},
  {path: '**', redirectTo: '404', pathMatch: 'full'},
  {path: '', redirectTo: 'user-dash', pathMatch: 'full'},
  {path: '', redirectTo: 'app-userdocuments', pathMatch: 'full'},
  {path: '', redirectTo: 'app-opendocuments', pathMatch: 'full'},

  {path: '', redirectTo: 'app-plan', pathMatch: 'full'},
  {path: 'paymentpage', redirectTo: 'app-paymentpage', pathMatch: 'full'},
  {path: 'resources', redirectTo: 'app-resources page', pathMatch: 'full'},

  {path: '', redirectTo: 'app-eventpage', pathMatch: 'full'},



];
