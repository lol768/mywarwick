import Immutable from 'immutable';
import _ from 'lodash';

import store from './store';
import { registerReducer } from './reducers';
import { makeStream, onStreamReceive } from './stream';

export const NOTIFICATION_RECEIVE = 'notifications.receive';
export const NOTIFICATION_FETCH = 'notifications.fetch';
export const ACTIVITY_RECEIVE = 'activity.receive';

export function receivedActivity(activity) {
  return {
    type: ACTIVITY_RECEIVE,
    activity: activity
  };
}

export function receivedNotification(notification) {
  return {
    type: NOTIFICATION_RECEIVE,
    notification: notification
  };
}

export function fetchedNotifications(notifications) {
  return {
    type: NOTIFICATION_FETCH,
    notifications: notifications
  };
}

let partitionByYearAndMonth = (n) => n.date.substr(0, 7); // YYYY-MM

export function mergeNotifications(stream, newNotifications) {
  return onStreamReceive(stream, partitionByYearAndMonth, newNotifications);
}

registerReducer('notifications', (state = makeStream(), action) => {
  switch (action.type) {
    case NOTIFICATION_RECEIVE:
      return mergeNotifications(state, Immutable.List([action.notification]));
    case NOTIFICATION_FETCH:
      return mergeNotifications(state, Immutable.List(action.notifications));
    default:
      return state;
  }
});

registerReducer('activity', (state = makeStream(), action) => {
  switch (action.type) {
    case ACTIVITY_RECEIVE:
      return mergeNotifications(state, Immutable.List(action.activity));
    default:
      return state;
  }
});