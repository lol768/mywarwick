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
      <a href={this.props.user.links.login} key="signInLink" className="sign-in-link">
        Sign in
      </a>
    );
  }

  onPhotoError() {
    const photo = ReactDOM.findDOMNode(this.refs.photo);

    photo.src = '/assets/images/no-photo.png';
  }

  renderPhoto(user) {
    return (
      <img src={ user.photo.url }
        ref="photo"
        className="img-circle"
        alt={ user.name }
        onError={ this.onPhotoError }
      />
    );
  }

  accountLink() {
    const isMobile = this.props.layoutClassName === 'mobile';

    const ssoLinks = this.props.user.links;
    const user = this.props.user.data || {};
    const link = isMobile ? this.renderPhoto(user) : user.name;

    return (
      <a
        key="accountLink"
        ref="accountLink"
        className="account-link"
        href="//warwick.ac.uk/myaccount"
        data-toggle="id7:account-popover"
        data-loginlink={ssoLinks.login}
        data-logoutlink={ssoLinks.logout}
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
          <li>{ userData.authenticated ? this.accountLink() : this.signInLink() }</li>
          : null}
      </ul>
    );
  }
}
