import { createAction } from 'redux-actions';
import { fetchWithCredentials, postJsonWithCredentials } from '../serverpipe';
import { fetchUserInfo, receiveUserInfo } from '../userinfo';

export const EAP_REQUEST = 'eap.request';
export const EAP_RECEIVE = 'eap.receive';

const start = createAction(EAP_REQUEST);
export const receive = createAction(EAP_RECEIVE);

export function fetch() {
  return (dispatch) => {
    dispatch(start());
    return fetchWithCredentials('/api/eap')
      .then(response => response.json())
      .then((json) => {
        if (json.data !== undefined && 'enabled' in json.data) {
          dispatch(receive(json.data));
        } else {
          throw new Error('Invalid response returned from EAP pref API');
        }
      })
      .catch(e => dispatch(receive(e)));
  };
}

export function persist(enabled) {
  return () =>
    postJsonWithCredentials('/api/eap', { enabled })
      .then(fetchUserInfo)
      .then(receiveUserInfo);
}

export function toggleEnabled(value) {
  return (dispatch, getState) => {
    const currentPref = getState().eap.enabled;
    if (currentPref !== value) {
      dispatch(receive({ enabled: value }));
      dispatch(persist(value));
    }
  };
}

const initialState = {
  fetching: false,
  failed: false,
  enabled: false,
  fetched: false,
};

export function reducer(state = initialState, action) {
  switch (action.type) {
    case EAP_REQUEST:
      return {
        ...state,
        fetching: true,
        failed: false,
      };
    case EAP_RECEIVE:
      return action.error ? {
        ...state,
        fetching: false,
        failed: true,
      } : {
        ...state,
        fetching: false,
        failed: false,
        fetched: true,
        ...action.payload,
      };
    default:
      return state;
  }
}
