import React from 'react';
import ReactDOM from 'react-dom';
import ReactComponent from 'react/lib/ReactComponent';

import { connect } from 'react-redux';

class UtilityBar extends ReactComponent {

  componentDidMount() {
    this.attachAccountPopover();
  }

  componentDidUpdate() {
    this.attachAccountPopover();
  }

  attachAccountPopover() {
    var element = ReactDOM.findDOMNode(this.refs.accountLink);
    $(element).accountPopover({logoutlink: window.SSO.LOGOUT_URL});
  }

  render() {
    // Wrap Sign in link in a <span> so React does not use the same <a> element
    // as for the account link.  This causes the account popover to be attached
    // to the sign in link if the user signs out.
    let signInLink = (
      <span>
        <a href={window.SSO.LOGIN_URL}>
          Sign in
        </a>
      </span>
    );
    let accountLink = (
      <a ref="accountLink" href="//warwick.ac.uk/myaccount" data-toggle="id7:account-popover" data-name={this.props.name}>
        {this.props.name}
        <span className="caret"></span>
      </a>
    );

    return (
      <ul>
        <li>
          {this.props.authenticated ? accountLink : signInLink}
        </li>
      </ul>
    );
  }

}

let select = (state) => state.getIn(['user', 'data']).toJS();

export default connect(select)(UtilityBar);