import {inject, Injectable} from '@angular/core';
import {environment} from '../../../environments/environment';
import {HttpClient, HttpErrorResponse} from '@angular/common/http';
import {ResultType} from '../../share/model/share.model';
import {LoginRequestType, RegisterRequestType, UserTokenType, UserType} from '../model/auth.model';
import {catchError, Observable, throwError} from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  api: string = environment.server;

  http: HttpClient = inject(HttpClient);

  // REGISTER

  register(credentials: RegisterRequestType): Observable<ResultType<UserType>> {
    return this.http.post<ResultType<UserType>>(`${this.api}/api/users/register/hr`, credentials).pipe(
      catchError(this.handleError)
    );
  }



  // LOGIN
  login(credentials: LoginRequestType): Observable<ResultType<UserTokenType>> {
    return this.http.post<ResultType<UserTokenType>>(`${this.api}/api/users/login`, credentials).pipe(
      catchError(this.handleError)
    );
  }

  // ERROR HANDLER

  private handleError(error: HttpErrorResponse) {
    let errorMessage = 'An unknown error occurred!';

    if (error.error instanceof ErrorEvent) {
      // Client-side error
      errorMessage = `Client-side error: ${error.error.message}`;
    } else {
      // Server-side error
      if (error.error && error.error.message) {
        errorMessage = error.error.message;
      } else {
        errorMessage = `Server error (${error.status}): ${error.message}`;
      }
    }

    console.error('Auth Service Error:', errorMessage);
    return throwError(() => error);
  }
}
