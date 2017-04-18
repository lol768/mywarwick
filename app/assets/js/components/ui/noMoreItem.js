import React, { PropTypes } from 'react';

export default class NoMoreItem extends React.Component {

  static propTypes = {
    visible: PropTypes.bool,
  };

  render() {
    return (
      <div className="centered">
        { this.props.visible ?
          <span className="label label-info" id="no_more_item">No more item</span> : '' }
      </div>
    );
  }

}
