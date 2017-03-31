import _ from 'lodash-es';
import log from 'loglevel';
import { createAction } from 'redux-actions';
import { fetchWithCredentials } from '../serverpipe';

const NEWS_REFRESH = 'NEWS_REFRESH';
const NEWS_REQUEST = 'NEWS_REQUEST';
const NEWS_RECEIVE = 'NEWS_RECEIVE';

const startRefresh = createAction(NEWS_REFRESH);
const start = createAction(NEWS_REQUEST);
const receive = createAction(NEWS_RECEIVE);

export function fetch() {
  return (dispatch, getState) => {
    if (getState().news.fetching) {
      log.debug('Not fetching because already fetching');
      return null;
    }

    log.debug('Fetching news.');
    dispatch(start());

    const { offset } = getState().news;
    const url = `/api/news/feed?offset=${offset}`;

    return fetchWithCredentials(url)
      .then(response => response.json())
      .then(json => {
        if (json.data !== undefined) {
          dispatch(receive(json));
        } else {
          throw new Error('Invalid response returned from news feed');
        }
      })
      .catch((e) => dispatch(receive(e)));
  };
}

export function refresh() {
  return dispatch => {
    dispatch(startRefresh());
    return dispatch(fetch());
  };
}

const initialState = {
  fetching: false,
  failed: false,
  items: [],
  offset: 0,
  moreAvailable: true,
};

export function reducer(state = initialState, action) {
  switch (action.type) {
    case NEWS_REFRESH:
      return initialState;
    case NEWS_REQUEST:
      return {
        ...state,
        fetching: true,
        failed: false,
      };
    case NEWS_RECEIVE: {
      if (action.error) {
        return {
          ...state,
          fetching: false,
          failed: true,
        };
      }

      const newItems = action.payload.data;

      if (newItems.length === 0) {
        return {
          ...state,
          fetching: false,
          failed: false,
          moreAvailable: false,
        };
      }

      const items = [
        ...state.items,
        ...newItems,
      ];
      const uniqueItems = _.uniqBy(items, 'id');
      const numberOfDuplicates = items.length - uniqueItems.length;

      return {
        ...state,
        fetching: false,
        failed: false,
        offset: state.offset + newItems.length + numberOfDuplicates,
        moreAvailable: true,
        items: uniqueItems,
      };
    }
    default:
      return state;
  }
}
