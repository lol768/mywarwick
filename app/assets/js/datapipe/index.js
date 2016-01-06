"use strict";

import log from 'loglevel';

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
    throw new TypeError("Must implement requestData");
  }

  send(obj) {
    throw new TypeError("Must implement send");
  }

  getUpdateStream():Rx.Observable {
    throw new TypeError("Must implement getUpdateStream");
  }

}