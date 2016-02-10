import moment from 'moment';
import { registerReducer } from './reducers';

export const NOTIFICATIONS_READ = 'notifications.read';

export function readNotifications(date) {
  return {
    type: NOTIFICATIONS_READ,
    date,
  };
}

registerReducer('notifications-lastRead', (state = moment('2015-01-01'), action) => {
  switch (action.type) {
    case NOTIFICATIONS_READ:
      return moment.max(state, action.date);
    default:
      return state;
  }
});
