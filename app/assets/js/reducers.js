import Immutable from 'immutable';

const initialState = Immutable.Map();

/*
 * Named to make it super clear when you're not being totally functional
 */
var mutableGlobalReducers = makeReducers();

/*
 * Update the reducers that are registered with the global reducer
 */
export function mutateReducers(value) {
  mutableGlobalReducers = value;
}

/*
 * Create a data structure suitable for holding namespaced reducers, without
 * caring what it is
 */
export function makeReducers() {
  return Immutable.Map();
}

// const store = require('./store');
import store from './store';
export function registerReducer(name, reducer) {
  mutateReducers(appendReducer(mutableGlobalReducers, name, reducer));

  // Dispatch an action handled by the newly-added receiver, to add
  // the initial state to the store immediately
  store.dispatch({
    type: name + '.__init'
  });
}

/*
 * Unregister all reducers for a certain namespace
 */
export function unregisterAllReducers(name) {
  mutateReducers(mutableGlobalReducers.delete(name));
}

/*
 * Unregister a specific reducer within a namespace
 */
export function unregisterReducer(name, reducer) {
  if (mutableGlobalReducers.has(name))
    mutateReducers(mutableGlobalReducers.get(name).filterNot((r) => r == reducer));
}

/*
 * Register a reducer to operate on a subtree of the application state.
 *
 * A reducer is a function of the form (state, action) => stateAfterAction.
 *
 * The reducer will be called upon dispatch of an action with a type starting
 * with "`name`." , and the resulting state changes applied to a subtree of the
 * application state with the key `name`.
 */
export function appendReducer(reducers, name, reducer) {
  return reducers.has(name) ?
    reducers.update(name, (list) => list.push(reducer)) :
    reducers.set(name, Immutable.List().push(reducer));
}

/*
 * Create the (state, action) function that results from running each of the
 * reducer functions in sequence.
 *
 *     composeReducers([one, two, three])(state, action)
 *       => three(two(one(state, action), action), action)
 *
 * If called with undefined or a list with no reducers, return the identity
 * function, so the state remains unchanged.
 */
export function composeReducers(reducers) {
  if (reducers === undefined || reducers.count() == 0) {
    return (state) => state;
  } else {
    return (state, action) => reducers.reduce((state, reducer) => reducer(state, action), state);
  }
}

/*
 * Primary reducer for the application
 */
export default function app(state = initialState, action = undefined) {
  if (action === undefined || action.type === undefined) {
    return state;
  }

  // Only actions with a namespace-style type may use sub-reducers
  if (action.type.indexOf('.') >= 0) {
    // The action's namespace is everything before the first full-
    // stop
    let namespace = action.type.substring(0, action.type.indexOf('.'));

    // Compose the reducers for this namespace, then run them with
    // the current subtree
    let fn = composeReducers(mutableGlobalReducers.get(namespace));
    let subtree = fn(state.get(namespace), action);

    return (subtree === undefined) ?
      state.delete(namespace) :
      state.mergeDeep({
        [namespace]: subtree
      });
  } else {
    return state;
  }
}