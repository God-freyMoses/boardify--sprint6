import { Route } from '@angular/router';

export const TEMPLATES_ROUTES: Route[] = [
  {
    path: 'templates',
    loadComponent: () => import('./components/template-list/template-list.component').then(m => m.TemplateListComponent)
  },
  {
    path: 'templates/create',
    loadComponent: () => import('./components/template-create/template-create.component').then(m => m.TemplateCreateComponent)
  },
  {
    path: 'templates/:id/edit',
    loadComponent: () => import('./components/template-edit/template-edit.component').then(m => m.TemplateEditComponent)
  },
  {
    path: 'templates/:id/assign',
    loadComponent: () => import('./components/template-assign/template-assign.component').then(m => m.TemplateAssignComponent)
  },
  {
    path: 'onboarding/:templateId',
    loadComponent: () => import('./components/onboarding-progress/onboarding-progress.component').then(m => m.OnboardingProgressComponent)
  }
];