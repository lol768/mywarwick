import React, { Component } from 'react';

export default class MasqueradeNotice extends Component {

  render() {
    return (
      <div className="top-page-notice">
        Masquerading as <strong>{this.props.masqueradingAs.name}</strong>
        ({this.props.masqueradingAs.usercode}). <a href="/admin/masquerade">Change</a>
      </div>
    );
  }

}

MasqueradeNotice.propTypes = {
  masqueradingAs: {
    name: React.PropTypes.string.isRequired,
    usercode: React.PropTypes.string.isRequired,
  },
};
