/* global MyWarwickNative */

/**
 * API for native apps.
 */

import $ from 'jquery';
import _ from 'lodash';
import * as stream from './stream';
import { push } from 'react-router-redux';
import { displayUpdateProgress } from './state/update';
import { postJsonWithCredentials } from './serverpipe';
import { createSelector } from 'reselect';
import { hasAuthoritativeAuthenticatedUser } from './state';
import * as warwickSearch from 'warwick-search-frontend';

/**
 * Factory method for bridge so you can create an instance
 * with different dependencies.
 */
export default function init(opts) {
  const { store, tiles, notifications } = opts;

  function doInit(native) {
    const nativeSelectors = [
      createSelector(
        state => state.user,
        user => {
          if (!user.empty) {
            native.setUser(user.data);
          }
        }
      ),
      createSelector(
        state => _.get(state, 'routing.locationBeforeTransitions.pathname', '/'),
        path => native.setPath(path)
      ),
      createSelector(
        state => state.user.links,
        ({ login, logout }) => native.setWebSignOnUrls(login, logout)
      ),
      createSelector(
        state => [state.notifications.stream, state.notificationsLastRead.date],
        ([strm, lastReadDate]) => native.setUnreadNotificationCount(
          stream.getNumItemsSince(strm, lastReadDate)
        )
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

  if (userAgent.indexOf('MyWarwick/') >= 0) {
    $html.addClass('app standalone');

    if (userAgent.indexOf('Android') >= 0) {
      $html.addClass('android');
    }

    window.MyWarwick = {
      navigate(path) {
        // click event to dismiss active tooltips
        document.dispatchEvent(new Event('click'));
        store.dispatch(push(path));
        window.scrollTo(0, 0);
      },

      search(query) {
        warwickSearch.submitSearch(query);
      },

      onApplicationDidBecomeActive() {
        if (navigator.onLine) {
          store.dispatch(tiles.fetchTileContent());

          if (hasAuthoritativeAuthenticatedUser(store.getState())) {
            store.dispatch(notifications.fetch());
          }
        }
        store.dispatch(displayUpdateProgress);
      },

      registerForAPNs(deviceToken) {
        postJsonWithCredentials('/api/push/apns/subscribe', { deviceToken });
      },

      registerForFCM(deviceToken) {
        postJsonWithCredentials('/api/push/fcm/subscribe', { deviceToken });
      },
    };

    maybeInit();
  } else {
    $html.addClass('not-app');
  }
}

