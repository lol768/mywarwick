import log from 'loglevel';
import { browserHistory } from 'react-router';
import $ from 'jquery';
import { Routes } from '../components/AppRoot';

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
  // We make the same 'native is mobile' assumption in bridge.js
  return !isNative() && mq('only all and (min-width: 768px)');
}
const isWideLayout = () => mq('only all and (min-width: 992px)');

const initialState = {
  className: undefined,
  isWideLayout: false,
  colourTheme: 'default',
  native: false,
};

export function reducer(state = initialState, action) {
  switch (action.type) {
    case 'ui.class':
      return { ...state, className: action.className };
    case 'ui.native':
      if (action.native !== state.native) return { ...state, native: action.native };
      return state;
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
    const state = getState();
    const currentClassName = state.ui.className;

    if (currentClassName === undefined || isDesktop() !== (currentClassName === 'desktop')) {
      dispatch({
        type: 'ui.class',
        className: isDesktop() ? 'desktop' : 'mobile',
      });
    }

    if (isWideLayout() !== state.ui.isWideLayout) {
      dispatch({
        type: 'ui.layout',
        isWideLayout: isWideLayout(),
      });
    }

    dispatch({
      type: 'ui.native',
      native: isNative(),
    });
  };
}

export function scrollTopOnTabChange(scrollTops) {
  function isTopLevelUrlNotEdit(location) {
    return (location.pathname.match(/\//g) || []).length === 1
      && location.pathname !== `/${Routes.EDIT}`;
  }

  browserHistory.listen(location => {
    if (isTopLevelUrlNotEdit(location)) {
      const path = window.location.pathname;
      const scrolltop = scrollTops[path] || 0;
      log.debug(`path: ${path} => scrollTop: ${scrolltop}`);
      $(window).scrollTop(scrolltop);
    }
  });
}

