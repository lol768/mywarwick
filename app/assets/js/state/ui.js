import log from 'loglevel';
import $ from 'jquery';
import { Routes } from '../components/AppRoot';
import { goBack, replace } from 'react-router-redux';
import _ from 'lodash-es';

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

const showBetaWarning = () => $('#app-container').attr('data-show-beta-warning') === 'true';

const initialState = {
  className: undefined,
  layoutWidth: 2, // 2 columns by default
  colourTheme: 'transparent',
  native: false,
  showBetaWarning: false,
};

export function reducer(state = initialState, action) {
  switch (action.type) {
    case 'ui.class':
      return { ...state, className: action.className };
    case 'ui.native':
      if (action.native !== state.native) return { ...state, native: action.native };
      return state;
    case 'ui.navRequest':
      return { ...state, navRequest: action.navRequest };
    case 'ui.theme':
      return { ...state, colourTheme: action.theme };
    case 'ui.showBetaWarning':
      if (action.showBetaWarning !== state.showBetaWarning) {
        return { ...state, showBetaWarning: action.showBetaWarning };
      }
      return state;
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
    const betaWarn = showBetaWarning();
    const native = isNative();

    if (currentClassName === undefined || isDesktop() !== (currentClassName === 'desktop')) {
      dispatch({
        type: 'ui.class',
        className: isDesktop() ? 'desktop' : 'mobile',
      });
    }

    if (native !== state.ui.native) {
      dispatch({
        type: 'ui.native',
        native,
      });
    }

    if (betaWarn !== state.ui.showBetaWarning) {
      dispatch({
        type: 'ui.showBetaWarning',
        showBetaWarning: betaWarn,
      });
    }
  };
}

const scrollRestoreLookup = {};

export function detachScrollRestore(key) {
  return $(window).off(`scroll.scrollRestore.${key}`);
}

export function attachScrollRestore(key) {
  if ('scrollRestoration' in history) {
    history.scrollRestoration = 'manual';
  }

  const scrollTop = scrollRestoreLookup[key] || 0;
  log.debug(`key: ${key} => attempt scrollTop: ${scrollTop}`);
  window.scrollTo(0, scrollTop);
  log.debug(`key: ${key} => actual scrollTop: ${document.body.scrollTop}`);

  detachScrollRestore(key).on(`scroll.scrollRestore.${key}`, _.throttle(() => {
    scrollRestoreLookup[key] = $(window).scrollTop();
  }, 250));
}

/**
 * When we change tiles we want to replace the history, so going back will close the app.
 * However if we're in /edit or /tiles we'd end up replacing just that path, so going back would go
 * back to /.
 * To sort out the history we need to go back _then_ replace, however react-router-redux gets into
 * a race condition if you try and dispatch both at the same time.
 * Therefore put the path we actually want to go to in the store separately, then just go back here.
 * The AppRoot then handles sending you on to where you wanted.
 */
export function navRequest(path, dispatch) {
  if (path === window.location.pathname) {
    window.scrollTo(0, 0);
  } else if (window.location.pathname.indexOf(Routes.TILES) !== -1 ||
    window.location.pathname.indexOf(Routes.EDIT) !== -1 ||
    window.location.pathname.indexOf(Routes.SETTINGS) !== -1
  ) {
    dispatch({
      type: 'ui.navRequest',
      navRequest: path,
    });
    dispatch(goBack());
  } else {
    dispatch(replace(path));
  }
}

