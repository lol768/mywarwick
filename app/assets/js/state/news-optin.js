import log from 'loglevel';
import { createAction } from 'redux-actions';
import { fetchWithCredentials } from '../serverpipe';
import _ from 'lodash-es';

const NEWS_OPT_IN_FETCH = 'NEWS_OPT_IN_FETCH';

export const fetchedNewsOptin = createAction(NEWS_OPT_IN_FETCH);

export function fetch() {
  return dispatch => fetchWithCredentials('/api/news/optin')
    .then(response => response.json())
    .then(json => json.data)
    .then(data => dispatch(fetchedNewsOptin(data)))
    .catch(e => {
      log.warn('Failed to fetch news opt-in', e);
      throw e;
    });
}

export function persist(fields) {
  return dispatch => {
    const data = _.mapValues(_.groupBy(fields, v => v.name), v => _.map(v, 'value'));

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
  options: {},
  selected: {},
};

export function reducer(state = initialState, action) {
  switch (action.type) {
    case NEWS_OPT_IN_FETCH: {
      return {
        ...state,
        options: action.payload.options,
        selected: action.payload.selected,
      };
    }
    default:
      return state;
  }
}
