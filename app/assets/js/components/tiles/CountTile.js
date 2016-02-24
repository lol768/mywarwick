import React from 'react';

import TileContent from './TileContent';
import { ListTileItem } from './ListTile';

export default class CountView extends TileContent {

  static canZoom(content) {
    if (content && content.items) {
      return content.items.length > 1;
    }
    return false;
  }

  isEmpty(content) {
    return !content.count && (!content.items || content.items.length === 0);
  }

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
