import Immutable from 'immutable';
import localforage from 'localforage';

import { registerReducer, resetStore } from './reducers';

export const USER_RECEIVE = 'user.receive';

registerReducer('user', (state = Immutable.Map({loaded: false}), action) => {
  switch (action.type) {
    case USER_RECEIVE:
      return Immutable.Map(action.data).set('loaded', true);
    default:
      return state;
  }
});

function userReceiveAction(data) {
  return {
    type: USER_RECEIVE,
    data: data
  };
}

export function userReceive(data) {
  return dispatch => {
    localforage.getItem('usercode', (err, currentUsercode) => {
      // If we are a different user than we were before (incl. anonymous),
      // nuke the store, which also clears local storage
      if (currentUsercode !== data.usercode) {
        dispatch(resetStore())
          .then(() => localforage.setItem('usercode', data.usercode))
          .then(() => dispatch(userReceiveAction(data)));
      } else {
        dispatch(userReceiveAction(data));
      }
    });
  };
}
