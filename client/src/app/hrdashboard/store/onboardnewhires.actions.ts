import { createAction, props } from '@ngrx/store';
import { NewHireType, ResultType } from '../model/NewHire.model';


// LOAD (GET) ALL NEW HIRES
export const LOAD_NEW_HIRES = createAction('[NewHire] Load All');
export const LOAD_NEW_HIRES_SUCCESS = createAction(
  '[NewHire] Load All Success',
  props<{ hires: NewHireType[] }>()
);
export const LOAD_NEW_HIRES_FAILURE = createAction(
  '[NewHire] Load All Failure',
  props<{ error: any }>()
);

// ADD (POST) A NEW HIRE
export const ADD_NEW_HIRE = createAction(
  '[NewHire] Add',
  props<{ hire: NewHireType }>()
);
export const ADD_NEW_HIRE_SUCCESS = createAction(
  '[NewHire] Add Success',
  props<{ hire: NewHireType }>()
);
export const ADD_NEW_HIRE_FAILURE = createAction(
  '[NewHire] Add Failure',
  props<{ error: any }>()
);
