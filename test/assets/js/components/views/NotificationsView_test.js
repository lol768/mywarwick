import NotificationsView from 'components/views/NotificationsView';
import * as React from 'react';
import moment from 'moment';
import * as enzyme from 'enzyme';
import { mergeNotifications } from 'state/notifications';
import * as PropTypes from 'prop-types';

describe('NotificationsView', () => {

  it('only shows Older if it\'s not the only group', () => {
    const ungroupedProps = {
      notifications: mergeNotifications({}, [
          {
            id: '123',
            notification: true,
            provider: 'foo',
            type: 'foo',
            title: 'title',
            text: 'text',
            date: moment().subtract(1, 'months').toISOString()
          },
      ]),
      dispatch: () => {},
      notificationsLastRead: {
        fetched: true,
        date: moment(),
      },
      numberToShow: 20,
      isFiltered: false,
      isOnline: true,
    };
    const ungrouped = enzyme.render(<NotificationsView.WrappedComponent {...ungroupedProps} />);
    expect(ungrouped.find('.list-group')).to.have.length(0);

    const groupedProps = {
      notifications: mergeNotifications({}, [
        {
          id: '123',
          notification: true,
          provider: 'foo',
          type: 'foo',
          title: 'title',
          text: 'text',
          date: moment().subtract(1, 'months').toISOString()
        },
        {
          id: '234',
          notification: true,
          provider: 'foo',
          type: 'foo',
          title: 'title',
          text: 'text',
          date: moment().toISOString()
        }
      ]),
      dispatch: () => {},
      notificationsLastRead: {
        fetched: true,
        date: moment(),
      },
      numberToShow: 20,
      isFiltered: false,
      isOnline: true,
    };
    const grouped = enzyme.render(<NotificationsView.WrappedComponent {...groupedProps} />);
    expect(grouped.find('.list-group')).to.have.length(2);
  });

});
