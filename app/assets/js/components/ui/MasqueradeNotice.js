import React, { Component } from 'react';

export default class MasqueradeNotice extends Component {

  render() {
    return (
      <div className="masquerade-notice">
        Masquerading as <strong>{this.props.masqueradingAs.name}</strong> ({this.props.masqueradingAs.usercode}). <a href="/sysadmin/masquerade">Change</a>
      </div>
    );
  }

}
