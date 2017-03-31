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

export default class TileContent extends React.Component {

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

  getBody() {
    if (this.props.zoomed) {
      return this.getZoomedBody();
    }

    switch (this.props.size) {
      case TILE_SIZES.TALL:
        return this.getZoomedBody();
      case TILE_SIZES.WIDE:
        return this.getWideBody();
      case TILE_SIZES.SMALL:
        return this.getSmallBody();
      case TILE_SIZES.LARGE:
      default:
        return this.getLargeBody();
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

  needsContentToRender() {
    return true;
  }

  render() {
    if (!this.isError()) {
      try {
        if (this.props.content || !this.needsContentToRender()) {
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

}

TileContent.propTypes = {
  content: React.PropTypes.object,
  size: React.PropTypes.oneOf(_.values(TILE_SIZES)).isRequired,
  zoomed: React.PropTypes.bool,
};
