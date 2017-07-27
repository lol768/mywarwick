import React from 'react';
import * as PropTypes from 'prop-types';

export default class MasqueradeNotice extends React.PureComponent {
  render() {
    return (
      <div className="top-page-notice masquerade">
        Masquerading as <strong>{this.props.masqueradingAs.name}</strong>
        ({this.props.masqueradingAs.usercode}). <a href="/admin/masquerade">Change</a>
      </div>
    );
  }
}

MasqueradeNotice.propTypes = {
  masqueradingAs: {
    name: PropTypes.string.isRequired,
    usercode: PropTypes.string.isRequired,
  }.isRequired,
};
