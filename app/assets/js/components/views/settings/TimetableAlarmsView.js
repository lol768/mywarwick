import React, { PropTypes } from 'react';
import { connect } from 'react-redux';
import HideableView from '../HideableView';
import SwitchListGroupItem from '../../ui/SwitchListGroupItem';
import RadioListGroupItem from '../../ui/RadioListGroupItem';
import { update } from '../../../state/timetable-alarms';

const TIMINGS = [
  5,
  15,
  30,
  60,
];

class TimetableAlarmsView extends HideableView {
  static propTypes = {
    enabled: PropTypes.bool.isRequired,
    minutesBeforeEvent: PropTypes.number.isRequired,
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

  render() {
    const { enabled, minutesBeforeEvent } = this.props;

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

        <div className="list-group setting-colour-0">
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
        <div className="list-group setting-colour-1">
          {TIMINGS.map(minutes =>
            (<RadioListGroupItem
              description={`${minutes} minutes before`}
              onClick={this.onSetTiming(minutes)}
              value={minutes}
              checked={minutesBeforeEvent === minutes}
            />),
          )}
        </div>}
      </div>
    );
  }
}

function select(state) {
  return state.timetableAlarms;
}

export default connect(select)(TimetableAlarmsView);
