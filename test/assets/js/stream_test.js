import moment from 'moment'
import formatDate from 'dateFormatter'
import _ from 'lodash';
import { onStreamReceive, getStreamPartition, mergeReceivedItems, takeFromStream, getStreamSize, getNumItemsSince } from '../../../app/assets/js/stream';

let item = (id, date) => ({id: id, date: moment('2015-01-01').add(date, 'd').format()});

describe('stream', () => {

  let grouper = (n) => n.date.toString().substr(0, 7);

  let stream = onStreamReceive(undefined, grouper, [
    item('a', 0), item('b', 31), item('c', 59)
  ]);

  it('partitions received items', () => {
    let stream = onStreamReceive(undefined, grouper, [item('a', 1)]);

    expect(getStreamPartition(stream, 0)).to.eql([item('a', 1)]);
    expect(getStreamPartition(stream, 1)).to.eql([]);
  });

  it('orders partitions by key', () => {
    expect(_.keys(stream)).to.have.lengthOf(3);
    expect(getStreamPartition(stream, 0)[0].date).to.equal('2015-03-01T00:00:00+00:00');
    expect(getStreamPartition(stream, 1)[0].date).to.equal('2015-02-01T00:00:00+00:00');
    expect(getStreamPartition(stream, 2)[0].date).to.equal('2015-01-01T00:00:00+00:00');
  });

  it('knows the length of the stream', () => {
    expect(getStreamSize(stream)).to.equal(3);
  });

  it('knows the number of items since a date', () => {
    expect(getNumItemsSince(stream, moment('2014-12-31'))).to.equal(3);
    expect(getNumItemsSince(stream, moment('2015-01-01'))).to.equal(2);
    expect(getNumItemsSince(stream, moment('2015-02-01'))).to.equal(1);
    expect(getNumItemsSince(stream, moment('2015-03-01'))).to.equal(0);
  });

  it('takes a given number of items from the stream in order', () => {
    expect(takeFromStream(stream, -1)).to.eql([]);

    expect(takeFromStream(stream, 2)).to.eql([item('c', 59), item('b', 31)]);
  });

  it('returns zero items from an empty stream', () => {
    expect(takeFromStream(onStreamReceive(undefined, grouper, []), 100)).to.eql([]);
  });

});

describe('mergeReceivedItems', () => {

  it('initially sorts items', () => {
    let stream = mergeReceivedItems([], [item('a', 1), item('b', 2)]);

    expect(stream.map((i) => i.id)).to.eql(['b', 'a']);
  });

  it('adds first item', () => {
    let stream = mergeReceivedItems([], [item('a', 1)]);

    expect(stream.length).to.equal(1);
  });

  it('prepends a newer item', () => {
    let stream = mergeReceivedItems([item('a', 1)], [item('b', 2)]);

    expect(stream.length).to.equal(2);
    expect(stream[0].id).to.equal('b');
  });

  it('adds items where all are newer than existing', () => {
    let stream1 = mergeReceivedItems([
      item('b', 2),
      item('a', 1)
    ], [
      item('c', 3),
      item('e', 5),
      item('d', 4)
    ]);

    let stream2 = mergeReceivedItems([
      item('e', 5),
      item('d', 4),
      item('b', 2),
      item('a', 1)
    ], [
      item('c', 3)
    ]);

    let stream3 = mergeReceivedItems([
      item('d', 4)
    ], [
      item('c', 3),
      item('e', 5),
      item('b', 2),
      item('a', 1)
    ]);

    let stream4 = mergeReceivedItems([
      item('e', 5),
      item('d', 4)
    ], [
      item('c', 3),
      item('b', 2),
      item('a', 1)
    ]);

    expect(stream1).to.eql(stream2);
    expect(stream1).to.eql(stream3);
    expect(stream1).to.eql(stream4);

  });

});

