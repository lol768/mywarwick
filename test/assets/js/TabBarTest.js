const Dispatcher = require('Dispatcher');
sinon.spy(Dispatcher, "dispatch");

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

    it('dispatches navigate action', () => {
        let tabBar = <TabBar items={TabBarItems}/>;

        let instance = ReactTestUtils.renderIntoDocument(tabBar);
        ReactTestUtils.Simulate.click(instance.refs.Notifications.refs.li);

        expect(Dispatcher.dispatch.called).to.equal(true);
        expect(Dispatcher.dispatch.args[0][0]).to.eql({
            type: 'navigate',
            path: '/notifications'
        });
    });

});
