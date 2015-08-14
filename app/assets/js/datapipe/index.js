"use strict";

const log = require('loglevel');

export default class DataPipe {

  /**
   * Request from the server any updates that the client may have missed
   * since last connecting.
   *
   * The info should probably be per-tile since some tiles may be more
   * up to date than others (like if you add a new one or just decide to clear
   * the data for one).
   */
  requestData(info) {

  }

  send(obj) {
    throw new TypeError("Must implement send");
  }

  getUpdateStream():Rx.Observable {
    throw new TypeError("Must implement getUpdateStream");
  }

  /**
   * Returns an observable instance of just tile updates from the update stream.
   * Subclasses can choose to override this if they get these updates from a
   * separate stream.
   */
  getTileUpdates():Rx.Observable {
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