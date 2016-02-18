import React, { PropTypes } from 'react';
import classNames from 'classnames';

const TRAFFIC_STATE = {
  SEVERE: 'fa-exclamation-triangle', // takes at least four times as long as usual
  MODERATE: 'fa-hourglass-half', // takes at least twice as long as usual
  NORMAL: 'fa-check-circle',
};

export default class TrafficCondition extends React.Component {
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
      <i className={classNames('fa', status)}> </i>
      <strong>{route.name}</strong>{message}
    </li>);
  }
}

TrafficCondition.propTypes = {
  route: PropTypes.object,
  summary: PropTypes.string,
  usualDuration: PropTypes.object,
  actualDuration: PropTypes.object,
};
