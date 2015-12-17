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

  it('groups today', () => {
    expect(groupForItem('2015-12-16')).to.equal(0);
  });

  it('groups yesterday', () => {
    expect(groupForItem('2015-12-15')).to.equal(1);
  });

  it('groups earlier this week', () => {
    expect(groupForItem('2015-12-14')).to.equal(2);
  });

  it('groups last week', () => {
    expect(groupForItem('2015-12-12')).to.equal(3);
    expect(groupForItem('2015-12-07')).to.equal(3);
  });

  it('considers the most recent past Sunday to be last week', () => {
    expect(groupForItem('2015-12-13')).to.equal(3);
  });

  it('groups earlier than last week', () => {
    expect(groupForItem('2015-12-06')).to.equal(4);
    expect(groupForItem('2015-01-01')).to.equal(4);
  });

});