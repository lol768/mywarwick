import React from 'react';
import * as PropTypes from 'prop-types';

export default class SelectNumberInput extends React.PureComponent {
  static defaultProps = {
    disabled: false,
  };

  static propTypes = {
    min: PropTypes.number.isRequired,
    max: PropTypes.number.isRequired,
    onChange: PropTypes.func.isRequired,
    className: PropTypes.string,
    selectedValue: PropTypes.number,
    formatOptionDisplayName: PropTypes.func,
    disabled: PropTypes.bool,
    disabledOption: PropTypes.number,
  };

  render() {
    const { selectedValue, min, max, onChange, className, formatOptionDisplayName, disabled }
      = this.props;
    return (
      <select
        disabled={disabled}
        value={selectedValue}
        className={`form-control ${className || ''}`}
        onChange={({ target: { value } }) =>
          onChange(parseInt(value, 10))
        }
      >
        {[...Array(max + 1).keys()].map((i) => {
          const val = i + min;
          return (<option disabled={this.props.disabledOption === val} key={i} value={val}>
            {typeof formatOptionDisplayName === 'function' ?
              formatOptionDisplayName(val) : val}
          </option>);
        })}
      </select>
    );
  }
}
