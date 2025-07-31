import { Injectable, inject } from '@angular/core';
import { Store } from '@ngrx/store';
import * as AuthActions from '../store/auth.actions';

@Injectable({
  providedIn: 'root'
})
export class AuthInitService {
  private store = inject(Store);

  initializeAuth() {
    console.log('Initializing auth state from localStorage...');
    this.store.dispatch(AuthActions.INITIALIZE_AUTH());
  }
}