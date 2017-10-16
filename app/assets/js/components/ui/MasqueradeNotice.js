import React from 'react';
import * as PropTypes from 'prop-types';

export default class MasqueradeNotice extends React.PureComponent {
  static propTypes = {
    masqueradingAs: PropTypes.shape({
      name: PropTypes.string.isRequired,
      usercode: PropTypes.string.isRequired,
    }).isRequired,
  };

  render() {
    return (
      <div className="top-page-notice masquerade">
        Masquerading as <strong>{this.props.masqueradingAs.name}</strong>
        ({this.props.masqueradingAs.usercode}). <a href="/admin/masquerade">Change</a>
      </div>
    );
  }
}

