import log from 'loglevel';
import localforage from 'localforage';
import { createSelector } from 'reselect';

import { fetchedActivities, fetchedNotifications } from './notifications';

export function getNotificationsFromLocalStorage() {
  return dispatch => {
    localforage.getItem('notifications').then(
      (value) => {
        if (value != null) dispatch(fetchedNotifications(value));
      },
      (err) => log.warn('Problem reading notifications from local storage', err)
    );
  };
}

export function getActivitiesFromLocalStorage() {
  return dispatch => {
    localforage.getItem('activities').then(
      (value) => {
        if (value != null) dispatch(fetchedActivities(value));
      },
      (err) => log.warn('Problem reading activities from local storage', err)
    );
  };
}

const notificationsSelector = (state) => state.get('notifications');
const activitiesSelector = (state) => state.get('activities');

export const persistActivitiesSelect = createSelector([activitiesSelector], (activities) => {
  // Persist the current set of activities to local storage on change
  localforage.setItem('activities', activities.valueSeq().flatten().toJS());
});

export const persistNotificationsSelect = createSelector([notificationsSelector], (notifications) => {
  // Persist the current set of notifications to local storage on change
  localforage.setItem('notifications', notifications.valueSeq().flatten().toJS());
});
