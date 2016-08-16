import React from 'react';
import ReactComponent from 'react/lib/ReactComponent';
import classNames from 'classnames';


export class SettingsView extends ReactComponent {

  constructor(props) {
    super(props);

    this.state = {
      browserPushDisabled: 'Notification' in window && Notification.permission === 'denied',
    };

    if ('permissions' in navigator) {
      navigator.permissions.query({ name: 'notifications' })
        .then(notificationPermissions => {
          /* eslint-disable no-param-reassign */
          /*
           * function parameter reassignment is valid here. See the Permissions API docs ...
           * https://developers.google.com/web/updates/2015/04/permissions-api-for-the-web?hl=en
           */
          notificationPermissions.onchange = this.onBrowserPermissionChange.bind(this);
          /* eslint-enable no-param-reassign */
        });
    }
  }

  onBrowserPermissionChange() {
    this.setState({
      browserPushDisabled: 'Notification' in window && Notification.permission === 'denied',
    });
  }

  render() {
    return (
      <div>
        { this.state.browserPushDisabled ?
          <div className="permission-warning">
            You have blocked Start.Warwick from showing system notifications. You'll need to open
            your browser preferences to change that.
          </div>
          : null
        }
        <ul className={ classNames('settings-list') }>
          { this.props.settings.map((item) => {
            const disabled = item.props.isDisabled ? 'disabled' : '';
            return (
              <li
                key={ item.id }
                className={ classNames('settings-list-item', 'well', disabled) }
              >
                {item}
              </li>
            );
          }) }
        </ul>
      </div>
    );
  }
}

// TODO: implement settings props fed in from top of app
const appSettings = [
  // TODO: If your adding the first settings to the app remember to ...
  // Remove the notification permissions stuff from NotificationsView. It will live here instead.
  // Put the settings MastheadIcon back in ID7Layout
];

SettingsView.defaultProps = {
  settings: appSettings,
};
