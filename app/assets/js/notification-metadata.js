import { registerReducer } from './reducers';

export const NOTIFICATIONS_READ = 'notifications.read';

export function readNotifications(date) {
  return {
    type: NOTIFICATIONS_READ,
    date,
  };
}

registerReducer('notificationsLastRead', (state = null, action) => {
  if (action.type === NOTIFICATIONS_READ) {
    if (state === null || action.date.isAfter(state)) {
      return action.date;
    }
  }

  return state;
});
