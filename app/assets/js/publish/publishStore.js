import { createStore } from 'redux';

const initialState = {
  audience: {},
  canEstimateAudience: true,
};

function reducer(state = initialState, action) {
  switch (action.type) {
    case 'AUDIENCE_UPDATE':
      return {
        ...state,
        audience: (action.components
          ? action.components : { ...state.audience }),
        canEstimateAudience: (Object.keys(action).includes('canEstimateAudience')
          ? action.canEstimateAudience : state.canEstimateAudience),
      };
    default:
      return state;
  }
}

export default createStore(
  reducer,
  initialState,
  window.__REDUX_DEVTOOLS_EXTENSION__ && window.__REDUX_DEVTOOLS_EXTENSION__(),
);
