import _ from 'lodash';

function calcDeviceWidth() {
  const w = window.innerWidth * window.devicePixelRatio;
  return _.find([320, 640, 750, 960, 1080, 1136, 1334, 2208], width => width >= w);
}

const initialState = {
  width: calcDeviceWidth(),
};

export function reducer(state = initialState, action) {
  switch (action.type) {
    case 'device.width':
      return { ...state, width: action.width };
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
