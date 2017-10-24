import { createStore } from 'redux';

const initialState = {
  audience: {},
};

function reducer(state = initialState, action) {
  switch (action.type) {
    case 'AUDIENCE_UPDATE':
      return {
        ...state,
        audience: action.components,
      };
    default:
      return state;
  }
}

export default createStore(reducer, initialState);
