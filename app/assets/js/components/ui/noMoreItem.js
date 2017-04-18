import React, { PropTypes } from 'react';

export default class NoMoreItem extends React.PureComponent {

  static propTypes = {
    visible: PropTypes.bool,
    type: PropTypes.String,
  };

  constructor(props) {
    super(props);
    this.phrases = {
      ActivityView: 'There are no older activities.',
      NotificationsView: 'There are no older notifications.',
      NewsView: 'There is no older news.',
    };
  }

  render() {
    return (
      <div className="centered empty-state">
        { this.props.visible ?
          <p className="lead" id="no_more_item">{ this.phrases[this.props.type] }</p> : '' }
      </div>
    );
  }

}
