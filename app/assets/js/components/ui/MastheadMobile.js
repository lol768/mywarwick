import React from 'react';
import UtilityBar from './UtilityBar';

export default class MastheadMobile extends React.Component {
  constructor(props) {
    super(props);
  }

  render() {
    const { zoomedTile, onBackClick, path } = this.props;
    return (
      <div className="start-masthead">
        {zoomedTile && path === '/' ?
          <div className="back-btn" onClick={onBackClick}>
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
  path: React.PropTypes.string,
  onBackClick: React.PropTypes.func,
};
