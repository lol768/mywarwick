import localforage from 'localforage';
import { createSelector } from 'reselect';

import SocketDatapipe from './SocketDatapipe';
import store from './store';

import { fetchedNotifications } from './notifications';

localforage.getItem('notifications').then(
  (value) => store.dispatch(fetchedNotifications(value)),
  (err) => console.log('Problem reading notifications from local storage', err)
);

const notificationsSelector = (state) => state.get('notifications');

const persistNotificationsSelect = createSelector([notificationsSelector], (notifications) => {
  // Persist the current set of notifications to local storage on change
  localforage.setItem('notifications', notifications.valueSeq().flatten().toJS());
});

store.subscribe(() => persistNotificationsSelect(store.getState()));

//TODO I'm sure this should happen somewhere more sensible
SocketDatapipe.send({
  tileId: "1",
  data: {
    type: "fetch-notifications" // since last login
  }
});
