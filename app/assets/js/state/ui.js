import log from 'loglevel';
import { browserHistory } from 'react-router';
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
const isWideLayout = () => mq('only all and (min-width: 992px)');

const showBetaWarning = () => $('#app-container').attr('data-show-beta-warning') === 'true';

const initialState = {
  className: undefined,
  isWideLayout: false,
  colourTheme: 'default',
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
    case 'ui.layout':
      return { ...state, isWideLayout: action.isWideLayout };
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

    dispatch({
      type: 'ui.showBetaWarning',
      showBetaWarning: showBetaWarning(),
    });
  };
}

export function scrollTopOnTabChange(scrollTops) {
  function isTopLevelUrl(location) {
    return (location.pathname.match(/\//g) || []).length === 1;
  }

  browserHistory.listen(location => {
    if (isTopLevelUrl(location)) {
      const originalPath = window.location.pathname;
      const path = (_.startsWith(originalPath, `/${Routes.EDIT}`)) ? '/' : originalPath;
      const scrolltop = scrollTops[path] || 0;
      log.debug(`path: ${path} => scrollTop: ${scrolltop}`);
      $(window).scrollTop(scrolltop);
    }
  });
}

/**
 * When we change tiles we want to replace the history, so going back will close the app.
 * However if we're in /edit or /tiles we'd end up replacing just that path, so going back would go
 * back to /.
 * To sort out the history we need to go back _then_ replace, however react-router-redux gets into
 * a race condition if you try and dispatch both at the same time.
 * Therefore put the path we actually want to go to in the store separately, then just go back here.
 * The MeView then handles sending you on to where you wanted.
 */
export function navRequest(path, dispatch) {
  if (path === window.location.pathname) {
    window.scrollTo(0, 0);
  } else if (window.location.pathname.indexOf(Routes.TILES) !== -1 ||
    window.location.pathname.indexOf(Routes.EDIT) !== -1
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

