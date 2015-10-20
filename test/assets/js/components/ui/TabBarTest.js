const TabBar = require('components/ui/TabBar');
const TabBarItem = require('components/ui/TabBarItem');

const TabBarItems = [
    {
        title: 'Notifications',
        icon: 'inbox',
        path: '/notifications'
    }
];

describe('TabBar', () => {
    it('calls onSelectItem', () => {
        let fn = sinon.spy();
        let tabBar = <TabBar items={TabBarItems} onSelectItem={fn}/>;

        let instance = ReactTestUtils.renderIntoDocument(tabBar);
        ReactTestUtils.Simulate.click(instance.refs.Notifications.refs.li);

        assert(fn.called);
    });
});
