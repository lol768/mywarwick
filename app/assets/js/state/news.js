import Immutable from 'immutable';
import log from 'loglevel';
import { createAction } from 'redux-actions';
import { fetchWithCredentials } from '../serverpipe';

const NEWS_REQUEST = 'NEWS_REQUEST';
const NEWS_RECEIVE = 'NEWS_RECEIVE';

const start = createAction(NEWS_REQUEST);
const receive = createAction(NEWS_RECEIVE);

export function fetch() {
  return dispatch => {
    log.debug('Fetching news.');
    dispatch(start());
    return fetchWithCredentials('/api/news/feed')
      .then(response => response.json())
      .then(json => {
        if (json.items !== undefined) {
          dispatch(receive({ items: json.items }));
        } else {
          throw new Error('Invalid response returned from news feed');
        }
      })
      .catch((e) => dispatch(receive(e)));
  };
}

const initialState = Immutable.fromJS({
  fetching: false,
  failed: false,
  items: [],
});

export function reducer(state = initialState, action) {
  switch (action.type) {
    case NEWS_REQUEST:
      return state.mergeDeep({
        fetching: true,
        failed: false,
      });
    case NEWS_RECEIVE:
      return action.error ? state.mergeDeep({
        fetching: false,
        failed: true,
      })
      : state.mergeDeep({
        fetching: false,
        failed: false,
        items: action.payload.items,
      });
    default:
      return state;
  }
}
