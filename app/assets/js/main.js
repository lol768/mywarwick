import 'core-js/modules/es6.object.assign';

import initErrorReporter from './errorreporter';
initErrorReporter();

import attachFastClick from 'fastclick';
import $ from 'jquery';
import _ from 'lodash';
import localforage from 'localforage';
import moment from 'moment';
import log from 'loglevel';
import * as es6Promise from 'es6-promise';
import { Provider } from 'react-redux';
import ReactDOM from 'react-dom';
import React from 'react';
import store from './store';
import Application from './components/Application';
import * as notificationsGlue from './notifications-glue';
import * as pushNotifications from './push-notifications';
import * as serverpipe from './serverpipe';

import * as notifications from './state/notifications';
import * as notificationMetadata from './state/notification-metadata';
import * as tiles from './state/tiles';
import * as update from './state/update';
import * as user from './state/user';
import * as ui from './state/ui';

import persisted from './persisted';
import SocketDatapipe from './SocketDatapipe';
import { Router, Route, IndexRoute, IndexRedirect, browserHistory } from 'react-router';
import { syncHistoryWithStore } from 'react-router-redux';

import NewsView from './components/views/NewsView';
import MeView from './components/views/MeView';
import TileView from './components/views/TileView';
import ActivityView from './components/views/ActivityView';
import NotificationsView from './components/views/NotificationsView';
import SearchView from './components/views/SearchView';
import * as analytics from './analytics';

import './bridge';

log.enableAll(false);
es6Promise.polyfill();

localforage.config({
  name: 'Start',
});

const history = syncHistoryWithStore(browserHistory, store, {
  selectLocationState(state) {
    return state.get('routing').toJS();
  },
});

history.listen(location => analytics.track(location.pathname));

store.dispatch(ui.updateUIContext());
$(() => {
  $(window).on('resize', () => store.dispatch(ui.updateUIContext()));
});

$.getJSON('/ssotest', shouldRedirect => {
  if (shouldRedirect) window.location = window.SSO.LOGIN_URL;
});

$(() => {
  attachFastClick(document.body);

  window.addEventListener('online', () =>
    store.dispatch(notifications.fetch())
  );

  ReactDOM.render(
    <Provider store={store}>
      <Router history={history}>
        <Route path="/" component={Application}>
          <IndexRoute component={MeView} />
          <Route path="tiles" component={MeView}>
            <IndexRedirect to="/" />
            <Route path=":id" component={TileView} />
          </Route>
          <Route path="notifications" component={NotificationsView} />
          <Route path="activity" component={ActivityView} />
          <Route path="news" component={NewsView} />
          <Route path="search" component={SearchView} />
        </Route>
      </Router>
    </Provider>,
    document.getElementById('app-container'));

  if (window.navigator.userAgent.indexOf('Mobile') >= 0) {
    $('html').addClass('mobile');
  }

  $(document).tooltip({
    selector: '.toggle-tooltip',
    container: 'body',
    trigger: 'hover click',
  });
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

update.displayUpdateProgress(store.dispatch);

const freezeStream = stream => stream.valueSeq().flatten().toJS();

const loadPersonalisedDataFromServer = _.once(() => {
  store.dispatch(notifications.fetch());
  store.dispatch(tiles.fetchTiles());
  store.dispatch(tiles.fetchTileContent());
});

store.subscribe(() => {
  const u = store.getState().get('user');

  if (u && u.get('authoritative') === true) {
    loadPersonalisedDataFromServer();
  }
});

store.dispatch(serverpipe.fetchUserIdentity());

const freezeDate = (d) => ((!!d && 'format' in d) ? d.format() : d);
const thawDate = (d) => (!!d ? moment(d) : d);

persisted('notificationsLastRead.date', notificationMetadata.loadedNotificationsLastRead,
  freezeDate, thawDate);

persisted('activities', notifications.fetchedActivities, freezeStream);
persisted('notifications', notifications.fetchedNotifications, freezeStream);

persisted('tiles.data', tiles.fetchedTiles);
persisted('tileContent', tiles.loadedAllTileContent);

store.subscribe(() => notificationsGlue.persistNotificationsLastRead(store.getState()));

window.Store = store;
