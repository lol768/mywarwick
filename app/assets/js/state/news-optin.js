import { createAction } from 'redux-actions';
import { fetchWithCredentials } from '../serverpipe';

const NEWS_OPT_IN_REQUEST = 'NEWS_OPT_IN_REQUEST';
const NEWS_OPT_IN_RECEIVE = 'NEWS_OPT_IN_RECEIVE';

const start = createAction(NEWS_OPT_IN_REQUEST);
const receive = createAction(NEWS_OPT_IN_RECEIVE);

export function fetch() {
  return dispatch => {
    dispatch(start());
    return fetchWithCredentials('/api/news/optin')
      .then(response => response.json())
      .then(json => {
        if (json.data !== undefined) {
          dispatch(receive(json.data));
        } else {
          throw new Error('Invalid response returned from news opt-in');
        }
      })
      .catch((e) => dispatch(receive(e)));
  };
}

export function persist(optInType, values) {
  return dispatch => {
    const data = { [optInType]: values };

    return global.fetch('/api/news/optin', {
      credentials: 'same-origin',
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(data),
    }).then(() => dispatch(fetch()));
  };
}

const initialState = {
  fetching: false,
  failed: false,
  options: {},
  selected: {},
};

export function reducer(state = initialState, action) {
  switch (action.type) {
    case NEWS_OPT_IN_REQUEST:
      return {
        ...state,
        fetching: true,
        failed: false,
      };
    case NEWS_OPT_IN_RECEIVE:
      return action.error ? {
        ...state,
        fetching: false,
        failed: true,
      } : {
        ...state,
        fetching: false,
        failed: false,
        options: action.payload.options,
        selected: action.payload.selected,
      };
    default:
      return state;
  }
}
