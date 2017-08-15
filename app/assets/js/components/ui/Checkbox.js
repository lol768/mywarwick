import React from 'react';
import * as PropTypes from 'prop-types';

/**
 * Simple checkbox or radio input that displays sub-fields as children when checked
 */
export class Checkbox extends React.PureComponent {
  static propTypes = {
    btnGroup: PropTypes.string,
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
    const { handleChange, btnGroup, type } = this.props;
    handleChange(name, btnGroup, type);
  }

  render() {
    const { name, label, type, isChecked, children } = this.props;

    return (
      <div className={type}>
        <label>
          <input
            type={type}
            name={name}
            value={this.props.value}
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
