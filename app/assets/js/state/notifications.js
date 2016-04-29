import Immutable from 'immutable';
import moment from 'moment';
import log from 'loglevel';
import _ from 'lodash';

import { fetchWithCredentials } from '../serverpipe';
import { makeStream, onStreamReceive } from '../stream';
import { USER_CLEAR } from './user';
import * as notificationMetadata from './notification-metadata';

export const NOTIFICATION_RECEIVE = 'notifications.receive';
export const NOTIFICATION_FETCH = 'notifications.fetch';
export const ACTIVITY_RECEIVE = 'activities.receive';
export const ACTIVITIES_FETCH = 'activities.fetch';

export function receivedActivity(activity) {
  return {
    type: ACTIVITY_RECEIVE,
    activity,
  };
}

export function fetchedActivities(activities) {
  return {
    type: ACTIVITIES_FETCH,
    activities,
  };
}

export function receivedNotification(notification) {
  return {
    type: NOTIFICATION_RECEIVE,
    notification,
  };
}

export function fetchedNotifications(notifications) {
  return {
    type: NOTIFICATION_FETCH,
    notifications,
  };
}

export function fetch() {
  return dispatch =>
    fetchWithCredentials('/api/streams/user')
      .then(response => response.json())
      .then(json => {
        const notifications = _.filter(json.data.activities, (a) => a.notification);
        const activities = _.filter(json.data.activities, (a) => !a.notification);

        const date = json.data.notificationsRead ? moment(json.data.notificationsRead) : null;
        dispatch(notificationMetadata.fetchedNotificationsLastRead(date));

        dispatch(fetchedNotifications(notifications));
        dispatch(fetchedActivities(activities));
      })
      .catch(err => {
        log.info('Failed to fetch notifications:', err);
      });
}

const partitionByYearAndMonth = (n) => n.date.toString().substr(0, 7);

export function mergeNotifications(stream, newNotifications) {
  return onStreamReceive(stream, partitionByYearAndMonth, newNotifications);
}

export function notificationsReducer(state = makeStream(), action) {
  switch (action.type) {
    case USER_CLEAR:
      return makeStream();
    case NOTIFICATION_RECEIVE:
      return mergeNotifications(state, Immutable.List([action.notification]));
    case NOTIFICATION_FETCH:
      return mergeNotifications(state, Immutable.List(action.notifications));
    default:
      return state;
  }
}

export function activitiesReducer(state = makeStream(), action) {
  switch (action.type) {
    case USER_CLEAR:
      return makeStream();
    case ACTIVITY_RECEIVE:
      return mergeNotifications(state, Immutable.List([action.activity]));
    case ACTIVITIES_FETCH:
      return mergeNotifications(state, Immutable.List(action.activities));
    default:
      return state;
  }
}

