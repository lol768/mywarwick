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

export const persistActivities = createSelector(state => state.get('activities'), (activities) => {
  // Persist the current set of activities to local storage on change
  localforage.setItem('activities', activities.valueSeq().flatten().toJS());
});

export const persistNotifications = createSelector(state => state.get('notifications'), (notifications) => {
  // Persist the current set of notifications to local storage on change
  localforage.setItem('notifications', notifications.valueSeq().flatten().toJS());
});
