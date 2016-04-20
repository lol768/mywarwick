/**
 * API for native (iOS) app.
 */

import $ from 'jquery';
import Immutable from 'immutable';
import store from './store';
import { fetchTileContent } from './serverpipe';
import * as stream from './stream';
import { push } from 'react-router-redux';

let appState = Immutable.Map();

window.Start = {

  APP: {},

  navigate: (path) => store.dispatch(push(path)),

  appToForeground() {
    store.dispatch(fetchTileContent());

    if ('applicationCache' in window) {
      window.applicationCache.update();
    }
  },

  registerForAPNs(deviceToken) {
    fetch('/api/push/apns/subscribe', {
      credentials: 'same-origin',
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({ deviceToken }),
    });
  },

};

function update(state) {
  appState = appState.merge(state);
  window.Start.APP = appState.toJS();
  window.location = 'start://';
}

function setAppCached() {
  update({
    isAppCached: true,
  });
}

if (window.navigator.userAgent.indexOf('Start/') >= 0) {
  $('html').addClass('app standalone');

  store.subscribe(() => {
    const state = store.getState();

    update({
      unreadNotificationCount:
        stream.getNumItemsSince(
          state.get('notifications'),
          state.get('notificationsLastRead')
        ),
      // FIXME - remove this once app has been updated to have no unreadActivityCount
      unreadActivityCount: 0,
      unreadNewsCount: 0,
      currentPath: window.location.pathname,
      isUserLoggedIn: state.getIn(['user', 'data', 'usercode']) !== undefined,
      tabBarHidden: state.getIn(['ui', 'className']) !== 'mobile',
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
