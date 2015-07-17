var Rx = require('rx');
var moment = require('moment');

var DataSource = require('./datasources').DataSource

/**
 * Implementation of DataSource that provides an Observable
 * of fake data items, generated at random intervals.
 */
export class FakeDataSource extends DataSource {

  constructor() {
    super();
    var stream = new Rx.ReplaySubject(1);
    var counter = 0;

    // This is all fake data pumped into the stream
    setInterval(() => {
      stream.onNext({
        type: 'tile-update',
        tileId: '1',
        value: counter++
      });
    }, 900);

    setInterval(() => {
      stream.onNext({
        type: 'tile-update',
        tileId: '2',
        temperature: 25 + Math.floor(Math.random()*10)
      });
    }, 3200);

    setTimeout(() => {
      stream.onNext({
        type: 'tile-update',
        tileId: '3',
        items: [
          {
            key: 1,
            title: 'Good news, nobody!',
            published: moment().startOf('year').format()
          }
        ]
      });
    }, 0);

    setInterval(() => {
      stream.onNext({
        type: 'tile-update',
        tileId: '3',
        items: [
          {
            key: 1000 + counter,
            title: 'A number is ' + counter,
            published: moment().format(),
          }
        ]
      });
    }, 1000);

    setInterval(() => {
      stream.onNext({
        type: 'tile-update',
        tileId: '4',
        items: [
          {
            key: 1000 + counter,
            title: 'Coursework marked: CS' + (100+(counter*7 % 300)),
            published: moment().format()
          }
        ]
      });
    }, 17000);

    this.updateStream = stream;
  }

  /**
   * @returns {*} fuck all
   */
  getUpdateStream(): Rx.Observable {
    "use strict";
    return this.updateStream.asObservable();
  }
}

export function createSource() {
  return new FakeDataSource();
}