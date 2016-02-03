import React from 'react';

import Tile from './Tile';
import { ListTileItem } from './ListTile';

export default class CountView extends Tile {

  getBody(content) {
    return (
      <div className="tile__item">
        <span className="tile__callout">{content.count || content.items.length}</span>
        <span className="tile__text">{content.word}</span>
      </div>
    );
  }

  getZoomedBody(content) {
    return (
      <ul>
        {content.items.map(item => <ListTileItem key={item.id} {...item} />)}
      </ul>
    );
  }

}
