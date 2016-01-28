import log from 'loglevel';
import localforage from 'localforage';
import moment from 'moment';
import { createSelector } from 'reselect';

import { fetchedActivities, fetchedNotifications } from './notifications';
import { readNotifications, readActivities } from './notification-metadata';

export function getNotificationsFromLocalStorage() {
  return dispatch => {
    localforage.getItem('notificationsLastRead').then(
      (value) => {
        if (value != null) dispatch(readNotifications(moment(value)));
      },
      (err) => log.warn('Problem reading notificationsLastRead from local storage', err)
    );

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

    localforage.getItem('activitiesLastRead').then(
      (value) => {
        if (value != null) dispatch(readActivities(moment(value)));
      },
      (err) => log.warn('Problem reading activitiesLastRead from local storage', err)
    );

    localforage.getItem('activities').then(
      (value) => {
        if (value != null) dispatch(fetchedActivities(value));
      },
      (err) => log.warn('Problem reading activities from local storage', err)
    );
  };
}

const notificationsMetadataSelector = state => state.get("notifications-metadata");
const activitiesMetadataSelector = state => state.get("activities-metadata");
const userSelector = state => state.get("user");

export const persistActivities = createSelector(state => state.get('activities'), (activities) => {
  // Persist the current set of activities to local storage on change
  localforage.setItem('activities', activities.valueSeq().flatten().toJS());
});

export const persistActivitiesMetadata = createSelector([activitiesMetadataSelector, userSelector], (metadata, user) => {
  let data = { usercode: user.get("usercode"), activitiesRead: metadata.lastRead.format() };
  persistLastRead('activitiesLastRead', data, metadata.lastRead);
});

export const persistNotifications = createSelector(state => state.get('notifications'), (notifications) => {
  // Persist the current set of notifications to local storage on change
  localforage.setItem('notifications', notifications.valueSeq().flatten().toJS());
});

export const persistNotificationsMetadata = createSelector([notificationsMetadataSelector, userSelector], (metadata, user) => {
  let data = { usercode: user.get("usercode"), notificationsRead: metadata.lastRead.format() };
  persistLastRead('notificationsLastRead', data, metadata.lastRead);
});

const persistLastRead = (field, data, lastRead) => {
  localforage.getItem(field).then(lastReadLocal => {
    if (lastReadLocal == null || moment(lastReadLocal).isBefore(lastRead)) {
      localforage.setItem(field, lastRead.format()).then(() =>
        fetch('/api/streams/read', {
          method: 'post',
          headers: {
            'Accept': 'application/json',
            'Content-Type': 'application/json'
          },
          body: JSON.stringify(data),
          credentials: 'same-origin'
        })
      );
    }
  });
};
