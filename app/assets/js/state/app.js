import { createAction } from 'redux-actions';

const initialState = {
  assets: {
    revision: null,
  },
};

export const updateAssets = createAction('assets.update');

export function reducer(state = initialState, action) {
  switch (action.type) {
    case 'assets.update':
      return {
        ...state,
        assets: action.payload,
      };
    default:
      return state;
  }
}
