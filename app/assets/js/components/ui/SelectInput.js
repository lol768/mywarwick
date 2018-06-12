import React from 'react';
import * as PropTypes from 'prop-types';

export default class SelectInput extends React.PureComponent {
  static defaultProps = {
    disabled: false,
  };

  static propTypes = {
    values: PropTypes.arrayOf(PropTypes.string).isRequired,
    onChange: PropTypes.func.isRequired,
    className: PropTypes.string,
    selectedValue: PropTypes.string,
    disabled: PropTypes.bool,
    disabledOption: PropTypes.string,
  };

  render() {
    const { selectedValue, onChange, className, disabled, disabledOption } = this.props;
    return (
      <select
        disabled={disabled}
        value={selectedValue}
        className={`form-control ${className || ''}`}
        onChange={({ target: { value } }) =>
          onChange(value)
        }
      >
        {this.props.values.map((val, i) => (
          <option disabled={disabledOption === val} key={i} value={val}>
            {val}
          </option>
        ))}
      </select>
    );
  }
}
