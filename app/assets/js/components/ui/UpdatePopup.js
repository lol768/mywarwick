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
        <div className="update-popup">
          <div className="media">
            <div className="media-body">
              A new version of My Warwick is available.
            </div>
            <div className="media-right">
              <a href="#update-refresh" className="btn btn-brand" onClick={UpdatePopup.reload}>Update</a>
            </div>
          </div>
        </div>
      );
    }

    return null;
  }
}

export default connect(state => state.update)(UpdatePopup);
