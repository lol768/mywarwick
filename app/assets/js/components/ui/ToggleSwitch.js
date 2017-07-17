import React from 'react';
import * as PropTypes from 'prop-types';
import classNames from 'classnames';

export default class ToggleSwitch extends React.PureComponent {

  constructor(props) {
    super(props);
    this.state = {
      switchOn: (props.defaultToggleState || false),
    };
    this.handleClick = this.handleClick.bind(this);
  }

  handleClick() {
    this.setState({
      switchOn: !this.state.switchOn,
    });
    this.props.handleToggle();
  }

  render() {
    return (
      <div>
        { this.props.buttonText }
        { this.state.switchOn ?
          <i
            className={ classNames('fa', 'fa-toggle-on', 'fa-2x', 'active', 'pull-right') }
            onClick={ this.handleClick }
          >
          </i>
          :
          <i
            className={
              classNames('fa', 'fa-toggle-on', 'fa-rotate-180', 'fa-2x', 'inactive', 'pull-right')
            }
            onClick={ this.handleClick}
          >
          </i>
        }
      </div>
    );
  }
}

ToggleSwitch.propTypes = {
  defaultToggleState: PropTypes.bool,
  handleToggle: PropTypes.func.isRequired,
  isDisabled: PropTypes.bool,
  buttonText: PropTypes.string.isRequired,
};

ToggleSwitch.defaultProps = {
  isDisabled: false,
};
