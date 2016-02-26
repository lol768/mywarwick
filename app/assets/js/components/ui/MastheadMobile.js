import React from 'react';
import UtilityBar from './UtilityBar';

export default class MastheadMobile extends React.Component {
  constructor(props) {
    super(props);
  }

  render() {
    return (
      <div className="start-masthead">
        {this.props.zoomedTile ?
          <div className="back-btn" onClick={this.props.onBackClick}>
            <i className="fa fa-chevron-left" />
            Back
          </div>
          : null }
        <div className="masthead-title">
          <span className="light">
          START.
          </span>
          WARWICK
        </div>
        <nav className="id7-utility-bar">
          <UtilityBar {...this.props} />
        </nav>
      </div>
    );
  }
}

MastheadMobile.propTypes = {
  zoomedTile: React.PropTypes.string,
  onBackClick: React.PropTypes.func,
};
