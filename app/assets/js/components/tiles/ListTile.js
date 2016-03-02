import React, { PropTypes } from 'react';
import TileContent from './TileContent';

import formatDate from '../../dateFormatter';
import _ from 'lodash';

export default class ListTile extends TileContent {

  getLargeBody(content) {
    // only show the first maxItemsToDisplay items (defaults to 3) if not zoomed
    const maxItemsToDisplay = this.props.maxItemsToDisplay ? this.props.maxItemsToDisplay : 3;
    const itemsToDisplay = this.props.zoomed ?
      content.items : _.take(content.items, maxItemsToDisplay);
    return (<ul>
      {itemsToDisplay.map(item => <ListTileItem key={item.id} {...item} />)}
    </ul>);
  }

}

export const ListTileItem = (props) => (
  <li className="list-tile-item">
    <a href={props.href} target="_blank" onClick={ ListTileItem.onclick }>
      <span className="list-tile-item__title">{props.title}</span>
      { props.date ? <span className="list-tile-item__date">{formatDate(props.date)}</span> : null }
      <span className="list-tile-item__text">{props.text}</span>
    </a>
  </li>
);

ListTileItem.onclick = e => e.stopPropagation();

ListTileItem.propTypes = {
  date: PropTypes.string,
  href: PropTypes.string,
  text: PropTypes.string,
  title: PropTypes.string,
};
