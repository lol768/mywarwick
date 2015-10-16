const Dispatcher = require('Dispatcher');

const AppActions = require('AppActions');

describe('AppActions', () => {
    spy(Dispatcher, 'dispatch');

    it('should dispatch navigate action', () => {
        AppActions.navigate('/test');

        assert(Dispatcher.dispatch.calledWith({
            type: 'navigate',
            path: '/test'
        }));
    });
});