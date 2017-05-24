import moment from 'moment';
import log from 'loglevel';
import { fetchWithCredentials } from '../serverpipe';
import { USER_CLEAR } from './user';
import * as notificationMetadata from './notification-metadata';
import { getLastItemInStream, makeStream, onStreamReceive, takeFromStream } from '../stream';
import { createAction } from 'redux-actions';
import qs from 'qs';
import _ from 'lodash-es';

export const NOTIFICATION_FETCHING = 'notifications.fetching';
export const NOTIFICATION_RECEIVE = 'notifications.receive';
export const NOTIFICATION_FETCH = 'notifications.fetch';
export const NOTIFICATION_NUMBER_TO_SHOW = 'notifications.numberToShow';
export const ACTIVITY_FETCHING = 'activities.fetching';
export const ACTIVITY_RECEIVE = 'activities.receive';
export const ACTIVITY_FETCH = 'activities.fetch';
export const ACTIVITY_NUMBER_TO_SHOW = 'activity.numberToShow';
export const ACTIVITY_MUTE_FETCH = 'mutes.fetch';

export const fetchingActivities = createAction(ACTIVITY_FETCHING);
export const receivedActivity = createAction(ACTIVITY_RECEIVE);
export const fetchedActivities = createAction(ACTIVITY_FETCH);
export const fetchingNotifications = createAction(NOTIFICATION_FETCHING);
export const receivedNotification = createAction(NOTIFICATION_RECEIVE);
export const fetchedNotifications = createAction(NOTIFICATION_FETCH);
export const fetchedActivityMutes = createAction(ACTIVITY_MUTE_FETCH);

const ITEM_FETCH_LIMIT = 100;

export class UnnecessaryFetchError {

  constructor(message) {
    this.name = 'UnnecessaryFetchError';
    this.message = message;
  }

}

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
    const { lastItemFetched } = getState().activities;

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

export function fetchActivityMutes() {
  return dispatch => fetchWithCredentials('/api/streams/mute')
    .then(response => response.json())
    .then(json => json.data.activityMutes)
    .then(activityMutes => dispatch(fetchedActivityMutes({ activityMutes })))
    .catch(e => {
      log.warn('Failed to fetch activity mutes', e);
      throw e;
    });
}

export function fetch() {
  return dispatch => Promise.all([
    dispatch(fetchActivities()),
    dispatch(fetchNotifications()),
    dispatch(fetchActivityMutes()),
  ]);
}

export function fetchMoreNotifications() {
  return (dispatch, getState) => {
    const { notifications } = getState();

    if (notifications.fetching) {
      return Promise.reject(new UnnecessaryFetchError('Already fetching'));
    } else if (!notifications.olderItemsOnServer) {
      return Promise.reject(new UnnecessaryFetchError('No more to fetch'));
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

    if (activities.fetching) {
      return Promise.reject(new UnnecessaryFetchError('Already fetching'));
    } else if (!activities.olderItemsOnServer) {
      return Promise.reject(new UnnecessaryFetchError('No more to fetch'));
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

export function showMoreNotifications(numberToShow) {
  return (dispatch) => dispatch(createAction(NOTIFICATION_NUMBER_TO_SHOW)({ numberToShow }));
}

export function showMoreActivities(numberToShow) {
  return (dispatch) => dispatch(createAction(ACTIVITY_NUMBER_TO_SHOW)({ numberToShow }));
}

const partitionByYearAndMonth = (n) => n.date.toString().substr(0, 7);

export function mergeNotifications(stream, newNotifications) {
  return onStreamReceive(stream, partitionByYearAndMonth, newNotifications);
}

export const activityMuteDurations = [
  {
    value: '1hour',
    displayValue: 'An hour',
    toExpiryDate: () => moment().add(1, 'h'),
  },
  {
    value: '1day',
    displayValue: 'A day',
    toExpiryDate: () => moment().add(1, 'd'),
  },
  {
    value: '1week',
    displayValue: 'A week',
    toExpiryDate: () => moment().add(1, 'w'),
  },
  {
    value: '1month',
    displayValue: 'A month',
    toExpiryDate: () => moment().add(1, 'M'),
  },
  {
    value: 'indefinite',
    displayValue: 'Until I turn them back on',
    toExpiryDate: () => null,
  },
];

export function saveActivityMute(activity, options) {
  return dispatch => {
    const durationValue = _.find(options, o => o.name === 'duration').value;
    const duration = _.find(activityMuteDurations, (d) => d.value === durationValue);

    const tags = _.filter(activity.tags, (tag) => {
      const optionTag = _.find(options, o => o.name === `tags[${tag.name}]`);
      return optionTag !== undefined && optionTag.value === tag.value;
    });

    const data = {
      expiresAt: duration.toExpiryDate(),
      activityType: _.find(options, o => o.name === 'activityType') ? activity.type : null,
      providerId: _.find(options, o => o.name === 'providerId') ? activity.provider : null,
      tags,
    };

    return global.fetch('/api/streams/mute', {
      credentials: 'same-origin',
      method: 'PUT',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(data),
    }).then(() => dispatch(fetchActivityMutes()));
  };
}

export function deleteActivityMute(activity) {
  return dispatch => global.fetch(`/api/streams/mute?id=${activity.id}`, {
    credentials: 'same-origin',
    method: 'DELETE',
  }).then(() => dispatch(fetchActivityMutes()));
}

const initialState = {
  stream: makeStream(),
  fetching: false,
  olderItemsOnServer: true,
  lastItemFetched: null,
  numberToShow: 20,
  activityMutes: [],
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
    case NOTIFICATION_NUMBER_TO_SHOW:
      return {
        ...state,
        numberToShow: action.payload.numberToShow,
      };
    case ACTIVITY_MUTE_FETCH: {
      return {
        ...state,
        activityMutes: _.sortBy(action.payload.activityMutes, (mute) => (mute.expiresAt || 'ZZZ')),
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
    case ACTIVITY_NUMBER_TO_SHOW:
      return {
        ...state,
        numberToShow: action.payload.numberToShow,
      };
    default:
      return state;
  }
}

