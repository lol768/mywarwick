/* global MyWarwickNative */
import _ from 'lodash-es';
import $ from 'jquery';

function getDevicePixelWidth() {
  const MAX = 2208;
  const w = window.innerWidth * window.devicePixelRatio;
  return _.find([320, 640, 750, 960, 1080, 1136, 1334, MAX], width => width >= w) || MAX;
}

const initialState = {
  pixelWidth: getDevicePixelWidth(),
  width: window.innerWidth,
};

export function reducer(state = initialState, action) {
  switch (action.type) {
    case 'device.width':
      return { ...state, width: action.width, pointWidth: action.pointWidth };
    case 'UPDATE_NOTIFICATION_PERMISSIONS':
      return { ...state,
        notificationPermission: window.Notification && Notification.permission,
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

export const updateNotificationPermissions = { type: 'UPDATE_NOTIFICATION_PERMISSIONS' };

const feedbackFormLocation =
  'http://www2.warwick.ac.uk/services/its/servicessupport/web/mywarwick/feedback';

export function loadDeviceDetails() {
  if (typeof MyWarwickNative !== 'undefined' && MyWarwickNative.loadDeviceDetails) {
    MyWarwickNative.loadDeviceDetails();
  } else {
    showFeedbackForm({
      "os": navigator.platform,
      "model": navigator.userAgent,
      "screen-width": $(window).width(),
      "screen-height": $(window).height(),
      "path": window.location.pathname,
    });
  }
}

export function showFeedbackForm(deviceDetails) {
  window.location = `${feedbackFormLocation}?${$.param(deviceDetails || {})}`;
}
