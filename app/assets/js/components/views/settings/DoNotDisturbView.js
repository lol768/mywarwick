import React from 'react';
import * as PropTypes from 'prop-types';
import HideableView from '../HideableView';
import { connect } from 'react-redux';
import SwitchListGroupItem from '../../ui/SwitchListGroupItem';
import { update, fetch } from '../../../state/do-not-disturb';
import SelectInput from '../../ui/SelectInput';
import { Info } from '../../FA';
import _ from 'lodash-es';

const numberRangePropType =
  (end, start = 0) => function timeHourPropType(props, propName, componentName) {
    if (!_.inRange(props[propName], start, end)) {
      return new Error(
        `Invalid prop '${propName}' supplied to '${componentName}'.
      Number ${props[propName]} not in range ${start}-${end}`,
      );
    }
    return null;
  };

const doNotDisturbTimePropType = PropTypes.shape({
  hr: numberRangePropType(24),
  min: numberRangePropType(60),
});

class DoNotDisturbView extends HideableView {
  static propTypes = {
    dispatch: PropTypes.func.isRequired,
    isOnline: PropTypes.bool.isRequired,
    enabled: PropTypes.bool.isRequired,
    start: doNotDisturbTimePropType.isRequired,
    end: doNotDisturbTimePropType.isRequired,
  };

  static options = [...Array(24).keys()].map(i =>
    ({ val: i, displayName: `${_.padStart(i, 2, '0')}:00` }),
  );

  constructor(props) {
    super(props);
    this.toggleDoNotDisturb = this.toggleDoNotDisturb.bind(this);
    this.onStartChange = this.onStartChange.bind(this);
    this.onEndChange = this.onEndChange.bind(this);
    this.dndPeriodHrs = this.dndPeriodHrs.bind(this);
  }

  componentDidShow() {
    if (!this.props.isOnline) return;
    this.props.dispatch(fetch());
  }

  toggleDoNotDisturb() {
    this.props.dispatch(update({
      enabled: !this.props.enabled,
    }));
  }

  /**
   * UI currently does not support the configuration of minute value, so it is hardcoded here.
   * @param value passed in as string from synthesised event object, must parseInt
   */
  onStartChange(value) {
    this.props.dispatch(update({
      start: {
        hr: parseInt(value, 10),
        min: 0,
      },
    }));
  }

  /**
   * UI currently does not support the configuration of minute value, so it is hardcoded here.
   * @param value passed in as string from synthesised event object, must parseInt
   */
  onEndChange(value) {
    this.props.dispatch(update({
      end: {
        hr: parseInt(value, 10),
        min: 0,
      },
    }));
  }

  dndPeriodHrs() {
    const { start, end } = this.props;
    if (end.hr < start.hr) {
      return Math.abs((start.hr < 12 ? end.hr + 24 : end.hr + 12) - (start.hr % 12));
    }
    return end.hr - start.hr;
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
            between <strong>From</strong> and <strong>Until</strong> times each day. Any alerts that
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
              options={DoNotDisturbView.options}
              disabledOption={this.props.end.hr}
              selectedValue={this.props.start.hr}
              onChange={this.onStartChange}
            />
          </div>
          <div className="list-group-item">
            <label>Until</label>
            <SelectInput
              disabled={!this.props.enabled}
              options={DoNotDisturbView.options}
              disabledOption={this.props.start.hr}
              selectedValue={this.props.end.hr}
              onChange={this.onEndChange}
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
  return {
    ...state.doNotDisturb,
    isOnline: state.device.isOnline,
  };
}

export default connect(select)(DoNotDisturbView);
