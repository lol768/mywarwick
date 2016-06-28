function calcDeviceWidth() {
  const w = window.innerWidth * window.devicePixelRatio;
  if (w <= 320) return 320; // rubbish phone, mate
  else if (w <= 640) return 640; // iPhone 4/5 portrait
  else if (w <= 750) return 750; // iPhone 6 portrait
  else if (w <= 960) return 960; // iPhone 4 landscape
  else if (w <= 1080) return 1080; // iPhone 6p portrait
  else if (w <= 1136) return 1136; // iPhone 5 landscape
  else if (w <= 1334) return 1334; // iPhone 6 landscape
  else if (w <= 2208) return 2208; // iPhone 6p landscape
  return w;
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
