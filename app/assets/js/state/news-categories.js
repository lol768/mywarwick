import log from 'loglevel';
import { createAction } from 'redux-actions';
import { fetchWithCredentials, postJsonWithCredentials } from '../serverpipe';
import _ from 'lodash-es';
import * as news from './news';

const NEWS_CATEGORIES_REQUEST = 'NEWS_CATEGORIES_REQUEST';
const NEWS_CATEGORIES_RECEIVE = 'NEWS_CATEGORIES_RECEIVE';
const NEWS_CATEGORY_SUBSCRIBE = 'NEWS_CATEGORY_SUBSCRIBE';
const NEWS_CATEGORY_UNSUBSCRIBE = 'NEWS_CATEGORY_UNSUBSCRIBE';

const start = createAction(NEWS_CATEGORIES_REQUEST);
const receive = createAction(NEWS_CATEGORIES_RECEIVE);

const sub = createAction(NEWS_CATEGORY_SUBSCRIBE);
const unsub = createAction(NEWS_CATEGORY_UNSUBSCRIBE);

export function fetch() {
  return dispatch => {
    log.debug('Fetching news categories.');
    dispatch(start());
    return fetchWithCredentials('/api/news/categories')
      .then(response => response.json())
      .then(json => {
        if (json.data !== undefined) {
          dispatch(receive(json));
        } else {
          throw new Error('Invalid response returned from news categories');
        }
      })
      .catch((e) => dispatch(receive(e)));
  };
}

const persistSubscribedCategories = categories => dispatch =>
  postJsonWithCredentials('/api/news/categories', { categories })
    .then(() => dispatch(news.refresh()));

let store = {};
const persistSubscriptionsDebounced = _.debounce(() =>
  store.dispatch(persistSubscribedCategories(store.getState().newsCategories.subscribed))
, 500);

export function subscribe(id) {
  return (dispatch, getState) => {
    store = { dispatch, getState };
    dispatch(sub(id));
    persistSubscriptionsDebounced();
  };
}

export function unsubscribe(id) {
  return (dispatch, getState) => {
    store = { dispatch, getState };
    dispatch(unsub(id));
    persistSubscriptionsDebounced();
  };
}

const initialState = {
  fetching: false,
  failed: false,
  items: [],
  subscribed: [],
};

export function reducer(state = initialState, action) {
  switch (action.type) {
    case NEWS_CATEGORIES_REQUEST:
      return {
        ...state,
        fetching: true,
        failed: false,
      };
    case NEWS_CATEGORIES_RECEIVE:
      return action.error ? {
        ...state,
        fetching: false,
        failed: true,
      } : {
        ...state,
        fetching: false,
        failed: false,
        items: action.payload.data.items,
        subscribed: _.uniq(action.payload.data.subscribed),
      };
    case NEWS_CATEGORY_SUBSCRIBE:
      return {
        ...state,
        subscribed: _.uniq([
          ...state.subscribed,
          action.payload,
        ]),
      };
    case NEWS_CATEGORY_UNSUBSCRIBE: {
      return {
        ...state,
        subscribed: _.without(state.subscribed, action.payload),
      };
    }
    default:
      return state;
  }
}
