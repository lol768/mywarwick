import Immutable from 'immutable';
import { registerReducer } from './reducers';

export const TILES_FETCH = 'tiles.fetch';
export const TILES_FETCH_SUCCESS = 'tiles.fetch.success';
export const TILES_FETCH_FAILURE = 'tiles.fetch.failure';

export const TILE_CONTENT_FETCH = 'tiles.content.fetch';
export const TILE_CONTENT_FETCH_SUCCESS = 'tiles.content.fetch.success';
export const TILE_CONTENT_FETCH_FAILURE = 'tiles.content.fetch.failure';

// Used for bringing back tile content from local storage
export const TILE_CONTENT_LOAD_ALL = 'tiles.content.load';

export function fetchedTiles(tiles) {
  return {
    type: TILES_FETCH_SUCCESS,
    tiles,
  };
}

export function loadedAllTileContent(content) {
  return {
    type: TILE_CONTENT_LOAD_ALL,
    content,
  };
}

export function fetchedTileContent(tile, content) {
  return {
    type: TILE_CONTENT_FETCH_SUCCESS,
    tile,
    content,
    fetchedAt: new Date().getTime(),
  };
}

export function failedTileContentFetch(tile, errors) {
  return {
    type: TILE_CONTENT_FETCH_FAILURE,
    tile,
    errors,
  };
}

const initialState = Immutable.fromJS({
  fetching: false,
  fetched: false,
  failed: false,
  items: [],
});

registerReducer('tiles', (state = initialState, action) => {
  switch (action.type) {
    case TILES_FETCH:
      return state.merge({
        fetching: true,
        fetched: false,
        failed: false,
      });
    case TILES_FETCH_FAILURE:
      return state.merge({
        fetching: false,
        fetched: false,
        failed: true,
      });
    case TILES_FETCH_SUCCESS:
      return state.merge({
        fetching: false,
        fetched: true,
        failed: false,
        items: Immutable.List(action.tiles), // eslint-disable-line new-cap
      });
    default:
      return state;
  }
});

registerReducer('tileContent', (state = Immutable.Map(), action) => { // eslint-disable-line new-cap
  switch (action.type) {
    case TILE_CONTENT_FETCH: {
      const update = tile => tile.delete('errors').set('fetching', true);

      if (action.tile) {
        return state.update(
          action.tile,
          update(Immutable.Map()), // eslint-disable-line new-cap
          update
        );
      }
      return state.map(update);
    }
    case TILE_CONTENT_FETCH_SUCCESS: {
      const update = tile => tile.merge({
        fetching: false,
        fetchedAt: action.fetchedAt,
        content: action.content,
      }).delete('errors');

      return state.update(
        action.tile,
        update(Immutable.Map()), // eslint-disable-line new-cap
        update
      );
    }
    case TILE_CONTENT_FETCH_FAILURE: {
      const update = tile => tile.merge({
        fetching: false,
        errors: action.errors,
      });

      if (action.tile) {
        return state.update(
          action.tile,
          Immutable.Map({ fetching: false, errors: action.errors }), // eslint-disable-line new-cap
          update
        );
      }
      return state.map(update);
    }
    case TILE_CONTENT_LOAD_ALL:
      const merger = (prev, next) => {
        if (next.has('content') && !prev.has('content')) {
          return prev.merge({
            content: next.get('content'),
            fetchedAt: next.get('fetchedAt'),
          });
        }
        return prev;
      };

      return state.mergeWith(merger, action.content);
    default:
      return state;
  }
});
