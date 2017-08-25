import log from 'loglevel';
import _ from 'lodash-es';
import * as theme from './ui';
import { createAction } from 'redux-actions';
import { fetchWithCredentials, postJsonWithCredentials } from '../serverpipe';

const COLOUR_SCHEME_PREFERENCE_REQUEST = 'COLOUR_SCHEME_PREFERENCE_REQUEST';
const COLOUR_SCHEME_PREFERENCE_RECEIVE = 'COLOUR_SCHEME_PREFERENCE_RECEIVE';
const COLOUR_SCHEME_SAVE_CHOICE = 'COLOUR_SCHEME_SAVE_CHOICE';
const COLOUR_SCHEME_LOADED = 'COLOUR_SCHEME_LOADED';

const start = createAction(COLOUR_SCHEME_PREFERENCE_REQUEST);
export const receive = createAction(COLOUR_SCHEME_PREFERENCE_RECEIVE);
const save = createAction(COLOUR_SCHEME_SAVE_CHOICE);
export const loaded = createAction(COLOUR_SCHEME_LOADED);

const doPostToServer = (colourScheme) => {
  postJsonWithCredentials('/api/colour-schemes', { colourScheme });
};

const postToServer = _.debounce(getState =>
  doPostToServer(getState().colourSchemes.chosen), 500,
);

export function updateUi() {
  return (dispatch, getState) => {
    const chosen = getState().colourSchemes.chosen;
    dispatch(theme.updateColourTheme(`transparent-${chosen}`));
  };
}

export function changeColourScheme(chosen) {
  return (dispatch, getState) => {
    const currentPref = getState().colourSchemes.chosen;
    if (chosen && (currentPref !== chosen)) {
      dispatch(save(chosen));
      dispatch(updateUi());
      postToServer(getState);
    }
  };
}

export function fetch() {
  return (dispatch) => {
    log.debug('Fetching colour scheme preference.');
    dispatch(start());
    return fetchWithCredentials('/api/colour-schemes')
      .then(response => response.json())
      .then((json) => {
        if (json.data !== undefined) {
          dispatch(receive(json));
          dispatch(updateUi());
        } else {
          throw new Error('Invalid response returned from colour scheme API');
        }
      })
      .catch(e => dispatch(receive(e)));
  };
}

const initialState = {
  fetching: false,
  failed: false,
  fetched: false,
  loaded: false,
  chosen: 1,
  schemes: [
    {
      id: 1,
      url: 'bg01.jpg',
      name: 'Scarman',
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
        fetching: false,
        failed: false,
        fetched: true,
        chosen: action.payload.data.chosen,
        schemes: action.payload.data.schemes,
      };
    case COLOUR_SCHEME_SAVE_CHOICE:
      return {
        ...state,
        chosen: action.payload,
      };
    case COLOUR_SCHEME_LOADED:
      return {
        ...state,
        loaded: true,
      };
    default:
      return state;
  }
}
