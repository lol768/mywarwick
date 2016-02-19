/* eslint no-unused-vars:0 */
export default class DataPipe {

  send(obj) {
    throw new TypeError('Must implement send');
  }

  subscribe() {
    throw new TypeError('Must implement subscribe');
  }

}
