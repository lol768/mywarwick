import { pick } from 'lodash-es';

const UPDATE = 'TIMETABLE_ALARMS_UPDATE';

export function update(payload) {
  return (dispatch, getState) => {
    dispatch({
      type: UPDATE,
      payload,
    });

    const { enabled, minutesBeforeEvent } = getState().timetableAlarms;

    const native = window.MyWarwickNative; // eslint-disable-line no-undef

    if (!!native && 'setTimetableNotificationsEnabled' in native) {
      native.setTimetableNotificationsEnabled(enabled);
      native.setTimetableNotificationTiming(minutesBeforeEvent);
    }
  };
}

const initialState = {
  enabled: false,
  minutesBeforeEvent: 15,
};

export function reducer(state = initialState, action) {
  if (action.type === UPDATE) {
    return {
      ...state,
      ...pick(action.payload, ['enabled', 'minutesBeforeEvent']),
    };
  }

  return state;
}
