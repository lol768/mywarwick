import React from 'react';
import TabBar from 'components/ui/TabBar';
import TabBarItem from 'components/ui/TabBarItem';

describe('TabBar', () => {
  it('calls onSelectItem', () => {
    let fn = sinon.spy();
    let tabBar = (
      <TabBar onSelectItem={fn}>
        <TabBarItem title="Me" icon="user" path="/"/>
        <TabBarItem title="Notifications" icon="inbox" path="/notifications"/>
      </TabBar>
    );

    let instance = ReactTestUtils.renderIntoDocument(tabBar);
    ReactTestUtils.Simulate.click(instance.refs.notifications.refs.li);

    assert(fn.called);
  });
});
