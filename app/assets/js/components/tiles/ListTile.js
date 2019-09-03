import _ from 'lodash-es';
import React from 'react';
import * as PropTypes from 'prop-types';
import TileContent, { DEFAULT_TILE_SIZES, TILE_SIZES } from './TileContent';
import { formatDateTime } from '../../dateFormats';
import DismissableInfoModal from '../ui/DismissableInfoModal';

export default class ListTile extends TileContent {
  static canZoom() {
    return true;
  }

  static supportedTileSizes() {
    return DEFAULT_TILE_SIZES.concat([TILE_SIZES.LARGE, TILE_SIZES.TALL]);
  }

  static needsContentToRender() {
    return true;
  }

  constructor(props) {
    super(props);
    this.showModal = this.showModal.bind(this);
    this.hideModal = this.hideModal.bind(this);
    this.onItemClick = this.onItemClick.bind(this);
    this.listItem = this.listItem.bind(this);
  }

  listItem(props) {
    return <ListTileItem {...props} />;
  }

  modalMoreButton() {
    return null;
  }

  hideModal() {
    this.props.showModal(null);
  }

  showModal(heading, subHeadings, body, href) {
    const modal = (<DismissableInfoModal
      heading={heading}
      subHeadings={subHeadings}
      onDismiss={this.hideModal}
      href={href}
      moreButton={this.modalMoreButton()}
      selectableText
    >
      {(body && body.length) ? _.map(_.filter(body, item => item.length > 0), (item, i) => (
        <p key={`body-${i}`}>{item}</p>
      )) : body}
    </DismissableInfoModal>);
    this.props.showModal(modal);
  }

  getNumberOfItemsToDisplay() {
    switch (this.props.size) {
      case TILE_SIZES.SMALL:
      case TILE_SIZES.WIDE:
        return 3;
      case TILE_SIZES.LARGE:
        return 4;
      case TILE_SIZES.TALL:
      default:
        return 8;
    }
  }

  onItemClick(itemProps) {
    this.showModal(itemProps.text, [itemProps.title], itemProps.body.split('\r\n'), itemProps.href);
  }

  getSmallBody() {
    const { content } = this.props;

    const itemsToDisplay = this.props.zoomed
      ? content.items : _.take(content.items, this.getNumberOfItemsToDisplay());
    return (
      <ul className="list-unstyled tile-list-group">
        {_.compact(itemsToDisplay).map((item) => {
          const clickProps = (item.body) ? { handleOnClick: this.onItemClick } : {};
          return this.listItem({
            key: item.id,
            size: this.props.size,
            ...clickProps,
            ...item,
          });
        })}
      </ul>
    );
  }
}

export class ListTileItem extends React.PureComponent {
  static propTypes = {
    date: PropTypes.string,
    href: PropTypes.string,
    text: PropTypes.string,
    title: PropTypes.string,
    body: PropTypes.string,
    handleOnClick: PropTypes.func,
  };

  constructor(props) {
    super(props);
    this.onClick = this.onClick.bind(this);
  }

  onClick(e) {
    e.preventDefault();
    this.props.handleOnClick(this.props);
  }

  render() {
    const clickProps = (this.props.handleOnClick) ? {
      onClick: this.onClick,
      onKeyUp: this.onClick,
      role: 'button',
      tabIndex: 0,
    } : {};
    return (
      <li className="tile-list-item--with-separator">
        <a href={this.props.href}
           target="_blank"
           rel="noopener noreferrer"
           {...clickProps}>
          { this.props.title && <span className="list-group-item__title">{this.props.title}</span> }
          { this.props.date && <span className="list-group-item__date">{formatDateTime(this.props.date)}</span> }
          <span className="list-group-item__text">
            {this.props.text}
            {this.props.body && <i className="fal fa-fw fa-info-circle" />}
          </span>
        </a>
      </li>
    );
  }
}
