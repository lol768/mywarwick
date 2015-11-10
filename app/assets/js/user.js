import Immutable from 'immutable';

import { registerReducer } from './reducers';
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

SocketDatapipe.getUpdateStream().subscribe((data) => {
  if (data.type === 'who-am-i') {
    store.dispatch({
      type: USER_RECEIVE,
      data: data['user-info']
    });
  }
});

