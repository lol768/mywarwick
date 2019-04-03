/* eslint-env browser */
import React from 'react';
import * as PropTypes from 'prop-types';
import { connect } from 'react-redux';

class UpdatePopup extends React.PureComponent {
  static propTypes = {
    isUpdateReady: PropTypes.bool,
  };

  static reload(e) {
    e.preventDefault();
    window.location.reload();
  }

  render() {
    if (this.props.isUpdateReady) {
      return (
        <div className="alert alert-success update-popup" style={{ marginBottom: 15 }}>
          <div className="media">
            <div className="media-left">
              <i
                className="app-icon app-icon--lg fal fa-fw fa-check"
                style={{ backgroundColor: '#8c6e96', color: '#fff' }}
              />
            </div>
            <div className="media-body" style={{ paddingTop: 2 }}>
              A new version of My Warwick is available. Just&nbsp;
              <a href="#update-refresh" onClick={UpdatePopup.reload}>refresh the page</a>
              &nbsp;to upgrade.
            </div>
          </div>
        </div>
      );
    }

    return null;
  }
}

export default connect(state => state.update)(UpdatePopup);
