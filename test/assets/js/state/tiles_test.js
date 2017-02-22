import { tilesReducer as reducer } from 'state/tiles';
import { formatPreferenceData } from 'state/tiles';
import * as tiles from 'state/tiles';

describe('tiles', () => {

  it('has initial state', () => {
    reducer(undefined, { type: '*' }).data.should.eql({
      tiles: [], layout: [], options: {},
    });
  });

  it('fetches tiles', () => {
    // const tiles = [];
    // const action = tiles.fetchedTiles({ tiles, layout });
    // const state = reducer(undefined, action)
  });

  it('format preference data properly', () => {

    const preferencesDataFromAction = [
      {
        name: 'stops',
        value: '43000065301',
      },
      {
        name: 'stops',
        value: '43000065302',
      },
      {
        name: 'location',
        value: 'warwick-westwood',
      },
    ];

    const availableTileOptions = {
      stops: {
        type: 'array',
      },
      location: {
        type: 'string',
      }
    };

    const expected = {
      stops: [
        '43000065301',
        '43000065302',
      ],
      location: 'warwick-westwood',
    };
    const result = formatPreferenceData(preferencesDataFromAction, availableTileOptions);
    result.should.deep.equal(expected);

  });

});