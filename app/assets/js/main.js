import 'core-js/modules/es6.object.assign';

import initErrorReporter from './errorreporter';
initErrorReporter();

import $ from 'jquery';
import _ from 'lodash';
import moment from 'moment';
import log from 'loglevel';
import * as es6Promise from 'es6-promise';
import attachFastClick from 'fastclick';
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
import * as analytics from './analytics';
import store from './store';
import AppRoot from './components/AppRoot';
import bridge from './bridge';

bridge({ store, tiles });

log.enableAll(false);
es6Promise.polyfill();

localforage.config({
  name: 'Start',
});

const history = syncHistoryWithStore(browserHistory, store);
history.listen(location => analytics.track(location.pathname));

$.getJSON('/ssotest', shouldRedirect => {
  if (shouldRedirect) window.location = window.SSO.LOGIN_URL;
});

$(() => {
  attachFastClick(document.body);

  window.addEventListener('resize', () => store.dispatch(ui.updateUIContext()));

  window.addEventListener('online', () =>
    store.dispatch(notifications.fetch())
  );

  if (window.navigator.userAgent.indexOf('Mobile') >= 0) {
    $('html').addClass('mobile');
  }

  $(document).tooltip({
    selector: '.toggle-tooltip',
    container: 'body',
    trigger: 'click',
  });

  $(document).on('click', (e) => {
    if ($(e.target).data('toggle') === 'tooltip') {
      if (!$(e.target).hasClass('tooltip-active')) {
        // hide active tooltips after clicking on a non-active tooltip
        $('.tooltip-active').tooltip('hide').toggleClass('tooltip-active');
        $(e.target).toggleClass('tooltip-active').tooltip('toggle');
      }
    } else {
      // click elsewhere on body, dismiss all open tooltips
      $('.toggle-tooltip').tooltip('hide').removeClass('tooltip-active');
    }
  });

  ReactDOM.render(
    <AppRoot history={history} />,
    document.getElementById('app-container')
  );
});

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
    case 'who-am-i':
      store.dispatch(user.userReceive(data.userIdentity));
      break;
    default:
      // nowt
  }
});

/** Fetching/storing locally persisted data */

const freezeStream = stream => _(stream).values().flatten().value();
const freezeDate = (d) => ((!!d && 'format' in d) ? d.format() : d);
const thawDate = (d) => (!!d ? moment(d) : d);

const persisted = persistedLib({ store, localforage });

persisted('notificationsLastRead.date', notificationMetadata.loadedNotificationsLastRead,
  freezeDate, thawDate);

persisted('activities', notifications.fetchedActivities, freezeStream);
persisted('notifications', notifications.fetchedNotifications, freezeStream);

persisted('tiles.data', tiles.fetchedTiles);
persisted('tileContent', tiles.loadedAllTileContent);


/** Initial requests for data */

const loadPersonalisedDataFromServer = _.once(() => {
  store.dispatch(notifications.fetch());
  store.dispatch(tiles.fetchTiles());
  store.dispatch(tiles.fetchTileContent());
});

store.subscribe(() => {
  const u = store.getState().user;

  if (u && u.authoritative === true) {
    loadPersonalisedDataFromServer();
  }
});

store.dispatch(ui.updateUIContext());
store.dispatch(update.displayUpdateProgress);
store.subscribe(() => notificationsGlue.persistNotificationsLastRead(store.getState()));

// kicks off the whole data flow - when user is received we fetch tile data
store.dispatch(serverpipe.fetchUserIdentity());

window.Store = store;
