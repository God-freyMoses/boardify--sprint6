import { Injectable, inject } from '@angular/core';
import { Store } from '@ngrx/store';
import { selectUser, selectToken, selectIsLoggedIn } from '../store/auth.selectors';
import { Observable } from 'rxjs';
import { UserType } from '../model/auth.model';

@Injectable({
  providedIn: 'root'
})
export class AuthDebugService {
  private store = inject(Store);

  user$: Observable<UserType | null> = this.store.select(selectUser);
  token$: Observable<string | null> = this.store.select(selectToken);
  isLoggedIn$: Observable<boolean> = this.store.select(selectIsLoggedIn);

  checkAuthState() {
    console.log('=== AUTH STATE DEBUG ===');
    
    // Check localStorage
    const localStorageToken = localStorage.getItem('token');
    const localStorageUser = localStorage.getItem('user');
    const localStorageAuth = localStorage.getItem('auth');
    
    console.log('localStorage token:', localStorageToken);
    console.log('localStorage user:', localStorageUser);
    console.log('localStorage auth state:', localStorageAuth);
    
    // Check NgRx store
    this.user$.subscribe(user => {
      console.log('NgRx store user:', user);
    }).unsubscribe();
    
    this.token$.subscribe(token => {
      console.log('NgRx store token:', token);
    }).unsubscribe();
    
    this.isLoggedIn$.subscribe(isLoggedIn => {
      console.log('NgRx store isLoggedIn:', isLoggedIn);
    }).unsubscribe();
    
    console.log('=== END AUTH DEBUG ===');
  }

  getCurrentUser(): UserType | null {
    let currentUser: UserType | null = null;
    this.user$.subscribe(user => {
      currentUser = user;
    }).unsubscribe();
    return currentUser;
  }
}