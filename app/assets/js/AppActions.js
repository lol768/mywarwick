const Dispatcher = require('./Dispatcher');

export default class AppActions {

    static selectTab(tab) {
        Dispatcher.dispatch({
            type: 'select-tab',
            tab: tab
        });
    }

}
