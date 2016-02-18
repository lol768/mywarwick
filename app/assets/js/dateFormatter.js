import moment from 'moment-timezone';

export const localMoment = date => moment(date).tz('Europe/London');

export default function formatDate(d, nowDate = new Date(), alwaysDisplayWeekday = false) {
  const date = localMoment(d);
  const now = localMoment(nowDate);

  const isThisYear = date.year() === now.year();

  if (
    date.isSame(now, 'day') ||
    date.isSame(now.subtract(1, 'day'), 'day') ||
    date.isSame(now.add(2, 'day'), 'day')
  ) {
    return alwaysDisplayWeekday ? date.format('ddd H:mm') : date.format('H:mm');
  }

  if (isThisYear) {
    return date.format('ddd D MMM, H:mm');
  }

  return date.format('ddd D MMM YYYY, H:mm');
}
