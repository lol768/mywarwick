import { createSelector } from 'reselect';

export const persistNotificationsLastRead =
  createSelector(state => state.get('notificationsLastRead'), (lastRead) => {
    if (lastRead) {
      fetch('/api/streams/read', {
        method: 'post',
        headers: {
          Accept: 'application/json',
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          lastRead: lastRead.format(),
        }),
        credentials: 'same-origin',
      });
    }
  });
