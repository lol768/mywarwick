import React, { PropTypes } from 'react';
import TileContent, { DEFAULT_TILE_SIZES, TILE_SIZES } from './TileContent';
import { formatDateTime } from '../../dateFormats';
import _ from 'lodash-es';

export default class ListTile extends TileContent {

  static canZoom() {
    return true;
  }

  static supportedTileSizes() {
    return DEFAULT_TILE_SIZES.concat([TILE_SIZES.LARGE, TILE_SIZES.TALL]);
  }

  getSmallBody() {
    const { content } = this.props;

    // only show the first maxItemsToDisplay items (defaults to 3) if not zoomed
    const maxItemsToDisplay = this.props.maxItemsToDisplay ? this.props.maxItemsToDisplay : 3;
    const itemsToDisplay = this.props.zoomed ?
      content.items : _.take(content.items, maxItemsToDisplay);
    return (<ul className="list-unstyled tile-list-group">
      {itemsToDisplay.map(item =>
        <ListTileItem key={item.id} onClickLink={this.onClickLink} {...item} />
      )}
    </ul>);
  }

}

export const ListTileItem = (props) => (
  <li className="tile-list-item--with-separator">
    <a href={props.href} target="_blank" onClick={ props.onClickLink }>
      { props.title && <span className="list-group-item__title">{props.title}</span> }
      { props.date && <span className="list-group-item__date">{formatDateTime(props.date)}</span> }
      <span className="list-group-item__text">{props.text}</span>
    </a>
  </li>
);

ListTileItem.propTypes = {
  date: PropTypes.string,
  href: PropTypes.string,
  text: PropTypes.string,
  title: PropTypes.string,
  onClickLink: PropTypes.func,
};
