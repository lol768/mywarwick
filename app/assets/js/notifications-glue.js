import log from 'loglevel';
import localforage from 'localforage';
import { createSelector } from 'reselect';
import fetch from 'isomorphic-fetch';

import SocketDatapipe from './SocketDatapipe';
import store from './store';

import { receivedActivity, fetchedActivities, receivedNotification, fetchedNotifications } from './notifications';

localforage.getItem('notifications').then(
  (value) => {
    if (value != null) store.dispatch(fetchedNotifications(value));
  },
  (err) => log.warn('Problem reading notifications from local storage', err)
);

localforage.getItem('activities').then(
  (value) => {
    if (value != null) store.dispatch(fetchedActivities(value));
  },
  (err) => log.warn('Problem reading activities from local storage', err)
);

const notificationsSelector = (state) => state.get('notifications');
const activitiesSelector = (state) => state.get('activities');

const persistActivitiesSelect = createSelector([activitiesSelector], (activities) => {
  // Persist the current set of activities to local storage on change
  localforage.setItem('activities', activities.valueSeq().flatten().toJS());
});

const persistNotificationsSelect = createSelector([notificationsSelector], (notifications) => {
  // Persist the current set of notifications to local storage on change
  localforage.setItem('notifications', notifications.valueSeq().flatten().toJS());
});

store.subscribe(() => persistActivitiesSelect(store.getState()));
store.subscribe(() => persistNotificationsSelect(store.getState()));

SocketDatapipe.getUpdateStream().subscribe((data) => {
  //TODO is this the best place for this? Perhaps some type of general message hub
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

fetch('/api/streams/user')
  .then(response => response.json())
  .then(json => store.dispatch(fetchedNotifications(json.activities)));

SocketDatapipe.send({
  tileId: "1",
  data: {
    type: 'who-am-i'
  }
});
