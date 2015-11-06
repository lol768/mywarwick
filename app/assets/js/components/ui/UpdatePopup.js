import React from 'react';
import ReactComponent from 'react/lib/ReactComponent';

import ProgressBar from './ProgressBar';
import AppIcon from './AppIcon';

import { connect } from 'react-redux';

class UpdatePopup extends ReactComponent {

  render() {
    if (!this.props.isUpdating) {
      return <div />;
    }

    if (this.props.loaded < this.props.total || this.props.loaded == 0) {
      return (
        <div className="activity-item" style={{marginBottom: 15}}>
          <div className="media">
            <div className="media-left">
              <AppIcon app="Update" size="lg"/>
            </div>
            <div className="media-body" style={{lineHeight: 2}}>
              An update to Start.Warwick is being downloaded.
              <ProgressBar value={this.props.loaded} max={this.props.total}/>
            </div>
          </div>
        </div>
      );
    } else {
      return (
        <div className="activity-item" style={{marginBottom: 15}}>
          <div className="media">
            <div className="media-left">
              <AppIcon app="Update" size="lg"/>
            </div>
            <div className="media-body" style={{lineHeight: 2}}>
              An update to Start.Warwick is ready to be installed.
            </div>
            <div className="media-right">
              <button className="btn btn-default" onClick={this.reload}>Install now</button>
            </div>
          </div>
        </div>
      );
    }
  }

  reload() {
    window.location.reload();
  }

}

export default connect((state) => state.get('update').toJS())(UpdatePopup);
