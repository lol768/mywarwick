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

type Partition = Item[];
type Rx = Item[];
// Stream is an object-type map, where keys
// are date partition names and the value is
// a list of items within that partition.
type Stream = {
  [string]: Partition
};
type Filter = {

};

type GroupFunction = Item => string;

type FetchedActivities = {
  stream: Stream,
  olderItemsOnServer: boolean,
  filter: any,
  filterOptions: any
}

const sortItems = (items: Item[]) => _.orderBy(items, [DATE_KEY, ID_KEY], [DESC, DESC]);

const uniqItems = (items: Item[]) => _.uniqBy(items, ID_KEY);

export function makeStream(): Stream {
  return {};
}

/*
 * Add the items in rx to this partition.  The resulting array is ordered on date
 * descending, and is free of duplicate items.
 *
 * A number of tricks are attempted to reduce the amount of work required to
 * perform the merge.  If these all fail, it falls back to concatenating,
 * de-duplicating and sorting the whole thing.
 */
export function mergeReceivedItems(partition: Partition = [], rx: Rx = []) {
  // Preconditions: partition has no duplicates and is in reverse date order

  const uniqRx = uniqItems(rx);

  const newest = _.first(partition);
  const oldestRx = _.minBy(uniqRx, x => x[DATE_KEY]);

  // Short circuit if existing partition is empty
  if (partition.length === 0) {
    return sortItems(uniqRx);
  }

  // Short circuit if all received things are newer than the newest we have
  if (newest[DATE_KEY] < oldestRx[DATE_KEY]) {
    return uniqItems(sortItems(uniqRx).concat(partition));
  }

  // Try and do the smallest possible merge
  // (>= to include identical items in dedupe later)
  const mergeStart = _.findLastIndex(partition, x => x[DATE_KEY] >= oldestRx[DATE_KEY]);

  if (mergeStart >= 0) {
    const toMerge = partition.splice(0, mergeStart + 1).concat(uniqRx);
    return sortItems(uniqItems(toMerge)).concat(partition);
  }

  // If all rx items older than all stream items, merge whole array
  return sortItems(uniqItems(uniqRx.concat(partition)));
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
  stream: Stream = {},
  grouper: GroupFunction = item => item.date,
  rx: Rx = [],
) {
  if (rx.length === 0) return stream;
  const result = _.clone(stream);
  _.each(_.groupBy(rx, grouper), (v, k) => {
    result[k] = mergeReceivedItems(result[k] || [], v);
  });
  return result;
}

function getOrderedStreamPartitions(stream): Partition[] {
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
export function getStreamPartition(stream: Stream, i: number): Partition {
  return getOrderedStreamPartitions(stream)[i] || [];
}

/*
 * Return the n most recent items from the stream.
 */
export function takeFromStream(stream: Stream, n: number): Item[] {
  return _.reduce(getOrderedStreamPartitions(stream),
    (result, part) => {
      if (result.length >= n) return result;
      return result.concat(_.take(part, n - result.length));
    },
    [],
  );
}

export function getLastItemInStream(stream: Stream): Item {
  return _.last(
    getOrderedStreamPartitions(stream)
      .map(part => _.last(part))
      .filter(part => !!part),
  );
}

/*
 * Return the total number of items in the stream.
 */
export function getStreamSize(stream: Stream): number {
  return _.reduce(stream, (sum, part) => sum + part.length, 0);
}

/*
 * Return the total number of items in the stream with a date after the given date.
 *
 * If date is falsy, return the total number of items in the stream.
 */
export function getNumItemsSince(stream: Stream, date: moment): number {
  if (!date) {
    return getStreamSize(stream);
  }

  return _.reduce(stream,
    (sum, part) => sum + part.filter(item => moment(item.date).isAfter(date)).length,
    0,
  );
}

/** Convert to a regular array for the persisted module */
export function freeze({
  stream,
  olderItemsOnServer,
  filter,
  filterOptions,
  }: FetchedActivities): any {
  return {
    items: _.flatten(_.values(stream)),
    meta: {
      olderItemsOnServer,
      filter,
      filterOptions,
    },
  };
}

export function filterStream(stream: Stream, filter: Filter): Stream {
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
