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
    $(element).accountPopover();
  }

  render() {
    let signInLink = (
      <a href={window.SSO.LOGIN_URL}>
        Sign in
      </a>
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

let select = (state) => state.get('user').toJS();

export default connect(select)(UtilityBar);