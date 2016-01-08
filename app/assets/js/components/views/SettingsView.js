import React, { PropTypes } from 'react';
import ReactComponent from 'react/lib/ReactComponent';
import classNames from 'classnames';

import { handlePushSubscribe } from '../../push-notifications';
import ToggleSwitch from '../ui/ToggleSwitch';

export class SettingsView extends ReactComponent {

  render() {
    return (
      <div>
        <ul className={classNames('settings-list')}>
          {this.props.settings.map((item) => {
              let disabled = item.props.isDisabled ? 'disabled' : '';
              return (
                <li key={item.id} className={classNames("settings-list-item", 'well', disabled)}>
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
  <ToggleSwitch id="push-notifications" buttonText="Push notifications" handleToggle={handlePushSubscribe}/>
];

SettingsView.defaultProps = {
  settings: appSettings
};

