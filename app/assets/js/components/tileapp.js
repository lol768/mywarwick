"use strict";

/***
 * All the React UI components.
 *
 * TODO break into files under a components folder.
 * Can still aggregate them here.
 */

const React = require('react/addons');
const moment = require('moment');

export default class TileApp extends React.Component {

  render() {
    return <div className="tiles row">
      {this.props.children}
    </div>;
  }
}