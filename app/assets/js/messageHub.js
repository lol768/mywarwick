import store from './store';
import SocketDatapipe from './SocketDatapipe';

import fetch from 'isomorphic-fetch';
import { polyfill } from 'es6-promise';
polyfill();

import { USER_RECEIVE } from './user';
import { NEWS_FETCH, NEWS_FETCH_SUCCESS, NEWS_FETCH_FAILURE } from './news';
import { TILES_FETCH, TILES_RECEIVE, TILES_FETCH_FAILURE, receivedTileData } from './tiles';
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

export function fetchTileData() {
  return dispatch => {
    dispatch({type: TILES_FETCH});

    return fetch('/api/tiles')
      .then(response => response.json())
      .then(json => dispatch(receivedTileData(json.tiles)))
      .catch(err => dispatch({type: TILES_FETCH_FAILURE}));
  }
}

export function fetchNotifications() {
  SocketDatapipe.send({
    tileId: "1",
    data: {
      type: "fetch-notifications" // since last login
    }
  });
}

//                       //
//    MESSAGE RECEIVE    //
//                       //

SocketDatapipe.getUpdateStream().subscribe((data) => {
  switch (data.type) {
    case 'fetch-notifications':
      store.dispatch(fetchedNotifications(data.notifications));
      break;
    case 'notification':
      store.dispatch(receivedNotification(data));
      break;
    case 'activity':
      store.dispatch(receivedActivity(data));
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
