import React, { PropTypes } from 'react';

export default class Switch extends React.PureComponent {

  static propTypes = {
    onClick: PropTypes.func.isRequired, // naming for consistency
    checked: PropTypes.bool.optional,
  };

  static defaultProps = {
    checked: true,
  };

  render() {
    return (
      <div className="switch">
        <input type="checkbox" id="TODO" className="switch__checkbox" />
        <label className="switch__length" htmlFor="TODO"
          checked={ this.props.checked } onChange = { this.props.onClick }
        />
      </div>
    );
  }
}
