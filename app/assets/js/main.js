/* eslint-env browser */

import $ from 'jquery';
import _ from 'lodash-es';
import moment from 'moment';
import localforage from './localdata';

import React from 'react';
import ReactDOM from 'react-dom';
import { Provider } from 'react-redux';
import { syncHistoryWithStore } from 'react-router-redux';
import fetch from 'isomorphic-fetch';
import log from 'loglevel';

import { persistNotificationsLastRead } from './notifications-glue';
import * as pushNotifications from './push-notifications';
import * as userinfo from './userinfo';
import persistedLib from './persisted';
import SocketDatapipe from './SocketDatapipe';
import * as notifications from './state/notifications';
import * as notificationMetadata from './state/notification-metadata';
import * as app from './state/app';
import * as tiles from './state/tiles';
import * as update from './state/update';
import * as user from './state/user';
import * as news from './state/news';
import * as newsCategories from './state/news-categories';
import * as newsOptIn from './state/news-optin';
import * as colourSchemes from './state/colour-schemes';
import * as ui from './state/ui';
import * as device from './state/device';
import * as doNotDisturb from './state/do-not-disturb';
import * as analytics from './analytics';
import * as stream from './stream';
import * as timetableAlarms from './state/timetable-alarms';
import * as eap from './state/eap';
import store, { browserHistory } from './store';
import AppRoot from './components/AppRoot';
import bridge from './bridge';
import { hasAuthoritativeAuthenticatedUser, hasAuthoritativeUser } from './state';

