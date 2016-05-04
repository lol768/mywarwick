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

const initialState = { fetched: false, date: null };

export function reducer(state = initialState, { type, date }) {
  const currentDate = state.date;
  const isNewer = !currentDate || !date || date.isAfter(currentDate);

  switch (type) {
    case USER_CLEAR:
      return initialState;
    case NOTIFICATIONS_READ_LOADED:
      if (state.fetched === false && isNewer) {
        // Do not overwrite a fetched value with a loaded one
        // Don't load an older last-read time
        return {
          ...state,
          date,
        };
      }
      return state;
    case NOTIFICATIONS_READ_FETCHED:
      // Trust the data from the server
      return {
        ...state,
        fetched: true,
        date,
      };
    case NOTIFICATIONS_READ:
      // Last-read date must not decrease
      if (isNewer) {
        return {
          ...state,
          date,
        };
      }
      return state;
    default:
      return state;
  }
}
