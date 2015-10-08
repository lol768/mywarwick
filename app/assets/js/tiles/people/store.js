
const log = require('loglevel');
const Rx = require('rx');

const BaseStore = require('../../stores/base');

export default class PeopleStore extends BaseStore {

  getResults() {
    return this.currentResults;
  }

  search(name) {
    log.info("Search for", name);
    this.searching = true;
    this.changes.onNext();

    const response = this.dataPipe.ask({
      tileId: this.tileId,
      data: {
        name: name
      }
    });

    const done = (err, results) => {
      this.searching = false;
      this.error = err;
      this.currentResults = results;
      this.changes.onNext();
    }

    response
      .timeout(10000)
      .map(msg => msg.data)
      .subscribe(
        result => done(null, result),
        error => done(error, null)
      )

  }

  constructor(tileId, dataPipe) {
    super();

    this.currentResults = null;

    this.tileId = tileId;
    this.dataPipe = dataPipe;

    this.changes = new Rx.Subject();
  }

}