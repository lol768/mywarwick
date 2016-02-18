import React from 'react';
import ReactDOM from 'react-dom';
import ReactComponent from 'react/lib/ReactComponent';
import $ from 'jquery';

import { connect } from 'react-redux';

export class UtilityBar extends ReactComponent {

  componentDidMount() {
    this.attachAccountPopover();
  }

  componentDidUpdate() {
    this.attachAccountPopover();
  }

  attachAccountPopover() {
    const element = ReactDOM.findDOMNode(this.refs.accountLink);
    $(element).accountPopover({ logoutlink: window.SSO.LOGOUT_URL });
  }

  signInLink() {
    return (<a href={window.SSO.LOGIN_URL} key="signInLink">
      Sign in
    </a>);
  }

  accountLink(user) {
    return (
      <a
        key="accountLink"
        ref="accountLink"
        href="//warwick.ac.uk/myaccount"
        data-toggle="id7:account-popover"
        data-name={user.name}
      >
        {user.name}
        <span className="caret"></span>
      </a>
    );
  }

  render() {
    const data = this.props.data || {};
    return (
      <ul>
        {!this.props.empty ?
          <li>{ data.authenticated ? this.accountLink(data) : this.signInLink() }</li>
          : null}
      </ul>
    );
  }

}

/**
 * user.empty is true before we've loaded anything
 * user.data contains most of the info about the user.
 * user.authoritative is true if we've had a response from the server.
 */
const select = (state) => state.getIn(['user']).toObject();

export default connect(select)(UtilityBar);
