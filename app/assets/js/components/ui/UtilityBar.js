import React from 'react';
import ReactComponent from 'react/lib/ReactComponent';

import { connect } from 'react-redux';

class UtilityBar extends ReactComponent {

  render() {
    let signInLink = (
      <a href={window.SSO.LOGIN_URL}>
        Sign in
      </a>
    );
    let accountLink = (
      <a href="//warwick.ac.uk/myaccount" data-toggle="id7:account-popover" data-name={this.props.name}>
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

let select = (state) => state.get('user').toJS();

export default connect(select)(UtilityBar);