import log from 'loglevel';

import thunk from 'redux-thunk';
import { createStore, applyMiddleware, combineReducers } from 'redux';
import { browserHistory } from 'react-router';
import { routerMiddleware, routerReducer } from 'react-router-redux';

import * as allReducers from './state/all-reducers';

const initialState = {};

// build a combined reducer, adding in any weird 3rd party reducers we need.
const reducer = combineReducers({
  ...allReducers,
  routing: routerReducer,
});

const logger = (/* store */) => next => action => {
  log.debug('store.dispatch(action=', action, ')');
  return next(action);
};

export default createStore(
  reducer,
  initialState,
  applyMiddleware(
    thunk,
    logger,
    routerMiddleware(browserHistory)
  )
);
