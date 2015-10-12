const FluxStore = require('flux/lib/FluxStore');

const Dispatcher = require('../Dispatcher');

// Start the application at the requested URL
var path = window.location.pathname;

class ApplicationStore extends FluxStore {

    getCurrentPath() {
        return path;
    }

    __onDispatch(action) {
        switch (action.type) {
            case 'navigate':
                path = action.path;
                this.__emitChange();
                break;
            default:
            // no-op
        }
    }


}

export default new ApplicationStore(Dispatcher);