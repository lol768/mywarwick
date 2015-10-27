import { Stream, onReceive } from '../../../app/assets/js/stream';

let item = (id, date) => ({id: id, date: date});

describe('Stream', () => {

  it('partitions received items', () => {

    let stream = new Stream((x) => Math.floor(x.date / 10))

    stream.receive([item('a', 1)]);

    expect(stream.partitionCount()).to.equal(1);
    expect(stream.getPartition(0)).to.eql([item('a', 1)]);
    expect(stream.getPartition(1)).to.equal(undefined);

  });

  it('imposes an ordering on partition keys', () => {

    let stream = new Stream((x) => Math.floor(x.date / 10))

    stream.receive([item('a', 1), item('b', 11), item('c', 21)]);

    expect(stream.partitionCount()).to.equal(3);
    expect(stream.getPartition(0)[0].date).to.equal(1);
    expect(stream.getPartition(1)[0].date).to.equal(11);
    expect(stream.getPartition(2)[0].date).to.equal(21);

  });

});

describe('onReceive', () => {

  it('initially sorts items', () => {
    let stream = onReceive([], [item('a', 1), item('b', 2)]);

    expect(stream.map((i) => i.id)).to.eql(['b', 'a']);
  });

  it('adds first item', () => {
    let stream = onReceive([], [item('a', 1)]);

    expect(stream.length).to.equal(1);
  });

  it('prepends a newer item', () => {
    let stream = onReceive([item('a', 1)], [item('b', 2)]);

    expect(stream.length).to.equal(2);
    expect(stream[0].id).to.equal('b');
  });

  it('adds items where all are newer than existing', () => {
    let stream1 = onReceive([
      item('b', 2),
      item('a', 1)
    ], [
      item('c', 3),
      item('e', 5),
      item('d', 4)
    ]);

    let stream2 = onReceive([
      item('e', 5),
      item('d', 4),
      item('b', 2),
      item('a', 1)
    ], [
      item('c', 3)
    ]);

    let stream3 = onReceive([
      item('d', 4)
    ], [
      item('c', 3),
      item('e', 5),
      item('b', 2),
      item('a', 1)
    ]);

    let stream4 = onReceive([
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

