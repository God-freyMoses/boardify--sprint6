import { ApplicationConfig, provideZoneChangeDetection, isDevMode } from '@angular/core';
import { provideRouter } from '@angular/router';

import { routes } from './app.routes';
import {ActionReducer, provideState, provideStore} from '@ngrx/store';
import { provideEffects } from '@ngrx/effects';
import { provideStoreDevtools } from '@ngrx/store-devtools';
import {provideHttpClient, withInterceptors} from '@angular/common/http';
import { localStorageSync } from 'ngrx-store-localstorage';
import {authInterceptor} from './auth/interceptor/auth.interceptor';

import * as fromAuth from './auth/store/auth.reducer';
import { AuthEffects } from './auth/store/auth.effects';

import * as fromNewHires from './hrdashboard/store/onboardnewhires.reducer';
import { NewHireEffects }      from './hrdashboard/store/onboardnewhires.effects';

function authLocalStorageSyncReducer(
  reducer: ActionReducer<any>
): ActionReducer<any> {
  return localStorageSync({
    keys: [fromAuth.authFeatureKey],
    rehydrate: true,
    storage: window.localStorage,
  })(reducer);
}

const metaReducers = [authLocalStorageSyncReducer];

export const appConfig: ApplicationConfig = {
  providers: [
    provideHttpClient(withInterceptors([authInterceptor])),
    provideZoneChangeDetection({ eventCoalescing: true }),
    provideRouter(routes),
    provideStore({}, { metaReducers }),
    provideState(fromAuth.authFeatureKey, fromAuth.authReducer),
    provideState(fromNewHires.newHireFeatureKey, fromNewHires.newHireReducer),
    provideEffects([AuthEffects, NewHireEffects]),
    provideStoreDevtools({ maxAge: 25, logOnly: !isDevMode() })
  ]
};
