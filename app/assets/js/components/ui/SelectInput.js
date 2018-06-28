import React from 'react';
import * as PropTypes from 'prop-types';

export default class SelectInput extends React.PureComponent {
  static defaultProps = {
    disabled: false,
  };

  static propTypes = {
    options: PropTypes.arrayOf(PropTypes.shape({
      val: PropTypes.number.isRequired,
      displayName: PropTypes.string,
    })).isRequired,
    onChange: PropTypes.func.isRequired,
    className: PropTypes.string,
    selectedValue: PropTypes.number,
    disabled: PropTypes.bool,
    disabledOption: PropTypes.number,
  };

  render() {
    const { selectedValue, onChange, className, disabled, disabledOption, options } = this.props;
    return (
      <select
        disabled={disabled}
        value={selectedValue}
        className={`form-control ${className || ''}`}
        onChange={({ target: { value } }) =>
          onChange(value)
        }
      >
        {options.map((opt, i) => (
          <option disabled={disabledOption === opt.val} key={i} value={opt.val}>
            {opt.displayName}
          </option>
        ))}
      </select>
    );
  }
}
