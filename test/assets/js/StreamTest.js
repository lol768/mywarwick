import Immutable from 'immutable';
import { onStreamReceive, getStreamPartition, mergeReceivedItems } from '../../../app/assets/js/stream';

let item = (id, date) => ({id: id, date: date});

describe('Stream', () => {

  let grouper = (x) => Math.floor(x.date / 10);

  it('partitions received items', () => {
    let stream = onStreamReceive(undefined, grouper, Immutable.List([item('a', 1)]));

    expect(getStreamPartition(stream, 0)).to.eql(Immutable.List([item('a', 1)]));
    expect(getStreamPartition(stream, 1)).to.equal(Immutable.List());
  });

  it('imposes an ordering on partition keys', () => {
    let stream = onStreamReceive(undefined, grouper, Immutable.List([
      item('a', 1), item('b', 11), item('c', 21)
    ]));

    expect(stream.count()).to.equal(3);
    expect(getStreamPartition(stream, 0).first().date).to.equal(1);
    expect(getStreamPartition(stream, 1).first().date).to.equal(11);
    expect(getStreamPartition(stream, 2).first().date).to.equal(21);
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

