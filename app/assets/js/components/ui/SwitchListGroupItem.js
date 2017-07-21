import React from 'react';
import * as PropTypes from 'prop-types';
import Switch from './Switch';
import ListGroupItem from './ListGroupItem';

export default class SwitchListGroupItem extends React.PureComponent {
  static propTypes = {
    ...ListGroupItem.propTypes,
    id: PropTypes.string.isRequired,
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

  renderSwitch() {
    return (
      <Switch
        id={ this.props.id }
        checked={ this.props.checked }
        disabled={ this.props.disabled }
      />
    );
  }

  render() {
    return (
      <ListGroupItem {...this.props} onClick={this.onClick} uiControl={this.renderSwitch()} />
    );
  }
}
