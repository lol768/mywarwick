import moment from 'moment';

export default function formatDate(d, nowDate = new Date()) {
  let date = moment(d);
  let now = moment(nowDate);

  if (date.isSame(now, 'day') || date.isSame(now.subtract(1, 'day'), 'day')) {
    return date.format('H:mm');
  } else if (date.isSame(now, 'week')) {
    return date.format('ddd H:mm');
  } else if (date.isSame(now, 'year')) {
    return date.format('ddd D MMM, H:mm');
  } else {
    return date.format('ddd D MMM YYYY, H:mm');
  }
}