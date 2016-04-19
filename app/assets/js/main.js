import 'core-js/modules/es6.object.assign';

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
import * as notifications from './notifications';
import * as notificationMetadata from './notification-metadata';
import * as notificationsGlue from './notifications-glue';
import * as pushNotifications from './push-notifications';
import * as serverpipe from './serverpipe';
import * as tiles from './tiles';
import * as update from './update';
import * as user from './user';
import persisted from './persisted';
import SocketDatapipe from './SocketDatapipe';
import { Router, Route, IndexRoute, IndexRedirect, browserHistory } from 'react-router';
import { syncHistoryWithStore, routerReducer } from 'react-router-redux';
import initErrorReporter from './errorreporter';
import { registerReducer } from './reducers';
import NewsView from './components/views/NewsView';
import MeView from './components/views/MeView';
import TileView from './components/views/TileView';
import ActivityView from './components/views/ActivityView';
import NotificationsView from './components/views/NotificationsView';
import SearchView from './components/views/SearchView';
import './bridge';
import * as analytics from './analytics';

log.enableAll(false);
es6Promise.polyfill();
initErrorReporter();

localforage.config({
  name: 'Start',
});

registerReducer('routing', routerReducer);

const history = syncHistoryWithStore(browserHistory, store, {
  selectLocationState: state => state.get('routing'),
});

history.listen(location => analytics.track(location.pathname));

$.getJSON('/ssotest', shouldRedirect => {
  if (shouldRedirect) window.location = window.SSO.LOGIN_URL;
});

$(() => {
  attachFastClick(document.body);

  window.addEventListener('online', () =>
    store.dispatch(serverpipe.fetchActivities())
  );

  ReactDOM.render(
    <Provider store={store}>
      <Router history={history}>
        <Route path="/" component={Application}>
          <IndexRoute component={MeView}/>
          <Route path="tiles" component={MeView}>
            <IndexRedirect to="/"/>
            <Route path=":id" component={TileView}/>
          </Route>
          <Route path="notifications" component={NotificationsView}/>
          <Route path="activity" component={ActivityView}/>
          <Route path="news" component={NewsView}/>
          <Route path="search" component={SearchView}/>
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
    trigger: 'click',
  });

  $(document).on('click', (e) => {
    if ($(e.target).data('toggle') === 'tooltip') {
      if (!$(e.target).hasClass('tooltip-active')) {
        $('.tooltip-active').tooltip('hide').toggleClass('tooltip-active');
        $(e.target).toggleClass('tooltip-active').tooltip('toggle');
      }
    } else {
      $('[data-toggle="tooltip"]').tooltip('hide').removeClass('tooltip-active');
    }
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

const freezeDate = (d) => (!!d && 'format' in d) ? d.format() : d;
const thawDate = (d) => !!d ? moment(d) : d;

persisted('notificationsLastRead.date', notificationMetadata.loadedNotificationsLastRead,
  freezeDate, thawDate);

persisted('activities', notifications.fetchedActivities, freezeStream);
persisted('notifications', notifications.fetchedNotifications, freezeStream);

persisted('tiles.items', tiles.fetchedTiles);
persisted('tileContent', tiles.loadedAllTileContent);

store.subscribe(() => notificationsGlue.persistNotificationsLastRead(store.getState()));
