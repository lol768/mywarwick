import React, { PropTypes } from 'react';

export default class Switch extends React.PureComponent {

  render() {
    return <div className="switch">
      <input type="checkbox" id="TODO" className="switch__checkbox" />
      <label className="switch__length" htmlFor="TODO"/>
    </div>;
  }
}