import React from 'react';
import Tile from './Tile';

import formatDate from '../../dateFormatter';
import _ from 'lodash';

export default class ListTile extends Tile {

  getBody(content) {
    // only show the first maxItemsToDisplay items (defaults to 3) if not zoomed
    let maxItemsToDisplay = this.props.maxItemsToDisplay ? this.props.maxItemsToDisplay : 3;
    let itemsToDisplay = this.isZoomed() ? content.items : _.take(content.items, maxItemsToDisplay);
    return <ul>
      {itemsToDisplay.map(item => <ListTileItem key={item.id} {...item} />)}
    </ul>;
  }

}

let ListTileItem = (props) => (
  <li className="list-tile-item">
    <a href={props.href} target="_blank" onClick={e => e.stopPropagation()}>
      <span className="list-tile-item__title">{props.title}</span>
      { props.date ? <span className="list-tile-item__date">{formatDate(props.date)}</span> : null }
      <span className="list-tile-item__text">{props.text}</span>
    </a>
  </li>
);
