import React from 'react';

import Tile from './Tile';

export default class CountView extends Tile {

  getContent() {
    return (
      <div className="tile__item">
        <span className="tile__callout">{this.props.content.count || (this.props.content.items.length)}</span>
        <span className="tile__text">{this.props.content.word}</span>
      </div>
    );
  }

  getZoomedContent() {
    return (
      <ul>
        {this.props.content.items.map((item) => <ListTileItem {...item} />)}
      </ul>
    );
  }

}
