/* eslint-env browser */
import { pick } from 'lodash-es';

const UPDATE = 'TIMETABLE_ALARMS_UPDATE';
const SET_NATIVE = 'SET_NATIVE';

const initialState = {
  enabled: true,
  minutesBeforeEvent: 15,
  soundEnabled: true,
};

function updateNativeWithState(state) {
  const native = window.MyWarwickNative;
  if (!!native && 'setTimetableNotificationsEnabled' in native) {
    native.setTimetableNotificationsEnabled(state.enabled);
    native.setTimetableNotificationTiming(state.minutesBeforeEvent);
    if (native.setTimetableNotificationsSoundEnabled) {
      native.setTimetableNotificationsSoundEnabled(state.soundEnabled);
    }
  }
}

export function setNative(payload) {
  return (dispatch, getState) => {
    dispatch({
      type: SET_NATIVE,
      payload,
    });
    updateNativeWithState(getState().timetableAlarms);
  };
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
      ...pick(action.payload, ['enabled', 'minutesBeforeEvent', 'soundEnabled']),
    };
  }

  return state;
}
