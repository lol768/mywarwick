import React, { PropTypes } from 'react';
import ReactComponent from 'react/lib/ReactComponent';
import classNames from 'classnames';
import { handlePushSubscribe } from '../../push-notifications';

export class ToggleSwitch extends ReactComponent {

  constructor(props) {
    super(props);
    this.state = {
      switchOn: (props.defaultToggleState || false)
    };
  }

  handleClick() {
    this.setState({
      switchOn: !this.state.switchOn
    });
    this.props.handleToggle()
  }

  render() {
    return (
      <div>
        {this.props.buttonText}
        {this.state.switchOn ?
          <i className={classNames('fa', 'fa-toggle-on', 'fa-2x', 'active')}
             onClick={this.handleClick.bind(this)}>
          </i>
          :
          <i className={classNames('fa', 'fa-toggle-on', 'fa-rotate-180', 'fa-2x', 'inactive')}
             onClick={this.handleClick.bind(this)}>
          </i>
        }
      </div>
    )
  }
}

ToggleSwitch.propTypes = {
  defaultToggleState: PropTypes.bool,
  handleToggle: PropTypes.func.isRequired,
  isDisabled: PropTypes.bool
};

ToggleSwitch.defaultProps = {
  isDisabled: false
};

export class SettingsView extends ReactComponent {

  render() {
    return (
      <div>
        <ul className={classNames('settings-list','well')}>
          {this.props.settings.map((item) => {
              let disabled = item.props.isDisabled ? 'disabled' : '';
              return (
                <li className={classNames("settings-list-item", disabled)}>
                  {item}
                </li>
              )
            }
          )}
        </ul>
      </div>
    )
  }
}

// TODO: implement settings props fed in from top of app
let appSettings = [
  <ToggleSwitch buttonText="Push notifications" handleToggle={handlePushSubscribe}/>
];

SettingsView.defaultProps = {
  settings: appSettings
};

