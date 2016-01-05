import moment from 'moment';
import 'moment-timezone';

describe('Date format', () => {

  it('interprets UTC dates', () => {
    let date = moment("2016-01-01T09:00:00.000Z").tz('Europe/London');

    expect(date.hour()).to.equal(9);
    expect(date.utcOffset()).to.equal(0);
  });

  it('interprets dates during British Summer Time', () => {
    let date = moment("2016-06-01T08:00:00.000Z").tz('Europe/London');

    expect(date.hour()).to.equal(9);
    expect(date.utcOffset()).to.equal(60);

    expect(date.utc().hour()).to.equal(8);
  });

});