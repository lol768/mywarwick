import React from 'react';
import * as PropTypes from 'prop-types';

export default class ListHeader extends React.PureComponent {

  static propTypes = {
    title: PropTypes.string.isRequired,
  };

  render() {
    return (
      <div className="list-group-item list-group-item--header">
        {this.props.title}
      </div>
    );
  }

}
