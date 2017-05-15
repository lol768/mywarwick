import { formatPreferenceData, tilesReducer as reducer } from 'state/tiles';

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
        options: [
          {
            name: 'Stop 1',
            value: '43000065301',
          },
          {
            name: 'Stop 2',
            value: '43000065302',
          },
          {
            name: 'Stop 3',
            value: '43000065303',
          },
        ],
        type: 'array',
      },
      location: {
        options: [
          {
            name: 'Westwood',
            value: 'warwick-westwood',
          },
          {
            name: 'The Moon',
            value: 'the-moon',
          },
        ],
        type: 'string',
      }
    };

    const expected = {
      stops: {
        '43000065301': true,
        '43000065302': true,
        '43000065303': false,
      },
      location: 'warwick-westwood',
    };
    const result = formatPreferenceData(preferencesDataFromAction, availableTileOptions);
    result.should.deep.equal(expected);

  });

});