import React, { PropTypes } from 'react';
import TileContent from './TileContent';

import formatDate from '../../dateFormatter';
import _ from 'lodash';

export default class ListTile extends TileContent {

  getLargeBody() {
    const { content } = this.props;

    // only show the first maxItemsToDisplay items (defaults to 3) if not zoomed
    const maxItemsToDisplay = this.props.maxItemsToDisplay ? this.props.maxItemsToDisplay : 3;
    const itemsToDisplay = this.props.zoomed ?
      content.items : _.take(content.items, maxItemsToDisplay);
    return (<ul className="list-unstyled">
      {itemsToDisplay.map(item =>
        <ListTileItem key={item.id} onClickLink={this.onClickLink} {...item} />
      )}
    </ul>);
  }

}

export const ListTileItem = (props) => (
  <li className="tile-list-item tile-list-item--with-separator">
    <a href={props.href} target="_blank" onClick={ props.onClickLink }>
      { props.title ? <span className="tile-list-item__title">{props.title}</span> : null }
      { props.date ? <span className="tile-list-item__date">{formatDate(props.date)}</span> : null }
      <span className="tile-list-item__text">{props.text}</span>
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
