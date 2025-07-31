import { createReducer, on } from '@ngrx/store';
import * as NewHireActions from './onboardnewhires.actions';
import { NewHireType } from '../model/NewHire.model';

export interface NewHireState {
  hires: NewHireType[];
  loading: boolean;
  error: any;
}

export const newHireFeatureKey = 'newHires';

export const initialState: NewHireState = {
  hires: [],
  loading: false,
  error: null
};

export const newHireReducer = createReducer(
  initialState,

  // Load all
  on(NewHireActions.LOAD_NEW_HIRES, state => ({
    ...state,
    loading: true,
    error: null
  })),
  on(NewHireActions.LOAD_NEW_HIRES_SUCCESS, (state, { hires }) => ({
    ...state,
    hires,
    loading: false
  })),
  on(NewHireActions.LOAD_NEW_HIRES_FAILURE, (state, { error }) => ({
    ...state,
    loading: false,
    error
  })),

  // Add one
  on(NewHireActions.ADD_NEW_HIRE, state => ({
    ...state,
    loading: true,
    error: null
  })),
  on(NewHireActions.ADD_NEW_HIRE_SUCCESS, (state, { hire }) => ({
    ...state,
    hires: [...state.hires, hire],
    loading: false
  })),
  on(NewHireActions.ADD_NEW_HIRE_FAILURE, (state, { error }) => ({
    ...state,
    loading: false,
    error
  }))
);
