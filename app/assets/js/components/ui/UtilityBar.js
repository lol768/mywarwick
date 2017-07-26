import React from 'react';
import * as PropTypes from 'prop-types';
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

  renderSignInLink() {
    return (
      <a href={this.props.user.links.login} key="signInLink" className="sign-in-link">
        Sign in
      </a>
    );
  }

  renderAccountPhoto() {
    const user = this.props.user.data || {};

    return (
        <AccountPhoto user={ user } className="img-circle" />
    );
  }

  render() {
    const user = this.props.user || {};
    const userData = user.data || {};

    return (
      <ul>
        {!user.empty ?
          <li>{ userData.authenticated ? this.renderAccountPhoto() : this.renderSignInLink() }</li>
          : null}
      </ul>
    );
  }
}
