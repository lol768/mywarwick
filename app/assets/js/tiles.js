import Immutable from 'immutable';
import localforage from 'localforage';
import log from 'loglevel';

import store from './store';
import { createSelector } from 'reselect';
import { registerReducer } from './reducers';
import { makeStream, takeFromStream } from './stream';

import SocketDatapipe from './SocketDatapipe';

export const TILES_FETCH = 'tiles.fetch';
export const TILES_CONFIG_RECEIVE = 'tiles.config.receive';
export const TILE_CONTENT_RECEIVE = 'tile.content.receive';
export const TILES_FETCH_FAILURE = 'tiles.fetch.failure';

export function receivedTilesConfig(data) {
  return {
    type: TILES_RECEIVE,
    tiles: data
  };
}

export function receivedTileContent(data) {
  return {
    type: TILE_CONTENT_RECEIVE,
    tilesContent: data
  };
}

// TODO: should local storage keep tile config and tile content
localforage.getItem('tiles').then(
  (value) => {
    if (value != null) store.dispatch(receivedTilesConfig(value));
  },
  (err) => log.warn('Problem loading tiles from local storage', err)
);

const tilesSelector = (state) => state.get('tiles');

const persistTilesSelect = createSelector([tilesSelector], (tiles) => {
  // Persist tile data to local storage on change
  localforage.setItem('tiles', tiles.toJS());
});

store.subscribe(() => persistTilesSelect(store.getState()));

registerReducer('tiles', (state = Immutable.List(), action) => {
  switch (action.type) {
    case TILES_FETCH:
      // Could set `fetching: true` and display a loading indicator on the UI
      return state;
    case TILES_FETCH_FAILURE:
      // Could set `error: true` and display an error message and/or retry
      return state;
    case TILES_CONTENT_RECEIVE:
      return action.tilesContent;
    case TILES_CONFIG_RECEIVE:
      return action.tiles;
    default:
      return state;
  }
});
