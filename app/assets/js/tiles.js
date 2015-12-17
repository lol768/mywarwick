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
    content: data.tiles,
    errors: data.errors
  };
}

// TODO: should local storage keep tile config and tile content
localforage.getItem('tiles').then(
  (value) => {
    if (value != null && store.getState().get('tiles').get('fetched') == false) {
      store.dispatch(receivedTilesConfig(value));
    }
  },
  (err) => log.warn('Problem loading tiles from local storage', err)
);

const tilesSelector = (state) => state.get('tiles').get('items');

const persistTilesSelect = createSelector([tilesSelector], (tiles) => {
  // Persist tile data to local storage on change
  localforage.setItem('tiles', tiles.toJS());
});

store.subscribe(() => persistTilesSelect(store.getState()));

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
  items: [],
  errors: []
});

registerReducer('tileContent', (state = contentInitialState, action) => {
  switch (action.type) {
    case TILES_CONTENT_RECEIVE:
      return Immutable.fromJS({
        items: action.content || {},
        errors: action.errors || {}
      });
    default:
      return state;
  }
});