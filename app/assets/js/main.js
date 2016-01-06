import attachFastClick from 'fastclick';
import $ from 'jquery';
import localforage from 'localforage';
import log from 'loglevel';
import { polyfill } from 'es6-promise';
import { Provider } from 'react-redux';
import ReactDOM from 'react-dom';
import React from 'react';

log.enableAll(false);
polyfill();

import Application from './components/Application';
import UtilityBar from './components/ui/UtilityBar';
import ID7Layout from './components/ui/ID7Layout';

import store from './store';
window.Store = store;
import { navigate } from './navigate';

import './update';
import './user';

import './notifications';
import './notifications-glue';

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

});

store.subscribe(() => {
  var path = store.getState().get('path');

  if (path != currentPath) {
    currentPath = path;

    if ('pushState' in window.history) {
      window.history.pushState(null, null, currentPath);
    }
  }
});

if ('serviceWorker' in navigator) {
  navigator.serviceWorker.register('/service-worker.js');
}

import { receivedNotification, receivedActivity } from './serverpipe';
import { userReceive } from './user';
import SocketDatapipe from './SocketDatapipe';
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

// TODO these initial fetches might happen someplace more appropriate
import { fetchWhoAmI, fetchActivities } from './serverpipe';
store.dispatch(fetchWhoAmI());
store.dispatch(fetchActivities());

import { getNotificationsFromLocalStorage, getActivitiesFromLocalStorage, persistActivitiesSelect, persistNotificationsSelect } from './notifications-glue';

store.subscribe(() => persistActivitiesSelect(store.getState()));
store.subscribe(() => persistNotificationsSelect(store.getState()));

store.dispatch(getNotificationsFromLocalStorage());
store.dispatch(getActivitiesFromLocalStorage());

import { getTilesFromLocalStorage, persistTilesSelect } from './tiles';

store.dispatch(getTilesFromLocalStorage());
store.subscribe(() => persistTilesSelect(store.getState()));

import { displayUpdateProgress } from './update';

store.dispatch(displayUpdateProgress());