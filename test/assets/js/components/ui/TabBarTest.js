const Dispatcher = require('Dispatcher');

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
    spy(Dispatcher, 'dispatch');

    it('dispatches navigate action', () => {
        let tabBar = <TabBar items={TabBarItems}/>;

        let instance = ReactTestUtils.renderIntoDocument(tabBar);
        ReactTestUtils.Simulate.click(instance.refs.Notifications.refs.li);

        assert(Dispatcher.dispatch.calledWith({
            type: 'navigate',
            path: '/notifications'
        }));
    });
});
