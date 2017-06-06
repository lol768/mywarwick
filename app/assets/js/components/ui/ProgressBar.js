import React, { PropTypes } from 'react';

export default class ProgressBar extends React.PureComponent {

  static propTypes = {
    value: PropTypes.number.isRequired,
    max: PropTypes.number.isRequired,
  };

  render() {
    const percentage = Math.round((this.props.value / this.props.max) * 100);

    return (
      <div className="progress">
        <div
          className="progress-bar progress-bar-striped active"
          role="progressbar"
          aria-valuemin="0"
          aria-valuenow={this.props.value}
          aria-valuemax={this.props.max}
          style={{ width: `${percentage}%`, transition: 'none', backgroundColor: '#8c6e96' }}
        >
          <span className="sr-only">{percentage}% complete</span>
        </div>
      </div>
    );
  }

}
