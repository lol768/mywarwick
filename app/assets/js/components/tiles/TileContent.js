/* eslint react/prop-types: 0, react/sort-comp: 0 */
import React, { Component } from 'react';
export const TILE_SIZES = {
  SMALL: 'small',
  WIDE: 'wide',
  LARGE: 'large',
};

export default class TileContent extends Component {

  // when do we consider the tile to have no valid content
  isEmpty(content) {
    return !content.items || content.items.length === 0;
  }

  contentOrDefault(content, fetchedAt, contentFunction) {
    const defaultText = (content.defaultText === undefined) ?
      'Nothing to show.' : content.defaultText;
    if (this.isEmpty(content)) {
      return <span>{defaultText}</span>;
    }
    return contentFunction.call(this, content, fetchedAt);
  }

  getBodyInternal(content, fetchedAt) {
    return this.contentOrDefault(content, fetchedAt, this.getBody);
  }

  // when overriding arguments are - content, fetchedAt
  getBody(content) {
    switch (this.props.size.toLowerCase()) {
      case TILE_SIZES.LARGE:
        return this.getLargeBody(content);
      case TILE_SIZES.WIDE:
        return this.getWideBody(content);
      case TILE_SIZES.SMALL:
        return this.getSmallBody(content);
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

  getZoomedBodyInternal(content, fetchedAt) {
    return this.contentOrDefault(content, fetchedAt, this.getZoomedBody);
  }

  getZoomedBody(content, fetchedAt) {
    return this.getLargeBody(content, fetchedAt);
  }

  render() {
    const { content, zoomed, fetchedAt } = this.props;
    if (content) {
      return zoomed ?
        this.getZoomedBodyInternal(content, fetchedAt) : this.getBodyInternal(content, fetchedAt);
    }
    return null;
  }

}
