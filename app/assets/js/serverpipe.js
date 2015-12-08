import store from './store';
import SocketDatapipe from './SocketDatapipe';

import fetch from 'isomorphic-fetch';
import { polyfill } from 'es6-promise';
polyfill();

import { USER_RECEIVE } from './user';
import { NEWS_FETCH, NEWS_FETCH_SUCCESS, NEWS_FETCH_FAILURE } from './news';
import { TILES_FETCH, TILES_CONFIG_RECEIVE, TILE_CONTENT_RECEIVE, TILES_FETCH_FAILURE, receivedTilesConfig, receivedTileContent } from './tiles';
import { receivedActivity, fetchedActivities, receivedNotification, fetchedNotifications } from './notifications';

//                       //
//     MESSAGE SEND      //
//                       //

export function fetchWhoAmI() {
  SocketDatapipe.send({
    tileId: "1",
    data: {
      type: 'who-am-i'
    }
  });
}

export function fetchNews() {
  return dispatch => {
    dispatch({type: NEWS_FETCH});
    return fetch('/news/feed')
      .then(response => response.json())
      .then(json => dispatch({type: NEWS_FETCH_SUCCESS, items: json.items}))
      .catch(err => dispatch({type: NEWS_FETCH_FAILURE}));
  }
}

function fetchWithCredentials(url) {
  return fetch(url, {
    credentials: 'same-origin'
  });
}

export function fetchTilesConfig() {
  return dispatch => {
    dispatch({type: TILES_FETCH});

    return fetchWithCredentials('/api/tiles')
      .then(response => response.json())
      .then(json => dispatch(receivedTilesConfig(json.tiles)))
      .catch(err => dispatch({type: TILES_FETCH_FAILURE}));
  }
}

import _ from 'lodash';

export function fetchTileContent() {
  fetchWithCredentials('/api/tileContent?tileId=' + tileId)
  .then(response => response.json())
  .then(json => dispatch(receivedTileContent(json.tileContent)))
}

export function fetchActivities() {
  fetchWithCredentials('/api/streams/user')
    .then(response => response.json())
    .then(json => {
      let notifications = _.filter(json.activites, (a) => a.notification);
      let activities = _.filter(json.activites, (a) => !a.notification);

      store.dispatch(fetchedNotifications(notifications));
      store.dispatch(fetchedActivities(activities));
    });
}

//                       //
//    MESSAGE RECEIVE    //
//                       //

SocketDatapipe.getUpdateStream().subscribe((data) => {
  switch (data.type) {
    case 'activity':
      store.dispatch(data.activity.notification ? receivedNotification(data.activity) : receivedActivity(data.activity));
      break;
    case 'who-am-i':
      store.dispatch({
        type: USER_RECEIVE,
        data: data['user-info']
      });
      break;
    default:
    // nowt
  }
});
