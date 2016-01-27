import Immutable from 'immutable';
import localforage from 'localforage';
import log from 'loglevel';

import { createSelector } from 'reselect';
import { registerReducer } from './reducers';
import { makeStream, takeFromStream } from './stream';

export const TILES_FETCH = 'tiles.fetch';
export const TILES_CONFIG_RECEIVE = 'tiles.config.receive';
export const TILES_CONTENT_RECEIVE = 'tile.content.receive';
export const TILES_FETCH_FAILURE = 'tiles.fetch.failure';

export function receivedTilesConfig(data) {
  return {
    type: TILES_CONFIG_RECEIVE,
    tiles: data
  };
}

export function receivedTilesContent(data) {
  return {
    type: TILES_CONTENT_RECEIVE,
    content: data.content,
    errors: data.errors
  };
}

let initialState = Immutable.fromJS({
  fetching: false,
  fetched: false,
  failed: false,
  items: []
});

registerReducer('tiles', (state = initialState, action) => {
  switch (action.type) {
    case TILES_FETCH:
      return state.mergeDeep({
        fetching: true,
        fetched: false,
        failed: false
      });
    case TILES_FETCH_FAILURE:
      return state.mergeDeep({
        fetching: false,
        fetched: false,
        failed: true
      });
    case TILES_CONFIG_RECEIVE:
      return state.mergeDeep({
        fetching: false,
        fetched: true,
        failed: false,
        items: Immutable.List(action.tiles)
      });
    default:
      return state;
  }
});

let contentInitialState = Immutable.fromJS({
  content: [],
  errors: []
});

registerReducer('tileContent', (state = contentInitialState, action) => {
  switch (action.type) {
    case TILES_CONTENT_RECEIVE:
      return Immutable.fromJS({
        content: action.content || {},
        errors: action.errors || {}
      });
    default:
      return state;
  }
});
