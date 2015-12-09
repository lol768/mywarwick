import store from './store';
import SocketDatapipe from './SocketDatapipe';

import fetch from 'isomorphic-fetch';
import { polyfill } from 'es6-promise';
polyfill();

import { userReceive } from './user';
import { NEWS_FETCH, NEWS_FETCH_SUCCESS, NEWS_FETCH_FAILURE } from './news';
import { TILES_FETCH, TILES_CONFIG_RECEIVE, TILES_CONTENT_RECEIVE, TILES_FETCH_FAILURE, receivedTilesConfig, receivedTilesContent } from './tiles';
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

    return fetchWithCredentials('/api/tiles/config')
      .then(response => response.json())
      .then(json => dispatch(receivedTilesConfig(json.data.tiles)))
      .catch(err => dispatch({type: TILES_FETCH_FAILURE}));
  }
}

import _ from 'lodash';

export function fetchTilesContentById(tileIds) {
  return dispatch => {
    dispatch({type: TILES_FETCH});

    let queryStr = tileIds.map((id) => `id=${id}`).join('');
    fetchWithCredentials(`/api/tiles/contentbyid?${queryStr}`)
      .then(response => response.json())
      .then(json => dispatch(receivedTilesContent(json.data)))
  }
}

export function fetchTilesContent() {
  return dispatch => {
    dispatch({type: TILES_FETCH});

    fetchWithCredentials('/api/tiles/content')
      .then(response => response.json())
      .then(json => dispatch(receivedTilesContent(json.data)))
  }
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
      store.dispatch(userReceive(data['user-info']));
      break;
    default:
    // nowt
  }
});
