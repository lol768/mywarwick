import moment from 'moment-timezone';

export const localMoment = date => moment(date).tz('Europe/London');

export default function formatDate(d, nowDate = new Date(), forceDisplayDay = false) {
  const date = localMoment(d);
  const now = localMoment(nowDate);

  // today, tomorrow, or yesterday
  if (
    date.isSame(now, 'day') ||
    date.isSame(now.clone().subtract(1, 'day'), 'day') ||
    date.isSame(now.clone().add(1, 'day'), 'day')
  ) {
    return forceDisplayDay ? date.format('ddd H:mm') : date.format('H:mm');
    // this year
  } else if (date.isSame(now, 'year')) {
    return date.format('ddd D MMM, H:mm');
    // not this year
  }
  return date.format('ddd D MMM YYYY, H:mm');
}
