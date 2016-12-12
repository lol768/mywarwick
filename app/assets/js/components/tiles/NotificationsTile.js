import React from 'react';
import TileContent from './TileContent';
import NotificationsView from '../views/NotificationsView';

export default class NotificationsTile extends TileContent {

  static canZoom() {
    return true;
  }

  static isVisibleAtLayoutWidth(width) {
    return width > 2;
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
