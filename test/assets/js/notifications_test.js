import {
  notificationsReducer as reducer,
  receivedNotification,
  fetchedNotifications
} from 'state/notifications';
import { takeFromStream, getStreamSize } from '../../../app/assets/js/stream';

describe('notifications', () => {

  const item = {
    id: 'abc',
    date: '2018-01-01T00:00:00Z',
  };

  it('has initial state', () => {
    const state = reducer(undefined, { type: '?' });

    expect(state.fetching).to.equal(false);
    expect(state.olderItemsOnServer).to.equal(true);
    expect(state.lastItemFetched).to.equal(null);
  });

  it('accepts a received notification', () => {
    const state = reducer(undefined, receivedNotification(item));

    expect(getStreamSize(state.stream)).to.equal(1);
    expect(takeFromStream(state.stream, 1)).to.eql([item]);
  });

  it('accepts fetched notifications', () => {
    const state = reducer({ fetching: true }, fetchedNotifications({
      items: [item],
      meta: {
        olderItemsOnServer: true,
      },
    }));

    expect(state.lastItemFetched).to.equal('abc');
    expect(state.fetching).to.equal(false);
    expect(state.olderItemsOnServer).to.equal(true);
    expect(getStreamSize(state.stream)).to.equal(1);
    expect(takeFromStream(state.stream, 1)).to.eql([item]);

    const state2 = reducer(state, receivedNotification({
      id: 'def',
      date: '2018-01-02T00:00:00Z',
    }));

    expect(state2.lastItemFetched).to.equal('abc');
  });

});