import React from 'react';
import ReactDOM from 'react-dom';
import ReactComponent from 'react/lib/ReactComponent';
import $ from 'jquery';

import { connect } from 'react-redux';

class UtilityBar extends ReactComponent {

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

  render() {
    const signInLink = (
      <a href={window.SSO.LOGIN_URL} key="signInLink">
        Sign in
      </a>
    );
    const accountLink = (
      <a
        key="accountLink"
        ref="accountLink"
        href="//warwick.ac.uk/myaccount"
        data-toggle="id7:account-popover"
        data-name={this.props.name}
      >
        {this.props.name}
        <span className="caret"></span>
      </a>
    );

    return (
      <ul>
        <li>
          { this.props.authenticated ? accountLink : signInLink }
        </li>
      </ul>
    );
  }

}

const select = (state) => state.getIn(['user', 'data']).toJS();

export default connect(select)(UtilityBar);
