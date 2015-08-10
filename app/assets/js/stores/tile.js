
const Store = require('./store');
const Rx = require('rx');
const log = require('loglevel');
const localforage = require('localforage');
const _ = require('lodash');
const moment = require('moment');

/**
 * Stores the whole state of individual "tiles" of data
 * locally (in the browser local storage).
 *
 * Uses the localforage library, which will instead use
 * websql or indexeddb if the browser has support (and
 * most do), which support non-string values and are
 * much faster.
 */
export default class TileStore extends Store {

  constructor(dataSource: Stream) {
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

    // Pipe tile updates from the stream into the store.
    // Replaces any previous "value", and appends to any
    // previous "items".
    dataSource.getTileUpdates().flatMap(withExisting).map((result) => {
      log.debug('Got tile update', result);

      let [data, existing] = result;
      existing.tileId = data.tileId;
      existing.value = data.value;
      let items = data.items;
      items.push.apply(items, existing.items);
      items = _.uniq(items, 'id');
      items = _.sortByOrder(items, [(item) => moment(item.published).unix()], ['desc'])

      // If 2 or more items have the same 'replaces' property value,
      // we keep the first one only (which is the newest one since we just
      // sorted descending by date
      items = _.uniq(items, uniqueIfPresent('replaces'));

      existing.items = items;
      return existing;
    }).flatMap((data) =>
      // flatMap because this returns a Promise that it wraps as an observable
      this.save(data.tileId, data),
      log.error
    );

    this.saves = new Rx.Subject();
    //this.saves.subscribeOnError(log.error);
  }

  /**
   * Saves a new item to the store.
   *
   * @returns {Promise} whose value will be the value you saved.
   */
  save(key, data): Promise<any> {
    log.debug('Saving', key, 'as', data);
    return localforage.setItem(this.toStorageKey(key), data)
      .then((v) => {
        this.saves.onNext({key, data});
      },
      log.error);
  }

  /**
   * Gets the data for a tile.
   *
   * If you use then() to run some code on completion and you
   * aren't returning the Promise to anyone else, make sure to
   * call done() at the end otherwise errors are swallowed.
   *
   * @param key The Tile ID to look up.
   * @returns {Promise} containing the value if found, otherwise a blank item
   *          with an empty items array.
   */
  get(key): Promise<object> {
    return localforage.getItem(this.toStorageKey(key))
      .then((value) => value || { items: [] }, log.error);
  }

  updates(key): Rx.Observable<string> {
    return this.saves.filter((info) => info.key == key)
      .map((info) => info.data)
  }

  toStorageKey(key): String {
    return `tiles.${key}`;
  }

}