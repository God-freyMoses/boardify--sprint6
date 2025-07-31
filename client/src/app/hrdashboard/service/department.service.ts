import { inject, Injectable } from '@angular/core';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { catchError, Observable, throwError } from 'rxjs';
import { environment } from '../../../environments/environment';

export interface Department {
  id: number;
  name: string;
}

export interface DepartmentResult {
  success: boolean;
  message: string;
  statusCode: number;
  data: Department[];
}

@Injectable({
  providedIn: 'root'
})
export class DepartmentService {
  api: string = environment.server;
  http: HttpClient = inject(HttpClient);

  /** GET ALL DEPARTMENTS */
  getDepartments(): Observable<DepartmentResult> {
    return this.http.get<DepartmentResult>(
      `${this.api}/api/departments`
    ).pipe(
      catchError(this.handleError)
    );
  }

  /** ERROR HANDLER */
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

    console.error('Department Service Error:', errorMessage);
    return throwError(() => error);
  }
}