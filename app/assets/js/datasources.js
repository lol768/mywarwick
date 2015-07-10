"use strict";

export class DataSource {

  getUpdateStream(): Rx.Observable {
    throw new TypeError("Must implement getUpdateStream");
  }

  /**
   * Returns an observable instance of just tile updates from the update stream.
   */
  getTileUpdates(): Rx.Observable {
    return this.getUpdateStream().filter(msg => msg.type === 'tile-update');
  }

  /**
   * @param tileId The tile to get updates for.
   * @returns {Rx.Observable<T>}
   */
  getTileUpdatesFor(tileId: string): Rx.Observable {
    return this.getTileUpdates().filter(msg => msg.tileId === tileId);
  }
}