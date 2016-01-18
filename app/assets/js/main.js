import attachFastClick from 'fastclick';
import { createSelector } from 'reselect';
import $ from 'jquery';
import localforage from 'localforage';
import log from 'loglevel';
import { polyfill } from 'es6-promise';
import { Provider } from 'react-redux';
import ReactDOM from 'react-dom';
import React from 'react';

log.enableAll(false);
polyfill();

import store from './store';
window.Store = store;

import Application from './components/Application';
import ID7Layout from './components/ui/ID7Layout';
import UtilityBar from './components/ui/UtilityBar';

import './notifications';
import './notifications-glue';
import './update';
import './user';

import { displayUpdateProgress } from './update';
import { fetchUserIdentity, fetchActivities } from './serverpipe';
import { getNotificationsFromLocalStorage, getActivitiesFromLocalStorage, persistActivities, persistNotifications } from './notifications-glue';
import { getTilesFromLocalStorage, persistTiles } from './tiles';
import { initialiseState } from './push-notifications';
import { navigate } from './navigate';
import { receivedNotification, receivedActivity } from './notifications';
import SocketDatapipe from './SocketDatapipe';
import { userReceive } from './user';

localforage.config({
  name: 'Start'
});

// String replaced by Gulp build.
log.info("Scripts built at: $$BUILDTIME$$");

$.getJSON('/ssotest', (shouldRedirect) => {
  if (shouldRedirect) window.location = SSO.LOGIN_URL;
});

var currentPath = '/';

$(function () {

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

  window.addEventListener('popstate', function () {
    currentPath = window.location.pathname;
    store.dispatch(navigate(window.location.pathname));
  });

  let $fixedHeader = $('.fixed-header');

  function updateFixedHeaderAtTop() {
    $fixedHeader.toggleClass('at-top', $(window).scrollTop() < 10);
  }

  $(window).on('scroll', updateFixedHeaderAtTop);
  updateFixedHeaderAtTop();

  if (window.navigator.userAgent.indexOf('Mobile') >= 0) {
    $('html').addClass('mobile');
  }

  if (window.navigator.userAgent.indexOf('Start/1.0') >= 0) {
    $('html').addClass('app standalone');
  }
});

store.subscribe(() => {
  var path = store.getState().get('path');

  if (path != currentPath) {
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
  navigator.serviceWorker.register('/offline-worker.js')
    .then(initialiseState);
} else {
  console.warn('Service workers aren\'t supported in this browser.');
}

SocketDatapipe.getUpdateStream().subscribe((data) => {
  switch (data.type) {
    case 'activity':
      store.dispatch(data.activity.notification ? receivedNotification(data.activity) : receivedActivity(data.activity));
      break;
    case 'who-am-i':
      store.dispatch(userReceive(data['user-info']));
      break;
    default:
    // nowt
  }
});

displayUpdateProgress(store.dispatch);

const loadPersonalisedData = _.once(() => {
  store.dispatch(fetchActivities());

  store.subscribe(() => persistActivities(store.getState()));
  store.subscribe(() => persistNotifications(store.getState()));
  store.dispatch(getActivitiesFromLocalStorage());
  store.dispatch(getNotificationsFromLocalStorage());

  store.subscribe(() => persistTiles(store.getState()));
  store.dispatch(getTilesFromLocalStorage());
});

store.subscribe(() => {
  let user = store.getState().get('user');

  if (user && user.get('loaded') === true)
    loadPersonalisedData();
});

store.dispatch(fetchUserIdentity());
