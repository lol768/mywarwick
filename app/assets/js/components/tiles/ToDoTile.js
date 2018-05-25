/* eslint-env browser */
import React from 'react';
import ListTile, { ListTileItem } from './ListTile';
import * as PropTypes from 'prop-types';
import { formatDate } from '../../dateFormats';
import { TILE_SIZES } from './TileContent';

export default class ToDoTile extends ListTile {
  listItem(props) {
    return (<ToDoItem {...props} />);
  }

  getNumberOfItemsToDisplay() {
    switch (this.props.size) {
      case TILE_SIZES.SMALL:
        return 3;
      case TILE_SIZES.WIDE:
        return 3;
      case TILE_SIZES.LARGE:
        return 7;
      case TILE_SIZES.TALL:
        return 15;
      default:
        return 200;
    }
  }
}

class ToDoItem extends ListTileItem {
  static propTypes = {
    id: PropTypes.string.isRequired,
    subject: PropTypes.string.isRequired,
    dueDateTime: PropTypes.string,
    completedDateTime: PropTypes.string,
    createdDateTime: PropTypes.string,
    reminderDateTime: PropTypes.string,
    completed: PropTypes.bool,
    handleOnClick: PropTypes.func,
    href: PropTypes.string,
  };

  render() {
    const clickProps = (this.props.handleOnClick) ? {
      onClick: this.onClick,
      onKeyUp: this.onClick,
      role: 'button',
      tabIndex: 0,
    } : {};
    return (
      <li className="tile-list-item--with-separator">
        <a href={this.props.href} target="_blank" {...clickProps}>
          {this.props.title && <span className="list-group-item__title">{this.props.subject}</span>}
          {
            this.props.dueDateTime &&
            <span className="list-group-item__date">{formatDate(this.props.dueDateTime)}</span>
          }
          <span className="list-group-item__text">
            {this.props.subject}
          </span>
        </a>
      </li>
    );
  }
}
