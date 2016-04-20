import * as Immutable from 'immutable';

import thunk from 'redux-thunk';
import { createStore, applyMiddleware } from 'redux';
import { combineReducers } from 'redux-immutable';
import { browserHistory } from 'react-router';
import { routerMiddleware, routerReducer } from 'react-router-redux';

import * as allReducers from './state/all-reducers';

const initialState = Immutable.Map();

// build a combined reducer, adding in any weird 3rd party reducers we need.
const reducer = combineReducers({
  ...allReducers,
  router: routerReducer,
});

export default createStore(
  reducer,
  initialState,
  applyMiddleware(
    thunk,
    routerMiddleware(browserHistory)
  )
);
