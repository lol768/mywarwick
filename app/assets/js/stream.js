import Immutable from 'immutable';
import _ from 'lodash';

export const DATE_KEY = 'date';
export const ID_KEY = 'id';

const DESC = 'desc';

let sortStream = (stream) => _.sortByOrder(stream, [DATE_KEY, ID_KEY], [DESC, DESC]);

let uniqStream = (stream) => _.uniq(stream, ID_KEY);

export function makeStream() {
  return Immutable.Map();
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
export function onStreamReceive(stream = Immutable.Map(), grouper = (item) => item.date, rx = Immutable.List()) {
  rx.groupBy(grouper).mapEntries(([k, v]) => {
    stream = stream.update(k, Immutable.List(), (str) => Immutable.List(mergeReceivedItems(str.toJS(), v.toList().toJS())));
  });

  return stream;
}

/*
 * Get the items in the stream partition at the given index.  Indexes are
 * determined by sorting the partition keys.
 *
 * If the partition does not exist, return an empty list.
 */
export function getStreamPartition(stream, i) {
  return stream.entrySeq().sortBy(([k, v]) => k).map(([k, v]) => v).get(i)
    || Immutable.List();
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

  // Defensive copy both
  stream = _.cloneDeep(stream);
  rx = uniqStream(rx);

  let newest = _(stream).first();
  let oldestRx = _(rx).min((x) => x[DATE_KEY]);

  // Short circuit if existing stream is empty
  if (stream.length == 0) {
    return sortStream(rx);
  }

  // Short circuit if all received things are newer than the newest we have
  if (newest[DATE_KEY] < oldestRx[DATE_KEY]) {
    stream.unshift(...sortStream(rx));

    return uniqStream(stream);
  }

  // Try and do the smallest possible merge
  // (>= to include identical items in dedupe later)
  let mergeStart = _(stream).findLastIndex((x) => x[DATE_KEY] >= oldestRx[DATE_KEY]);

  if (mergeStart >= 0) {
    let toMerge = stream.splice(0, mergeStart + 1);
    toMerge.push(...rx);

    stream.unshift(...sortStream(uniqStream(toMerge)));

    return stream;
  }

  // If all rx items older than all stream items, merge whole array
  stream.push(...rx);

  return sortStream(uniqStream(stream));
}
