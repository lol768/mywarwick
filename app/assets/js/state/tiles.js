import log from 'loglevel';
import _ from 'lodash';
import update from 'react-addons-update';

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
    const tiles = getState().tiles.data.tiles.map(item =>
      _.pick(item, ['id', 'preferences', 'removed'])
    );

    const layout = getState().tiles.data.layout;

    return fetch('/api/tiles', {
      credentials: 'same-origin',
      method: 'PUT',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({ tiles, layout }),
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

    return fetchWithCredentials(endpoint)
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


const initialContentState = {};

const initialState = {
  fetching: false,
  fetched: false,
  failed: false,
  data: {
    tiles: [],
    layout: [],
  },
};

function updateTileById(state, id, callback) {
  const tiles = state.data.tiles;
  const index = tiles.findIndex(tile => tile.id === id);
  const newTiles = Object.assign([], tiles, {
    [index]: callback(tiles[index]),
  });
  return { ...state,
    data: { ...state.data,
      tiles: newTiles,
    },
  };
}

export function tilesReducer(state = initialState, action) {
  switch (action.type) {
    case USER_CLEAR:
      return initialState;
    case TILES_FETCH:
      return {
        ...state,
        fetching: true,
        fetched: false,
        failed: false,
      };
    case TILES_FETCH_FAILURE:
      return {
        ...state,
        fetching: false,
        fetched: false,
        failed: true,
      };
    case TILES_FETCH_SUCCESS:
      return {
        ...state,
        fetching: false,
        fetched: true,
        failed: false,
        data: {
          tiles: action.tiles || [],
          layout: action.layout || [],
        },
      };
    case TILE_HIDE:
      return updateTileById(state, action.tile.id, (tile) => ({
        ...tile,
        removed: true,
      }));
    case TILE_SHOW:
      return updateTileById(state, action.tile.id, (tile) => ({
        ...tile,
        removed: false,
      }));
    case TILE_RESIZE: {
      const layout = state.data.layout;
      const index = layout.findIndex(i =>
        i.layoutWidth === action.layoutWidth && i.tile === action.tile.id
      );
      return {
        ...state,
        data: {
          ...state.data,
          layout: Object.assign([], layout, {
            [index]: {
              ...layout[index],
              width: action.width,
              height: action.height,
            },
          }),
        },
      };
    }
    case TILE_LAYOUT_CHANGE: {
      const toAdd = action.layout.map(i => ({
        layoutWidth: action.layoutWidth,
        tile: i.i,
        x: i.x,
        y: i.y,
        width: i.w,
        height: i.h,
      }));
      const oldRemoved = _.filter(state.data.layout, i => i.layoutWidth !== action.layoutWidth);
      const newLayout = toAdd.concat(oldRemoved);
      return { ...state,
        data: { ...state.data,
          layout: newLayout,
        },
      };
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
      const change = tile => ({ ...tile,
        errors: undefined,
        fetching: true,
      });
      if (action.tile) {
        return update(state, { [action.tile]: { $apply: change } });
      }
      return _.mapValues(state, change);
    }
    case TILE_CONTENT_FETCH_SUCCESS: {
      const change = tile => ({ ...tile,
        fetching: false,
        fetchedAt: action.fetchedAt,
        content: action.content,
        errors: undefined,
      });
      return update(state, { [action.tile]: { $apply: change } });
    }
    case TILE_CONTENT_FETCH_FAILURE: {
      const change = tile => ({ ...tile,
        fetching: false,
        errors: action.errors,
      });

      if (action.tile) {
        return update(state, { [action.tile]: { $apply: change } });
      }
      return _.mapValues(state, change);
    }
    case TILE_CONTENT_LOAD_ALL: {
      const merger = (prev = {}, next) => {
        if (next.content && !prev.content) {
          return { ...prev,
            content: next.content,
            fetchedAt: next.fetchedAt,
          };
        }
        return prev;
      };

      return _.mergeWith(state, action.content, merger);
    }
    default:
      return state;
  }
}

