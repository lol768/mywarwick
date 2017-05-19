import React from 'react';
import * as log from 'loglevel';
import * as errorreporter from '../../errorreporter';
import _ from 'lodash-es';

export const TILE_SIZES = {
  SMALL: 'small',
  WIDE: 'wide',
  LARGE: 'large',
  TALL: 'tall',
};

export const DEFAULT_TILE_SIZES = [TILE_SIZES.SMALL, TILE_SIZES.WIDE];

export default class TileContent extends React.PureComponent {

  static isVisibleOnDesktopOnly() {
    return false;
  }

  static canZoom() {
    return false;
  }

  constructor(props) {
    super(props);
    this.error = false;
  }

  static supportedTileSizes() {
    return DEFAULT_TILE_SIZES;
  }

  getBody() {
    if (this.props.zoomed) {
      return this.getZoomedBody();
    }

    if (this.constructor.supportedTileSizes(this.props.content).indexOf(this.props.size) === -1) {
      return this.getSmallBody();
    }

    switch (this.props.size) {
      case TILE_SIZES.LARGE:
        return this.getLargeBody();
      case TILE_SIZES.TALL:
        return this.getZoomedBody();
      case TILE_SIZES.WIDE:
        return this.getWideBody();
      case TILE_SIZES.SMALL:
      default:
        return this.getSmallBody();
    }
  }

  getSmallBody() {
    throw new TypeError('Must implement getSmallBody');
  }

  getWideBody() {
    return this.getSmallBody();
  }

  getLargeBody() {
    return this.getWideBody();
  }

  getZoomedBody() {
    return this.getLargeBody();
  }

  getIcon() {
    return null;
  }

  contentOrDefault(contentFunction) {
    if (this.isEmpty()) {
      return <span>{ this.props.content.defaultText || 'Nothing to show.' }</span>;
    }

    return contentFunction.call(this);
  }

  isError() {
    return this.error;
  }

  isEmpty() {
    const { content } = this.props;
    return !content.items || content.items.length === 0;
  }

  isRemovable() {
    return true;
  }

  static needsContentToRender() {
    return true;
  }

  expandsOnClick() {
    return false;
  }

  render() {
    if (!this.isError()) {
      try {
        if (this.props.content || !this.constructor.needsContentToRender()) {
          return this.contentOrDefault(this.getBody);
        }
      } catch (e) {
        log.error('Error rendering tile', e);
        errorreporter.post(e);
        this.error = true;
      }
    }
    if (this.isError()) {
      return <span>Unexpected error displaying this tile.</span>;
    }
    return null;
  }

  static propTypes = {
    content: React.PropTypes.object,
    size: React.PropTypes.oneOf(_.values(TILE_SIZES)).isRequired,
    zoomed: React.PropTypes.bool,
  }

}
