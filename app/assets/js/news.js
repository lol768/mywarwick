import Immutable from 'immutable';

import { registerReducer } from './reducers';

export const NEWS_FETCH = 'news.fetch';
export const NEWS_FETCH_SUCCESS = 'news.fetch.success';
export const NEWS_FETCH_FAILURE = 'news.fetch.failure';

const initialState = Immutable.fromJS({
  fetching: false,
  failed: false,
  items: [],
});

registerReducer('news', (state = initialState, action) => {
  switch (action.type) {
    case NEWS_FETCH:
      return state.mergeDeep({
        fetching: true,
        failed: false,
      });
    case NEWS_FETCH_SUCCESS:
      return state.mergeDeep({
        fetching: false,
        failed: false,
        items: action.items,
      });
    case NEWS_FETCH_FAILURE:
      return state.mergeDeep({
        fetching: false,
        failed: true,
      });
    default:
      return state;
  }
});
