import { Injectable, inject } from '@angular/core';
import { Actions, createEffect, ofType } from '@ngrx/effects';
import {EMPTY, of, tap} from 'rxjs';
import { map, exhaustMap, catchError } from 'rxjs/operators';
import { AuthService } from '../service/auth.service';
import * as AuthActions from './auth.actions';
import {Router} from '@angular/router';

@Injectable()
export class AuthEffects {
  private actions$ = inject(Actions);
  private authService = inject(AuthService);
  private router = inject(Router)


  register$ = createEffect(() => {
    return this.actions$.pipe(
      ofType(AuthActions.REGISTER),
      tap(action => console.log('REGISTER action received:', action)),
      exhaustMap(action =>
        this.authService.register(action.credentials).pipe(
          tap(result => console.log('Registration successful:', result)),
          map(result => AuthActions.REGISTER_SUCCESS({data: result.data})),
          catchError(error => {
            console.error('Registration error:', error);
            const errorMessage = error.error?.message || error.message || 'Registration failed';
            return of(AuthActions.REGISTER_FAILURE({error: errorMessage}));
          })
        )
      )
    );
  });



  registerSuccess$ = createEffect(() => {
    return this.actions$.pipe(ofType(AuthActions.REGISTER_SUCCESS),
      tap(data => {
        console.log('REGISTER_SUCCESS received, navigating to login:', data);
        this.router.navigate(['/login']).then(success => {
          console.log('Navigation to login successful:', success);
        }).catch(error => {
          console.error('Navigation to login failed:', error);
        });
      }),
    );
  }, { dispatch: false });



  login$ = createEffect(() => {
    return this.actions$.pipe(
      ofType(AuthActions.LOGIN),
      exhaustMap(action =>
        this.authService.login(action.credentials).pipe(
          map(result => AuthActions.LOGIN_SUCCESS({data: result.data})),
          catchError(error => {
            const errorMessage = error.error?.message || error.message || 'Login failed';
            return of(AuthActions.LOGIN_FAILURE({error: errorMessage}));
          })
        )
      )
    );
  });


  loginSuccess$ = createEffect(() => {
    return this.actions$.pipe(ofType(AuthActions.LOGIN_SUCCESS),
      tap(data => {
        // Store token in localStorage
        localStorage.setItem('token', data.data.token);
        localStorage.setItem('user', JSON.stringify(data.data.user));

        if (data.data.user.role === 'HR_MANAGER') {
          this.router.navigate(['/dashboard']);
        } else if (data.data.user.role === 'NEW_HIRE') {
          this.router.navigate(['/userdashboard']);
        }
      }),
    );
  }, { dispatch: false });

  initializeAuth$ = createEffect(() => {
    return this.actions$.pipe(
      ofType(AuthActions.INITIALIZE_AUTH),
      map(() => {
        const token = localStorage.getItem('token');
        const userStr = localStorage.getItem('user');
        
        if (token && userStr) {
          try {
            const user = JSON.parse(userStr);
            return AuthActions.LOAD_USER_FROM_STORAGE({ user, token });
          } catch (error) {
            console.error('Error parsing user from localStorage:', error);
            localStorage.removeItem('token');
            localStorage.removeItem('user');
          }
        }
        
        return { type: 'NO_ACTION' };
      })
    );
  });


}
