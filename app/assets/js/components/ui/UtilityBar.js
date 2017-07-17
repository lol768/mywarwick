import React, { PropTypes } from 'react';
import ReactDOM from 'react-dom';
import $ from 'jquery';
import log from 'loglevel';
import AccountPhoto from './AccountPhoto';

export default class UtilityBar extends React.PureComponent {

  static propTypes = {
    user: PropTypes.shape({
      links: PropTypes.shape({
        login: PropTypes.string,
      }),
      data: PropTypes.shape({
        authenticated: PropTypes.bool,
      }),
    }),
  };

  componentDidMount() {
    this.attachAccountPopover();
  }

  componentDidUpdate() {
    this.attachAccountPopover();
  }

  attachAccountPopover() {
    const $element = $(ReactDOM.findDOMNode(this.refs.accountLink));

    if ($element.data('id7.account-popover') === undefined) {
      try {
        $element.accountPopover();
      } catch (e) {
        log.warn('accountPopover plugin failed', e);
      }
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
    const ssoLinks = this.props.user.links;
    const user = this.props.user.data || {};

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
        <AccountPhoto user={ user } className="img-circle" />
        <span className="caret" />
      </a>
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
