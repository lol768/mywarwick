import attachFastClick from 'fastclick';
import $ from 'jquery';
import _ from 'lodash';
import localforage from 'localforage';
import moment from 'moment';
import log from 'loglevel';
import { polyfill } from 'es6-promise';
import { Provider } from 'react-redux';
import ReactDOM from 'react-dom';
import React from 'react';

log.enableAll(false);
polyfill();

import store from './store';
window.Store = store;
window.localforage = localforage;

import Application from './components/Application';
import ID7Layout from './components/ui/ID7Layout';
import UtilityBar from './components/ui/UtilityBar';

import * as notifications from './notifications';
import * as notificationMetadata from './notification-metadata';
import * as notificationsGlue from './notifications-glue';
import * as pushNotifications from './push-notifications';
import * as serverpipe from './serverpipe';
import * as tiles from './tiles';
import * as update from './update';
import * as user from './user';
import { navigate } from './navigate';
import persisted from './persisted';
import SocketDatapipe from './SocketDatapipe';
import './bridge';

localforage.config({
  name: 'Start',
});

// String replaced by Gulp build.
log.info('Scripts built at: $$BUILDTIME$$');

$.getJSON('/ssotest', shouldRedirect => {
  if (shouldRedirect) window.location = window.SSO.LOGIN_URL;
});

let currentPath = '/';

$(() => {
  attachFastClick(document.body);

  currentPath = window.location.pathname.match(/(\/[^/]*)/)[0];
  store.dispatch(navigate(currentPath));

  ReactDOM.render(
    <Provider store={store}>
      <ID7Layout utilityBar={<UtilityBar />}>
        <Application />
      </ID7Layout>
    </Provider>,
    document.getElementById('app-container'));

  window.addEventListener('popstate', () => {
    currentPath = window.location.pathname;
    store.dispatch(navigate(window.location.pathname));
  });

  const $fixedHeader = $('.fixed-header');

  function updateFixedHeaderAtTop() {
    $fixedHeader.toggleClass('at-top', $(window).scrollTop() < 10);
  }

  $(window).on('scroll', updateFixedHeaderAtTop);
  updateFixedHeaderAtTop();

  if (window.navigator.userAgent.indexOf('Mobile') >= 0) {
    $('html').addClass('mobile');
  }

  $('body').click((e) => {
    const $target = $(e.target);
    if ($target.data('toggle') !== 'tooltip' && $target.parents('.tooltip.in').length === 0) {
      $('[data-toggle="tooltip"]').tooltip('hide');
    }
  });
});

store.subscribe(() => {
  const path = store.getState().get('path');

  if (path !== currentPath) {
    currentPath = path;

    if ('pushState' in window.history) {
      window.history.pushState(null, null, currentPath);
      $(window).scrollTop(0);
    }
  }
});


/*
 Attempt to register service worker, to handle push notifications and offline
 */
if ('serviceWorker' in navigator) {
  navigator.serviceWorker.register('/service-worker.js')
    .then(pushNotifications.init);
}

SocketDatapipe.getUpdateStream().subscribe(data => {
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

update.displayUpdateProgress(store.dispatch);

const freezeStream = stream => stream.valueSeq().flatten().toJS();

const loadPersonalisedDataFromServer = _.once(() => {
  store.dispatch(serverpipe.fetchActivities());
  store.dispatch(serverpipe.fetchTiles());
  store.dispatch(serverpipe.fetchTileContent());
});

store.subscribe(() => {
  const u = store.getState().get('user');

  if (u && u.get('authoritative') === true) {
    loadPersonalisedDataFromServer();
  }
});

store.dispatch(serverpipe.fetchUserIdentity());

const freezeDate = (d) => (d !== undefined && 'format' in d) ? d.format() : d;
const thawDate = (d) => (d !== undefined) ? moment(d) : d;

persisted('activities-lastRead', notificationMetadata.readActivities, freezeDate, thawDate);
persisted('notifications-lastRead', notificationMetadata.readNotifications, freezeDate, thawDate);

persisted('activities', notifications.fetchedActivities, freezeStream);
persisted('notifications', notifications.fetchedNotifications, freezeStream);

persisted('tiles.items', tiles.fetchedTiles);
persisted('tileContent', tiles.loadedAllTileContent);

store.subscribe(() => notificationsGlue.persistNotificationsMetadata(store.getState()));
