/* eslint react/prop-types: 0, react/sort-comp: 0 */
import React, { Component } from 'react';
export const TILE_SIZES = {
  SMALL: 'small',
  WIDE: 'wide',
  LARGE: 'large',
  TALL: 'tall',
};

export default class TileContent extends Component {

  isEmpty() {
    const { content } = this.props;

    return !content.items || content.items.length === 0;
  }

  isRemovable() {
    return true;
  }

  static isVisibleOnDesktopOnly() {
    return false;
  }

  contentOrDefault(contentFunction) {
    if (this.isEmpty()) {
      return <span>{ this.props.content.defaultText || 'Nothing to show.' }</span>;
    }

    return contentFunction.call(this);
  }

  getBody() {
    switch (this.props.size.toLowerCase()) {
      case TILE_SIZES.LARGE:
      case TILE_SIZES.TALL:
        return this.getLargeBody();
      case TILE_SIZES.WIDE:
        return this.getWideBody();
      case TILE_SIZES.SMALL:
        return this.getSmallBody();
      default:
        throw new ReferenceError('Tile props.size is not one of [ large, wide, small ]');
    }
  }

  getLargeBody() {
    throw new TypeError('Must implement getLargeBody');
  }

  getWideBody() {
    return this.getLargeBody();
  }

  getSmallBody() {
    return this.getLargeBody();
  }

  static canZoom() {
    return false;
  }

  getZoomedBody() {
    return this.getLargeBody();
  }

  getIcon() {
    return null;
  }

  render() {
    const { content, zoomed } = this.props;
    if (content) {
      return this.contentOrDefault(zoomed ? this.getZoomedBody : this.getBody);
    }
    return null;
  }

}
