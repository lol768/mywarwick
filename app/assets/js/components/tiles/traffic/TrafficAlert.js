import React from 'react';
import * as PropTypes from 'prop-types';

export default class TrafficAlert extends React.PureComponent {
  static propTypes = {
    title: PropTypes.string.isRequired,
    href: PropTypes.string.isRequired,
  };

  render() {
    const { title, href } = this.props;
    return (
      <div className="traffic-alert">
        <i className="fa fa-fw fa-exclamation-triangle" />
        <a href={href}>{title}</a>
      </div>
    );
  }
}
