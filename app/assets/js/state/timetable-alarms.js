/* eslint-env browser */
import { pick } from 'lodash-es';

const UPDATE = 'TIMETABLE_ALARMS_UPDATE';

const initialState = {
  enabled: true,
  minutesBeforeEvent: 15,
};

export function updateNativeWithState(state) {
  const native = window.MyWarwickNative; // eslint-disable-line no-undef
  if (!!native && 'setTimetableNotificationsEnabled' in native) {
    native.setTimetableNotificationsEnabled(state.enabled);
    native.setTimetableNotificationTiming(state.minutesBeforeEvent);
  }
}

export function update(payload) {
  return (dispatch, getState) => {
    dispatch({
      type: UPDATE,
      payload,
    });
    updateNativeWithState(getState().timetableAlarms);
  };
}

export function reducer(state = initialState, action) {
  if (action.type === UPDATE) {
    return {
      ...state,
      ...pick(action.payload, ['enabled', 'minutesBeforeEvent']),
    };
  }

  return state;
}
