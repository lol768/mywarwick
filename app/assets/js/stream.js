import Immutable from 'immutable';
import _ from 'lodash';

export const DATE_KEY = 'date';
export const ID_KEY = 'id';

const DESC = 'desc';

let sortStream = (stream) => _.sortByOrder(stream, [DATE_KEY, ID_KEY], [DESC, DESC]);

let uniqStream = (stream) => _.uniq(stream, ID_KEY);

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
