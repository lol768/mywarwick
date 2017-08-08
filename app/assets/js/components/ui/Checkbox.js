import React from 'react';
import * as PropTypes from 'prop-types';

/**
 * Simple checkbox or radio input that displays sub-fields as children when checked
 */
export class Checkbox extends React.PureComponent {

  static propTypes = {
    label: PropTypes.string.isRequired,
    type: PropTypes.oneOf(['checkbox', 'radio']),
    handleChange: PropTypes.func.isRequired,
    isChecked: PropTypes.bool,
    btnGroup: PropTypes.string
  };

  static defaultProps = {
    type: 'checkbox'
  };

  constructor(props) {
    super(props);
    this.toggle = this.toggle.bind(this);
  }

  toggle(event) {
    const { handleChange, btnGroup, type } = this.props;
    handleChange(event, btnGroup, type)
  }

  render() {

    const { value, label, type, isChecked, children, btnGroup } = this.props;

    return (
      <div className={type}>
        <label>
          <input
            type={type}
            name={btnGroup}
            checked={isChecked}
            value={value}
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
    type: 'radio'
  };

  constructor(props) {
    super(props);
  }
}
