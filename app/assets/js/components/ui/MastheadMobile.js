import React from 'react';
import UtilityBar from './UtilityBar';

export default class MastheadMobile extends React.Component {
  constructor(props) {
    super(props);
  }

  render() {
    return (
      <div className="start-masthead">
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
