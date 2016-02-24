import moment from 'moment';
import formatDate from 'dateFormatter';

describe('dateFormatter', () => {

  let localMoment = () => moment().tz('Europe/London');

  it('formats time only on current date', () => {

    let date = localMoment().hour(9).minute(0).toDate();

    expect(formatDate(date)).to.equal('9:00');

  });

  it('formats time only yesterday', () => {

    let date = localMoment().subtract(1, 'day').hour(17).minute(0).toDate();

    expect(formatDate(date)).to.equal('17:00');

  });

  it('formats day name and time in current week', () => {

    let date = localMoment().year(2016).week(2).isoWeekday(1).hour(10).minute(30).toDate();
    let now = localMoment().year(2016).week(2).isoWeekday(4).toDate();

    expect(formatDate(date, now)).to.equal('Mon 4 Jan, 10:30');

  });

  it('formats as date without year in current year', () => {

    let date = localMoment().year(2016).dayOfYear(1).hour(12).minute(45).toDate();
    let now = localMoment().year(2016).dayOfYear(200).toDate();

    expect(formatDate(date, now)).to.equal('Fri 1 Jan, 12:45');

  });

  it('formats as date with year in other years', () => {

    let date = localMoment().year(1999).dayOfYear(365).hour(23).minute(59).toDate();
    let now = localMoment().year(2001).toDate();

    expect(formatDate(date, now)).to.equal('Fri 31 Dec 1999, 23:59');

  });

  it('gives precedence to shorter format', () => {

    let date = localMoment().year(1999).dayOfYear(365).hour(23).minute(59).toDate();
    let now = localMoment().year(2000).dayOfYear(1).toDate();

    expect(formatDate(date, now)).to.equal('23:59'); // yesterday rule takes precedence

  });

});
