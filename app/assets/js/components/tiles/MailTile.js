/* eslint-env browser */
import React from 'react';
import ListTile from './ListTile';

const WEBMAIL_URL = 'http://webmail.warwick.ac.uk/';

export default class MailTile extends ListTile {
  constructor(props) {
    super(props);
    this.handleInboxLinkClick = this.handleInboxLinkClick.bind(this);
  }

  handleInboxLinkClick() {
    const preferences = this.props.preferences || {};
    const externalapp = preferences.externalapp || 'webmail';
    if (externalapp !== 'webmail' &&
      'MyWarwickNative' in window &&
      'openMailApp' in window.MyWarwickNative
    ) {
      window.MyWarwickNative.openMailApp(externalapp);
    } else if (window.navigator.userAgent.indexOf('MyWarwick/') >= 0) {
      window.location = WEBMAIL_URL;
    } else {
      window.open(WEBMAIL_URL);
    }
  }

  modalMoreButton() {
    return (
      <button
        type="button"
        className="btn btn-default"
        onClick={this.handleInboxLinkClick}
      >
        Open inbox
      </button>
    );
  }

  contentOrDefault(contentFunction) {
    if (this.isEmpty()) {
      return (
        <div>
          <p>{this.props.content.defaultText}</p>
          <a
            role="button"
            className="text--dotted-underline"
            onClick={this.handleInboxLinkClick}
            tabIndex={0}
          >
            Open inbox
          </a>
        </div>
      );
    }

    return contentFunction.call(this);
  }
}
