import React, { PropTypes } from 'react';
import ReactDOM from 'react-dom';
import $ from 'jquery';

export default class UtilityBar extends React.Component {

  static propTypes = {
    user: PropTypes.shape({
      links: PropTypes.shape({
        login: PropTypes.string,
      }),
      data: PropTypes.shape({
        authenticated: PropTypes.bool,
      }),
    }),
    layoutClassName: PropTypes.oneOf(['desktop', 'mobile']),
  };

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

  onPhotoError() {
    const photo = ReactDOM.findDOMNode(this.refs.photo);

    photo.src = '/assets/images/no-photo.png';
  }

  attachAccountPopover() {
    const $element = $(ReactDOM.findDOMNode(this.refs.accountLink));

    if ($element.data('id7.account-popover') === undefined) {
      $element.accountPopover();
    }
  }

  renderSignInLink() {
    return (
      <a href={this.props.user.links.login} key="signInLink" className="sign-in-link">
        Sign in
      </a>
    );
  }

  renderAccountLink() {
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

  render() {
    const user = this.props.user || {};
    const userData = user.data || {};

    return (
      <ul>
        {!user.empty ?
          <li>{ userData.authenticated ? this.renderAccountLink() : this.renderSignInLink() }</li>
          : null}
      </ul>
    );
  }
}
