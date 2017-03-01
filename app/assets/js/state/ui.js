import log from 'loglevel';

let mq;
try {
  mq = require('modernizr').mq;
} catch (e) {
  log.warn('modernizr not present, using fallback.');
  mq = () => global.mqResult;
}

function isNative() {
  return ('navigator' in window) && navigator.userAgent.indexOf('MyWarwick/') > -1;
}

function isDesktop() {
  return !isNative() && mq('only all and (min-width: 768px)');
}
const isWideLayout = () => mq('only all and (min-width: 992px)');

const initialState = {
  className: undefined,
  isWideLayout: false,
  colourTheme: 'default',
};

export function reducer(state = initialState, action) {
  switch (action.type) {
    case 'ui.class':
      return { ...state, className: action.className };
    case 'ui.layout':
      return { ...state, isWideLayout: action.isWideLayout };
    case 'ui.theme':
      return { ...state, colourTheme: action.theme };
    default:
      return state;
  }
}

export function updateColourTheme(theme) {
  return {
    type: 'ui.theme',
    theme,
  };
}

export function updateUIContext() {
  return (dispatch, getState) => {
    const currentClassName = getState().ui.className;
    if (currentClassName === undefined || isDesktop() !== (currentClassName === 'desktop')) {
      dispatch({
        type: 'ui.class',
        className: isDesktop() ? 'desktop' : 'mobile',
      });
    }

    if (isWideLayout() !== getState().ui.isWideLayout) {
      dispatch({
        type: 'ui.layout',
        isWideLayout: isWideLayout(),
      });
    }
  };
}
