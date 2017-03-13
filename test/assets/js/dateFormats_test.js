import * as dateFormats from 'dateFormats';
import { localMoment } from 'dateFormats';

describe('dateFormats', () => {
  it('formats time only on current date', () => {
    let date = localMoment().hour(9).minute(0).toDate();

    expect(dateFormats.forNewsItem(date)).to.equal('09:00');
  });

  it('formats time yesterday for grouped activity', () => {
    let date = localMoment().subtract(1, 'day').hour(17).minute(0).toDate();

    expect(dateFormats.forActivity(date, true)).to.equal('17:00');
  });

  it('formats time yesterday', () => {
    let date = localMoment().subtract(1, 'day').hour(17).minute(0).toDate();

    expect(dateFormats.forNewsItem(date)).to.equal('Yesterday 17:00');
  });

  it('formats day name and time in current week', () => {
    let date = localMoment().year(2016).week(2).isoWeekday(1).hour(10).minute(30).toDate();
    let now = localMoment().year(2016).week(2).isoWeekday(4).toDate();

    expect(dateFormats.forNewsItem(date, now)).to.equal('Mon 10:30');
  });

  it('formats as date without year in current year', () => {
    let date = localMoment().year(2016).dayOfYear(1).hour(12).minute(45).toDate();
    let now = localMoment().year(2016).dayOfYear(200).toDate();

    expect(dateFormats.forNewsItem(date, now)).to.equal('Fri 1 Jan, 12:45');
  });

  it('formats as date with year in other years', () => {
    let date = localMoment().year(1999).dayOfYear(365).hour(23).minute(59).toDate();
    let now = localMoment().year(2001).toDate();

    expect(dateFormats.forNewsItem(date, now)).to.equal('Fri 31 Dec 1999, 23:59');
  });

  it('gives precedence to shorter format', () => {
    let date = localMoment().year(1999).dayOfYear(365).hour(23).minute(59).toDate();
    let now = localMoment().year(2000).dayOfYear(1).toDate();

    expect(dateFormats.forNewsItem(date, now)).to.equal('Yesterday 23:59');
  });
});
