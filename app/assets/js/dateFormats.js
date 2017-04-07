import moment from 'moment-timezone';

const TZ = 'Europe/London';

export const localMoment = date => moment(date).tz(TZ);
export const localMomentUnix = date => moment.unix(date).tz(TZ);

/**
 * @typedef {Object} DateTimeOptions
 * @property {boolean} [printToday=false] - whether to include "Today" if it's today.
 */

const NO_OPTIONS = {};

const ONLY_TIME = 'HH:mm';
const TODAY_TIME = '[Today] HH:mm';
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

/**
 * @param then The moment to format.
 * @param now A relative date for calculating today/tomorrow formatting.
 * @param {DateTimeOptions} options
 */
function formatDateTimeMoment(then, now, options = NO_OPTIONS) {
  const { printToday = false } = options;
  if (then.isSame(now, 'day')) {
    return then.format(printToday ? TODAY_TIME : ONLY_TIME);
  }

  if (then.isSame(now.clone().subtract(1, 'day'), 'day')) {
    return then.format(YESTERDAY_TIME);
  }

  if (then.isSame(now, 'isoWeek')) {
    return then.format(WEEKDAY_TIME);
  }

  if (then.isSame(now, 'year')) {
    return then.format(FULL_DATE_WITHOUT_YEAR_TIME);
  }

  return then.format(FULL_DATE_TIME);
}

/**
 * @param {Date} date
 * @param {Date?} referenceDate=now
 * @param {DateTimeOptions} options
 * @returns {string}
 */
export function forNewsItem(date, referenceDate = new Date(), options) {
  if (date === undefined) throw new Error('No date specified'); // otherwise we render now :|

  return formatDateTimeMoment(localMoment(date), localMoment(referenceDate), options);
}

/**
 * @param {Date} date - The date to format.
 * @param {boolean} grouped - Omits date for yesterday and tomorrow
 * @param {Date} referenceDate=now - For relative formatting
 * @param {DateTimeOptions} options
 * @returns {string} The date formatted for an activity/notification item.
 */
export function forActivity(date, grouped = false, referenceDate = new Date(), options) {
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

  return formatDateTimeMoment(then, now, options);
}

/**
 * @param {Date} date
 * @param {Date?} referenceDate=now
 * @param {DateTimeOptions} options
 * @returns {string}
 */
export default function formatDateTime(date, referenceDate = new Date(), options) {
  return forNewsItem(date, referenceDate, options);
}

export function formatDate(date, referenceDate = new Date()) {
  if (date === undefined) throw new Error('No date specified'); // otherwise we render now :|

  return formatDateMoment(localMoment(date), localMoment(referenceDate));
}

export function formatTime(date) {
  if (date === undefined) throw new Error('No date specified'); // otherwise we render now :|

  return formatTimeMoment(localMoment(date));
}
