import React from 'react';
import * as PropTypes from 'prop-types';

export default class Switch extends React.PureComponent {

  static propTypes = {
    checked: PropTypes.bool.isRequired,
    id: PropTypes.string.isRequired,
  };

  static defaultProps = {
    checked: true,
  };

  static stopPropagation(e) {
    e.stopPropagation();
  }

  render() {
    return (
      <div className="switch">
        <input type="checkbox" id={ this.props.id } className="switch__checkbox"
          checked={ this.props.checked } readOnly
        />
        <label className="switch__length" htmlFor={ this.props.id }
          onClick={ Switch.stopPropagation }
        />
      </div>
    );
  }
}
