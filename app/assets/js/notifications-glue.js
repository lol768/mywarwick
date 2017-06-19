import { createSelector } from 'reselect';
import { fetchWithCredentials } from './serverpipe';

export const persistNotificationsLastRead =
  createSelector(state => state.notificationsLastRead, (lastRead) => {
    const { date, fetched } = lastRead;
    if (date && fetched) {
      fetchWithCredentials('/api/streams/read', {
        method: 'post',
        headers: {
          Accept: 'application/json',
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          lastRead: date.format(),
        }),
      });
    }
  });
