/**
 * API for native (iOS) app.
 */

import $ from 'jquery';
import * as stream from './stream';
import { push } from 'react-router-redux';
import { displayUpdateProgress } from './state/update';
import url from 'url';
import { postJsonWithCredentials } from './serverpipe';

/**
 * Factory method for bridge so you can create an instance
 * with different dependencies.
 */
export default function init(opts) {
  const { store, tiles } = opts;

  const searchRoot = url.parse($('#app-container').attr('data-search-root-url'));
  const searchOrigin = `${searchRoot.protocol}//${searchRoot.host}`;

  let appState = {
    // Origins which serve pages to be rendered within the context of the
    // Start application.  Pages from other hosts are displayed in an
    // external web view that does not share cookies.
    applicationOrigins: [
      'https://websignon.warwick.ac.uk',
      searchOrigin,
    ],
  };

  window.Start = {

    APP: {},

    navigate(path) {
      // click event to dismiss active tooltips
      document.dispatchEvent(new Event('click'));
      store.dispatch(push(path));
    },

    search(query) {
      // This will be better once we revisit Search in Start - quick bodge for now
      this.navigate('/');
      this.navigate(`/search?q=${encodeURIComponent(query)}`);
    },

    appToForeground() {
      store.dispatch(tiles.fetchTileContent());
      store.dispatch(displayUpdateProgress);
    },

    registerForAPNs(deviceToken) {
      postJsonWithCredentials('/api/push/apns/subscribe', { deviceToken });
    },

  };

  function update(state) {
    appState = {
      ...appState,
      ...state,
    };
    window.Start.APP = appState;
    window.location = 'start://';
  }

  function setAppCached() {
    update({
      isAppCached: true,
    });
  }

  const $html = $('html');
  const userAgent = window.navigator.userAgent;

  if (userAgent.indexOf('Android') >= 0) {
    $html.addClass('android');
  }

  if (userAgent.indexOf('WarwickStart/') >= 0) {
    $html.addClass('app standalone');

    store.subscribe(() => {
      const state = store.getState();

      update({
        unreadNotificationCount:
          stream.getNumItemsSince(
            state.notifications,
            state.notificationsLastRead
          ),
        currentPath: window.location.pathname,
        isUserLoggedIn: state.user.data.usercode !== undefined,
        tabBarHidden: state.ui.className !== 'mobile',
        user: state.user.data,
        ssoUrls: state.user.links,
      });
    });

    if ('applicationCache' in window) {
      window.applicationCache.addEventListener('cached', setAppCached);
      window.applicationCache.addEventListener('noupdate', setAppCached);
      window.applicationCache.addEventListener('updateready', setAppCached);

      if (window.applicationCache.status === window.applicationCache.IDLE) {
        setAppCached();
      }
    }
  }
}
