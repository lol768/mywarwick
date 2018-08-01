/* eslint-env browser */

import _ from 'lodash-es';
import { createAction } from 'redux-actions';
import { fetchWithCredentials, postJsonWithCredentials } from '../serverpipe';
import log from 'loglevel';

const DO_NOT_DISTURB_UPDATE = 'DO_NOT_DISTURB_UPDATE';
const DO_NOT_DISTURB_RECEIVE = 'DO_NOT_DISTURB_RECEIVE';
const DO_NOT_DISTURB_REQUEST = 'DO_NOT_DISTURB_REQUEST';

export const receive = createAction(DO_NOT_DISTURB_RECEIVE);
const request = createAction(DO_NOT_DISTURB_REQUEST);

function updateServerWithState({ enabled, start, end, fetched }) {
  if (fetched) {
    postJsonWithCredentials('/api/donotdisturb', { enabled, doNotDisturb: { start, end } })
      .then(response => response.json())
      .then((response) => {
        if (response.status !== 'ok') {
          log.error(`Failed to POST request to Do Not Disturb API. 
          Response was ${JSON.stringify(response)}`);
        }
      })
      .catch(e => log.error('Failed to POST request to Do Not Disturb API', e));
  }
}

const postToServer = _.debounce(getState =>
  updateServerWithState(getState().doNotDisturb), 500,
);

export function fetch() {
  return (dispatch) => {
    log.debug('Fetching Do Not Disturb preference.');
    dispatch(request());
    return fetchWithCredentials('/api/donotdisturb')
      .then(response => response.json())
      .then((json) => {
        if (json.data !== undefined) {
          dispatch(receive(json));
        } else {
          throw new Error('Invalid response returned from Do Not Disturb API');
        }
      })
      .catch(e => dispatch(receive(e)));
  };
}

export function update(payload) {
  return (dispatch, getState) => {
    dispatch({
      type: DO_NOT_DISTURB_UPDATE,
      payload,
    });
    postToServer(getState);
  };
}

const initialState = {
  fetching: false,
  failed: false,
  fetched: false,
  enabled: false,
  start: {
    hr: 21,
    min: 0,
  },
  end: {
    hr: 7,
    min: 0,
  },
};

export function reducer(state = initialState, action) {
  switch (action.type) {
    case DO_NOT_DISTURB_REQUEST:
      return {
        ...state,
        fetching: true,
        failed: false,
        fetched: false,
      };
    case DO_NOT_DISTURB_RECEIVE:
      if (action.error) {
        return {
          ...state,
          fetching: false,
          failed: true,
          fetched: true,
        };
      }
      return {
        ...state,
        ...action.payload.data,
        fetching: false,
        failed: false,
        fetched: true,
      };
    case DO_NOT_DISTURB_UPDATE:
      return {
        ...state,
        ...action.payload,
      };
    default:
      return state;
  }
}
