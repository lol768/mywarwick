import React from 'react';
import * as PropTypes from 'prop-types';

export default class Switch extends React.PureComponent {

  constructor(props) {
    super(props);
    this.onChange = this.onChange.bind(this);
    this.state = { checked: true} ;
  }

  onChange(e) {
    this.setState({ checked: e.target.checked });
    this.props.onClick(e.target.checked);
  }

  static propTypes = {
    onClick: PropTypes.func.isRequired, // naming for consistency
    checked: PropTypes.bool.optional,
    id: PropTypes.string.isRequired,
  };

  static defaultProps = {
    checked: true,
  };

  render() {
    return (
      <div className="switch">
        <input type="checkbox" id={ this.props.id } className="switch__checkbox"
               checked={ this.state.checked } onChange={ this.onChange }
        />
        <label className="switch__length" htmlFor={ this.props.id } />
      </div>
    );
  }
}
