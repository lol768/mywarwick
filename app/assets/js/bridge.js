/* eslint-env browser */
/* global MyWarwickNative */

/**
 * API for native apps.
 */
import $ from 'jquery';
import get from 'lodash-es/get';
import { push } from 'react-router-redux';
import { createSelector } from 'reselect';
import * as stream from './stream';
import { displayUpdateProgress } from './state/update';
import { postJsonWithCredentials } from './serverpipe';
import { hasAuthoritativeAuthenticatedUser } from './state';
import { Routes } from './components/AppRoot';
import { navRequest } from './state/ui';
import { showFeedbackForm } from './userinfo';

/**
 * Factory method for bridge so you can create an instance
 * with different dependencies.
 */
export default function init(opts) {
  const { store, tiles, notifications, userinfo, news } = opts;

  function doInit(native) {
    const nativeSelectors = [
      createSelector(
        state => state.user,
        (user) => {
          if (!user.empty) {
            native.setUser({
              ...user.data,
              authoritative: user.authoritative,
            });
          }
        },
      ),
      createSelector(
        state => get(state, 'routing.locationBeforeTransitions.pathname', '/'),
        path => native.setPath(path),
      ),
      createSelector(
        state => state.user.links,
        ({ login, logout }) => native.setWebSignOnUrls(login, logout),
      ),
      createSelector(
        state => [state.notifications.stream, state.notificationsLastRead.date],
        ([strm, lastReadDate]) => native.setUnreadNotificationCount(
          stream.getNumItemsSince(strm, lastReadDate),
        ),
      ),
      createSelector(
        state => state.colourSchemes.chosen,
        state => state.colourSchemes.loaded,
        state => state.colourSchemes.isHighContrast,
        (chosen, loaded, isHighContrast) => {
          if (loaded && native.setBackgroundToDisplay) {
            native.setBackgroundToDisplay(chosen, isHighContrast);
          }
        },
      ),
    ];

    function update() {
      const state = store.getState();

      nativeSelectors.map(s => s(state));
    }

    function setAppCached() {
      native.setAppCached(true);
    }

    store.subscribe(update);
    update();

    if ('applicationCache' in window) {
      window.applicationCache.addEventListener('cached', setAppCached);
      window.applicationCache.addEventListener('noupdate', setAppCached);
      window.applicationCache.addEventListener('updateready', setAppCached);

      if (window.applicationCache.status === window.applicationCache.IDLE) {
        setAppCached();
      }
    }

    // When this gets called, the app knows that MyWarwick var is available.
    if (native.ready) {
      native.ready();
    }
  }

  function maybeInit() {
    if ('MyWarwickNative' in window) {
      doInit(MyWarwickNative);
    } else {
      // The native bridge needs a bit longer to appear
      setTimeout(maybeInit, 100);
    }
  }

  const $html = $('html');
  const userAgent = window.navigator.userAgent;

  if (userAgent.indexOf('Android') >= 0) {
    $html.addClass('android');
  }

  if (/iPad|iPhone|iPod/.test(userAgent)) {
    $html.addClass('ios');
  }

  if (navigator.standalone) {
    $html.addClass('standalone');
  }

  if (userAgent.indexOf('MyWarwick/') >= 0) {
    // 'native' is also 'mobile'. We make the same assumption in
    // ui.js - may need to change this if we want to give a different
    // experience to tablets later (we can tweak CSS with media queries).
    $html.addClass('app mobile');

    window.MyWarwick = {
      navigate(path) {
        // click event to dismiss active tooltips
        document.dispatchEvent(new Event('click'));
        if (path.indexOf(`/${Routes.EDIT}`) === 0 ||
          path.indexOf(`/${Routes.TILES}`) === 0 ||
          path.indexOf(`/${Routes.SETTINGS}`) === 0
        ) {
          store.dispatch(push(path));
        } else {
          navRequest(path, store.dispatch);
        }
      },

      search(query) {
        // lazy load the Search module
        import('warwick-search-frontend').then(s => s.submitSearch(query));
      },

      feedback(detailJson) {
        showFeedbackForm(JSON.parse(detailJson));
      },

      onApplicationDidBecomeActive() {
        if (navigator.onLine) {
          store.dispatch(tiles.fetchTiles());
          store.dispatch(news.refresh());

          if (hasAuthoritativeAuthenticatedUser(store.getState())) {
            store.dispatch(notifications.fetch());
          }

          userinfo.fetchUserInfo().then(userinfo.receiveUserInfo);
        }
        store.dispatch(displayUpdateProgress);
      },

      registerForAPNs(deviceToken) {
        postJsonWithCredentials('/api/push/apns/subscribe', { deviceToken });
      },

      registerForFCM(deviceToken) {
        postJsonWithCredentials('/api/push/fcm/subscribe', { deviceToken });
      },

      unregisterForPush(deviceToken) {
        postJsonWithCredentials('/api/push/unsubscribe', { deviceToken });
      },
    };

    maybeInit();
  } else {
    $html.addClass('not-app');
  }
}

