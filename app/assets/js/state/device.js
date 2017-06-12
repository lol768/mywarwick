import _ from 'lodash-es';

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

