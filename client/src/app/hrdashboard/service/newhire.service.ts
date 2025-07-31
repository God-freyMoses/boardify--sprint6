import {inject, Injectable} from '@angular/core';
import {HttpClient, HttpErrorResponse} from '@angular/common/http';
import {catchError, Observable, throwError} from 'rxjs';
import {environment} from '../../../environments/environment';
import {NewHireType, ResultType} from '../model/NewHire.model';



@Injectable({
  providedIn: 'root'
})
export class NewHireService {
  api: string = environment.server;
  http: HttpClient = inject(HttpClient);

  /** ADD NEW HIRE */
  addNewHire(newHire: NewHireType): Observable<ResultType<NewHireType>> {
    return this.http.post<ResultType<NewHireType>>(
      `${this.api}/api/users/register/hire`,
      newHire
    ).pipe(
      catchError(this.handleError)
    );
  }

  /** GET ALL NEW HIRE */
  getNewHires(): Observable<ResultType<NewHireType[]>> {
    return this.http.get<ResultType<NewHireType[]>>(
      `${this.api}/api/users/hires`,
    ).pipe(
      catchError(this.handleError)
    );
  }

  /** ERROR HANDLER (same as AuthService) */
  private handleError(error: HttpErrorResponse) {
    let errorMessage = 'An unknown error occurred!';

    if (error.error instanceof ErrorEvent) {
      errorMessage = `Client-side error: ${error.error.message}`;
    } else {
      if (error.error && error.error.message) {
        errorMessage = error.error.message;
      } else {
        errorMessage = `Server error (${error.status}): ${error.message}`;
      }
    }

    console.error('Employee Service Error:', errorMessage);
    return throwError(() => error);
  }
}
