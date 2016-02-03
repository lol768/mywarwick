import log from 'loglevel';
import _ from 'lodash';

import SocketDatapipe from './SocketDatapipe';

import fetch from 'isomorphic-fetch';
import moment from 'moment';

import { NEWS_FETCH, NEWS_FETCH_SUCCESS, NEWS_FETCH_FAILURE } from './news';
import * as tiles from './tiles';
import * as notification from './notifications';
import * as notificationMetadata from './notification-metadata';

//                       //
//     MESSAGE SEND      //
//                       //

export function fetchUserIdentity() {
  return () => {
    SocketDatapipe.send({
      tileId: '1',
      data: {
        type: 'who-am-i',
      },
    });
  };
}

export function fetchNews() {
  return dispatch => {
    dispatch({ type: NEWS_FETCH });
    return fetch('/news/feed')
      .then(response => response.json())
      .then(json => {
        if (json.items !== undefined) {
          dispatch({ type: NEWS_FETCH_SUCCESS, items: json.items });
        } else {
          throw new Error('Invalid response returned from news feed');
        }
      })
      .catch(() => dispatch({ type: NEWS_FETCH_FAILURE }));
  };
}

function fetchWithCredentials(url) {
  return fetch(url, {
    credentials: 'same-origin',
  });
}

export function persistTiles() {
  return (dispatch, getState) => {
    const result = getState().getIn(['tiles', 'items']).map(item => ({
      id: item.get('id'),
      size: item.get('size'),
      preferences: item.get('preferences'),
      removed: false,
    })).toJS();

    fetch('/api/tiles', {
      credentials: 'same-origin',
      method: 'PUT',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({ tiles: result }),
    });
  };
}

export function fetchTiles() {
  return dispatch => {
    dispatch({ type: tiles.TILES_FETCH });

    return fetchWithCredentials('/api/tiles')
      .then(response => response.json())
      .then(json => dispatch(tiles.fetchedTiles(json.data)))
      .catch(() => dispatch({ type: tiles.TILES_FETCH_FAILURE }));
  };
}

const NETWORK_ERRORS = [
  {
    id: 'network',
    message: 'Unable to contact the server.',
  },
];

const ALL_TILES = undefined;

export function fetchTileContent(tileId = ALL_TILES) {
  return dispatch => {
    dispatch({
      type: tiles.TILE_CONTENT_FETCH,
      tile: tileId,
    });

    const endpoint = tileId ? `/api/tiles/content/${tileId}` : '/api/tiles/content';

    fetchWithCredentials(endpoint)
      .then(response => response.json())
      .then(json => {
        _.each(json.data, (result, tile) => {
          if (result.content) {
            dispatch(tiles.fetchedTileContent(tile, result.content));
          } else {
            dispatch(tiles.failedTileContentFetch(tile, result.errors));
          }
        });
      })
      .catch(err => {
        log.warn('Tile fetch failed because', err);
        dispatch({
          type: tiles.TILE_CONTENT_FETCH_FAILURE,
          tile: tileId,
          errors: NETWORK_ERRORS,
        });
      });
  };
}

export function fetchActivities() {
  return dispatch => {
    fetchWithCredentials('/api/streams/user')
      .then(response => response.json())
      .then(json => {
        const notifications = _.filter(json.data.activities, (a) => a.notification);
        const activities = _.filter(json.data.activities, (a) => !a.notification);

        dispatch(notification.fetchedNotifications(notifications));
        dispatch(notification.fetchedActivities(activities));
      });

    fetchWithCredentials('/api/streams/read')
      .then(response => response.json())
      .then(json => {
        if (json.data.notificationsRead) {
          dispatch(notificationMetadata.readNotifications(moment(json.data.notificationsRead)));
        }
        if (json.data.activitiesRead) {
          dispatch(notificationMetadata.readActivities(moment(json.data.activitiesRead)));
        }
      });
  };
}
