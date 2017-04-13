import React, { PropTypes } from 'react';

export default class ListHeader extends React.Component {

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
