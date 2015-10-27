import Immutable from 'immutable';
import { uniq, sortByOrder } from 'lodash';

import store from './store';
import { registerReducer } from './reducers';

export const NOTIFICATION_RECEIVE = 'notifications.receive';
export const NOTIFICATION_FETCH = 'notifications.fetch';

import { Stream } from './stream';
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

export function mergeNotifications(oldStream, newNotifications) {
  let stream = new Stream(partitionByYearAndMonth);
  _(oldStream.partitions).each((items) => stream.receive(items)).value();

  stream.receive(newNotifications);

  return stream;
}

registerReducer('notifications', (state = new Stream(partitionByYearAndMonth), action) => {
  switch (action.type) {
    case NOTIFICATION_RECEIVE:
      return mergeNotifications(state, [action.notification]);
    case NOTIFICATION_FETCH:
      return mergeNotifications(state, action.notifications);
    default:
      return state;
  }
});
