import React from 'react';
import * as PropTypes from 'prop-types';

/**
 * Simple checkbox or radio input that displays sub-fields as children when checked
 */
export class Checkbox extends React.PureComponent {
  static propTypes = {
    children: PropTypes.oneOfType([
      PropTypes.arrayOf(PropTypes.node),
      PropTypes.node,
    ]),
    handleChange: PropTypes.func.isRequired,
    isChecked: PropTypes.bool,
    label: PropTypes.string.isRequired,
    name: PropTypes.string.isRequired,
    type: PropTypes.oneOf(['checkbox', 'radio']),
    value: PropTypes.string,
  };

  static defaultProps = {
    type: 'checkbox',
    value: undefined,
  };

  constructor(props) {
    super(props);
    this.toggle = this.toggle.bind(this);
  }

  toggle({ target: { name } }) {
    const { value, type, handleChange } = this.props;
    handleChange(value, type, name);
  }

  render() {
    const { name, label, type, isChecked, children, value } = this.props;

    return (
      <div className={type}>
        <label className="control-label">
          <input
            className="form-check"
            type={type}
            name={name}
            value={value}
            checked={isChecked}
            onChange={this.toggle}
          />
          {label}
        </label>
        {
          isChecked &&
          children
        }
      </div>
    );
  }
}

export class RadioButton extends Checkbox {
  static defaultProps = {
    type: 'radio',
  };
}
