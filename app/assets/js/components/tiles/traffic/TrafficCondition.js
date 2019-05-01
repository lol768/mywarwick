import React from 'react';
import * as PropTypes from 'prop-types';
import classNames from 'classnames';

const TRAFFIC_STATE = {
  SEVERE: 'fa-exclamation-triangle', // takes at least four times as long as usual
  MODERATE: 'fa-hourglass-half', // takes at least twice as long as usual
  NORMAL: 'fa-check-circle',
};

export default class TrafficCondition extends React.PureComponent {
  static propTypes = {
    route: PropTypes.shape({
      name: PropTypes.string,
      inbound: PropTypes.bool,
      start: PropTypes.shape({
        latitude: PropTypes.number,
        longitude: PropTypes.number,
      }),
      end: PropTypes.shape({
        latitude: PropTypes.number,
        longitude: PropTypes.number,
      }),
    }).isRequired,
    usualDuration: PropTypes.shape({
      text: PropTypes.string,
      value: PropTypes.number,
    }).isRequired,
    actualDuration: PropTypes.shape({
      text: PropTypes.string,
      value: PropTypes.number,
    }).isRequired,
  };

  render() {
    const { route, usualDuration, actualDuration } = this.props;

    const durationCoefficient =
      Math.floor(actualDuration.value / usualDuration.value);

    let status;
    if (durationCoefficient >= 4) {
      status = TRAFFIC_STATE.SEVERE;
    } else if (durationCoefficient >= 2) {
      status = TRAFFIC_STATE.MODERATE;
    } else {
      status = TRAFFIC_STATE.NORMAL;
    }

    let message = '';
    if (durationCoefficient >= 2) {
      const delaySeconds = actualDuration.value - usualDuration.value;
      const delayMinutes = Math.round(delaySeconds / 60);
      message = `: +${delayMinutes} mins`;
    }

    return (<li>
      <i className={classNames('fal', 'fa-fw', status)} />
      <span className="route-name">{route.name}</span>{message}
    </li>);
  }
}
