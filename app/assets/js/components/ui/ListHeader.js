import React from 'react';
import * as PropTypes from 'prop-types';

export default class ListHeader extends React.PureComponent {
  static propTypes = {
    title: PropTypes.string.isRequired,
    subtitle: PropTypes.string,
  };

  render() {
    return (
      <div className="list-group-item list-group-item--header">
        {this.props.title}
        { this.props.subtitle && <span className="text--light"> {this.props.subtitle}</span> }
      </div>
    );
  }
}
