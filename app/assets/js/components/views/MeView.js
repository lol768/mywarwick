import React from 'react';
import ReactComponent from 'react/lib/ReactComponent';
import { connect } from 'react-redux';
import { makeStream, takeFromStream } from '../../stream';
import { fetchTileData } from '../../tiles'
import * as tileElements from '../tiles';

import moment from 'moment';

export default class MeView extends ReactComponent {

  constructor(props) {
    fetchTileData();
    super(props);
  }

  render() {

    let key = "3738383";
    console.log(this.props.tiles);
    let tiles = this.props.tiles.map((tile) => React.createElement(tileElements[tile.type], tile));

    return <div key={key} className="row">{tiles}</div>;
  }

}

function select(state) {
  return {
    tiles: state.get('tiles').toJS()
  };
}

export default connect(select)(MeView);
