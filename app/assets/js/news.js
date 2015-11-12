import { registerReducer } from './reducers';
import Immutable from 'immutable';

import fetch from 'isomorphic-fetch';
import { polyfill } from 'es6-promise';
polyfill();

const NEWS_FETCH = 'news.fetch';
const NEWS_FETCH_SUCCESS = 'news.fetch.success';
const NEWS_FETCH_FAILURE = 'news.fetch.failure';

export function fetchNews() {
  return dispatch => {
    dispatch({type: NEWS_FETCH});
    return fetch('/news/feed')
      .then(response => response.json())
      .then(json => dispatch({type: NEWS_FETCH_SUCCESS, items: json.items}))
      .catch(err => dispatch({type: NEWS_FETCH_FAILURE}));
  }
}

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

