/* eslint new-cap: 0 */ // lots of immutable makes this rule annoying

import { List, Map } from 'immutable';
import _ from 'lodash';
import moment from 'moment';

export const DATE_KEY = 'date';
export const ID_KEY = 'id';

const DESC = 'desc';

const sortStream = (stream) => _.sortByOrder(stream, [DATE_KEY, ID_KEY], [DESC, DESC]);

const uniqStream = (stream) => _.uniq(stream, ID_KEY);

export function makeStream() {
  return Map();
}

/*
 * Add the items in rx to the stream.  The resulting array is ordered on date
 * descending, and is free of duplicate items.
 *
 * A number of tricks are attempted to reduce the amount of work required to
 * perform the merge.  If these all fail, it falls back to concatenating,
 * de-duplicating and sorting the whole thing.
 */
export function mergeReceivedItems(stream = [], rx = []) {
  // Preconditions: stream has no duplicates, stream is in reverse date order

  const uniqRx = uniqStream(rx);

  const newest = _(stream).first();
  const oldestRx = _(uniqRx).min((x) => x[DATE_KEY]);

  // Short circuit if existing stream is empty
  if (stream.length === 0) {
    return sortStream(uniqRx);
  }

  // Short circuit if all received things are newer than the newest we have
  if (newest[DATE_KEY] < oldestRx[DATE_KEY]) {
    stream.unshift(...sortStream(uniqRx));

    return uniqStream(stream);
  }

  // Try and do the smallest possible merge
  // (>= to include identical items in dedupe later)
  const mergeStart = _(stream).findLastIndex((x) => x[DATE_KEY] >= oldestRx[DATE_KEY]);

  if (mergeStart >= 0) {
    const toMerge = stream.splice(0, mergeStart + 1);
    toMerge.push(...uniqRx);

    stream.unshift(...sortStream(uniqStream(toMerge)));

    return stream;
  }

  // If all rx items older than all stream items, merge whole array
  stream.push(...uniqRx);

  return sortStream(uniqStream(stream));
}

/*
 * Takes a partition function that is used to group stream items into
 * buckets, to make updates and persistence more efficient for streams
 * with a large number of items.
 *
 * The result of the partition function has to be a valid object key, and
 * be sortable. Aside from that, it doesn't really matter what it is, as
 * long as it's the same for all items that belong in the same partition.
 */
export function onStreamReceive(stream = Map(), grouper = (item) => item.date, rx = List()) {
  let result = stream;

  rx.groupBy(grouper).mapEntries(([k, v]) => {
    result = result.update(
      k,
      List(),
      (str) => List(mergeReceivedItems(str.toJS(), v.toList().toJS()))
    );
  });

  return result;
}

function getOrderedStreamPartitions(stream) {
  return stream.entrySeq().sortBy(([k, v]) => k).map(([k, v]) => v).reverse();
}

/*
 * Get the items in the stream partition at the given index.  Indexes are
 * determined by sorting the partition keys.
 *
 * If the partition does not exist, return an empty list.
 */
export function getStreamPartition(stream, i) {
  return getOrderedStreamPartitions(stream).get(i) || List();
}

/*
 * Return the n most recent items from the stream.
 */
export function takeFromStream(stream, n) {
  return getOrderedStreamPartitions(stream)
    .reduce(
      (result, part) => result.concat(part.take(n - result.size)),
      List()
    );
}

/*
 * Return the total number of items in the stream.
 */
export function getStreamSize(stream) {
  return stream.valueSeq().reduce((sum, part) => sum + part.size, 0);
}

/*
 * Return the total number of items in the stream with a date after the given date.
 */
export function getNumItemsSince(stream, date) {
  return stream.valueSeq().reduce((sum, part) =>
    sum + part.filter(item => moment(item.date).isAfter(date)).size, 0
  );
}
