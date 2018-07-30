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

  constructor(props) {
    super(props);
    this.onChange = this.onChange.bind(this);
  }

  onChange({ target: { value } }) {
    this.props.onChange(value);
  }

  render() {
    const { selectedValue, className, disabled, disabledOption, options } = this.props;
    return (
      <select
        disabled={disabled}
        value={selectedValue}
        className={`form-control ${className || ''}`}
        onChange={this.onChange}
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
