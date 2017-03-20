import React from 'react';
import TileContent from './TileContent';
import NewsView from '../views/NewsView';

export default class NewsTile extends TileContent {

  static canZoom() {
    return true;
  }

  static isVisibleOnDesktopOnly() {
    return true;
  }

  needsContentToRender() {
    return false;
  }

  isEmpty() {
    return false;
  }

  isRemovable() {
    return false;
  }

  getLargeBody() {
    return <NewsView inTile />;
  }

}

NewsTile.propTypes = {

};
