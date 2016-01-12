import moment from 'moment-timezone';

export let localMoment = date => moment(date).tz('Europe/London');

export default function formatDate(d, nowDate = new Date(), forceDisplayDay = false) {
  let date = localMoment(d);
  let now = localMoment(nowDate);

  if (date.isSame(now, 'day') || date.isSame(now.clone().subtract(1, 'day'), 'day') || date.isSame(now.clone().add(1, 'day'), 'day')) {
    return forceDisplayDay ? date.format('ddd H:mm') : date.format('H:mm');
    // date is this week, last week, or next week
  } else if (date.isSame(now, 'week') || date.isSame(now.clone().subtract(1, 'week'), 'week') || date.isSame(now.clone().add(1, 'week'), 'week')) {
    return date.format('ddd H:mm');
  } else if (date.isSame(now, 'year')) {
    return date.format('ddd D MMM, H:mm');
  } else {
    return date.format('ddd D MMM YYYY, H:mm');
  }
}