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
import { Router, Route, IndexRoute, browserHistory } from 'react-router';
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
polyfill();
initErrorReporter();

localforage.config({
  name: 'Start',
});

registerReducer('routing', routerReducer);

const history = syncHistoryWithStore(browserHistory, store, {
  selectLocationState: state => state.get('routing'),
});

history.listen(location => analytics.track(location.pathname));

// String replaced by Gulp build.
log.info('Scripts built at: $$BUILDTIME$$');

$.getJSON('/ssotest', shouldRedirect => {
  if (shouldRedirect) window.location = window.SSO.LOGIN_URL;
});

$(() => {
  attachFastClick(document.body);

  ReactDOM.render(
    <Provider store={store}>
      <Router history={history}>
        <Route path="/" component={Application}>
          <IndexRoute component={MeView}/>
          <Route path="tiles" component={MeView}>
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

/*
 Attempt to register service worker, to handle push notifications and offline
 */
if ('serviceWorker' in navigator) {
  navigator.serviceWorker.register('/service-worker.js')
    .then(pushNotifications.init);
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

const freezeDate = (d) => (d !== undefined && 'format' in d) ? d.format() : d;
const thawDate = (d) => (d !== undefined) ? moment(d) : d;

persisted('activities-lastRead', notificationMetadata.readActivities, freezeDate, thawDate);
persisted('notifications-lastRead', notificationMetadata.readNotifications, freezeDate, thawDate);

persisted('activities', notifications.fetchedActivities, freezeStream);
persisted('notifications', notifications.fetchedNotifications, freezeStream);

persisted('tiles.items', tiles.fetchedTiles);
persisted('tileContent', tiles.loadedAllTileContent);

store.subscribe(() => notificationsGlue.persistNotificationsMetadata(store.getState()));
