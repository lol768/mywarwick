import React from 'react';
import Tile from './Tile';

import formatDate from '../../dateFormatter';

export default class ListTile extends Tile {

  getContent() {
    // only show the first maxItemsToDisplay items (defaults to 3) if not zoomed
    let maxItemsToDisplay = this.props.maxItemsToDisplay ? this.props.maxItemsToDisplay : 3;
    let itemsToDisplay = this.props.zoomed ? this.props.content.items : this.props.content.items.slice(0, maxItemsToDisplay);
    return <ul>
      {itemsToDisplay.map((item) => <ListTileItem {...item} />)}
    </ul>;
  }

}

let ListTileItem = (props) => (
  <li className="list-tile-item">
    <a href={props.href} target="_blank" onClick={function(e){e.stopPropagation();}}>
      <span className="list-tile-item__title">{props.title}</span>
      { props.date ? <span className="list-tile-item__date">{formatDate(props.date)}</span> : null }
      <span className="list-tile-item__text">{props.text}</span>
    </a>
  </li>
);
