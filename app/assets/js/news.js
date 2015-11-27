import { registerReducer } from './reducers';
import Immutable from 'immutable';

export const NEWS_FETCH = 'news.fetch';
export const NEWS_FETCH_SUCCESS = 'news.fetch.success';
export const NEWS_FETCH_FAILURE = 'news.fetch.failure';

let initialState = Immutable.fromJS({
  fetching: false,
  items: []
});

registerReducer('news', (state = initialState, action) => {
  switch (action.type) {
    case NEWS_FETCH:
      return state.mergeDeep({
        fetching: true
      });
    case NEWS_FETCH_SUCCESS:
      return state.mergeDeep({
        fetching: false,
        items: action.items
      });
    case NEWS_FETCH_FAILURE:
      return state.mergeDeep({
        fetching: false
      });
    default:
      return state;
  }
});

