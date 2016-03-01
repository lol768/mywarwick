import React from 'react';
import ReactDOM from 'react-dom';
import ReactComponent from 'react/lib/ReactComponent';
import $ from 'jquery';

export default class UtilityBar extends ReactComponent {

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
    const isMobile = this.props.layoutClassName === 'mobile';
    const noPhoto = '/assets/images/no-photo.png';
    const link = isMobile ?
      `<img src="/photo" class="img-circle" alt=${user.name} onError="this.onerror=null;this.src=
      '${noPhoto}';"/>`
      : user.name;

    return (
      <a
        key="accountLink"
        ref="accountLink"
        href="//warwick.ac.uk/myaccount"
        data-toggle="id7:account-popover"
        data-name={link}
      >
        {link}
        <span className="caret"></span>
      </a>
    );
  }

  render() {
    const user = this.props.user || {};
    const userData = user.data || {};

    return (
      <ul>
        {!user.empty ?
          <li>{ userData.authenticated ? this.accountLink(userData) : this.signInLink() }</li>
          : null}
      </ul>
    );
  }
}
