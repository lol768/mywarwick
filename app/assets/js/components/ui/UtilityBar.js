import React from 'react';
import ReactDOM from 'react-dom';
import ReactComponent from 'react/lib/ReactComponent';
import $ from 'jquery';

export default class UtilityBar extends ReactComponent {

  constructor(props) {
    super(props);

    this.onPhotoError = this.onPhotoError.bind(this);
  }

  componentDidMount() {
    this.attachAccountPopover();
  }

  componentDidUpdate() {
    this.attachAccountPopover();
  }

  attachAccountPopover() {
    const $element = $(ReactDOM.findDOMNode(this.refs.accountLink));

    if ($element.data('id7.account-popover') === undefined) {
      $element.accountPopover();
    }
  }

  signInLink() {
    return (
      <a href={window.SSO.LOGIN_URL} key="signInLink" className="sign-in-link">
        Sign in
      </a>
    );
  }

  onPhotoError() {
    const photo = ReactDOM.findDOMNode(this.refs.photo);

    photo.src = 'https://websignon.warwick.ac.uk/origin/static/images/no-photo.png';
  }

  renderPhoto(user) {
    return (
      <img src="/photo"
        ref="photo"
        className="img-circle"
        alt={ user.name }
        onError={ this.onPhotoError }
      />
    );
  }

  accountLink(user) {
    const isMobile = this.props.layoutClassName === 'mobile';

    const link = isMobile ? this.renderPhoto(user) : user.name;

    return (
      <a
        key="accountLink"
        ref="accountLink"
        className="account-link"
        href="//warwick.ac.uk/myaccount"
        data-toggle="id7:account-popover"
        data-logoutlink={window.SSO.LOGOUT_URL}
        data-loginlink={window.SSO.LOGIN_URL}
      >
        { link }
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