export function launch(userData) {
  bridge({ store, tiles, notifications, userinfo, news });

  // Expose to the world for debugging purposes
  // (no harm in having this in production)
  window.localforage = localforage;

  // sets and gets are implicitly wrapped in ready() waiting so no need to
  // wait on this promise for any ops.
  localforage.ready()
    .then(() => {
      log.info('localforage DB info:', localforage._dbInfo);
    });

  const history = syncHistoryWithStore(browserHistory, store);
  history.listen(location =>
    ((location !== undefined) ? analytics.track(location.pathname) : null),
  );

  if ('scrollRestoration' in history) {
    history.scrollRestoration = 'manual';
  }

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

    // Body element never scrolls on touch, main content scrolls, unless editing tiles
    document.body.addEventListener('touchmove', (e) => {
      const isMainContent = $(e.target).parents().addBack().hasClass('id7-main-content-area');
      const isDragging = $('.id7-main-content-area').hasClass('is-dragging');
      if (isMainContent && !isDragging) {
        e.stopPropagation();
        closeTooltips();
        return;
      }
      e.preventDefault();
    }, {
      passive: false,
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

        if (window.addEventListener) {
          window.addEventListener('beforeunload', () => {
            // We're about to reload the page
            // NEWSTART-1606: forcibly activate new SW
            if (reg.waiting) {
              reg.waiting.postMessage('force-activate');
            }
          });
        }

        reg.onupdatefound = () => { // eslint-disable-line no-param-reassign
          const installingWorker = reg.installing;

          installingWorker.onstatechange = () => {
            if (installingWorker.state === 'installed') {
              fetch('/service/revision')
                .then(res => res.text())
                .then(rev => store.dispatch(app.updateAssets(rev)))
                .catch(e => log.error('Error fetching revision information', e))
                .then(() => {
                  if (navigator.serviceWorker.controller) {
                    // The new service worker is ready to go, but there's an old service worker
                    // handling network operations.  Notify the user to refresh.
                    store.dispatch(update.updateReady());
                  }
                });
            }
          };
        };
      });

    navigator.serviceWorker.addEventListener('message', (event) => {
      if (!event.data) {
        return;
      }

      if (event.data === 'reload-window') {
        window.location.reload();
      }
    });
  }

  SocketDatapipe.subscribe((data) => {
    if (Object.keys(data).indexOf('user') !== -1) {
      log.debug('Got a user with this message, checking it is as expected...');
      if (!userData.user.authenticated || userData.user.usercode !== data.user) {
        // Division!
        log.info('User mismatch, reconnecting the WebSocket...');
        SocketDatapipe.reconnect();
        return; // don't process this message
      }
    }
    switch (data.type) {
      case 'activity':
        store.dispatch(data.activity.notification ?
          notifications.receivedNotification(data.activity)
          : notifications.receivedActivity(data.activity));
        break;
      default:
      // nowt
    }
  });

  /*
   Request that storage should be persistent if it isn't already
   */
  if ('storage' in navigator && 'persist' in navigator.storage) {
    navigator.storage.persisted().then((persistent) => {
      if (persistent) {
        log.info('Persistent storage not requested as store is already persistent');
      } else {
        navigator.storage.persist().then((granted) => {
          if (granted) {
            log.info('Storage will not be cleared except by explicit user action');
          } else {
            log.info('Storage may be cleared by the UA under storage pressure');
          }
        });
      }
    });
  }


  const freezeDate = d => ((!!d && 'format' in d) ? d.format() : d);
  const thawDate = d => (d ? moment(d) : d);

  const persisted = persistedLib({ store, localforage });

  ui.subscribeToStore(store);

  persisted('notificationsLastRead.date', notificationMetadata.loadedNotificationsLastRead,
    freezeDate, thawDate);

  persisted('activities', notifications.fetchedActivities, stream.freeze);
  persisted('notifications', notifications.fetchedNotifications, stream.freeze);

  persisted('tiles.data', tiles.fetchedTiles);
  persisted('tileContent', tiles.loadedAllTileContent);
  persisted('tileContent', tiles.loadedAllTileContent);
  persisted(
    'colourSchemes',
    colourSchemes.receive,
    ({ chosen, schemes, isHighContrast }) => ({ chosen, schemes, isHighContrast }),
    ({ chosen, schemes, isHighContrast }) => ({ data: { chosen, schemes, isHighContrast } }),
  ).then(() => store.dispatch(colourSchemes.loaded()));
  persisted(
    'ui',
    ui.updateColourTheme,
    ({ colourTheme, schemeColour }) => ({ colourTheme, schemeColour }),
    ({ colourTheme, schemeColour }) => ({ colourTheme, schemeColour }),
  );
  persisted('eap', eap.receive, ({ enabled }) => ({ enabled }));

  persisted(
    'newsCategories',
    newsCategories.receive,
    ({ items, subscribed }) => ({ items, subscribed }),
    ({ items, subscribed }) => ({ data: { items, subscribed } }),
  );
  persisted('newsOptIn', newsOptIn.receive, ({ options, selected }) => ({ options, selected }));

  persisted('app.assets', app.loadAssets)
    .then(() => {
      if (store.getState().app.assets.revision === null) {
        return fetch('/service/revision')
          .then(res => res.text())
          .then(rev => store.dispatch(app.loadAssets({ revision: rev })))
          .catch(e => log.error('Error fetching current revision information', e));
      }

      return Promise.resolve();
    })
    .then(() => store.dispatch(app.promoteNextRevision()));

  const persistedUserLinks = persisted('user.links', user.receiveSSOLinks);

  persisted('timetableAlarms', timetableAlarms.update).then(() => {
    store.dispatch(timetableAlarms.setNative);
  });

  persisted('doNotDisturb', doNotDisturb.receive, ({ enabled, start, end }) => ({ enabled, start, end }));

  /** Initial requests for data */

  const loadDataFromServer = _.once(() => {
    store.dispatch(tiles.fetchTiles());
    if (hasAuthoritativeAuthenticatedUser(store.getState())) {
      store.dispatch(colourSchemes.fetch());
      store.dispatch(notifications.fetch());
    }
  });

  function checkForAuthoritativeUser() {
    const shouldLoad = hasAuthoritativeUser(store.getState());
    if (shouldLoad) {
      loadDataFromServer();
    }
    return shouldLoad;
  }

  // If user has already arrived:
  checkForAuthoritativeUser();
  // If user is going to get updated in store later:
  const unsubscribe = store.subscribe(() => {
    if (checkForAuthoritativeUser()) {
      unsubscribe();
    }
  });

  store.dispatch(ui.updateUIContext());
  store.dispatch(update.displayUpdateProgress);
  window.addEventListener('offline', () => {
    store.dispatch(device.updateOnlineStatus(false));
  });
  window.addEventListener('online', () => {
    store.dispatch(device.updateOnlineStatus(true));
  });
  store.subscribe(() => persistNotificationsLastRead(store.getState()));

  user.loadUserFromLocalStorage(store.dispatch);

  const userInfoPromise = userData ?
    Promise.resolve(userData) :
    userinfo.fetchUserInfo().catch(e => log.warn('Failed to fetch user info:', e));

  // ensure local version is written first, then remote version if available.
  persistedUserLinks
    .then(() => userInfoPromise)
    .then((data) => { if (data) userinfo.receiveUserInfo(data); })
    .catch(e => log.warn('Failed to receive user info:', e));

  // Refresh all tile content every five minutes
  setInterval(() => {
    if (navigator.onLine) {
      store.dispatch(tiles.fetchTileContent());
    }
  }, 5 * 60 * 1000);

  // Refresh news every hour
  setInterval(() => {
    if (navigator.onLine) {
      store.dispatch(news.refresh());
    }
  }, 60 * 60 * 1000);

  // Just for access from the console
  window.Store = store;

  // Actually render the app
  const appContainer = $('#app-container');
  ReactDOM.render(
    <Provider store={store}>
      <AppRoot history={history} />
    </Provider>,
    appContainer.get(0),
  );

  if (window.myWarwickErrorHandler) {
    // Don't say "there was a problem loading My Warwick" once we're past the initial page load,
    // even if some errors happen. We should already have some content.
    window.removeEventListener('error', window.myWarwickErrorHandler, true);
  }

  $('#error-fallback').hide();
}
