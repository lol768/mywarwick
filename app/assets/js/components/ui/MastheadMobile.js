import React from 'react';
import UtilityBar from './UtilityBar';
import store from '../../store';
import { zoomOut } from '../views/MeView';

export default class MastheadMobile extends React.Component {
  constructor(props) {
    super(props);
  }

  tileZoomOut() {
    store.dispatch(zoomOut());
  }

  render() {
    return (
      <div className="start-masthead">
        {this.props.zoomedTile ?
          <div className="backBtn" onClick={this.tileZoomOut}>
            <i className="fa fa-chevron-left"></i>
            Back
          </div>
          : null }
        <div className="masthead-title">
          <span className="light">
          START.
          </span>
          WARWICK
        </div>
        <nav className="id7-utility-bar">
          <UtilityBar {...this.props} />
        </nav>
      </div>
    );
  }
}

MastheadMobile.propTypes = {
  zoomedTile: React.PropTypes.string,
};
