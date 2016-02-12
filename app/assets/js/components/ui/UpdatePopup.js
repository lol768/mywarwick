import React from 'react';
import ReactComponent from 'react/lib/ReactComponent';

import { connect } from 'react-redux';

class UpdatePopup extends ReactComponent {

  render() {
    if (this.props.isUpdateReady) {
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

    return <div></div>;
  }

  reload(e) {
    e.preventDefault();
    window.location.reload();
  }

}

export default connect((state) => state.get('update').toJS())(UpdatePopup);
