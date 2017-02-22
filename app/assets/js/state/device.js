import _ from 'lodash';

function calcDeviceWidth() {
  const MAX = 2208;
  const w = window.innerWidth * window.devicePixelRatio;
  return _.find([320, 640, 750, 960, 1080, 1136, 1334, MAX], width => width >= w) || MAX;
}

const initialState = {
  width: calcDeviceWidth(),
};

export function reducer(state = initialState, action) {
  switch (action.type) {
    case 'device.width':
      return { ...state, width: action.width };
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
    const currentWidth = getState().device.width;
    const newWidth = calcDeviceWidth();
    if (newWidth !== currentWidth) {
      dispatch({
        type: 'device.width',
        width: newWidth,
      });
    }
  };
}

export const updateNotificationPermissions = { type: 'UPDATE_NOTIFICATION_PERMISSIONS' };
