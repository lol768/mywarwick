import moment from 'moment-timezone';

export const localMoment = date => moment(date).tz('Europe/London');
export const localMomentUnix = date => moment.unix(date).tz('Europe/London');

const ONLY_TIME = 'H:mm';
const YESTERDAY_TIME = '[Yesterday] H:mm';
const WEEKDAY_TIME = 'ddd H:mm';
const FULL_DATE_WITHOUT_YEAR_TIME = 'ddd D MMM, H:mm';
const FULL_DATE_TIME = 'ddd D MMM YYYY, H:mm';

function formatMoment(then, now) {
  if (then.isSame(now, 'day')) {
    return then.format(ONLY_TIME);
  }

  if (then.isSame(now.subtract(1, 'day'), 'day')) {
    return then.format(YESTERDAY_TIME);
  }

  now.add(1, 'day');

  if (then.isSame(now, 'isoWeek')) {
    return then.format(WEEKDAY_TIME);
  }

  if (then.isSame(now, 'year')) {
    return then.format(FULL_DATE_WITHOUT_YEAR_TIME);
  }

  return then.format(FULL_DATE_TIME);
}

export function formatDateForNewsItem(date, referenceDate = new Date()) {
  if (date === undefined) throw new Error('No date specified'); // otherwise we render now :|

  return formatMoment(localMoment(date), localMoment(referenceDate));
}

export function formatDateForActivity(date, grouped = false, referenceDate = new Date()) {
  if (date === undefined) throw new Error('No date specified'); // otherwise we render now :|
  const then = localMoment(date);
  const now = localMoment(referenceDate);

  if (grouped) {
    now.subtract(1, 'day');

    if (then.isSame(now, 'day')) {
      return then.format(ONLY_TIME);
    }

    now.add(2, 'days');

    if (then.isSame(now, 'day')) {
      return then.format(ONLY_TIME);
    }

    now.subtract(1, 'day');
  }

  return formatMoment(then, now);
}

export default function formatDate(date, referenceDate = new Date()) {
  return formatDateForNewsItem(date, referenceDate);
}

