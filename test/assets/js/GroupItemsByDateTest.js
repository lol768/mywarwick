import GroupItemsByDate from 'GroupItemsByDate';
import moment from 'moment';

describe('GroupItemsByDate', () => {

  GroupItemsByDate.today = () => moment('2015-12-16T09:00:00Z');

  function item(date) {
    return {
      props: {
        date: moment(date).toDate()
      }
    };
  }

  it('groups today', () => {
    expect(GroupItemsByDate.groupForItem(item('2015-12-16'))).to.equal(0);
  });

  it('groups yesterday', () => {
    expect(GroupItemsByDate.groupForItem(item('2015-12-15'))).to.equal(1);
  });

  it('groups earlier this week', () => {
    expect(GroupItemsByDate.groupForItem(item('2015-12-14'))).to.equal(2);
  });

  it('groups last week', () => {
    expect(GroupItemsByDate.groupForItem(item('2015-12-12'))).to.equal(3);
    expect(GroupItemsByDate.groupForItem(item('2015-12-07'))).to.equal(3);
  });

  it('considers the most recent past Sunday to be last week', () => {
    expect(GroupItemsByDate.groupForItem(item('2015-12-13'))).to.equal(3);
  });

  it('groups earlier than last week', () => {
    expect(GroupItemsByDate.groupForItem(item('2015-12-06'))).to.equal(4);
    expect(GroupItemsByDate.groupForItem(item('2015-01-01'))).to.equal(4);
  });

});