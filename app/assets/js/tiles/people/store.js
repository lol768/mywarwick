
const log = require('loglevel');
const Rx = require('rx');

const BaseStore = require('../../stores/base');
const Dispatcher = require('../../dispatcher').default;
const actions = require('./').actions;

export default class PeopleStore extends BaseStore {

  getResults() {
    return this.currentResults;
  }

  constructor(mainStore) {
    super();

    this.currentResults = null;

    this.changes = new Rx.Subject();

    Dispatcher.actionsOfType(actions.SEARCH).subscribe(
      (action) => {
        let name = action.name;
        log.info("Search for", name);
        this.searching = true;
        this.changes.onNext();

        // TODO make request up to datapipe, wait for a response.
        setTimeout(() => {
          // Fake results
          let results = {
            count: 2,
            items: [
              { name: (name + " Johnson"), email: 'X.Johnson.777@warwick.ac.uk' },
              { name: (name + " Johanssen"), email: 'X.Johanssen.999@warwick.ac.uk' },
            ]
          }

          this.searching = false;
          this.currentResults = results;
          this.changes.onNext();
        }, 200);
      }
    );
  }

}