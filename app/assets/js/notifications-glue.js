import log from 'loglevel';
import localforage from 'localforage';
import { createSelector } from 'reselect';
import fetch from 'isomorphic-fetch';

import store from './store';

import { fetchWhoAmI, fetchActivities } from './serverpipe';
import { fetchedActivities, fetchedNotifications } from './notifications';

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

// TODO these initial fetches might happen someplace more appropriate
fetchWhoAmI();
fetchActivities();
