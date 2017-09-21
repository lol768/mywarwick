import React from 'react';
import * as PropTypes from 'prop-types';
import * as log from 'loglevel';
import _ from 'lodash-es';
import * as errorreporter from '../../errorreporter';

export const TILE_SIZES = {
  SMALL: 'small',
  WIDE: 'wide',
  LARGE: 'large',
  TALL: 'tall',
};

export const DEFAULT_TILE_SIZES = [TILE_SIZES.SMALL, TILE_SIZES.WIDE];

export default class TileContent extends React.PureComponent {
  static propTypes = {
    content: PropTypes.object,
    size: PropTypes.oneOf(_.values(TILE_SIZES)).isRequired,
    zoomed: PropTypes.bool,
    preferences: PropTypes.object,
  };

  static supportedTileSizes() {
    return DEFAULT_TILE_SIZES;
  }

  static needsContentToRender() {
    return true;
  }

  static isRemovable() {
    return true;
  }

  static expandsOnClick() {
    return false;
  }

  static overridesOnClick() {
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

  isEmpty() {
    const { content } = this.props;
    return !content.items || content.items.length === 0;
  }

  canZoom() {
    return this._canZoom;
  }

  _canZoom = false;

  contentOrDefault(contentFunction) {
    if (this.isEmpty()) {
      return <span>{ this.props.content.defaultText || 'Nothing to show.' }</span>;
    }

    return contentFunction.call(this);
  }

  isError() {
    return this.error;
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
}
