import React from 'react';
import TileContent from './TileContent';
import ActivityView from '../views/ActivityView';

export default class ActivityTile extends TileContent {

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
    return <ActivityView />;
  }

}

ActivityTile.propTypes = {

};
