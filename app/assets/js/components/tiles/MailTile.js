/* eslint-env browser */
import React from 'react';
import ListTile from './ListTile';

export default class MailTile extends ListTile {
  constructor(props) {
    super(props);
    this.modalMoreButtonOnClick = this.modalMoreButtonOnClick.bind(this);
  }

  modalMoreButtonOnClick() {
    const preferences = this.props.preferences || {};
    const externalapp = preferences.externalapp || 'webmail';
    if (externalapp !== 'webmail' &&
      'MyWarwickNative' in window &&
      'openMailApp' in window.MyWarwickNative
    ) {
      window.MyWarwickNative.openMailApp(externalapp);
    } else if (window.navigator.userAgent.indexOf('MyWarwick/') >= 0) {
      window.location = 'http://webmail.warwick.ac.uk/';
    } else {
      window.open('http://webmail.warwick.ac.uk/');
    }
  }

  modalMoreButton() {
    return (
      <button
        type="button"
        className="btn btn-default"
        onClick={this.modalMoreButtonOnClick}
      >
        Open inbox
      </button>
    );
  }
}
