import { createAction } from 'redux-actions';

const initialState = {
  assets: {
    revision: null,
    revisionOnNextLoad: null,
  },
  native: {
    loaded: false,
    platform: null,
    version: null,
    build: null,
  },
};

export const loadAssets = createAction('assets.load');
export const updateAssets = createAction('assets.update');
export const promoteNextRevision = createAction('assets.promote');
export const loadNative = createAction('app.native');

export function reducer(state = initialState, action) {
  switch (action.type) {
    case 'assets.load':
      return {
        ...state,
        assets: {
          ...state.assets,
          ...action.payload,
        },
      };
    case 'assets.update':
      return {
        ...state,
        assets: {
          ...state.assets,
          revisionOnNextLoad: action.payload,
        },
      };
    case 'assets.promote':
      if (state.assets.revisionOnNextLoad !== null) {
        return {
          ...state,
          assets: {
            revision: state.assets.revisionOnNextLoad,
            revisionOnNextLoad: null,
          },
        };
      }

      return state;
    case 'app.native':
      return {
        ...state,
        native: {
          ...action.payload,
          loaded: true,
        },
      };
    default:
      return state;
  }
}
