import React from 'react';
import TextTile from './TextTile';
import { DEFAULT_TILE_SIZES, TILE_SIZES } from './TileContent';
import { pluralise } from '../../helpers';

export default class WorkareaTile extends TextTile {
  static supportedTileSizes() {
    return DEFAULT_TILE_SIZES.concat([TILE_SIZES.LARGE, TILE_SIZES.TALL]);
  }

  renderItems(itemsToDisplay) {
    return itemsToDisplay.map(workarea => (
      <div key={workarea.id}>
        <div className="tile__item">
          {this.makeCallout(workarea)}
          {this.makeTileText(workarea)}
        </div>
      </div>
    ));
  }

  makeCallout(workarea) {
    return (
      <span>
        <span className="tile__callout">
          {workarea.availableSeats}/{workarea.totalSeats}
        </span>
        &nbsp;{pluralise('seat', workarea.totalSeats)} available
      </span>
    );
  }

  makeTileText(workarea) {
    return (
      <span className="tile__text">{workarea.location}</span>
    );
  }
}
