"use strict";

/***
 * Top level app component.
 */

import React from 'react/addons';
import moment from 'moment';
import log from 'loglevel';
import localforage from 'localforage';

export default class TileApp extends React.Component {

  render() {
    return <div>
      <div className="tiles row">
        {this.props.children}
      </div>
      <div>
        <a href="#" onClick={this.resetLocalData}>Reset local data</a>
      </div>
    </div>;
  }

  resetLocalData() {
    log.info("Resetting local data");
    localforage.clear();
  }

}