/* eslint-env browser */

import _ from 'lodash-es';
import { createAction } from 'redux-actions';

const DO_NOT_DISTURB_UPDATE = 'DO_NOT_DISTURB_UPDATE';
const DO_NOT_DISTURB_LOAD = 'DO_NOT_DISTURB_LOAD';

function updateNativeWithState({ enabled, weekday, weekend, loaded }) {
  const native = window.MyWarwickNative;
  if (!!native && 'setDoNotDisturb' in native && loaded) {
    native.setDoNotDisturb(JSON.stringify({ enabled, weekday, weekend }));
  }
}

export const loadDoNotDisturb = createAction(DO_NOT_DISTURB_LOAD);
export function updateDoNotDisturb(payload) {
  return (dispatch, getState) => {
    dispatch({
      type: DO_NOT_DISTURB_UPDATE,
      payload,
    });
    updateNativeWithState(getState().device.doNotDisturb);
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
    loaded: false,
    enabled: false,
    weekend: {
      start: '21:00',
      end: '07:00',
    },
    weekday: {
      start: '21:00',
      end: '07:00',
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
    case DO_NOT_DISTURB_LOAD:
      return {
        ...state,
        doNotDisturb: {
          ...action.payload,
          loaded: true,
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
