import log from 'loglevel';
import { createAction } from 'redux-actions';
import { fetchWithCredentials, postJsonWithCredentials } from '../serverpipe';

const COLOUR_SCHEME_PREFERENCE_REQUEST = 'COLOUR_SCHEME_PREFERENCE_REQUEST';
const COLOUR_SCHEME_PREFERENCE_RECEIVE = 'COLOUR_SCHEME_PREFERENCE_RECEIVE';

const start = createAction(COLOUR_SCHEME_PREFERENCE_REQUEST);
export const receive = createAction(COLOUR_SCHEME_PREFERENCE_RECEIVE);

export function fetch() {
  return (dispatch) => {
    log.debug('Fetching colour scheme preference.');
    dispatch(start());
    return fetchWithCredentials('/api/colour-schemes')
      .then(response => response.json())
      .then((json) => {
        if (json.data !== undefined) {
          dispatch(receive(json));
        } else {
          throw new Error('Invalid response returned from colour scheme API');
        }
      })
      .catch(e => dispatch(receive(e)));
  };
}

const persistColourSchemeChoice = chosenSchemeId => () =>
  postJsonWithCredentials('/api/colour-schemes', { chosenSchemeId });

export function persist(chosenSchemeId) {
  return persistColourSchemeChoice(chosenSchemeId);
}

let store = {};

const initialState = {
  fetching: false,
  failed: false,
  fetched: false,
  chosenSchemeId: 1,
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
        chosenSchemeId: action.payload.data.chosenSchemeId
      };
    default:
      return state;
  }
}
