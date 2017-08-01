import log from 'loglevel';
import _ from 'lodash-es';
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
        } else {
          throw new Error('Invalid response returned from colour scheme API');
        }
      })
      .catch(e => dispatch(receive(e)));
  };
}


let store = {};
const postToServer = _.debounce(() =>
  store.dispatch(doPostToServer(store.getState().colourSchemes.chosen), 500)
);

const doPostToServer = (colourScheme) => {
  postJsonWithCredentials('/api/colour-schemes', { colourScheme });
};

export function changeColourScheme(chosen) {
  return (dispatch, getState) => {
    store = { dispatch, getState };
    dispatch(persist(chosen));
    postToServer();
  };
};

const initialState = {
  fetching: false,
  failed: false,
  fetched: false,
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
      console.log(action.payload);
      return {
        ...state,
        chosen: action.payload,
      }
    default:
      return state;
  }
}
