import Immutable from 'immutable';
import localforage from 'localforage';
import log from 'loglevel';
import store from './store';

import { registerReducer } from './reducers';

export const USER_LOAD = 'user.load';
export const USER_RECEIVE = 'user.receive';
export const USER_CLEAR = 'user.clear';

const initialState = Immutable.fromJS({
  data: {},
  authoritative: false,
  empty: true,
});

registerReducer('user', (state = initialState, action) => {
  switch (action.type) {
    case USER_LOAD:
      return state.merge({
        data: action.data,
        empty: false,
      });
    case USER_RECEIVE:
      return state.merge({
        data: action.data,
        authoritative: true,
        empty: false,
      });
    case USER_CLEAR:
      return initialState;
    default:
      return state;
  }
});

function loadCachedUserIdentity(data) {
  return {
    type: USER_LOAD,
    data,
  };
}

function receiveUserIdentity(data) {
  return {
    type: USER_RECEIVE,
    data,
  };
}

const loadUserFromLocalStorage = localforage.getItem('user')
  .then(user => {
    if (user) {
      store.dispatch(loadCachedUserIdentity(user));

      return user;
    }
    return {};
  })
  .catch(err => {
    log.warn('Could not load user from local storage', err);
    return {};
  });

function clearUserData() {
  return dispatch =>
    localforage.clear().then(() => dispatch({ type: USER_CLEAR }));
}

export function userReceive(currentUser) {
  return (dispatch) => {
    // If we are a different user than we were before (incl. anonymous),
    // nuke the store, which also clears local storage
    loadUserFromLocalStorage.then(previousUser => {
      if (previousUser.usercode !== currentUser.usercode) {
        dispatch(clearUserData())
          .then(() => localforage.setItem('user', currentUser))
          .then(() => dispatch(receiveUserIdentity(currentUser)));
      } else {
        dispatch(receiveUserIdentity(currentUser));
      }
    });
  };
}
