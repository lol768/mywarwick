/* eslint-env browser */

import log from 'loglevel';
import thunk from 'redux-thunk';
import createHistory from 'history/lib/createBrowserHistory';
import { applyMiddleware, combineReducers, compose, createStore } from 'redux';
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

const logger = (/* store */) => next => (action) => {
  log.debug(`store.dispatch(${action.type})`);
  if (action.error && action.payload && action.payload.message) {
    log.warn('Error in action:', action.payload);
  }
  return next(action);
};

export const browserHistory = createHistory();

export default createStore(
  reducer,
  initialState,
  compose(
    applyMiddleware(
      thunk,
      logger,
      routerMiddleware(browserHistory),
      batchedUpdatesMiddleware,
    ),
    window.devToolsExtension ? window.devToolsExtension() : f => f,
  ),
);
