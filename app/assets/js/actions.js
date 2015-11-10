import fetch from 'isomorphic-fetch';
import { polyfill } from 'es6-promise';
polyfill();

export const NAVIGATE = 'NAVIGATE';

export const NEWS_FETCH = 'NEWS_FETCH';
export const NEWS_FETCH_SUCCESS = 'NEWS_FETCH_SUCCESS';
export const NEWS_FETCH_FAILURE = 'NEWS_FETCH_FAILURE';

export function navigate(path) {
  return {
    type: NAVIGATE,
    path: path
  };
}

export function fetchNews() {
  return dispatch => {
    dispatch({type: NEWS_FETCH});
    return fetch('/news/feed')
      .then(response => response.json())
      .then(json => dispatch({type: NEWS_FETCH_SUCCESS, items: json.items}))
      .catch(err => dispatch({type: NEWS_FETCH_FAILURE}));
  }
}