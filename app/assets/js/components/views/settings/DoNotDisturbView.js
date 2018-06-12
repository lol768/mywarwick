import React from 'react';
import * as PropTypes from 'prop-types';
import HideableView from '../HideableView';
import { connect } from 'react-redux';
import SwitchListGroupItem from '../../ui/SwitchListGroupItem';
import { updateDoNotDisturb } from '../../../state/device';
import SelectInput from '../../ui/SelectInput';
import { Info } from '../../FA';

const time24PropType = function timePropType(props, propName, componentName) {
  if (!/([01][0-9]|2[0-3]):[0-5][0-9]/.test(props[propName])) {
    return new Error(
      `Invalid prop '${propName}' supplied to '${componentName}'. Should be format "HH:mm".`,
    );
  }
  return null;
};

const doNotDisturbPeriodPropType = PropTypes.shape({
  start: time24PropType.isRequired,
  end: time24PropType.isRequired,
});

class DoNotDisturbView extends HideableView {
  static propTypes = {
    loaded: PropTypes.bool.isRequired,
    enabled: PropTypes.bool.isRequired,
    weekend: doNotDisturbPeriodPropType.isRequired,
    weekday: doNotDisturbPeriodPropType.isRequired,
  };

  static values = [...Array(24).keys()].map(i => `${(i + '').padStart(2, '0')}:00`);

  constructor(props) {
    super(props);
    this.toggleDoNotDisturb = this.toggleDoNotDisturb.bind(this);
    this.onStartHourChange = this.onStartHourChange.bind(this);
    this.onEndHourChange = this.onEndHourChange.bind(this);
    this.dndHoursAsNumbers = this.dndHoursAsNumbers.bind(this);
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

  dndHoursAsNumbers() {
    return {
      weekday: {
        start: parseInt(this.props.weekday.start.slice(2), 10),
        end: parseInt(this.props.weekday.end.slice(2), 10),
      },
    };
  }

  dndPeriodHrs() {
    const { weekday: { start, end } } = this.dndHoursAsNumbers();
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
            <SelectInput
              disabled={!this.props.enabled}
              values={DoNotDisturbView.values}
              disabledOption={this.props.weekday.end}
              selectedValue={this.props.weekday.start}
              onChange={this.onStartHourChange}
            />
          </div>
          <div className="list-group-item">
            <label>Until</label>
            <SelectInput
              disabled={!this.props.enabled}
              values={DoNotDisturbView.values}
              disabledOption={this.props.weekday.start}
              selectedValue={this.props.weekday.end}
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
