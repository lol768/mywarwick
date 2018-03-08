/* eslint-env browser */
import React, { PropTypes } from 'react';
import { connect } from 'react-redux';
import HideableView from '../HideableView';
import SwitchListGroupItem from '../../ui/SwitchListGroupItem';
import RadioListGroupItem from '../../ui/RadioListGroupItem';
import { update } from '../../../state/timetable-alarms';
import { pluralise } from '../../../helpers';

const TIMINGS = [
  5,
  15,
  30,
  60,
];

export class TimetableAlarmsView extends HideableView {
  static propTypes = {
    enabled: PropTypes.bool.isRequired,
    minutesBeforeEvent: PropTypes.number.isRequired,
    soundEnabled: PropTypes.bool.isRequired,
  };

  onToggleEnabled = () => {
    this.props.dispatch(update({
      enabled: !this.props.enabled,
    }));
  };

  onSetTiming = minutesBeforeEvent => () => {
    this.props.dispatch(update({
      minutesBeforeEvent,
    }));
  };

  onToggleSoundEnabled = () => {
    this.props.dispatch(update({
      soundEnabled: !this.props.soundEnabled,
    }));
  };

  render() {
    const { enabled, minutesBeforeEvent, soundEnabled } = this.props;

    return (
      <div>
        <div className="list-group fixed setting-colour-0">
          <div className="list-group-item">
            <div className="list-group-item-heading">
              <h3>Timetable alarms</h3>
            </div>
          </div>
        </div>

        <div className="hint-text container-fluid">
          <p>
            Turning Timetable alarms on will show a notification and play a sound on this device
            shortly before every event in your teaching timetable.
          </p>
        </div>

        <div className="list-group setting-colour-2">
          <SwitchListGroupItem
            id="timetableAlarmsEnabled"
            value=""
            icon="bell"
            description="Timetable alarms"
            role="button"
            tabIndex={0}
            onClick={this.onToggleEnabled}
            checked={enabled}
            failure={false}
            loading={false}
            disabled={false}
          />
        </div>

        {enabled &&
        <div className="list-group setting-colour-2">
          {TIMINGS.map(minutes =>
            (<RadioListGroupItem
              key={`timetable-minutes-${minutes.toString(10)}`}
              description={`${TimetableAlarmsView.getDescriptionForTiming(minutes)}`}
              onClick={this.onSetTiming(minutes)}
              value={minutes.toString(10)}
              checked={minutesBeforeEvent === minutes}
            />),
          )}
        </div>}

        {enabled &&
        window.MyWarwickNative &&
        window.MyWarwickNative.setTimetableNotificationsSoundEnabled &&
        <div className="list-group setting-colour-2">
          <SwitchListGroupItem
            id="timetableAlarmsSoundEnabled"
            value=""
            icon="volume-up"
            description="Play notification sound"
            role="button"
            tabIndex={0}
            onClick={this.onToggleSoundEnabled}
            checked={soundEnabled}
            failure={false}
            loading={false}
            disabled={false}
          />
        </div>}
      </div>
    );
  }

  static getDescriptionForTiming(timingInMinute) {
    const makeHourPhrase = hours => `${hours} ${pluralise('hour', hours)}`;
    const makeMinutePhrase = minutes => `${minutes} ${pluralise('minute', minutes)}`;
    if (timingInMinute < 60) return `${makeMinutePhrase(timingInMinute)} before`;
    const hours = Math.floor(timingInMinute / 60);
    const remaining = timingInMinute % 60;
    return remaining > 0 ?
      `${makeHourPhrase(hours)} ${makeMinutePhrase(remaining)} before` :
      `${makeHourPhrase(hours)} before`;
  }
}

function select(state) {
  return state.timetableAlarms;
}

export default connect(select)(TimetableAlarmsView);
