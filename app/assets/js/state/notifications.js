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
    const [latestActivity] = takeFromStream(getState().activities.stream, 1);

    dispatch(fetchingActivities());

    return fetchStream('activities', {
      since: latestActivity && latestActivity.id,
    })
      .then(data => {
        dispatch(fetchedActivities(data.activities));

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
    const [latestNotification] = takeFromStream(getState().notifications.stream, 1);

    dispatch(fetchingNotifications());

    return fetchStream('notifications', {
      since: latestNotification && latestNotification.id,
    })
      .then(data => {
        const date = data.read && moment(data.read);
        dispatch(notificationMetadata.fetchedNotificationsLastRead(date));

        dispatch(fetchedNotifications(data.notifications));

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
        olderItemsOnServer: data.notifications.length === ITEM_FETCH_LIMIT,
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
        olderItemsOnServer: data.activities.length === ITEM_FETCH_LIMIT,
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
    case NOTIFICATION_FETCH:
      if (action.payload instanceof Array) {
        return {
          ...state,
          stream: mergeNotifications(state.stream, action.payload),
          fetching: false,
        };
      }

      return {
        ...state,
        stream: mergeNotifications(state.stream, action.payload.items),
        fetching: false,
        olderItemsOnServer: action.payload.olderItemsOnServer,
      };
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
    case ACTIVITY_FETCH:
      if (action.payload instanceof Array) {
        return {
          ...state,
          stream: mergeNotifications(state.stream, action.payload),
          fetching: false,
        };
      }

      return {
        ...state,
        stream: mergeNotifications(state.stream, action.payload.items),
        fetching: false,
        olderItemsOnServer: action.payload.olderItemsOnServer,
      };
    default:
      return state;
  }
}

