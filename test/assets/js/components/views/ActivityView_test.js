import ActivityView from 'components/views/ActivityView';
import * as React from 'react';
import * as enzyme from 'enzyme';
import { mergeNotifications } from 'state/notifications';
import moment from 'moment';

describe('ActivityView', () => {

  it('only shows Older if it\'s not the only group', () => {
    const ungroupedProps = {
      activities: mergeNotifications({}, [
          {
            id: '123',
            notification: false,
            provider: 'foo',
            type: 'foo',
            title: 'title',
            text: 'text',
            date: moment().subtract(1, 'months').toISOString()
          },
      ]),
      dispatch: () => {},
    };
    const ungrouped = enzyme.render(<ActivityView.WrappedComponent {...ungroupedProps} />);
    expect(ungrouped.find('.list-group')).to.have.length(0);
    expect(ungrouped.find('.activity-item')).to.have.length(1);

    const groupedProps = {
      activities: mergeNotifications({}, [
        {
          id: '123',
          notification: false,
          provider: 'foo',
          type: 'foo',
          title: 'title',
          text: 'text',
          date: moment().subtract(1, 'months').toISOString()
        },
        {
          id: '234',
          notification: false,
          provider: 'foo',
          type: 'foo',
          title: 'title',
          text: 'text',
          date: moment().toISOString()
        }
      ]),
      dispatch: () => {},
    };
    const grouped = enzyme.render(<ActivityView.WrappedComponent {...groupedProps} />);
    expect(grouped.find('.activity-item')).to.have.length(2);
    expect(grouped.find('.list-group')).to.have.length(2);
  });

});