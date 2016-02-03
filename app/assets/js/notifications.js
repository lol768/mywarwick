import Immutable from 'immutable';

import { registerReducer } from './reducers';
import { makeStream, onStreamReceive } from './stream';

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

const partitionByYearAndMonth = (n) => n.date.toString().substr(0, 7);

export function mergeNotifications(stream, newNotifications) {
  return onStreamReceive(stream, partitionByYearAndMonth, newNotifications);
}

/* eslint-disable new-cap */
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

registerReducer('activities', (state = makeStream(), action) => {
  switch (action.type) {
    case ACTIVITY_RECEIVE:
      return mergeNotifications(state, Immutable.List([action.activity]));
    case ACTIVITIES_FETCH:
      return mergeNotifications(state, Immutable.List(action.activities));
    default:
      return state;
  }
});
/* eslint-enable new-cap */
