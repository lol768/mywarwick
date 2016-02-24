import * as grouper from 'GroupItemsByDate';
import moment from 'moment';

describe('grouper', () => {

  function item(date) {
    return {
      props: {
        date: moment(date).toDate()
      }
    };
  }

  let now = moment('2015-12-16T09:00:00Z');
  let groupForItem = (date) => grouper.groupForItem(item(date), now);

  it('groups next week', () => {
    expect(groupForItem('2015-12-21')).to.equal(0);
  });

  it('groups later this week', () => {
    expect(groupForItem('2015-12-18')).to.equal(1);
  });

  it('groups tomorrow', () => {
    expect(groupForItem('2015-12-17')).to.equal(2);
  });

  it('groups today', () => {
    expect(groupForItem('2015-12-16')).to.equal(3);
  });

  it('groups yesterday', () => {
    expect(groupForItem('2015-12-15')).to.equal(4);
  });

  it('groups earlier this week', () => {
    expect(groupForItem('2015-12-14')).to.equal(5);
  });

  it('groups last week', () => {
    expect(groupForItem('2015-12-12')).to.equal(6);
    expect(groupForItem('2015-12-07')).to.equal(6);
  });

  it('considers the most recent past Sunday to be last week', () => {
    expect(groupForItem('2015-12-13')).to.equal(6);
  });

  it('groups earlier than last week', () => {
    expect(groupForItem('2015-12-06')).to.equal(7);
    expect(groupForItem('2015-01-01')).to.equal(7);
  });

  /*
   * testing (Yesterday BUT ALSO Last Week) and (Tomorrow BUT ALSO Next Week)
   */
  let groupForItemMon = (date) => grouper.groupForItem(item(date), moment('2015-12-21T09:00:00Z'));
  let groupForItemSun = (date) => grouper.groupForItem(item(date), moment('2015-12-20T09:00:00Z'));

  it('groups yesterday but also last week', () => {
    expect(groupForItemMon('2015-12-20')).to.equal(4);
  });

  it('groups tomorrow but also next week', () => {
    expect(groupForItemSun('2015-12-21')).to.equal(2);
  });

  it('gets grouped items', () => {
    const items = [
      item('2015-12-21'),
      item('2015-12-18'),
      item('2015-12-17'),
      item('2015-12-16'),
      item('2015-12-15'),
      item('2015-12-14'),
      item('2015-12-12'),
      item('2015-12-07'),
      item('2015-12-06'),
      item('2015-01-01')
    ];

    const expectedGroupings = [
      [0, [
        item('2015-12-21')
      ]],
      [1, [
        item('2015-12-18')
      ]],
      [2, [
        item('2015-12-17')
      ]],
      [3, [
        item('2015-12-16')
      ]],
      [4, [
        item('2015-12-15')
      ]],
      [5, [
        item('2015-12-14')
      ]],
      [6, [
        item('2015-12-12'),
        item('2015-12-07')
      ]],
      [7, [
        item('2015-12-06'),
        item('2015-01-01')
      ]]
    ];

    const groupedItems = grouper.getGroupedItems(items, now);

    expect(groupedItems).to.deep.equal(expectedGroupings);
  });


});