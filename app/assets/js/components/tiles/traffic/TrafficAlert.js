import React, { PropTypes } from 'react';
import classNames from 'classnames';

export default class TrafficAlert extends React.Component {
  render() {
    const { title, href } = this.props;
    return (
      <div className="traffic-alert">
        <i className={classNames('fa', 'fa-exclamation-triangle')}> </i>
        <a href={href}>{title}</a>
      </div>
    );
  }
}

TrafficAlert.propTypes = {
  title: PropTypes.string,
  href: PropTypes.string,
};
