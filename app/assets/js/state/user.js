import localforage from 'localforage';
import log from 'loglevel';

export const USER_LOAD = 'USER_LOAD';
export const USER_RECEIVE = 'USER_RECEIVE';
export const USER_CLEAR = 'USER_CLEAR';

const initialState = {
  data: {},
  authoritative: false,
  empty: true,
};

export function reducer(state = initialState, action) {
  switch (action.type) {
    case USER_LOAD:
      return { ...state,
        data: action.data,
        empty: false,
      };
    case USER_RECEIVE:
      return { ...state,
        data: action.data,
        authoritative: true,
        empty: false,
      };
    case USER_CLEAR:
      return initialState;
    default:
      return state;
  }
}

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

function loadUserFromLocalStorage(dispatch) {
  return localforage.getItem('user')
    .then(user => {
      if (user) {
        dispatch(loadCachedUserIdentity(user));
        return user;
      }
      return {};
    })
    .catch(err => {
      log.warn('Could not load user from local storage', err);
      return {};
    });
}

function clearUserData() {
  return dispatch =>
    localforage.clear().then(() => dispatch({ type: USER_CLEAR }));
}

export function userReceive(currentUser) {
  return (dispatch) => {
    // If we are a different user than we were before (incl. anonymous),
    // nuke the store, which also clears local storage
    loadUserFromLocalStorage(dispatch).then(previousUser => {
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
