import React from 'react';
import * as PropTypes from 'prop-types';
import HideableView from '../HideableView';
import { connect } from 'react-redux';
import SwitchListGroupItem from '../../ui/SwitchListGroupItem';
import { updateDoNotDisturb } from '../../../state/device';
import SelectNumberInput from '../../ui/NumberSelectInput';
import { Info } from '../../FA';

const doNotDisturbPeriodPropType = PropTypes.shape({
  start: PropTypes.number.isRequired,
  end: PropTypes.number.isRequired,
});

class DoNotDisturbView extends HideableView {
  static propTypes = {
    loaded: PropTypes.bool.isRequired,
    enabled: PropTypes.bool.isRequired,
    weekend: doNotDisturbPeriodPropType.isRequired,
    weekday: doNotDisturbPeriodPropType.isRequired,
  };

  static MIN_HOUR = 0;
  static MAX_HOUR = 23;

  static formatOptionDisplayName(val) {
    return `${val.toString().padStart(2, '0')}:00`;
  }

  constructor(props) {
    super(props);
    this.toggleDoNotDisturb = this.toggleDoNotDisturb.bind(this);
    this.onStartHourChange = this.onStartHourChange.bind(this);
    this.onEndHourChange = this.onEndHourChange.bind(this);
  }

  toggleDoNotDisturb() {
    this.props.dispatch(updateDoNotDisturb({
      enabled: !this.props.enabled,
    }));
  }

  /**
   * Currently UI prevents separate configuration of weekday and weekend times
   * This updates start time of both weekend and weekday
   * @param value
   */
  onStartHourChange(value) {
    this.props.dispatch(updateDoNotDisturb({
      weekday: {
        start: value,
        end: this.props.weekday.end,
      },
      weekend: {
        start: value,
        end: this.props.weekday.end,
      },
    }));
  }

  /**
   * Currently UI prevents separate configuration of weekday and weekend times
   * This updates end time of both weekend and weekday
   * @param value
   */
  onEndHourChange(value) {
    this.props.dispatch(updateDoNotDisturb({
      weekday: {
        start: this.props.weekday.start,
        end: value,
      },
      weekend: {
        start: this.props.weekday.start,
        end: value,
      },
    }));
  }

  dndPeriodHrs() {
    const { weekday: { start, end } } = this.props;
    if (end < start) {
      return Math.abs((end + 12) - (start % 12));
    }
    return end - start;
  }

  render() {
    return (
      <div>
        <div className="list-group fixed">
          <div className="list-group-item">
            <div className="list-group-item-heading">
              <h3>Do not disturb</h3>
            </div>
          </div>
        </div>

        <div className="text--hint container-fluid">
          <p>
            Turning &lsquo;Do not disturb&rsquo; on means alerts won&apos;t pop up on your phone
            between <strong>from</strong> and <strong>until</strong> times each day. Any alerts that
            would have arrived during this period will instead be delivered shortly after the period
            ends.
          </p>
        </div>

        <div className="list-group setting-colour-2">
          <SwitchListGroupItem
            id="doNotDisturb"
            value=""
            checked={this.props.enabled}
            icon="clock-o"
            description="Do not disturb"
            onClick={this.toggleDoNotDisturb}
          />
        </div>

        <div className="list-group setting-colour-2">
          <div className="list-group-item">
            <label>From</label>
            <SelectNumberInput
              disabled={!this.props.enabled}
              min={DoNotDisturbView.MIN_HOUR}
              max={DoNotDisturbView.MAX_HOUR}
              disabledOption={this.props.weekday.end}
              selectedValue={this.props.weekday.start}
              formatOptionDisplayName={DoNotDisturbView.formatOptionDisplayName}
              onChange={this.onStartHourChange}
            />
          </div>
          <div className="list-group-item">
            <label>Until</label>
            <SelectNumberInput
              disabled={!this.props.enabled}
              min={DoNotDisturbView.MIN_HOUR}
              max={DoNotDisturbView.MAX_HOUR}
              disabledOption={this.props.weekday.start}
              selectedValue={this.props.weekday.end}
              formatOptionDisplayName={DoNotDisturbView.formatOptionDisplayName}
              onChange={this.onEndHourChange}
            />
          </div>
        </div>

        {this.props.enabled &&
        <div className="text--hint container-fluid">
          <p><Info fw /> You have a do not disturb period of {this.dndPeriodHrs()} hours.</p>
        </div>
        }

      </div>
    );
  }
}

function select(state) {
  return state.device.doNotDisturb;
}

export default connect(select)(DoNotDisturbView);
