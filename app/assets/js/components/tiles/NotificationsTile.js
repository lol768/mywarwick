import React from 'react';
import TileContent from './TileContent';
import NotificationsView from '../views/NotificationsView';

export default class NotificationsTile extends TileContent {

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
    return <NotificationsView />;
  }

}

NotificationsTile.propTypes = {

};
