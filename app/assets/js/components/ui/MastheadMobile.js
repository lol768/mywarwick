import React from 'react';

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
          { this.props.utilityBar}
        </nav>
      </div>
    );
  }
}

MastheadMobile.propTypes = {
  utilityBar: React.PropTypes.element,
};
