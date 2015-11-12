import log from 'loglevel';
import localforage from 'localforage';
import { createSelector } from 'reselect';

import SocketDatapipe from './SocketDatapipe';
import store from './store';

import { receivedActivity, receivedNotification, fetchedNotifications } from './notifications';

localforage.getItem('notifications').then(
  (value) => {
    if (value != null) store.dispatch(fetchedNotifications(value));
  },
  (err) => log.warn('Problem reading notifications from local storage', err)
);

const notificationsSelector = (state) => state.get('notifications');
const ActivitiesSelector = (state) => state.get('activities');

const persistActivitiesSelect = createSelector([notificationsSelector, ActivitiesSelector], (notifications, activities) => {
  // Persist the current set of activities to local storage on change
  localforage.setItem('notifications', notifications.valueSeq().flatten().toJS());
  localforage.setItem('activities', activities.valueSeq().flatten().toJS());
});

store.subscribe(() => persistActivitiesSelect(store.getState()));

SocketDatapipe.getUpdateStream().subscribe((data) => {
  //TODO is this the best place for this?
  switch (data.type) {
    case 'fetch-notifications':
      store.dispatch(fetchedNotifications(data.notifications));
      break;
    case 'notification':
      store.dispatch(receivedNotification(data));
      break;
    case 'activity':
      store.dispatch(receivedActivity(data));
      break;
    default:
    // nowt
  }
});

//TODO I'm sure this should happen somewhere more sensible
SocketDatapipe.send({
  tileId: "1",
  data: {
    type: "fetch-notifications" // since last login
  }
});

SocketDatapipe.send({
  tileId: "1",
  data: {
    type: 'who-am-i'
  }
});
