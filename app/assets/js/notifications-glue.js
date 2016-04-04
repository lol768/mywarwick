import { createSelector } from 'reselect';

export const persistNotificationsLastRead =
  createSelector(state => state.get('notificationsLastRead'), (lastRead) => {
    const { date, fetched } = lastRead.toObject();

    if (date && fetched) {
      fetch('/api/streams/read', {
        method: 'post',
        headers: {
          Accept: 'application/json',
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          lastRead: date.format(),
        }),
        credentials: 'same-origin',
      });
    }
  });
