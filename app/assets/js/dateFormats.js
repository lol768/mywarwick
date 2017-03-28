import moment from 'moment-timezone';

export const localMoment = date => moment(date).tz('Europe/London');
export const localMomentUnix = date => moment.unix(date).tz('Europe/London');

const ONLY_TIME = 'HH:mm';
const YESTERDAY_TIME = '[Yesterday] HH:mm';
const WEEKDAY_TIME = 'ddd HH:mm';
const FULL_DATE_WITHOUT_YEAR_TIME = 'ddd D MMM, HH:mm';
const FULL_DATE_TIME = 'ddd D MMM YYYY, HH:mm';

const DATE_WEEKDAY = 'ddd';
const DATE_FULL_WITHOUT_YEAR = 'ddd D MMM';
const DATE_FULL = 'ddd D MMM YYYY';

export function formatDateMoment(then, now = localMoment()) {
  if (then.isSame(now, 'day')) {
    return 'today';
  }

  if (then.isSame(now.add(1, 'day'), 'day')) {
    return 'tomorrow';
  }

  now.subtract(1, 'day');

  if (then.isSame(now, 'isoWeek')) {
    return then.format(DATE_WEEKDAY);
  }

  if (then.isSame(now, 'year')) {
    return then.format(DATE_FULL_WITHOUT_YEAR);
  }

  return then.format(DATE_FULL);
}

export function formatTimeMoment(then) {
  return then.format('HH:mm');
}

function formatDateTimeMoment(then, now) {
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

export function forNewsItem(date, referenceDate = new Date()) {
  if (date === undefined) throw new Error('No date specified'); // otherwise we render now :|

  return formatDateTimeMoment(localMoment(date), localMoment(referenceDate));
}

export function forActivity(date, grouped = false, referenceDate = new Date()) {
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

  return formatDateTimeMoment(then, now);
}

export default function formatDateTime(date, referenceDate = new Date()) {
  return forNewsItem(date, referenceDate);
}

export function formatDate(date, referenceDate = new Date()) {
  if (date === undefined) throw new Error('No date specified'); // otherwise we render now :|

  return formatDateMoment(localMoment(date), localMoment(referenceDate));
}

export function formatTime(date) {
  if (date === undefined) throw new Error('No date specified'); // otherwise we render now :|

  return formatTimeMoment(localMoment(date));
}
