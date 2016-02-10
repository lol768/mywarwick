import React from 'react';
import ReactComponent from 'react/lib/ReactComponent';

import ProgressBar from './ProgressBar';

import { connect } from 'react-redux';

class UpdatePopup extends ReactComponent {

  render() {
    if (!this.props.isUpdating) {
      return <div></div>;
    }

    if (this.props.loaded < this.props.total || this.props.loaded === 0) {
      return (
        <div className="alert alert-info" style={{ marginBottom: 15 }}>
          <div className="media">
            <div className="media-left">
              <i className="app-icon app-icon--lg fa fa-fw fa-arrow-up"
                style={{ backgroundColor: '#8c6e96', color: '#fff' }}
              > </i>
            </div>
            <div className="media-body" style={{ paddingTop: 2 }}>
              Start.Warwick is downloading an update.
              <ProgressBar value={ this.props.loaded } max={ this.props.total }/>
            </div>
          </div>
        </div>
      );
    }

    return (
      <div className="alert alert-success" style={{ marginBottom: 15 }}>
        <div className="media">
          <div className="media-left">
            <i className="app-icon app-icon--lg fa fa-fw fa-check"
              style={{ backgroundColor: '#8c6e96', color: '#fff' }}
            > </i>
          </div>
          <div className="media-body" style={{ paddingTop: 2 }}>
            A new version of Start.Warwick is available. Just&nbsp;
            <a href="#" onClick={this.reload}>refresh the page</a>
            &nbsp;to upgrade.
          </div>
        </div>
      </div>
    );
  }

  reload(e) {
    e.preventDefault();
    window.location.reload();
  }

}

export default connect((state) => state.get('update').toJS())(UpdatePopup);
