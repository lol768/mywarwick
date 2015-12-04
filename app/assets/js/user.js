import Immutable from 'immutable';

import localforage from 'localforage';

import { registerReducer, resetStore } from './reducers';
import SocketDatapipe from './SocketDatapipe';
import store from './store';

export const USER_RECEIVE = 'user.receive';

registerReducer('user', (state = Immutable.Map(), action) => {
  switch (action.type) {
    case USER_RECEIVE:
      return Immutable.Map(action.data);
    default:
      return state;
  }
});

export function userReceive(data) {
  return dispatch => {
    localforage.getItem('usercode', (err, currentUsercode) => {
      // If we are a different user than we were before (incl. anonymous),
      // nuke the store, which also clears local storage
      if (currentUsercode !== data.usercode)
        dispatch(resetStore());

      localforage.setItem('usercode', data.usercode, () => {
        dispatch({
          type: USER_RECEIVE,
          data: data
        });
      });
    });
  };
}
