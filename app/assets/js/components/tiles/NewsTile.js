import React from 'react';
import TileContent, { DEFAULT_TILE_SIZES, TILE_SIZES } from './TileContent';
import NewsView from '../views/NewsView';

export default class NewsTile extends TileContent {

  static canZoom() {
    return true;
  }

  static isVisibleOnDesktopOnly() {
    return true;
  }

  static needsContentToRender() {
    return false;
  }

  isEmpty() {
    return false;
  }

  isRemovable() {
    return false;
  }

  static supportedTileSizes() {
    return DEFAULT_TILE_SIZES.concat([TILE_SIZES.LARGE, TILE_SIZES.TALL]);
  }

  getSmallBody() {
    return <NewsView />;
  }

}

NewsTile.propTypes = {

};
