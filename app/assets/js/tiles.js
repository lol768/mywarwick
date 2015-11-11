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

export function fetchTileData() {
  $.getJSON('/api/tiles', function (data) {
    store.dispatch(receivedTileData(JSON.parse(data.tiles)));
  })
}

registerReducer('tiles', (state = [], action) => {
  switch (action.type) {
    case TILE_RECEIVE:
      return action.tiles;
    default:
      return state;
  }
});


//SocketDatapipe.getUpdateStream().subscribe((data) => {
//  switch (data.type) {
//    case 'tile-data':
//      store.dispatch((tileData) => {
//        return {
//          type: 'tile-data',
//          tileData: tileData
//        }
//      });
//      break;
//    default:
//      return;
//  }
//});
