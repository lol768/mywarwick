import React, { PropTypes } from 'react';
import ReactComponent from 'react/lib/ReactComponent';
import classNames from 'classnames';

import { handlePushSubscribe } from '../../push-notifications';
import ToggleSwitch from '../ui/ToggleSwitch';

export class SettingsView extends ReactComponent {

  constructor(props) {
    super(props);

    this.state = {
      browserPushDisabled: 'Notification' in window && Notification.permission === "denied"
    };

    navigator.permissions.query({name:'notifications'})
      .then(notificationPermissions => {
        notificationPermissions.onchange = this.onBrowserPermissionChange.bind(this);
      });
  }

  onBrowserPermissionChange() {
    this.setState({
      browserPushDisabled: 'Notification' in window && Notification.permission === "denied"
    });
  }

  render() {
    return (
      <div>
        { this.state.browserPushDisabled ?
          <div className="permission-warning">
            You have blocked Start.Warwick from sending notifications. You'll need to open your browser preferences to change that.
          </div>
          : null
        }
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
  // TODO: If your adding the first settings to the app remember to ...
      // Remove the notification permissions stuff from NotificationsView. It will live here instead.
      // Put the settings MastheadIcon back in ID7Layout
];

SettingsView.defaultProps = {
  settings: appSettings
};

