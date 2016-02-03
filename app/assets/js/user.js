import Immutable from 'immutable';
import localforage from 'localforage';
import log from 'loglevel';
import store from './store';

import { registerReducer, resetStore } from './reducers';

export const USER_LOAD = 'user.load';
export const USER_RECEIVE = 'user.receive';

const initialState = Immutable.fromJS({
  data: {},
  authoritative: false,
});

/* eslint-disable new-cap */
registerReducer('user', (state = initialState, action) => {
  switch (action.type) {
    case USER_LOAD:
      return state.merge({
        data: action.data,
      });
    case USER_RECEIVE:
      return state.merge({
        data: action.data,
        authoritative: true,
      });
    default:
      return state;
  }
});
/* eslint-enable new-cap */

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

export function userReceive(currentUser) {
  return (dispatch) => {
    // If we are a different user than we were before (incl. anonymous),
    // nuke the store, which also clears local storage
    loadUserFromLocalStorage.then(previousUser => {
      if (previousUser.usercode !== currentUser.usercode) {
        dispatch(resetStore())
          .then(() => localforage.setItem('user', currentUser))
          .then(() => dispatch(receiveUserIdentity(currentUser)));
      } else {
        dispatch(receiveUserIdentity(currentUser));
      }
    });
  };
}
