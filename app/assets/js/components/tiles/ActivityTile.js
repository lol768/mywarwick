import React from 'react';
import TileContent, { DEFAULT_TILE_SIZES, TILE_SIZES } from './TileContent';
import ActivityView from '../views/ActivityView';

export default class ActivityTile extends TileContent {

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
    return <ActivityView />;
  }

}

ActivityTile.propTypes = {

};
