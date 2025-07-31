import { createFeatureSelector, createSelector } from '@ngrx/store';
import { NewHireState, newHireFeatureKey } from './onboardnewhires.reducer';

// Feature selector
export const selectNewHireState =
  createFeatureSelector<NewHireState>(newHireFeatureKey);

// List of hires
export const selectAllNewHires = createSelector(
  selectNewHireState,
  state => state.hires
);

// Loading flag
export const selectNewHiresLoading = createSelector(
  selectNewHireState,
  state => state.loading
);

// Last error
export const selectNewHiresError = createSelector(
  selectNewHireState,
  state => state.error
);
