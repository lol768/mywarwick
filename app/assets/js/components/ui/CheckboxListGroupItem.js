import React from 'react';
import * as PropTypes from 'prop-types';
import classNames from 'classnames';
import ListGroupItem from './ListGroupItem';

export default class CheckboxListGroupItem extends React.PureComponent {
  static propTypes = {
    ...ListGroupItem.propTypes,
    name: PropTypes.string,
    value: PropTypes.string.isRequired,
    checked: PropTypes.bool.isRequired,
  };

  static defaultProps = {
    ...ListGroupItem.defaultProps,
  };

  constructor(props) {
    super(props);
    this.onClick = this.onClick.bind(this);
  }

  onClick() {
    this.props.onClick(this.props.value, this.props.name);
  }

  renderCheckbox() {
    return (
      <span
        className={ classNames('checkbox', {
          'checkbox--checked': this.props.checked,
          'checkbox--disabled': this.props.disabled,
        }) }
      >
        <i className="fal fa-check" />
      </span>
    );
  }

  render() {
    return (
      <ListGroupItem {...this.props} onClick={this.onClick} uiControl={this.renderCheckbox()} />
    );
  }
}
