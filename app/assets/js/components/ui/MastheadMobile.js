import React from 'react';
import UtilityBar from './UtilityBar';

export default class MastheadMobile extends React.Component {

  render() {
    return (
      <div className="start-masthead use-popover">
        <div className="back-btn" onClick={this.props.onBackClick}>
          <i className="fa fa-chevron-left"/>
          Back
        </div>
        <div className="masthead-title">
          <span className="light">Start.</span>Warwick
        </div>
        <nav className="id7-utility-bar">
          <UtilityBar {...this.props} layoutClassName="mobile" />
        </nav>
      </div>
    );
  }

}

MastheadMobile.propTypes = {
  zoomedTile: React.PropTypes.string,
  path: React.PropTypes.string,
  onBackClick: React.PropTypes.func,
};
