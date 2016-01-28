import _ from 'lodash';

import SocketDatapipe from './SocketDatapipe';

import $ from 'jquery';
import fetch from 'isomorphic-fetch';
import moment from 'moment';

import { userReceive } from './user';
import { NEWS_FETCH, NEWS_FETCH_SUCCESS, NEWS_FETCH_FAILURE } from './news';
import { TILES_FETCH, TILES_CONFIG_RECEIVE, TILES_CONTENT_RECEIVE, TILES_FETCH_FAILURE, receivedTilesConfig, receivedTilesContent } from './tiles';
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

export function fetchTilesConfig() {
  return dispatch => {
    dispatch({type: TILES_FETCH});

    return fetchWithCredentials('/api/tiles/config')
      .then(response => response.json())
      .then(json => dispatch(receivedTilesConfig(json.data)))
      .catch(err => dispatch({type: TILES_FETCH_FAILURE}));
  }
}

export function fetchTilesContentById(tileIds) {
  return dispatch => {
    dispatch({type: TILES_FETCH});

    let queryStr = $.param(tileIds);
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
      .then(json => {
        if (json.success)
          dispatch(receivedTilesContent(json.data));
        else
          dispatch(receivedTilesContent({errors: json.errors}));
      })
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
        if(json.data.notificationsRead) dispatch(notificationMetadata.readNotifications(moment(json.data.notificationsRead)));
        if(json.data.activitiesRead) dispatch(notificationMetadata.readActivities(moment(json.data.activitiesRead)));
      });
  }
}
