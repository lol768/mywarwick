// In case of JS error, hide the loading spinner.
// Has to be up top to catch all possible JS errors.
if (window.addEventListener) {
  window.addEventListener('error', () => {
    document.getElementById('react-app-spinner').style.display = 'none';
  }, true);
}

import 'core-js/modules/es6.object.assign';

import initErrorReporter from './errorreporter';
initErrorReporter();

import $ from 'jquery';
import _ from 'lodash';
import moment from 'moment';
import log from 'loglevel';
import * as es6Promise from 'es6-promise';
import localforage from 'localforage';

import React from 'react';
import ReactDOM from 'react-dom';
import { browserHistory } from 'react-router';
import { syncHistoryWithStore } from 'react-router-redux';

import * as notificationsGlue from './notifications-glue';
import * as pushNotifications from './push-notifications';
import * as serverpipe from './serverpipe';
import persistedLib from './persisted';
import SocketDatapipe from './SocketDatapipe';
import * as notifications from './state/notifications';
import * as notificationMetadata from './state/notification-metadata';
import * as tiles from './state/tiles';
import * as update from './state/update';
import * as user from './state/user';
import * as ui from './state/ui';
import * as device from './state/device';
import * as analytics from './analytics';
import store from './store';
import AppRoot from './components/AppRoot';
import bridge from './bridge';
import { hasAuthoritativeUser, hasAuthoritativeAuthenticatedUser } from './state';

bridge({ store, tiles, notifications });

log.enableAll(false);
es6Promise.polyfill();

localforage.config({
  name: 'Start',
});

const history = syncHistoryWithStore(browserHistory, store);
history.listen(location => analytics.track(location.pathname));

$(() => {
  $(window).on('contextmenu', () => window.navigator.userAgent.indexOf('Mobile') < 0);

  $(window).on('resize', () => store.dispatch(ui.updateUIContext()));

  $(window).on('deviceorientation resize', () => store.dispatch(device.updateDeviceWidth()));

  $(window).on('online', () => {
    store.dispatch(tiles.fetchTileContent());

    if (hasAuthoritativeAuthenticatedUser(store.getState())) {
      store.dispatch(notifications.fetch());
    }
  });

  if (window.navigator.userAgent.indexOf('Mobile') >= 0) {
    $('html').addClass('mobile');
  }

  $(document).tooltip({
    selector: '.toggle-tooltip',
    container: '.id7-main-content-area',
    trigger: 'click',
  });

  function closeTooltips() {
    $('.tooltip-active').tooltip('hide').removeClass('tooltip-active');
  }

  // Prevent the body element from scrolling on touch.
  $(document.body).on('touchmove', (e) => e.preventDefault());
  $(document.body).on('touchmove', '.id7-main-content-area', (e) => {
    e.stopPropagation();
    closeTooltips();
  });

  $(document).on('click', (e) => {
    if ($(e.target).data('toggle') === 'tooltip') {
      if (!$(e.target).hasClass('tooltip-active')) {
        // hide active tooltips after clicking on a non-active tooltip
        closeTooltips();
        $(e.target).toggleClass('tooltip-active').tooltip('toggle');
      }
    } else {
      // click elsewhere on body, dismiss all open tooltips
      closeTooltips();
    }
  });
});

/*
 save the initial state of notification permission to redux
  */
if ('Notification' in window) {
  store.dispatch(device.updateNotificationPermissions);
}
/*
 Attempt to register service worker, to handle push notifications and offline
 */
if ('serviceWorker' in navigator) {
  navigator.serviceWorker.register('/service-worker.js')
    .then((reg) => {
      pushNotifications.init();

      reg.onupdatefound = () => { // eslint-disable-line no-param-reassign
        const installingWorker = reg.installing;

        installingWorker.onstatechange = () => {
          if (installingWorker.state === 'installed' && navigator.serviceWorker.controller) {
            // The new service worker is ready to go, but there's an old service worker
            // handling network operations.  Notify the user to refresh.
            store.dispatch(update.updateReady());
          }
        };
      };
    });
}

SocketDatapipe.subscribe(data => {
  switch (data.type) {
    case 'activity':
      store.dispatch(data.activity.notification ? notifications.receivedNotification(data.activity)
        : notifications.receivedActivity(data.activity));
      break;
    default:
      // nowt
  }
});

/** Fetching/storing locally persisted data */

function freezeStream({ stream, olderItemsOnServer }) {
  return {
    items: _(stream).values().flatten().value(),
    olderItemsOnServer,
  };
}

const freezeDate = (d) => ((!!d && 'format' in d) ? d.format() : d);
const thawDate = (d) => (!!d ? moment(d) : d);

const persisted = persistedLib({ store, localforage });

persisted('notificationsLastRead.date', notificationMetadata.loadedNotificationsLastRead,
  freezeDate, thawDate);

persisted('activities', notifications.fetchedActivities, freezeStream);
persisted('notifications', notifications.fetchedNotifications, freezeStream);

persisted('tiles.data', tiles.fetchedTiles);
persisted('tileContent', tiles.loadedAllTileContent);

const persistedUserLinks = persisted('user.links', user.receiveSSOLinks);

/** Initial requests for data */

const loadDataFromServer = _.once(() => {
  store.dispatch(tiles.fetchTiles());

  if (hasAuthoritativeAuthenticatedUser(store.getState())) {
    store.dispatch(notifications.fetch());
  }
});

const unsubscribe = store.subscribe(() => {
  if (hasAuthoritativeUser(store.getState())) {
    loadDataFromServer();
    unsubscribe();
  }
});

store.dispatch(ui.updateUIContext());
store.dispatch(update.displayUpdateProgress);
store.subscribe(() => notificationsGlue.persistNotificationsLastRead(store.getState()));

// kicks off the whole data flow - when user is received we fetch tile data
function fetchUserInfo() {
  return serverpipe.fetchWithCredentials('/user/info');
}

function receiveUserInfo(response) {
  return response.json()
    .then(data => {
      if (data.refresh) {
        window.location = user.rewriteRefreshUrl(data.refresh, window.location.href);
      } else {
        if (!data.user.authenticated) {
          window.location = data.links.permissionDenied;
        } else {
          store.dispatch(user.userReceive(data.user));
          store.dispatch(user.receiveSSOLinks(data.links));

          const analyticsData = data.user.analytics;
          if (analyticsData !== undefined) {
            analyticsData.dimensions.forEach(dimension =>
              analytics.setDimension(dimension.index, dimension.value)
            );

            analytics.setUserId(analyticsData.identifier);
          }

          analytics.ready();
        }
      }
    })
    .catch(e => {
      setTimeout(() => fetchUserInfo().then(receiveUserInfo), 5000);
      throw e;
    });
}

user.loadUserFromLocalStorage(store.dispatch);
fetchUserInfo().then(res =>
  // ensure local version is written first, then remote version if available.
  persistedUserLinks.then(() =>
    receiveUserInfo(res)
  )
);

// Refresh all tile content every five minutes
setInterval(() => {
  if (navigator.onLine) {
    store.dispatch(tiles.fetchTileContent());
  }
}, 5 * 60 * 1000);

// Just for access from the console
window.Store = store;

ui.scrollTopOnTabChange();

// Actually render the app
ReactDOM.render(
  <AppRoot history={history} />,
  document.getElementById('app-container')
);

$(() => {
  // this element contains a fallback error - once we're fairly
  // sure that this script isn't completely broken, we can hide it.
  document.getElementById('error-fallback').style.display = 'none';
});
