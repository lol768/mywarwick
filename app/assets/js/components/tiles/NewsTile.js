import React from 'react';
import TileContent from './TileContent';
import NewsView from '../views/NewsView';

export default class NewsTile extends TileContent {

  static canZoom() {
    return true;
  }

  isVisibleAtLayoutWidth(width) {
    return width === 4;
  }

  isEmpty() {
    return false;
  }

  isRemovable() {
    return false;
  }

  getLargeBody() {
    return <NewsView />;
  }

}

NewsTile.propTypes = {

};
