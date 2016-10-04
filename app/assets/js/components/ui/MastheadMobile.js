import React from 'react';
import UtilityBar from './UtilityBar';

import $ from 'jquery';
import ReactDOM from 'react-dom';

export default class MastheadMobile extends React.Component {

  componentDidMount() {
    $(ReactDOM.findDOMNode(this)).on('touchmove', (e) => e.preventDefault());
  }

  render() {
    return (
      <div className="start-masthead use-popover">
        <div className="back-btn" onClick={this.props.onBackClick}>
          <i className="fa fa-chevron-left" />
          Back
        </div>
        <div className="masthead-title">
          <span className="light">My Warwick</span>
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
