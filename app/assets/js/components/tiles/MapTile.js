import React from 'react';
import TileContent from './TileContent';

export default class MapTile extends TileContent {

  static canZoom() {
    return true;
  }

  getLargeBody() {
    return null;
  }

  getZoomedBody() {
    return <iframe src="https://campus.warwick.ac.uk"></iframe>;
  }

  static needsContentToRender() {
    return false;
  }

  isEmpty() {
    return false;
  }

  expandsOnClick() {
    return true;
  }

}
