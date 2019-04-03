/* eslint-env browser */

import React from 'react';
import * as PropTypes from 'prop-types';
import classNames from 'classnames';
import * as pushNotifications from '../../push-notifications';

export default class PermissionRequest extends React.PureComponent {
  static propTypes = {
    isDisabled: PropTypes.bool,
  };

  constructor(props) {
    super(props);
    this.hide = this.hide.bind(this);
    this.requestPermission = this.requestPermission.bind(this);
    this.state = {
      visible: true,
    };
  }

  hide(e) {
    e.preventDefault();
    this.setState({ visible: false });
  }

  requestPermission(e) {
    e.preventDefault();
    this.setState({ visible: false });
    // FIXME: a new subscription is saved every time user re-enables push notifications
    pushNotifications.subscribe();
  }

  render() {
    return (
      window.Notification && !this.props.isDisabled && this.state.visible ?
        <div className={classNames('permission-request')}>
          <div className="permission-notice">
            My Warwick needs your permission to
            {' '}
            <a
              onClick={ this.requestPermission }
              role="button"
              tabIndex={0}
            >
              show system notifications
            </a>
          </div>
          <div className="permission-dismiss">
            <i
              className={classNames('fal', 'fa-fw', 'fa-times')}
              role="button"
              tabIndex={0}
              onClick={this.hide}
            />
          </div>
        </div> : null
    );
  }
}
