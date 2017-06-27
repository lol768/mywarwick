import { createAction } from 'redux-actions';
import { fetchWithCredentials, postJsonWithCredentials } from '../serverpipe';

// following news-optin here.


export const EMAIL_NOTIFICATIONS_OPT_IN_REQUEST = "email-notifications.request";
export const EMAIL_NOTIFICATIONS_OPT_IN_RECEIVE = "email-notifications.receive";

const start = createAction(EMAIL_NOTIFICATIONS_OPT_IN_REQUEST);
const receive = createAction(EMAIL_NOTIFICATIONS_OPT_IN_RECEIVE);

export function fetch() {
  return dispatch => {
    dispatch(start());
    return fetchWithCredentials('/api/emailNotificationPreferences')
      .then(response => response.json())
      .then(json => {
        if (json.data !== undefined && 'wantsEmails' in json.data) {
          dispatch(receive(json.data));
        } else {
          throw new Error('Invalid response returned from email notification pref API');
        }
      })
      .catch((e) => dispatch(receive(e)));
  };
}

export function persist(wantsEmails) {
  console.info("Getting ready to do ", wantsEmails);
  console.info(JSON.stringify(wantsEmails));
  return dispatch => {
    return postJsonWithCredentials('/api/emailNotificationPreferences', { wantsEmails })
      .then(() => dispatch(fetch()));
  };
}

const initialState = {
  fetching: false,
  wantsEmails: true
};

export function reducer(state = initialState, action) {
  switch (action.type) {
    case EMAIL_NOTIFICATIONS_OPT_IN_REQUEST:
      return {
        ...state,
        fetching: true,
        failed: false,
      };
    case EMAIL_NOTIFICATIONS_OPT_IN_RECEIVE:
      return action.error ? {
        ...state,
        fetching: false,
        failed: true,
      } : {
        ...state,
        fetching: false,
        failed: false,
      };
    default:
      return state;
  }
}
