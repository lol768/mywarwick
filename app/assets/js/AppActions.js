const Dispatcher = require('./Dispatcher');

export default class AppActions {

    static navigate(path) {
        Dispatcher.dispatch({
            type: 'navigate',
            path: path
        });
    }

}
