
const Store = require('./store');
const Rx = require('rx');
const log = require('loglevel');
const localforage = require('localforage');
const _ = require('lodash');
const moment = require('moment');

export default class BaseStore extends Store {

  constructor() {
    super();
    this.namespace = 'tiles';

    // Observable for anything that changes.
    this.changeEvents = new Rx.Subject();
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
        this.changeEvents.onNext({key, data});
      })
      .then(null, (e) => log.error(e));
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
    return this.changeEvents.filter((info) => info.key == key)
      .map((info) => info.data)
  }

  toStorageKey(key): String {
    return `${this.namespace}.${key}`;
  }

}