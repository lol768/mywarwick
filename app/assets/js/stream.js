import Immutable from 'immutable';
import _ from 'lodash';

export const DATE_KEY = 'date';
export const ID_KEY = 'id';

const DESC = 'desc';

let sortStream = (stream) => _.sortByOrder(stream, [DATE_KEY, ID_KEY], [DESC, DESC]);

let uniqStream = (stream) => _.uniq(stream, ID_KEY);

export class Stream {

  /*
   * Takes a partition function that is used to group stream items into
   * buckets, to make updates and persistence more efficient for streams
   * with a large number of items.
   *
   * The result of the partition function has to be a valid object key, and
   * be sortable. Aside from that, it doesn't really matter what it is, as
   * long as it's the same for all items that belong in the same partition.
   */
  constructor(partitionFn) {
    this.partitionFn = partitionFn;
    this.partitions = {};
    this.sortedPartitionKeys = [];
  }

  /*
   * Merge a list of received items into this Stream.
   */
  receive(rx) {
    let beforeCount = this._privatePartitionCount();

    _(rx).groupBy(this.partitionFn).each((items, key) => {
      this.partitions[key] = onReceive(this.partitions[key], items);
    }).value();

    // If any new partitions were added, re-sort the partition keys for
    // index-based access
    if (this._privatePartitionCount() != beforeCount) {
      this.sortedPartitionKeys = Object.keys(this.partitions).sort();
    }
  }

  /*
   * This is pseudo-private because apparently Object.keys() runs in linear
   * time on the size of the object...
   */
  _privatePartitionCount() {
    return Object.keys(this.partitions).length;
  }

  /*
   * ... whereas Array.length is constant time.
   */
  partitionCount() {
    return this.sortedPartitionKeys.length;
  }

  /*
   * Get the items in the stream partition at the given index.  Indexes are
   * determined by sorting the partition keys.
   */
  getPartition(i) {
    let key = this.sortedPartitionKeys[i];

    return this.partitions[key];
  }

}

/*
 * Add the items in rx to the stream.  The resulting array is ordered on date
 * descending, and is free of duplicate items.
 *
 * A number of tricks are attempted to reduce the amount of work required to
 * perform the merge.  If these all fail, it falls back to concatenating,
 * de-duplicating and sorting the whole thing.
 */
export function onReceive(stream = [], rx = []) {
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

  // Do the smallest possible merge
  // >= to include identical items in dedupe later
  let mergeStart = _(stream).findLastIndex((x) => x[DATE_KEY] >= oldestRx[DATE_KEY]);

  if (mergeStart == -1) {
    // If all rx items older than all stream items, merge whole array
    stream.push(...rx);

    return sortStream(uniqStream(stream));
  } else {
    let toMerge = stream.splice(0, mergeStart + 1);
    toMerge.push(...rx);

    stream.unshift(...sortStream(uniqStream(toMerge)));

    return stream;
  }
}
