import React from 'react';
import * as PropTypes from 'prop-types';
import ListGroupItem from './ListGroupItem';

export default class RadioListGroupItem extends React.PureComponent {
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

  renderRadio() {
    return (
      <div className="md-radio">
        <input
          type="radio"
          checked={ this.props.checked }
          readOnly
          disabled={ this.props.disabled }
        />
        <label />
      </div>
    );
  }

  render() {
    return (
      <ListGroupItem {...this.props} onClick={this.onClick} uiControl={this.renderRadio()} />
    );
  }
}
