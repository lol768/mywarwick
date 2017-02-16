import { tilesReducer as reducer } from 'state/tiles';
import * as tiles from 'state/tiles';

describe('tiles', () => {

  it('has initial state', () => {
    reducer(undefined, {type: '*'}).data.should.eql({
      tiles: [], layout: [], options: {},
    });
  });

  it('fetches tiles', () => {
    // const tiles = [];
    // const action = tiles.fetchedTiles({ tiles, layout });
    // const state = reducer(undefined, action)
  })

});