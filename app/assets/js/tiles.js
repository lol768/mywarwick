import Immutable from 'immutable';

import store from './store';
import { registerReducer } from './reducers';
import { makeStream, takeFromStream } from './stream';

import SocketDatapipe from './SocketDatapipe';

export const TILE_RECEIVE = 'tiles.receive';

export function receivedTileData(data) {
  return {
    type: TILE_RECEIVE,
    tiles: data
  };
}

// TODO: not sure if ajax is what we want here, perhaps some other way to request tile-specific data. Or use websocket msg
export function fetchTileData() {
  $.getJSON('/api/tiles', function (data) {
    store.dispatch(receivedTileData(data.tiles));
  })
}

registerReducer('tiles', (state = Immutable.List(), action) => {
  switch (action.type) {
    case TILE_RECEIVE:
      return action.tiles;
    default:
      return state;
  }
});

SocketDatapipe.getUpdateStream().subscribe((data) => {
  switch (data.type) {
    case 'tile-data':
      store.dispatch(receivedTileData(JSON.parse(data.tiles)));
      break;
    default:
      return;
  }
});
