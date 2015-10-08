
const Store = require('./store');
const Rx = require('rx');
const log = require('loglevel');
const localforage = require('localforage');
const _ = require('lodash');
const moment = require('moment');
const BaseStore = require('./base');

/**
 * Stores the whole state of individual "tiles" of data
 * locally (in the browser local storage).
 *
 * Uses the localforage library, which will instead use
 * websql or indexeddb if the browser has support (and
 * most do), which support non-string values and are
 * much faster.
 *
 * TODO should there be an overall TileStore, or is this
 * really just the activity stream store?
 */
export default class TileStore extends BaseStore {

  constructor(dataSource) {
    super();

    /**
     * Takes a data item for a tile, and returns
     * a Promise for an array containing the data
     * and any existing data in the form [data, existing].
     * @param data Update data for a tile
     * @returns {Promise<[data, existing]>}
     */
    let withExisting = (data) =>
      this.get(data.tileId)
        .then((existing) => [data, existing]);

    // A comparator function for finding items
    // with a unique value for `propName`, without
    // collapsing every item that has no such property
    // or a null value.
    let uniqueIfPresent = (propName) => {
      let count = 0;
      return (item) => {
        if (item[propName]) return item[propName];
        else return `uniqueRand__${count++}`;
      }
    };

    //dataSource.getTileUpdates().flatMap(withExisting).subscribe((u) => {
    //  log.info("Well lookee here, an updatee",u);
    //})

    // Pipe tile updates from the stream into the store.
    // Replaces any previous "value", and appends to any
    // previous "items".
    dataSource.getTileUpdates().flatMap(withExisting).map((result) => {
      let [data, existing] = result;
      existing.tileId = data.tileId;
      existing.value = data.value;
      let items = data.items;
      if (items) {
        items.push.apply(items, existing.items);
        items = _.uniq(items, 'id');
        items = _.sortByOrder(items, [(item) => moment(item.published).unix()], ['desc'])

        // If 2 or more items have the same 'replaces' property value,
        // we keep the first one only (which is the newest one since we just
        // sorted descending by date
        items = _.uniq(items, uniqueIfPresent('replaces'));

        existing.items = items;
      }
      return existing;
    }).subscribe((data) =>
      // flatMap because this returns a Promise that it wraps as an observable
      this.save(data.tileId, data),
      log.error
    );

    //this.saves.subscribeOnError(log.error);
  }

}