"use strict";

/***
 * All the React UI components.
 *
 * TODO break into files under a components folder.
 * Can still aggregate them here.
 */

var React = require('react/addons');
var moment = require('moment');

export var ActivityStreamTile = require('./components/activitystreamtile');

export class TileApp extends React.Component {

  render() {
    return <div className="tiles row">
      {this.props.children}
    </div>;
  }
}