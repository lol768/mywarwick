import Immutable from 'immutable';
import { registerReducer } from './reducers';
import { USER_CLEAR } from './user';

export const NOTIFICATIONS_READ = 'notifications.read';
export const NOTIFICATIONS_READ_LOADED = 'notifications.read.loaded';
export const NOTIFICATIONS_READ_FETCHED = 'notifications.read.fetched';

// User just read their notifications
export function markNotificationsRead(date) {
  return {
    type: NOTIFICATIONS_READ,
    date,
  };
}

// Last-read time fetched from server
export function fetchedNotificationsLastRead(date) {
  return {
    type: NOTIFICATIONS_READ_FETCHED,
    date,
  };
}

// Last-read time loaded from local storage
export function loadedNotificationsLastRead(date) {
  return {
    type: NOTIFICATIONS_READ_LOADED,
    date,
  };
}

const initialState = Immutable.Map({ fetched: false, date: null });

registerReducer('notificationsLastRead', (state = initialState, { type, date }) => {
  const currentDate = state.get('date');
  const isNewer = !currentDate || !date || date.isAfter(currentDate);

  switch (type) {
    case USER_CLEAR:
      return initialState;
    case NOTIFICATIONS_READ_LOADED:
      if (state.get('fetched') === false && isNewer) {
        // Do not overwrite a fetched value with a loaded one
        // Don't load an older last-read time
        return state.merge({ date });
      }
      return state;
    case NOTIFICATIONS_READ_FETCHED:
      // Trust the data from the server
      return state.merge({
        fetched: true,
        date,
      });
    case NOTIFICATIONS_READ:
      if (isNewer) {
        // Last-read date must not decrease
        return state.merge({ date });
      }
      return state;
    default:
      return state;
  }
});
