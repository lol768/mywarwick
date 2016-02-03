import _ from 'lodash';

import SocketDatapipe from './SocketDatapipe';

import $ from 'jquery';
import fetch from 'isomorphic-fetch';
import moment from 'moment';

import { NEWS_FETCH, NEWS_FETCH_SUCCESS, NEWS_FETCH_FAILURE } from './news';
import { TILES_FETCH, TILES_FETCH_SUCCESS, TILE_CONTENT_FETCH, TILE_CONTENT_FETCH_SUCCESS, TILES_FETCH_FAILURE, TILE_CONTENT_FETCH_FAILURE, fetchedTiles, fetchedTileContent, failedTileContentFetch } from './tiles';
import { receivedActivity, fetchedActivities, receivedNotification, fetchedNotifications } from './notifications';
import * as notificationMetadata from './notification-metadata'

//                       //
//     MESSAGE SEND      //
//                       //

export function fetchUserIdentity() {
  return () => {
    SocketDatapipe.send({
      tileId: "1",
      data: {
        type: 'who-am-i'
      }
    });
  };
}

export function fetchNews() {
  return dispatch => {
    dispatch({type: NEWS_FETCH});
    return fetch('/news/feed')
      .then(response => response.json())
      .then(json => {
        if (json.items !== undefined)
          dispatch({type: NEWS_FETCH_SUCCESS, items: json.items});
        else
          throw new Error('Invalid response returned from news feed');
      })
      .catch(err => dispatch({type: NEWS_FETCH_FAILURE}));
  }
}

function fetchWithCredentials(url) {
  return fetch(url, {
    credentials: 'same-origin'
  });
}

export function persistTiles() {
  return (dispatch, getState) => {
    let tiles = getState().getIn(['tiles', 'items']).map(item => ({
      tileId: item.get('id'),
      size: item.get('size'),
      preferences: item.get('preferences'),
      removed: false
    })).toJS();

    fetch('/api/tiles', {
      credentials: 'same-origin',
      method: 'PUT',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({tiles: tiles})
    });
  };
}

export function fetchTiles() {
  return dispatch => {
    dispatch({type: TILES_FETCH});

    return fetchWithCredentials('/api/tiles')
      .then(response => response.json())
      .then(json => dispatch(fetchedTiles(json.data)))
      .catch(err => dispatch({type: TILES_FETCH_FAILURE}));
  }
}

const NETWORK_ERRORS = [
  {
    id: 'network',
    message: 'Unable to contact the server.'
  }
];

const ALL_TILES = undefined;

export function fetchTileContent(tileId = ALL_TILES) {
  return dispatch => {
    dispatch({
      type: TILE_CONTENT_FETCH,
      tile: tileId
    });

    let endpoint = tileId ? `/api/tiles/content/${tileId}` : '/api/tiles/content';

    fetchWithCredentials(endpoint)
      .then(response => response.json())
      .then(json => {
        _.each(json.data, (result, tile) => {
          if (result.content) {
            dispatch(fetchedTileContent(tile, result.content));
          } else {
            dispatch(failedTileContentFetch(tile, result.errors));
          }
        });
      })
      .catch(err => {
        console.warn('Tile fetch failed because', err);
        dispatch({
          type: TILE_CONTENT_FETCH_FAILURE,
          tile: tileId,
          errors: NETWORK_ERRORS
        })
      });
  }
}

export function fetchActivities() {
  return dispatch => {
    fetchWithCredentials('/api/streams/user')
      .then(response => response.json())
      .then(json => {
        let notifications = _.filter(json.data.activities, (a) => a.notification);
        let activities = _.filter(json.data.activities, (a) => !a.notification);

        dispatch(fetchedNotifications(notifications));
        dispatch(fetchedActivities(activities));
      });

    fetchWithCredentials('/api/streams/read')
      .then(response => response.json())
      .then(json => {
        if (json.data.notificationsRead) dispatch(notificationMetadata.readNotifications(moment(json.data.notificationsRead)));
        if (json.data.activitiesRead) dispatch(notificationMetadata.readActivities(moment(json.data.activitiesRead)));
      });
  }
}
