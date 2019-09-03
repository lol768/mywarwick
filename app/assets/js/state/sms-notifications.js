import { createAction } from 'redux-actions';
import { fetchWithCredentials, postJsonWithCredentials } from '../serverpipe';

export const SMS_NOTIFICATIONS_REQUEST = 'sms-notifications.request';
export const SMS_NOTIFICATIONS_RECEIVE = 'sms-notifications.receive';

const start = createAction(SMS_NOTIFICATIONS_REQUEST);
const receive = createAction(SMS_NOTIFICATIONS_RECEIVE);

export function fetch() {
  return (dispatch) => {
    dispatch(start());
    return fetchWithCredentials('/api/smsNotificationPreferences')
      .then(response => response.json())
      .then((json) => {
        if (json.data !== undefined) {
          dispatch(receive(json.data));
        } else {
          throw new Error('Invalid response returned from SMS notification pref API');
        }
      })
      .catch(e => dispatch(receive(e)));
  };
}

export function persist(wantsSms, smsNumber, verificationCode, resendVerification = false) {
  return postJsonWithCredentials(
    '/api/smsNotificationPreferences',
    {
      wantsSms,
      smsNumber,
      verificationCode,
      resendVerification,
    },
  );
}

const initialState = {
  fetching: false,
  failed: false,
  fetched: false,
  wantsSms: false,
  smsNumber: null,
};

export function reducer(state = initialState, action) {
  switch (action.type) {
    case SMS_NOTIFICATIONS_REQUEST:
      return {
        ...state,
        fetching: true,
        failed: false,
        fetched: false,
      };
    case SMS_NOTIFICATIONS_RECEIVE:
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
        ...action.payload,
      };
    default:
      return state;
  }
}
