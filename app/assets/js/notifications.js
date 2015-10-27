import Immutable from 'immutable';

import store from './store';
import { registerReducer } from './reducers';

export const NOTIFICATION_RECEIVE = 'notifications.receive';
export const NOTIFICATION_FETCH = 'notifications.fetch';

import { makeStream, onStreamReceive } from './stream';
import _ from 'lodash';

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
