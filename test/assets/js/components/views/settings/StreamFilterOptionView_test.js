import { ActivityStreamFilterOptionView } from 'components/views/settings/StreamFilterOptionView';
import * as React from 'react';
import { mount } from 'enzyme';

describe('StreamFilterOptionView', () => {
  it('read correct data types from element dataset', () => {
    const coursesyncProviderId = 'coursesync';
    const props = {
      filterType: 'Alerts',
      filter: {},
      filterOptions: {
        provider: [
          { id: coursesyncProviderId }
        ],
      },
      saveFilter: () => {},
      isOnline: true,
    };

    const component = mount(<ActivityStreamFilterOptionView.WrappedComponent {...props} />);

    expect(component.state().provider[coursesyncProviderId]).to.equal(true);

    component.find(`[data-value="${coursesyncProviderId}"]`).first().simulate('click');

    expect(component.state().provider[coursesyncProviderId]).to.equal(false);
  });
});