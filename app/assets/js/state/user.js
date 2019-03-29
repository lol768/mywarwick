import localforage from '../localdata';
import log from 'loglevel';
import _ from 'lodash-es';

export const USER_LOAD = 'USER_LOAD';
export const USER_RECEIVE = 'USER_RECEIVE';
export const USER_CLEAR = 'USER_CLEAR';
export const SSO_LINKS_RECEIVE = 'SSO_LINKS_RECEIVE';
export const FEATURES_RECEIVE = 'FEATURES_RECEIVE';

const initialState = {
  data: {
    authenticated: false,
  },
  authoritative: false,
  empty: true,
  links: {
    login: null,
    logout: null,
  },
  features: {
    updateTileEditUI: true,
  },
};

export function reducer(state = initialState, action) {
  switch (action.type) {
    case USER_LOAD: {
      const data = _.isEqual(state.data, action.data) ? state.data : action.data;
      return { ...state,
        data,
        empty: false,
      };
    }
    case USER_RECEIVE:
      return { ...state,
        data: action.data,
        authoritative: true,
        empty: false,
      };
    case USER_CLEAR:
      return { ...initialState,
        links: state.links,
      };
    case SSO_LINKS_RECEIVE:
      if (_.isEqual(state.links, action.links)) return state;
      return { ...state,
        links: action.links,
      };
    case FEATURES_RECEIVE:
      return { ...state,
        features: action.features,
      };
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

export function receiveSSOLinks(links) {
  return {
    type: SSO_LINKS_RECEIVE,
    links,
  };
}

export function receiveFeatures(features) {
  return {
    type: FEATURES_RECEIVE,
    features,
  };
}

export function loadUserFromLocalStorage(dispatch) {
  return localforage.getItem('user')
    .then((user) => {
      if (user) {
        dispatch(loadCachedUserIdentity(user));
        return user;
      }
      return {};
    })
    .catch((err) => {
      log.warn('Could not load user from local storage', err);
      return {};
    });
}

function clearUserData() {
  return dispatch =>
    localforage.clear().then(() => dispatch({ type: USER_CLEAR }));
}


export function userReceive(currentUser) {
  function setItemFailed(e) {
    log.error('Failed to cache user', e);
    return {}; // result is ignored below anyway.
  }

  return dispatch =>
    // If we are a different user than we were before (incl. anonymous),
    // nuke the store, which also clears local storage
    loadUserFromLocalStorage(dispatch).then((previousUser) => {
      if (previousUser.usercode !== currentUser.usercode) {
        return dispatch(clearUserData())
          .then(() => localforage.setItem('user', currentUser).catch(setItemFailed))
          .then(() => dispatch(receiveUserIdentity(currentUser)));
      }
      return localforage.setItem('user', currentUser)
        .catch(setItemFailed)
        .then(() => dispatch(receiveUserIdentity(currentUser)));
    });
}
