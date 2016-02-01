import React, { PropTypes } from 'react';
import ReactComponent from 'react/lib/ReactComponent';
import classNames from 'classnames';

export default class PermissionRequest extends ReactComponent {

  constructor(props) {
    super(props);

    this.state = {
      visible: true
    };
  }

  hide(e) {
    e.preventDefault();
    this.setState({ visible: false });
  }

  requestPermission(e) {
    e.preventDefault();
    this.setState({ visible: false });
    window.Notification.requestPermission();
  }

  render() {
    return (
      window.Notification && !this.props.isDisabled && this.state.visible ?
      <div className={classNames('permission-request')}>
        <div className="permission-notice">
          Start needs your permission to <a onClick={this.requestPermission.bind(this)}>send notifications</a>
        </div>
        <div className="permission-dismiss">
          <i className={classNames('fa', 'fa-fw', 'fa-times')} onClick={this.hide.bind(this)}> </i>
        </div>
      </div> : null
    )
  }

}