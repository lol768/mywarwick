/* eslint-env browser */

import _ from 'lodash-es';
import { createAction } from 'redux-actions';
import { fetchWithCredentials, postJsonWithCredentials } from '../serverpipe';
import log from 'loglevel';

const DO_NOT_DISTURB_UPDATE = 'DO_NOT_DISTURB_UPDATE';
const DO_NOT_DISTURB_RECEIVE = 'DO_NOT_DISTURB_RECEIVE';
const DO_NOT_DISTURB_REQUEST = 'DO_NOT_DISTURB_REQUEST';

export const receive = createAction(DO_NOT_DISTURB_RECEIVE);
const request = createAction(DO_NOT_DISTURB_REQUEST);

function updateServerWithState({ enabled, start, end, fetched }) {
  if (fetched) {
    postJsonWithCredentials('/api/donotdisturb', { enabled, doNotDisturb: { start, end } })
      .then(response => response.json())
      .then((response) => {
        if (response.status !== 'ok') {
          log.error(`Failed to POST request to Do Not Disturb API. 
          Response was ${JSON.stringify(response)}`);
        }
      })
      .catch(e => log.error('Failed to POST request to Do Not Disturb API', e));
  }
}

const postToServer = _.debounce(getState =>
  updateServerWithState(getState().device.doNotDisturb), 500,
);

export function fetchDoNotDisturb() {
  return (dispatch) => {
    log.debug('Fetching Do Not Disturb preference.');
    dispatch(request());
    return fetchWithCredentials('/api/donotdisturb')
      .then(response => response.json())
      .then((json) => {
        if (json.data !== undefined) {
          dispatch(receive(json));
        } else {
          throw new Error('Invalid response returned from Do Not Disturb API');
        }
      })
      .catch(e => dispatch(receive(e)));
  };
}

export function updateDoNotDisturb(payload) {
  return (dispatch, getState) => {
    dispatch({
      type: DO_NOT_DISTURB_UPDATE,
      payload,
    });
    postToServer(getState);
  };
}

function getDevicePixelWidth() {
  const MAX = 2208;
  const w = window.innerWidth * window.devicePixelRatio;
  return _.find([320, 640, 750, 960, 1080, 1136, 1334, MAX], width => width >= w) || MAX;
}

const initialState = {
  pixelWidth: getDevicePixelWidth(),
  width: window.innerWidth,
  isOnline: navigator.onLine,
  doNotDisturb: {
    fetching: false,
    failed: false,
    fetched: false,
    enabled: false,
    start: {
      hr: 21,
      min: 0,
    },
    end: {
      hr: 7,
      min: 0,
    },
  },
};

export function reducer(state = initialState, action) {
  switch (action.type) {
    case 'device.width':
      return { ...state, width: action.width, pointWidth: action.pointWidth };
    case 'UPDATE_NOTIFICATION_PERMISSIONS':
      return { ...state,
        notificationPermission: window.Notification && Notification.permission,
      };
    case 'UPDATE_DEVICE_ONLINE_STATUS':
      return {
        ...state,
        isOnline: action.isOnline,
      };
    case DO_NOT_DISTURB_REQUEST:
      return {
        ...state,
        doNotDisturb: {
          ...state.doNotDisturb,
          fetching: true,
          failed: false,
          fetched: false,
        },
      };
    case DO_NOT_DISTURB_RECEIVE:
      if (action.error) {
        return {
          ...state,
          doNotDisturb: {
            ...state.doNotDisturb,
            fetching: false,
            failed: true,
            fetched: true,
          },
        };
      }
      return {
        ...state,
        doNotDisturb: {
          ...state.doNotDisturb,
          ...action.payload.data,
          fetching: false,
          failed: false,
          fetched: true,
        },
      };
    case DO_NOT_DISTURB_UPDATE:
      return {
        ...state,
        doNotDisturb: {
          ...state.doNotDisturb,
          ...action.payload,
        },
      };
    default:
      return state;
  }
}

export function updateDeviceWidth() {
  return (dispatch, getState) => {
    if (window.innerWidth !== getState().device.width) {
      dispatch({
        type: 'device.width',
        width: window.innerWidth,
        pixelWidth: getDevicePixelWidth(),
      });
    }
  };
}

export function updateOnlineStatus(isOnline) {
  return (dispatch) => {
    dispatch({
      type: 'UPDATE_DEVICE_ONLINE_STATUS',
      isOnline,
    });
  };
}

export const updateNotificationPermissions = { type: 'UPDATE_NOTIFICATION_PERMISSIONS' };
