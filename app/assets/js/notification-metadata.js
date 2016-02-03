import moment from 'moment';
import { registerReducer } from './reducers';

export const NOTIFICATIONS_READ = 'notifications.read';
export const ACTIVITIES_READ = 'activities.read';

export function readActivities(date) {
  return {
    type: ACTIVITIES_READ,
    date,
  };
}

export function readNotifications(date) {
  return {
    type: NOTIFICATIONS_READ,
    date,
  };
}

registerReducer('notifications-metadata', (state = { lastRead: moment('2015-01-01') }, action) => {
  switch (action.type) {
    case NOTIFICATIONS_READ:
      return { lastRead: moment.max(state.lastRead, action.date) };
    default:
      return state;
  }
});

registerReducer('activities-metadata', (state = { lastRead: moment('2015-01-01') }, action) => {
  switch (action.type) {
    case ACTIVITIES_READ:
      return { lastRead: moment.max(state.lastRead, action.date) };
    default:
      return state;
  }
});
