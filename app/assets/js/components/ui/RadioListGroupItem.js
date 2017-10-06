import React from 'react';
import * as PropTypes from 'prop-types';
import ListGroupItem from './ListGroupItem';

export default class RadioListGroupItem extends React.PureComponent {
  static propTypes = {
    ...ListGroupItem.propTypes,
    name: PropTypes.string,
    value: PropTypes.string.isRequired,
    checked: PropTypes.bool.isRequired,
    settingColour: PropTypes.string,
  };

  static defaultProps = {
    ...ListGroupItem.defaultProps,
  };

  constructor(props) {
    super(props);

    this.state = {
      animated: false,
    };

    this.onClick = this.onClick.bind(this);
  }

  onClick() {
    // Only allow the ripple animation to happen after the user has clicked the radio button
    this.setState({
      animated: true,
    });

    this.props.onClick(this.props.value, this.props.name);
  }

  renderRadio() {
    return (
      <div className={ `md-radio ${this.state.animated ? 'md-radio--animated' : ''}` }>
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
