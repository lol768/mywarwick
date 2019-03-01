import log from 'loglevel';
import _ from 'lodash-es';
import * as theme from './ui';
import { createAction } from 'redux-actions';
import { fetchWithCredentials, postJsonWithCredentials } from '../serverpipe';

const COLOUR_SCHEME_PREFERENCE_REQUEST = 'COLOUR_SCHEME_PREFERENCE_REQUEST';
const COLOUR_SCHEME_PREFERENCE_RECEIVE = 'COLOUR_SCHEME_PREFERENCE_RECEIVE';
const COLOUR_SCHEME_SAVE_CHOICE = 'COLOUR_SCHEME_SAVE_CHOICE';
const COLOUR_SCHEME_LOADED = 'COLOUR_SCHEME_LOADED';

const COLOUR_SCHEME_TOGGLE_HIGH_CONTRAST = 'COLOUR_SCHEME_TOGGLE_HIGHCONTRAST';

const start = createAction(COLOUR_SCHEME_PREFERENCE_REQUEST);
export const receive = createAction(COLOUR_SCHEME_PREFERENCE_RECEIVE);
const save = createAction(COLOUR_SCHEME_SAVE_CHOICE);
export const loaded = createAction(COLOUR_SCHEME_LOADED);
const toggleContrast = createAction(COLOUR_SCHEME_TOGGLE_HIGH_CONTRAST);

const doPostToServer = ({ chosen: colourScheme, isHighContrast }) => {
  postJsonWithCredentials('/api/colour-schemes', { colourScheme, isHighContrast });
};

const postToServer = _.debounce(getState =>
  doPostToServer(getState().colourSchemes), 500,
);

export function updateUi() {
  return (dispatch, getState) => {
    const chosenId = getState().colourSchemes.chosen;
    const isHighContrast = getState().colourSchemes.isHighContrast;
    const chosenScheme = _.find(getState().colourSchemes.schemes, scheme => scheme.id === chosenId);
    dispatch(theme.updateColourTheme({
      colourTheme: `transparent-${chosenId}${isHighContrast ? '--high-contrast' : ''}`,
      schemeColour: chosenScheme.schemeColour,
    }));
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

export function toggleHighContrast(value) {
  return (dispatch, getState) => {
    const currentPref = getState().colourSchemes.isHighContrast;
    if (currentPref !== value) {
      dispatch(toggleContrast(value));
      dispatch(updateUi());
      postToServer(getState);
    }
  };
}

export function fetch() {
  return (dispatch) => {
    log.debug('Fetching colour scheme preference');
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
  isHighContrast: false,
  schemes: [
    {
      id: 1,
      url: 'bg01.jpg',
      name: 'Scarman',
      schemeColour: '#8C6E96',
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
        isHighContrast: action.payload.data.isHighContrast,
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
    case COLOUR_SCHEME_TOGGLE_HIGH_CONTRAST:
      return {
        ...state,
        isHighContrast: !state.isHighContrast,
      };
    default:
      return state;
  }
}
