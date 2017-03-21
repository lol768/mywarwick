import moment from 'moment';
import log from 'loglevel';
import { fetchWithCredentials } from '../serverpipe';
import { USER_CLEAR } from './user';
import * as notificationMetadata from './notification-metadata';
import { makeStream, onStreamReceive, takeFromStream, getLastItemInStream } from '../stream';
import { createAction } from 'redux-actions';
import qs from 'qs';

export const NOTIFICATION_FETCHING = 'notifications.fetching';
export const NOTIFICATION_RECEIVE = 'notifications.receive';
export const NOTIFICATION_FETCH = 'notifications.fetch';
export const ACTIVITY_FETCHING = 'activities.fetching';
export const ACTIVITY_RECEIVE = 'activities.receive';
export const ACTIVITY_FETCH = 'activities.fetch';

export const fetchingActivities = createAction(ACTIVITY_FETCHING);
export const receivedActivity = createAction(ACTIVITY_RECEIVE);
export const fetchedActivities = createAction(ACTIVITY_FETCH);
export const fetchingNotifications = createAction(NOTIFICATION_FETCHING);
export const receivedNotification = createAction(NOTIFICATION_RECEIVE);
export const fetchedNotifications = createAction(NOTIFICATION_FETCH);

const ITEM_FETCH_LIMIT = 100;

function fetchStream(name, options = {}) {
  const query = qs.stringify({
    limit: ITEM_FETCH_LIMIT,
    ...options,
  }, { skipNulls: true });

  return fetchWithCredentials(`/api/streams/${name}?${query}`)
    .then(response => response.json())
    .then(json => json.data);
}

export function fetchActivities() {
  return (dispatch, getState) => {
    const { lastItemFetched } = getState().notifications;

    dispatch(fetchingActivities());

    return fetchStream('activities', {
      since: lastItemFetched,
    })
      .then(data => {
        dispatch(fetchedActivities({
          items: data.activities,
        }));

        if (data.activities.length === ITEM_FETCH_LIMIT) {
          // Likely there are more recent items available, so fetch them
          dispatch(fetchActivities());
        }
      })
      .catch(e => {
        log.warn('Failed to fetch activities', e);
        throw e;
      });
  };
}

export function fetchNotifications() {
  return (dispatch, getState) => {
    const { lastItemFetched } = getState().notifications;

    dispatch(fetchingNotifications());

    return fetchStream('notifications', {
      since: lastItemFetched,
    })
      .then(data => {
        const date = data.read && moment(data.read);
        dispatch(notificationMetadata.fetchedNotificationsLastRead(date));

        dispatch(fetchedNotifications({
          items: data.notifications,
        }));

        if (data.notifications.length === ITEM_FETCH_LIMIT) {
          dispatch(fetchNotifications());
        }
      })
      .catch(e => {
        log.warn('Failed to fetch notifications', e);
        throw e;
      });
  };
}

export function fetch() {
  return dispatch => Promise.all([dispatch(fetchActivities()), dispatch(fetchNotifications())]);
}

export function fetchMoreNotifications() {
  return (dispatch, getState) => {
    const { notifications } = getState();

    if (notifications.fetching || !notifications.olderItemsOnServer) {
      return Promise.reject(null);
    }

    dispatch(fetchingNotifications());

    const oldestItem = getLastItemInStream(notifications.stream);

    return fetchStream('notifications', {
      before: oldestItem && oldestItem.id,
    })
      .then(data => dispatch(fetchedNotifications({
        items: data.notifications,
        meta: {
          olderItemsOnServer: data.notifications.length === ITEM_FETCH_LIMIT,
        },
      })));
  };
}

export function fetchMoreActivities() {
  return (dispatch, getState) => {
    const { activities } = getState();

    if (activities.fetching || !activities.olderItemsOnServer) {
      return Promise.reject(null);
    }

    dispatch(fetchingActivities());

    const oldestItem = getLastItemInStream(activities.stream);

    return fetchStream('activities', {
      before: oldestItem && oldestItem.id,
    })
      .then(data => dispatch(fetchedActivities({
        items: data.activities,
        meta: {
          olderItemsOnServer: data.activities.length === ITEM_FETCH_LIMIT,
        },
      })));
  };
}

const partitionByYearAndMonth = (n) => n.date.toString().substr(0, 7);

export function mergeNotifications(stream, newNotifications) {
  return onStreamReceive(stream, partitionByYearAndMonth, newNotifications);
}

const initialState = {
  stream: makeStream(),
  fetching: false,
  olderItemsOnServer: true,
  lastItemFetched: null,
};

export function notificationsReducer(state = initialState, action) {
  switch (action.type) {
    case USER_CLEAR:
      return initialState;
    case NOTIFICATION_FETCHING:
      return {
        ...state,
        fetching: true,
      };
    case NOTIFICATION_RECEIVE:
      return {
        ...state,
        stream: mergeNotifications(state.stream, [action.payload]),
      };
    case NOTIFICATION_FETCH: {
      const updatedStream = mergeNotifications(state.stream, action.payload.items);
      const [lastItem] = takeFromStream(updatedStream, 1);

      return {
        ...state,
        ...action.payload.meta,
        stream: updatedStream,
        fetching: false,
        lastItemFetched: lastItem && lastItem.id,
      };
    }
    default:
      return state;
  }
}

export function activitiesReducer(state = initialState, action) {
  switch (action.type) {
    case USER_CLEAR:
      return initialState;
    case ACTIVITY_FETCHING:
      return {
        ...state,
        fetching: true,
      };
    case ACTIVITY_RECEIVE:
      return {
        ...state,
        stream: mergeNotifications(state.stream, [action.payload]),
      };
    case ACTIVITY_FETCH: {
      const updatedStream = mergeNotifications(state.stream, action.payload.items);
      const [lastItem] = takeFromStream(updatedStream, 1);

      return {
        ...state,
        ...action.payload.meta,
        stream: updatedStream,
        fetching: false,
        lastItemFetched: lastItem && lastItem.id,
      };
    }
    default:
      return state;
  }
}

