import log from 'loglevel';
import _ from 'lodash-es';
import * as theme from './ui';
import { createAction } from 'redux-actions';
import { fetchWithCredentials, postJsonWithCredentials } from '../serverpipe';

const COLOUR_SCHEME_PREFERENCE_REQUEST = 'COLOUR_SCHEME_PREFERENCE_REQUEST';
const COLOUR_SCHEME_PREFERENCE_RECEIVE = 'COLOUR_SCHEME_PREFERENCE_RECEIVE';
const SAVE_CHOICE = 'SAVE_CHOICE';

const start = createAction(COLOUR_SCHEME_PREFERENCE_REQUEST);
export const receive = createAction(COLOUR_SCHEME_PREFERENCE_RECEIVE);
const persist = createAction(SAVE_CHOICE);

export function fetch() {
  return (dispatch) => {
    log.debug('Fetching colour scheme preference.');
    dispatch(start());
    return fetchWithCredentials('/api/colour-schemes')
      .then(response => response.json())
      .then((json) => {
        if (json.data !== undefined) {
          dispatch(receive(json));
          dispatch(changeColourScheme());
        } else {
          throw new Error('Invalid response returned from colour scheme API');
        }
      })
      .catch(e => dispatch(receive(e)));
  };
}

const doPostToServer = (colourScheme) => {
  postJsonWithCredentials('/api/colour-schemes', { colourScheme });
};

const postToServer = _.debounce(getState =>
  doPostToServer(getState().colourSchemes.chosen), 500,
);

export function changeColourScheme(chosen) {
  return (dispatch, getState) => {
    const currentPref = getState().colourSchemes.chosen;
    dispatch(persist(chosen || currentPref));
    dispatch(theme.updateColourTheme(`transparent-${chosen || currentPref}`));
    if (chosen !== currentPref) postToServer(getState);
  };
}

const initialState = {
  fetching: false,
  failed: false,
  fetched: false,
  chosen: 1,
  schemes: [
    {
      id: 1,
      url: 'bg01.jpg',
    },
  ],
};

export function reducer(state = initialState, action) {
  switch (action.type) {
    case COLOUR_SCHEME_PREFERENCE_REQUEST:
      return {
        ...state,
        fetching: true,
        failed: false,
        fetched: false,
      };
    case COLOUR_SCHEME_PREFERENCE_RECEIVE:
      return action.error ? {
        ...state,
        fetching: false,
        failed: true,
        fetched: true,
      } : {
        ...state,
        fetching: false,
        failed: false,
        fetched: true,
        chosen: action.payload.data.chosen,
        schemes: action.payload.data.schemes,
      };
    case SAVE_CHOICE:
      return {
        ...state,
        chosen: action.payload,
      };
    default:
      return state;
  }
}
