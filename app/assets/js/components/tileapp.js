"use strict";

/***
 * Top level app component.
 */

const React = require('react/addons');
const moment = require('moment');
const log = require('loglevel');
const localforage = require('localforage');

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