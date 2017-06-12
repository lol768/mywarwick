import React, { PropTypes } from 'react';

export default class TrafficAlert extends React.PureComponent {
  render() {
    const { title, href } = this.props;
    return (
      <div className="traffic-alert">
        <i className="fa fa-fw fa-exclamation-triangle"> </i>
        <a href={href}>{title}</a>
      </div>
    );
  }
}

TrafficAlert.propTypes = {
  title: PropTypes.string,
  href: PropTypes.string,
};
