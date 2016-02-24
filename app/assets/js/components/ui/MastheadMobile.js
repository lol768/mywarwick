import React from 'react';

export default class MastheadMobile extends React.Component {
  constructor(props) {
    super(props);
  }

  render() {
    return (
      <div className="id7-utility-bar start-masthead">
        <div className="masthead-title">
          <span className="light">
          START.
          </span>
          WARWICK
          <nav>
            { this.props.utilityBar}
          </nav>
        </div>
      </div>
    );
  }
}

MastheadMobile.propTypes = {
  utilityBar: React.PropTypes.element,
};
