import * as Immutable from 'immutable';
import { LOCATION_CHANGE } from 'react-router-redux';

// https://github.com/reactjs/react-router-redux/tree/v4.0.2#what-if-i-use-immutablejs-with-my-redux-store
// https://github.com/gajus/redux-immutable#using-with-react-router-redux

const initialState = Immutable.fromJS({
  location: {},
});

/**
 * Immutable-friendly version of react-router-redux/routeReducer
 */
export default function reducer(state = initialState, { type, payload } = {}) {
  if (type === LOCATION_CHANGE) {
    return state.merge({ locationBeforeTransitions: payload });
  }
  return state;
}
