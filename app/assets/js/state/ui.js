/* eslint-env browser */

import log from 'loglevel';
import $ from 'jquery';
import { goBack, replace } from 'react-router-redux';
import _ from 'lodash-es';
import { Routes } from '../components/AppRoot';
import { createSelector } from 'reselect';

/* eslint-disable */
let mq;
try {
  mq = require('modernizr').mq;
} catch (e) {
  log.warn('modernizr not present, using fallback.');
  mq = () => global.mqResult;
}
/* eslint-enable */

function isNative() {
  return ('navigator' in window) && navigator.userAgent.indexOf('MyWarwick/') > -1;
}

const initialState = {
  layoutWidth: 2, // 2 columns by default
  colourTheme: 'transparent-1',
  schemeColour: '#8C6E96',
  native: false,
};

export function reducer(state = initialState, action) {
  switch (action.type) {
    case 'ui.native':
      if (action.native !== state.native) return { ...state, native: action.native };
      return state;
    case 'ui.navRequest':
      return { ...state, navRequest: action.navRequest };
    case 'ui.theme':
      return { ...state, colourTheme: action.colourTheme, schemeColour: action.schemeColour };
    default:
      return state;
  }
}

export function updateColourTheme(payload) {
  return {
    type: 'ui.theme',
    ...payload,
  };
}

export function updateUIContext() {
  return (dispatch, getState) => {
    const state = getState();
    const native = isNative();

    if (native !== state.ui.native) {
      dispatch({
        type: 'ui.native',
        native,
      });
    }
  };
}

export function subscribeToStore(store) {
  const themeSelector = createSelector(
    state => state.colourSchemes.chosen,
    (chosenId) => {
      const chosenScheme = _.find(
        store.getState().colourSchemes.schemes,
        scheme => scheme.id === chosenId,
      );
      store.dispatch(updateColourTheme({
        colourTheme: `transparent-${chosenId}`,
        schemeColour: chosenScheme.schemeColour,
      }));
    },
  );
  store.subscribe(() => {
    const state = store.getState();
    themeSelector(state);
  });
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

