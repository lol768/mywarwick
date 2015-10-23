import Rx from 'rx';
import moment from 'moment';
import log from 'loglevel';
import DataPipe from '../datapipe';

/**
 * Implementation of DataSource that provides an Observable
 * of fake data items, generated at random intervals.
 */
export default class FakeDataPipe extends DataPipe {

  constructor() {
    super();
    let stream = new Rx.ReplaySubject(1);

    this.fake = true;
    this.counter = 0;

    this.stream = stream;
  }

  connect() {}

  requestData(info) {
    super.requestData(info);

    this.stream.onNext({
      type: 'tile-update',
      tileId: ("3"),
      items: [
        {
          id: '1000001',
          title: 'Staff Summer Festival',
          links: {
            canonical: { href: "http://www2.warwick.ac.uk/about/warwick50/events/stafffestival" }
          },
          published: moment(1438336410350).format(),
        },
        {
          id: '1000009',
          title: 'Economics seminar Friday 7th Aug in S0.23',
          links: {
            canonical: { href: "http://www2.warwick.ac.uk/about/warwick50/events/stafffestival" }
          },
          published: moment(1438636410350).format(),
        }
      ]
    });

    this.stream.onNext({
      type: 'tile-update',
      tileId: ("4"),
      items: [
        {
          id: '1200010',
          title: 'Your CourseSync was updated - 13 changes',
          replaces: 'coursesync-updated-cusxxx',
          links: {
            canonical: { href: "http://www2.warwick.ac.uk/about/warwick50/events/stafffestival" }
          },
          published: moment(1438336410350).format(),
        },
        {
          id: '1200011',
          title: 'Your CourseSync was updated - 3 changes',
          replaces: 'coursesync-updated-cusxxx',
          links: {
            canonical: { href: "http://www2.warwick.ac.uk/about/warwick50/events/stafffestival" }
          },
          published: moment(1438636410350).format(),
        },
        {
          id: '1200015',
          title: 'Your CS118 coursework is due in 12pm',
          links: {
            canonical: { href: "http://www2.warwick.ac.uk/about/warwick50/events/stafffestival" }
          },
          published: moment(1438436410350).format(),
        }
      ]
    });

  }

  getUpdateStream(): Rx.Observable {
    return this.stream.asObservable();
  }

  fakeNewsItems({interval, tileId}, callback) {
    setInterval(() => {
      const id = this.fakeId();
      this.stream.onNext({
        type: 'tile-update',
        tileId: (""+tileId),
        items: [
          {
            id: id,
            title: callback(),
            published: moment().format(),
          }
        ]
      });
    }, interval || 10000);
  }

  fakeId() {
    return (new Date()).getTime();
  }
}