// @flow

import _ from 'lodash-es';
import moment from 'moment';

export const DATE_KEY = 'date';
export const ID_KEY = 'id';

const DESC = 'desc';

type Item = {
  id: string,
  date: string
}

type Rx = Item[];
type Stream = Item[];
type Filter = {

};

type FetchedActivities = {
  stream: any,
  olderItemsOnServer: any,
  filter: any,
  filterOptions: any
}

const sortStream = (stream: Stream) => _.orderBy(stream, [DATE_KEY, ID_KEY], [DESC, DESC]);

const uniqStream = (stream: Stream) => _.uniqBy(stream, ID_KEY);

export function makeStream(): Stream {
  return [];
}

/*
 * Add the items in rx to the stream.  The resulting array is ordered on date
 * descending, and is free of duplicate items.
 *
 * A number of tricks are attempted to reduce the amount of work required to
 * perform the merge.  If these all fail, it falls back to concatenating,
 * de-duplicating and sorting the whole thing.
 */
export function mergeReceivedItems(stream: Stream = [], rx: Rx = []) {
  // Preconditions: stream has no duplicates, stream is in reverse date order

  const uniqRx = uniqStream(rx);

  const newest = _.first(stream);
  const oldestRx = _.minBy(uniqRx, x => x[DATE_KEY]);

  // Short circuit if existing stream is empty
  if (stream.length === 0) {
    return sortStream(uniqRx);
  }

  // Short circuit if all received things are newer than the newest we have
  if (newest[DATE_KEY] < oldestRx[DATE_KEY]) {
    return uniqStream(sortStream(uniqRx).concat(stream));
  }

  // Try and do the smallest possible merge
  // (>= to include identical items in dedupe later)
  const mergeStart = _.findLastIndex(stream, x => x[DATE_KEY] >= oldestRx[DATE_KEY]);

  if (mergeStart >= 0) {
    const toMerge = stream.splice(0, mergeStart + 1).concat(uniqRx);
    return sortStream(uniqStream(toMerge)).concat(stream);
  }

  // If all rx items older than all stream items, merge whole array
  return sortStream(uniqStream(uniqRx.concat(stream)));
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
export function onStreamReceive(
  stream: Stream = [],
  grouper: Item => any = item => item.date,
  rx: Rx = [],
) {
  if (rx.length === 0) return stream;
  const result = _.clone(stream);
  _.each(_.groupBy(rx, grouper), (v, k) => {
    result[k] = mergeReceivedItems(result[k] || [], v);
  });
  return result;
}

function getOrderedStreamPartitions(stream) {
  return _.flow(
    _.toPairs,
    pairs => _.sortBy(pairs, ([k]) => k).map(([, v]) => v),
    _.reverse,
  )(stream);
}

/*
 * Get the items in the stream partition at the given index.  Indexes are
 * determined by sorting the partition keys.
 *
 * If the partition does not exist, return an empty list.
 */
export function getStreamPartition(stream: Stream, i: number) {
  return getOrderedStreamPartitions(stream)[i] || [];
}

/*
 * Return the n most recent items from the stream.
 */
export function takeFromStream(stream: Stream, n: number) {
  return _.reduce(getOrderedStreamPartitions(stream),
    (result, part) => {
      if (result.length >= n) return result;
      return result.concat(_.take(part, n - result.length));
    },
    [],
  );
}

export function getLastItemInStream(stream: Stream) {
  return _.last(
    getOrderedStreamPartitions(stream)
      .map(part => _.last(part))
      .filter(part => !!part),
  );
}

/*
 * Return the total number of items in the stream.
 */
export function getStreamSize(stream: Stream) {
  return _.reduce(stream, (sum, part) => sum + part.length, 0);
}

/*
 * Return the total number of items in the stream with a date after the given date.
 *
 * If date is falsy, return the total number of items in the stream.
 */
export function getNumItemsSince(stream: Stream, date: moment) {
  if (!date) {
    return getStreamSize(stream);
  }

  return _.reduce(stream,
    (sum, part) => sum + part.filter(item => moment(item.date).isAfter(date)).length,
    0,
  );
}

/** Convert to a regular array for the persisted module */
export function freeze({ stream, olderItemsOnServer, filter, filterOptions }: FetchedActivities) {
  return {
    items: _.flatten(_.values(stream)),
    meta: {
      olderItemsOnServer,
      filter,
      filterOptions,
    },
  };
}

export function filterStream(stream: Stream, filter: Filter) {
  return _.pickBy(
    _.mapValues(stream, part =>
      _.filter(part, item =>
        _.every(_.keys(filter), key =>
          item[key] === undefined ||
          filter[key] === undefined ||
          filter[key][item[key]] === undefined ||
          filter[key][item[key]],
        ),
      ),
    ),
    part => part.length > 0,
  );
}
