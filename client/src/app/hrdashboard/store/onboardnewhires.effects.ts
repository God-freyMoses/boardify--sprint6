import { Injectable, inject } from '@angular/core';
import { Actions, createEffect, ofType } from '@ngrx/effects';
import { NewHireService } from '../service/newhire.service';
import * as NewHireActions from './onboardnewhires.actions';
import { map, catchError, exhaustMap, tap } from 'rxjs/operators';
import { of } from 'rxjs';
import { Router } from '@angular/router';

@Injectable()
export class NewHireEffects {
  private actions$ = inject(Actions);
  private svc    = inject(NewHireService);
  private router = inject(Router);

  loadAll$ = createEffect(() =>
    this.actions$.pipe(
      ofType(NewHireActions.LOAD_NEW_HIRES),
      exhaustMap(() =>
        this.svc.getNewHires().pipe(
          map(result => NewHireActions.LOAD_NEW_HIRES_SUCCESS({ hires: result.data })),
          catchError(error => of(NewHireActions.LOAD_NEW_HIRES_FAILURE({ error })))
        )
      )
    )
  );

  add$ = createEffect(() =>
    this.actions$.pipe(
      ofType(NewHireActions.ADD_NEW_HIRE),
      exhaustMap(action =>
        this.svc.addNewHire(action.hire).pipe(
          map(result => NewHireActions.ADD_NEW_HIRE_SUCCESS({ hire: result.data })),
          catchError(error => of(NewHireActions.ADD_NEW_HIRE_FAILURE({ error })))
        )
      )
    )
  );

  addSuccess$ = createEffect(
    () =>
      this.actions$.pipe(
        ofType(NewHireActions.ADD_NEW_HIRE_SUCCESS),
        tap(() => this.router.navigate(['/dashboard/newhires']))
      ),
    { dispatch: false }
  );
}
