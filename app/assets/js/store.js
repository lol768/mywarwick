import log from 'loglevel';

import thunk from 'redux-thunk';
import { createStore, applyMiddleware, combineReducers } from 'redux';
import { browserHistory } from 'react-router';
import { routerMiddleware, routerReducer } from 'react-router-redux';
import { unstable_batchedUpdates } from 'react-dom'; // eslint-disable-line camelcase

import * as allReducers from './state/all-reducers';

// "Strategy for avoiding cascading renders": https://github.com/reactjs/redux/issues/125
// A plugin, broken in React 0.14 https://github.com/acdlite/redux-batched-updates
// This function is essentially the fixed plugin.
// Prevents a few unnecessary component updates.
function batchedUpdatesMiddleware() {
  return next => action => unstable_batchedUpdates(() => next(action));
}

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
    routerMiddleware(browserHistory),
    batchedUpdatesMiddleware
  )
);
