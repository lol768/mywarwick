import Immutable from 'immutable';
import log from 'loglevel';
import _ from 'lodash';

import { USER_CLEAR } from './user';
import { fetchWithCredentials } from '../serverpipe';

const TILES_FETCH = 'tiles.fetch';
const TILES_FETCH_SUCCESS = 'tiles.fetch.success';
const TILES_FETCH_FAILURE = 'tiles.fetch.failure';

const TILE_CONTENT_FETCH = 'tiles.content.fetch';
const TILE_CONTENT_FETCH_SUCCESS = 'tiles.content.fetch.success';
const TILE_CONTENT_FETCH_FAILURE = 'tiles.content.fetch.failure';

// for bringing back tile content from local storage
const TILE_CONTENT_LOAD_ALL = 'tiles.content.load';

const TILE_SHOW = 'tiles.show';
const TILE_HIDE = 'tiles.hide';
const TILE_RESIZE = 'tiles.resize';

const TILE_LAYOUT_CHANGE = 'me.tile-layout-change';

const NETWORK_ERRORS = [
  {
    id: 'network',
    message: 'Unable to contact the server.',
  },
];

// Action creators

export function tileLayoutChange(layout, layoutWidth) {
  return {
    type: TILE_LAYOUT_CHANGE,
    layout,
    layoutWidth,
  };
}

export function fetchedTiles({ tiles, layout }) {
  return {
    type: TILES_FETCH_SUCCESS,
    tiles,
    layout,
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

export function showTile(tile) {
  return {
    type: TILE_SHOW,
    tile,
  };
}

export function hideTile(tile) {
  return {
    type: TILE_HIDE,
    tile,
  };
}

export function resizeTile(tile, layoutWidth, width, height) {
  return {
    type: TILE_RESIZE,
    tile,
    layoutWidth,
    width,
    height,
  };
}

export function persistTiles() {
  return (dispatch, getState) => {
    const tileData = getState().getIn(['tiles', 'data', 'tiles']).map(item => ({
      id: item.get('id'),
      preferences: item.get('preferences'),
      removed: item.get('removed'),
    })).toJS();

    const layout = getState().getIn(['tiles', 'data', 'layout']).toJS();

    fetch('/api/tiles', {
      credentials: 'same-origin',
      method: 'PUT',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({ tiles: tileData, layout }),
    });
  };
}

export function fetchTiles() {
  return dispatch => {
    dispatch({ type: TILES_FETCH });

    return fetchWithCredentials('/api/tiles')
      .then(response => response.json())
      .then(json => dispatch(fetchedTiles(json.data)))
      .catch(() => dispatch({ type: TILES_FETCH_FAILURE }));
  };
}

const ALL_TILES = undefined;
export function fetchTileContent(tileId = ALL_TILES) {
  return dispatch => {
    dispatch({
      type: TILE_CONTENT_FETCH,
      tile: tileId,
    });

    const endpoint = tileId ? `/api/tiles/content/${tileId}` : '/api/tiles/content';

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
        log.warn('Tile fetch failed because', err);
        return dispatch(failedTileContentFetch(tileId, NETWORK_ERRORS));
      });
  };
}


const initialContentState = Immutable.Map();

const initialState = Immutable.fromJS({
  fetching: false,
  fetched: false,
  failed: false,
  data: {
    tiles: [],
    layout: [],
  },
});

export function tilesReducer(state = initialState, action) {
  switch (action.type) {
    case USER_CLEAR:
      return initialState;
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
        data: {
          tiles: action.tiles,
          layout: action.layout,
        },
      });
    case TILE_HIDE:
      return state.updateIn(['data', 'tiles'], items => {
        const index = items.findIndex(tile => tile.get('id') === action.tile.id);

        return items.update(index, tile => tile.set('removed', true));
      });
    case TILE_SHOW:
      return state.updateIn(['data', 'tiles'], items => {
        const index = items.findIndex(tile => tile.get('id') === action.tile.id);

        return items.update(index, tile => tile.set('removed', false));
      });
    case TILE_RESIZE:
      return state.updateIn(['data', 'layout'], layout => {
        const index = layout.findIndex(i =>
          i.get('layoutWidth') === action.layoutWidth && i.get('tile') === action.tile.id
        );
        return layout.update(index, i => i.set('width', action.width).set('height', action.height));
      });
    case TILE_LAYOUT_CHANGE: {
      const thisLayout = action.layout.map(i => Immutable.Map({
        layoutWidth: action.layoutWidth,
        tile: i.i,
        x: i.x,
        y: i.y,
        width: i.w,
        height: i.h,
      }));

      return state.updateIn(['data', 'layout'], layout =>
        layout.filter(i => i.get('layoutWidth') !== action.layoutWidth).concat(thisLayout)
      );
    }
    default:
      return state;
  }
}

export function tileContentReducer(state = initialContentState, action) {
  switch (action.type) {
    case USER_CLEAR:
      return initialContentState;
    case TILE_CONTENT_FETCH: {
      const update = tile => tile.delete('errors').set('fetching', true);

      if (action.tile) {
        return state.update(
          action.tile,
          update(Immutable.Map()),
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
        update(Immutable.Map()),
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
          Immutable.Map({ fetching: false, errors: action.errors }),
          update
        );
      }
      return state.map(update);
    }
    case TILE_CONTENT_LOAD_ALL: {
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
    }
    default:
      return state;
  }
}

