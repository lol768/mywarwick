/** Pretend localforage API. */
export default class MemoryLocalForage {
  constructor() {
    this.data = {};
  }
  getItem(key) {
    return Promise.resolve(this.data[key]);
  }
  setItem(key, value) {
    this.data[key] = value;
    return Promise.resolve(value);
  }
  clear() {
    this.data = {};
    return Promise.resolve();
  }
}