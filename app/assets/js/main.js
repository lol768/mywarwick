import log from 'loglevel';
// only 'warn' otherwise
log.enableAll(false);

import $ from 'jquery';

import localforage from 'localforage';

import React from 'react';
import ReactDOM from 'react-dom';

import attachFastClick from 'fastclick';

import Application from './components/Application';
import UtilityBar from './components/ui/UtilityBar';
import ID7Layout from './components/ui/ID7Layout';

import store from './store';
window.Store = store;
import { navigate } from './navigate';
import { initialiseState } from './push-notifications';

import { Provider } from 'react-redux';

import './update';
import './user';

import './notifications';
import './notifications-glue';

(()=> {

  localforage.config({
    name: 'Start'
  });

  // String replaced by Gulp build.
  const BUILD_TIME = "$$BUILDTIME$$";

  log.info("Scripts built at:", BUILD_TIME);

})();

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

});

store.subscribe(() => {
  var path = store.getState().get('path');

  if (path != currentPath) {
    currentPath = path;

    if (window.history.pushState) {
      window.history.pushState(null, null, currentPath);
    }

  }
});


/*
 Attempt to register service worker, to handle push notifications
 */
if ('serviceWorker' in navigator) {
  navigator.serviceWorker.register('/service-worker.js')
    .then(initialiseState);
} else {
  console.warn('Service workers aren\'t supported in this browser.');
}

